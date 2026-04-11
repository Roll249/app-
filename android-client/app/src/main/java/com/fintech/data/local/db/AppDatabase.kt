package com.fintech.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.fintech.data.local.db.converter.DateConverter
import com.fintech.data.local.db.entity.*
import com.fintech.data.local.db.dao.*

/**
 * Room Database for local storage
 *
 * Provides offline-first capability with local caching of server data.
 */
@Database(
    entities = [
        AccountEntity::class,
        TransactionEntity::class,
        CategoryEntity::class,
        FundEntity::class,
        BudgetEntity::class,
        SyncEntity::class,
        UserBankAccountEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    // DAOs
    abstract fun accountDao(): AccountDao
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun fundDao(): FundDao
    abstract fun budgetDao(): BudgetDao
    abstract fun syncDao(): SyncDao
    abstract fun userBankAccountDao(): UserBankAccountDao
}
