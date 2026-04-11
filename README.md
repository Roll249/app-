# Personal Finance App V2

Ứng dụng quản lý tài chính cá nhân hoàn chỉnh với kiến trúc hướng dịch vụ (Service-Oriented Architecture).

## Tính năng chính

- **Quản lý tài khoản**: Tiền mặt, ngân hàng, ví điện tử
- **Theo dõi giao dịch**: Thu chi, chuyển khoản
- **Quỹ tiết kiệm**: Mục tiêu tiết kiệm cá nhân
- **Ngân sách**: Giới hạn chi tiêu theo danh mục
- **Báo cáo**: Biểu đồ thu chi, xu hướng
- **10 Ngân hàng ảo**: Vietcombank, VietinBank, BIDV, TPBank, ACB, MBBank, SHB, OCB, HDBank, VIB
- **QR Code**: Tạo và quét mã QR VietQR
- **AI Chatbot (Fina)**: Trợ lý AI tài chính cá nhân
- **Thị trường tài chính**: Tỷ giá, vàng, chứng khoán, crypto
- **Dịch vụ đồng bộ**: Cloud backup, đồng bộ đa thiết bị

## Kiến trúc dịch vụ (Service Layer)

Phần mềm sử dụng kiến trúc hướng dịch vụ để tích hợp các API bên ngoài:

| Service | Mô tả | API |
|---------|--------|-----|
| **AIService** | Chatbot AI, phân tích hóa đơn | Ollama |
| **BankingService** | Ngân hàng, VietQR, chuyển khoản | VietQR API |
| **NotificationService** | Push, Email, SMS | Firebase, SendGrid, Twilio |
| **SyncService** | Đồng bộ đa thiết bị | Google Drive |
| **MarketService** | Tỷ giá, vàng, chứng khoán | Real APIs |
| **InvestmentService** | Danh mục đầu tư | Stock APIs |

Xem chi tiết: [SERVICE_LAYER.md](./docs/SERVICE_LAYER.md)

## Backend (Node.js/Fastify)

### Công nghệ
- Node.js + TypeScript
- Fastify Web Framework
- PostgreSQL + pg
- JWT Authentication
- Service-Oriented Architecture

### Thiết lập

```bash
cd backend
npm install
cp ../.env.example .env  # Cấu hình các biến môi trường
npm run dev
```

Backend chạy trên port 3000.

### API Endpoints

**Authentication:**
- `POST /api/v1/auth/register` - Đăng ký
- `POST /api/v1/auth/login` - Đăng nhập

**Core:**
- `GET /api/v1/banks` - Danh sách ngân hàng
- `POST /api/v1/banks/connect` - Liên kết tài khoản ngân hàng
- `POST /api/v1/transactions` - Tạo giao dịch
- `GET /api/v1/reports/summary` - Báo cáo tổng quan

**Services (Health Check):**
- `GET /api/v1/services/` - Trạng thái tất cả services
- `GET /api/v1/services/ai` - AI service status
- `GET /api/v1/services/banking` - Banking service status
- `GET /api/v1/services/notification` - Notification status
- `GET /api/v1/services/sync` - Sync status

**Market Data:**
- `GET /api/v1/market` - Tất cả dữ liệu thị trường
- `GET /api/v1/market/exchange-rates` - Tỷ giá ngoại tệ
- `GET /api/v1/market/gold-prices` - Giá vàng
- `GET /api/v1/market/stock-indices` - Chỉ số chứng khoán
- `GET /api/v1/market/crypto-prices` - Giá tiền điện tử

**Investments:**
- `GET /api/v1/investments/portfolio` - Danh mục đầu tư
- `POST /api/v1/investments/investments` - Thêm đầu tư
- `GET /api/v1/investments/recommendations` - Gợi ý đầu tư
- `GET /api/v1/investments/risk-profile` - Hồ sơ rủi ro

## Android Client (Kotlin/Jetpack Compose)

### Công nghệ
- Kotlin 2.0
- Jetpack Compose + Material3
- Hilt DI
- Room Database
- Retrofit + OkHttp
- Clean Architecture + MVI

### Build

```bash
cd android-client
./gradlew assembleDebug
```

### Cấu hình

API URL: `https://acrobat-equate-emphasize.ngrok-free.dev/api/v1/`

### Màn hình mới (Service Integration)

- **Dịch vụ** (`ui/services/`): Quản lý trạng thái tất cả dịch vụ bên ngoài
- **Thị trường** (`ui/market/`): Tỷ giá, vàng, chứng khoán, crypto

## Cấu hình Services

### Cách lấy API Keys

#### 1. AI Service (Ollama)

**Miễn phí - Self-hosted**

```bash
# Cài đặt Ollama trên máy local
curl -fsSL https://ollama.com/install.sh | sh

# Tải model
ollama pull qwen3:8b

# Chạy server
ollama serve
```

Hoặc sử dụng server có sẵn:
```env
OLLAMA_URL=http://localhost:11434
# Hoặc server remote
OLLAMA_URL=https://research.neu.edu.vn/ollama
```

#### 2. Firebase Cloud Messaging (Push Notification)

