/**
 * AI Service - External Service Integration
 * Handles communication with Ollama AI API
 */
import { BaseService, ServiceConfig, ServiceHealth } from '../base/BaseService.js';

export interface AIModel {
  name: string;
  modified_at: string;
}

export interface ChatMessage {
  role: 'user' | 'assistant' | 'system';
  content: string;
}

export interface ChatRequest {
  messages: ChatMessage[];
  userContext?: string;
  model?: string;
}

export interface ChatResponse {
  response: string;
  model: string;
  done: boolean;
}

export interface AIStatus {
  status: 'online' | 'offline' | 'error';
  models: string[];
  url: string;
  message?: string;
}

// System prompt for financial AI assistant
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

export class AIService extends BaseService {
  private availableModels: string[] = [];
  private currentModel: string = 'qwen3:8b';

  constructor(config: ServiceConfig, logger: any) {
    super(config, logger);
  }

  getName(): string {
    return 'AIService';
  }

  async initialize(): Promise<void> {
    this.logger.info(`[${this.getName()}] Initializing AI service...`);
    
    try {
      const status = await this.checkStatus();
      if (status.status === 'online') {
        this.availableModels = status.models;
        this.isInitialized = true;
        this.logger.info(`[${this.getName()}] Connected to Ollama. Available models: ${this.availableModels.join(', ')}`);
      } else {
        this.logger.warn(`[${this.getName()}] Ollama is not available: ${status.message}`);
      }
    } catch (error: any) {
      this.logger.error(`[${this.getName()}] Failed to initialize: ${error.message}`);
    }
  }

  protected async ping(): Promise<boolean> {
    try {
      const response = await fetch(`${this.config.baseUrl}/api/tags`, {
        method: 'GET',
        signal: AbortSignal.timeout(3000)
      });
      return response.ok;
    } catch {
      return false;
    }
  }

  async getHealth(): Promise<ServiceHealth> {
    const startTime = Date.now();
    try {
      const status = await this.checkStatus();
      return {
        status: status.status === 'online' ? 'healthy' : 'offline',
        latencyMs: Date.now() - startTime,
        message: status.message,
        lastChecked: new Date()
      };
    } catch (error: any) {
      return {
        status: 'offline',
        latencyMs: Date.now() - startTime,
        message: error.message,
        lastChecked: new Date()
      };
    }
  }

  async checkStatus(): Promise<AIStatus> {
    try {
      const controller = new AbortController();
      const timeout = setTimeout(() => controller.abort(), 3000);

      const response = await fetch(`${this.config.baseUrl}/api/tags`, {
        signal: controller.signal
      });

      clearTimeout(timeout);

      if (response.ok) {
        const data = await response.json() as { models?: AIModel[] };
        const models = data.models?.map(m => m.name) || [];
        
        return {
          status: 'online',
          models: models,
          url: this.config.baseUrl
        };
      } else {
        return {
          status: 'error',
          models: [],
          url: this.config.baseUrl,
          message: `Ollama returned ${response.status}`
        };
      }
    } catch (error: any) {
      return {
        status: 'offline',
        models: [],
        url: this.config.baseUrl,
        message: error.message
      };
    }
  }

  async chat(request: ChatRequest): Promise<ChatResponse> {
    const { messages, userContext, model } = request;
    const modelName = model || this.currentModel;

    this.logger.info(`[${this.getName()}] Chat request, model: ${modelName}, messages: ${messages.length}`);

    // Build system prompt with user context
    let systemMessage = SYSTEM_PROMPT;
    if (userContext) {
      systemMessage += `\n\n## Ngữ cảnh người dùng:\n${userContext}`;
    }

    // Build conversation history
    const conversationHistory = messages
      .map(m => `${m.role === 'user' ? 'User' : 'Assistant'}: ${m.content}`)
      .join('\n');

    const fullPrompt = `${systemMessage}\n\n${conversationHistory}\n\nAssistant:`;

    this.logger.info(`[${this.getName()}] Sending prompt to Ollama (${fullPrompt.length} chars)`);

    const response = await fetch(`${this.config.baseUrl}/api/generate`, {
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
      this.logger.error(`[${this.getName()}] Ollama error: ${errorText}`);
      throw new Error(`AI service error: ${response.status}`);
    }

    const data: any = await response.json();

    this.logger.info(`[${this.getName()}] Response from model ${modelName}: ${data.response?.substring(0, 100)}...`);

    return {
      response: data.response,
      model: data.model || modelName,
      done: data.done
    };
  }

  async analyzeInvoice(rawText: string, userContext?: string): Promise<any> {
    const prompt = `
Bạn là trợ lý phân tích hóa đơn tài chính. Hãy phân tích văn bản hóa đơn sau và trả về JSON:

Văn bản hóa đơn:
${rawText}

Ngữ cảnh người dùng: ${userContext || 'Không có'}

Hãy trả về JSON với các trường:
- amount: số tiền (number)
- date: ngày tháng năm (string, format YYYY-MM-DD)
- merchant: tên người bán (string)
- category: danh mục chi tiêu gợi ý (string)
- description: mô tả ngắn (string)

Chỉ trả về JSON, không giải thích gì thêm.
    `.trim();

    const response = await this.chat({
      messages: [{ role: 'user', content: prompt }]
    });

    // Parse JSON response
    try {
      const jsonMatch = response.response.match(/\{[\s\S]*\}/);
      if (jsonMatch) {
        return JSON.parse(jsonMatch[0]);
      }
    } catch (e) {
      this.logger.error(`[${this.getName()}] Failed to parse invoice JSON: ${e}`);
    }

    return null;
  }

  async suggestFundAllocation(
    income: number,
    expenses: Record<string, number>,
    funds: Array<{ id: string; name: string; currentAmount: number; targetAmount: number }>
  ): Promise<any> {
    const fundsJson = funds.map(f => `- ${f.name}: ${f.currentAmount} / ${f.targetAmount}`).join('\n');
    const expensesJson = Object.entries(expenses).map(([k, v]) => `- ${k}: ${v}`).join('\n');

    const prompt = `
Bạn là chuyên gia tư vấn tài chính. Hãy đề xuất cách chia tiền vào các quỹ tiết kiệm.

Thu nhập tháng: ${income.toLong()} VND
Chi tiêu hiện tại:
${expensesJson}

Các quỹ hiện tại:
${fundsJson}

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
    `.trim();

    const response = await this.chat({
      messages: [{ role: 'user', content: prompt }]
    });

    try {
      const jsonMatch = response.response.match(/\{[\s\S]*\}/);
      if (jsonMatch) {
        return JSON.parse(jsonMatch[0]);
      }
    } catch (e) {
      this.logger.error(`[${this.getName()}] Failed to parse allocation JSON: ${e}`);
    }

    return null;
  }

  setModel(model: string): void {
    this.currentModel = model;
    this.logger.info(`[${this.getName()}] Model changed to: ${model}`);
  }

  getAvailableModels(): string[] {
    return this.availableModels;
  }
}
