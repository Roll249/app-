package com.fintech.ui.bank

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fintech.ui.theme.Primary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkBankScreen(
    bankCode: String,
    onNavigateBack: () -> Unit,
    viewModel: LinkBankViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(bankCode) {
        viewModel.setBankCode(bankCode)
    }

    LaunchedEffect(state.linkSuccess) {
        if (state.linkSuccess) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Liên kết ngân hàng") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Bank Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Primary.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BankLogo(
                        bankCode = state.bankCode,
                        bankName = state.bankName,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = state.bankName.ifEmpty { state.bankCode },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = state.bankCode,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Instructions
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Nhập thông tin tài khoản ngân hàng của bạn để liên kết. Chúng tôi sẽ lưu trữ thông tin này một cách bảo mật.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Account Number
            OutlinedTextField(
                value = state.accountNumber,
                onValueChange = { viewModel.updateAccountNumber(it) },
                label = { Text("Số tài khoản") },
                placeholder = { Text("Nhập số tài khoản") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                leadingIcon = {
                    Icon(Icons.Default.CreditCard, contentDescription = null)
                },
                isError = state.accountNumberError != null,
                supportingText = {
                    state.accountNumberError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            )

            // Account Holder Name
            OutlinedTextField(
                value = state.accountHolderName,
                onValueChange = { viewModel.updateAccountHolderName(it) },
                label = { Text("Tên chủ tài khoản") },
                placeholder = { Text("Nhập tên chủ tài khoản") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = null)
                },
                isError = state.accountHolderError != null,
                supportingText = {
                    state.accountHolderError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            )

            // Initial Balance (optional)
            OutlinedTextField(
                value = state.initialBalance,
                onValueChange = { viewModel.updateInitialBalance(it) },
                label = { Text("Số dư ban đầu (tùy chọn)") },
                placeholder = { Text("0") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                leadingIcon = {
                    Icon(Icons.Default.AttachMoney, contentDescription = null)
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            // Error Message
            state.error?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // Link Button
            Button(
                onClick = { viewModel.linkBank() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading && state.accountNumber.isNotBlank() && state.accountHolderName.isNotBlank()
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White
                    )
                } else {
                    Icon(Icons.Default.Link, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Liên kết tài khoản")
                }
            }

            // Disclaimer
            Text(
                text = "Bằng việc liên kết, bạn đồng ý với các điều khoản sử dụng của chúng tôi.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BankLogo(
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

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
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
}

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
