package com.fintech.data.remote.services

/**
 * Service Status Models
 * 
 * These models represent the health status of external services
 */
data class ServiceHealth(
    val status: String,  // "healthy", "degraded", "offline"
    val latencyMs: Long? = null,
    val message: String? = null,
    val lastChecked: String? = null
)

data class ServiceInfo(
    val name: String,
    val status: String,  // "initialized", "error", "not_initialized"
    val health: ServiceHealth? = null,
    val error: String? = null
)

data class ServicesHealthResponse(
    val overall: String,  // "healthy", "degraded", "offline"
    val services: List<ServiceInfo>,
    val lastChecked: String
)

data class AIHealthResponse(
    val name: String,
    val status: String,
    val models: List<String>?,
    val url: String?,
    val message: String?,
    val health: ServiceHealth?
)

data class BankingHealthResponse(
    val name: String,
    val health: ServiceHealth?,
    val banks: List<BankInfo>
)

data class BankInfo(
    val code: String,
    val name: String,
    val shortName: String
)

data class NotificationHealthResponse(
    val name: String,
    val health: ServiceHealth?,
    val capabilities: NotificationCapabilities
)

data class NotificationCapabilities(
    val push: Boolean,
    val email: Boolean,
    val sms: Boolean
)

data class SyncHealthResponse(
    val name: String,
    val health: ServiceHealth?,
    val userStatus: UserSyncStatus?
)

data class UserSyncStatus(
    val lastSyncAt: String?,
    val nextSyncAt: String?,
    val syncInProgress: Boolean,
    val error: String?
)

data class SyncTriggerRequest(
    val userId: String
)

data class SyncTriggerResponse(
    val success: Boolean,
    val recordsProcessed: Int,
    val recordsFailed: Int,
    val duration: Long
)

data class TestNotificationRequest(
    val userId: String,
    val type: String? = null,
    val title: String? = null,
    val body: String? = null
)

data class NotificationResult(
    val success: Boolean,
    val messageId: String?,
    val error: String?
)
