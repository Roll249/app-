package com.fintech.ui.fund

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintech.data.remote.api.services.AccountApi
import com.fintech.data.remote.api.services.FundApi
import com.fintech.data.remote.model.request.CreateFundRequest
import com.fintech.data.remote.model.request.ContributeFundRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FundState(
    val isLoading: Boolean = false,
    val funds: List<Fund> = emptyList(),
    val totalBalance: Double = 0.0,
    val linkedBalance: Double = 0.0,
    val cashBalance: Double = 0.0,
    val accountCount: Int = 0,
    val error: String? = null,
    val allocationSuggestion: AllocationSuggestion? = null,
    val isAllocating: Boolean = false
)

data class Fund(
    val id: String,
    val name: String,
    val icon: String?,
    val color: String?,
    val description: String?,
    val targetAmount: Double,
    val currentAmount: Double,
    val progress: Double,
    val startDate: String?,
    val endDate: String?
)

data class AllocationSuggestion(
    val totalAmount: Double,
    val allocations: List<FundAllocation>,
    val totalSavings: Double,
    val suggestion: String
)

data class FundAllocation(
    val fundId: String,
    val fundName: String,
    val amount: Double,
    val reason: String
)

@HiltViewModel
class FundViewModel @Inject constructor(
    private val fundApi: FundApi,
    private val accountApi: AccountApi
) : ViewModel() {

    private val _state = MutableStateFlow(FundState())
    val state: StateFlow<FundState> = _state.asStateFlow()

    fun loadAccountSummary() {
        viewModelScope.launch {
            try {
                val response = accountApi.getAccountSummary()
                if (response.isSuccessful && response.body()?.success == true) {
                    val summary = response.body()?.data
                    _state.update {
                        it.copy(
                            totalBalance = summary?.totalBalance ?: 0.0,
                            linkedBalance = summary?.linkedBalance ?: 0.0,
                            cashBalance = summary?.cashBalance ?: 0.0,
                            accountCount = summary?.accountCount ?: 0
                        )
                    }
                }
            } catch (e: Exception) {
                // Silently fail for account summary
            }
        }
    }

    fun loadFunds() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                // Load both funds and account summary in parallel
                val fundsResponse = fundApi.getFunds()
                val summaryResponse = accountApi.getAccountSummary()

                if (fundsResponse.isSuccessful && fundsResponse.body()?.success == true) {
                    val funds = fundsResponse.body()?.data?.map { dto ->
                        Fund(
                            id = dto.id,
                            name = dto.name,
                            icon = dto.icon,
                            color = dto.color,
                            description = dto.description,
                            targetAmount = dto.targetAmount?.toDoubleOrNull() ?: 0.0,
                            currentAmount = dto.currentAmount.toDoubleOrNull() ?: 0.0,
                            progress = dto.progress ?: 0.0,
                            startDate = dto.startDate,
                            endDate = dto.endDate
                        )
                    } ?: emptyList()

                    val summary = summaryResponse.body()?.data

                    _state.update {
                        it.copy(
                            isLoading = false,
                            funds = funds,
                            totalBalance = summary?.totalBalance ?: it.totalBalance,
                            linkedBalance = summary?.linkedBalance ?: 0.0,
                            cashBalance = summary?.cashBalance ?: 0.0,
                            accountCount = summary?.accountCount ?: 0
                        )
                    }
                } else {
                    _state.update { it.copy(isLoading = false, error = "Failed to load funds") }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun createFund(
        name: String,
        targetAmount: Double,
        icon: String? = null,
        color: String? = null,
        description: String? = null
    ) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val response = fundApi.createFund(
                    CreateFundRequest(
                        name = name,
                        targetAmount = targetAmount.toString(),
                        icon = icon,
                        color = color,
                        description = description
                    )
                )
                if (response.isSuccessful) {
                    loadFunds()
                } else {
                    _state.update { it.copy(isLoading = false, error = "Failed to create fund") }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun contribute(fundId: String, amount: Double) {
        viewModelScope.launch {
            try {
                fundApi.contributeToFund(
                    fundId, 
                    ContributeFundRequest(
                        amount = amount.toString(),
                        type = "EXPENSE",
                        accountId = null,
                        description = "Contribute to fund"
                    )
                )
                loadFunds()
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    fun deleteFund(fundId: String) {
        viewModelScope.launch {
            try {
                fundApi.deleteFund(fundId)
                loadFunds()
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}
