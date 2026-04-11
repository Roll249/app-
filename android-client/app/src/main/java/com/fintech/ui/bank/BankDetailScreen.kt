package com.fintech.ui.bank

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fintech.ui.theme.IncomeGreen
import com.fintech.ui.theme.Primary

/**
 * Màn hình chi tiết ngân hàng
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BankDetailScreen(
    bankCode: String,
    viewModel: BankViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToTransfer: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    // Tìm ngân hàng theo code
    val bankAccount = state.banks.find { it.bank.code == bankCode }

    LaunchedEffect(bankCode) {
        viewModel.loadBanks()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = bankAccount?.bank?.name ?: bankAccount?.bank?.code ?: "Ngân hàng",
                        maxLines = 1
                    ) 
                },
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
        if (bankAccount == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Card thông tin tài khoản
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Primary)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Logo ngân hàng - Hiển thị mã ngân hàng
                        BankLogoText(
                            bankCode = bankAccount.bank.code,
                            modifier = Modifier.size(80.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = bankAccount.bank.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Số dư
                        Text(
                            text = "Số dư",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Text(
                            text = formatCurrency(bankAccount.balance) + " đ",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        HorizontalDivider(color = Color.White.copy(alpha = 0.3f))

                        Spacer(modifier = Modifier.height(16.dp))

                        // Thông tin tài khoản
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Số tài khoản",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = formatAccountNumber(bankAccount.accountNumber),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Tên tài khoản",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = bankAccount.accountHolderName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                // Các nút hành động
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onNavigateToTransfer,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Chuyển tiền")
                    }
                    Button(
                        onClick = { /* TODO: Generate QR */ },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = IncomeGreen
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.QrCode, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Nhận tiền")
                    }
                }

                // Thông tin ngân hàng
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Thông tin ngân hàng",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        InfoRow(
                            icon = Icons.Default.Business,
                            label = "Tên đầy đủ",
                            value = bankAccount.bank.name
                        )
                        InfoRow(
                            icon = Icons.Default.Code,
                            label = "Mã ngân hàng",
                            value = bankAccount.bank.code
                        )
                        InfoRow(
                            icon = Icons.Default.Language,
                            label = "Swift Code",
                            value = bankAccount.bank.swiftCode ?: "N/A"
                        )
                        InfoRow(
                            icon = Icons.Default.QrCodeScanner,
                            label = "Prefix VietQR",
                            value = bankAccount.bank.vietqrPrefix ?: "N/A"
                        )
                    }
                }

                // Lịch sử giao dịch (mock)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Giao dịch gần đây",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            TextButton(onClick = { /* TODO */ }) {
                                Text("Xem tất cả")
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        // Mock transactions
                        TransactionItem(
                            icon = Icons.Default.ArrowDownward,
                            title = "Nhận tiền từ Nguyen Van B",
                            date = "Hôm nay, 14:30",
                            amount = "+5.000.000",
                            isPositive = true
                        )
                        TransactionItem(
                            icon = Icons.Default.ArrowUpward,
                            title = "Chuyển tiền cho Nguyen Van C",
                            date = "Hôm nay, 10:15",
                            amount = "-2.500.000",
                            isPositive = false
                        )
                        TransactionItem(
                            icon = Icons.Default.ArrowDownward,
                            title = "Nhận lương tháng 4",
                            date = "05/04/2026",
                            amount = "+15.000.000",
                            isPositive = true
                        )
                        TransactionItem(
                            icon = Icons.Default.ArrowUpward,
                            title = "Thanh toán hóa đơn điện",
                            date = "03/04/2026",
                            amount = "-350.000",
                            isPositive = false
                        )
                    }
                }
            }
        }
    }
}

/**
 * Hàng thông tin
 */
@Composable
private fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Item giao dịch
 */
@Composable
private fun TransactionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    date: String,
    amount: String,
    isPositive: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    if (isPositive) IncomeGreen.copy(alpha = 0.1f)
                    else MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isPositive) IncomeGreen else MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = date,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = "$amount đ",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = if (isPositive) IncomeGreen else MaterialTheme.colorScheme.error
        )
    }
    HorizontalDivider()
}

/**
 * Logo ngân hàng - Hiển thị mã ngân hàng
 */
@Composable
private fun BankLogoText(
    bankCode: String,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (bankCode.uppercase()) {
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
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}
