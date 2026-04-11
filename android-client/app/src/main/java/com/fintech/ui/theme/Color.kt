package com.fintech.ui.theme

import androidx.compose.ui.graphics.Color

// =============================================================================
// EDITORIAL FINANCE DESIGN SYSTEM
// Based on "The Financial Curator" philosophy
// =============================================================================

// Primary - Deep Navy (Authority & Trust)
val Primary = Color(0xFF001e40)
val PrimaryDark = Color(0xFF001e40)
val PrimaryLight = Color(0xFF799dd6)
val PrimaryContainer = Color(0xFF003366)
val OnPrimary = Color(0xFFFFFFFF)
val OnPrimaryContainer = Color(0xFF799dd6)

// Secondary - Emerald (Growth & Success)
val Secondary = Color(0xFF006c49)
val SecondaryDark = Color(0xFF003b27)
val SecondaryLight = Color(0xFF6cf8bb)
val SecondaryContainer = Color(0xFF6cf8bb)
val OnSecondary = Color(0xFFFFFFFF)
val OnSecondaryContainer = Color(0xFF00714d)

// Tertiary - Deep Green (Alternative positive)
val Tertiary = Color(0xFF002316)
val TertiaryContainer = Color(0xFF003b27)
val TertiaryFixed = Color(0xFF85f8c4)
val TertiaryFixedDim = Color(0xFF68dba9)
val OnTertiary = Color(0xFFFFFFFF)
val OnTertiaryFixed = Color(0xFF002114)
val OnTertiaryFixedVariant = Color(0xFF005236)
val OnTertiaryContainer = Color(0xFF35ae7f)

val TertiaryDark = Color(0xFF68dba9)
val Error = Color(0xFFba1a1a)
val ErrorContainer = Color(0xFFffdad6)
val OnError = Color(0xFFFFFFFF)
val OnErrorContainer = Color(0xFF93000a)

// Surface & Background - Light Theme
val Surface = Color(0xFFf8f9fa)
val SurfaceBright = Color(0xFFf8f9fa)
val SurfaceDim = Color(0xFFd9dadb)
val SurfaceContainer = Color(0xFFedeeef)
val SurfaceContainerLow = Color(0xFFf3f4f5)
val SurfaceContainerHigh = Color(0xFFe7e8e9)
val SurfaceContainerHighest = Color(0xFFe1e3e4)
val SurfaceContainerLowest = Color(0xFFffffff)
val SurfaceTint = Color(0xFF3a5f94)

// On Surface
val OnSurface = Color(0xFF191c1d)
val OnSurfaceVariant = Color(0xFF43474f)
val OnBackground = Color(0xFF191c1d)

// Surface Dark Theme
val SurfaceDark = Color(0xFF001e40)
val SurfaceBrightDark = Color(0xFF1a1c1d)
val SurfaceContainerDark = Color(0xFF1a1c1d)
val SurfaceContainerLowDark = Color(0xFF232627)
val SurfaceContainerHighDark = Color(0xFF2d3032)
val SurfaceContainerHighestDark = Color(0xFF383b3c)
val SurfaceContainerLowestDark = Color(0xFF111315)
val OnSurfaceDark = Color(0xFFe2e3e5)
val OnSurfaceVariantDark = Color(0xFFc3c6d1)
val OnBackgroundDark = Color(0xFFe2e3e5)

// Inverse
val InverseSurface = Color(0xFF2e3132)
val InverseOnSurface = Color(0xFFf0f1f2)
val InversePrimary = Color(0xFFa7c8ff)

// Outline
val Outline = Color(0xFF737780)
val OutlineVariant = Color(0xFFc3c6d1)

// Primary Fixed (for glassmorphism states)
val PrimaryFixed = Color(0xFFd5e3ff)
val PrimaryFixedDim = Color(0xFFa7c8ff)
val OnPrimaryFixed = Color(0xFF001b3c)
val OnPrimaryFixedVariant = Color(0xFF1f477b)

// Secondary Fixed
val SecondaryFixed = Color(0xFF6ffbbe)
val SecondaryFixedDim = Color(0xFF4edea3)
val OnSecondaryFixed = Color(0xFF002113)
val OnSecondaryFixedVariant = Color(0xFF005236)

// Accent (for positive numbers, growth)
val Accent = Color(0xFF006c49)
val AccentLight = Color(0xFF6cf8bb)

// =============================================================================
// LEGACY COMPATIBILITY - Mapped to new Editorial Finance colors
// =============================================================================

// Background colors (legacy)
val Background = Surface
val BackgroundDark = SurfaceDark

// Text colors (legacy)
val TextPrimary = OnSurface
val TextSecondary = OnSurfaceVariant
val TextPrimaryDark = OnSurfaceDark
val TextSecondaryDark = OnSurfaceVariantDark

// Status colors (legacy)
val Success = Secondary
val Warning = Color(0xFFFFC107)
val Info = Primary

// Income/Expense colors (legacy)
val IncomeGreen = Secondary
val ExpenseRed = Error

// =============================================================================
// CATEGORY COLORS - Updated to Editorial Finance palette
// =============================================================================

val CategoryFood = Color(0xFFFF5722)
val CategoryTransport = Color(0xFF795548)
val CategoryShopping = Color(0xFF9C27B0)
val CategoryEntertainment = Color(0xFFE91E63)
val CategoryBills = Color(0xFFF44336)
val CategoryHealth = Color(0xFFE53935)
val CategoryEducation = Color(0xFF3F51B5)
val CategoryHome = Color(0xFF009688)
val CategorySalary = Secondary
val CategoryInvestment = Tertiary

// =============================================================================
// BANK COLORS
// =============================================================================

val BankVCB = Color(0xFF003087)
val BankVTB = Color(0xFFFF6600)
val BankBIDV = Color(0xFFFF8F00)
val BankTPB = Color(0xFF00A651)
val BankACB = Color(0xFFFFD600)
val BankMB = Color(0xFFE31837)
val BankSHB = Color(0xFF003087)
val BankOCB = Color(0xFFFF6D00)
val BankHDB = Color(0xFFFF6D00)
val BankVIB = Color(0xFF0055A5)

// =============================================================================
// CRYPTO COLORS
// =============================================================================

val CryptoBitcoin = Color(0xFFf7931a)
val CryptoEthereum = Color(0xFF627eea)
val CryptoBNB = Color(0xFFF3BA2F)
val CryptoSolana = Color(0xFF9945FF)
val CryptoXRP = Color(0xFF23292F)
