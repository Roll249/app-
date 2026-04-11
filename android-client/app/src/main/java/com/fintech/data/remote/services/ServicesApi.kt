package com.fintech.data.remote.services

import com.fintech.data.remote.model.ApiResponse
import retrofit2.Response
import retrofit2.http.*

/**
 * Services API - External Services Management
 * 
 * This interface provides endpoints to manage external services:
 * - Get health status of all services
 * - Get specific service status
 * - Trigger manual sync
 * - Send test notifications
 */
interface ServicesApi {

    @GET("services")
    suspend fun getAllServicesStatus(): Response<ApiResponse<ServicesHealthResponse>>

    @GET("services/ai")
    suspend fun getAIServiceStatus(): Response<ApiResponse<AIHealthResponse>>

    @GET("services/banking")
    suspend fun getBankingServiceStatus(): Response<ApiResponse<BankingHealthResponse>>

    @GET("services/notification")
    suspend fun getNotificationServiceStatus(): Response<ApiResponse<NotificationHealthResponse>>

    @GET("services/sync")
    suspend fun getSyncServiceStatus(
        @Query("userId") userId: String
    ): Response<ApiResponse<SyncHealthResponse>>

    @POST("services/sync/trigger")
    suspend fun triggerSync(
        @Body request: SyncTriggerRequest
    ): Response<ApiResponse<SyncTriggerResponse>>

    @POST("services/notification/test")
    suspend fun sendTestNotification(
        @Body request: TestNotificationRequest
    ): Response<ApiResponse<NotificationResult>>
}
