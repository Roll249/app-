# Service Layer Architecture

## Tổng quan

Phần mềm quản lý tài chính cá nhân được tái cấu trúc theo mô hình **Service-Oriented Architecture (SOA)** để tích hợp và quản lý các dịch vụ bên ngoài một cách hiệu quả.

## Kiến trúc tổng quan

```
┌─────────────────────────────────────────────────────────────────────────┐
│                           ANDROID CLIENT                                  │
│  ┌─────────────────────────────────────────────────────────────────────┐ │
│  │                      UI Layer (Compose)                              │ │
│  │  ServicesScreen | MarketScreen | AIChatScreen | ...                 │ │
│  └─────────────────────────────────────────────────────────────────────┘ │
│  ┌─────────────────────────────────────────────────────────────────────┐ │
│  │                   Data Layer (Retrofit + Room)                       │ │
│  │  ServiceManager | MarketManager | ServiceManager | ...                │ │
│  └─────────────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────────┘
                                    │
                                    │ HTTP/REST
                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                            BACKEND SERVER                                 │
│  ┌─────────────────────────────────────────────────────────────────────┐ │
│  │                      API Routes Layer                                 │ │
│  │  /api/v1/services/* | /api/v1/market/* | /api/v1/ai/* | ...         │ │
│  └─────────────────────────────────────────────────────────────────────┘ │
│  ┌─────────────────────────────────────────────────────────────────────┐ │
│  │                      Service Registry                                │ │
│  │  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐     │ │
│  │  │AI       │ │Banking  │ │Market   │ │Invest-  │ │Notifi-  │     │ │
│  │  │Service  │ │Service  │ │Service  │ │mentSvc  │ │cationSvc│     │ │
│  │  └─────────┘ └─────────┘ └─────────┘ └─────────┘ └─────────┘     │ │
│  └─────────────────────────────────────────────────────────────────────┘ │
│  ┌─────────────────────────────────────────────────────────────────────┐ │
│  │                    External Services                                  │ │
│  │  Ollama | VietQR | FCM | Google Drive | Real APIs                    │ │
│  └─────────────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────────┘
```

## Các Service đã triển khai

### 1. AI Service (`/services/ai/`)

**Chức năng:**
- Chatbot AI tài chính cá nhân (Fina)
- Phân tích hóa đơn OCR
- Gợi ý phân bổ quỹ tiết kiệm
- Đổi model AI

**Endpoints:**
- `POST /api/v1/ai/chat` - Gửi tin nhắn chat
- `POST /api/v1/ai/analyze-invoice` - Phân tích hóa đơn
- `POST /api/v1/ai/suggest-allocation` - Gợi ý phân bổ
- `GET /api/v1/ai/models` - Danh sách model có sẵn
- `PUT /api/v1/ai/model` - Đổi model

**External API:** Ollama (qwen3:8b, llava, mistral...)

### 2. Banking Service (`/services/banking/`)

**Chức năng:**
- Quản lý tài khoản ngân hàng liên kết
- Chuyển khoản nội bộ
- Tạo mã VietQR
- Truy vấn số dư

**Endpoints:**
- `GET /api/v1/banking/accounts` - Danh sách tài khoản
- `POST /api/v1/banking/link` - Liên kết tài khoản
- `POST /api/v1/banking/transfer` - Chuyển khoản
- `GET /api/v1/banking/balance/:id` - Số dư

**External APIs:** VietQR, Ngân hàng Việt Nam (mô phỏng)

### 3. Notification Service (`/services/notification/`)

**Chức năng:**
- Gửi thông báo đẩy (Push)
- Gửi email
- Gửi SMS
- Cài đặt thông báo theo người dùng

**Endpoints:**
- `POST /api/v1/notification/send` - Gửi thông báo
- `GET /api/v1/notification/preferences` - Lấy cài đặt
- `PUT /api/v1/notification/preferences` - Cập nhật cài đặt

**External APIs:** Firebase Cloud Messaging (FCM), Email (SendGrid), SMS (Twilio)

### 4. Sync Service (`/services/sync/`)

**Chức năng:**
- Đồng bộ dữ liệu đa thiết bị
- Cloud backup
- Conflict resolution

**Endpoints:**
- `POST /api/v1/sync/trigger` - Kích hoạt đồng bộ
- `GET /api/v1/sync/status` - Trạng thái đồng bộ
- `POST /api/v1/sync/resolve` - Giải quyết xung đột

**External APIs:** Google Drive API, iCloud

### 5. Market Service (`/api/v1/market/`)

**Chức năng:**
- Tỷ giá ngoại tệ
- Giá vàng
- Chỉ số chứng khoán
- Giá tiền điện tử
- Chuyển đổi tiền tệ

