package com.fintech.ui.fund

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintech.data.remote.api.services.FundApi
import com.fintech.data.remote.model.request.CreateFundRequest
import com.fintech.data.remote.model.request.ContributeFundRequest
import com.fintech.domain.model.Fund
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class FundState(
    val isLoading: Boolean = false,
    val funds: List<Fund> = emptyList(),
    val error: String? = null,
    val depositSuccess: Boolean = false
)

@HiltViewModel
class FundViewModel @Inject constructor(
    private val fundApi: FundApi
) : ViewModel() {
    private val _state = MutableStateFlow(FundState())
    val state: StateFlow<FundState> = _state.asStateFlow()

    fun loadFunds() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val response = fundApi.getFunds()
                if (response.isSuccessful && response.body()?.success == true) {
                    val funds = response.body()?.data?.map { dto ->
                        Fund(
                            id = dto.id,
                            userId = dto.userId,
                            name = dto.name,
                            icon = dto.icon,
                            color = dto.color,
                            description = dto.description,
                            targetAmount = dto.targetAmount?.toDoubleOrNull(),
                            currentAmount = dto.currentAmount.toDoubleOrNull() ?: 0.0,
                            progress = dto.progress,
                            startDate = parseDate(dto.startDate),
                            endDate = parseDate(dto.endDate),
                            isActive = dto.isActive,
                            createdAt = parseDateToTimestamp(dto.createdAt),
                            updatedAt = parseDateToTimestamp(dto.updatedAt)
                        )
                    } ?: emptyList()
                    _state.update { it.copy(isLoading = false, funds = funds) }
                } else {
                    _state.update { it.copy(isLoading = false, error = "Không thể tải danh sách quỹ") }
                }
            } catch (e: Exception) {
                // Nếu API lỗi, dùng demo data
                _state.update {
                    it.copy(
                        isLoading = false,
                        funds = getDemoFunds()
                    )
                }
            }
        }
    }

    fun createFund(name: String, targetAmount: Long, icon: String, color: String) {
        viewModelScope.launch {
            try {
                val request = CreateFundRequest(
                    name = name,
                    icon = icon,
                    color = color,
                    description = null,
                    targetAmount = targetAmount.toString(),
                    startDate = null,
                    endDate = null
                )
                val response = fundApi.createFund(request)
                if (response.isSuccessful) {
                    loadFunds()
                }
            } catch (e: Exception) {
                // Thêm vào demo data
                val now = System.currentTimeMillis()
                val progress = 0.0
                val newFund = Fund(
                    id = now.toString(),
                    userId = "demo",
                    name = name,
                    icon = icon,
                    color = color,
                    description = null,
                    targetAmount = targetAmount.toDouble(),
                    currentAmount = 0.0,
                    progress = progress,
                    startDate = now,
                    endDate = null,
                    isActive = true,
                    createdAt = now,
                    updatedAt = now
                )
                _state.update { it.copy(funds = it.funds + newFund) }
            }
        }
    }

    fun depositToFund(fundId: String, amount: Long) {
        viewModelScope.launch {
            try {
                val request = ContributeFundRequest(
                    amount = amount.toString(),
                    type = "DEPOSIT",
                    accountId = null,
                    description = "Nạp tiền vào quỹ"
                )
                val response = fundApi.contributeToFund(fundId, request)
                if (response.isSuccessful) {
                    loadFunds()
                    _state.update { it.copy(depositSuccess = true) }
                }
            } catch (e: Exception) {
                // Cập nhật local
                _state.update { currentState ->
                    currentState.copy(
                        funds = currentState.funds.map { fund ->
                            if (fund.id == fundId) {
                                val newAmount = fund.currentAmount + amount
                                val progress = if (fund.targetAmount != null && fund.targetAmount > 0) {
                                    (newAmount / fund.targetAmount * 100).coerceIn(0.0, 100.0)
                                } else 0.0
                                fund.copy(
                                    currentAmount = newAmount,
                                    progress = progress,
                                    updatedAt = System.currentTimeMillis()
                                )
                            } else fund
                        },
                        depositSuccess = true
                    )
                }
            }
        }
    }

    private fun parseDate(dateStr: String?): Long? {
        if (dateStr == null) return null
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            sdf.parse(dateStr)?.time
        } catch (e: Exception) {
            null
        }
    }

    private fun parseDateToTimestamp(dateStr: String): Long {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            sdf.parse(dateStr)?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    private fun getDemoFunds(): List<Fund> {
        val now = System.currentTimeMillis()
        return listOf(
            Fund(
                id = "1",
                userId = "demo",
                name = "Quỹ du lịch Nhật Bản",
                icon = "flight",
                color = "#E91E63",
                description = "Tiết kiệm cho chuyến đi Nhật Bản",
                targetAmount = 50_000_000.0,
                currentAmount = 18_500_000.0,
                progress = 37.0,
                startDate = now - 30L * 24 * 60 * 60 * 1000,
                endDate = null,
                isActive = true,
                createdAt = now - 30L * 24 * 60 * 60 * 1000,
                updatedAt = now
            ),
            Fund(
                id = "2",
                userId = "demo",
                name = "Quỹ mua xe máy mới",
                icon = "directions_car",
                color = "#3F51B5",
                description = "Quỹ thay xe máy mới",
                targetAmount = 35_000_000.0,
                currentAmount = 12_000_000.0,
                progress = 34.3,
                startDate = now - 60L * 24 * 60 * 60 * 1000,
                endDate = null,
                isActive = true,
                createdAt = now - 60L * 24 * 60 * 60 * 1000,
                updatedAt = now
            ),
            Fund(
                id = "3",
                userId = "demo",
                name = "Quỹ khẩn cấp",
                icon = "security",
                color = "#009688",
                description = "Quỹ dự phòng cho trường hợp khẩn cấp",
                targetAmount = 30_000_000.0,
                currentAmount = 22_000_000.0,
                progress = 73.3,
                startDate = now - 90L * 24 * 60 * 60 * 1000,
                endDate = null,
                isActive = true,
                createdAt = now - 90L * 24 * 60 * 60 * 1000,
                updatedAt = now
            )
        )
    }
}
