package com.fintech.ui.bank

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fintech.ui.theme.IncomeGreen
import com.fintech.ui.theme.Primary
import androidx.compose.material3.Text

/**
 * Màn hình chuyển tiền giữa các ngân hàng
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferScreen(
    viewModel: BankViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    // Form state
    var selectedFromBank by remember { mutableStateOf<SimulatedBank?>(null) }
    var selectedToBank by remember { mutableStateOf<SimulatedBank?>(null) }
    var amount by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var showFromBankPicker by remember { mutableStateOf(false) }
    var showToBankPicker by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    // Xử lý khi chuyển tiền thành công
    LaunchedEffect(state.transferSuccess) {
        if (state.transferSuccess) {
            showSuccessDialog = true
            viewModel.clearTransferSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chuyển tiền") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Card tài khoản nguồn
                BankSelectionCard(
                    title = "Từ tài khoản",
                    selectedBank = selectedFromBank,
                    onClick = { showFromBankPicker = true }
                )
            }

            item {
                // Icon mũi tên chuyển tiền
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            item {
                // Card tài khoản đích
                BankSelectionCard(
                    title = "Đến tài khoản",
                    selectedBank = selectedToBank,
                    onClick = { showToBankPicker = true }
                )
            }

            item {
                // Số tiền chuyển
                OutlinedTextField(
                    value = amount,
                    onValueChange = { 
                        amount = it.filter { c -> c.isDigit() }
                    },
                    label = { Text("Số tiền") },
                    placeholder = { Text("0") },
                    leadingIcon = {
                        Icon(Icons.Default.AttachMoney, contentDescription = null)
                    },
                    trailingIcon = {
                        Text("VND", style = MaterialTheme.typography.bodyMedium)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                // Hiển thị số tiền bằng chữ
                if (amount.isNotBlank() && (amount.toLongOrNull() ?: 0) > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "(" + convertNumberToText(amount.toLongOrNull() ?: 0) + " đồng)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                // Nội dung chuyển khoản
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Nội dung (tùy chọn)") },
                    placeholder = { Text("VD: Chuyển tiền thanh toán") },
                    leadingIcon = {
                        Icon(Icons.Default.Message, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            item {
                // Nút chuyển tiền
                Button(
                    onClick = { showConfirmDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = selectedFromBank != null && 
                               selectedToBank != null && 
                               amount.isNotBlank() &&
                               (amount.toLongOrNull() ?: 0) > 0,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                        Icon(Icons.Default.Send, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Chuyển tiền", fontWeight = FontWeight.Bold)
                    }
                }
            }

            item {
                // Thông tin phí
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Phí chuyển tiền: Miễn phí cho các giao dịch nội bộ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    // Dialog chọn ngân hàng nguồn
    if (showFromBankPicker) {
        BankPickerDialog(
            banks = state.linkedAccounts,
            title = "Chọn tài khoản nguồn",
            excludeBank = selectedToBank?.bank?.code,
            onSelect = { bank ->
                selectedFromBank = bank
                showFromBankPicker = false
            },
            onDismiss = { showFromBankPicker = false }
        )
    }

    // Dialog chọn ngân hàng đích
    if (showToBankPicker) {
        BankPickerDialog(
            banks = state.banks,
            title = "Chọn tài khoản đích",
            excludeBank = selectedFromBank?.bank?.code,
            onSelect = { bank ->
                selectedToBank = bank
                showToBankPicker = false
            },
            onDismiss = { showToBankPicker = false }
        )
    }

    // Dialog xác nhận chuyển tiền
    if (showConfirmDialog && selectedFromBank != null && selectedToBank != null) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Xác nhận chuyển tiền") },
            text = {
                Column {
                    Text("Bạn có chắc chắn muốn chuyển:")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = formatCurrency(amount.toLongOrNull() ?: 0) + " VND",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Từ: ${selectedFromBank?.bank?.name}")
                    Text("Số tài khoản: ${formatAccountNumber(selectedFromBank?.accountNumber.orEmpty())}")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Đến: ${selectedToBank?.bank?.name}")
                    Text("Số tài khoản: ${formatAccountNumber(selectedToBank?.accountNumber.orEmpty())}")
                    if (message.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Nội dung: $message")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDialog = false
                        viewModel.transfer(
                            selectedFromBank!!,
                            selectedToBank!!,
                            amount.toDoubleOrNull() ?: 0.0
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text("Xác nhận")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showConfirmDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }

    // Dialog thành công
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                // Reset form
                selectedFromBank = null
                selectedToBank = null
                amount = ""
                message = ""
            },
            icon = {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = IncomeGreen,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = { Text("Chuyển tiền thành công!") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = formatCurrency(amount.toLongOrNull() ?: 0) + " VND",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = IncomeGreen
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("đã được chuyển đến")
                    Text(
                        text = selectedToBank?.bank?.name ?: "",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text("Đóng")
                }
            }
        )
    }
}

/**
 * Card chọn ngân hàng
 */
