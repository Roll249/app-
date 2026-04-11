package com.fintech.data.local.db.dao

import androidx.room.*
import com.fintech.data.local.db.entity.SyncEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Sync status operations
 */
@Dao
interface SyncDao {

    @Query("SELECT * FROM sync_status WHERE status = 'PENDING' ORDER BY createdAt ASC")
    fun getPendingSyncItems(): Flow<List<SyncEntity>>

    @Query("SELECT * FROM sync_status WHERE entityType = :entityType AND entityId = :entityId")
    suspend fun getSyncItem(entityType: String, entityId: String): SyncEntity?

    @Query("SELECT COUNT(*) FROM sync_status WHERE status = 'PENDING'")
    fun getPendingCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(syncEntity: SyncEntity)

    @Update
    suspend fun update(syncEntity: SyncEntity)

    @Query("UPDATE sync_status SET status = :status, syncedAt = :syncedAt WHERE entityType = :entityType AND entityId = :entityId")
    suspend fun updateStatus(entityType: String, entityId: String, status: String, syncedAt: Long)

    @Delete
    suspend fun delete(syncEntity: SyncEntity)

    @Query("DELETE FROM sync_status WHERE status = 'SYNCED' AND syncedAt < :olderThan")
    suspend fun deleteOldSynced(olderThan: Long)

    @Query("DELETE FROM sync_status")
    suspend fun deleteAll()
}
