package com.fintech.data.remote.services

import com.fintech.data.local.datastore.PreferencesManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service Manager - Centralized Service Management
 * 
 * This manager provides access to all external services and their status.
 * It's the single entry point for service-related operations.
 */
@Singleton
class ServiceManager @Inject constructor(
    private val servicesApi: ServicesApi,
    private val preferencesManager: PreferencesManager
) {
    
    /**
     * Get overall services health status
     */
    suspend fun getServicesHealth(): Result<ServicesHealthResponse> {
        return try {
            val response = servicesApi.getAllServicesStatus()
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to get services status"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get AI service specific status
     */
    suspend fun getAIServiceStatus(): Result<AIHealthResponse> {
        return try {
            val response = servicesApi.getAIServiceStatus()
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to get AI status"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get Banking service specific status
     */
    suspend fun getBankingServiceStatus(): Result<BankingHealthResponse> {
        return try {
            val response = servicesApi.getBankingServiceStatus()
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to get banking status"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get Notification service specific status
     */
    suspend fun getNotificationServiceStatus(): Result<NotificationHealthResponse> {
        return try {
            val response = servicesApi.getNotificationServiceStatus()
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to get notification status"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get Sync service specific status
     */
    suspend fun getSyncServiceStatus(): Result<SyncHealthResponse> {
        return try {
            val userId = runBlocking { preferencesManager.userId.first() ?: "demo" }
            val response = servicesApi.getSyncServiceStatus(userId)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to get sync status"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Trigger manual sync
     */
    suspend fun triggerSync(): Result<SyncTriggerResponse> {
        return try {
            val userId = runBlocking { preferencesManager.userId.first() ?: "demo" }
            val response = servicesApi.triggerSync(SyncTriggerRequest(userId))
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to trigger sync"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Send test notification
     */
    suspend fun sendTestNotification(
        title: String = "Test Notification",
        body: String = "This is a test notification"
    ): Result<NotificationResult> {
        return try {
            val userId = runBlocking { preferencesManager.userId.first() ?: "demo" }
            val response = servicesApi.sendTestNotification(
                TestNotificationRequest(
                    userId = userId,
                    title = title,
                    body = body
                )
            )
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()!!.data!!)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to send notification"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Check if a specific service is healthy
     */
    suspend fun isServiceHealthy(serviceName: String): Boolean {
        return try {
            val health = getServicesHealth()
            health.getOrNull()?.services?.find { 
                it.name.contains(serviceName, ignoreCase = true) 
            }?.health?.status == "healthy"
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Check if AI service is online
     */
    suspend fun isAIOnline(): Boolean {
        return try {
            val status = getAIServiceStatus()
            status.getOrNull()?.status == "online"
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Check if banking service is available
     */
    suspend fun isBankingAvailable(): Boolean {
        return try {
            val status = getBankingServiceStatus()
            status.getOrNull()?.health?.status != "offline"
        } catch (e: Exception) {
            false
        }
    }
}
