package com.fintech.data.remote.api.services

import com.fintech.data.remote.model.ApiResponse
import com.fintech.data.remote.model.PaginatedResponse
import com.fintech.data.remote.model.request.*
import com.fintech.data.remote.model.response.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Auth API Service
 */
interface AuthApi {

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<AuthResponse>>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<AuthResponse>>

    @POST("auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<ApiResponse<AuthResponse>>

    @POST("auth/logout")
    suspend fun logout(@Body request: RefreshTokenRequest): Response<ApiResponse<Unit>>
}

/**
 * User API Service
 */
interface UserApi {

    @GET("users/me")
    suspend fun getCurrentUser(): Response<ApiResponse<UserDto>>

    @PUT("users/me")
    suspend fun updateUser(@Body request: UpdateUserRequest): Response<ApiResponse<UserDto>>

    @PUT("users/password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<ApiResponse<Unit>>
}

/**
 * Account API Service
 */
interface AccountApi {

    @GET("accounts")
    suspend fun getAccounts(): Response<ApiResponse<List<AccountDto>>>

    @POST("accounts")
    suspend fun createAccount(@Body request: CreateAccountRequest): Response<ApiResponse<AccountDto>>

    @GET("accounts/{id}")
    suspend fun getAccount(@Path("id") id: String): Response<ApiResponse<AccountDto>>

    @PUT("accounts/{id}")
    suspend fun updateAccount(
        @Path("id") id: String,
        @Body request: UpdateAccountRequest
    ): Response<ApiResponse<AccountDto>>

    @DELETE("accounts/{id}")
    suspend fun deleteAccount(@Path("id") id: String): Response<ApiResponse<Unit>>
}

/**
 * Transaction API Service
 */
interface TransactionApi {

    @GET("transactions")
    suspend fun getTransactions(
        @Query("accountId") accountId: String? = null,
        @Query("categoryId") categoryId: String? = null,
        @Query("type") type: String? = null,
        @Query("startDate") startDate: Long? = null,
        @Query("endDate") endDate: Long? = null,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20
    ): Response<PaginatedResponse<TransactionDto>>

    @GET("transactions/recent")
    suspend fun getRecentTransactions(
        @Query("limit") limit: Int = 10
    ): Response<ApiResponse<List<TransactionDto>>>

    @POST("transactions")
    suspend fun createTransaction(
        @Body request: CreateTransactionRequest
    ): Response<ApiResponse<TransactionDto>>

    @GET("transactions/{id}")
    suspend fun getTransaction(@Path("id") id: String): Response<ApiResponse<TransactionDto>>

    @DELETE("transactions/{id}")
    suspend fun deleteTransaction(@Path("id") id: String): Response<ApiResponse<Unit>>
}

/**
 * Category API Service
 */
interface CategoryApi {

    @GET("categories")
    suspend fun getCategories(
        @Query("type") type: String? = null
    ): Response<ApiResponse<List<CategoryDto>>>

    @POST("categories")
    suspend fun createCategory(
        @Body request: CreateCategoryRequest
    ): Response<ApiResponse<CategoryDto>>

    @GET("categories/{id}")
    suspend fun getCategory(@Path("id") id: String): Response<ApiResponse<CategoryDto>>

    @PUT("categories/{id}")
    suspend fun updateCategory(
        @Path("id") id: String,
        @Body request: UpdateCategoryRequest
    ): Response<ApiResponse<CategoryDto>>

    @DELETE("categories/{id}")
    suspend fun deleteCategory(@Path("id") id: String): Response<ApiResponse<Unit>>
}

/**
 * Fund API Service
 */
interface FundApi {

    @GET("funds")
    suspend fun getFunds(): Response<ApiResponse<List<FundDto>>>

    @POST("funds")
    suspend fun createFund(@Body request: CreateFundRequest): Response<ApiResponse<FundDto>>

    @GET("funds/{id}")
    suspend fun getFund(@Path("id") id: String): Response<ApiResponse<FundDto>>

    @POST("funds/{id}/contribute")
    suspend fun contributeToFund(
        @Path("id") id: String,
        @Body request: ContributeFundRequest
    ): Response<ApiResponse<FundDto>>

    @DELETE("funds/{id}")
    suspend fun deleteFund(@Path("id") id: String): Response<ApiResponse<Unit>>
}

/**
 * Budget API Service
 */
interface BudgetApi {

    @GET("budgets")
    suspend fun getBudgets(): Response<ApiResponse<List<BudgetDto>>>

    @POST("budgets")
    suspend fun createBudget(@Body request: CreateBudgetRequest): Response<ApiResponse<BudgetDto>>

