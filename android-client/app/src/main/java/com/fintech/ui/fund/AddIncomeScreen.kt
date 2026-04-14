package com.fintech.ui.fund

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fintech.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddIncomeScreen(
    onNavigateBack: () -> Unit,
    onIncomeAdded: () -> Unit,
    viewModel: FundViewModel = hiltViewModel()
) {
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) }

    val tabs = listOf("Thêm tiền", "Chia quỹ")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thêm nguồn tiền") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Close, contentDescription = "Hủy")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tab selector
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = SurfaceContainerLow
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            when (selectedTab) {
                0 -> AddIncomeTab(
                    amount = amount,
                    onAmountChange = { amount = it.filter { c -> c.isDigit() } },
                    description = description,
                    onDescriptionChange = { description = it },
                    onSubmit = {
                        amount.toDoubleOrNull()?.let {
                            onIncomeAdded()
                        }
                    }
                )
                1 -> AIFundAllocationTab(
                    onAllocationComplete = {
                        onIncomeAdded()
                    }
                )
            }
        }
    }
}

@Composable
private fun AddIncomeTab(
    amount: String,
    onAmountChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Amount input
        OutlinedTextField(
            value = amount,
            onValueChange = onAmountChange,
            label = { Text("Số tiền") },
            placeholder = { Text("0") },
            leadingIcon = { Text("VND", style = MaterialTheme.typography.bodyMedium) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Description
        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = { Text("Mô tả (tùy chọn)") },
            placeholder = { Text("VD: Lương tháng, Thưởng...") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Quick amounts
        Text(
            text = "Số tiền nhanh",
            style = MaterialTheme.typography.labelMedium,
            color = OnSurfaceVariant
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("500,000", "1,000,000", "2,000,000", "5,000,000").forEach { quickAmount ->
                val amountValue = quickAmount.replace(",", "").toDoubleOrNull() ?: 0.0
                FilterChip(
                    selected = amount == amountValue.toLong().toString(),
                    onClick = { onAmountChange(amountValue.toLong().toString()) },
                    label = { Text(quickAmount) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Submit button
        Button(
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth(),
            enabled = amount.isNotBlank() && (amount.toDoubleOrNull() ?: 0.0) > 0,
            colors = ButtonDefaults.buttonColors(containerColor = Primary)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Thêm tiền")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AIFundAllocationTab(
    onAllocationComplete: () -> Unit,
    viewModel: FundViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var amount by remember { mutableStateOf("") }
    var showAllocationDialog by remember { mutableStateOf(false) }
    var allocationSuggestion by remember { mutableStateOf<AllocationSuggestionState?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadFunds()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Info card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Primary.copy(alpha = 0.1f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Chia quỹ thông minh",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                    Text(
                        text = "AI sẽ phân tích và đề xuất cách chia tiền vào các quỹ tiết kiệm của bạn",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )
                }
            }
        }

        // Amount input
        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it.filter { c -> c.isDigit() } },
            label = { Text("Số tiền muốn chia") },
            placeholder = { Text("Nhập số tiền...") },
            leadingIcon = { Text("VND", style = MaterialTheme.typography.bodyMedium) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Funds preview
        Text(
            text = "Các quỹ hiện tại",
            style = MaterialTheme.typography.labelMedium,
            color = OnSurfaceVariant
        )

        if (state.funds.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = OnSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Chưa có quỹ nào",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant
                    )
                    Text(
                        text = "Tạo quỹ trước để sử dụng chia quỹ",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.funds.take(3)) { fund ->
                    FundPreviewCard(fund = fund)
                }
            }
        }

        // Get AI suggestion button
        Button(
            onClick = {
                val inputAmount = amount.toDoubleOrNull() ?: 0.0
                if (inputAmount > 0 && state.funds.isNotEmpty()) {
                    showAllocationDialog = true
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = amount.isNotBlank() && 
                       (amount.toDoubleOrNull() ?: 0.0) > 0 && 
                       state.funds.isNotEmpty(),
            colors = ButtonDefaults.buttonColors(containerColor = Secondary)
        ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Fina phân tích và chia quỹ")
        }
    }

    // AI Allocation Dialog
    if (showAllocationDialog) {
        AIAllocationDialog(
            amount = amount.toDoubleOrNull() ?: 0.0,
            funds = state.funds,
            onDismiss = { showAllocationDialog = false },
            onConfirm = {
                showAllocationDialog = false
                onAllocationComplete()
            }
        )
    }
}

@Composable
private fun FundPreviewCard(fund: Fund) {
    val progress = (fund.currentAmount / fund.targetAmount).coerceIn(0.0, 1.0)
    val fundColor = try {
        Color(android.graphics.Color.parseColor(fund.color ?: "#4CAF50"))
    } catch (e: Exception) {
        Primary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(fundColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Savings,
                        contentDescription = null,
                        tint = fundColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = fund.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = fundColor
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatCurrency(fund.currentAmount),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "/ ${formatCurrency(fund.targetAmount)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AIAllocationDialog(
    amount: Double,
    funds: List<Fund>,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    var isLoading by remember { mutableStateOf(true) }
    var allocations by remember { mutableStateOf<List<AllocationDisplay>?>(null) }
    var suggestion by remember { mutableStateOf<String?>(null) }
    var selectedIndices by remember { mutableStateOf(setOf<Int>()) }

    LaunchedEffect(Unit) {
        // Simulate AI allocation (in real app, call API)
        kotlinx.coroutines.delay(1500)
        
        val fundAmounts = funds.map { fund ->
            val progress = fund.currentAmount / fund.targetAmount
            // Prioritize funds with lower progress
            val priority = if (progress < 0.3) 3 else if (progress < 0.7) 2 else 1
            Pair(fund, priority)
        }.sortedBy { it.second }.reversed()

        val total = fundAmounts.sumOf { it.first.targetAmount - it.first.currentAmount }
        val remaining = amount

        var left = remaining
        val results = fundAmounts.mapIndexed { index, (fund, _) ->
            val needed = (fund.targetAmount - fund.currentAmount).coerceAtLeast(0.0)
            val allocation = if (needed > 0 && left > 0) {
                minOf(needed, left / (fundAmounts.size - index))
            } else 0.0
            left -= allocation
            AllocationDisplay(
                fund = fund,
                amount = allocation,
                reason = when {
                    fund.currentAmount / fund.targetAmount < 0.3 -> "Quỹ cần ưu tiên"
                    allocation > 0 -> "Bổ sung quỹ"
                    else -> "Quỹ đã đạt mục tiêu"
                }
            )
        }.filter { it.amount > 0 || it.fund.currentAmount / it.fund.targetAmount < 1.0 }

        allocations = results
        selectedIndices = results.filter { it.amount > 0 }.map { results.indexOf(it) }.toSet()
        suggestion = "Ưu tiên quỹ có tiến độ thấp nhất để đạt mục tiêu sớm nhất."
        isLoading = false
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Secondary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Fina gợi ý chia quỹ")
            }
        },
        text = {
            Column {
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Secondary)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Fina đang phân tích...",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                } else if (allocations != null) {
                    // Summary
                    Text(
                        text = "Số tiền: ${formatCurrency(amount)} VNĐ",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Suggestion
                    if (suggestion != null) {
                        Text(
                            text = suggestion!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Allocation list
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 250.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(allocations!!.filter { it.amount > 0 || selectedIndices.contains(allocations!!.indexOf(it)) }) { allocation ->
                            val index = allocations!!.indexOf(allocation)
                            val isSelected = selectedIndices.contains(index)
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(
                                        1.dp,
                                        if (isSelected) Secondary else OutlineVariant,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        selectedIndices = if (isSelected) {
                                            selectedIndices - index
                                        } else {
                                            selectedIndices + index
                                        }
                                    }
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = {
                                            selectedIndices = if (it) {
                                                selectedIndices + index
                                            } else {
                                                selectedIndices - index
                                            }
                                        }
                                    )
                                    Column {
                                        Text(
                                            text = allocation.fund.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = allocation.reason,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = OnSurfaceVariant
                                        )
                                    }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = formatCurrency(allocation.amount),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (allocation.amount > 0) Primary else OnSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tổng: ${formatCurrency(selectedIndices.mapNotNull { idx -> allocations?.getOrNull(idx)?.amount }.sum())} VNĐ",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = !isLoading && selectedIndices.isNotEmpty()
            ) {
                Text("Xác nhận chia quỹ")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}

data class AllocationDisplay(
    val fund: Fund,
    val amount: Double,
    val reason: String
)

data class AllocationSuggestionState(
    val allocations: List<AllocationDisplay>,
    val suggestion: String
)

private fun formatCurrency(amount: Double): String {
    return NumberFormat.getNumberInstance(Locale("vi", "VN"))
        .format(amount.toLong())
}
