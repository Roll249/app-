package com.fintech.ui.fund

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fintech.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FundConfigurationScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLinkedAccounts: () -> Unit,
    viewModel: FundViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingFund by remember { mutableStateOf<Fund?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadFunds()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cấu hình quỹ chi tiêu") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToLinkedAccounts) {
                        Icon(Icons.Default.AccountBalance, contentDescription = "Liên kết ngân hàng")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Thêm quỹ", tint = Color.White)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Total Balance Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = PrimaryContainer)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Tổng số dư liên kết",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = formatCurrency(state.totalBalance),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = OnPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        if (state.linkedBalance > 0) {
                            Text(
                                text = "Ngân hàng: ${formatCurrency(state.linkedBalance)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                        if (state.cashBalance > 0) {
                            Text(
                                text = "Tiền mặt: ${formatCurrency(state.cashBalance)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = onNavigateToLinkedAccounts) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Liên kết thêm tài khoản")
                    }
                }
            }

            // Fund List Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Các quỹ chi tiêu",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${state.funds.size} quỹ",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant
                )
            }

            if (state.funds.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
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
                            "Chưa có quỹ chi tiêu",
                            style = MaterialTheme.typography.bodyLarge,
                            color = OnSurfaceVariant
                        )
                        Text(
                            "Tạo quỹ để bắt đầu phân bổ tiền",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant
                        )
                    }
                }
            } else {
                // Fund allocation summary
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = SecondaryContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${state.funds.size}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = OnSecondaryContainer
                            )
                            Text(
                                text = "Quỹ",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSecondaryContainer
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = formatCurrency(state.funds.sumOf { it.targetAmount }),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = OnSecondaryContainer
                            )
                            Text(
                                text = "Tổng mục tiêu",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSecondaryContainer
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val totalProgress = if (state.funds.isNotEmpty()) {
                                state.funds.sumOf { it.progress } / state.funds.size
                            } else 0.0
                            Text(
                                text = "${totalProgress.toInt()}%",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = OnSecondaryContainer
                            )
                            Text(
                                text = "Trung bình",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSecondaryContainer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Fund list
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.funds) { fund ->
                        FundConfigurationCard(
                            fund = fund,
                            totalBalance = state.totalBalance,
                            onEdit = { editingFund = fund },
                            onDelete = { viewModel.deleteFund(fund.id) }
                        )
                    }
                }
            }
        }
    }

    // Add Fund Dialog
    if (showAddDialog) {
        AddFundDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, targetAmount, icon, color ->
                viewModel.createFund(name, targetAmount, icon, color)
                showAddDialog = false
            }
        )
    }

    // Edit Fund Dialog
    editingFund?.let { fund ->
        EditFundDialog(
            fund = fund,
            onDismiss = { editingFund = null },
            onConfirm = { name, targetAmount, icon, color ->
                // TODO: implement update
                editingFund = null
            }
        )
    }
}

