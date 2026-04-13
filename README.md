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

## Yêu cầu hệ thống

### Backend
- Node.js 20+
- PostgreSQL 16+
- Docker & Docker Compose (khuyến nghị)
- Ollama (tùy chọn, cho AI Chatbot)

### Android Client
- Android SDK 35+
- Kotlin 2.0
- Java 17+
- Android Studio Ladybug hoặc mới hơn

---

## 🚀 Hướng Dẫn Setup Nhanh (5 Phút)

### Cách 1: Sử dụng Docker (Khuyến nghị)

```bash
# 1. Clone project
git clone <repo-url>
cd personal-finance-app

# 2. Copy và cấu hình file môi trường
cp .env.example .env

# 3. Khởi động PostgreSQL và Backend
docker-compose up -d postgres backend

# 4. Chờ database khởi tạo (~10 giây), sau đó setup database
docker-compose exec backend npm run db:setup

# 5. Backend đã chạy tại http://localhost:3000
```

### Cách 2: Chạy Backend trực tiếp (không dùng Docker)

```bash
# 1. Đảm bảo đã cài đặt PostgreSQL
# Ubuntu/Debian:
sudo apt install postgresql postgresql-contrib

# macOS:
brew install postgresql
brew services start postgresql

# Windows: Tải từ https://www.postgresql.org/download/

# 2. Tạo database
sudo -u postgres psql
CREATE DATABASE fintech_db;
CREATE USER fintech WITH PASSWORD 'postgres123';
GRANT ALL PRIVILEGES ON DATABASE fintech_db TO fintech;
\q

# 3. Clone và setup backend
cd backend
npm install
cp ../.env.example .env  # Chỉnh sửa .env nếu cần

# 4. Setup database và chạy
npm run db:setup
npm run dev

# Backend chạy tại http://localhost:3000
```

### Cách 3: Setup Android Client

```bash
# 1. Cài đặt Android Studio
# Tải từ: https://developer.android.com/studio

# 2. Import project android-client vào Android Studio

# 3. Sync Gradle (Android Studio sẽ tự động)

# 4. Build và chạy
./gradlew assembleDebug
# Hoặc chạy trực tiếp trên thiết bị/emulator từ Android Studio
```

---

## 📋 Cấu Hình Chi Tiết

### 1. File .env

Copy `.env.example` sang `.env` và cấu hình:

```bash
cp .env.example .env
```

#### Cấu hình Database (bắt buộc)

```env
# PostgreSQL
POSTGRES_PASSWORD=postgres123
POSTGRES_PORT=5432

# Nếu dùng Docker, database URL sẽ tự động được set
# Nếu chạy local:
DATABASE_URL=postgres://postgres:postgres123@localhost:5432/fintech_db
```

#### Cấu hình JWT (bắt buộc)

```env
# Thay đổi các giá trị này bằng chuỗi ngẫu nhiên bảo mật
JWT_SECRET=your-super-secret-jwt-key-minimum-32-characters-here
JWT_REFRESH_SECRET=your-super-secret-refresh-key-minimum-64-characters-here
```

#### CORS Configuration

```env
# Các origins được phép (comma-separated)
CORS_ORIGIN=http://localhost:3000,http://10.0.2.2:3000

# Android Emulator: http://10.0.2.2:3000
# Android Device qua ngrok: https://your-ngrok-url.ngrok-free.app
```

### 2. Cấu hình Android Client

#### API URL

Chỉnh sửa API base URL trong Android app:

**Tùy chọn A: Dùng ngrok (cho device thật)**

```bash
# Cài đặt ngrok
# Đăng ký tài khoản tại https://ngrok.com

# Chạy ngrok
ngrok http 3000

# Copy URL được cấp (ví dụ: https://abc123.ngrok-free.app)
# Paste vào Android app API config
```

**Tùy chọn B: Dùng Android Emulator**

```gradle
// Trong android-client/app/build.gradle.kts
// Hoặc sử dụng BuildConfig
```

**Tùy chọn C: Dùng Android Studio Device Manager**

Android Emulator có thể truy cập localhost qua địa chỉ `10.0.2.2:3000`.

