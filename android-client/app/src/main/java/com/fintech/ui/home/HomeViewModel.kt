package com.fintech.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintech.data.local.datastore.PreferencesManager
import com.fintech.domain.model.Account
import com.fintech.domain.model.Transaction
import com.fintech.domain.usecase.account.GetAccountsUseCase
import com.fintech.domain.usecase.account.CalculateBalanceUseCase
import com.fintech.domain.usecase.transaction.GetRecentTransactionsUseCase
import com.fintech.domain.usecase.transaction.GetMonthlyStatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class HomeState(
    val isLoading: Boolean = true,
    val userName: String? = null,
    val totalBalance: Double = 0.0,
    val incomeThisMonth: Double = 0.0,
    val expenseThisMonth: Double = 0.0,
    val recentTransactions: List<Transaction> = emptyList(),
    val accounts: List<Account> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val getAccountsUseCase: GetAccountsUseCase,
    private val calculateBalanceUseCase: CalculateBalanceUseCase,
    private val getRecentTransactionsUseCase: GetRecentTransactionsUseCase,
    private val getMonthlyStatsUseCase: GetMonthlyStatsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    init {
        loadUserInfo()
    }

    private fun loadUserInfo() {
        viewModelScope.launch {
            preferencesManager.userInfo.collect { userInfo ->
                _state.update { it.copy(userName = userInfo?.name) }
            }
        }
    }

    fun loadData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            try {
                // Load accounts
                getAccountsUseCase.refresh()

                combine(
                    getAccountsUseCase(),
                    calculateBalanceUseCase(),
                    getRecentTransactionsUseCase(10),
                    getMonthlyStatsUseCase()
                ) { accounts, balance, transactions, monthlyStats ->
                    HomeState(
                        isLoading = false,
                        userName = _state.value.userName,
                        totalBalance = balance,
                        incomeThisMonth = monthlyStats.first,
                        expenseThisMonth = monthlyStats.second,
                        recentTransactions = transactions,
                        accounts = accounts
                    )
                }.collect { newState ->
                    _state.value = newState
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(isLoading = false, error = e.message)
                }
            }
        }
    }
}
