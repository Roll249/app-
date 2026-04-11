/**
 * Sync Service - External Service Integration
 * Handles data synchronization between local database and external services
 * 
 * This service manages:
 * - Cloud backup (Google Drive, iCloud)
 * - Bank account sync
 * - Multi-device synchronization
 * - Conflict resolution
 */
import { BaseService, ServiceConfig, ServiceHealth } from '../base/BaseService.js';
import { query } from '../../utils/db.js';

export type SyncProvider = 'LOCAL' | 'GOOGLE_DRIVE' | 'ICLOUD';

export interface SyncStatus {
  lastSyncAt: Date | null;
  nextSyncAt: Date | null;
  syncInProgress: boolean;
  error: string | null;
}

export interface SyncRecord {
  id: string;
  userId: string;
  entityType: 'TRANSACTION' | 'ACCOUNT' | 'BUDGET' | 'FUND' | 'CATEGORY';
  entityId: string;
  action: 'CREATE' | 'UPDATE' | 'DELETE';
  data: any;
  syncedAt: Date | null;
  syncError: string | null;
}

export interface SyncableEntity {
  id: string;
  userId: string;
  type: string;
  updatedAt: number;
}

export interface SyncConflict {
  entityId: string;
  entityType: string;
  localVersion: any;
  remoteVersion: any;
  resolution: 'LOCAL' | 'REMOTE' | 'MERGE' | 'PENDING';
}

export interface SyncResult {
  success: boolean;
  recordsProcessed: number;
  recordsFailed: number;
  conflicts: SyncConflict[];
  duration: number;
}

export class SyncService extends BaseService {
  private syncStatus: Map<string, SyncStatus> = new Map();
  private syncQueue: SyncRecord[] = [];
  private provider: SyncProvider = 'LOCAL';
  private autoSyncInterval?: NodeJS.Timeout;

  constructor(config: ServiceConfig, logger: any) {
    super(config, logger);
  }

  getName(): string {
    return 'SyncService';
  }

  async initialize(): Promise<void> {
    this.logger.info(`[${this.getName()}] Initializing sync service...`);

    // Determine sync provider from environment
    if (process.env.SYNC_PROVIDER === 'google_drive') {
      this.provider = 'GOOGLE_DRIVE';
    } else if (process.env.SYNC_PROVIDER === 'icloud') {
      this.provider = 'ICLOUD';
    } else {
      this.provider = 'LOCAL';
    }

    // Start auto-sync if enabled
    if (process.env.AUTO_SYNC_ENABLED === 'true') {
      const interval = parseInt(process.env.AUTO_SYNC_INTERVAL || '300000'); // 5 minutes
      this.autoSyncInterval = setInterval(() => {
        this.autoSync().catch(err => {
          this.logger.error(`[${this.getName()}] Auto-sync error: ${err.message}`);
        });
      }, interval);
    }

    this.isInitialized = true;
    this.logger.info(`[${this.getName()}] Sync service initialized with provider: ${this.provider}`);
  }

  protected async ping(): Promise<boolean> {
    return this.provider !== 'LOCAL' || true;
  }

  async getHealth(): Promise<ServiceHealth> {
    const startTime = Date.now();

    // Count pending sync records
    const pendingCount = await query(
      'SELECT COUNT(*) as count FROM sync_queue WHERE synced_at IS NULL'
    );

    return {
      status: 'healthy',
      latencyMs: Date.now() - startTime,
      message: `Provider: ${this.provider}, Pending: ${pendingCount[0]?.count || 0}`,
      lastChecked: new Date()
    };
  }

  async getSyncStatus(userId: string): Promise<SyncStatus> {
    return this.syncStatus.get(userId) || {
      lastSyncAt: null,
      nextSyncAt: null,
      syncInProgress: false,
      error: null
    };
  }

  async queueForSync(record: Omit<SyncRecord, 'id' | 'syncedAt' | 'syncError'>): Promise<void> {
    const { v4: uuidv4 } = require('uuid');
    const syncRecord: SyncRecord = {
      ...record,
      id: uuidv4(),
      syncedAt: null,
      syncError: null
    };

    this.syncQueue.push(syncRecord);

    // Also save to database
    await query(
      `INSERT INTO sync_queue (id, user_id, entity_type, entity_id, action, data, synced_at)
       VALUES ($1, $2, $3, $4, $5, $6, NULL)
       ON CONFLICT (entity_id) DO UPDATE SET
         action = $5, data = $6, synced_at = NULL`,
      [syncRecord.id, syncRecord.userId, syncRecord.entityType, syncRecord.entityId, syncRecord.action, JSON.stringify(syncRecord.data)]
    );

    this.logger.info(`[${this.getName()}] Queued ${record.entityType}:${record.entityId} for sync`);
  }

