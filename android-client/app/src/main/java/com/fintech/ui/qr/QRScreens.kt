package com.fintech.ui.qr

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.fintech.ui.components.LoadingIndicator

/**
 * Màn hình tạo QR Code để nhận tiền
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRGenerateScreen(
    viewModel: QRViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    var amount by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var bankAccountId by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    // Tạo QR image bitmap
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(state.qrCodeData) {
        state.qrCodeData?.let { qrData ->
            try {
                val writer = QRCodeWriter()
                val bitMatrix = writer.encode(qrData, BarcodeFormat.QR_CODE, 512, 512)
                val width = bitMatrix.width
                val height = bitMatrix.height
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
                for (x in 0 until width) {
                    for (y in 0 until height) {
                        bitmap.setPixel(
                            x, y,
                            if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
                        )
                    }
                }
                qrBitmap = bitmap
            } catch (e: Exception) {
                // Không tạo được QR
            }
        }
    }

    LaunchedEffect(state.error) {
        if (state.error != null) {
            showError = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tạo QR Code nhận tiền") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Thông tin tài khoản ngân hàng
            OutlinedTextField(
                value = bankAccountId,
                onValueChange = { bankAccountId = it },
                label = { Text("ID tài khoản ngân hàng") },
                placeholder = { Text("Nhập ID tài khoản ngân hàng của bạn") },
                leadingIcon = { Icon(Icons.Default.AccountBalance, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Số tiền
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Số tiền (VND)") },
                placeholder = { Text("0") },
                leadingIcon = { Icon(Icons.Default.AttachMoney, null) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Nội dung chuyển khoản
            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                label = { Text("Nội dung") },
                placeholder = { Text("Nội dung chuyển khoản (tùy chọn)") },
                leadingIcon = { Icon(Icons.Default.Message, null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Nút tạo QR
            Button(
                onClick = {
                    val amountValue = amount.toDoubleOrNull() ?: 0.0
                    if (bankAccountId.isNotBlank()) {
                        viewModel.generateReceiveQR(
                            bankAccountId = bankAccountId,
                            amount = amountValue,
                            message = message
                        )
                    } else {
                        showError = true
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading && bankAccountId.isNotBlank()
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Default.QrCode, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tạo QR Code")
                }
            }

            // Hiển thị QR code
            qrBitmap?.let { bitmap ->
                Spacer(modifier = Modifier.height(32.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "QR Code",
                            modifier = Modifier.size(250.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        if (amount.isNotBlank() && amount.toDoubleOrNull() != null && amount.toDoubleOrNull()!! > 0) {
                            Text(
                                text = "${amount.toDoubleOrNull()?.toLong()?.let { formatCurrency(it) } ?: amount} VND",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        if (message.isNotBlank()) {
                            Text(
                                text = message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Mã QR có hiệu lực trong 5 phút",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Nút lưu hoặc chia sẻ
            qrBitmap?.let {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { /* TODO: Share QR */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Share, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Chia sẻ QR Code")
                }
            }
        }

        // Snackbar for errors
        if (showError && state.error != null) {
            Snackbar(
                modifier = Modifier.padding(16.dp),
                action = {
                    TextButton(onClick = { showError = false }) {
                        Text("Đóng")
                    }
                }
            ) {
                Text(state.error ?: "Đã xảy ra lỗi")
            }
        }
    }
}

/**
 * Màn hình quét QR Code
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRScanScreen(
    viewModel: QRViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    var scannedData by remember { mutableStateOf("") }
    var showResultDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.processSuccess) {
        if (state.processSuccess) {
            showResultDialog = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quét QR Code") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (state.isLoading) {
                LoadingIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Đang xử lý...")
            } else {
                // Camera placeholder (thực tế cần implement camera)
                Icon(
                    Icons.Default.QrCodeScanner,
                    contentDescription = null,
                    modifier = Modifier.size(200.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Đưa camera về phía mã QR",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(32.dp))

                // Nút nhập mã QR thủ công
                OutlinedButton(
                    onClick = { /* TODO: Show manual input dialog */ }
                ) {
                    Icon(Icons.Default.Edit, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Nhập mã QR thủ công")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Test input for development
                OutlinedTextField(
                    value = scannedData,
                    onValueChange = { scannedData = it },
                    label = { Text("Mã QR (test)") },
                    placeholder = { Text("Dán mã QR vào đây để test") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        if (scannedData.isNotBlank()) {
                            viewModel.processQR(scannedData)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Xử lý QR")
                }
            }
        }
    }

    // Dialog hiển thị kết quả
    if (showResultDialog) {
        AlertDialog(
            onDismissRequest = {
                showResultDialog = false
                viewModel.clearState()
            },
            title = { Text("QR Code hợp lệ") },
            text = {
                Column {
                    state.paymentInfo?.let { info ->
                        info.amount?.let { Text("Số tiền: ${formatCurrency(it)} đ") }
                        info.message?.let { Text("Nội dung: $it") }
                        info.format?.let { Text("Định dạng: $it") }
                    } ?: Text("Không có thông tin")
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showResultDialog = false
                    viewModel.clearState()
                }) {
                    Text("Đóng")
                }
            }
        )
    }
}

/**
 * Format số tiền VND
 */
private fun formatCurrency(amount: Long): String {
    val formatter = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("vi", "VN"))
    return formatter.format(amount).replace("₫", "").trim()
}

private fun formatCurrency(amount: Double): String {
    val formatter = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("vi", "VN"))
    return formatter.format(amount).replace("₫", "").trim()
}