    @GET("budgets/{id}")
    suspend fun getBudget(@Path("id") id: String): Response<ApiResponse<BudgetDto>>

    @PUT("budgets/{id}")
    suspend fun updateBudget(
        @Path("id") id: String,
        @Body request: CreateBudgetRequest
    ): Response<ApiResponse<BudgetDto>>

    @DELETE("budgets/{id}")
    suspend fun deleteBudget(@Path("id") id: String): Response<ApiResponse<Unit>>
}

/**
 * Bank API Service
 */
interface BankApi {

    @GET("banks")
    suspend fun getBanks(): Response<ApiResponse<List<BankDto>>>

    @GET("banks/{code}")
    suspend fun getBankByCode(@Path("code") code: String): Response<ApiResponse<BankDto>>

    @GET("banks/my-accounts")
    suspend fun getMyBankAccounts(): Response<ApiResponse<List<UserBankAccountDto>>>

    @POST("banks/connect")
    suspend fun connectBank(@Body request: ConnectBankRequest): Response<ApiResponse<UserBankAccountDto>>

    @DELETE("banks/disconnect/{id}")
    suspend fun disconnectBank(@Path("id") id: String): Response<ApiResponse<Unit>>

    @POST("banks/transfer")
    suspend fun transfer(@Body request: TransferRequest): Response<ApiResponse<TransferResult>>
}

data class TransferResult(
    val success: Boolean,
    val transactionId: String?,
    val message: String,
    val fromBalance: String?,
    val toBalance: String?
)

/**
 * QR Code API Service
 */
interface QRCodeApi {

    @POST("qr/generate")
    suspend fun generateReceiveQR(@Body request: GenerateQRRequest): Response<ApiResponse<QRCodeResponse>>

    @POST("qr/generate-transfer")
    suspend fun generateTransferQR(@Body request: GenerateTransferQRRequest): Response<ApiResponse<QRCodeResponse>>

    @POST("qr/process")
    suspend fun processQR(@Body request: ProcessQRRequest): Response<ApiResponse<QRPaymentResult>>

    @POST("qr/validate")
    suspend fun validateQR(@Body request: ValidateQRRequest): Response<ApiResponse<QRValidationResult>>
}

data class QRPaymentResult(
    val processed: Boolean,
    val type: String,
    val amount: Double,
    val message: String?,
    val transactionId: String?,
    val timestamp: String
)

data class QRValidationResult(
    val valid: Boolean,
    val format: String,
    val amount: Double?,
    val message: String?,
    val expiresIn: Int?,
    val usedAt: String?
)

/**
 * Report API Service
 */
interface ReportApi {

    @GET("reports/summary")
    suspend fun getFinancialSummary(): Response<ApiResponse<FinancialSummary>>

    @GET("reports/income-expense")
    suspend fun getIncomeExpenseReport(
        @Query("period") period: String = "MONTHLY",
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null
    ): Response<ApiResponse<IncomeExpenseReport>>

    @GET("reports/by-category")
    suspend fun getCategoryReport(
        @Query("type") type: String = "EXPENSE",
        @Query("limit") limit: Int = 10
    ): Response<ApiResponse<List<CategoryAmount>>>

    @GET("reports/trend")
    suspend fun getTrendReport(
        @Query("months") months: Int = 6
    ): Response<ApiResponse<TrendReport>>
}

/**
 * AI API Service
 */
interface AIApi {

    @GET("ai/status")
    suspend fun getAIStatus(): Response<ApiResponse<AIStatusResponse>>

    @POST("ai/chat")
    suspend fun chat(
        @Body request: AIChatRequest
    ): Response<ApiResponse<AIChatResponse>>

    @POST("ai/chat/log")
    suspend fun saveChatLog(
        @Body request: AISaveLogRequest
    ): Response<ApiResponse<Unit>>

    @GET("ai/chat/history")
    suspend fun getChatHistory(
        @Query("sessionId") sessionId: String = "default"
    ): Response<ApiResponse<AIChatHistoryResponse>>
}

data class AIStatusResponse(
    val status: String,
    val models: List<String>?,
    val url: String?,
    val message: String?
)

data class AIChatRequest(
    val messages: List<AIMessage>,
    val userContext: String,
    val model: String? = null
)

data class AIMessage(
    val role: String,
    val content: String
)

data class AIChatResponse(
    val response: String,
    val model: String?,
    val done: Boolean?
)

data class AISaveLogRequest(
    val sessionId: String,
    val messages: List<AIMessage>
)

data class AIChatHistoryResponse(
    val messages: List<AIMessage>?,
    val createdAt: Long?
)