### 3. Cấu hình Ollama (cho AI Chatbot)

#### Cách 1: Cài Ollama trên máy local

```bash
# macOS/Linux
curl -fsSL https://ollama.com/install.sh | sh

# Windows: Tải từ https://ollama.com/download

# Pull model
ollama pull qwen3:8b

# Khởi động server
ollama serve
```

Trong `.env`:
```env
OLLAMA_URL=http://localhost:11434
OLLAMA_MODEL=qwen3:8b
```

#### Cách 2: Dùng Ollama server có sẵn

```env
OLLAMA_URL=https://your-ollama-server.com
OLLAMA_MODEL=qwen3:8b
```

#### Cách 3: Disable AI Service (nếu không cần)

AI service sẽ tự động chuyển sang chế độ mock nếu Ollama không khả dụng.

---

## 🗄️ Database Setup

### PostgreSQL Commands

```bash
# Tạo database
sudo -u postgres psql -c "CREATE DATABASE fintech_db;"

# Tạo user
sudo -u postgres psql -c "CREATE USER fintech WITH PASSWORD 'postgres123';"

# Grant privileges
sudo -u postgres psql -c "GRANT ALL PRIVILEGES ON DATABASE fintech_db TO fintech;"

# Setup xong, chạy migration
npm run db:setup
```

### Database Migration

```bash
cd backend

# Setup database (tạo tables, seed data)
npm run db:setup

# Seed thêm demo data (tùy chọn)
npm run db:seed

# Hoặc dùng CLI tool
node src/scripts/simulation-cli.js seed 5
```

---

## 🔧 Troubleshooting

### Backend không chạy được

**Lỗi: Database connection failed**

```bash
# Kiểm tra PostgreSQL đang chạy
sudo systemctl status postgresql
# Hoặc docker
docker ps

# Kiểm tra database tồn tại
psql -U postgres -c "\l"
```

**Lỗi: Port 3000 đã được sử dụng**

```bash
# Tìm process đang dùng port 3000
lsof -i :3000
# Hoặc
sudo netstat -tlnp | grep 3000

# Thay đổi port trong .env
BACKEND_PORT=3001
```

**Lỗi: Module not found**

```bash
cd backend
rm -rf node_modules package-lock.json
npm install
```

### Android build failed

**Lỗi: Gradle sync failed**

```bash
cd android-client
./gradlew clean
./gradlew --stop
# Sync lại trong Android Studio
```

**Lỗi: SDK not found**

```bash
# Kiểm tra ANDROID_HOME
echo $ANDROID_HOME

# Set trong local.properties
echo "sdk.dir=/path/to/android/sdk" > android-client/local.properties
```

**Lỗi: Java version mismatch**

```bash
# Kiểm tra Java version
java -version
# Cần Java 17+

# Đổi Java version
# macOS:
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
# Ubuntu:
sudo update-alternatives --config java
```

### Database migration errors

**Lỗi: Table already exists**

```bash
# Xóa database và tạo lại
sudo -u postgres psql -c "DROP DATABASE fintech_db;"
sudo -u postgres psql -c "CREATE DATABASE fintech_db;"
npm run db:setup
```

---

## 📱 Cấu Trúc Project

