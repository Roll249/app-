package com.fintech.ui.services

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.fintech.data.remote.services.*
import com.fintech.ui.components.*
import com.fintech.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServicesScreen(
    onNavigateBack: () -> Unit,
    viewModel: ServicesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadServicesStatus()
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
                                .background(PrimaryFixed),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = OnPrimaryFixed,
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
                    IconButton(onClick = { viewModel.loadServicesStatus() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
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
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Overall Health Banner
            item {
                Spacer(modifier = Modifier.height(8.dp))
                OverallHealthBanner(
                    overall = state.overallStatus,
                    avgLatency = state.services.mapNotNull { it.health?.latencyMs }.average().toLong(),
                    serviceCount = state.services.size
                )
            }

            // Section Header
            item {
                EditorialSectionHeader(
                    title = "Service Health Check",
                    actionText = "Refresh All",
                    onAction = { viewModel.loadServicesStatus() }
                )
            }

            // Services Grid (3 columns on larger screens)
            items(state.services) { service ->
                ServiceCardItem(
                    modifier = Modifier.fillMaxWidth(),
                    service = service
                )
            }

            // Status Logs Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                StatusLogsSection()
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun OverallHealthBanner(
    overall: String,
    avgLatency: Long,
    serviceCount: Int
) {
    val bannerColor = when (overall) {
        "healthy" -> PrimaryContainer
        "degraded" -> TertiaryContainer
        else -> ErrorContainer
    }

    val statusText = when (overall) {
        "healthy" -> "All Systems Operational"
        "degraded" -> "Partial Service Disruption"
        else -> "System Outage"
    }

    val statusDesc = when (overall) {
        "healthy" -> "Global health index at 99.98%. All $serviceCount external service layers are responding within nominal parameters."
        "degraded" -> "Some services are experiencing degraded performance."
        else -> "Multiple services are currently offline."
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bannerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Animated status dot
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(
                                when (overall) {
                                    "healthy" -> Secondary
                                    "degraded" -> TertiaryFixed
                                    else -> Error
                                }
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "System Integrity",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = OnPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = OnPrimaryContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = statusDesc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnPrimaryContainer.copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Latency Card
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = OnPrimaryContainer.copy(alpha = 0.1f)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${avgLatency}ms",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = OnPrimaryContainer
                    )
                    Text(
                        text = "Avg. Latency",
                        style = MaterialTheme.typography.labelSmall,
                        color = OnPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun ServiceCardItem(
    modifier: Modifier = Modifier,
    service: ServiceInfo
) {
    val serviceIcon = when {
        service.name.contains("AI", ignoreCase = true) -> Icons.Default.SmartToy
        service.name.contains("Banking", ignoreCase = true) -> Icons.Default.AccountBalance
        service.name.contains("Notification", ignoreCase = true) -> Icons.Default.Mail
        service.name.contains("Sync", ignoreCase = true) -> Icons.Default.Sync
        service.name.contains("Market", ignoreCase = true) -> Icons.Default.ShowChart
        service.name.contains("Investment", ignoreCase = true) -> Icons.Default.AccountBalanceWallet
        else -> Icons.Default.Cloud
    }

    val iconBgColor = when {
        service.name.contains("AI", ignoreCase = true) -> SecondaryFixed
        service.name.contains("Banking", ignoreCase = true) -> SecondaryFixed
        service.name.contains("Notification", ignoreCase = true) -> PrimaryFixed
        service.name.contains("Market", ignoreCase = true) -> SecondaryFixed
        service.name.contains("Investment", ignoreCase = true) -> ErrorContainer
        else -> SecondaryFixed
    }

    val iconTextColor = when {
        service.name.contains("AI", ignoreCase = true) -> OnSecondaryFixedVariant
        service.name.contains("Banking", ignoreCase = true) -> OnSecondaryFixedVariant
        service.name.contains("Notification", ignoreCase = true) -> OnPrimaryFixedVariant
        service.name.contains("Market", ignoreCase = true) -> OnSecondaryFixedVariant
        service.name.contains("Investment", ignoreCase = true) -> OnErrorContainer
        else -> OnSecondaryFixedVariant
    }

    val status = when (service.health?.status) {
        "healthy" -> ServiceStatus.HEALTHY
        "degraded" -> ServiceStatus.DEGRADED
        "offline" -> ServiceStatus.OFFLINE
        else -> ServiceStatus.INITIALIZING
    }

    val description = when {
        service.name.contains("AI", ignoreCase = true) -> "Predictive analytics & curation engine"
        service.name.contains("Banking", ignoreCase = true) -> "Core transaction & ledger bridge"
        service.name.contains("Notification", ignoreCase = true) -> "Push alerts & webhooks engine"
        service.name.contains("Sync", ignoreCase = true) -> "Cross-device state synchronization"
        service.name.contains("Market", ignoreCase = true) -> "Real-time stock & crypto data feed"
        service.name.contains("Investment", ignoreCase = true) -> "Portfolio management & trading API"
        else -> "Service integration"
    }

    val latency = service.health?.latencyMs?.toString() ?: "--"
    val lastSync = when {
        service.health?.latencyMs != null && service.health.latencyMs < 60000 -> "Just now"
        else -> "2m ago"
    }

    EditorialServiceCard(
        modifier = modifier,
        icon = serviceIcon,
        iconBackgroundColor = iconBgColor,
        iconTextColor = iconTextColor,
        name = service.name,
        description = description,
        status = status,
        latency = "${latency}ms",
        lastSync = lastSync,
        healthPercentage = when (status) {
            ServiceStatus.HEALTHY -> 0.95f
            ServiceStatus.DEGRADED -> 0.5f
            ServiceStatus.INITIALIZING -> 0.25f
            ServiceStatus.OFFLINE -> 0f
        }
    )
}

@Composable
fun StatusLogsSection() {
    EditorialCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "Recent Status Changes",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Primary
            )
            Spacer(modifier = Modifier.height(24.dp))

            StatusLogItem(
                color = Secondary,
                message = "Banking Service recovered from intermittent latency.",
                time = "14:22:01 UTC - System Auto-resolution"
            )
            Spacer(modifier = Modifier.height(16.dp))
            StatusLogItem(
                color = Error,
                message = "Investment Service entered 'Offline' state due to upstream provider outage.",
                time = "14:10:45 UTC - Connectivity Error (503)"
            )
            Spacer(modifier = Modifier.height(16.dp))
            StatusLogItem(
                color = Primary,
                message = "Notification Service deployment initiated.",
                time = "14:05:00 UTC - CI/CD Trigger"
            )
        }
    }
}

@Composable
fun StatusLogItem(
    color: Color,
    message: String,
    time: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(top = 8.dp)
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Column {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = Primary
            )
            Text(
                text = time,
                style = MaterialTheme.typography.labelSmall,
                color = OnSurfaceVariant
            )
        }
    }
}
