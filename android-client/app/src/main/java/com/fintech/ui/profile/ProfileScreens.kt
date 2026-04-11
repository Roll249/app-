package com.fintech.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fintech.ui.theme.Primary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
    onNavigateToSettings: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToNotificationSettings: () -> Unit,
    onNavigateToSecuritySettings: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToServices: () -> Unit,
    onNavigateToMarket: () -> Unit,
    onLogout: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cá nhân") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Primary,
                    titleContentColor = androidx.compose.ui.graphics.Color.White
                ),
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            Icons.Default.Settings, 
                            contentDescription = "Cài đặt",
                            tint = androidx.compose.ui.graphics.Color.White
                        )
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar với icon
            Icon(
                Icons.Default.AccountCircle,
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                tint = Primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = state.userName ?: "Người dùng",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = state.email ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(32.dp))

            // Menu items
            Card(modifier = Modifier.fillMaxWidth()) {
                Column {
                    ProfileMenuItem(
                        icon = Icons.Default.Person,
                        title = "Hồ sơ",
                        subtitle = "Cập nhật thông tin cá nhân",
                        onClick = onNavigateToEditProfile
                    )
                    HorizontalDivider()
                    ProfileMenuItem(
                        icon = Icons.Default.Notifications,
                        title = "Thông báo",
                        subtitle = "Cài đặt thông báo",
                        onClick = onNavigateToNotificationSettings
                    )
                    HorizontalDivider()
                    ProfileMenuItem(
                        icon = Icons.Default.Security,
                        title = "Bảo mật",
                        subtitle = "Đổi mật khẩu, bảo mật 2 lớp",
                        onClick = onNavigateToSecuritySettings
                    )
                    HorizontalDivider()
                    ProfileMenuItem(
                        icon = Icons.Default.Cloud,
                        title = "Dịch vụ",
                        subtitle = "Quản lý dịch vụ bên ngoài",
                        onClick = onNavigateToServices
                    )
                    HorizontalDivider()
                    ProfileMenuItem(
                        icon = Icons.Default.ShowChart,
                        title = "Thị trường",
                        subtitle = "Tỷ giá, vàng, chứng khoán",
                        onClick = onNavigateToMarket
                    )
                    HorizontalDivider()
                    ProfileMenuItem(
                        icon = Icons.Default.Info,
                        title = "Về ứng dụng",
                        subtitle = "Phiên bản 1.0.0",
                        onClick = onNavigateToAbout
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            OutlinedButton(
                onClick = {
                    viewModel.logout()
                    onLogout()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Default.Logout, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Đăng xuất")
            }
        }
    }
}

@Composable
private fun ProfileMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = Primary)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge)
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onNavigateBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cài đặt") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.ui.graphics.Color.White,
                    titleContentColor = androidx.compose.ui.graphics.Color.Black,
                    navigationIconContentColor = androidx.compose.ui.graphics.Color.Black
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Cài đặt ứng dụng")
        }
    }
}
