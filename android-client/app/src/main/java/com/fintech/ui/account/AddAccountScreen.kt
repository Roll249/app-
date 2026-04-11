package com.fintech.ui.account

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fintech.domain.model.AccountType
import com.fintech.ui.components.LoadingIndicator

/**
 * Màn hình thêm tài khoản mới
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAccountScreen(
    viewModel: AccountViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    // Form state
    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(AccountType.CASH) }
    var selectedIcon by remember { mutableStateOf("account_balance_wallet") }
    var selectedColor by remember { mutableStateOf("#2196F3") }
    var initialBalance by remember { mutableStateOf("0") }
    var currency by remember { mutableStateOf("VND") }
    var includeInTotal by remember { mutableStateOf(true) }

    // UI state
    var showTypeDropdown by remember { mutableStateOf(false) }
    var showIconDropdown by remember { mutableStateOf(false) }
    var showColorDropdown by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Icons available
    val availableIcons = listOf(
        "account_balance_wallet" to Icons.Default.AccountBalanceWallet,
        "account_balance" to Icons.Default.AccountBalance,
        "savings" to Icons.Default.Savings,
        "credit_card" to Icons.Default.CreditCard,
        "payments" to Icons.Default.Payments,
        "wallet" to Icons.Default.Wallet
    )

    // Colors available
    val availableColors = listOf(
        "#2196F3" to "Xanh dương",
        "#4CAF50" to "Xanh lá",
        "#FF9800" to "Cam",
        "#F44336" to "Đỏ",
        "#9C27B0" to "Tím",
        "#00BCD4" to "Cyan"
    )

    // Handle navigation after success
    LaunchedEffect(state.createSuccess) {
        if (state.createSuccess) {
            onNavigateBack()
        }
    }

    // Show error if any
    LaunchedEffect(state.error) {
        if (state.error != null) {
            errorMessage = state.error!!
            showError = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thêm tài khoản") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Tên tài khoản
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Tên tài khoản") },
                placeholder = { Text("VD: Tiền mặt, Tài khoản ngân hàng...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.Label, contentDescription = null)
                }
            )

            // Loại tài khoản
            ExposedDropdownMenuBox(
                expanded = showTypeDropdown,
                onExpandedChange = { showTypeDropdown = it }
            ) {
                OutlinedTextField(
                    value = when (selectedType) {
                        AccountType.CASH -> "Tiền mặt"
                        AccountType.BANK -> "Ngân hàng"
                        AccountType.WALLET -> "Ví điện tử"
                        AccountType.SAVINGS -> "Tiết kiệm"
                    },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Loại tài khoản") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showTypeDropdown) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = showTypeDropdown,
                    onDismissRequest = { showTypeDropdown = false }
                ) {
                    AccountType.entries.forEach { type ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    when (type) {
                                        AccountType.CASH -> "Tiền mặt"
                                        AccountType.BANK -> "Ngân hàng"
                                        AccountType.WALLET -> "Ví điện tử"
                                        AccountType.SAVINGS -> "Tiết kiệm"
                                    }
                                )
                            },
                            onClick = {
                                selectedType = type
                                showTypeDropdown = false
                            }
                        )
                    }
                }
            }

            // Icon picker
            ExposedDropdownMenuBox(
                expanded = showIconDropdown,
                onExpandedChange = { showIconDropdown = it }
            ) {
                OutlinedTextField(
                    value = availableIcons.find { it.first == selectedIcon }
                        ?.first
                        ?.replace("_", " ")
                        ?.replaceFirstChar { it.uppercase() }
                        ?: "Chọn icon",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Biểu tượng") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showIconDropdown) },
                    leadingIcon = {
                        Icon(
                            imageVector = availableIcons.find { it.first == selectedIcon }?.second
                                ?: Icons.Default.AccountBalanceWallet,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = showIconDropdown,
                    onDismissRequest = { showIconDropdown = false }
                ) {
                    availableIcons.forEach { (iconName, icon) ->
                        DropdownMenuItem(
                            text = { Text(iconName.replace("_", " ").replaceFirstChar { it.uppercase() }) },
                            leadingIcon = {
                                Icon(imageVector = icon, contentDescription = null)
                            },
                            onClick = {
                                selectedIcon = iconName
                                showIconDropdown = false
                            }
                        )
                    }
                }
            }

            // Color picker
            ExposedDropdownMenuBox(
                expanded = showColorDropdown,
                onExpandedChange = { showColorDropdown = it }
            ) {
                OutlinedTextField(
                    value = availableColors.find { it.first == selectedColor }?.second ?: "Chọn màu",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Màu sắc") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showColorDropdown) },
                    leadingIcon = {
                        Surface(
                            modifier = Modifier.size(24.dp),
                            shape = MaterialTheme.shapes.small,
                            color = androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(selectedColor))
                        ) {}
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = showColorDropdown,
                    onDismissRequest = { showColorDropdown = false }
                ) {
                    availableColors.forEach { (colorHex, colorName) ->
                        DropdownMenuItem(
                            text = { Text(colorName) },
                            leadingIcon = {
                                Surface(
                                    modifier = Modifier.size(24.dp),
                                    shape = MaterialTheme.shapes.small,
                                    color = androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(colorHex))
                                ) {}
                            },
                            onClick = {
                                selectedColor = colorHex
                                showColorDropdown = false
                            }
                        )
                    }
                }
            }

            // Số dư ban đầu
            OutlinedTextField(
                value = initialBalance,
                onValueChange = { initialBalance = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Số dư ban đầu") },
                placeholder = { Text("0") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                leadingIcon = {
                    Icon(Icons.Default.AttachMoney, contentDescription = null)
                }
            )

            // Tiền tệ
            OutlinedTextField(
                value = currency,
                onValueChange = { currency = it.uppercase().take(3) },
                label = { Text("Đơn vị tiền tệ") },
                placeholder = { Text("VND") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.CurrencyExchange, contentDescription = null)
                }
            )

            // Bao gồm trong tổng
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = includeInTotal,
                    onCheckedChange = { includeInTotal = it }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Bao gồm trong tổng số dư",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Nút tạo tài khoản
            Button(
                onClick = {
                    if (name.isBlank()) {
                        errorMessage = "Vui lòng nhập tên tài khoản"
                        showError = true
                        return@Button
                    }
                    viewModel.createAccount(
                        name = name.trim(),
                        type = selectedType.name,
                        icon = selectedIcon,
                        color = selectedColor,
                        initialBalance = initialBalance.toDoubleOrNull() ?: 0.0,
                        currency = currency
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading && name.isNotBlank()
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tạo tài khoản")
                }
            }
        }

        // Snackbar for errors
        if (showError) {
            Snackbar(
                modifier = Modifier.padding(16.dp),
                action = {
                    TextButton(onClick = { showError = false }) {
                        Text("Đóng")
                    }
                }
            ) {
                Text(errorMessage)
            }
        }
    }
}
