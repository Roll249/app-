package com.fintech.data.remote.ocr

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * OCR Service sử dụng ML Kit Text Recognition
 * Fallback: Pattern matching trên text thuần khi ML Kit không nhận diện được
 */
@Singleton
class OCRService @Inject constructor() {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    data class OCRResult(
        val fullText: String,
        val amount: Double?,
        val date: String?,
        val merchantName: String?,
        val invoiceNumber: String?,
        val confidence: Float
    )

    data class InvoiceData(
        val amount: Double,
        val date: String,
        val merchantName: String?,
        val invoiceNumber: String?,
        val category: String,
        val description: String
    )

    suspend fun recognizeText(bitmap: Bitmap): OCRResult {
        val inputImage = InputImage.fromBitmap(bitmap, 0)
        return suspendCancellableCoroutine { continuation ->
            recognizer.process(inputImage)
                .addOnSuccessListener { visionText ->
                    val text = visionText.text
                    val parsed = parseInvoiceText(text)
                    continuation.resume(
                        OCRResult(
                            fullText = text,
                            amount = parsed.amount,
                            date = parsed.date,
                            merchantName = parsed.merchantName,
                            invoiceNumber = parsed.invoiceNumber,
                            confidence = calculateConfidence(text, parsed)
                        )
                    )
                }
                .addOnFailureListener { e ->
                    val fallbackResult = fallbackParse(e.message ?: "")
                    continuation.resume(fallbackResult)
                }
        }
    }

    private fun parseInvoiceText(text: String): ParsedInvoiceData {
        val lines = text.split('\n', '\r').map { it.trim() }.filter { it.isNotEmpty() }
        val amount = extractAmount(lines)
        val date = extractDate(lines)
        val merchantName = extractMerchantName(lines)
        val invoiceNumber = extractInvoiceNumber(lines)

        return ParsedInvoiceData(amount, date, merchantName, invoiceNumber)
    }

    private fun extractAmount(lines: List<String>): Double? {
        val amountPatterns = listOf(
            Regex("""(tổng|cộng|thành tiền|thanh toán|amount|total|grand total)[:\s]*([\d.,]+)\s*(?:đ|VND)?""", RegexOption.IGNORE_CASE),
            Regex("""([\d.,]{5,12})\s*(?:đ|VND)""", RegexOption.IGNORE_CASE),
            Regex("""([\d.,]{5,12})\s*$"""),
        )

        for (pattern in amountPatterns) {
            for (line in lines) {
                val match = pattern.find(line)
                if (match != null) {
                    val amountStr = match.groupValues.lastOrNull()?.replace(".", "")?.replace(",", ".") ?: continue
                    val amount = amountStr.toDoubleOrNull() ?: continue
                    if (amount >= 10000 && amount <= 500000000) {
                        return amount
                    }
                }
            }
        }

        for (line in lines) {
            val amounts = Regex("""([\d]{1,3}(?:[.,][\d]{3})+)""").findAll(line)
            for (match in amounts) {
                val amountStr = match.value.replace(".", "").replace(",", ".")
                val amount = amountStr.toDoubleOrNull() ?: continue
                if (amount >= 10000 && amount <= 500000000) {
                    return amount
                }
            }
        }

        return null
    }

    private fun extractDate(lines: List<String>): String? {
        val datePatterns = listOf(
            Regex("""(\d{1,2})[/\-.](\d{1,2})[/\-.](\d{4})"""),
            Regex("""(\d{1,2})[/\-.](\d{1,2})[/\-.](\d{2})"""),
            Regex("""(\d{4})[/\-.](\d{1,2})[/\-.](\d{1,2})"""),
        )

        val monthNames = mapOf(
            "jan" to 1, "feb" to 2, "mar" to 3, "apr" to 4,
            "may" to 5, "jun" to 6, "jul" to 7, "aug" to 8,
            "sep" to 9, "oct" to 10, "nov" to 11, "dec" to 12,
            "tháng" to -1
        )

        for (line in lines) {
            for (pattern in datePatterns) {
                val match = pattern.find(line)
                if (match != null) {
                    val groups = match.destructured.toList()
                    if (groups.size == 3) {
                        val (a, b, c) = groups
                        return when {
                            a.length == 4 -> "$c-${b.padStart(2, '0')}-${a.padStart(2, '0')}"
                            c.length == 4 -> "${a.padStart(2, '0')}-${b.padStart(2, '0')}-$c"
                            c.length == 2 -> {
                                val year = if (c.toInt() > 50) "19$c" else "20$c"
                                "${a.padStart(2, '0')}-${b.padStart(2, '0')}-$year"
                            }
                            else -> null
                        }
                    }
                }
            }
        }

        return null
    }

