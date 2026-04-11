package com.fintech.ui.ocr

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintech.data.remote.api.services.AIApi
import com.fintech.data.remote.api.services.TransactionApi
import com.fintech.data.remote.model.request.CreateTransactionRequest
import com.fintech.data.remote.ocr.OCRService
import com.fintech.data.remote.api.services.AccountApi
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class OcrState(
    val isProcessing: Boolean = false,
    val isCreatingTransaction: Boolean = false,
    val parsedAmount: Double? = null,
    val parsedDate: String? = null,
    val merchantName: String? = null,
    val invoiceNumber: String? = null,
    val suggestedCategory: String = "Chi tiêu khác",
    val rawText: String = "",
    val confidence: Float = 0f,
    val error: String? = null,
    val processSuccess: Boolean = false,
    val isUsingAI: Boolean = false,
    val aiAnalysis: String? = null
)

data class AIInvoiceAnalysis(
    val amount: Double?,
    val date: String?,
    val merchant: String?,
    val category: String?,
    val description: String?
)

@HiltViewModel
class OcrViewModel @Inject constructor(
    private val ocrService: OCRService,
    private val transactionApi: TransactionApi,
    private val accountApi: AccountApi,
    private val aiApi: AIApi
) : ViewModel() {

    private val _state = MutableStateFlow(OcrState())
    val state: StateFlow<OcrState> = _state.asStateFlow()

    private var selectedAccountId: String? = null
    private var selectedCategoryId: String? = null
    private val gson = Gson()

    init {
        loadDefaultAccount()
    }

    private fun loadDefaultAccount() {
        viewModelScope.launch {
            try {
                val response = accountApi.getAccounts()
                if (response.isSuccessful && response.body()?.success == true) {
                    val accounts = response.body()?.data ?: emptyList()
                    selectedAccountId = accounts.firstOrNull()?.id
                }
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    fun processImage(bitmap: Bitmap) {
        _state.update { it.copy(isProcessing = true, error = null, processSuccess = false, isUsingAI = false) }

        viewModelScope.launch {
            try {
                // Step 1: OCR với ML Kit
                val result = ocrService.recognizeText(bitmap)
                
                // Step 2: Gọi AI để phân tích sâu hơn
                val aiAnalysis = analyzeWithAI(result.fullText, result.amount, result.merchantName)
                
                // Kết hợp kết quả
                val finalAmount = aiAnalysis?.amount ?: result.amount
                val finalDate = aiAnalysis?.date ?: result.date
                val finalMerchant = aiAnalysis?.merchant ?: result.merchantName
                val finalCategory = aiAnalysis?.category ?: ocrService.parseToInvoiceCategory(result.merchantName, result.fullText)
                val finalDescription = aiAnalysis?.description ?: buildDescription(result.merchantName, result.invoiceNumber)
                
                // Tính confidence
                var confidence = result.confidence
                if (aiAnalysis != null) confidence += 0.2f
                confidence = confidence.coerceIn(0f, 1f)

                _state.update {
                    it.copy(
                        isProcessing = false,
                        parsedAmount = finalAmount,
                        parsedDate = finalDate,
                        merchantName = finalMerchant,
                        invoiceNumber = result.invoiceNumber,
                        suggestedCategory = finalCategory,
                        rawText = result.fullText,
                        confidence = confidence,
                        processSuccess = confidence >= 0.3f || finalAmount != null,
                        isUsingAI = aiAnalysis != null,
                        aiAnalysis = aiAnalysis?.let { gson.toJson(it) }
                    )
                }

                if ((confidence < 0.3f && finalAmount == null) || (finalAmount == null && result.fullText.isBlank())) {
                    _state.update {
                        it.copy(error = "Không nhận diện được số tiền. Vui lòng quét lại hoặc nhập thủ công.")
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(isProcessing = false, error = "Lỗi xử lý ảnh: ${e.message}")
                }
            }
        }
    }

    private suspend fun analyzeWithAI(rawText: String, ocrAmount: Double?, ocrMerchant: String?): AIInvoiceAnalysis? {
        return try {
            val prompt = buildAIAnalysisPrompt(rawText, ocrAmount, ocrMerchant)
            
            val request = com.fintech.data.remote.api.services.AIChatRequest(
                messages = listOf(
                    com.fintech.data.remote.api.services.AIMessage(
                        role = "user",
                        content = prompt
                    )
                ),
                userContext = "OCR Invoice Analysis"
            )

            val response = aiApi.chat(request)
            if (response.isSuccessful) {
                val aiResponse = response.body()?.data?.response
                if (!aiResponse.isNullOrBlank()) {
                    return parseAIResponse(aiResponse)
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }

    private fun buildAIAnalysisPrompt(rawText: String, ocrAmount: Double?, ocrMerchant: String?): String {
        return """
Hãy phân tích văn bản hóa đơn sau và trả về JSON:

OCR đã nhận diện được:
- Số tiền: ${ocrAmount ?: "chưa xác định"}
- Tên cửa hàng: ${ocrMerchant ?: "chưa xác định"}

Văn bản gốc từ OCR:
$rawText

Hãy trả về JSON với các trường:
{
  "amount": số tiền (number, bỏ trống nếu không xác định được),
  "date": ngày tháng năm (string, format YYYY-MM-DD, bỏ trống nếu không xác định được),
  "merchant": tên người bán (string),
  "category": danh mục chi tiêu gợi ý (string: "Ăn uống", "Mua sắm", "Di lại", "Hóa đơn", "Giải trí", "Sức khỏe", "Giáo dục", "Nhà ở", "Chi tiêu khác"),
  "description": mô tả ngắn (string)
}

CHỈ trả về JSON, không giải thích gì thêm. Nếu không chắc chắn, hãy bỏ trống các trường.
        """.trimIndent()
    }

    private fun parseAIResponse(response: String): AIInvoiceAnalysis? {
        return try {
            // Try to extract JSON from response
            val jsonMatch = Regex("""\{[\s\S]*\}""").find(response)
            if (jsonMatch != null) {
                val json = jsonMatch.value
                val parsed = gson.fromJson(json, AIInvoiceAnalysis::class.java)
                parsed
            } else null
        } catch (e: Exception) {
            null
        }
    }

    private fun buildDescription(merchant: String?, invoiceNumber: String?): String {
        return buildString {
            merchant?.let { append(it) }
            invoiceNumber?.let {
                if (isNotEmpty()) append(" - ")
                append("HĐ: $it")
            }
        }.ifEmpty { "Thanh toán hóa đơn" }
    }

    fun updateAmount(amount: Double) {
        _state.update { it.copy(parsedAmount = amount) }
    }

    fun updateCategory(category: String) {
        _state.update { it.copy(suggestedCategory = category) }
    }

    fun confirmAndCreateTransaction() {
        val currentState = _state.value
        val amount = currentState.parsedAmount ?: return
        val accountId = selectedAccountId

        if (accountId == null) {
            _state.update { it.copy(error = "Không tìm thấy tài khoản. Vui lòng thêm tài khoản trước.") }
            return
        }

        _state.update { it.copy(isCreatingTransaction = true, error = null) }

        viewModelScope.launch {
            try {
                val dateTimestamp = currentState.parsedDate?.let { parseDate(it) }
                    ?: (System.currentTimeMillis() / 1000)

                val description = buildDescription(currentState.merchantName, currentState.invoiceNumber)

                val sourceNote = buildString {
                    append("Tạo từ OCR")
                    if (currentState.isUsingAI) append(" + AI Analysis")
                    append(" - Độ chính xác: ${(currentState.confidence * 100).toInt()}%")
                }

                val response = transactionApi.createTransaction(
                    CreateTransactionRequest(
                        accountId = accountId,
                        categoryId = selectedCategoryId,
                        type = "EXPENSE",
                        amount = amount.toLong().toString(),
                        currency = "VND",
                        description = description,
                        note = sourceNote,
                        date = dateTimestamp.toString(),
                        sourceType = if (currentState.isUsingAI) "OCR_AI" else "OCR"
                    )
                )

                if (response.isSuccessful && response.body()?.success == true) {
                    _state.update {
                        it.copy(
                            isCreatingTransaction = false,
                            processSuccess = true
                        )
                    }
                } else {
                    _state.update {
                        it.copy(
                            isCreatingTransaction = false,
                            error = response.body()?.message ?: "Tạo giao dịch thất bại"
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isCreatingTransaction = false,
                        error = "Lỗi tạo giao dịch: ${e.message}"
                    )
                }
            }
        }
    }

    fun setError(message: String) {
        _state.update { it.copy(error = message) }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun reset() {
        _state.update { OcrState() }
        loadDefaultAccount()
    }

    private fun parseDate(dateStr: String): Long? {
        val formats = listOf(
            "yyyy-MM-dd",
            "dd-MM-yyyy",
            "dd/MM/yyyy",
            "yyyy/MM/dd",
            "dd.MM.yyyy"
        )

        for (format in formats) {
            try {
                val sdf = SimpleDateFormat(format, Locale.getDefault())
                return sdf.parse(dateStr)?.time?.div(1000)
            } catch (e: Exception) {
                continue
            }
        }
        return null
    }
}