```
personal-finance-app/
├── backend/                      # Node.js/Fastify Backend
│   ├── src/
│   │   ├── index.ts            # Entry point
│   │   ├── routes/             # API endpoints
│   │   │   ├── auth.ts         # Authentication
│   │   │   ├── transactions.ts # Transactions
│   │   │   ├── accounts.ts     # Accounts
│   │   │   ├── ai.ts           # AI Chatbot
│   │   │   ├── market.ts       # Market data
│   │   │   └── services/       # Service endpoints
│   │   ├── services/           # Service Layer
│   │   │   ├── ai/             # AI Service (Ollama)
│   │   │   ├── banking/         # Banking Service
│   │   │   ├── market/         # Market Service
│   │   │   ├── investment/     # Investment Service
│   │   │   ├── notification/   # Notification Service
│   │   │   ├── sync/           # Sync Service
│   │   │   └── ServiceRegistry.ts
│   │   ├── utils/
│   │   │   ├── db.ts           # Database utilities
│   │   │   └── simulation.ts   # Demo data generator
│   │   └── scripts/
│   │       ├── setup-db.ts     # Database setup
│   │       └── simulation-cli.js # CLI tool
│   ├── Dockerfile
│   └── package.json
│
├── android-client/               # Android Client
│   └── app/src/main/java/com/fintech/
│       ├── data/
│       │   ├── local/          # Room Database
│       │   ├── remote/         # Retrofit API
│       │   └── repository/     # Repository pattern
│       ├── domain/
│       │   └── model/          # Domain models
│       └── ui/
│           ├── home/           # Home screen
│           ├── transactions/   # Transaction screens
│           ├── accounts/       # Account screens
│           ├── ai/             # AI Chat screen
│           ├── market/         # Market screen
│           └── services/       # Services management
│
├── docs/
│   ├── SERVICE_LAYER.md        # Service architecture
│   └── SIMULATION_EVENTS.md     # Event simulation guide
│
├── docker-compose.yml           # Docker orchestration
├── .env.example                 # Environment template
└── README.md
```

---

## 🔌 API Endpoints

### Authentication
```
POST /api/v1/auth/register   - Đăng ký tài khoản mới
POST /api/v1/auth/login      - Đăng nhập
POST /api/v1/auth/refresh    - Refresh token
POST /api/v1/auth/logout     - Đăng xuất
```

### Core
```
GET  /api/v1/accounts         - Danh sách tài khoản
POST /api/v1/accounts         - Tạo tài khoản
GET  /api/v1/transactions     - Danh sách giao dịch
POST /api/v1/transactions     - Tạo giao dịch
GET  /api/v1/funds            - Danh sách quỹ
POST /api/v1/funds            - Tạo quỹ tiết kiệm
GET  /api/v1/budgets          - Danh sách ngân sách
POST /api/v1/budgets          - Tạo ngân sách
```

### Banking
```
GET  /api/v1/banks             - Danh sách ngân hàng
POST /api/v1/banks/connect     - Liên kết tài khoản ngân hàng
POST /api/v1/qr/generate      - Tạo mã QR
POST /api/v1/qr/payment       - Thanh toán QR
POST /api/v1/transfer          - Chuyển khoản
```

### AI Chatbot
```
POST /api/v1/ai/chat          - Gửi tin nhắn chat
GET  /api/v1/ai/health         - Kiểm tra AI service
```

### Market Data
```
GET  /api/v1/market            - Tất cả dữ liệu thị trường
GET  /api/v1/market/exchange-rates  - Tỷ giá ngoại tệ
GET  /api/v1/market/gold-prices     - Giá vàng
GET  /api/v1/market/stock-indices   - Chỉ số chứng khoán
GET  /api/v1/market/crypto-prices   - Giá tiền điện tử
```

### Services
```
GET  /api/v1/services/         - Trạng thái tất cả services
GET  /api/v1/services/ai       - AI service status
GET  /api/v1/services/banking  - Banking service status
GET  /api/v1/services/notification - Notification status
GET  /api/v1/services/sync     - Sync service status
```

### Simulation
```
POST /api/v1/simulation/demo      - Tạo demo user
GET  /api/v1/simulation/scenarios - Danh sách kịch bản
POST /api/v1/simulation/trigger   - Trigger event
GET  /api/v1/simulation/stats     - Thống kê simulation
```

---

## ⚙️ Cấu Hình Services Ngoài (Optional)

### 1. AI Service (Ollama)

**Miễn phí - Self-hosted**

```bash
# Cài Ollama
curl -fsSL https://ollama.com/install.sh | sh

# Pull model
ollama pull qwen3:8b

# Chạy
ollama serve
```

**.env:**
```env
OLLAMA_URL=http://localhost:11434
OLLAMA_MODEL=qwen3:8b
```

### 2. Firebase Cloud Messaging (Push Notification)

