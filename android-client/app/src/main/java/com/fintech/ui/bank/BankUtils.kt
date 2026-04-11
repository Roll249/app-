package com.fintech.ui.bank

import java.text.NumberFormat
import java.util.Locale

/**
 * Format số tài khoản: 1234 5678 90xx
 */
fun formatAccountNumber(accountNumber: String): String {
    return accountNumber.chunked(4).joinToString(" ")
}

/**
 * Format số tiền: 123.456.789
 */
fun formatCurrency(amount: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
    return formatter.format(amount.toLong())
}

/**
 * Format số tiền từ Long: 123.456.789
 */
fun formatCurrency(amount: Long): String {
    val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
    return formatter.format(amount)
}

/**
 * Chuyển số thành chữ (đơn giản)
 */
fun convertNumberToText(number: Long): String {
    if (number <= 0) return "không"
    
    val units = arrayOf("", "nghìn", "triệu", "tỷ")
    val result = StringBuilder()
    var n = number
    var unitIndex = 0
    
    while (n > 0) {
        val part = n % 1000
        if (part > 0) {
            result.insert(0, "$part ${units[unitIndex]} ")
        }
        n /= 1000
        unitIndex++
    }
    
    return result.toString().trim().replace("  ", " ")
}
