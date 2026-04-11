package com.fintech.ui.bank

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fintech.domain.model.Bank
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Trạng thái của màn hình Ngân hàng
 */
data class BankState(
    val isLoading: Boolean = false,
    val banks: List<SimulatedBank> = emptyList(),
    val linkedAccounts: List<SimulatedBank> = emptyList(),
    val selectedBank: SimulatedBank? = null,
    val error: String? = null,
    val transferSuccess: Boolean = false
)

/**
 * Ngân hàng mô phỏng với số dư giả định
 */
data class SimulatedBank(
    val bank: Bank,
    val accountNumber: String,
    val accountHolderName: String,
    val balance: Double
)

/**
 * ViewModel cho màn hình Ngân hàng
 */
@HiltViewModel
class BankViewModel @Inject constructor() : ViewModel() {
    private val _state = MutableStateFlow(BankState())
    val state: StateFlow<BankState> = _state.asStateFlow()

    init {
        loadBanks()
    }

    fun loadBanks() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            // Tạo danh sách ngân hàng với số dư giả định cố định
            val simulatedBanks = listOf(
                SimulatedBank(
                    bank = Bank(
                        id = "1",
                        code = "VCB",
                        name = "Ngân hàng TMCP Ngoại Thương Việt Nam",
                        shortName = "Vietcombank",
                        logoUrl = null,
                        vietqrPrefix = "970436",
                        swiftCode = "ICBVVNVX"
                    ),
                    accountNumber = "1234567890",
                    accountHolderName = "NGUYEN VAN A",
                    balance = 156_780_000.0
                ),
                SimulatedBank(
                    bank = Bank(
                        id = "2",
                        code = "VTB",
                        name = "Ngân hàng TMCP Công Thương Việt Nam",
                        shortName = "VietinBank",
                        logoUrl = null,
                        vietqrPrefix = "970415",
                        swiftCode = "ICBVVNVX"
                    ),
                    accountNumber = "9876543210",
                    accountHolderName = "NGUYEN VAN A",
                    balance = 89_500_000.0
                ),
                SimulatedBank(
                    bank = Bank(
                        id = "3",
                        code = "BIDV",
                        name = "Ngân hàng TMCP Đầu tư và Phát triển Việt Nam",
                        shortName = "BIDV",
                        logoUrl = null,
                        vietqrPrefix = "970418",
                        swiftCode = "BIDVVNVX"
                    ),
                    accountNumber = "4501234567",
                    accountHolderName = "NGUYEN VAN A",
                    balance = 234_000_000.0
                ),
                SimulatedBank(
                    bank = Bank(
                        id = "4",
                        code = "TPB",
                        name = "Ngân hàng TMCP Tiên Phong",
                        shortName = "TPBank",
                        logoUrl = null,
                        vietqrPrefix = "970423",
                        swiftCode = "TPBVVNVX"
                    ),
                    accountNumber = "5081234567",
                    accountHolderName = "NGUYEN VAN A",
                    balance = 45_600_000.0
                ),
                SimulatedBank(
                    bank = Bank(
                        id = "5",
                        code = "ACB",
                        name = "Ngân hàng TMCP Á Châu",
                        shortName = "ACB",
                        logoUrl = null,
                        vietqrPrefix = "970416",
                        swiftCode = "ASCBVNVX"
                    ),
                    accountNumber = "1234567891",
                    accountHolderName = "NGUYEN VAN A",
                    balance = 67_800_000.0
                ),
                SimulatedBank(
                    bank = Bank(
                        id = "6",
                        code = "MB",
                        name = "Ngân hàng TMCP Quân đội",
                        shortName = "MB Bank",
                        logoUrl = null,
                        vietqrPrefix = "970422",
                        swiftCode = "MSCBVNVX"
                    ),
                    accountNumber = "2221234567",
                    accountHolderName = "NGUYEN VAN A",
                    balance = 112_350_000.0
                ),
                SimulatedBank(
                    bank = Bank(
                        id = "7",
                        code = "SHB",
                        name = "Ngân hàng TMCP Sài Gòn - Hà Nội",
                        shortName = "SHB",
                        logoUrl = null,
                        vietqrPrefix = "970429",
                        swiftCode = "SHBAVNVX"
                    ),
                    accountNumber = "3331234567",
                    accountHolderName = "NGUYEN VAN A",
                    balance = 28_900_000.0
                ),
                SimulatedBank(
                    bank = Bank(
                        id = "8",
                        code = "OCB",
                        name = "Ngân hàng TMCP Phương Đông",
                        shortName = "OCB",
                        logoUrl = null,
                        vietqrPrefix = "970448",
                        swiftCode = "ORCOVNVX"
                    ),
                    accountNumber = "4441234567",
                    accountHolderName = "NGUYEN VAN A",
                    balance = 56_750_000.0
                ),
                SimulatedBank(
                    bank = Bank(
                        id = "9",
                        code = "HDB",
                        name = "Ngân hàng TMCP Phát triển TP.HCM",
                        shortName = "HDBank",
                        logoUrl = null,
                        vietqrPrefix = "970437",
                        swiftCode = "HDBCVNVX"
                    ),
                    accountNumber = "5551234567",
                    accountHolderName = "NGUYEN VAN A",
                    balance = 41_200_000.0
                ),
                SimulatedBank(
                    bank = Bank(
                        id = "10",
                        code = "VIB",
                        name = "Ngân hàng TMCP Quốc tế Việt Nam",
                        shortName = "VIB",
                        logoUrl = null,
                        vietqrPrefix = "970441",
                        swiftCode = "VNIBVNVX"
                    ),
                    accountNumber = "6661234567",
                    accountHolderName = "NGUYEN VAN A",
                    balance = 73_450_000.0
                )
            )

            _state.update {
                it.copy(
                    isLoading = false,
                    banks = simulatedBanks,
                    linkedAccounts = simulatedBanks.take(4) // 4 ngân hàng đầu là đã liên kết
                )
            }
        }
    }

    fun selectBank(bank: SimulatedBank) {
        _state.update { it.copy(selectedBank = bank) }
    }

    fun clearSelectedBank() {
        _state.update { it.copy(selectedBank = null) }
    }

    fun transfer(fromBank: SimulatedBank, toBank: SimulatedBank, amount: Double) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            // Giả lập chuyển tiền thành công
            kotlinx.coroutines.delay(1500)
            
            _state.update {
                it.copy(
                    isLoading = false,
                    transferSuccess = true
                )
            }
        }
    }

    fun clearTransferSuccess() {
        _state.update { it.copy(transferSuccess = false) }
    }
}
