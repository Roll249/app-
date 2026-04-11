package com.fintech.ui.bank

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.fintech.ui.components.LoadingIndicator
import com.fintech.ui.theme.IncomeGreen
import com.fintech.ui.theme.Primary

/**
 * Màn hình danh sách ngân hàng với tài khoản giả định
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BankListScreen(
    viewModel: BankViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
    onNavigateToTransfer: () -> Unit,
    onNavigateToBankDetail: (String) -> Unit,
    onNavigateToLinkBank: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadBanks()
    }

    // Lọc ngân hàng chưa liên kết
    val unlinkedBanks = state.banks.filter { bank ->
        state.linkedAccounts.none { it.bank.code == bank.bank.code }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tài khoản ngân hàng") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        if (state.isLoading) {
            LoadingIndicator(modifier = Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Tổng quan số dư
                item {
                    TotalBalanceCard(
                        totalBalance = state.linkedAccounts.sumOf { it.balance },
                        accountCount = state.linkedAccounts.size
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tài khoản của tôi",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(onClick = onNavigateToTransfer) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Thêm tài khoản")
                        }
                    }
                }

                // Danh sách tài khoản đã liên kết
                if (state.linkedAccounts.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.AccountBalance,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Chưa có tài khoản ngân hàng",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "Thêm tài khoản để bắt đầu",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    items(state.linkedAccounts) { bankAccount ->
                        LinkedBankCard(
                            bankAccount = bankAccount,
                            onClick = { onNavigateToBankDetail(bankAccount.bank.code) }
                        )
                    }
                }

                // Danh sách tất cả ngân hàng (chưa liên kết)
                if (unlinkedBanks.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Ngân hàng khả dụng",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    items(unlinkedBanks) { bankAccount ->
                        AllBanksCard(
                            bankAccount = bankAccount,
                            onClick = { onNavigateToLinkBank(bankAccount.bank.code) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Card hiển thị tổng số dư
 */
@Composable
private fun TotalBalanceCard(
    totalBalance: Double,
    accountCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Primary
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBalance,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Tổng số dư",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = formatCurrency(totalBalance) + " đ",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$accountCount tài khoản ngân hàng",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * Card cho tài khoản đã liên kết
 */
@Composable
private fun LinkedBankCard(
    bankAccount: SimulatedBank,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Logo ngân hàng với icon thật
            BankLogo(
                bankCode = bankAccount.bank.code,
                bankName = bankAccount.bank.name,
                modifier = Modifier.size(56.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = bankAccount.bank.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = formatAccountNumber(bankAccount.accountNumber),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = bankAccount.accountHolderName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatCurrency(bankAccount.balance),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = IncomeGreen
                )
                Text(
                    text = "VND",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Card cho tất cả ngân hàng
 */
@Composable
private fun AllBanksCard(
    bankAccount: SimulatedBank,
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
            // Logo ngân hàng với icon thật
            BankLogo(
                bankCode = bankAccount.bank.code,
                bankName = bankAccount.bank.name,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = bankAccount.bank.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = bankAccount.bank.code,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Thêm",
                tint = Primary
            )
        }
    }
}

/**
 * Logo ngân hàng - Hiển thị chữ cái đầu của tên ngân hàng
 */
@Composable
private fun BankLogo(
    bankCode: String,
    bankName: String,
    modifier: Modifier = Modifier
) {
    val backgroundColor = getBankColor(bankCode)
    // Lấy 2 chữ cái đầu của shortName hoặc code
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

/**
 * Lấy màu cho ngân hàng
 */
private fun getBankColor(bankCode: String): Color {
    return when (bankCode.uppercase()) {
        "VCB" -> Color(0xFF1565C0) // Vietcombank - xanh dương
        "VTB" -> Color(0xFF0D47A1) // VietinBank - xanh đậm
        "BIDV" -> Color(0xFFFF8F00) // BIDV - cam
        "TPB" -> Color(0xFFE53935) // TPBank - đỏ
        "ACB" -> Color(0xFF43A047) // ACB - xanh lá
        "MB" -> Color(0xFF5E35B1) // MB Bank - tím
        "SHB" -> Color(0xFF00897B) // SHB - xanh ngọc
        "OCB" -> Color(0xFFFFA000) // OCB - vàng cam
        "HDB" -> Color(0xFFD81B60) // HDBank - hồng
        "VIB" -> Color(0xFF3949AB) // VIB - xanh indigo
        else -> Color(0xFF1976D2) // Mặc định - xanh dương
    }
}
