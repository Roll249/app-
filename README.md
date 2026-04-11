# Personal Finance App V2

Ứng dụng quản lý tài chính cá nhân hoàn chỉnh với:

## Tính năng chính

- **Quản lý tài khoản**: Tiền mặt, ngân hàng, ví điện tử
- **Theo dõi giao dịch**: Thu chi, chuyển khoản
- **Quỹ tiết kiệm**: Mục tiêu tiết kiệm cá nhân
- **Ngân sách**: Giới hạn chi tiêu theo danh mục
- **Báo cáo**: Biểu đồ thu chi, xu hướng
- **10 Ngân hàng ảo**: Vietcombank, VietinBank, BIDV, TPBank, ACB, MBBank, SHB, OCB, HDBank, VIB
- **QR Code**: Tạo và quét mã QR VietQR

## Backend (Kotlin/Ktor)

### Công nghệ
- Kotlin 2.0
- Ktor 2.3.x
- PostgreSQL + Exposed ORM
- JWT Authentication
- Redis Cache

### Thiết lập và khởi động Backend

### Khởi tạo Database (migration + seed data)

```bash
# Cách 1 (khuyên dùng): chạy PostgreSQL bằng Docker
docker compose up -d postgres

# Cách 2: nếu đã có PostgreSQL local thì tạo DB thủ công
psql -U postgres -c "CREATE DATABASE fintech_db;"
```

Sau khi DB đã sẵn sàng, chạy backend để tự động tạo schema và seed dữ liệu mẫu (ngân hàng + danh mục mặc định):

```bash
cd backend
npm install
npm run dev
```

Lần chạy đầu tiên backend sẽ tự động chạy migration theo mã nguồn (`initDatabase`) và tạo dữ liệu mặc định.

Backend chạy trên port 3000

### API Endpoints

- `POST /api/v1/auth/register` - Đăng ký
- `POST /api/v1/auth/login` - Đăng nhập
- `GET /api/v1/banks` - Danh sách ngân hàng
- `POST /api/v1/banks/connect` - Liên kết tài khoản ngân hàng
- `POST /api/v1/transactions` - Tạo giao dịch
- `GET /api/v1/reports/summary` - Báo cáo tổng quan

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
├── backend/              # Kotlin/Ktor Backend
│   ├── src/main/kotlin/com/fintech/
│   │   ├── plugins/     # Ktor plugins
│   │   ├── routes/      # API routes
│   │   ├── services/    # Business logic
│   │   ├── models/      # Database models
│   │   ├── dto/         # Data transfer objects
│   │   └── utils/       # Utilities
│   └── resources/       # Config & migrations
│
└── android-client/       # Android Client
    └── app/src/main/java/com/fintech/
        ├── di/           # Hilt modules
        ├── data/         # Data layer
        │   ├── local/    # Room DB
        │   ├── remote/   # Retrofit API
        │   └── repository/
        ├── domain/       # Domain layer
        │   ├── model/
        │   └── usecase/
        └── ui/            # UI layer
            ├── auth/
            ├── home/
            ├── account/
            ├── transaction/
            ├── fund/
            ├── bank/
            ├── budget/
            ├── report/
            ├── qr/
            └── profile/
```
