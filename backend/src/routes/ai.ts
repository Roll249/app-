/**
 * AI Routes - Updated to use AIService
 * 
 * This module handles all AI-related endpoints using the centralized AIService
 */
import { query, queryOne } from '../utils/db.js';
import { getServiceRegistry } from '../services/index.js';

export interface ChatMessage {
  role: 'user' | 'assistant' | 'system';
  content: string;
}

export async function aiRoutes(fastify: any) {
  const getAIService = () => getServiceRegistry(fastify.log).getAIService();

  // ========================================
  // Lưu cuộc trò chuyện (public - không cần auth)
  // ========================================
  fastify.post('/chat/log', async (request: any, reply: any) => {
    try {
      const { sessionId, messages } = request.body;
      const userId = request.body.userId || 'demo';

      // JSONB expects the value to be a string or proper JSON
      const messagesJson = JSON.stringify(messages || []);

      await query(
        `INSERT INTO ai_chat_logs (user_id, session_id, messages, created_at)
         VALUES ($1, $2, $3, $4)
         ON CONFLICT (user_id) WHERE user_id = $1 DO UPDATE SET
         session_id = EXCLUDED.session_id,
         messages = EXCLUDED.messages,
         created_at = EXCLUDED.created_at`,
        [userId, sessionId || 'default', messagesJson, Math.floor(Date.now() / 1000)]
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

      const result = await queryOne(
        'SELECT * FROM ai_chat_logs WHERE user_id = $1 AND session_id = $2',
        [userId, sessionId]
      );

      if (!result) {
        return reply.send({
          success: true,
          data: {
            messages: [],
            createdAt: null
          }
        });
      }

      // Parse JSONB if it's a string
      let messages = result.messages;
      if (typeof messages === 'string') {
        try {
          messages = JSON.parse(messages);
        } catch (e) {
          messages = [];
        }
      }

      return reply.send({
        success: true,
        data: {
          messages: messages || [],
          createdAt: result.created_at ? new Date(result.created_at * 1000).toISOString() : null
        }
      });
    } catch (error: any) {
      fastify.log.error(error);
      return reply.status(500).send({ success: false, message: 'Failed to get chat history' });
    }
  });

  // ========================================
  // Gửi chat đến AI (sử dụng AIService)
  // ========================================
  fastify.post('/chat', async (request: any, reply: any) => {
    try {
      const { messages, userContext, model, userId } = request.body;
      const userIdFinal = userId || 'demo';

      fastify.log.info(`[AI] Chat request from user ${userIdFinal}, model: ${model || 'default'}, messages: ${messages?.length || 0}`);

      const aiService = getAIService();

      // Check if AI service is available
      const status = await aiService.checkStatus();
      if (status.status !== 'online') {
        return reply.status(503).send({
          success: false,
          message: 'AI service is currently offline. Please try again later.'
        });
      }

      // Use service to generate response
      const chatRequest = {
        messages: messages as ChatMessage[],
        userContext,
        model
      };

      const response = await aiService.chat(chatRequest);

      fastify.log.info(`[AI] Response from model ${response.model}: ${response.response?.substring(0, 100)}...`);

      return reply.send({
        success: true,
        data: {
          response: response.response,
          model: response.model,
          done: response.done
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

  // ========================================
  // Phân tích hóa đơn OCR (sử dụng AIService)
  // ========================================
  fastify.post('/analyze-invoice', async (request: any, reply: any) => {
    try {
      const { rawText, userContext } = request.body;

      if (!rawText) {
        return reply.status(400).send({
          success: false,
          message: 'rawText is required'
        });
      }

      fastify.log.info(`[AI] Analyzing invoice (${rawText.length} chars)`);

      const aiService = getAIService();
      const result = await aiService.analyzeInvoice(rawText, userContext);

      return reply.send({
        success: true,
        data: result
      });
    } catch (error: any) {
      fastify.log.error(`[AI] Invoice analysis error: ${error.message}`);
      return reply.status(500).send({
        success: false,
        message: `Invoice analysis error: ${error.message}`
      });
    }
  });

  // ========================================
  // Gợi ý phân bổ quỹ tiết kiệm (sử dụng AIService)
  // ========================================
  fastify.post('/suggest-allocation', async (request: any, reply: any) => {
    try {
      const { income, expenses, funds } = request.body;

      if (!income || !expenses || !funds) {
        return reply.status(400).send({
          success: false,
          message: 'income, expenses, and funds are required'
        });
      }

      fastify.log.info(`[AI] Generating fund allocation suggestions`);

      const aiService = getAIService();
      const result = await aiService.suggestFundAllocation(income, expenses, funds);

      return reply.send({
        success: true,
        data: result
      });
    } catch (error: any) {
      fastify.log.error(`[AI] Allocation suggestion error: ${error.message}`);
      return reply.status(500).send({
        success: false,
        message: `Allocation suggestion error: ${error.message}`
      });
    }
  });

  // ========================================
  // Đổi model AI
  // ========================================
  fastify.put('/model', async (request: any, reply: any) => {
    try {
      const { model } = request.body;

      if (!model) {
        return reply.status(400).send({
          success: false,
          message: 'model is required'
        });
      }

      const aiService = getAIService();
      const status = await aiService.checkStatus();

      if (!status.models.includes(model)) {
        return reply.status(400).send({
          success: false,
          message: `Model '${model}' is not available. Available models: ${status.models.join(', ')}`
        });
      }

      aiService.setModel(model);

      return reply.send({
        success: true,
        message: `Model changed to ${model}`
      });
    } catch (error: any) {
      fastify.log.error(`[AI] Model change error: ${error.message}`);
      return reply.status(500).send({
        success: false,
        message: `Model change error: ${error.message}`
      });
    }
  });

  // ========================================
  // Lấy danh sách model có sẵn
  // ========================================
  fastify.get('/models', async (request: any, reply: any) => {
    try {
      const aiService = getAIService();
      const status = await aiService.checkStatus();
      const availableModels = aiService.getAvailableModels();

      return reply.send({
        success: true,
        data: {
          status: status.status,
          models: availableModels,
          url: status.url
        }
      });
    } catch (error: any) {
      fastify.log.error(`[AI] Get models error: ${error.message}`);
      return reply.status(500).send({
        success: false,
        message: `Get models error: ${error.message}`
      });
    }
  });
}
