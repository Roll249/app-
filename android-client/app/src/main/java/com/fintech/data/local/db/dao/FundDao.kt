package com.fintech.data.local.db.dao

import androidx.room.*
import com.fintech.data.local.db.entity.FundEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Fund operations
 */
@Dao
interface FundDao {

    @Query("SELECT * FROM funds WHERE userId = :userId AND isActive = 1 ORDER BY createdAt ASC")
    fun getFundsByUser(userId: String): Flow<List<FundEntity>>

    @Query("SELECT * FROM funds WHERE id = :fundId")
    suspend fun getFundById(fundId: String): FundEntity?

    @Query("SELECT SUM(currentAmount) FROM funds WHERE userId = :userId AND isActive = 1")
    fun getTotalFundAmount(userId: String): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(fund: FundEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(funds: List<FundEntity>)

    @Update
    suspend fun update(fund: FundEntity)

    @Query("UPDATE funds SET currentAmount = :newAmount, updatedAt = :updatedAt WHERE id = :fundId")
    suspend fun updateAmount(fundId: String, newAmount: Double, updatedAt: Long)

    @Delete
    suspend fun delete(fund: FundEntity)

    @Query("DELETE FROM funds WHERE userId = :userId")
    suspend fun deleteAllByUser(userId: String)

    @Query("DELETE FROM funds")
    suspend fun deleteAll()
}
