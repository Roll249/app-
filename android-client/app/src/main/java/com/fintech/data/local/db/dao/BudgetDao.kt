package com.fintech.data.local.db.dao

import androidx.room.*
import com.fintech.data.local.db.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Budget operations
 */
@Dao
interface BudgetDao {

    @Query("SELECT * FROM budgets WHERE userId = :userId AND isActive = 1 ORDER BY createdAt DESC")
    fun getBudgetsByUser(userId: String): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE id = :budgetId")
    suspend fun getBudgetById(budgetId: String): BudgetEntity?

    @Query("SELECT * FROM budgets WHERE userId = :userId AND period = :period AND isActive = 1")
    fun getBudgetsByPeriod(userId: String, period: String): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE userId = :userId AND categoryId = :categoryId AND isActive = 1")
    fun getBudgetsByCategory(userId: String, categoryId: String): Flow<List<BudgetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(budget: BudgetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(budgets: List<BudgetEntity>)

    @Update
    suspend fun update(budget: BudgetEntity)

    @Query("UPDATE budgets SET spentAmount = :newSpentAmount WHERE id = :budgetId")
    suspend fun updateSpentAmount(budgetId: String, newSpentAmount: Double)

    @Delete
    suspend fun delete(budget: BudgetEntity)

    @Query("DELETE FROM budgets WHERE userId = :userId")
    suspend fun deleteAllByUser(userId: String)

    @Query("DELETE FROM budgets")
    suspend fun deleteAll()
}
