package com.fintech.ui.market

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintech.data.remote.market.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MarketState(
    val isLoading: Boolean = false,
    val exchangeRates: List<ExchangeRate> = emptyList(),
    val goldPrices: List<GoldPrice> = emptyList(),
    val stockIndices: List<StockIndex> = emptyList(),
    val cryptoPrices: List<CryptoPrice> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class MarketViewModel @Inject constructor(
    private val marketManager: MarketManager
) : ViewModel() {

    private val _state = MutableStateFlow(MarketState())
    val state: StateFlow<MarketState> = _state.asStateFlow()

    fun loadMarketData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            try {
                val result = marketManager.getAllMarketData()

                result.fold(
                    onSuccess = { data ->
                        _state.update {
                            it.copy(
                                isLoading = false,
                                exchangeRates = data.exchangeRates,
                                goldPrices = data.goldPrices,
                                stockIndices = data.stockIndices,
                                cryptoPrices = data.cryptoPrices
                            )
                        }
                    },
                    onFailure = { e ->
                        _state.update {
                            it.copy(
                                isLoading = false,
                                error = "Không thể tải dữ liệu thị trường: ${e.message}"
                            )
                        }
                    }
                )
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
}
