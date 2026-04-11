package com.fintech.ui.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintech.domain.model.Account
import com.fintech.domain.usecase.account.CreateAccountUseCase
import com.fintech.domain.usecase.account.GetAccountsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AccountState(
    val isLoading: Boolean = false,
    val accounts: List<Account> = emptyList(),
    val error: String? = null,
    val createSuccess: Boolean = false
)

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val getAccountsUseCase: GetAccountsUseCase,
    private val createAccountUseCase: CreateAccountUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AccountState())
    val state: StateFlow<AccountState> = _state.asStateFlow()

    fun loadAccounts() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                getAccountsUseCase.refresh()
                getAccountsUseCase.getAccountsForUser().collect { accounts ->
                    _state.update { it.copy(isLoading = false, accounts = accounts) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun createAccount(
        name: String,
        type: String,
        icon: String? = null,
        color: String? = null,
        initialBalance: Double = 0.0,
        currency: String = "VND"
    ) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, createSuccess = false) }
            val result = createAccountUseCase(
                name = name,
                type = type,
                icon = icon,
                color = color,
                initialBalance = initialBalance,
                currency = currency
            )
            result.fold(
                onSuccess = {
                    _state.update { it.copy(isLoading = false, createSuccess = true) }
                    // Reload accounts list
                    loadAccounts()
                },
                onFailure = { e ->
                    _state.update { it.copy(isLoading = false, error = e.message) }
                }
            )
        }
    }
}
