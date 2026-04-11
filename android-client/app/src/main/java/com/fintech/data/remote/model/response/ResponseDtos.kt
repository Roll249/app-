package com.fintech.data.remote.model.response

import com.google.gson.annotations.SerializedName

/**
 * Auth DTOs
 */
data class AuthResponse(
    @SerializedName("accessToken") val accessToken: String,
    @SerializedName("refreshToken") val refreshToken: String,
    @SerializedName("tokenType") val tokenType: String,
    @SerializedName("expiresIn") val expiresIn: Long,
    @SerializedName("user") val user: UserDto
)

data class UserDto(
    @SerializedName("id") val id: String,
    @SerializedName("email") val email: String,
    @SerializedName("fullName") val fullName: String?,
    @SerializedName("avatarUrl") val avatarUrl: String?,
    @SerializedName("phone") val phone: String?,
    @SerializedName("isVerified") val isVerified: Boolean,
    @SerializedName("createdAt") val createdAt: String
)

/**
 * Account DTOs
 */
data class AccountDto(
    @SerializedName("id") val id: String,
    @SerializedName("userId") val userId: String,
    @SerializedName("name") val name: String,
    @SerializedName("type") val type: String,
    @SerializedName("icon") val icon: String?,
    @SerializedName("color") val color: String?,
    @SerializedName("initialBalance") val initialBalance: String,
    @SerializedName("currentBalance") val currentBalance: String,
    @SerializedName("currency") val currency: String,
    @SerializedName("includeInTotal") val includeInTotal: Boolean,
    @SerializedName("isActive") val isActive: Boolean,
    @SerializedName("bankAccountId") val bankAccountId: String?,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String
)

/**
 * Transaction DTOs
 */
data class TransactionDto(
    @SerializedName("id") val id: String,
    @SerializedName("userId") val userId: String,
    @SerializedName("accountId") val accountId: String,
    @SerializedName("accountName") val accountName: String?,
    @SerializedName("categoryId") val categoryId: String?,
    @SerializedName("categoryName") val categoryName: String?,
    @SerializedName("categoryIcon") val categoryIcon: String?,
    @SerializedName("categoryColor") val categoryColor: String?,
    @SerializedName("type") val type: String,
    @SerializedName("amount") val amount: String,
    @SerializedName("currency") val currency: String,
    @SerializedName("description") val description: String?,
    @SerializedName("note") val note: String?,
    @SerializedName("date") val date: String,
    @SerializedName("sourceType") val sourceType: String?,
    @SerializedName("referenceId") val referenceId: String?,
    @SerializedName("relatedTransactionId") val relatedTransactionId: String?,
    @SerializedName("createdAt") val createdAt: String
)

/**
 * Category DTOs
 */
data class CategoryDto(
    @SerializedName("id") val id: String,
    @SerializedName("userId") val userId: String?,
    @SerializedName("name") val name: String,
    @SerializedName("icon") val icon: String?,
    @SerializedName("color") val color: String?,
    @SerializedName("type") val type: String,
    @SerializedName("parentId") val parentId: String?,
    @SerializedName("isSystem") val isSystem: Boolean,
    @SerializedName("isActive") val isActive: Boolean,
    @SerializedName("sortOrder") val sortOrder: Int,
    @SerializedName("createdAt") val createdAt: String
)

/**
 * Fund DTOs
 */
data class FundDto(
    @SerializedName("id") val id: String,
    @SerializedName("userId") val userId: String,
    @SerializedName("name") val name: String,
    @SerializedName("icon") val icon: String?,
    @SerializedName("color") val color: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("targetAmount") val targetAmount: String?,
    @SerializedName("currentAmount") val currentAmount: String,
    @SerializedName("progress") val progress: Double,
    @SerializedName("startDate") val startDate: String?,
    @SerializedName("endDate") val endDate: String?,
    @SerializedName("isActive") val isActive: Boolean,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String
)

/**
 * Budget DTOs
 */
