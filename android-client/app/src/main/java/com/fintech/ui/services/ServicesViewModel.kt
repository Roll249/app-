package com.fintech.ui.services

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintech.data.remote.services.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class ServicesState(
    val isLoading: Boolean = false,
    val overallStatus: String = "unknown",
    val lastChecked: String? = null,
    val services: List<ServiceInfo> = emptyList(),
    val syncStatus: UserSyncStatus? = null,
    val notificationCapabilities: NotificationCapabilities? = null,
    val isSyncing: Boolean = false,
    val isSendingNotification: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ServicesViewModel @Inject constructor(
    private val serviceManager: ServiceManager
) : ViewModel() {

    private val _state = MutableStateFlow(ServicesState())
    val state: StateFlow<ServicesState> = _state.asStateFlow()

    private val dateFormat = SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale("vi", "VN"))

    fun loadServicesStatus() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            try {
                val healthResult = serviceManager.getServicesHealth()

                healthResult.fold(
                    onSuccess = { health ->
                        _state.update {
                            it.copy(
                                isLoading = false,
                                overallStatus = health.overall,
                                lastChecked = dateFormat.format(Date()),
                                services = health.services
                            )
                        }
                    },
                    onFailure = { e ->
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = "Không thể tải trạng thái dịch vụ: ${e.message}"
                            )
                        }
                    }
                )

                // Load sync status
                loadSyncStatus()

                // Load notification status
                loadNotificationStatus()

            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Lỗi: ${e.message}"
                    )
                }
            }
        }
    }

    private suspend fun loadSyncStatus() {
        try {
            val result = serviceManager.getSyncServiceStatus()
            result.fold(
                onSuccess = { status ->
                    _state.update {
                        it.copy(syncStatus = status.userStatus)
                    }
                },
                onFailure = { /* Silent fail for sync status */ }
            )
        } catch (e: Exception) {
            // Silent fail
        }
    }

    private suspend fun loadNotificationStatus() {
        try {
            val result = serviceManager.getNotificationServiceStatus()
            result.fold(
                onSuccess = { status ->
                    _state.update {
                        it.copy(notificationCapabilities = status.capabilities)
                    }
                },
                onFailure = { /* Silent fail for notification status */ }
            )
        } catch (e: Exception) {
            // Silent fail
        }
    }

    fun triggerSync() {
        viewModelScope.launch {
            _state.update { it.copy(isSyncing = true, error = null) }

            try {
                val result = serviceManager.triggerSync()

                result.fold(
                    onSuccess = { response ->
                        _state.update {
                            it.copy(
                                isSyncing = false,
                                syncStatus = it.syncStatus?.copy(
                                    lastSyncAt = dateFormat.format(Date())
                                )
                            )
                        }
                        // Reload to get updated sync status
                        loadServicesStatus()
                    },
                    onFailure = { e ->
                        _state.update {
                            it.copy(
                                isSyncing = false,
                                error = "Đồng bộ thất bại: ${e.message}"
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isSyncing = false,
                        error = "Lỗi đồng bộ: ${e.message}"
                    )
                }
            }
        }
    }

    fun sendTestNotification() {
        viewModelScope.launch {
            _state.update { it.copy(isSendingNotification = true, error = null) }

            try {
                val result = serviceManager.sendTestNotification(
                    title = "Thông báo test",
                    body = "Đây là thông báo test từ ứng dụng Fintech"
                )

                result.fold(
                    onSuccess = { response ->
                        _state.update {
                            it.copy(
                                isSendingNotification = false,
                                error = if (response.success) null else "Gửi thất bại: ${response.error}"
                            )
                        }
                    },
                    onFailure = { e ->
                        _state.update {
                            it.copy(
                                isSendingNotification = false,
                                error = "Gửi thất bại: ${e.message}"
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isSendingNotification = false,
                        error = "Lỗi: ${e.message}"
                    )
                }
            }
        }
    }
}
