package com.fintech.ui.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintech.data.remote.api.services.AIApi
import com.fintech.data.remote.api.services.AIMessage
import com.fintech.data.remote.api.services.AIStatusResponse
import com.fintech.data.remote.api.services.ReportApi
import com.fintech.data.remote.api.services.SavingsGoalApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AIChatState(
    val messages: List<AIMessage> = emptyList(),
    val inputText: String = "",
    val isTyping: Boolean = false,
    val error: String? = null,
    val userContext: UserFinancialContext = UserFinancialContext(),
    val aiStatus: AIStatusResponse? = null,
    val isAIOnline: Boolean = false
)

data class UserFinancialContext(
    val totalBalance: Double = 0.0,
    val monthlyIncome: Double = 0.0,
    val monthlyExpenses: Double = 0.0,
    val funds: List<FundSummary> = emptyList(),
    val savingsGoals: List<SavingsGoalSummary> = emptyList(),
    val recentTransactions: List<TransactionSummary> = emptyList()
)

data class FundSummary(
    val id: String,
    val name: String,
    val currentAmount: Double,
    val targetAmount: Double
)

data class SavingsGoalSummary(
    val id: String,
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val amountPerPeriod: Double,
    val period: String
)

data class TransactionSummary(
    val id: String,
    val amount: Double,
    val category: String,
    val description: String
)