1. Tạo project tại [Firebase Console](https://console.firebase.google.com/)
2. Thêm Android app với package name: `com.fintech`
3. Download `google-services.json`
4. Lấy Server Key từ **Project Settings → Cloud Messaging**

**.env:**
```env
FCM_SERVER_KEY=your-fcm-server-key
```

### 3. Email Service (SendGrid)

**Miễn phí - 100 emails/ngày**

1. Đăng ký [SendGrid](https://sendgrid.com/)
2. Verify sender email
3. Tạo API Key

**.env:**
```env
EMAIL_API_KEY=SG.your-sendgrid-api-key
EMAIL_FROM=noreply@yourdomain.com
```

### 4. SMS Service (Twilio)

**Trả phí - ~$0.01/sms**

1. Đăng ký [Twilio](https://www.twilio.com/)
2. Lấy Account SID và Auth Token
3. Đăng ký số điện thoại gửi

**.env:**
```env
SMS_API_KEY=ACCOUNT_SID:AUTH_TOKEN
SMS_FROM=+1234567890
```

### 5. Market Data APIs

**Exchangerate-api (Miễn phí - 1500 requests/tháng)**

1. Đăng ký [Exchangerate-api](https://exchangerate-api.com/)
2. Lấy API Key

**.env:**
```env
MARKET_API_KEY=your-exchangerate-api-key
```

**Polygon.io (Stock Data)**

1. Đăng ký [Polygon.io](https://polygon.io/)
2. Chọn Free tier

**.env:**
```env
STOCK_API_KEY=your-polygon-api-key
```

### 6. Google Drive Sync

1. Tạo project tại [Google Cloud Console](https://console.cloud.google.com/)
2. Enable **Google Drive API**
3. Tạo OAuth Client ID (Desktop app)
4. Download credentials

**.env:**
```env
SYNC_PROVIDER=google_drive
GOOGLE_DRIVE_CREDENTIALS_PATH=./config/google-drive-credentials.json
```

---

## 📊 Bảng Tổng Hợp API Keys

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

**Lưu ý:** Tất cả services đều optional. App sẽ hoạt động với dữ liệu mock nếu không có API keys.

---

## 🧪 Testing

### Tạo Demo Data

```bash
cd backend

# Tạo 5 demo users với transactions ngẫu nhiên
node src/scripts/simulation-cli.js seed 5

# Tạo user với kịch bản cụ thể
node src/scripts/simulation-cli.js create tech_lead "Test User"

# Trigger event
node src/scripts/simulation-cli.js trigger salary 15000000
node src/scripts/simulation-cli.js trigger expense 50000

# Xem thống kê
node src/scripts/simulation-cli.js stats
```

### Test API

```bash
# Health check
curl http://localhost:3000/health

# Register
curl -X POST http://localhost:3000/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"Test123!","fullName":"Test User"}'

# Login
curl -X POST http://localhost:3000/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"Test123!"}'
```

---

## 🐳 Docker Commands

```bash
# Khởi động tất cả services
docker-compose up -d

# Khởi động với logs
docker-compose up -d --follow

# Xem logs
docker-compose logs -f backend

# Stop services
docker-compose down

# Stop và xóa volumes (reset database)
docker-compose down -v

# Rebuild images
docker-compose build --no-cache
```

### Docker Profiles

```bash
# Chạy với devtools (pgAdmin)
docker-compose --profile devtools up -d

# Truy cập pgAdmin tại http://localhost:5050
# Email: admin@fintech.local
# Password: admin123
```

---

## 🔒 Security Notes

### Development
- Sử dụng `.env` file (đã ignore trong .gitignore)
- Không commit secrets vào git

### Production
```env
NODE_ENV=production
JWT_SECRET=<use-crypto-random-64-chars>
JWT_REFRESH_SECRET=<use-crypto-random-128-chars>
```

Sử dụng secrets manager:
- AWS Secrets Manager
- Google Secret Manager
- HashiCorp Vault

---

## 📞 Hỗ Trợ

- **Bug reports**: Tạo issue trên GitHub
- **Questions**: Liên hệ qua email
- **Documentation**: Xem thêm trong `/docs`

---

## 📄 License

MIT License