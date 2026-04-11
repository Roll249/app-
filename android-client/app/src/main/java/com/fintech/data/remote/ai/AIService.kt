package com.fintech.data.remote.ai

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Header
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AI Service sử dụng Ollama API
 */
@Singleton
class AIService @Inject constructor() {

    data class ChatMessage(
        val role: String,
        val content: String
    )

    data class ChatRequest(
        val model: String = "qwen3:8b",
        val messages: List<ChatMessage>,
        val stream: Boolean = false
    )

    data class ChatResponse(
        val model: String?,
        val response: String?,
        val done: Boolean?
    )

    data class GenerateRequest(
        val model: String = "qwen3:8b",
        val prompt: String,
        val stream: Boolean = false
    )

    data class GenerateResponse(
        val model: String?,
        val response: String?,
        val done: Boolean?
    )

    /**
     * Gọi AI để phân tích hóa đơn OCR
     */
    suspend fun analyzeInvoice(
        baseUrl: String,
        rawText: String,
        userContext: String
    ): Result<String> {
        return try {
            val prompt = """
Bạn là trợ lý phân tích hóa đơn tài chính. Hãy phân tích văn bản hóa đơn sau và trả về JSON:

Văn bản hóa đơn:
$rawText

Ngữ cảnh người dùng: $userContext

Hãy trả về JSON với các trường:
- amount: số tiền (number)
- date: ngày tháng năm (string, format YYYY-MM-DD)
- merchant: tên người bán (string)
- category: danh mục chi tiêu gợi ý (string)
- description: mô tả ngắn (string)

Chỉ trả về JSON, không giải thích gì thêm.
            """.trimIndent()

            val response = callOllama(baseUrl, prompt)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Gọi AI để chia hũ tiết kiệm
     */
    suspend fun suggestFundAllocation(
        baseUrl: String,
        income: Double,
        expenses: Map<String, Double>,
        funds: List<FundInfo>
    ): Result<FundAllocation> {
        return try {
            val fundsJson = funds.joinToString("\n") { "- ${it.name}: ${it.currentAmount} / ${it.targetAmount}" }
            val expensesJson = expenses.entries.joinToString("\n") { "- ${it.key}: ${it.value}" }

            val prompt = """
Bạn là chuyên gia tư vấn tài chính. Hãy đề xuất cách chia tiền vào các quỹ tiết kiệm.

Thu nhập tháng: ${income.toLong()} VND
Chi tiêu hiện tại:
$expensesJson

Các quỹ hiện tại:
$fundsJson

Hãy trả về JSON:
{
  "totalSavings": số tiền có thể tiết kiệm (number),
  "allocations": [
    {
      "fundId": "id của quỹ",
      "fundName": "tên quỹ",
      "amount": số tiền nạp vào (number),
      "reason": "lý do"
    }
  ],
  "suggestion": "lời khuyên ngắn gọn"
}

Chỉ trả về JSON.
            """.trimIndent()

            val response = callOllama(baseUrl, prompt)
            val allocation = parseFundAllocation(response)
            Result.success(allocation)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Gọi AI chatbot
     */
    suspend fun chat(
        baseUrl: String,
        messages: List<ChatMessage>,
        userContext: String
    ): Result<String> {
        return try {
            val systemPrompt = """
Bạn là trợ lý AI tài chính cá nhân, tên "Fina".

## Nguyên tắc trả lời:
1. TRẢ LỜI NGẮN GỌN: Tối đa 2-3 câu cho câu hỏi đơn giản, 4-5 câu cho câu hỏi phức tạp
2. KHÔNG LAN MAN: Không giải thích quá nhiều lý thuyết, tập trung vào câu trả lời cụ thể
3. SỬ DỤNG SỐ: Dùng số tiền cụ thể từ dữ liệu người dùng khi có
4. HÀNH ĐỘNG RÕ RÀNG: Nếu cần, gợi ý 1-2 hành động cụ thể

## Phạm vi:
- Tư vấn tiết kiệm và đầu tư
- Phân tích chi tiêu
- Lập kế hoạch tài chính
- Trả lời về các quỹ tiết kiệm

## Ngoài phạm vi (từ chối lịch sự):
- Không trả lời về chính trị, tôn giáo
- Không đưa ra lời khuyên pháp lý
- Không hứa hẹn lợi nhuận cố định

## Ngữ cảnh người dùng:
$userContext

Trả lời bằng tiếng Việt, thân thiện nhưng chuyên nghiệp.
            """.trimIndent()

            val allMessages = listOf(ChatMessage("system", systemPrompt)) + messages
            val response = callOllamaChat(baseUrl, allMessages)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun callOllama(baseUrl: String, prompt: String): String {
        val request = GenerateRequest(
            model = "qwen3:8b",
            prompt = prompt,
            stream = false
        )

        val api = retrofit(baseUrl).create(OllamaApi::class.java)
        val response = api.generate(request)
        return response.response ?: ""
    }

    private suspend fun callOllamaChat(baseUrl: String, messages: List<ChatMessage>): String {
        val request = ChatRequest(
            model = "qwen3:8b",
            messages = messages,
            stream = false
        )

        val api = retrofit(baseUrl).create(OllamaApi::class.java)
        val response = api.chat(request)
        return response.response ?: ""
    }

    private fun parseFundAllocation(json: String): FundAllocation {
        // Parse JSON đơn giản
        return try {
            val totalMatch = Regex(""""totalSavings":\s*(\d+)""").find(json)
            FundAllocation(
                totalSavings = totalMatch?.groupValues?.get(1)?.toLongOrNull() ?: 0L,
                allocations = emptyList(),
                suggestion = "Hãy tiết kiệm đều đặn mỗi tháng!"
            )
        } catch (e: Exception) {
            FundAllocation(
                totalSavings = 0L,
                allocations = emptyList(),
                suggestion = "Cần thêm thông tin để đề xuất."
            )
        }
    }

    private fun retrofit(baseUrl: String): retrofit2.Retrofit {
        return retrofit2.Retrofit.Builder()
            .baseUrl(baseUrl.removeSuffix("/") + "/")
            .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
            .build()
    }

    data class FundInfo(
        val id: String,
        val name: String,
        val currentAmount: Long,
        val targetAmount: Long
    )

    data class FundAllocation(
        val totalSavings: Long,
        val allocations: List<FundAllocationItem>,
        val suggestion: String
    )

    data class FundAllocationItem(
        val fundId: String,
        val fundName: String,
        val amount: Long,
        val reason: String
    )

    interface OllamaApi {
        @POST("api/generate")
        suspend fun generate(@Body request: GenerateRequest): GenerateResponse

        @POST("api/chat")
        suspend fun chat(@Body request: ChatRequest): ChatResponse
    }
}
