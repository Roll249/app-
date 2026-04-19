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
    var deletingFund by remember { mutableStateOf<Fund?>(null) }
    var contributingToFund by remember { mutableStateOf<Fund?>(null) }
    var showSourceDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadFunds()
    }

    val totalAllocated = state.funds.sumOf { it.currentAmount }
    val totalTargetAmount = state.funds.sumOf { it.targetAmount }
    val unallocatedMoney = state.totalBalance - totalAllocated

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
                    IconButton(onClick = { showSourceDialog = true }) {
                        Icon(Icons.Default.AccountBalance, contentDescription = "Liên kết ngân hàng")
                    }
                }
            )
        },
        floatingActionButton = {
            if (unallocatedMoney > 0) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = Primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Thêm quỹ", tint = Color.White)
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                TotalBalanceCard(
                    totalBalance = state.totalBalance,
                    linkedBalance = state.linkedBalance,
                    cashBalance = state.cashBalance,
                    onSourceClick = { showSourceDialog = true }
                )
            }

            item {
                AllocationSummaryCard(
                    totalAllocated = totalAllocated,
                    totalTargetAmount = totalTargetAmount,
                    unallocatedMoney = unallocatedMoney,
                    totalBalance = state.totalBalance
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
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
            }

            if (state.funds.isEmpty()) {
                item {
                    EmptyFundState(
                        unallocatedMoney = unallocatedMoney,
                        onAddFund = { showAddDialog = true }
                    )
                }
            } else {
                items(state.funds) { fund ->
                    FundCard(
                        fund = fund,
                        onContribute = { contributingToFund = fund },
                        onEdit = { editingFund = fund },
                        onDelete = { deletingFund = fund }
                    )
                }
            }
        }
    }

    // Source of Money Dialog
    if (showSourceDialog) {
        SourceOfMoneyDialog(
            linkedBalance = state.linkedBalance,
            cashBalance = state.cashBalance,
            onDismiss = { showSourceDialog = false },
            onAddAccount = {
                showSourceDialog = false
                onNavigateToLinkedAccounts()
            }
        )
    }

    // Add Fund Dialog
    if (showAddDialog) {
        AddFundDialog(
            availableAmount = unallocatedMoney,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, targetAmount, icon, color ->
                viewModel.createFund(name, targetAmount, icon, color)
                showAddDialog = false
            },
            onCreateWithRemaining = { name, icon, color ->
                viewModel.createFund(name, unallocatedMoney, icon, color)
                showAddDialog = false
            }
        )
    }

    // Contribute to Fund Dialog
    contributingToFund?.let { fund ->
        ContributeDialog(
            fund = fund,
            availableAmount = unallocatedMoney + fund.targetAmount,
            onDismiss = { contributingToFund = null },
            onConfirm = { amount ->
                viewModel.contribute(fund.id, amount)
                contributingToFund = null
            }
        )
    }

    // Edit Fund Dialog
    editingFund?.let { fund ->
        EditFundDialog(
            fund = fund,
            onDismiss = { editingFund = null },
            onConfirm = { name, targetAmount, icon, color ->
                editingFund = null
            }
        )
    }

    // Delete Confirmation
    deletingFund?.let { fund ->
        AlertDialog(
            onDismissRequest = { deletingFund = null },
            title = { Text("Xóa quỹ") },
            text = {
                Text("Bạn có chắc muốn xóa quỹ \"${fund.name}\"?\n\nSố tiền ${formatCurrency(fund.targetAmount)} sẽ được hoàn trả vào \"Tiền chưa có kế hoạch\".")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteFund(fund.id)
                        deletingFund = null
                    }
                ) {
                    Text("Xóa", color = Error)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingFund = null }) {
                    Text("Hủy")
                }
            }
        )
    }
}

