package com.fintech.ui.budget

import androidx.lifecycle.ViewModel
import com.fintech.domain.model.Budget
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class BudgetState(
    val isLoading: Boolean = false,
    val budgets: List<Budget> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class BudgetViewModel @Inject constructor() : ViewModel() {
    private val _state = MutableStateFlow(BudgetState())
    val state: StateFlow<BudgetState> = _state.asStateFlow()
}