@Composable
private fun FundConfigurationCard(
    fund: Fund,
    totalBalance: Double,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
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
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(fundColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Savings,
                            contentDescription = null,
                            tint = fundColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = fund.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Mục tiêu: ${formatCurrency(fund.targetAmount)} VNĐ",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant
                        )
                    }
                }
                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Sửa",
                            modifier = Modifier.size(18.dp),
                            tint = OnSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Xóa",
                            modifier = Modifier.size(18.dp),
                            tint = Error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { progress.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = fundColor,
                trackColor = fundColor.copy(alpha = 0.2f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatCurrency(fund.currentAmount),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = fundColor
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = fundColor
                )
            }

            // Allocation percentage
            val allocationPercent = if (totalBalance > 0) {
                (fund.targetAmount / totalBalance * 100).coerceIn(0.0, 100.0)
            } else 0.0
            Text(
                text = "Phân bổ: ${allocationPercent.toInt()}% tổng số dư",
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddFundDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Double, String?, String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var targetAmount by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf("savings") }
    var selectedColor by remember { mutableStateOf("#4CAF50") }

    val icons = listOf(
        "savings" to Icons.Default.Savings,
        "flight" to Icons.Default.Flight,
        "directions_car" to Icons.Default.DirectionsCar,
        "home" to Icons.Default.Home,
        "school" to Icons.Default.School,
        "local_hospital" to Icons.Default.LocalHospital,
        "shopping_cart" to Icons.Default.ShoppingCart,
        "restaurant" to Icons.Default.Restaurant,
        "sports_esports" to Icons.Default.SportsEsports,
        "beach_access" to Icons.Default.BeachAccess
    )

    val colors = listOf(
        "#4CAF50", "#2196F3", "#FF9800", "#F44336",
        "#9C27B0", "#00BCD4", "#795548", "#607D8B"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Thêm quỹ chi tiêu") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tên quỹ") },
                    placeholder = { Text("VD: Quỹ du lịch") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = targetAmount,
                    onValueChange = { targetAmount = it.filter { c -> c.isDigit() } },
                    label = { Text("Số tiền mục tiêu (VNĐ)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text("Biểu tượng", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    icons.take(5).forEach { (iconName, icon) ->
                        val isSelected = selectedIcon == iconName
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) Primary.copy(alpha = 0.2f) else SurfaceContainerLow)
                                .clickable { selectedIcon = iconName },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(icon, contentDescription = iconName, modifier = Modifier.size(20.dp))
                        }
                    }
                }

                Text("Màu sắc", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    colors.take(4).forEach { color ->
                        val isSelected = selectedColor == color
                        val parsedColor = try {
                            Color(android.graphics.Color.parseColor(color))
                        } catch (e: Exception) {
                            Primary
                        }
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(parsedColor)
                                .then(
                                    if (isSelected) Modifier.border(2.dp, OnSurface, CircleShape)
                                    else Modifier
                                )
                                .clickable { selectedColor = color }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amount = targetAmount.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank() && amount > 0) {
                        onConfirm(name, amount, selectedIcon, selectedColor)
                    }
                },
                enabled = name.isNotBlank() && (targetAmount.toDoubleOrNull() ?: 0.0) > 0
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
private fun EditFundDialog(
    fund: Fund,
    onDismiss: () -> Unit,
    onConfirm: (String, Double, String?, String?) -> Unit
) {
    var name by remember { mutableStateOf(fund.name) }
    var targetAmount by remember { mutableStateOf(fund.targetAmount.toLong().toString()) }
    var selectedIcon by remember { mutableStateOf(fund.icon ?: "savings") }
    var selectedColor by remember { mutableStateOf(fund.color ?: "#4CAF50") }

    val icons = listOf(
        "savings" to Icons.Default.Savings,
        "flight" to Icons.Default.Flight,
        "directions_car" to Icons.Default.DirectionsCar,
        "home" to Icons.Default.Home,
        "school" to Icons.Default.School
    )

    val colors = listOf(
        "#4CAF50", "#2196F3", "#FF9800", "#F44336",
        "#9C27B0", "#00BCD4"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Chỉnh sửa quỹ") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tên quỹ") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = targetAmount,
                    onValueChange = { targetAmount = it.filter { c -> c.isDigit() } },
                    label = { Text("Số tiền mục tiêu (VNĐ)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text("Màu sắc", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    colors.forEach { color ->
                        val isSelected = selectedColor == color
                        val parsedColor = try {
                            Color(android.graphics.Color.parseColor(color))
                        } catch (e: Exception) {
                            Primary
                        }
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(parsedColor)
                                .then(
                                    if (isSelected) Modifier.border(2.dp, OnSurface, CircleShape)
                                    else Modifier
                                )
                                .clickable { selectedColor = color }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amount = targetAmount.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank() && amount > 0) {
                        onConfirm(name, amount, selectedIcon, selectedColor)
                    }
                },
                enabled = name.isNotBlank() && (targetAmount.toDoubleOrNull() ?: 0.0) > 0
            ) {
                Text("Lưu")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}

private fun formatCurrency(amount: Double): String {
    return NumberFormat.getNumberInstance(Locale("vi", "VN"))
        .format(amount.toLong())
}