**Endpoints:**
- `GET /api/v1/market` - Tất cả dữ liệu thị trường
- `GET /api/v1/market/exchange-rates` - Tỷ giá
- `GET /api/v1/market/gold-prices` - Giá vàng
- `GET /api/v1/market/stock-indices` - Chỉ số chứng khoán
- `GET /api/v1/market/crypto-prices` - Giá crypto
- `GET /api/v1/market/convert` - Chuyển đổi tiền tệ

### 6. Investment Service (`/api/v1/investments/`)

**Chức năng:**
- Quản lý danh mục đầu tư
- Theo dõi lợi nhuận/lỗ
- Gợi ý đầu tư
- Phân tích rủi ro

**Endpoints:**
- `GET /api/v1/investments/portfolio` - Danh mục đầu tư
- `POST /api/v1/investments/investments` - Thêm đầu tư
- `GET /api/v1/investments/recommendations` - Gợi ý
- `GET /api/v1/investments/risk-profile` - Hồ sơ rủi ro
- `GET /api/v1/investments/performance` - Hiệu suất

## Services Health Check

**Endpoint:** `GET /api/v1/services/`

```json
{
  "success": true,
  "data": {
    "overall": "healthy",
    "services": [
      {
        "name": "AIService",
        "status": "initialized",
        "health": {
          "status": "healthy",
          "latencyMs": 45,
          "lastChecked": "2026-04-12T10:30:00.000Z"
        }
      }
    ],
    "lastChecked": "2026-04-12T10:30:00.000Z"
  }
}
```

## Cấu trúc thư mục

```
backend/src/
├── services/
│   ├── base/
│   │   └── BaseService.ts          # Base interface cho all services
│   ├── ai/
│   │   └── AIService.ts            # AI/Chatbot integration
│   ├── banking/
│   │   └── BankingService.ts       # Banking integration
│   ├── notification/
│   │   └── NotificationService.ts  # Push/Email/SMS
│   ├── sync/
│   │   └── SyncService.ts          # Data synchronization
│   ├── market/
│   │   └── MarketService.ts        # Market data (FX, Gold, Crypto)
│   ├── investment/
│   │   └── InvestmentService.ts    # Investment portfolio
│   ├── ServiceRegistry.ts          # Central service manager
│   └── index.ts
├── routes/
│   ├── services.ts                  # Services health endpoints
│   ├── market.ts                    # Market data endpoints
│   ├── investment.ts                # Investment endpoints
│   └── ...
└── index.ts
```

## Mô hình Service Interface

```typescript
export interface IService {
  /** Initialize the service */
  initialize(): Promise<void>;
  
  /** Check if service is available */
  isAvailable(): Promise<boolean>;
  
  /** Get service health status */
  getHealth(): Promise<ServiceHealth>;
  
  /** Get service name */
  getName(): string;
}
```

## Tích hợp dịch vụ mới

Để thêm một dịch vụ mới:

1. **Tạo service class** trong `src/services/<name>/`:
```typescript
import { BaseService, ServiceConfig } from '../base/BaseService';

export class MyService extends BaseService {
  getName(): string { return 'MyService'; }
  
  async initialize(): Promise<void> {
    // Connect to external API
  }
}
```

2. **Đăng ký trong ServiceRegistry:**
```typescript
import { MyService } from './my/MyService';

const myService = new MyService(config, logger);
await myService.initialize();
this.services.set('my', myService);
```

3. **Tạo routes** trong `src/routes/`:
```typescript
export async function myRoutes(fastify: any) {
  // Define endpoints
}
```

4. **Đăng ký routes** trong `index.ts`:
```typescript
import { myRoutes } from './routes/my';
await fastify.register(myRoutes, { prefix: '/api/v1/my' });
```

## Environment Variables

```env
# AI Service
OLLAMA_URL=http://localhost:11434
OLLAMA_MODEL=qwen3:8b

# Banking
BANKING_API_URL=https://api.vietqr.vn

# Notifications
FCM_SERVER_KEY=your-fcm-key
EMAIL_API_KEY=your-email-key
SMS_API_KEY=your-sms-key

# Sync
SYNC_PROVIDER=google_drive
AUTO_SYNC_ENABLED=true
AUTO_SYNC_INTERVAL=300000

# Market
MARKET_CACHE_DURATION=300
```

## Monitoring

Health status của tất cả services có thể được kiểm tra qua:
- Backend: `GET /api/v1/services`
- Android: ServicesScreen trong ứng dụng

## Bảo mật

- API keys được lưu trong environment variables
- Rate limiting áp dụng cho tất cả endpoints
- JWT authentication cho protected routes
- Service-level error handling
