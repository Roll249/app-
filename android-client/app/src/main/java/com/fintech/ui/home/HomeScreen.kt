package com.fintech.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fintech.ui.components.*
import com.fintech.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToAddTransaction: () -> Unit,
    onNavigateToTransaction: () -> Unit,
    onNavigateToAccounts: () -> Unit,
    onNavigateToQRGenerate: () -> Unit,
    onNavigateToQRScan: () -> Unit,
    onNavigateToTransfer: () -> Unit,
    onNavigateToAI: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Xin chào!",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = state.userName ?: "Người dùng",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToQRScan) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = "Quét QR")
                    }
                    IconButton(onClick = onNavigateToAI) {
                        Icon(Icons.Default.Psychology, contentDescription = "AI Chat")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddTransaction,
                containerColor = Primary
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Thêm giao dịch",
                    tint = androidx.compose.ui.graphics.Color.White
                )
            }
        }
    ) { paddingValues ->
        if (state.isLoading) {
            LoadingIndicator(modifier = Modifier.padding(paddingValues))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Balance Card
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    BalanceCard(
                        balance = state.totalBalance,
                        title = "Tổng số dư"
                    )
                }

                // Quick Actions
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        QuickActionButton(
                            icon = Icons.Default.QrCode,
                            label = "QR",
                            onClick = onNavigateToQRGenerate,
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionButton(
                            icon = Icons.Default.AccountBalance,
                            label = "Ngân hàng",
                            onClick = onNavigateToAccounts,
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionButton(
                            icon = Icons.Default.AddCard,
                            label = "Chuyển tiền",
                            onClick = onNavigateToTransfer,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Income/Expense Summary
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.ArrowDownward,
                                    contentDescription = null,
                                    tint = IncomeGreen
                                )
                                Text(
                                    text = "Thu nhập",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = formatCurrency(state.incomeThisMonth),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = IncomeGreen
                                )
                            }
                            Divider(
                                modifier = Modifier
                                    .height(48.dp)
                                    .width(1.dp)
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.ArrowUpward,
                                    contentDescription = null,
                                    tint = ExpenseRed
                                )
                                Text(
                                    text = "Chi tiêu",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = formatCurrency(state.expenseThisMonth),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = ExpenseRed
                                )
                            }
                        }
                    }
                }

                // Recent Transactions Header
                item {
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
                        TextButton(onClick = onNavigateToTransaction) {
                            Text("Xem tất cả")
                        }
                    }
                }

                // Recent Transactions
                if (state.recentTransactions.isEmpty()) {
                    item {
                        EmptyStateView(
                            icon = Icons.Default.Receipt,
                            title = "Chưa có giao dịch",
                            subtitle = "Bắt đầu thêm giao dịch đầu tiên"
                        )
                    }
                } else {
                    items(state.recentTransactions.take(5)) { transaction ->
                        TransactionItem(
                            icon = transaction.categoryIcon,
                            iconColor = transaction.categoryColor,
                            categoryName = transaction.categoryName ?: "Khác",
                            description = transaction.description,
                            amount = transaction.amount,
                            type = transaction.type.name,
                            date = formatDate(transaction.date)
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
private fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        onClick = onClick,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("dd/MM HH:mm", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
}
