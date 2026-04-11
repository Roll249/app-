package com.fintech.ui.qr

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintech.data.remote.api.services.QRCodeApi
import com.fintech.data.remote.api.services.QRValidationResult
import com.fintech.data.remote.model.request.GenerateQRRequest
import com.fintech.data.remote.model.request.GenerateTransferQRRequest
import com.fintech.data.remote.model.request.ProcessQRRequest
import com.fintech.data.remote.model.request.ValidateQRRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * QR Code ViewModel
 */
data class QRState(
    val isLoading: Boolean = false,
    val qrCodeData: String? = null,
    val error: String? = null,
    val paymentInfo: QRValidationResult? = null,
    val processSuccess: Boolean = false
)

@HiltViewModel
class QRViewModel @Inject constructor(
    private val qrCodeApi: QRCodeApi
) : ViewModel() {

    private val _state = MutableStateFlow(QRState())
    val state: StateFlow<QRState> = _state.asStateFlow()

    /**
     * Tạo QR code để nhận tiền
     */
    fun generateReceiveQR(
        bankAccountId: String,
        amount: Double,
        message: String
    ) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val response = qrCodeApi.generateReceiveQR(
                    GenerateQRRequest(
                        bankAccountId = bankAccountId,
                        amount = amount.toString(),
                        message = message,
                        expiresIn = 300
                    )
                )
                if (response.isSuccessful && response.body()?.success == true) {
                    val data = response.body()?.data
                    _state.update {
                        it.copy(
                            isLoading = false,
                            qrCodeData = data?.qrImage ?: data?.qrContent
                        )
                    }
                } else {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = response.body()?.message ?: "Tạo QR thất bại"
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    /**
     * Tạo QR code để chuyển tiền
     */
    fun generateTransferQR(
        fromBankAccountId: String,
        toBankCode: String,
        toAccountNumber: String,
        amount: Double,
        message: String
    ) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val response = qrCodeApi.generateTransferQR(
                    GenerateTransferQRRequest(
                        fromBankAccountId = fromBankAccountId,
                        toBankCode = toBankCode,
                        toAccountNumber = toAccountNumber,
                        amount = amount.toString(),
                        message = message,
                        expiresIn = 300
                    )
                )
                if (response.isSuccessful && response.body()?.success == true) {
                    val data = response.body()?.data
                    _state.update {
                        it.copy(
                            isLoading = false,
                            qrCodeData = data?.qrImage ?: data?.qrContent
                        )
                    }
                } else {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = response.body()?.message ?: "Tạo QR thất bại"
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    /**
     * Xử lý QR code đã quét - validate
     */
    fun processQR(qrPayload: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val response = qrCodeApi.validateQR(
                    ValidateQRRequest(qrContent = qrPayload)
                )
                if (response.isSuccessful && response.body()?.success == true) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            processSuccess = true,
                            paymentInfo = response.body()?.data
                        )
                    }
                } else {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = "QR không hợp lệ hoặc đã hết hạn"
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    /**
     * Validate QR code
     */
    fun validateQR(qrPayload: String) {
        processQR(qrPayload)
    }

    fun clearState() {
        _state.update { QRState() }
    }
}
