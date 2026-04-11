package com.fintech.ui.bank

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintech.domain.model.Bank
import com.fintech.domain.usecase.bank.LinkBankAccountUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LinkBankState(
    val bankCode: String = "",
    val bankName: String = "",
    val bankLogoUrl: String? = null,
    val accountNumber: String = "",
    val accountHolderName: String = "",
    val initialBalance: String = "0",
    val isLoading: Boolean = false,
    val linkSuccess: Boolean = false,
    val error: String? = null,
    val accountNumberError: String? = null,
    val accountHolderError: String? = null
)

@HiltViewModel
class LinkBankViewModel @Inject constructor(
    private val linkBankAccountUseCase: LinkBankAccountUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(LinkBankState())
    val state: StateFlow<LinkBankState> = _state.asStateFlow()

    // Demo user ID - thay bằng userId thực từ auth
    private val userId = "demo_user"

    // Danh sách ngân hàng (lấy từ BankViewModel)
    private val availableBanks = mapOf(
        "VCB" to Bank("1", "VCB", "Ngân hàng TMCP Ngoại Thương Việt Nam", "Vietcombank", null, "970436", "ICBVVNVX"),
        "VTB" to Bank("2", "VTB", "Ngân hàng TMCP Công Thương Việt Nam", "VietinBank", null, "970415", "ICBVVNVX"),
        "BIDV" to Bank("3", "BIDV", "Ngân hàng TMCP Đầu tư và Phát triển Việt Nam", "BIDV", null, "970418", "BIDVVNVX"),
        "TPB" to Bank("4", "TPB", "Ngân hàng TMCP Tiên Phong", "TPBank", null, "970423", "TPBVVNVX"),
        "ACB" to Bank("5", "ACB", "Ngân hàng TMCP Á Châu", "ACB", null, "970416", "ASCBVNVX"),
        "MB" to Bank("6", "MB", "Ngân hàng TMCP Quân đội", "MB Bank", null, "970422", "MSCBVNVX"),
        "SHB" to Bank("7", "SHB", "Ngân hàng TMCP Sài Gòn - Hà Nội", "SHB", null, "970429", "SHBAVNVX"),
        "OCB" to Bank("8", "OCB", "Ngân hàng TMCP Phương Đông", "OCB", null, "970448", "ORCOVNVX"),
        "HDB" to Bank("9", "HDB", "Ngân hàng TMCP Phát triển TP.HCM", "HDBank", null, "970437", "HDBCVNVX"),
        "VIB" to Bank("10", "VIB", "Ngân hàng TMCP Quốc tế Việt Nam", "VIB", null, "970441", "VNIBVNVX")
    )

    fun setBankCode(code: String) {
        val bank = availableBanks[code.uppercase()]
        _state.update {
            it.copy(
                bankCode = code.uppercase(),
                bankName = bank?.name ?: bank?.shortName ?: code
            )
        }
    }

    fun updateAccountNumber(value: String) {
        _state.update {
            it.copy(
                accountNumber = value,
                accountNumberError = null
            )
        }
    }

    fun updateAccountHolderName(value: String) {
        _state.update {
            it.copy(
                accountHolderName = value,
                accountHolderError = null
            )
        }
    }

    fun updateInitialBalance(value: String) {
        _state.update {
            it.copy(initialBalance = value)
        }
    }

    fun linkBank() {
        // Validate
        var hasError = false

        if (_state.value.accountNumber.isBlank()) {
            _state.update { it.copy(accountNumberError = "Vui lòng nhập số tài khoản") }
            hasError = true
        }

        if (_state.value.accountHolderName.isBlank()) {
            _state.update { it.copy(accountHolderError = "Vui lòng nhập tên chủ tài khoản") }
            hasError = true
        }

        if (hasError) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val bank = availableBanks[_state.value.bankCode]
            val result = linkBankAccountUseCase(
                userId = userId,
                bankId = bank?.id ?: _state.value.bankCode,
                bankName = _state.value.bankName,
                bankCode = _state.value.bankCode,
                bankLogoUrl = null,
                accountNumber = _state.value.accountNumber,
                accountHolderName = _state.value.accountHolderName.uppercase().trim(),
                initialBalance = _state.value.initialBalance.toDoubleOrNull() ?: 0.0
            )

            when (result) {
                is LinkBankAccountUseCase.Result.Success -> {
                    _state.update {
                        it.copy(isLoading = false, linkSuccess = true)
                    }
                }
                is LinkBankAccountUseCase.Result.AlreadyLinked -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = "Tài khoản này đã được liên kết trước đó"
                        )
                    }
                }
                is LinkBankAccountUseCase.Result.Error -> {
                    _state.update {
                        it.copy(isLoading = false, error = result.message)
                    }
                }
            }
        }
    }
}