**Bước 1:** Tạo project Firebase
1. Truy cập [Firebase Console](https://console.firebase.google.com/)
2. Tạo project mới
3. Thêm Android app với package name: `com.fintech`

**Bước 2:** Lấy FCM Server Key
1. Vào **Project Settings** → **Cloud Messaging**
2. Copy **Server key** (legacy token)

**Bước 3:** Cấu hình
```env
FCM_SERVER_KEY=AAAA...your-key-here
```

**Bước 4:** Thêm google-services.json
1. Download `google-services.json` từ Firebase Console
2. Đặt vào `android-client/app/`

#### 3. Email Service (SendGrid)

**Miễn phí - 100 emails/ngày**

1. Đăng ký [SendGrid](https://sendgrid.com/)
2. Verify sender email
3. Tạo API Key: **Settings** → **API Keys** → **Create API Key**
4. Chọn "Full Access" hoặc "Restricted Access" với quyền Mail Send

```env
EMAIL_API_KEY=SG.your-sendgrid-api-key
EMAIL_FROM=noreply@yourdomain.com
```

#### 4. SMS Service (Twilio)

**Trả phí - ~$0.01/sms**

1. Đăng ký [Twilio](https://www.twilio.com/)
2. Lấy Account SID và Auth Token từ Console
3. Đăng ký số điện thoại gửi

```env
SMS_API_KEY=ACCOUNT_SID:AUTH_TOKEN
SMS_FROM=+1234567890
```

#### 5. Market Data APIs

**Exchangerate-api (Miễn phí - 1500 requests/tháng)**

1. Đăng ký [Exchangerate-api](https://exchangerate-api.com/)
2. Lấy API Key từ dashboard

```env
MARKET_API_KEY=your-exchangerate-api-key
```

**Polygon.io (Stock Data)**

1. Đăng ký [Polygon.io](https://polygon.io/)
2. Chọn plan (Free tier available)

```env
STOCK_API_KEY=your-polygon-api-key
```

#### 6. VietQR API (Banking)

**Miễn phí cho mô phỏng**

Để sử dụng VietQR thật:
1. Đăng ký tài khoản VietQR
2. Lấy API Key từ dashboard

```env
VIETQR_API_KEY=your-vietqr-api-key
```

#### 7. Google Drive Sync

**Bước 1:** Tạo Google Cloud Project
1. Truy cập [Google Cloud Console](https://console.cloud.google.com/)
2. Tạo project mới

**Bước 2:** Enable Google Drive API
1. Vào **APIs & Services** → **Library**
2. Tìm và enable **Google Drive API**

**Bước 3:** Tạo OAuth Credentials
1. Vào **APIs & Services** → **Credentials**
2. Tạo **OAuth Client ID** (Desktop app)
3. Download JSON credentials

**Bước 4:** Cấu hình
```env
SYNC_PROVIDER=google_drive
GOOGLE_DRIVE_CREDENTIALS_PATH=./config/google-drive-credentials.json
```

### Bảng tổng hợp API Keys

| Service | Provider | Chi phí | Link đăng ký |
|---------|----------|---------|--------------|
| AI Chat | Ollama | Miễn phí | [ollama.com](https://ollama.com) |
| Push Notif | Firebase | Miễn phí | [firebase.google.com](https://firebase.google.com) |
| Email | SendGrid | Miễn phí (100/day) | [sendgrid.com](https://sendgrid.com) |
| SMS | Twilio | ~$0.01/sms | [twilio.com](https://twilio.com) |
| Tỷ giá | Exchangerate-api | Miễn phí | [exchangerate-api.com](https://exchangerate-api.com) |
| Chứng khoán | Polygon | Miễn phí (Limited) | [polygon.io](https://polygon.io) |
| VietQR | VietQR | Miễn phí (Demo) | [vietqr.io](https://vietqr.io) |
| Cloud Sync | Google Drive | Miễn phí (15GB) | [Google Cloud](https://console.cloud.google.com) |

### Môi trường Production

Để bảo mật API keys trong production:

```bash
# Sử dụng .env production (KHÔNG commit vào git)
.env.production
```

Hoặc sử dụng secrets manager:
- **AWS Secrets Manager**
- **Google Secret Manager**
- **HashiCorp Vault**

```env
# Ví dụ: Load từ secrets manager
OLLAMA_URL=${SECRET_OLLAMA_URL}
FCM_SERVER_KEY=${SECRET_FCM_KEY}
```

## Ngrok Setup

```bash
ngrok http 3000 --domain=acrobat-equate-emphasize.ngrok-free.dev
```

## Quy mô

- Hỗ trợ 30 người dùng đồng thời
- Rate limit: 100 requests/phút/user

## Cấu trúc thư mục

```
personal-finance-app/
├── backend/                  # Node.js/Fastify Backend
│   ├── src/
│   │   ├── routes/         # API endpoints
│   │   ├── services/       # Service Layer
│   │   │   ├── ai/        # AI Service
│   │   │   ├── banking/   # Banking Service
│   │   │   ├── market/    # Market Service
│   │   │   ├── investment/# Investment Service
│   │   │   ├── notification/ # Notification Service
│   │   │   ├── sync/      # Sync Service
│   │   │   └── ServiceRegistry.ts
│   │   └── utils/         # Utilities
│   └── package.json
│
├── android-client/           # Android Client
│   └── app/src/main/java/com/fintech/
│       ├── data/           # Data layer
│       │   ├── local/     # Room DB
│       │   ├── remote/    # Retrofit API
│       │   │   ├── api/   # API services
│       │   │   ├── market/# Market data
│       │   │   └── services/ # Service managers
│       │   └── repository/
│       ├── domain/         # Domain layer
│       └── ui/            # UI layer
│           ├── services/  # Services screen
│           ├── market/    # Market screen
│           └── ...
│
├── docs/
│   └── SERVICE_LAYER.md   # Service architecture docs
│
├── .env.example            # Environment template
└── README.md
```
