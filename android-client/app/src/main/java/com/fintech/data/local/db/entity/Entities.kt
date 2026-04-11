package com.fintech.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Account Entity - represents a financial account (cash, bank, wallet, savings)
 */
@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val name: String,
    val type: String,  // CASH, BANK, WALLET, SAVINGS
    val icon: String?,
    val color: String?,
    val initialBalance: Double,
    val currentBalance: Double,
    val currency: String = "VND",
    val includeInTotal: Boolean = true,
    val isActive: Boolean = true,
    val bankAccountId: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
    val isSynced: Boolean = true
)

/**
 * Transaction Entity - represents a financial transaction
 */
@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val accountId: String,
    val accountName: String?,
    val categoryId: String?,
    val categoryName: String?,
    val categoryIcon: String?,
    val categoryColor: String?,
    val type: String,  // INCOME, EXPENSE, TRANSFER
    val amount: Double,
    val currency: String = "VND",
    val description: String?,
    val note: String?,
    val date: Long,
    val sourceType: String?,  // SALARY, BANK_TRANSFER, QR_PAYMENT, CASH
    val referenceId: String?,
    val relatedTransactionId: String?,
    val createdAt: Long,
    val isSynced: Boolean = true
)

/**
 * Category Entity - represents a transaction category
 */
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey
    val id: String,
    val userId: String?,
    val name: String,
    val icon: String?,
    val color: String?,
    val type: String,  // INCOME, EXPENSE
    val parentId: String?,
    val isSystem: Boolean = false,
    val isActive: Boolean = true,
    val sortOrder: Int = 0,
    val createdAt: Long
)

/**
 * Fund Entity - represents a savings goal/fund
 */
@Entity(tableName = "funds")
data class FundEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val name: String,
    val icon: String?,
    val color: String?,
    val description: String?,
    val targetAmount: Double?,
    val currentAmount: Double,
    val startDate: String?,
    val endDate: String?,
    val isActive: Boolean = true,
    val createdAt: Long,
    val updatedAt: Long,
    val isSynced: Boolean = true
)

/**
 * Budget Entity - represents a spending budget
 */
@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val categoryId: String?,
    val categoryName: String?,
    val name: String,
    val amount: Double,
    val spentAmount: Double,
    val period: String,  // WEEKLY, MONTHLY, YEARLY
    val startDate: String,
    val endDate: String?,
    val isActive: Boolean = true,
    val createdAt: Long,
    val isSynced: Boolean = true
)

/**
 * Sync Entity - tracks sync status for offline-first functionality
 */
@Entity(tableName = "sync_status")
data class SyncEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val entityType: String,  // TRANSACTION, ACCOUNT, etc.
    val entityId: String,
    val action: String,  // CREATE, UPDATE, DELETE
    val status: String,  // PENDING, SYNCED, FAILED
    val errorMessage: String?,
    val createdAt: Long,
    val syncedAt: Long?
)

/**
 * UserBankAccount Entity - represents a linked bank account
 */
@Entity(tableName = "user_bank_accounts")
data class UserBankAccountEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val bankId: String,
    val bankName: String,
    val bankCode: String,
    val bankLogoUrl: String?,
    val accountNumber: String,
    val accountHolderName: String,
    val balance: Double = 0.0,
    val isActive: Boolean = true,
    val linkedAt: Long,
    val updatedAt: Long,
    val isSynced: Boolean = false
)