@Composable
private fun TotalBalanceCard(
    totalBalance: Double,
    linkedBalance: Double,
    cashBalance: Double,
    onSourceClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                text = formatCurrency(totalBalance),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = OnPrimaryContainer
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                TextButton(
                    onClick = onSourceClick,
                    colors = ButtonDefaults.textButtonColors(contentColor = OnPrimaryContainer)
                ) {
                    Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Nguồn tiền")
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun AllocationSummaryCard(
    totalAllocated: Double,
    totalTargetAmount: Double,
    unallocatedMoney: Double,
    totalBalance: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow)
    ) {
            Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Phân bổ tiền",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Đã lên kế hoạch",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )
                    Text(
                        text = formatCurrency(totalTargetAmount),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Chưa có kế hoạch",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )
                    Text(
                        text = formatCurrency(unallocatedMoney),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (unallocatedMoney > 0) Secondary else Error
                    )
                }
            }

            if (totalBalance > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                val plannedPercent = (totalTargetAmount / totalBalance * 100).toInt()
                LinearProgressIndicator(
                    progress = { (totalTargetAmount / totalBalance).toFloat().coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Primary,
                    trackColor = Secondary.copy(alpha = 0.3f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$plannedPercent% đã lên kế hoạch",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SourceOfMoneyDialog(
    linkedBalance: Double,
    cashBalance: Double,
    onDismiss: () -> Unit,
    onAddAccount: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nguồn tiền") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Các tài khoản liên kết",
                    style = MaterialTheme.typography.labelMedium,
                    color = OnSurfaceVariant
                )
                
                // Bank account
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.AccountBalance,
                                contentDescription = null,
                                tint = Primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Ngân hàng liên kết", fontWeight = FontWeight.Medium)
                                Text(
                                    "VCB",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OnSurfaceVariant
                                )
                            }
                        }
                        Text(
                            formatCurrency(linkedBalance),
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                    }
                }

                // Cash account
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Wallet,
                                contentDescription = null,
                                tint = Secondary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Tiền mặt", fontWeight = FontWeight.Medium)
                                Text(
                                    "Sử dụng cho quỹ",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OnSurfaceVariant
                                )
                            }
                        }
                        Text(
                            formatCurrency(cashBalance),
                            fontWeight = FontWeight.Bold,
                            color = Secondary
                        )
                    }
                }

                TextButton(
                    onClick = onAddAccount,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Liên kết tài khoản ngân hàng")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng")
            }
        }
    )
}

