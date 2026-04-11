package com.fintech.data.local.db.dao

import androidx.room.*
import com.fintech.data.local.db.entity.UserBankAccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserBankAccountDao {

    @Query("SELECT * FROM user_bank_accounts WHERE userId = :userId AND isActive = 1 ORDER BY linkedAt DESC")
    fun getLinkedBanks(userId: String): Flow<List<UserBankAccountEntity>>

    @Query("SELECT * FROM user_bank_accounts WHERE id = :id")
    suspend fun getById(id: String): UserBankAccountEntity?

    @Query("SELECT * FROM user_bank_accounts WHERE userId = :userId AND accountNumber = :accountNumber AND bankId = :bankId AND isActive = 1")
    suspend fun findByAccountAndBank(userId: String, accountNumber: String, bankId: String): UserBankAccountEntity?

    @Query("SELECT SUM(balance) FROM user_bank_accounts WHERE userId = :userId AND isActive = 1")
    fun getTotalBalance(userId: String): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bankAccount: UserBankAccountEntity)

    @Update
    suspend fun update(bankAccount: UserBankAccountEntity)

    @Query("UPDATE user_bank_accounts SET balance = :balance, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateBalance(id: String, balance: Double, updatedAt: Long)

    @Query("UPDATE user_bank_accounts SET isActive = 0, updatedAt = :updatedAt WHERE id = :id")
    suspend fun deactivate(id: String, updatedAt: Long)

    @Query("DELETE FROM user_bank_accounts WHERE id = :id")
    suspend fun delete(id: String)

    @Query("SELECT COUNT(*) FROM user_bank_accounts WHERE userId = :userId AND isActive = 1")
    suspend fun getLinkedCount(userId: String): Int
}
