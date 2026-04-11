package com.fintech.data.local.db.dao

import androidx.room.*
import com.fintech.data.local.db.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Category operations
 */
@Dao
interface CategoryDao {

    @Query("SELECT * FROM categories WHERE userId = :userId OR userId IS NULL AND isActive = 1 ORDER BY sortOrder ASC")
    fun getCategories(userId: String): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE (userId = :userId OR userId IS NULL) AND type = :type AND isActive = 1 ORDER BY sortOrder ASC")
    fun getCategoriesByType(userId: String, type: String): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :categoryId")
    suspend fun getCategoryById(categoryId: String): CategoryEntity?

    @Query("SELECT * FROM categories WHERE userId = :userId AND isActive = 1")
    fun getUserCategories(userId: String): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE userId IS NULL AND isActive = 1")
    fun getSystemCategories(): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: CategoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<CategoryEntity>)

    @Update
    suspend fun update(category: CategoryEntity)

    @Delete
    suspend fun delete(category: CategoryEntity)

    @Query("DELETE FROM categories WHERE userId = :userId")
    suspend fun deleteAllByUser(userId: String)

    @Query("DELETE FROM categories")
    suspend fun deleteAll()
}