    private fun extractMerchantName(lines: List<String>): String? {
        val skipKeywords = listOf(
            "hóa đơn", "invoice", "date", "ngày", "số", "no", "địa chỉ",
            "tel", "phone", "fax", "tax", "mst", "tổng", "cộng", "thành tiền",
            "khách hàng", "customer", "buyer", "seller", "cảm ơn", "thank"
        )

        for (line in lines.take(5)) {
            val clean = line.trim()
            if (clean.length >= 3 &&
                clean.length <= 60 &&
                !clean.all { it.isDigit() || it == '/' || it == '-' || it == '.' } &&
                !skipKeywords.any { clean.lowercase().contains(it) }
            ) {
                val wordCount = clean.split(Regex("""\s+""")).filter { it.length > 1 }.size
                if (wordCount >= 1 && wordCount <= 8) {
                    return clean
                }
            }
        }

        return lines.firstOrNull { it.length > 3 && !it.all { c -> c.isDigit() } }
    }

    private fun extractInvoiceNumber(lines: List<String>): String? {
        val patterns = listOf(
            Regex("""(invoice|no|số|so| bill|mã|#)[:\s\#]*([A-Z0-9\-]{4,20})""", RegexOption.IGNORE_CASE),
            Regex("""\b([A-Z]{2,5}[0-9]{4,15})\b"""),
            Regex("""\b(\d{8,15})\b"""),
        )

        for (line in lines) {
            for (pattern in patterns) {
                val match = pattern.find(line)
                if (match != null) {
                    val value = match.groupValues.lastOrNull() ?: continue
                    if (value.length >= 4 && value.length <= 20) {
                        return value
                    }
                }
            }
        }

        return null
    }

    private fun calculateConfidence(text: String, parsed: ParsedInvoiceData): Float {
        var score = 0f
        if (text.length > 50) score += 0.2f
        if (parsed.amount != null) score += 0.3f
        if (parsed.date != null) score += 0.2f
        if (parsed.merchantName != null) score += 0.15f
        if (parsed.invoiceNumber != null) score += 0.15f
        return score.coerceIn(0f, 1f)
    }

    private fun fallbackParse(errorMessage: String): OCRResult {
        return OCRResult(
            fullText = "",
            amount = null,
            date = null,
            merchantName = null,
            invoiceNumber = null,
            confidence = 0f
        )
    }

    private data class ParsedInvoiceData(
        val amount: Double?,
        val date: String?,
        val merchantName: String?,
        val invoiceNumber: String?
    )

    fun parseToInvoiceCategory(merchantName: String?, invoiceText: String): String {
        val text = "$merchantName $invoiceText".lowercase()
        return when {
            text.contains("grab") || text.contains("gojek") || text.contains("be") ||
            text.contains("vinasun") || text.contains("taxi") -> "Di lại"
            text.contains("coopmart") || text.contains("big c") || text.contains("vinmart") ||
            text.contains("supermarket") || text.contains("tiki") || text.contains("shopee") ||
            text.contains("lazada") || text.contains("bách hóa") -> "Mua sắm"
            text.contains("starbucks") || text.contains("coffee") || text.contains(" Highlands") ||
            text.contains("phở") || text.contains("cơm") || text.contains("nhà hàng") ||
            text.contains("food") || text.contains("ăn") -> "Ăn uống"
            text.contains("electric") || text.contains("điện") || text.contains("nước") ||
            text.contains("internet") || text.contains("fpt") || text.contains("viettel") ||
            text.contains("mobifone") || text.contains("hóa đơn") -> "Hóa đơn"
            text.contains("hospital") || text.contains("phòng khám") || text.contains("nhà thuốc") ||
            text.contains("pharm") || text.contains("bệnh viện") -> "Sức khỏe"
            text.contains("cinema") || text.contains("rạp") || text.contains("game") ||
            text.contains("netflix") || text.contains("spotify") -> "Giải trí"
            text.contains("school") || text.contains("đại học") || text.contains("khóa học") ||
            text.contains("udemy") || text.contains("coursera") -> "Giáo dục"
            text.contains("rent") || text.contains("nhà") || text.contains("apartment") -> "Nhà ở"
            else -> "Chi tiêu khác"
        }
    }
}
