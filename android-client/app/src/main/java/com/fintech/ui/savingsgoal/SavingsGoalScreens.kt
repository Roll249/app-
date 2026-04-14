package com.fintech.ui.savingsgoal

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fintech.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavingsGoalListScreen(
    onNavigateBack: () -> Unit,
    onAddGoal: () -> Unit,
    viewModel: SavingsGoalViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadGoals()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mục tiêu tiết kiệm") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddGoal) {
                Icon(Icons.Default.Add, contentDescription = "Thêm mục tiêu")
            }
        }
    ) { padding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (state.goals.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Savings,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = OnSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Chưa có mục tiêu tiết kiệm",
                        style = MaterialTheme.typography.bodyLarge,
                        color = OnSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Tạo mục tiêu để bắt đầu tiết kiệm",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(state.goals) { goal ->
                    SavingsGoalCard(
                        goal = goal,
                        onContribute = { amount ->
                            viewModel.contribute(goal.id, amount)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SavingsGoalCard(
    goal: SavingsGoal,
    onContribute: (Double) -> Unit
) {
    var showContributeDialog by remember { mutableStateOf(false) }
    val progress = (goal.currentAmount / goal.targetAmount).coerceIn(0.0, 1.0)
    val formattedCurrency = remember { NumberFormat.getNumberInstance(Locale("vi", "VN")) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = goal.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = when (goal.period) {
                            "MONTHLY" -> "Hàng tháng"
                            "QUARTERLY" -> "Hàng quý"
                            "YEARLY" -> "Hàng năm"
                            else -> goal.period
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Savings,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progress
            LinearProgressIndicator(
                progress = { progress.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Primary,
                trackColor = SurfaceContainerLow
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${formattedCurrency.format(goal.currentAmount.toLong())} VNĐ",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant
                )
            }

            Text(
                text = "Mục tiêu: ${formattedCurrency.format(goal.targetAmount.toLong())} VNĐ",
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceVariant
            )

            if (goal.amountPerPeriod > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tiết kiệm: ${formattedCurrency.format(goal.amountPerPeriod.toLong())} VNĐ/kỳ",
                    style = MaterialTheme.typography.bodySmall,
                    color = Secondary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { showContributeDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Thêm tiết kiệm")
            }
        }
    }

    if (showContributeDialog) {
        ContributeDialog(
            goalName = goal.name,
            onDismiss = { showContributeDialog = false },
            onContribute = { amount ->
                onContribute(amount)
                showContributeDialog = false
            }
        )
    }
}

@Composable
private fun ContributeDialog(
    goalName: String,
    onDismiss: () -> Unit,
    onContribute: (Double) -> Unit
) {
    var amount by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Thêm tiết kiệm vào \"$goalName\"") },
        text = {
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it.filter { c -> c.isDigit() } },
                label = { Text("Số tiền (VNĐ)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    amount.toDoubleOrNull()?.let { onContribute(it) }
                },
                enabled = amount.isNotBlank() && (amount.toDoubleOrNull() ?: 0.0) > 0
            ) {
                Text("Thêm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSavingsGoalScreen(
    onNavigateBack: () -> Unit,
    onGoalCreated: () -> Unit,
    viewModel: SavingsGoalViewModel = hiltViewModel()
) {
    var name by remember { mutableStateOf("") }
    var targetAmount by remember { mutableStateOf("") }
    var amountPerPeriod by remember { mutableStateOf("") }
    var selectedPeriod by remember { mutableStateOf("MONTHLY") }

    val periodOptions = listOf(
        "MONTHLY" to "Hàng tháng",
        "QUARTERLY" to "Hàng quý",
        "YEARLY" to "Hàng năm"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tạo mục tiêu tiết kiệm") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Close, contentDescription = "Hủy")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            viewModel.createGoal(
                                name = name,
                                targetAmount = targetAmount.toDoubleOrNull() ?: 0.0,
                                amountPerPeriod = amountPerPeriod.toDoubleOrNull() ?: 0.0,
                                period = selectedPeriod
                            )
                            onGoalCreated()
                        },
                        enabled = name.isNotBlank() && (targetAmount.toDoubleOrNull() ?: 0.0) > 0
                    ) {
                        Text("Lưu")
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Tên mục tiêu") },
                placeholder = { Text("VD: Mua xe máy mới") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = targetAmount,
                onValueChange = { targetAmount = it.filter { c -> c.isDigit() } },
                label = { Text("Số tiền mục tiêu (VNĐ)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )

            // Period selector
            Text(
                text = "Chu kỳ tiết kiệm",
                style = MaterialTheme.typography.labelMedium,
                color = OnSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                periodOptions.forEach { (value, label) ->
                    FilterChip(
                        selected = selectedPeriod == value,
                        onClick = { selectedPeriod = value },
                        label = { Text(label) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            OutlinedTextField(
                value = amountPerPeriod,
                onValueChange = { amountPerPeriod = it.filter { c -> c.isDigit() } },
                label = { Text("Số tiền mỗi kỳ (VNĐ) - Tùy chọn") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                leadingIcon = { Icon(Icons.Default.Savings, contentDescription = null) },
                supportingText = { Text("Số tiền bạn muốn tiết kiệm mỗi ${periodOptions.find { it.first == selectedPeriod }?.second?.lowercase()}") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
