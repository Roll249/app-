package com.fintech.ui.fund

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fintech.ui.components.EmptyStateView
import com.fintech.ui.components.LoadingIndicator
import com.fintech.ui.theme.IncomeGreen
import com.fintech.ui.theme.Primary
import java.text.NumberFormat
import java.util.Locale

// Use Fund from ui.fund instead of domain.model
private typealias FundModel = Fund

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FundListScreen(
    viewModel: FundViewModel = hiltViewModel(),
    onNavigateToAdd: () -> Unit,
    onNavigateToFundDetail: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadFunds()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quỹ tiết kiệm") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAdd,
                containerColor = Primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Thêm quỹ", tint = Color.White)
            }
        }
    ) { padding ->
        if (state.isLoading) {
            LoadingIndicator(modifier = Modifier.padding(padding))
        } else if (state.funds.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.Savings,
                title = "Chưa có quỹ",
                subtitle = "Tạo quỹ để bắt đầu tiết kiệm",
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Tổng quan
                item {
                    TotalFundCard(
                        totalSaved = state.funds.sumOf { it.currentAmount },
                        totalTarget = state.funds.mapNotNull { it.targetAmount }.sum(),
                        fundCount = state.funds.size
                    )
                }

                items(state.funds) { fund ->
                    FundCard(
                        fund = fund,
                        onClick = { onNavigateToFundDetail(fund.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TotalFundCard(
    totalSaved: Double,
    totalTarget: Double,
    fundCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Primary),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Savings,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Tổng tiết kiệm",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = formatCurrency(totalSaved) + " đ",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$fundCount quỹ đang hoạt động",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun FundCard(
    fund: Fund,
    onClick: () -> Unit
) {
    val progress = fund.progress

    val fundColor = try {
        Color(android.graphics.Color.parseColor(fund.color ?: "#4CAF50"))
    } catch (e: Exception) {
        Primary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Icon hũ
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(fundColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getFundIcon(fund.icon ?: "savings"),
                        contentDescription = null,
                        tint = fundColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = fund.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${formatCurrency(fund.currentAmount)} / ${fund.targetAmount?.let { formatCurrency(it) } ?: "∞"} đ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${progress.toInt()}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = fundColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { (progress / 100).toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = fundColor,
                trackColor = fundColor.copy(alpha = 0.2f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFundScreen(
    onNavigateBack: () -> Unit,
    onFundCreated: () -> Unit
) {
    val viewModel: FundViewModel = hiltViewModel()

    var name by remember { mutableStateOf("") }
    var targetAmount by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf("savings") }
    var selectedColor by remember { mutableStateOf("#4CAF50") }
    var showIconPicker by remember { mutableStateOf(false) }

    val colors = listOf("#4CAF50", "#2196F3", "#FF9800", "#E91E63", "#9C27B0", "#00BCD4", "#795548", "#607D8B")
    val icons = listOf("savings", "flight", "directions_car", "home", "laptop_mac", "school", "fitness_center", "shopping_bag")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tạo quỹ mới") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Close, contentDescription = "Hủy")
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tên quỹ
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Tên quỹ") },
                placeholder = { Text("VD: Quỹ du lịch Nhật Bản") },
                leadingIcon = { Icon(Icons.Default.Flag, null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // Số tiền mục tiêu
            OutlinedTextField(
                value = targetAmount,
                onValueChange = { targetAmount = it.filter { c -> c.isDigit() } },
                label = { Text("Số tiền mục tiêu") },
                placeholder = { Text("0") },
                leadingIcon = { Icon(Icons.Default.AttachMoney, null) },
                trailingIcon = { Text("VND") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp)
            )

            // Chọn icon
            Text("Icon", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                icons.take(4).forEach { icon ->
                    IconOption(
                        icon = icon,
                        isSelected = selectedIcon == icon,
                        onClick = { selectedIcon = icon }
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                icons.drop(4).forEach { icon ->
                    IconOption(
                        icon = icon,
                        isSelected = selectedIcon == icon,
                        onClick = { selectedIcon = icon }
                    )
                }
            }

            // Chọn màu
            Text("Màu sắc", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                colors.forEach { color ->
                    ColorOption(
                        color = color,
                        isSelected = selectedColor == color,
                        onClick = { selectedColor = color }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Nút tạo
            Button(
                onClick = {
                    val target = targetAmount.toDoubleOrNull() ?: 0.0
                    viewModel.createFund(name, target, selectedIcon, selectedColor)
                    onFundCreated()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Tạo quỹ", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun IconOption(
    icon: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val fundColor = Primary
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(if (isSelected) fundColor else fundColor.copy(alpha = 0.1f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = getFundIcon(icon),
            contentDescription = icon,
            tint = if (isSelected) Color.White else fundColor,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun ColorOption(
    color: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val parsedColor = try {
        Color(android.graphics.Color.parseColor(color))
    } catch (e: Exception) {
        Primary
    }

    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(parsedColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                Icons.Default.Check,
                contentDescription = "Selected",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FundDetailScreen(
    fundId: String,
    onNavigateBack: () -> Unit
) {
    val viewModel: FundViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()
    val fund = state.funds.find { it.id == fundId }

    var showDepositDialog by remember { mutableStateOf(false) }
    var depositAmount by remember { mutableStateOf("") }

    LaunchedEffect(fundId) {
        viewModel.loadFunds()
    }

    if (fund == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val progress = if (fund.targetAmount != null && fund.targetAmount > 0) {
        (fund.currentAmount / fund.targetAmount * 100).coerceIn(0.0, 100.0)
    } else 0.0

    val fundColor = try {
        Color(android.graphics.Color.parseColor(fund.color ?: "#4CAF50"))
    } catch (e: Exception) {
        Primary
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(fund.name) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = fundColor,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
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
            // Card số dư
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = fundColor),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Đã tiết kiệm",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = formatCurrency(fund.currentAmount) + " đ",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    if (fund.targetAmount != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Mục tiêu: ${formatCurrency(fund.targetAmount)} đ",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    LinearProgressIndicator(
                        progress = { (progress / 100).toFloat() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${progress.toInt()}% hoàn thành",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            // Nút nạp tiền
            Button(
                onClick = { showDepositDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = fundColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Nạp tiền vào quỹ", fontWeight = FontWeight.Bold)
            }

            // Thông tin chi tiết
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Thông tin quỹ",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    InfoRow(label = "Tên quỹ", value = fund.name)
                    if (fund.targetAmount != null) {
                        InfoRow(label = "Số tiền mục tiêu", value = formatCurrency(fund.targetAmount) + " đ")
                    }
                    InfoRow(label = "Đã tiết kiệm", value = formatCurrency(fund.currentAmount) + " đ")
                    if (fund.targetAmount != null) {
                        val remaining = fund.targetAmount - fund.currentAmount
                        InfoRow(
                            label = "Còn thiếu",
                            value = if (remaining > 0) formatCurrency(remaining) + " đ" else "Đã đạt mục tiêu!"
                        )
                    }
                }
            }
        }
    }

    // Dialog nạp tiền
    if (showDepositDialog) {
        AlertDialog(
            onDismissRequest = { showDepositDialog = false },
            title = { Text("Nạp tiền vào quỹ") },
            text = {
                Column {
                    Text("Nhập số tiền bạn muốn nạp vào quỹ này")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = depositAmount,
                        onValueChange = { depositAmount = it.filter { c -> c.isDigit() } },
                        label = { Text("Số tiền nạp") },
                        leadingIcon = { Icon(Icons.Default.AttachMoney, null) },
                        trailingIcon = { Text("VND") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = depositAmount.toLongOrNull() ?: 0L
                        if (amount > 0) {
                            viewModel.contribute(fundId, amount.toDouble())
                            depositAmount = ""
                            showDepositDialog = false
                        }
                    },
                    enabled = (depositAmount.toLongOrNull() ?: 0L) > 0,
                    colors = ButtonDefaults.buttonColors(containerColor = fundColor)
                ) {
                    Text("Nạp tiền")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDepositDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
    HorizontalDivider()
}

private fun getFundIcon(iconName: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (iconName) {
        "flight" -> Icons.Default.Flight
        "directions_car" -> Icons.Default.DirectionsCar
        "home" -> Icons.Default.Home
        "laptop_mac" -> Icons.Default.LaptopMac
        "school" -> Icons.Default.School
        "fitness_center" -> Icons.Default.FitnessCenter
        "shopping_bag" -> Icons.Default.ShoppingBag
        "security" -> Icons.Default.Security
        else -> Icons.Default.Savings
    }
}

private fun formatCurrency(amount: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
    return formatter.format(amount.toLong())
}
