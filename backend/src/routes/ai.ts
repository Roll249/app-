import { query } from '../utils/db.js';
import { v4 as uuidv4 } from 'uuid';

// Types
interface ChatMessage {
  role: 'user' | 'assistant' | 'system';
  content: string;
}

// System prompt cho AI tài chính
const SYSTEM_PROMPT = `Bạn là trợ lý AI tài chính cá nhân, tên "Fina".

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

Trả lời bằng tiếng Việt, thân thiện nhưng chuyên nghiệp.`;

export async function aiRoutes(fastify: any) {
  // ========================================
  // Lưu cuộc trò chuyện (public - không cần auth)
  // ========================================
  fastify.post('/chat/log', async (request: any, reply: any) => {
    try {
      const { sessionId, messages } = request.body;
      const userId = request.body.userId || 'demo';

      await query(
        'DELETE FROM ai_chat_logs WHERE user_id = $1 AND session_id = $2',
        [userId, sessionId || 'default']
      );

      await query(
        `INSERT INTO ai_chat_logs (user_id, session_id, messages) VALUES ($1, $2, $3)`,
        [userId, sessionId || 'default', JSON.stringify(messages)]
      );

      return reply.send({ success: true, message: 'Chat log saved' });
    } catch (error: any) {
      fastify.log.error('ERROR saving chat log:', error.message, error.stack);
      return reply.status(500).send({ success: false, message: 'Failed to save chat log: ' + error.message });
    }
  });

  // ========================================
  // Lấy lịch sử chat (public)
  // ========================================
  fastify.get('/chat/history', async (request: any, reply: any) => {
    try {
      const userId = request.query.userId || 'demo';
      const sessionId = request.query.sessionId || 'default';

      const result = await query(
        'SELECT * FROM ai_chat_logs WHERE user_id = $1 AND session_id = $2',
        [userId, sessionId]
      );

      if (result.length === 0) {
        return reply.send({ success: true, data: { messages: [] } });
      }

      return reply.send({
        success: true,
        data: {
          messages: result[0].messages || [],
          createdAt: result[0].created_at
        }
      });
    } catch (error: any) {
      fastify.log.error(error);
      return reply.status(500).send({ success: false, message: 'Failed to get chat history' });
    }
  });

  // ========================================
  // Gửi chat đến Ollama (public)
  // ========================================
  fastify.post('/chat', async (request: any, reply: any) => {
    try {
      const { messages, userContext, model, userId } = request.body;
      const userIdFinal = userId || 'demo';
      const ollamaUrl = process.env.OLLAMA_URL || 'https://research.neu.edu.vn/ollama';
      const modelName = model || process.env.OLLAMA_MODEL || 'qwen3:8b';

      fastify.log.info(`[AI] Chat request from user ${userIdFinal}, model: ${modelName}, messages: ${messages?.length || 0}`);

      // Xây dựng system prompt với user context
      let systemMessage = SYSTEM_PROMPT;
      if (userContext) {
        systemMessage += `\n\n## Ngữ cảnh người dùng:\n${userContext}`;
      }

      // Ghép lịch sử chat thành prompt cho /api/generate
      const conversationHistory = messages
        ?.map((m: ChatMessage) => `${m.role === 'user' ? 'User' : 'Assistant'}: ${m.content}`)
        .join('\n') || '';

      const fullPrompt = `${systemMessage}\n\n${conversationHistory}\n\nAssistant:`;

      fastify.log.info(`[AI] Sending prompt to Ollama (${fullPrompt.length} chars)`);

      // Gọi /api/generate thay vì /api/chat
      const response = await fetch(`${ollamaUrl}/api/generate`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          model: modelName,
          prompt: fullPrompt,
          stream: false,
          think: false
        })
      });

      if (!response.ok) {
        const errorText = await response.text();
        fastify.log.error(`[AI] Ollama error: ${errorText}`);
        return reply.status(502).send({
          success: false,
          message: `AI service error: ${response.status}`
        });
      }

      const data: any = await response.json();

      fastify.log.info(`[AI] Response from model ${modelName}: ${data.response?.substring(0, 100)}...`);

      return reply.send({
        success: true,
        data: {
          response: data.response,
          model: data.model,
          done: data.done
        }
      });
    } catch (error: any) {
      fastify.log.error(`[AI] Chat error: ${error.message}`);
      return reply.status(500).send({
        success: false,
        message: `Chat error: ${error.message}`
      });
    }
  });
}