@Composable
private fun BankSelectionCard(
    title: String,
    selectedBank: SimulatedBank?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selectedBank != null) {
                // Logo ngân hàng - hiển thị chữ cái
                BankLogoText(
                    bankCode = selectedBank.bank.code,
                    bankName = selectedBank.bank.shortName ?: selectedBank.bank.name,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = selectedBank.bank.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = formatAccountNumber(selectedBank.accountNumber),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatCurrency(selectedBank.balance) + " đ",
                        style = MaterialTheme.typography.bodySmall,
                        color = IncomeGreen
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.Default.AddCircleOutline,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Chọn $title",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Dialog chọn ngân hàng
 */
@Composable
private fun BankPickerDialog(
    banks: List<SimulatedBank>,
    title: String,
    excludeBank: String?,
    onSelect: (SimulatedBank) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            LazyColumn {
                items(banks.filter { it.bank.code != excludeBank }) { bank ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(bank) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BankLogoText(
                            bankCode = bank.bank.code,
                            bankName = bank.bank.shortName ?: bank.bank.name,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = bank.bank.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1
                            )
                            Text(
                                text = formatAccountNumber(bank.accountNumber),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = formatCurrency(bank.balance) + " đ",
                            style = MaterialTheme.typography.bodySmall,
                            color = IncomeGreen
                        )
                    }
                    HorizontalDivider()
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}

/**
 * Lấy màu cho ngân hàng
 */
private fun getBankColor(bankCode: String): Color {
    return when (bankCode.uppercase()) {
        "VCB" -> Color(0xFF1565C0)
        "VTB" -> Color(0xFF0D47A1)
        "BIDV" -> Color(0xFFFF8F00)
        "TPB" -> Color(0xFFE53935)
        "ACB" -> Color(0xFF43A047)
        "MB" -> Color(0xFF5E35B1)
        "SHB" -> Color(0xFF00897B)
        "OCB" -> Color(0xFFFFA000)
        "HDB" -> Color(0xFFD81B60)
        "VIB" -> Color(0xFF3949AB)
        else -> Color(0xFF1976D2)
    }
}

/**
 * Logo ngân hàng - Hiển thị chữ cái đầu của tên ngân hàng
 */
@Composable
private fun BankLogoText(
    bankCode: String,
    bankName: String,
    modifier: Modifier = Modifier
) {
    val backgroundColor = getBankColor(bankCode)
    val initials = when (bankCode.uppercase()) {
        "VCB" -> "VCB"
        "VTB" -> "VTB"
        "BIDV" -> "BIDV"
        "TPB" -> "TPB"
        "ACB" -> "ACB"
        "MB" -> "MB"
        "SHB" -> "SHB"
        "OCB" -> "OCB"
        "HDB" -> "HDB"
        "VIB" -> "VIB"
        else -> bankCode.take(3).uppercase()
    }

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}
