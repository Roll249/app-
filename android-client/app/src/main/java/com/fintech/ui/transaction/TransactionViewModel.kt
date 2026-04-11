package com.fintech.ui.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintech.domain.model.Transaction
import com.fintech.domain.usecase.transaction.CreateTransactionUseCase
import com.fintech.domain.usecase.transaction.GetTransactionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TransactionState(
    val isLoading: Boolean = false,
    val transactions: List<Transaction> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val createTransactionUseCase: CreateTransactionUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(TransactionState())
    val state: StateFlow<TransactionState> = _state.asStateFlow()

    fun loadTransactions() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                getTransactionsUseCase.refresh()
                getTransactionsUseCase.getTransactionsForUser().collect { transactions ->
                    _state.update { it.copy(isLoading = false, transactions = transactions) }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun createTransaction(
        amount: Double,
        type: String,
        description: String?,
        accountId: String = "default",
        categoryId: String? = null
    ) {
        viewModelScope.launch {
            createTransactionUseCase(
                accountId = accountId,
                categoryId = categoryId,
                type = type,
                amount = amount,
                description = description
            )
            loadTransactions()
        }
    }
}
