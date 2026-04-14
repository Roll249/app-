package com.fintech.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.fintech.ui.auth.LoginScreen
import com.fintech.ui.auth.RegisterScreen
import com.fintech.ui.home.HomeScreen
import com.fintech.ui.account.AccountListScreen
import com.fintech.ui.account.AccountDetailScreen
import com.fintech.ui.account.AddAccountScreen
import com.fintech.ui.transaction.TransactionListScreen
import com.fintech.ui.transaction.AddTransactionScreen
import com.fintech.ui.fund.FundListScreen
import com.fintech.ui.fund.AddFundScreen
import com.fintech.ui.fund.FundConfigurationScreen
import com.fintech.ui.fund.AddIncomeScreen
import com.fintech.ui.bank.BankListScreen
import com.fintech.ui.bank.BankDetailScreen
import com.fintech.ui.bank.LinkBankScreen
import com.fintech.ui.bank.TransferScreen
import com.fintech.ui.budget.BudgetListScreen
import com.fintech.ui.report.ReportScreen
import com.fintech.ui.qr.QRGenerateScreen
import com.fintech.ui.qr.QRScanScreen
import com.fintech.ui.ocr.OcrScanScreen
import com.fintech.ui.profile.ProfileScreen
import com.fintech.ui.profile.SettingsScreen
import com.fintech.ui.profile.EditProfileScreen
import com.fintech.ui.profile.NotificationSettingsScreen
import com.fintech.ui.profile.SecuritySettingsScreen
import com.fintech.ui.profile.AboutScreen
import com.fintech.ui.ai.AIChatScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    isLoggedIn: Boolean,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Check if current route is in bottom nav items
    val showBottomBar = bottomNavItems.any { it.route == currentDestination?.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar && isLoggedIn) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true

                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.title
                                )
                            },
                            label = { Text(item.title) },
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    // Pop up to the start destination of the current graph
                                    popUpTo(Screen.Home.route) {
                                        saveState = true
                                        inclusive = false
                                    }
                                    // Avoid multiple copies of the same destination
                                    launchSingleTop = true
                                    // Restore state when reselecting a previously selected item
                                    restoreState = false
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = if (isLoggedIn) Screen.Home.route else Screen.Login.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            // Auth
            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(Screen.Register.route)
                    }
                )
            }

            composable(Screen.Register.route) {
                RegisterScreen(
                    onRegisterSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        navController.popBackStack()
                    }
                )
            }

            // Main screens
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToAddTransaction = {
                        navController.navigate(Screen.AddTransaction.route)
                    },
                    onNavigateToTransaction = {
                        navController.navigate(Screen.TransactionList.route)
                    },
                    onNavigateToAccounts = {
                        navController.navigate(Screen.AccountList.route)
                    },
                    onNavigateToQRGenerate = {
                        navController.navigate(Screen.QRGenerate.route)
                    },
                    onNavigateToQRScan = {
                        navController.navigate(Screen.QRScan.route)
                    },
                    onNavigateToTransfer = {
                        navController.navigate(Screen.Transfer.route)
                    },
                    onNavigateToAI = {
                        navController.navigate(Screen.AIChat.route)
                    },
                    onNavigateToOptimize = {
                        navController.navigate(Screen.AIChat.createRoute(autoOptimize = true))
                    },
                    onNavigateToFundConfig = {
                        navController.navigate(Screen.FundConfiguration.route)
                    }
                )
            }

            composable(Screen.AccountList.route) {
                AccountListScreen(
                    onNavigateToDetail = { accountId ->
                        navController.navigate(Screen.AccountDetail.createRoute(accountId))
                    },
                    onNavigateToAddAccount = {
                        navController.navigate(Screen.AddAccount.route)
                    }
                )
            }

            composable(Screen.AddAccount.route) {
                AddAccountScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.AccountDetail.route) { backStackEntry ->
                val accountId = backStackEntry.arguments?.getString("accountId") ?: return@composable
                AccountDetailScreen(
                    accountId = accountId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.TransactionList.route) {
                TransactionListScreen(
                    onNavigateToAdd = {
                        navController.navigate(Screen.AddTransaction.route)
                    },
                    onNavigateToOcrScan = {
                        navController.navigate(Screen.OcrScan.route)
                    }
                )
            }

            composable(Screen.AddTransaction.route) {
                AddTransactionScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onTransactionCreated = { navController.popBackStack() }
                )
            }

            composable(Screen.FundList.route) {
                FundListScreen(
                    onNavigateToAdd = {
                        navController.navigate(Screen.AddFund.route)
                    },
                    onNavigateToFundDetail = { fundId ->
                        navController.navigate(Screen.FundDetail.createRoute(fundId))
                    }
                )
            }

            composable(Screen.FundDetail.route) { backStackEntry ->
                val fundId = backStackEntry.arguments?.getString("fundId") ?: return@composable
                com.fintech.ui.fund.FundDetailScreen(
                    fundId = fundId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.AddFund.route) {
                AddFundScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onFundCreated = { navController.popBackStack() }
                )
            }

            composable(Screen.FundConfiguration.route) {
                FundConfigurationScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToLinkedAccounts = {
                        navController.navigate(Screen.BankList.route)
                    }
                )
            }

            composable(Screen.BankList.route) {
                BankListScreen(
                    onNavigateToTransfer = {
                        navController.navigate(Screen.Transfer.route)
                    },
                    onNavigateToBankDetail = { bankCode ->
                        navController.navigate(Screen.BankDetail.createRoute(bankCode))
                    },
                    onNavigateToLinkBank = { bankCode ->
                        navController.navigate(Screen.LinkBank.createRoute(bankCode))
                    }
                )
            }

            composable(Screen.BankDetail.route) { backStackEntry ->
                val bankCode = backStackEntry.arguments?.getString("bankCode") ?: return@composable
                BankDetailScreen(
                    bankCode = bankCode,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToTransfer = {
                        navController.navigate(Screen.Transfer.route)
                    }
                )
            }

            composable(Screen.LinkBank.route) { backStackEntry ->
                val bankCode = backStackEntry.arguments?.getString("bankCode") ?: return@composable
                LinkBankScreen(
                    bankCode = bankCode,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Transfer.route) {
                TransferScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.BudgetList.route) {
                BudgetListScreen()
            }

            composable(Screen.Report.route) {
                ReportScreen()
            }

            composable(Screen.QRGenerate.route) {
                QRGenerateScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.QRScan.route) {
                QRScanScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.OcrScan.route) {
                OcrScanScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onTransactionCreated = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    onNavigateToSettings = {
                        navController.navigate(Screen.Settings.route)
                    },
                    onNavigateToEditProfile = {
                        navController.navigate(Screen.EditProfile.route)
                    },
                    onNavigateToNotificationSettings = {
                        navController.navigate(Screen.NotificationSettings.route)
                    },
                    onNavigateToSecuritySettings = {
                        navController.navigate(Screen.SecuritySettings.route)
                    },
                    onNavigateToAbout = {
                        navController.navigate(Screen.About.route)
                    },
                    onNavigateToServices = {
                        navController.navigate(Screen.Services.route)
                    },
                    onNavigateToMarket = {
                        navController.navigate(Screen.Market.route)
                    },
                    onLogout = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.EditProfile.route) {
                EditProfileScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.NotificationSettings.route) {
                NotificationSettingsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.SecuritySettings.route) {
                SecuritySettingsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.About.route) {
                AboutScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = "ai_chat?autoOptimize={autoOptimize}",
                deepLinks = listOf(navDeepLink { uriPattern = "fintech://ai_chat?autoOptimize={autoOptimize}" })
            ) { backStackEntry ->
                val autoOptimize = backStackEntry.arguments?.getString("autoOptimize")?.toBoolean() ?: false
                if (isLoggedIn) {
                    AIChatScreen(
                        onNavigateBack = { navController.popBackStack() },
                        autoOptimizePortfolio = autoOptimize
                    )
                } else {
                    LaunchedEffect(Unit) {
                        navController.navigate(Screen.Login.route) {
                            popUpTo("ai_chat") { inclusive = true }
                        }
                    }
                }
            }

            composable(Screen.Services.route) {
                if (isLoggedIn) {
                    com.fintech.ui.services.ServicesScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                } else {
                    LaunchedEffect(Unit) {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Services.route) { inclusive = true }
                        }
                    }
                }
            }

            composable(Screen.Market.route) {
                if (isLoggedIn) {
                    com.fintech.ui.market.MarketScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                } else {
                    LaunchedEffect(Unit) {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Market.route) { inclusive = true }
                        }
                    }
                }
            }

            composable(Screen.SavingsGoals.route) {
                com.fintech.ui.savingsgoal.SavingsGoalListScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onAddGoal = { navController.navigate(Screen.AddSavingsGoal.route) }
                )
            }

            composable(Screen.AddSavingsGoal.route) {
                com.fintech.ui.savingsgoal.AddSavingsGoalScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onGoalCreated = { navController.popBackStack() }
                )
            }
        }
    }
}