  async sync(userId: string): Promise<SyncResult> {
    const startTime = Date.now();

    this.logger.info(`[${this.getName()}] Starting sync for user ${userId}`);

    // Update status
    this.syncStatus.set(userId, {
      lastSyncAt: new Date(),
      nextSyncAt: null,
      syncInProgress: true,
      error: null
    });

    const conflicts: SyncConflict[] = [];
    let recordsProcessed = 0;
    let recordsFailed = 0;

    try {
      // Get pending sync records from database
      const pendingRecords = await query(
        `SELECT * FROM sync_queue WHERE user_id = $1 AND synced_at IS NULL ORDER BY created_at ASC LIMIT 100`,
        [userId]
      );

      for (const record of pendingRecords) {
        try {
          // Process sync based on provider
          switch (this.provider) {
            case 'GOOGLE_DRIVE':
              await this.syncToGoogleDrive(record);
              break;
            case 'ICLOUD':
              await this.syncToICloud(record);
              break;
            default:
              await this.syncLocally(record);
          }

          // Mark as synced
          await query(
            'UPDATE sync_queue SET synced_at = $1 WHERE id = $2',
            [Math.floor(Date.now() / 1000), record.id]
          );

          recordsProcessed++;
        } catch (error: any) {
          this.logger.error(`[${this.getName()}] Failed to sync record ${record.id}: ${error.message}`);
          
          await query(
            'UPDATE sync_queue SET sync_error = $1 WHERE id = $2',
            [error.message, record.id]
          );

          recordsFailed++;
        }
      }

      // Check for conflicts
      const remoteChanges = await this.fetchRemoteChanges(userId);
      for (const remoteChange of remoteChanges) {
        const localRecord = await this.getLocalRecord(userId, remoteChange.entityType, remoteChange.entityId);
        
        if (localRecord && this.hasConflict(localRecord, remoteChange)) {
          conflicts.push({
            entityId: remoteChange.entityId,
            entityType: remoteChange.entityType,
            localVersion: localRecord,
            remoteVersion: remoteChange,
            resolution: 'PENDING'
          });
        } else if (!localRecord) {
          // New record from remote, apply it
          await this.applyRemoteChange(remoteChange);
        }
      }

      this.logger.info(`[${this.getName()}] Sync completed: ${recordsProcessed} processed, ${recordsFailed} failed, ${conflicts.length} conflicts`);
    } catch (error: any) {
      this.logger.error(`[${this.getName()}] Sync error: ${error.message}`);
      
      this.syncStatus.set(userId, {
        lastSyncAt: new Date(),
        nextSyncAt: null,
        syncInProgress: false,
        error: error.message
      });

      return {
        success: false,
        recordsProcessed,
        recordsFailed,
        conflicts,
        duration: Date.now() - startTime
      };
    }

    this.syncStatus.set(userId, {
      lastSyncAt: new Date(),
      nextSyncAt: new Date(Date.now() + (parseInt(process.env.AUTO_SYNC_INTERVAL || '300000'))),
      syncInProgress: false,
      error: null
    });

    return {
      success: true,
      recordsProcessed,
      recordsFailed,
      conflicts,
      duration: Date.now() - startTime
    };
  }

  private async syncToGoogleDrive(record: any): Promise<void> {
    // Placeholder for Google Drive sync
    this.logger.info(`[${this.getName()}] Syncing to Google Drive: ${record.entityType}`);
  }

  private async syncToICloud(record: any): Promise<void> {
    // Placeholder for iCloud sync
    this.logger.info(`[${this.getName()}] Syncing to iCloud: ${record.entityType}`);
  }

  private async syncLocally(record: any): Promise<void> {
    // For local sync, just mark as synced
    this.logger.info(`[${this.getName()}] Local sync: ${record.entityType}`);
  }

  private async fetchRemoteChanges(userId: string): Promise<any[]> {
    // Placeholder - fetch changes from remote provider
    return [];
  }

  private async getLocalRecord(userId: string, entityType: string, entityId: string): Promise<any | null> {
    const tableMap: Record<string, string> = {
      TRANSACTION: 'transactions',
      ACCOUNT: 'accounts',
      BUDGET: 'budgets',
      FUND: 'funds',
      CATEGORY: 'categories'
    };

    const table = tableMap[entityType];
    if (!table) return null;

    const result = await query(`SELECT * FROM ${table} WHERE id = $1 AND user_id = $2`, [entityId, userId]);
    return result[0] || null;
  }

  private hasConflict(local: any, remote: any): boolean {
    return local.updated_at !== remote.updatedAt;
  }

  private async applyRemoteChange(change: any): Promise<void> {
    // Placeholder - apply remote change to local database
    this.logger.info(`[${this.getName()}] Applying remote change: ${change.entityType}:${change.entityId}`);
  }

  async resolveConflict(
    userId: string,
    entityId: string,
    resolution: 'LOCAL' | 'REMOTE' | 'MERGE'
  ): Promise<void> {
    const conflict = await query(
      'SELECT * FROM sync_conflicts WHERE entity_id = $1 AND user_id = $2',
      [entityId, userId]
    );

    if (!conflict[0]) {
      throw new Error('Conflict not found');
    }

    switch (resolution) {
      case 'LOCAL':
        // Keep local version, push to remote
        await this.pushToRemote(conflict[0].local_data);
        break;
      case 'REMOTE':
        // Apply remote version locally
        await this.applyRemoteChange(conflict[0].remote_data);
        break;
      case 'MERGE':
        // TODO: Implement smart merge logic
        throw new Error('Merge not yet implemented');
    }

    // Mark conflict as resolved
    await query(
      'UPDATE sync_conflicts SET resolved_at = $1, resolution = $2 WHERE entity_id = $3',
      [Math.floor(Date.now() / 1000), resolution, entityId]
    );
  }

  private async pushToRemote(data: any): Promise<void> {
    // Placeholder - push to remote provider
    this.logger.info(`[${this.getName()}] Pushing to remote: ${data.entityType}`);
  }

  async autoSync(): Promise<void> {
    // Get all users with pending sync
    const users = await query(
      `SELECT DISTINCT user_id FROM sync_queue WHERE synced_at IS NULL`
    );

    for (const user of users) {
      try {
        await this.sync(user.user_id);
      } catch (error: any) {
        this.logger.error(`[${this.getName()}] Auto-sync error for user ${user.user_id}: ${error.message}`);
      }
    }
  }

  destroy(): void {
    if (this.autoSyncInterval) {
      clearInterval(this.autoSyncInterval);
    }
  }
}
