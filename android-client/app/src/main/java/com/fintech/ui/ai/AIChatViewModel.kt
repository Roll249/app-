package com.fintech.ui.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintech.data.remote.api.services.AIApi
import com.fintech.data.remote.api.services.AIMessage
import com.fintech.data.remote.api.services.AIStatusResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AIChatState(
    val messages: List<ChatMessage> = emptyList(),
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
    val recentTransactions: List<TransactionSummary> = emptyList()
)

data class FundSummary(
    val id: String,
    val name: String,
    val currentAmount: Double,
    val targetAmount: Double
)

data class TransactionSummary(
    val id: String,
    val amount: Double,
    val category: String,
    val description: String
)

@HiltViewModel
class AIChatViewModel @Inject constructor(
    private val aiApi: AIApi
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
            // TODO: Load from API
            // Hiện tại dùng demo data
            _state.update {
                it.copy(
                    userContext = UserFinancialContext(
                        totalBalance = 156_780_000.0,
                        monthlyIncome = 15_000_000.0,
                        monthlyExpenses = 8_500_000.0,
                        funds = listOf(
                            FundSummary("1", "Quỹ du lịch Nhật Bản", 18_500_000.0, 50_000_000.0),
                            FundSummary("2", "Quỹ mua xe máy", 12_000_000.0, 35_000_000.0),
                            FundSummary("3", "Quỹ khẩn cấp", 22_000_000.0, 30_000_000.0)
                        )
                    )
                )
            }
        }
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
        val userMessage = ChatMessage(role = "user", content = text)
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
                // Build messages for API
                val messages = _state.value.messages.map {
                    AIMessage(role = it.role, content = it.content)
                }

                // Call backend API
                val response = aiApi.chat(
                    com.fintech.data.remote.api.services.AIChatRequest(
                        messages = messages,
                        userContext = buildUserContextString()
                    )
                )

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    val aiResponseText = apiResponse?.data?.response ?: "Xin lỗi, tôi không nhận được phản hồi."
                    val aiMessage = ChatMessage(role = "assistant", content = aiResponseText)

                    _state.update {
                        it.copy(
                            messages = it.messages + aiMessage,
                            isTyping = false
                        )
                    }

                    saveChatLog()
                } else {
                    val errorMessage = ChatMessage(
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
                val errorMessage = ChatMessage(
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
                val messages = _state.value.messages.map {
                    AIMessage(role = it.role, content = it.content)
                }
                aiApi.saveChatLog(
                    com.fintech.data.remote.api.services.AISaveLogRequest(
                        sessionId = sessionId,
                        messages = messages
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
        val fundsStr = ctx.funds.joinToString("\n") {
            "- ${it.name}: ${formatCurrency(it.currentAmount)} / ${formatCurrency(it.targetAmount)} VNĐ (${((it.currentAmount / it.targetAmount) * 100).toInt()}%)"
        }

        return """
Tổng số dư: ${formatCurrency(ctx.totalBalance)} VNĐ
Thu nhập hàng tháng: ${formatCurrency(ctx.monthlyIncome)} VNĐ
Chi tiêu hàng tháng: ${formatCurrency(ctx.monthlyExpenses)} VNĐ

Các quỹ tiết kiệm:
$fundsStr

Số dư có thể tiết kiệm: ${formatCurrency(ctx.monthlyIncome - ctx.monthlyExpenses)} VNĐ/tháng
        """.trimIndent()
    }

    private fun formatCurrency(amount: Double): String {
        return java.text.NumberFormat.getNumberInstance(java.util.Locale("vi", "VN"))
            .format(amount.toLong())
    }

    fun refreshAIStatus() {
        checkAIStatus()
    }
}
