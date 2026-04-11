package com.fintech.ui.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(PrimaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = OnPrimaryContainer,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Editorial Finance",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* notifications */ }) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = OnSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Surface
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Total Net Worth Hero Card
            item {
                Spacer(modifier = Modifier.height(8.dp))
                TotalNetWorthCard(
                    totalNetWorth = state.totalBalance,
                    income = state.incomeThisMonth,
                    expense = state.expenseThisMonth
                )
            }

            // Growth Target Card
            item {
                GrowthTargetCard(
                    currentAmount = state.totalBalance,
                    goalAmount = 200000.0
                )
            }

            // Quick Actions Grid
            item {
                QuickActionsGrid(
                    onSend = onNavigateToTransfer,
                    onReceive = onNavigateToAccounts,
                    onQRScan = onNavigateToQRScan,
                    onBillPay = onNavigateToAddTransaction
                )
            }

            // AI Chat Shortcut
            item {
                AIChatShortcut(onClick = onNavigateToAI)
            }

            // Spending Trend Section
            item {
                SpendingTrendSection()
            }

            // Recent Transactions Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Transactions",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                    TextButton(onClick = onNavigateToTransaction) {
                        Text(
                            text = "View All",
                            color = Secondary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Recent Transactions
            if (state.recentTransactions.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Default.Receipt,
                        title = "No transactions yet",
                        subtitle = "Start adding your first transaction"
                    )
                }
            } else {
                items(state.recentTransactions.take(4)) { transaction ->
                    EditorialTransactionCard(
                        icon = transaction.categoryIcon ?: "receipt",
                        iconColor = transaction.categoryColor ?: "#6ffbbe",
                        name = transaction.categoryName ?: "Other",
                        description = transaction.description ?: "",
                        amount = transaction.amount,
                        category = transaction.type.name,
                        isExpense = transaction.type.name == "EXPENSE"
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
private fun TotalNetWorthCard(
    totalNetWorth: Double,
    income: Double,
    expense: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Primary),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box {
            // Decorative circle
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 80.dp, y = (-80).dp)
                    .background(
                        Color(0xFF6cf8bb).copy(alpha = 0.1f),
                        CircleShape
                    )
            )

            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "TOTAL NET WORTH",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = OnPrimaryContainer.copy(alpha = 0.7f),
                    letterSpacing = androidx.compose.ui.unit.TextUnit.Unspecified
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = formatCurrency(totalNetWorth),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = OnPrimary
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Income/Expense pills
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IncomeExpensePill(
                        icon = Icons.AutoMirrored.Filled.ArrowForward,
                        label = "Income",
                        amount = income,
                        isIncome = true
                    )
                    IncomeExpensePill(
                        icon = Icons.AutoMirrored.Filled.TrendingDown,
                        label = "Expenses",
                        amount = expense,
                        isIncome = false
                    )
                }
            }
        }
    }
}

