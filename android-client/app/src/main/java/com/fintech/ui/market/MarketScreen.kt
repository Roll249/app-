package com.fintech.ui.market

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import com.fintech.data.remote.market.*
import com.fintech.ui.components.*
import com.fintech.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketScreen(
    onNavigateBack: () -> Unit,
    viewModel: MarketViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadMarketData()
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
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadMarketData() }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Surface,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { },
                containerColor = Secondary,
                contentColor = OnSecondary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Investment")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Portfolio Hero Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                PortfolioHeroSection(
                    totalNetWorth = 1248390.42,
                    invested = 842000.0,
                    cash = 406390.0,
                    changePercent = 12.4
                )
            }

            // Allocation Section
            item {
                AllocationSection()
            }

            // Stock Indices Section
            if (state.stockIndices.isNotEmpty()) {
                item {
                    EditorialSectionHeader(title = "Stock Indices")
                }

                items(state.stockIndices) { index ->
                    EditorialIndexCard(
                        modifier = Modifier.fillMaxWidth(),
                        icon = Icons.Default.ShowChart,
                        name = index.name,
                        subtitle = index.code,
                        value = String.format("%,.2f", index.currentValue),
                        change = String.format("%+.2f%%", index.changePercent),
                        isPositive = index.changePercent >= 0
                    )
                }
            }

            // Cryptocurrency Section
            item {
                EditorialSectionHeader(title = "Cryptocurrency")
            }

            item {
                EditorialCryptoSection(cryptos = state.cryptoPrices)
            }

            // Exchange Rates Section
            item {
                EditorialSectionHeader(title = "Exchange Rates")
            }

            item {
                ExchangeRatesSection(rates = state.exchangeRates)
            }

            // Gold Prices Section
            item {
                EditorialSectionHeader(title = "Precious Metals")
            }

            items(state.goldPrices.take(2)) { gold ->
                EditorialGoldCard(
                    modifier = Modifier.fillMaxWidth(),
                    type = gold.type,
                    badge = if (gold.type.contains("PNJ") || gold.type.contains("24K")) "Premium" else "Standard",
                    company = "PNJ Gold",
                    price = formatVND(gold.sellPrice)
                )
            }

            // Recent Activity Section
            item {
                Spacer(modifier = Modifier.height(16.dp))
                RecentActivitySection()
            }

            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
