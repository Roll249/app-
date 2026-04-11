package com.fintech.domain.model

/**
 * Domain model for Account
 */
data class Account(
    val id: String,
    val userId: String,
    val name: String,
    val type: AccountType,
    val icon: String?,
    val color: String?,
    val initialBalance: Double,
    val currentBalance: Double,
    val currency: String,
    val includeInTotal: Boolean,
    val isActive: Boolean,
    val bankAccountId: String?,
    val createdAt: Long,
    val updatedAt: Long
)

enum class AccountType {
    CASH, BANK, WALLET, SAVINGS
}

/**
 * Domain model for Transaction
 */
data class Transaction(
    val id: String,
    val userId: String,
    val accountId: String,
    val accountName: String?,
    val categoryId: String?,
    val categoryName: String?,
    val categoryIcon: String?,
    val categoryColor: String?,
    val type: TransactionType,
    val amount: Double,
    val currency: String,
    val description: String?,
    val note: String?,
    val date: Long,
    val sourceType: String?,
    val referenceId: String?,
    val relatedTransactionId: String?,
    val createdAt: Long
)

enum class TransactionType {
    INCOME, EXPENSE, TRANSFER
}

/**
 * Domain model for Category
 */
data class Category(
    val id: String,
    val userId: String?,
    val name: String,
    val icon: String?,
    val color: String?,
    val type: CategoryType,
    val parentId: String?,
    val isSystem: Boolean,
    val isActive: Boolean,
    val sortOrder: Int
)

enum class CategoryType {
    INCOME, EXPENSE
}

/**
 * Domain model for Fund
 */
data class Fund(
    val id: String,
    val userId: String,
    val name: String,
    val icon: String?,
    val color: String?,
    val description: String?,
    val targetAmount: Double?,
    val currentAmount: Double,
    val progress: Double,
    val startDate: Long?,
    val endDate: Long?,
    val isActive: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)

/**
 * Domain model for Budget
 */
data class Budget(
    val id: String,
    val userId: String,
    val categoryId: String?,
    val categoryName: String?,
    val name: String,
    val amount: Double,
    val spentAmount: Double,
    val remainingAmount: Double,
    val progress: Double,
    val period: BudgetPeriod,
    val startDate: Long,
    val endDate: Long?,
    val isActive: Boolean,
    val createdAt: Long
)

enum class BudgetPeriod {
    WEEKLY, MONTHLY, YEARLY
}

/**
 * Domain model for Bank
 */
data class Bank(
    val id: String,
    val code: String,
    val name: String,
    val shortName: String?,
    val logoUrl: String?,
    val vietqrPrefix: String?,
    val swiftCode: String?
)

/**
 * Domain model for User Bank Account
 */
data class UserBankAccount(
    val id: String,
    val userId: String,
    val bank: Bank,
    val accountNumber: String,
    val accountHolderName: String,
    val balance: Double,
    val isActive: Boolean,
    val linkedAt: Long
)

/**
 * Domain model for User
 */
data class User(
    val id: String,
    val email: String,
    val fullName: String?,
    val avatarUrl: String?,
    val phone: String?,
    val isVerified: Boolean,
    val createdAt: Long
)

/**
 * Domain model for Financial Summary
 */
data class FinancialSummary(
    val totalBalance: Double,
    val totalIncome: Double,
    val totalExpense: Double,
    val netSavings: Double,
    val accountCount: Int,
    val transactionCount: Int
)

/**
 * Domain model for Category Amount (for reports)
 */
data class CategoryAmount(
    val categoryId: String,
    val categoryName: String,
    val categoryIcon: String?,
    val categoryColor: String?,
    val amount: Double,
    val percentage: Double
)
