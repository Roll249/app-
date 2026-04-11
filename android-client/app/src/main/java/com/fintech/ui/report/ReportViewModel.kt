package com.fintech.ui.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintech.data.remote.api.services.ReportApi
import com.fintech.data.remote.model.response.CategoryAmount
import com.fintech.data.remote.model.response.TrendItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReportState(
    val isLoading: Boolean = false,
    val income: Double = 0.0,
    val expense: Double = 0.0,
    val savings: Double = 0.0,
    val totalBalance: Double = 0.0,
    val categoryBreakdown: List<CategoryAmount> = emptyList(),
    val trendIncome: List<TrendItem> = emptyList(),
    val trendExpense: List<TrendItem> = emptyList(),
    val trendSavings: List<TrendItem> = emptyList(),
    val selectedPeriod: String = "MONTHLY",
    val error: String? = null
)

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val reportApi: ReportApi
) : ViewModel() {
    private val _state = MutableStateFlow(ReportState())
    val state: StateFlow<ReportState> = _state.asStateFlow()

    init {
        loadReport()
    }

    fun loadReport(period: String = "MONTHLY") {
        _state.update { it.copy(isLoading = true, error = null, selectedPeriod = period) }

        viewModelScope.launch {
            try {
                val summaryRes = reportApi.getFinancialSummary()
                val trendRes = reportApi.getTrendReport(months = 6)

                if (summaryRes.isSuccessful && summaryRes.body()?.success == true) {
                    val data = summaryRes.body()?.data
                    _state.update {
                        it.copy(
                            totalBalance = data?.totalBalance?.toDoubleOrNull() ?: 0.0,
                            income = data?.totalIncome?.toDoubleOrNull() ?: 0.0,
                            expense = data?.totalExpense?.toDoubleOrNull() ?: 0.0,
                            savings = data?.netSavings?.toDoubleOrNull() ?: 0.0,
                            categoryBreakdown = data?.categoryBreakdown ?: emptyList()
                        )
                    }
                }

                if (trendRes.isSuccessful && trendRes.body()?.success == true) {
                    val trendData = trendRes.body()?.data
                    _state.update {
                        it.copy(
                            trendIncome = trendData?.income ?: emptyList(),
                            trendExpense = trendData?.expense ?: emptyList(),
                            trendSavings = trendData?.savings ?: emptyList()
                        )
                    }
                }

                _state.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun setPeriod(period: String) {
        if (period != _state.value.selectedPeriod) {
            loadReport(period)
        }
    }

    fun refresh() {
        loadReport(_state.value.selectedPeriod)
    }
}
