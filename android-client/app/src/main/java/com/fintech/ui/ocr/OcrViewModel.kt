package com.fintech.ui.ocr

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintech.data.remote.api.services.TransactionApi
import com.fintech.data.remote.model.request.CreateTransactionRequest
import com.fintech.data.remote.ocr.OCRService
import com.fintech.data.remote.api.services.AccountApi
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
    val processSuccess: Boolean = false
)

@HiltViewModel
class OcrViewModel @Inject constructor(
    private val ocrService: OCRService,
    private val transactionApi: TransactionApi,
    private val accountApi: AccountApi
) : ViewModel() {

    private val _state = MutableStateFlow(OcrState())
    val state: StateFlow<OcrState> = _state.asStateFlow()

    private var selectedAccountId: String? = null
    private var selectedCategoryId: String? = null

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
        _state.update { it.copy(isProcessing = true, error = null, processSuccess = false) }

        viewModelScope.launch {
            try {
                val result = ocrService.recognizeText(bitmap)
                val category = ocrService.parseToInvoiceCategory(result.merchantName, result.fullText)

                _state.update {
                    it.copy(
                        isProcessing = false,
                        parsedAmount = result.amount,
                        parsedDate = result.date,
                        merchantName = result.merchantName,
                        invoiceNumber = result.invoiceNumber,
                        suggestedCategory = category,
                        rawText = result.fullText,
                        confidence = result.confidence,
                        processSuccess = result.confidence >= 0.3f
                    )
                }

                if (result.confidence < 0.3f && result.amount == null) {
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

                val description = buildString {
                    currentState.merchantName?.let { append(it) }
                    currentState.invoiceNumber?.let {
                        if (isNotEmpty()) append(" - ")
                        append("HĐ: $it")
                    }
                }.ifEmpty { "Thanh toán hóa đơn" }

                val response = transactionApi.createTransaction(
                    CreateTransactionRequest(
                        accountId = accountId,
                        categoryId = selectedCategoryId,
                        type = "EXPENSE",
                        amount = amount.toLong().toString(),
                        currency = "VND",
                        description = description,
                        note = "Tạo từ OCR - Độ chính xác: ${(currentState.confidence * 100).toInt()}%",
                        date = dateTimestamp.toString(),
                        sourceType = "OCR"
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
