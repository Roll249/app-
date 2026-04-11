package com.fintech.data.local.db.dao

import androidx.room.*
import com.fintech.data.local.db.entity.AccountEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Account operations
 */
@Dao
interface AccountDao {

    @Query("SELECT * FROM accounts WHERE userId = :userId AND isActive = 1 ORDER BY createdAt ASC")
    fun getAccountsByUser(userId: String): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE id = :accountId")
    suspend fun getAccountById(accountId: String): AccountEntity?

    @Query("SELECT * FROM accounts WHERE userId = :userId AND isActive = 1 AND includeInTotal = 1")
    fun getAccountsForTotal(userId: String): Flow<List<AccountEntity>>

    @Query("SELECT SUM(currentBalance) FROM accounts WHERE userId = :userId AND isActive = 1 AND includeInTotal = 1")
    fun getTotalBalance(userId: String): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(account: AccountEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(accounts: List<AccountEntity>)

    @Update
    suspend fun update(account: AccountEntity)

    @Query("UPDATE accounts SET currentBalance = :newBalance, updatedAt = :updatedAt WHERE id = :accountId")
    suspend fun updateBalance(accountId: String, newBalance: Double, updatedAt: Long)

    @Delete
    suspend fun delete(account: AccountEntity)

    @Query("DELETE FROM accounts WHERE userId = :userId")
    suspend fun deleteAllByUser(userId: String)

    @Query("DELETE FROM accounts")
    suspend fun deleteAll()
}
