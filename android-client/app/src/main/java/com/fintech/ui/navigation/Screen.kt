package com.fintech.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Navigation routes
 */
sealed class Screen(val route: String) {
    // Auth
    object Login : Screen("login")
    object Register : Screen("register")

    // Main
    object Home : Screen("home")
    object AccountList : Screen("accounts")
    object AccountDetail : Screen("account/{accountId}") {
        fun createRoute(accountId: String) = "account/$accountId"
    }
    object AddAccount : Screen("add_account")

    object TransactionList : Screen("transactions")
    object AddTransaction : Screen("add_transaction")
    object CategoryList : Screen("categories")
    object FundList : Screen("funds")
    object FundDetail : Screen("fund/{fundId}") {
        fun createRoute(fundId: String) = "fund/$fundId"
    }
    object AddFund : Screen("add_fund")
    object BudgetList : Screen("budgets")
    object BankList : Screen("banks")
    object BankDetail : Screen("bank/{bankCode}") {
        fun createRoute(bankCode: String) = "bank/$bankCode"
    }
    object LinkBank : Screen("link_bank/{bankCode}") {
        fun createRoute(bankCode: String) = "link_bank/$bankCode"
    }
    object Transfer : Screen("transfer")
    object QRGenerate : Screen("qr_generate")
    object QRScan : Screen("qr_scan")
    object QRConfirm : Screen("qr_confirm")
    object OcrScan : Screen("ocr_scan")
    object Report : Screen("reports")
    object Profile : Screen("profile")
    object Settings : Screen("settings")
    object EditProfile : Screen("edit_profile")
    object NotificationSettings : Screen("notification_settings")
    object SecuritySettings : Screen("security_settings")
    object About : Screen("about")
    object AIChat : Screen("ai_chat")
}

/**
 * Bottom navigation items
 */
sealed class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Home : BottomNavItem(
        route = Screen.Home.route,
        title = "Trang chủ",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    )

    object Transactions : BottomNavItem(
        route = Screen.TransactionList.route,
        title = "Giao dịch",
        selectedIcon = Icons.Filled.Receipt,
        unselectedIcon = Icons.Outlined.Receipt
    )

    object Accounts : BottomNavItem(
        route = Screen.AccountList.route,
        title = "Tài khoản",
        selectedIcon = Icons.Filled.AccountBalanceWallet,
        unselectedIcon = Icons.Outlined.AccountBalanceWallet
    )

    object Reports : BottomNavItem(
        route = Screen.Report.route,
        title = "Báo cáo",
        selectedIcon = Icons.Filled.BarChart,
        unselectedIcon = Icons.Outlined.BarChart
    )

    object Profile : BottomNavItem(
        route = Screen.Profile.route,
        title = "Cá nhân",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person
    )
}

val bottomNavItems = listOf(
    BottomNavItem.Home,
    BottomNavItem.Transactions,
    BottomNavItem.Accounts,
    BottomNavItem.Reports,
    BottomNavItem.Profile
)