private fun PortfolioHeroSection(
    totalNetWorth: Double,
    invested: Double,
    cash: Double,
    changePercent: Double
) {
    Box {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PrimaryContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box {
                // Decorative gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Primary.copy(alpha = 0.3f)
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "TOTAL NET WORTH",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = OnPrimaryContainer.copy(alpha = 0.5f),
                        letterSpacing = androidx.compose.ui.unit.TextUnit.Unspecified
                    )
                    Text(
                        text = formatUSD(totalNetWorth),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = OnPrimaryContainer
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Change indicator
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = Secondary
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.TrendingUp,
                                contentDescription = null,
                                tint = OnSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "+${String.format("%.1f", changePercent)}% this month",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = OnSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        Column {
                            Text(
                                text = "Invested",
                                style = MaterialTheme.typography.labelSmall,
                                color = OnPrimaryContainer.copy(alpha = 0.5f)
                            )
                            Text(
                                text = formatUSD(invested),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = OnPrimaryContainer
                            )
                        }
                        Column {
                            Text(
                                text = "Available Cash",
                                style = MaterialTheme.typography.labelSmall,
                                color = OnPrimaryContainer.copy(alpha = 0.5f)
                            )
                            Text(
                                text = formatUSD(cash),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = OnPrimaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AllocationSection() {
    EditorialCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Text(
                text = "Allocation",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            val allocations = listOf(
                AllocationItem("Stocks", 54, Primary),
                AllocationItem("Crypto", 22, Secondary),
                AllocationItem("Gold", 14, TertiaryFixedDim),
                AllocationItem("Cash", 10, SurfaceContainerHighest)
            )

            allocations.forEachIndexed { index, item ->
                AllocationRow(item = item)
                if (index < allocations.lastIndex) {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Primary
                )
            ) {
                Text("Detailed Report")
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun EditorialCryptoSection(cryptos: List<CryptoPrice>) {
    EditorialCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            cryptos.forEach { crypto ->
                CryptoRow(
                    symbol = crypto.symbol,
                    name = crypto.name,
                    price = crypto.price,
                    change = crypto.changePercent24h
                )
            }
        }
    }
}

@Composable
private fun CryptoRow(
    symbol: String,
    name: String,
    price: Double,
    change: Double
) {
    val isPositive = change >= 0
    val cryptoColor = when (symbol) {
        "BTC" -> CryptoBitcoin
        "ETH" -> CryptoEthereum
        "BNB" -> CryptoBNB
        "SOL" -> CryptoSolana
        else -> Secondary
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(cryptoColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CurrencyBitcoin,
                    contentDescription = null,
                    tint = cryptoColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = symbol,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Primary
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = formatUSD(price),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${if (isPositive) "+" else ""}${String.format("%.1f", change)}%",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (isPositive) Secondary else Error
            )
        }
    }
}

@Composable
private fun ExchangeRatesSection(rates: List<ExchangeRate>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        rates.take(2).forEach { rate ->
            ExchangeRateCard(
                modifier = Modifier.weight(1f),
                currency = rate.currency,
                rateValue = rate.transferRate.toLong(),
                change = 0.02,
                isPositive = true
            )
        }
    }
}

@Composable
private fun ExchangeRateCard(
    modifier: Modifier = Modifier,
    currency: String,
    rateValue: Long,
    change: Double,
    isPositive: Boolean
) {
    EditorialCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$currency / VND",
                style = MaterialTheme.typography.labelMedium,
                color = OnSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = rateValue.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = if (isPositive) SecondaryContainer.copy(alpha = 0.2f)
                else ErrorContainer.copy(alpha = 0.2f)
            ) {
                Text(
                    text = "${if (isPositive) "+" else ""}${String.format("%.2f", change)}%",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isPositive) Secondary else Error
                )
            }
        }
    }
}

@Composable
private fun RecentActivitySection() {
    EditorialCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "Recent Activity",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Primary
                )
                TextButton(onClick = { }) {
                    Text(
                        text = "View Archive",
                        color = Secondary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sample activity items
            ActivityItem(
                icon = Icons.Default.AddShoppingCart,
                iconBackgroundColor = SecondaryFixed,
                iconColor = OnSecondaryFixedVariant,
                name = "Bitcoin Purchase",
                date = "April 24, 2024",
                amount = "+$12,400.00",
                status = "Settled",
                isPositive = true
            )

            ActivityItem(
                icon = Icons.Default.Payments,
                iconBackgroundColor = PrimaryContainer,
                iconColor = OnPrimaryContainer,
                name = "Apple Inc. Dividend",
                date = "April 22, 2024",
                amount = "+$482.15",
                status = "Settled",
                isPositive = true
            )

            ActivityItem(
                icon = Icons.Default.Sell,
                iconBackgroundColor = ErrorContainer,
                iconColor = OnErrorContainer,
                name = "Gold SJC Sell",
                date = "April 19, 2024",
                amount = "-$4,200.00",
                status = "Processed",
                isPositive = false
            )
        }
    }
}

@Composable
private fun ActivityItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBackgroundColor: Color,
    iconColor: Color,
    name: String,
    date: String,
    amount: String,
    status: String,
    isPositive: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBackgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
                Text(
                    text = date,
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant
                )
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = amount,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (isPositive) Secondary else Primary
            )
            Text(
                text = status.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (isPositive) Secondary else OnSurfaceVariant,
                letterSpacing = androidx.compose.ui.unit.TextUnit.Unspecified
            )
        }
    }
}

private fun formatUSD(amount: Double): String {
    return "$${String.format("%,.2f", amount)}"
}

private fun formatVND(amount: Double): String {
    val formatter = java.text.NumberFormat.getNumberInstance(java.util.Locale("vi", "VN"))
    formatter.maximumFractionDigits = 0
    return "${formatter.format(amount.toLong())} VND"
}
