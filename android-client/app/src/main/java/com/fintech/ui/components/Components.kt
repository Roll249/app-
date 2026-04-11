package com.fintech.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.fintech.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

// =============================================================================
// EDITORIAL FINANCE DESIGN SYSTEM - Common Components
// =============================================================================

/**
 * EditorialCard - Main card component following Editorial Finance design
 * No borders, use tonal shifts for hierarchy
 */
@Composable
fun EditorialCard(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerLowest,
    shape: RoundedCornerShape = RoundedCornerShape(16.dp),
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        onClick = onClick ?: {}
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            content = content
        )
    }
}

/**
 * EditorialHeroCard - For hero sections (Total Net Worth, etc.)
 * Uses primary container for high-impact sections
 */
@Composable
fun EditorialHeroCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    change: String? = null,
    changePositive: Boolean = true,
    content: @Composable ColumnScope.() -> Unit = {}
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            if (change != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.secondary
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (changePositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSecondary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = change,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondary
                        )
                    }
                }
            }
            content()
        }
    }
}

/**
 * EditorialAllocationCard - For allocation sections
 */
@Composable
fun EditorialAllocationCard(
    modifier: Modifier = Modifier,
    title: String,
    allocations: List<AllocationItem>,
    onViewDetails: (() -> Unit)? = null
) {
    EditorialCard(modifier = modifier) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(24.dp))
            
            allocations.forEach { item ->
                AllocationRow(item = item)
                if (item != allocations.last()) {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
            
            if (onViewDetails != null) {
                Spacer(modifier = Modifier.height(24.dp))
                OutlinedButton(
                    onClick = onViewDetails,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Xem chi tiết")
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

data class AllocationItem(
    val name: String,
    val percentage: Int,
    val color: Color
)

@Composable
fun AllocationRow(item: AllocationItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(item.color)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Text(
            text = "${item.percentage}%",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * EditorialIndexCard - For stock/crypto index cards
 */
@Composable
fun EditorialIndexCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    name: String,
    subtitle: String,
    value: String,
    change: String,
    isPositive: Boolean,
    onClick: () -> Unit = {}
) {
    EditorialCard(
        modifier = modifier.clickable(onClick = onClick),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.padding(12.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = change,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isPositive) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

/**
 * EditorialServiceCard - For service health cards
 */
@Composable
fun EditorialServiceCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconBackgroundColor: Color,
    iconTextColor: Color = OnSecondaryFixedVariant,
    name: String,
    description: String,
    status: ServiceStatus,
    latency: String,
    lastSync: String,
    healthPercentage: Float = 1f,
    onClick: () -> Unit = {}
) {
    EditorialCard(
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(iconBackgroundColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = iconTextColor
                    )
                }
                ServiceStatusChip(status = status)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Stats section
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Latency",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = latency,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Last Sync",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = lastSync,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    // Health bar
                    val animatedProgress by animateFloatAsState(
                        targetValue = healthPercentage,
                        label = "health"
                    )
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        shape = RoundedCornerShape(3.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(animatedProgress)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    when (status) {
                                        ServiceStatus.HEALTHY -> MaterialTheme.colorScheme.secondary
                                        ServiceStatus.DEGRADED -> TertiaryFixedDim
                                        ServiceStatus.OFFLINE -> MaterialTheme.colorScheme.error
                                        ServiceStatus.INITIALIZING -> MaterialTheme.colorScheme.primary
                                    }
                                )
                        )
                    }
                }
            }
        }
    }
}

enum class ServiceStatus {
    HEALTHY,
    DEGRADED,
    OFFLINE,
    INITIALIZING
}

@Composable
fun ServiceStatusChip(status: ServiceStatus) {
    val (color, text) = when (status) {
        ServiceStatus.HEALTHY -> Pair(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer
        )
        ServiceStatus.DEGRADED -> Pair(
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer
        )
        ServiceStatus.OFFLINE -> Pair(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer
        )
        ServiceStatus.INITIALIZING -> Pair(
            PrimaryFixed,
            OnPrimaryFixed
        )
    }
    
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = color
    ) {
        Text(
            text = when (status) {
                ServiceStatus.HEALTHY -> "Healthy"
                ServiceStatus.DEGRADED -> "Degraded"
                ServiceStatus.OFFLINE -> "Offline"
                ServiceStatus.INITIALIZING -> "Initializing"
            },
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = text
        )
    }
}

/**
 * EditorialProgressCard - For budget/fund progress cards
 */