data class BudgetDto(
    @SerializedName("id") val id: String,
    @SerializedName("userId") val userId: String,
    @SerializedName("categoryId") val categoryId: String?,
    @SerializedName("categoryName") val categoryName: String?,
    @SerializedName("name") val name: String,
    @SerializedName("amount") val amount: String,
    @SerializedName("spentAmount") val spentAmount: String,
    @SerializedName("remainingAmount") val remainingAmount: String,
    @SerializedName("progress") val progress: Double,
    @SerializedName("period") val period: String,
    @SerializedName("startDate") val startDate: String,
    @SerializedName("endDate") val endDate: String?,
    @SerializedName("isActive") val isActive: Boolean,
    @SerializedName("createdAt") val createdAt: String
)

/**
 * Bank DTOs
 */
data class BankDto(
    @SerializedName("id") val id: String,
    @SerializedName("code") val code: String,
    @SerializedName("name") val name: String,
    @SerializedName("shortName") val shortName: String?,
    @SerializedName("logoUrl") val logoUrl: String?,
    @SerializedName("vietqrPrefix") val vietqrPrefix: String?,
    @SerializedName("swiftCode") val swiftCode: String?
)

data class UserBankAccountDto(
    @SerializedName("id") val id: String,
    @SerializedName("userId") val userId: String,
    @SerializedName("bank") val bank: BankDto,
    @SerializedName("accountNumber") val accountNumber: String,
    @SerializedName("accountHolderName") val accountHolderName: String,
    @SerializedName("balance") val balance: String,
    @SerializedName("isActive") val isActive: Boolean,
    @SerializedName("linkedAt") val linkedAt: String
)

/**
 * QR Code DTOs
 */
data class QRCodeResponse(
    @SerializedName("id") val id: String,
    @SerializedName("type") val type: String,
    @SerializedName("qrImage") val qrImage: String,
    @SerializedName("qrContent") val qrContent: String,
    @SerializedName("payload") val payload: String?,
    @SerializedName("signature") val signature: String?,
    @SerializedName("amount") val amount: String?,
    @SerializedName("message") val message: String?,
    @SerializedName("expiresAt") val expiresAt: String,
    @SerializedName("createdAt") val createdAt: String
)

/**
 * Report DTOs
 */
data class FinancialSummary(
    @SerializedName("totalBalance") val totalBalance: String,
    @SerializedName("totalIncome") val totalIncome: String,
    @SerializedName("totalExpense") val totalExpense: String,
    @SerializedName("netSavings") val netSavings: String,
    @SerializedName("accountCount") val accountCount: Int,
    @SerializedName("transactionCount") val transactionCount: Int,
    @SerializedName("categoryBreakdown") val categoryBreakdown: List<CategoryAmount>?
)

data class CategoryAmount(
    @SerializedName("categoryId") val categoryId: String,
    @SerializedName("categoryName") val categoryName: String,
    @SerializedName("categoryIcon") val categoryIcon: String?,
    @SerializedName("categoryColor") val categoryColor: String?,
    @SerializedName("amount") val amount: String,
    @SerializedName("percentage") val percentage: Double
)

data class IncomeExpenseReport(
    @SerializedName("period") val period: String,
    @SerializedName("totalIncome") val totalIncome: String,
    @SerializedName("totalExpense") val totalExpense: String,
    @SerializedName("netAmount") val netAmount: String,
    @SerializedName("incomeByCategory") val incomeByCategory: List<CategoryAmount>,
    @SerializedName("expenseByCategory") val expenseByCategory: List<CategoryAmount>
)

data class TrendReport(
    @SerializedName("period") val period: String,
    @SerializedName("months") val months: Int,
    @SerializedName("income") val income: List<TrendItem>,
    @SerializedName("expense") val expense: List<TrendItem>,
    @SerializedName("savings") val savings: List<TrendItem>
)

data class TrendItem(
    @SerializedName("date") val date: String,
    @SerializedName("amount") val amount: String
)
