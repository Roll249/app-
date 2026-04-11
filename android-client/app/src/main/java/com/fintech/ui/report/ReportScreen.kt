package com.fintech.ui.report

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fintech.data.remote.model.response.CategoryAmount
import com.fintech.data.remote.model.response.TrendItem
import com.fintech.ui.theme.*
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    viewModel: ReportViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Báo cáo tài chính") },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Làm mới")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Primary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Đang tải báo cáo...")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }

                // Period selector
                item {
                    PeriodSelector(
                        selectedPeriod = state.selectedPeriod,
                        onPeriodSelected = { viewModel.setPeriod(it) }
                    )
                }

                // Tổng quan tài chính
                item {
                    FinancialOverviewCard(
                        totalBalance = state.totalBalance,
                        income = state.income,
                        expense = state.expense,
                        savings = state.savings
                    )
                }

                // Biểu đồ đường xu hướng (6 tháng)
                if (state.trendIncome.isNotEmpty() || state.trendExpense.isNotEmpty()) {
                    item {
                        TrendLineChart(
                            income = state.trendIncome,
                            expense = state.trendExpense,
                            savings = state.trendSavings,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Biểu đồ cột so sánh Thu/Chi
                if (state.trendIncome.isNotEmpty() || state.trendExpense.isNotEmpty()) {
                    item {
                        IncomeExpenseBarChart(
                            income = state.trendIncome,
                            expense = state.trendExpense,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Biểu đồ tròn phân bổ chi tiêu
                if (state.categoryBreakdown.isNotEmpty()) {
                    item {
                        ExpensePieChart(
                            categories = state.categoryBreakdown,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Danh sách chi tiêu theo danh mục
                if (state.categoryBreakdown.isNotEmpty()) {
                    item {
                        Text(
                            text = "Chi tiêu theo danh mục",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    items(state.categoryBreakdown) { category ->
                        CategoryExpenseItem(
                            category = category,
                            totalExpense = state.expense
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun PeriodSelector(
    selectedPeriod: String,
    onPeriodSelected: (String) -> Unit
) {
    val periods = listOf("WEEKLY" to "Tuần này", "MONTHLY" to "Tháng này", "YEARLY" to "Năm nay")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        periods.forEach { (key, label) ->
            FilterChip(
                selected = selectedPeriod == key,
                onClick = { onPeriodSelected(key) },
                label = { Text(label) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Primary,
                    selectedLabelColor = Color.White
                ),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun FinancialOverviewCard(
    totalBalance: Double,
    income: Double,
    expense: Double,
    savings: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Primary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Tổng tài sản",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )
            Text(
                text = "${formatCurrency(totalBalance)} đ",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryItem(
                    icon = Icons.Default.ArrowDownward,
                    label = "Thu nhập",
                    value = income,
                    color = IncomeGreen
                )
                SummaryItem(
                    icon = Icons.Default.ArrowUpward,
                    label = "Chi tiêu",
                    value = expense,
                    color = ExpenseRed
                )
                SummaryItem(
                    icon = Icons.Default.Savings,
                    label = "Tiết kiệm",
                    value = savings,
                    color = Color(0xFFFFD700)
                )
            }
        }
    }
}

@Composable
private fun SummaryItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: Double,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.7f)
        )
        Text(
            text = "${formatCurrency(value)}đ",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
private fun TrendLineChart(
    income: List<TrendItem>,
    expense: List<TrendItem>,
    savings: List<TrendItem>,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Xu hướng thu nhập & chi tiêu",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                LegendDot(color = IncomeGreen, label = "Thu nhập")
                LegendDot(color = ExpenseRed, label = "Chi tiêu")
                LegendDot(color = Primary, label = "Tiết kiệm")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                val w = size.width
                val h = size.height
                val padding = 40f

                val maxValue = maxOf(
                    income.maxOfOrNull { it.amount.toDoubleOrNull() ?: 0.0 } ?: 0.0,
                    expense.maxOfOrNull { it.amount.toDoubleOrNull() ?: 0.0 } ?: 0.0,
                    savings.maxOfOrNull { it.amount.toDoubleOrNull() ?: 0.0 } ?: 0.0
                ).toFloat().coerceAtLeast(1f)

                val dataCount = income.size.coerceAtLeast(1)
                val stepX = (w - padding * 2) / (dataCount - 1).coerceAtLeast(1)

                val incomePath = Path()
                val expensePath = Path()
                val savingsPath = Path()

                income.forEachIndexed { index, item ->
                    val x = padding + index * stepX
                    val y = h - padding - ((item.amount.toFloatOrNull() ?: 0f) / maxValue * (h - padding * 2))
                    if (index == 0) incomePath.moveTo(x, y) else incomePath.lineTo(x, y)
                }

                expense.forEachIndexed { index, item ->
                    val x = padding + index * stepX
                    val y = h - padding - ((item.amount.toFloatOrNull() ?: 0f) / maxValue * (h - padding * 2))
                    if (index == 0) expensePath.moveTo(x, y) else expensePath.lineTo(x, y)
                }

                savings.forEachIndexed { index, item ->
                    val x = padding + index * stepX
                    val y = h - padding - ((item.amount.toFloatOrNull() ?: 0f) / maxValue * (h - padding * 2))
                    if (index == 0) savingsPath.moveTo(x, y) else savingsPath.lineTo(x, y)
                }

                drawPath(
                    path = incomePath,
                    color = Color(0xFF4CAF50),
                    style = Stroke(width = 3f, cap = StrokeCap.Round)
                )
                drawPath(
                    path = expensePath,
                    color = Color(0xFFF44336),
                    style = Stroke(width = 3f, cap = StrokeCap.Round)
                )
                drawPath(
                    path = savingsPath,
                    color = Color(0xFF9C27B0),
                    style = Stroke(width = 3f, cap = StrokeCap.Round)
                )

                income.forEachIndexed { index, item ->
                    val x = padding + index * stepX
                    val y = h - padding - ((item.amount.toFloatOrNull() ?: 0f) / maxValue * (h - padding * 2))
                    drawCircle(IncomeGreen, radius = 5f, center = Offset(x, y))
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                income.forEach { item ->
                    val parts = item.date.split("-")
                    val monthLabel = if (parts.size >= 2) parts[1] else item.date
                    Text(
                        text = "Tháng $monthLabel",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(50.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun IncomeExpenseBarChart(
    income: List<TrendItem>,
    expense: List<TrendItem>,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "So sánh thu chi theo tháng",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                val w = size.width
                val h = size.height
                val barPadding = 12f
                val groupPadding = 20f

                val maxValue = maxOf(
                    income.maxOfOrNull { it.amount.toDoubleOrNull() ?: 0.0 } ?: 0.0,
                    expense.maxOfOrNull { it.amount.toDoubleOrNull() ?: 0.0 } ?: 0.0
                ).toFloat().coerceAtLeast(1f)

                val groupCount = income.size.coerceAtLeast(1)
                val barGroupWidth = (w - barPadding * 2) / groupCount
                val barWidth = (barGroupWidth - groupPadding) / 2

                income.forEachIndexed { index, item ->
                    val groupX = barPadding + index * barGroupWidth
                    val incomeBarH = (item.amount.toFloatOrNull() ?: 0f) / maxValue * (h - 30f)
                    val expenseAmt = expense.getOrNull(index)?.amount?.toFloatOrNull() ?: 0f
                    val expenseBarH = expenseAmt / maxValue * (h - 30f)

                    drawRect(
                        color = IncomeGreen,
                        topLeft = Offset(groupX, h - 30f - incomeBarH),
                        size = Size(barWidth, incomeBarH)
                    )
                    drawRect(
                        color = ExpenseRed,
                        topLeft = Offset(groupX + barWidth + 4f, h - 30f - expenseBarH),
                        size = Size(barWidth, expenseBarH)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendDot(color = IncomeGreen, label = "Thu nhập")
                LegendDot(color = ExpenseRed, label = "Chi tiêu")
            }
        }
    }
}

@Composable
private fun ExpensePieChart(
    categories: List<CategoryAmount>,
    modifier: Modifier = Modifier
) {
    val colors = listOf(
        Color(0xFFE91E63), Color(0xFF9C27B0), Color(0xFF673AB7),
        Color(0xFF3F51B5), Color(0xFF2196F3), Color(0xFF00BCD4),
        Color(0xFF009688), Color(0xFF4CAF50), Color(0xFF8BC34A),
        Color(0xFFFF9800), Color(0xFFFF5722), Color(0xFF795548)
    )

    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Phân bổ chi tiêu theo danh mục",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Canvas(
                    modifier = Modifier.size(140.dp)
                ) {
                    var startAngle = -90f

                    categories.forEachIndexed { index, cat ->
                        val percentage = (cat.percentage / 100.0).toFloat()
                        val sweepAngle = percentage * 360f
                        val color = colors[index % colors.size]

                        drawArc(
                            color = color,
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = true
                        )
                        startAngle += sweepAngle
                    }
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.width(140.dp)
                ) {
                    categories.take(6).forEachIndexed { index, cat ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(colors[index % colors.size])
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = cat.categoryName,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "${cat.percentage.toInt()}%",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@Composable
private fun CategoryExpenseItem(
    category: CategoryAmount,
    totalExpense: Double
) {
    val percentage = if (totalExpense > 0) {
        (category.amount.toDoubleOrNull() ?: 0.0) / totalExpense * 100
    } else 0.0

    val color = try {
        Color(android.graphics.Color.parseColor(category.categoryColor ?: "#607D8B"))
    } catch (e: Exception) {
        Primary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getCategoryIcon(category.categoryIcon),
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = category.categoryName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${formatCurrency(category.amount.toDoubleOrNull() ?: 0.0)}đ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "${percentage.roundToInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { (percentage / 100.0).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = color,
                trackColor = color.copy(alpha = 0.15f),
            )
        }
    }
}

private fun getCategoryIcon(iconName: String?): androidx.compose.ui.graphics.vector.ImageVector {
    return when (iconName) {
        "restaurant" -> Icons.Default.Restaurant
        "directions_car" -> Icons.Default.DirectionsCar
        "shopping_cart" -> Icons.Default.ShoppingCart
        "movie" -> Icons.Default.Movie
        "receipt" -> Icons.Default.Receipt
        "local_hospital" -> Icons.Default.LocalHospital
        "school" -> Icons.Default.School
        "home" -> Icons.Default.Home
        "call_made" -> Icons.Default.CallMade
        "more_horiz" -> Icons.Default.MoreHoriz
        else -> Icons.Default.Category
    }
}

private fun formatCurrency(amount: Double): String {
    val formatter = java.text.NumberFormat.getCurrencyInstance(java.util.Locale("vi", "VN"))
    return formatter.format(amount).replace("₫", "").trim()
}
