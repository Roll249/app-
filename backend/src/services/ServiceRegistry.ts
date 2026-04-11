/**
 * Service Registry - Central Service Manager
 * 
 * This registry manages all external services and provides:
 * - Service initialization
 * - Health checks
 * - Access to individual services
 */
import { AIService } from './ai/AIService.js';
import { BankingService } from './banking/BankingService.js';
import { NotificationService } from './notification/NotificationService.js';
import { SyncService } from './sync/SyncService.js';
import { MarketService } from './market/MarketService.js';
import { InvestmentService } from './investment/InvestmentService.js';
import { IService, ServiceHealth } from './base/BaseService.js';

export interface ServiceInfo {
  name: string;
  status: 'initialized' | 'error' | 'not_initialized';
  health: ServiceHealth | null;
  error?: string;
}

export class ServiceRegistry {
  private services: Map<string, IService> = new Map();
  private logger: any;
  private initialized: boolean = false;

  constructor(logger: any) {
    this.logger = logger;
  }

  getName(): string {
    return 'ServiceRegistry';
  }

  async initialize(): Promise<void> {
    if (this.initialized) {
      this.logger.warn('[ServiceRegistry] Already initialized');
      return;
    }

    this.logger.info('[ServiceRegistry] Initializing all services...');

    // Initialize AI Service
    const aiService = new AIService(
      {
        baseUrl: process.env.OLLAMA_URL || 'http://localhost:11434',
        timeout: 30000,
        retryAttempts: 3
      },
      this.logger
    );
    await aiService.initialize();
    this.services.set('ai', aiService);

    // Initialize Banking Service
    const bankingService = new BankingService(
      {
        baseUrl: process.env.BANKING_API_URL || 'http://localhost:3000',
        timeout: 10000,
        retryAttempts: 2
      },
      this.logger
    );
    await bankingService.initialize();
    this.services.set('banking', bankingService);

    // Initialize Notification Service
    const notificationService = new NotificationService(
      {
        baseUrl: '',
        timeout: 5000,
        retryAttempts: 2
      },
      this.logger
    );
    await notificationService.initialize();
    this.services.set('notification', notificationService);

    // Initialize Sync Service
    const syncService = new SyncService(
      {
        baseUrl: '',
        timeout: 30000,
        retryAttempts: 3
      },
      this.logger
    );
    await syncService.initialize();
    this.services.set('sync', syncService);

    // Initialize Market Service
    const marketService = new MarketService(
      {
        baseUrl: '',
        timeout: 10000,
        retryAttempts: 2
      },
      this.logger
    );
    await marketService.initialize();
    this.services.set('market', marketService);

    // Initialize Investment Service
    const investmentService = new InvestmentService(
      {
        baseUrl: '',
        timeout: 10000,
        retryAttempts: 2
      },
      this.logger
    );
    await investmentService.initialize();
    this.services.set('investment', investmentService);

    this.initialized = true;
    this.logger.info('[ServiceRegistry] All services initialized');
  }

  getService<T extends IService>(name: string): T | null {
    const service = this.services.get(name);
    return service as T | null;
  }

  getAIService(): AIService {
    return this.getService<AIService>('ai')!;
  }

  getBankingService(): BankingService {
    return this.getService<BankingService>('banking')!;
  }

  getNotificationService(): NotificationService {
    return this.getService<NotificationService>('notification')!;
  }

  getSyncService(): SyncService {
    return this.getService<SyncService>('sync')!;
  }

  getMarketService(): MarketService {
    return this.getService<MarketService>('market')!;
  }

  getInvestmentService(): InvestmentService {
    return this.getService<InvestmentService>('investment')!;
  }

  async getAllServicesStatus(): Promise<ServiceInfo[]> {
    const statuses: ServiceInfo[] = [];

    for (const [name, service] of this.services) {
      try {
        const health = await service.getHealth();
        statuses.push({
          name: service.getName(),
          status: 'initialized',
          health
        });
      } catch (error: any) {
        statuses.push({
          name: service.getName(),
          status: 'error',
          health: null,
          error: error.message
        });
      }
    }

    return statuses;
  }

  async getHealthSummary(): Promise<{
    overall: 'healthy' | 'degraded' | 'offline';
    services: ServiceInfo[];
    lastChecked: Date;
  }> {
    const services = await this.getAllServicesStatus();
    const healthyCount = services.filter(s => s.health?.status === 'healthy').length;
    const totalCount = services.length;

    let overall: 'healthy' | 'degraded' | 'offline';
    if (healthyCount === totalCount) {
      overall = 'healthy';
    } else if (healthyCount > 0) {
      overall = 'degraded';
    } else {
      overall = 'offline';
    }

    return {
      overall,
      services,
      lastChecked: new Date()
    };
  }

  isInitialized(): boolean {
    return this.initialized;
  }
}

// Singleton instance
let registry: ServiceRegistry | null = null;

export function getServiceRegistry(logger: any): ServiceRegistry {
  if (!registry) {
    registry = new ServiceRegistry(logger);
  }
  return registry;
}

export function resetServiceRegistry(): void {
  registry = null;
}