@Composable
fun EditorialProgressCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconBackgroundColor: Color,
    title: String,
    subtitle: String,
    currentAmount: String,
    totalAmount: String,
    progress: Float,
    isOverBudget: Boolean = false,
    onClick: () -> Unit = {}
) {
    val progressColor = if (isOverBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "progress")
    
    EditorialCard(
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = iconBackgroundColor
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.padding(14.dp),
                            tint = MaterialTheme.colorScheme.onSecondary
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = currentAmount,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isOverBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "of $totalAmount",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Progress bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animatedProgress.coerceIn(0f, 1f))
                        .clip(RoundedCornerShape(8.dp))
                        .background(progressColor)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${(progress * 100).toInt()}% utilized",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = progressColor
                )
                Text(
                    text = if (isOverBudget) "Over budget" else "On track",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isOverBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

/**
 * EditorialGoldCard - For gold price cards with border accent
 */
@Composable
fun EditorialGoldCard(
    modifier: Modifier = Modifier,
    type: String,
    badge: String?,
    company: String,
    price: String,
    isPremium: Boolean = false
) {
    EditorialCard(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = type,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (badge != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.surfaceContainer
                        ) {
                            Text(
                                text = badge,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Sell Price",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = price,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = company,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * EditorialSectionHeader - Section header with optional action
 */
@Composable
fun EditorialSectionHeader(
    title: String,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        if (actionText != null && onAction != null) {
            TextButton(onClick = onAction) {
                Text(
                    text = actionText,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Icon(
                    imageVector = Icons.Default.OpenInNew,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

/**
 * EditorialEmptyState - Empty state with editorial styling
 */
@Composable
fun EditorialEmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (subtitle != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

// =============================================================================
// LEGACY COMPATIBILITY
// =============================================================================

@Composable
fun BalanceCard(
    balance: Double,
    currency: String = "VND",
    modifier: Modifier = Modifier,
    title: String = "Tổng số dư"
) {
    EditorialHeroCard(
        modifier = modifier,
        label = title,
        value = formatCurrency(balance, currency)
    )
}

@Composable
fun TransactionItem(
    icon: String?,
    iconColor: String?,
    categoryName: String,
    description: String?,
    amount: Double,
    type: String,
    date: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(parseColor(iconColor) ?: SecondaryFixed),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = getIconFromName(icon) ?: Icons.Default.Receipt,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondary,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = categoryName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!description.isNullOrEmpty()) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = if (type == "INCOME") "+${formatCurrency(amount)}"
                else "-${formatCurrency(amount)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (type == "INCOME") MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
            )
            Text(
                text = date,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun LoadingIndicator(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun ErrorView(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRetry) {
            Text("Thử lại")
        }
    }
}

@Composable
fun EmptyStateView(
    icon: ImageVector = Icons.Default.Inbox,
    title: String,
    subtitle: String? = null,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (subtitle != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
        if (actionText != null && onAction != null) {
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onAction) {
                Text(actionText)
            }
        }
    }
}

// Helper functions
fun formatCurrency(amount: Double, currency: String = "VND"): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    formatter.maximumFractionDigits = 0
    return formatter.format(amount)
}

fun parseColor(colorString: String?): Color? {
    if (colorString == null) return null
    return try {
        Color(android.graphics.Color.parseColor(colorString))
    } catch (e: Exception) {
        null
    }
}

fun getIconFromName(iconName: String?): ImageVector? {
    return when (iconName) {
        "cash" -> Icons.Default.AttachMoney
        "account_balance_wallet" -> Icons.Default.AccountBalanceWallet
        "account_balance" -> Icons.Default.AccountBalance
        "restaurant" -> Icons.Default.Restaurant
        "directions_car" -> Icons.Default.DirectionsCar
        "shopping_cart" -> Icons.Default.ShoppingCart
        "movie" -> Icons.Default.Movie
        "receipt" -> Icons.Default.Receipt
        "local_hospital" -> Icons.Default.LocalHospital
        "school" -> Icons.Default.School
        "home" -> Icons.Default.Home
        "card_giftcard" -> Icons.Default.CardGiftcard
        "trending_up" -> Icons.Default.TrendingUp
        "call_received" -> Icons.Default.CallReceived
        "call_made" -> Icons.Default.CallMade
        "flight" -> Icons.Default.Flight
        "warning" -> Icons.Default.Warning
        else -> Icons.Default.Receipt
    }
}
