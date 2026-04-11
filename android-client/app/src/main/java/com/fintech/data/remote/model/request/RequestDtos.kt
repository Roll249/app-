package com.fintech.data.remote.model.request

import com.google.gson.annotations.SerializedName

/**
 * Auth Request DTOs
 */
data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class RegisterRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("fullName") val fullName: String? = null,
    @SerializedName("phone") val phone: String? = null
)

data class RefreshTokenRequest(
    @SerializedName("refreshToken") val refreshToken: String
)

/**
 * Account Request DTOs
 */
data class CreateAccountRequest(
    @SerializedName("name") val name: String,
    @SerializedName("type") val type: String,
    @SerializedName("icon") val icon: String? = null,
    @SerializedName("color") val color: String? = null,
    @SerializedName("initialBalance") val initialBalance: String = "0",
    @SerializedName("currency") val currency: String = "VND",
    @SerializedName("includeInTotal") val includeInTotal: Boolean = true
)

data class UpdateAccountRequest(
    @SerializedName("name") val name: String? = null,
    @SerializedName("icon") val icon: String? = null,
    @SerializedName("color") val color: String? = null,
    @SerializedName("includeInTotal") val includeInTotal: Boolean? = null
)

/**
 * Transaction Request DTOs
 */
data class CreateTransactionRequest(
    @SerializedName("accountId") val accountId: String,
    @SerializedName("categoryId") val categoryId: String? = null,
    @SerializedName("type") val type: String,
    @SerializedName("amount") val amount: String,
    @SerializedName("currency") val currency: String = "VND",
    @SerializedName("description") val description: String? = null,
    @SerializedName("note") val note: String? = null,
    @SerializedName("date") val date: String? = null,
    @SerializedName("sourceType") val sourceType: String? = null
)

/**
 * Category Request DTOs
 */
data class CreateCategoryRequest(
    @SerializedName("name") val name: String,
    @SerializedName("icon") val icon: String? = null,
    @SerializedName("color") val color: String? = null,
    @SerializedName("type") val type: String,
    @SerializedName("parentId") val parentId: String? = null
)

data class UpdateCategoryRequest(
    @SerializedName("name") val name: String? = null,
    @SerializedName("icon") val icon: String? = null,
    @SerializedName("color") val color: String? = null,
    @SerializedName("parentId") val parentId: String? = null,
    @SerializedName("sortOrder") val sortOrder: Int? = null
)

/**
 * Fund Request DTOs
 */
data class CreateFundRequest(
    @SerializedName("name") val name: String,
    @SerializedName("icon") val icon: String? = null,
    @SerializedName("color") val color: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("targetAmount") val targetAmount: String? = null,
    @SerializedName("startDate") val startDate: String? = null,
    @SerializedName("endDate") val endDate: String? = null
)

data class ContributeFundRequest(
    @SerializedName("amount") val amount: String,
    @SerializedName("type") val type: String,
    @SerializedName("accountId") val accountId: String? = null,
    @SerializedName("description") val description: String? = null
)

/**
 * Budget Request DTOs
 */
data class CreateBudgetRequest(
    @SerializedName("categoryId") val categoryId: String? = null,
    @SerializedName("name") val name: String,
    @SerializedName("amount") val amount: String,
    @SerializedName("period") val period: String = "MONTHLY",
    @SerializedName("startDate") val startDate: String? = null,
    @SerializedName("endDate") val endDate: String? = null
)

/**
 * Bank Request DTOs
 */
data class ConnectBankRequest(
    @SerializedName("bankId") val bankId: String,
    @SerializedName("accountNumber") val accountNumber: String,
    @SerializedName("accountHolderName") val accountHolderName: String
)

data class TransferRequest(
    @SerializedName("fromBankAccountId") val fromBankAccountId: String,
    @SerializedName("toAccountNumber") val toAccountNumber: String,
    @SerializedName("toBankCode") val toBankCode: String,
    @SerializedName("amount") val amount: String,
    @SerializedName("description") val description: String? = null
)

/**
 * QR Code Request DTOs
 */
data class GenerateQRRequest(
    @SerializedName("accountId") val accountId: String? = null,
    @SerializedName("bankAccountId") val bankAccountId: String? = null,
    @SerializedName("amount") val amount: String? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("bankCode") val bankCode: String? = null,
    @SerializedName("accountNumber") val accountNumber: String? = null,
    @SerializedName("expiresIn") val expiresIn: Int = 300
)

data class GenerateTransferQRRequest(
    @SerializedName("fromBankAccountId") val fromBankAccountId: String,
    @SerializedName("toBankCode") val toBankCode: String,
    @SerializedName("toAccountNumber") val toAccountNumber: String,
    @SerializedName("toAccountName") val toAccountName: String? = null,
    @SerializedName("amount") val amount: String? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("expiresIn") val expiresIn: Int = 300
)

data class ProcessQRRequest(
    @SerializedName("payload") val payload: String
)

data class ValidateQRRequest(
    @SerializedName("qrContent") val qrContent: String
)

/**
 * User Request DTOs
 */
data class UpdateUserRequest(
    @SerializedName("fullName") val fullName: String? = null,
    @SerializedName("avatarUrl") val avatarUrl: String? = null,
    @SerializedName("phone") val phone: String? = null
)

data class ChangePasswordRequest(
    @SerializedName("currentPassword") val currentPassword: String,
    @SerializedName("newPassword") val newPassword: String
)