@HiltViewModel
class AIChatViewModel @Inject constructor(
    private val aiApi: AIApi,
    private val reportApi: ReportApi,
    private val savingsGoalApi: SavingsGoalApi
) : ViewModel() {

    private val _state = MutableStateFlow(AIChatState())
    val state: StateFlow<AIChatState> = _state.asStateFlow()

    private val sessionId = "default"

    init {
        checkAIStatus()
        loadUserContext()
    }

    private fun checkAIStatus() {
        viewModelScope.launch {
            try {
                val response = aiApi.getAIStatus()
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    val status = apiResponse?.data
                    _state.update {
                        it.copy(
                            aiStatus = status,
                            isAIOnline = status?.status == "online"
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isAIOnline = false,
                        error = "Không thể kết nối đến AI server: ${e.message}"
                    )
                }
            }
        }
    }

    private fun loadUserContext() {
        viewModelScope.launch {
            try {
                // Load monthly stats from report API
                val reportResponse = reportApi.getIncomeExpenseReport(period = "MONTHLY")
                val monthlyIncome = if (reportResponse.isSuccessful) {
                    reportResponse.body()?.data?.totalIncome?.toDoubleOrNull() ?: 0.0
                } else 0.0

                val monthlyExpenses = if (reportResponse.isSuccessful) {
                    reportResponse.body()?.data?.totalExpense?.toDoubleOrNull() ?: 0.0
                } else 0.0

                // Load savings goals
                val savingsGoalsResponse = savingsGoalApi.getSavingsGoals()
                val savingsGoals = if (savingsGoalsResponse.isSuccessful) {
                    savingsGoalsResponse.body()?.data?.map { dto ->
                        SavingsGoalSummary(
                            id = dto.id,
                            name = dto.name,
                            targetAmount = dto.targetAmount.toDoubleOrNull() ?: 0.0,
                            currentAmount = dto.currentAmount.toDoubleOrNull() ?: 0.0,
                            amountPerPeriod = dto.amountPerPeriod.toDoubleOrNull() ?: 0.0,
                            period = dto.period
                        )
                    } ?: emptyList()
                } else emptyList()

                // Calculate total balance from account API
                val totalBalance = 0.0 // Will be updated from HomeScreen

                _state.update {
                    it.copy(
                        userContext = UserFinancialContext(
                            totalBalance = totalBalance,
                            monthlyIncome = monthlyIncome,
                            monthlyExpenses = monthlyExpenses,
                            funds = it.userContext.funds,
                            savingsGoals = savingsGoals
                        )
                    )
                }
            } catch (e: Exception) {
                // Fallback to empty context on error
                _state.update {
                    it.copy(
                        userContext = UserFinancialContext()
                    )
                }
            }
        }
    }

    fun updateTotalBalance(balance: Double) {
        _state.update {
            it.copy(
                userContext = it.userContext.copy(totalBalance = balance)
            )
        }
    }

    fun refreshUserContext() {
        loadUserContext()
    }

    fun updateInputText(text: String) {
        _state.update { it.copy(inputText = text) }
    }

    fun sendMessage() {
        val text = _state.value.inputText.trim()
        if (text.isBlank()) return
        if (!_state.value.isAIOnline) {
            _state.update { it.copy(error = "AI server đang offline. Vui lòng thử lại sau.") }
            return
        }

        // Add user message
        val userMessage = AIMessage(role = "user", content = text)
        _state.update {
            it.copy(
                messages = it.messages + userMessage,
                inputText = "",
                isTyping = true,
                error = null
            )
        }

        viewModelScope.launch {
            try {
                // Call backend API
                val response = aiApi.chat(
                    com.fintech.data.remote.api.services.AIChatRequest(
                        messages = _state.value.messages,
                        userContext = buildUserContextString()
                    )
                )

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    val aiResponseText = apiResponse?.data?.response ?: "Xin lỗi, tôi không nhận được phản hồi."
                    val aiMessage = AIMessage(role = "assistant", content = aiResponseText)

                    _state.update {
                        it.copy(
                            messages = it.messages + aiMessage,
                            isTyping = false
                        )
                    }

                    saveChatLog()
                } else {
                    val errorMessage = AIMessage(
                        role = "assistant",
                        content = "Xin lỗi, tôi đang gặp sự cố. Vui lòng thử lại sau.\n\nLỗi: ${response.message()}"
                    )
                    _state.update {
                        it.copy(
                            messages = it.messages + errorMessage,
                            isTyping = false,
                            error = response.message()
                        )
                    }
                }
            } catch (e: Exception) {
                val errorMessage = AIMessage(
                    role = "assistant",
                    content = "Đã xảy ra lỗi: ${e.message}"
                )
                _state.update {
                    it.copy(
                        messages = it.messages + errorMessage,
                        isTyping = false,
                        error = e.message
                    )
                }
            }
        }
    }

    private fun saveChatLog() {
        viewModelScope.launch {
            try {
                aiApi.saveChatLog(
                    com.fintech.data.remote.api.services.AISaveLogRequest(
                        sessionId = sessionId,
                        messages = _state.value.messages
                    )
                )
            } catch (e: Exception) {
                // Silent fail - don't interrupt user experience
            }
        }
    }

    fun askAboutFundAllocation() {
        val prompt = "Hãy giúp tôi chia tiền tiết kiệm vào các quỹ hiện tại. Tôi nhận được lương ${formatCurrency(_state.value.userContext.monthlyIncome)} VNĐ/tháng và chi tiêu khoảng ${formatCurrency(_state.value.userContext.monthlyExpenses)} VNĐ/tháng."
        _state.update { it.copy(inputText = prompt) }
    }

    fun askAboutSpending() {
        val prompt = "Hãy phân tích chi tiêu của tôi trong tháng này và đưa ra lời khuyên tiết kiệm."
        _state.update { it.copy(inputText = prompt) }
    }

    private fun buildUserContextString(): String {
        val ctx = _state.value.userContext
        val fundsStr = if (ctx.funds.isNotEmpty()) {
            ctx.funds.joinToString("\n") {
                "- ${it.name}: ${formatCurrency(it.currentAmount)} / ${formatCurrency(it.targetAmount)} VND (${((it.currentAmount / it.targetAmount) * 100).toInt()}%)"
            }
        } else "Chua co quy tiet kiem"

        val savingsGoalsStr = if (ctx.savingsGoals.isNotEmpty()) {
            ctx.savingsGoals.joinToString("\n") { goal ->
                "- ${goal.name}: ${formatCurrency(goal.currentAmount)} / ${formatCurrency(goal.targetAmount)} VND (tiet kiem ${formatCurrency(goal.amountPerPeriod)}/${goal.period.lowercase()})"
            }
        } else "Chua co muc tieu tiet kiem"

        val availableForSavings = ctx.monthlyIncome - ctx.monthlyExpenses

        return """
|TINH HINH TAI CHINH HIEN TAI:
|Tong so du: ${formatCurrency(ctx.totalBalance)} VND
|Thu nhap hang thang: ${formatCurrency(ctx.monthlyIncome)} VND
|Chi tieu hang thang: ${formatCurrency(ctx.monthlyExpenses)} VND
|So du kha dung: ${formatCurrency(availableForSavings)} VND/thang
|
|CAC QUY TIET KIEM:
|$fundsStr
|
|CAC MUC TIEU TIET KIEM:
|$savingsGoalsStr
        """.trimIndent()
    }

    private fun formatCurrency(amount: Double): String {
        return java.text.NumberFormat.getNumberInstance(java.util.Locale("vi", "VN"))
            .format(amount.toLong())
    }

    fun refreshAIStatus() {
        checkAIStatus()
    }

    // Auto-send message when coming from "Optimize Portfolio" button
    fun autoSendPortfolioOptimization() {
        val userCtx = _state.value.userContext
        val prompt = "Hãy phân tích và tối ưu hóa danh mục đầu tư của tôi. " +
            "Tổng số dư: ${formatCurrency(userCtx.totalBalance)} VNĐ. " +
            "Thu nhập hàng tháng: ${formatCurrency(userCtx.monthlyIncome)} VNĐ. " +
            "Chi tiêu hàng tháng: ${formatCurrency(userCtx.monthlyExpenses)} VNĐ. " +
            "Số dư khả dụng: ${formatCurrency(userCtx.totalBalance)} VNĐ. " +
            "Hãy đưa ra lời khuyên phân bổ đầu tư tốt nhất."
        sendAutoMessage(prompt)
    }

    private fun sendAutoMessage(text: String) {
        if (!_state.value.isAIOnline) {
            _state.update { it.copy(error = "AI server đang offline. Vui lòng thử lại sau.") }
            return
        }

        // Add user message
        val userMessage = AIMessage(role = "user", content = text)
        _state.update {
            it.copy(
                messages = it.messages + userMessage,
                inputText = "",
                isTyping = true,
                error = null
            )
        }

        viewModelScope.launch {
            try {
                val response = aiApi.chat(
                    com.fintech.data.remote.api.services.AIChatRequest(
                        messages = _state.value.messages,
                        userContext = buildUserContextString()
                    )
                )

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    val aiResponseText = apiResponse?.data?.response ?: "Xin lỗi, tôi không nhận được phản hồi."
                    val aiMessage = AIMessage(role = "assistant", content = aiResponseText)

                    _state.update {
                        it.copy(
                            messages = it.messages + aiMessage,
                            isTyping = false
                        )
                    }

                    saveChatLog()
                } else {
                    val errorMessage = AIMessage(
                        role = "assistant",
                        content = "Xin lỗi, tôi đang gặp sự cố. Vui lòng thử lại sau."
                    )
                    _state.update {
                        it.copy(
                            messages = it.messages + errorMessage,
                            isTyping = false
                        )
                    }
                }
            } catch (e: Exception) {
                val errorMessage = AIMessage(
                    role = "assistant",
                    content = "Đã xảy ra lỗi: ${e.message}"
                )
                _state.update {
                    it.copy(
                        messages = it.messages + errorMessage,
                        isTyping = false
                    )
                }
            }
        }
    }
}
