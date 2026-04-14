package com.fintech.ui.savingsgoal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintech.data.remote.api.services.SavingsGoalApi
import com.fintech.data.remote.api.services.CreateSavingsGoalRequest
import com.fintech.data.remote.api.services.ContributeSavingsGoalRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SavingsGoalState(
    val isLoading: Boolean = false,
    val goals: List<SavingsGoal> = emptyList(),
    val error: String? = null
)

data class SavingsGoal(
    val id: String,
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val period: String,
    val amountPerPeriod: Double,
    val progress: Double,
    val startDate: String?,
    val endDate: String?
)

@HiltViewModel
class SavingsGoalViewModel @Inject constructor(
    private val savingsGoalApi: SavingsGoalApi
) : ViewModel() {

    private val _state = MutableStateFlow(SavingsGoalState())
    val state: StateFlow<SavingsGoalState> = _state.asStateFlow()

    fun loadGoals() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val response = savingsGoalApi.getSavingsGoals()
                if (response.isSuccessful && response.body()?.success == true) {
                    val goals = response.body()?.data?.map { dto ->
                        SavingsGoal(
                            id = dto.id,
                            name = dto.name,
                            targetAmount = dto.targetAmount.toDoubleOrNull() ?: 0.0,
                            currentAmount = dto.currentAmount.toDoubleOrNull() ?: 0.0,
                            period = dto.period,
                            amountPerPeriod = dto.amountPerPeriod.toDoubleOrNull() ?: 0.0,
                            progress = dto.progress,
                            startDate = dto.startDate,
                            endDate = dto.endDate
                        )
                    } ?: emptyList()
                    _state.update { it.copy(isLoading = false, goals = goals) }
                } else {
                    _state.update { it.copy(isLoading = false, error = "Failed to load goals") }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun createGoal(
        name: String,
        targetAmount: Double,
        amountPerPeriod: Double,
        period: String
    ) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val response = savingsGoalApi.createSavingsGoal(
                    CreateSavingsGoalRequest(
                        name = name,
                        targetAmount = targetAmount,
                        amountPerPeriod = amountPerPeriod,
                        period = period
                    )
                )
                if (response.isSuccessful) {
                    loadGoals()
                } else {
                    _state.update { it.copy(isLoading = false, error = "Failed to create goal") }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun contribute(goalId: String, amount: Double) {
        viewModelScope.launch {
            try {
                savingsGoalApi.contributeToSavingsGoal(
                    goalId,
                    ContributeSavingsGoalRequest(amount)
                )
                loadGoals()
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    fun deleteGoal(goalId: String) {
        viewModelScope.launch {
            try {
                savingsGoalApi.deleteSavingsGoal(goalId)
                loadGoals()
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message) }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}