@Composable
private fun EmptyFundState(
    unallocatedMoney: Double,
    onAddFund: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
            Spacer(modifier = Modifier.height(8.dp))

            if (unallocatedMoney > 0) {
                Text(
                    "Bạn có ${formatCurrency(unallocatedMoney)} chưa có kế hoạch",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onAddFund) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tạo quỹ đầu tiên")
                }
            } else {
                Text(
                    "Tất cả tiền đã được phân bổ vào các quỹ",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun FundCard(
    fund: Fund,
    onContribute: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
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
                            text = "Mục tiêu: ${formatCurrency(fund.targetAmount)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant
                        )
                    }
                }
                Row {
                    if (fund.currentAmount < fund.targetAmount) {
                        IconButton(onClick = onContribute, modifier = Modifier.size(36.dp)) {
                            Icon(
                                Icons.Default.AddCircle,
                                contentDescription = "Nạp tiền",
                                modifier = Modifier.size(20.dp),
                                tint = Primary
                            )
                        }
                    }
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

            val progress = if (fund.targetAmount > 0) {
                (fund.currentAmount / fund.targetAmount).coerceIn(0.0, 1.0)
            } else 0.0

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
                    text = "${formatCurrency(fund.currentAmount)} / ${formatCurrency(fund.targetAmount)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = fundColor
                )
            }

            if (fund.currentAmount < fund.targetAmount) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onContribute,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = fundColor)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Nạp thêm tiền vào hũ")
                }
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Primary.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Đã hoàn thành!",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AddFundDialog(
    availableAmount: Double,
    onDismiss: () -> Unit,
    onConfirm: (String, Double, String?, String?) -> Unit,
    onCreateWithRemaining: (String, String?, String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var targetAmount by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf("savings") }
    var selectedColor by remember { mutableStateOf("#4CAF50") }
    var showError by remember { mutableStateOf(false) }
    var showRemainingConfirm by remember { mutableStateOf(false) }

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

    // If first fund, auto-fill with all available money
    LaunchedEffect(Unit) {
        if (availableAmount > 0) {
            targetAmount = availableAmount.toLong().toString()
        }
    }

    if (showRemainingConfirm) {
        AlertDialog(
            onDismissRequest = { showRemainingConfirm = false },
            title = { Text("Số tiền vượt quá khả dụng") },
            text = {
                Text("Bạn nhập nhiều hơn số tiền khả dụng (${formatCurrency(availableAmount)}).\n\nTạo quỹ với toàn bộ số tiền còn lại?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onCreateWithRemaining(name, selectedIcon, selectedColor)
                    }
                ) {
                    Text("Tạo với ${formatCurrency(availableAmount)}")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRemainingConfirm = false }) {
                    Text("Nhập lại")
                }
            }
        )
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Thêm quỹ chi tiêu") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SecondaryContainer)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Số tiền khả dụng:", style = MaterialTheme.typography.bodySmall)
                            Text(
                                formatCurrency(availableAmount),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Secondary
                            )
                        }
                    }

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it; showError = false },
                        label = { Text("Tên quỹ") },
                        placeholder = { Text("VD: Quỹ du lịch") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = showError && name.isBlank()
                    )

                    OutlinedTextField(
                        value = targetAmount,
                        onValueChange = {
                            targetAmount = it.filter { c -> c.isDigit() }
                            showError = false
                        },
                        label = { Text("Số tiền phân bổ (VNĐ)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = showError,
                        supportingText = {
                            val amount = targetAmount.toDoubleOrNull() ?: 0.0
                            if (amount > availableAmount) {
                                Text("Vượt quá khả dụng", color = Error)
                            }
                        }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(0.25, 0.5, 0.75, 1.0).forEach { fraction ->
                            val amount = (availableAmount * fraction).toLong()
                            val percent = (fraction * 100).toInt()
                            OutlinedButton(
                                onClick = { targetAmount = amount.toString() },
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                            ) {
                                Text("$percent%", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }

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
                        when {
                            name.isBlank() -> showError = true
                            amount <= 0 -> showError = true
                            amount > availableAmount -> showRemainingConfirm = true
                            else -> onConfirm(name, amount, selectedIcon, selectedColor)
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
}

@Composable
private fun ContributeDialog(
    fund: Fund,
    availableAmount: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var showOverflowAlert by remember { mutableStateOf(false) }

    val remainingToFill = (fund.targetAmount - fund.currentAmount).coerceAtLeast(0.0)
    val maxCanContribute = minOf(remainingToFill, availableAmount)

    if (remainingToFill <= 0) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Quỹ đã đầy") },
            text = { Text("${fund.name} đã đạt mục tiêu ${formatCurrency(fund.targetAmount)}. Không thể nạp thêm.") },
            confirmButton = {
                TextButton(onClick = onDismiss) { Text("Đóng") }
            }
        )
        return
    }

    if (showOverflowAlert) {
        AlertDialog(
            onDismissRequest = { showOverflowAlert = false },
            title = { Text("Số tiền vượt quá giới hạn") },
            text = {
                Text("Quỹ chỉ còn thiếu ${formatCurrency(remainingToFill)}.\n\nBạn có muốn nạp đủ ${formatCurrency(remainingToFill)} không?")
            },
            confirmButton = {
                TextButton(onClick = {
                    onConfirm(remainingToFill)
                    onDismiss()
                }) {
                    Text("Nạp đủ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showOverflowAlert = false }) {
                    Text("Nhập lại")
                }
            }
        )
        return
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nạp tiền vào ${fund.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Mục tiêu:")
                            Text(formatCurrency(fund.targetAmount), fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Hiện tại:")
                            Text(formatCurrency(fund.currentAmount))
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Còn thiếu:")
                            Text(formatCurrency(remainingToFill), color = Primary)
                        }
                        if (availableAmount < remainingToFill) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Bạn có:")
                                Text(formatCurrency(availableAmount), color = Secondary)
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = amount,
                    onValueChange = {
                        amount = it.filter { c -> c.isDigit() }
                        showError = false
                    },
                    label = { Text("Số tiền nạp (VNĐ)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = showError,
                    supportingText = {
                        val amountValue = amount.toDoubleOrNull() ?: 0.0
                        if (amountValue > remainingToFill) {
                            Text("Vượt quá giới hạn (tối đa ${formatCurrency(remainingToFill)})", color = Error)
                        }
                    }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { amount = remainingToFill.toLong().toString() },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Text("Đủ", style = MaterialTheme.typography.labelSmall)
                    }
                    OutlinedButton(
                        onClick = { amount = (remainingToFill / 2).toLong().toString() },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Text("50%", style = MaterialTheme.typography.labelSmall)
                    }
                    OutlinedButton(
                        onClick = { amount = maxCanContribute.toLong().toString() },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Text("Max", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amountValue = amount.toDoubleOrNull() ?: 0.0
                    when {
                        amountValue <= 0 -> showError = true
                        amountValue > remainingToFill -> showOverflowAlert = true
                        else -> {
                            onConfirm(amountValue)
                            onDismiss()
                        }
                    }
                },
                enabled = (amount.toDoubleOrNull() ?: 0.0) > 0
            ) {
                Text("Nạp tiền")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}

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
        .format(amount.toLong().coerceAtLeast(0))
}