@Composable
private fun IncomeExpensePill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    amount: Double,
    isIncome: Boolean
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.White.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isIncome) SecondaryContainer else Color(0xFFff6b6b),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = formatCurrency(amount),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun GrowthTargetCard(
    currentAmount: Double,
    goalAmount: Double
) {
    val progress = (currentAmount / goalAmount).coerceIn(0.0, 1.0)

    EditorialCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = SecondaryContainer
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "Growth Target",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = OnSecondaryFixedVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "On track for Q3 goals",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Secondary
                    )
                }
                Icon(
                    Icons.AutoMirrored.Filled.TrendingUp,
                    contentDescription = null,
                    tint = OnSecondaryFixedVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progress bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Secondary.copy(alpha = 0.1f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress.toFloat())
                        .height(12.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Secondary)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${(progress * 100).toInt()}% ACHIEVED",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Secondary
                )
                Text(
                    text = "${formatCurrency(goalAmount)} GOAL",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Secondary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Secondary,
                    contentColor = OnSecondary
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.TrendingUp,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Optimize Portfolio",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun QuickActionsGrid(
    onSend: () -> Unit,
    onReceive: () -> Unit,
    onQRScan: () -> Unit,
    onBillPay: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickActionCard(
            icon = Icons.Default.Send,
            label = "Send",
            iconBackgroundColor = Primary,
            iconColor = OnPrimary,
            onClick = onSend,
            modifier = Modifier.weight(1f)
        )
        QuickActionCard(
            icon = Icons.Default.Download,
            label = "Receive",
            iconBackgroundColor = Secondary,
            iconColor = OnSecondary,
            onClick = onReceive,
            modifier = Modifier.weight(1f)
        )
        QuickActionCard(
            icon = Icons.Default.QrCodeScanner,
            label = "QR Scan",
            iconBackgroundColor = SurfaceContainerHigh,
            iconColor = Primary,
            onClick = onQRScan,
            modifier = Modifier.weight(1f)
        )
        QuickActionCard(
            icon = Icons.Default.Receipt,
            label = "Bill Pay",
            iconBackgroundColor = SurfaceContainerHigh,
            iconColor = Primary,
            onClick = onBillPay,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun QuickActionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    iconBackgroundColor: Color,
    iconColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    EditorialCard(
        modifier = modifier,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBackgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Primary,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun SpendingTrendSection() {
    EditorialCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "Spending Trend",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Monthly analysis of your liquid capital",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant
                    )
                }
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = SurfaceContainer
                ) {
                    Text(
                        text = "Last 6 Months",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Simple bar chart
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                val data = listOf(0.4f, 0.65f, 0.35f, 0.85f, 0.6f, 0.5f)
                data.forEachIndexed { index, height ->
                    val barColor = if (index == 4) Secondary else SurfaceContainerLow
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(height)
                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                            .background(barColor)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("JAN", "FEB", "MAR", "APR", "MAY", "JUN").forEach { month ->
                    Text(
                        text = month,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (month == "MAY") Secondary else OnSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun AIChatShortcut(onClick: () -> Unit) {
    EditorialCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = PrimaryContainer,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(SecondaryFixed),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.SmartToy,
                        contentDescription = null,
                        tint = Secondary,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Fina AI Assistant",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = OnPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Chat with your financial curator",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = OnPrimaryContainer.copy(alpha = 0.5f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun EditorialTransactionCard(
    icon: String,
    iconColor: String,
    name: String,
    description: String,
    amount: Double,
    category: String,
    isExpense: Boolean
) {
    val iconBackgroundColor = try {
        Color(android.graphics.Color.parseColor(iconColor))
    } catch (e: Exception) {
        SecondaryFixed
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceContainerLowest)
            .clickable { }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconBackgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = getIconFromName(icon) ?: Icons.Default.Receipt,
                contentDescription = null,
                tint = if (isExpense) Secondary else TertiaryFixed,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Primary,
                maxLines = 1
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceVariant,
                maxLines = 1
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${if (isExpense) "-" else "+"}${formatCurrency(amount)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isExpense) Primary else Secondary
            )
            Text(
                text = category.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = OnSurfaceVariant,
                letterSpacing = androidx.compose.ui.unit.TextUnit.Unspecified
            )
        }
    }
}

private fun getIconFromName(name: String?): androidx.compose.ui.graphics.vector.ImageVector? {
    return when (name?.lowercase()) {
        "restaurant", "food" -> Icons.Default.Restaurant
        "shopping", "shopping_bag" -> Icons.Default.ShoppingBag
        "car", "transport" -> Icons.Default.DirectionsCar
        "payments", "salary" -> Icons.Default.Payments
        "electric_car" -> Icons.Default.ElectricCar
        "flight" -> Icons.Default.Flight
        "home" -> Icons.Default.Home
        "health" -> Icons.Default.HealthAndSafety
        "school" -> Icons.Default.School
        else -> Icons.Default.Receipt
    }
}

private fun formatCurrency(amount: Double): String {
    val formatter = java.text.NumberFormat.getCurrencyInstance(java.util.Locale.US)
    return formatter.format(amount)
}
