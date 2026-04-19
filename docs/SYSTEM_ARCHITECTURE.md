# Personal Finance App - System Architecture

## Overview

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           PERSONAL FINANCE APP                               │
│                                                                             │
│  ┌──────────────────────┐      ┌──────────────────────┐                   │
│  │    Android App       │      │    Backend API        │                   │
│  │    (Compose + MVI)   │─────▶│    (Fastify + TS)     │──────┐           │
│  │                       │ HTTP │                       │      │           │
│  │  - AI Chat (via API) │◀─────│  - Service Registry  │      │           │
│  │  - OCR (ML Kit)     │      │  - REST Routes        │      │           │
│  │  - QR Scanning      │      └───────────────────────┘      │           │
│  └──────────────────────┘                                     │           │
│                                                                  │           │
│                              ┌──────────────────────────────────┘           │
│                              │                                              │
│          ┌──────────────────┼──────────────────┐                         │
│          │                  │                  │                           │
│          ▼                  ▼                  ▼                           │
│  ┌─────────────┐   ┌─────────────┐   ┌─────────────────┐                  │
│  │ PostgreSQL  │   │ Ollama AI   │   │ External APIs   │                  │
│  │ (fintech_db)│   │ (qwen3:8b) │   │ (Market data)   │                  │
│  └─────────────┘   └─────────────┘   └─────────────────┘                  │
└─────────────────────────────────────────────────────────────────────────────┘
```

## Project Structure

```
personal-finance-app/
├── android-client/
│   └── app/src/main/java/com/fintech/
│       ├── ui/                  # Compose UI screens
│       │   ├── home/
│       │   ├── transaction/
│       │   ├── account/
│       │   ├── budget/
│       │   ├── fund/
│       │   ├── investment/
│       │   ├── market/
│       │   ├── ai/
│       │   ├── ocr/
│       │   ├── qr/
│       │   ├── settings/
│       │   └── auth/
│       ├── data/
│       │   ├── remote/
│       │   │   ├── api/         # Retrofit + OkHttp (ApiClient)
│       │   │   ├── ai/          # Ollama direct calls (unused)
│       │   │   └── services/     # API service interfaces
│       │   ├── local/
│       │   │   ├── dao/          # Room DAOs
│       │   │   ├── entity/       # Room entities
│       │   │   ├── datastore/    # Preferences DataStore
│       │   │   └── ocr/          # ML Kit OCR
│       │   └── repository/       # Repository pattern
│       └── di/                   # Hilt modules
│
├── backend/src/
│   ├── index.ts                 # Fastify entry point
│   ├── config/                  # Env config loader
│   ├── utils/
│   │   └── db.ts                # PostgreSQL connection pool
│   ├── services/               # Business logic services
│   │   ├── base/               # BaseService abstract class
│   │   ├── ServiceRegistry.ts  # Service lifecycle manager
│   │   ├── ai/                 # Ollama AI integration
│   │   ├── banking/            # VietQR + simulated banks
│   │   ├── market/             # Market data (CoinGecko, iTick)
│   │   ├── notification/        # FCM, Email, SMS (stubs)
│   │   ├── sync/               # Cloud sync (stubs)
│   │   └── investment/         # Portfolio tracking
│   └── routes/                 # Fastify route handlers
│       ├── auth.ts             # /auth/*
│       ├── user.ts             # /user/*
│       ├── account.ts          # /accounts/*
│       ├── transaction.ts      # /transactions/*
│       ├── category.ts         # /categories/*
│       ├── fund.ts             # /funds/*
│       ├── budget.ts           # /budgets/*
│       ├── savingsGoal.ts      # /savings-goals/*
│       ├── investment.ts       # /investments/*
│       ├── bank.ts             # /banks/*
│       ├── qr.ts               # /qr/*
│       ├── market.ts           # /market/*
│       ├── ai.ts               # /ai/*
│       ├── report.ts           # /reports/*
│       ├── demo.ts             # /demo/*
│       ├── services.ts         # /services/*
│       └── index.ts            # Route registration
│
├── docs/
│   ├── SYSTEM_ARCHITECTURE.md  # This file
│   └── THIRD_PARTY_SERVICES.md # External integrations reference
│
├── .env                        # Root env (Android reference)
├── .env.example                # Env template
└── backend/.env               # Backend runtime env
```

## Component Stack

### Android Client

| Component | Technology | Purpose |
|-----------|-----------|---------|
| UI Framework | Jetpack Compose + Material 3 | Declarative UI |
| State Management | MVI Pattern | Unidirectional data flow |
| Dependency Injection | Hilt | Constructor injection |
| Network | Retrofit + OkHttp | REST API calls to backend |
| Local Database | Room | Offline-first caching |
| Preferences | DataStore | Key-value user preferences |
| Camera | CameraX + ML Kit | QR scanning, OCR |
| Image Loading | Coil | Async image rendering |
| Barcode | ZXing | QR code generation |
| Serialization | Kotlinx Serialization | JSON parsing |

### Backend API

| Component | Technology | Purpose |
|-----------|-----------|---------|
| Runtime | Node.js 20+ | Server runtime |
| Framework | Fastify | HTTP server + plugins |
| Language | TypeScript | Type safety |
| Database | PostgreSQL 16+ | Primary data store |
| DB Driver | pg (raw SQL) | Connection pooling |
| Authentication | JWT (fastify-jwt) | Stateless auth |
| Validation | Zod | Request schema validation |
| Logging | Pino | Structured JSON logs |
| External HTTP | Axios | Third-party API calls |

## API Endpoints (Fastify Routes)

### Authentication -- `/api/v1/auth`
```
POST   /auth/register          Register new user (email, password, name)
POST   /auth/login             Login (returns JWT access + refresh tokens)
POST   /auth/refresh            Refresh access token
POST   /auth/logout            Invalidate refresh token
```

### User -- `/api/v1/user`
```
GET    /user/me                Get current user profile
PUT    /user/me                Update profile (name, phone, avatar)
```

### Accounts -- `/api/v1/accounts`
```
GET    /accounts               List user's accounts (cash, bank, e-wallet, credit)
GET    /accounts/summary       Net worth summary across all accounts
POST   /accounts               Create account
GET    /accounts/:id           Get account details
DELETE /accounts/:id           Delete account
```

### Transactions -- `/api/v1/transactions`
```
GET    /transactions           List with filters (account, category, date range, type)
POST   /transactions           Create transaction (income/expense/transfer)
DELETE /transactions/:id        Delete transaction
```

### Categories -- `/api/v1/categories`
```
GET    /categories             List categories (system + custom)
POST   /categories             Create custom category
```

### Funds (Savings Goals) -- `/api/v1/funds`
```
GET    /funds                  List savings funds
POST   /funds                  Create fund
GET    /funds/:id              Get fund details
POST   /funds/:id/contribute   Add money to fund
DELETE /funds/:id              Delete fund
POST   /funds/ai-allocate      AI-suggested allocation based on income
POST   /funds/execute-allocation  Execute AI allocation
```

### Budgets -- `/api/v1/budgets`
```
GET    /budgets                List budgets
POST   /budgets                Create budget (monthly limit per category)
```

### Savings Goals -- `/api/v1/savings-goals`
```
GET    /savings-goals          List savings goals
POST   /savings-goals           Create goal
PUT    /savings-goals/:id       Update goal
DELETE /savings-goals/:id      Delete goal
POST   /savings-goals/:id/contribute  Add contribution
```

### Investments -- `/api/v1/investments`
```
GET    /investments/portfolio          Get portfolio summary
POST   /investments                    Add investment (stock, crypto, bond, etc.)
PUT    /investments/:id/price          Update current price
DELETE /investments/:id                Remove investment
GET    /investments/recommendations     Get AI recommendations
GET    /investments/risk-profile       Calculate risk profile
GET    /investments/performance        Portfolio performance over time
```

### Banks -- `/api/v1/banks`
```
GET    /banks                            List supported banks (10 simulated Vietnamese banks)
GET    /banks/accounts                   List user's connected bank accounts
POST   /banks/connect                     Connect simulated bank account
POST   /banks/transfer                   Internal transfer between accounts
```

### QR Codes -- `/api/v1/qr`
```
POST   /qr/generate               Generate VietQR payload + image URL
POST   /qr/generate-transfer       Generate transfer QR with amount
POST   /qr/validate                Validate VietQR payload
POST   /qr/process                 Process incoming VietQR payment
```

### Market Data -- `/api/v1/market`
```
GET    /market                    All market data (exchange, gold, stocks, crypto)
GET    /market/exchange-rates     USD, EUR, JPY, GBP... vs VND (simulated)
GET    /market/gold-prices        SJC gold prices (simulated)
GET    /market/stock-indices       VN-Index, HNX, UPCOM (iTick API or simulated)
GET    /market/crypto-prices       BTC, ETH, BNB, SOL, XRP (CoinGecko live)
GET    /market/convert            Currency conversion
```

### AI -- `/api/v1/ai`
```
GET    /ai/status                 Ollama health check + available models
POST   /ai/chat                   Send chat message to AI (qwen3:8b)
GET    /ai/chat/history           Get chat history
POST   /ai/chat/log               Save chat message to DB
POST   /ai/analyze-invoice        Parse OCR result with AI analysis
POST   /ai/suggest-allocation      AI fund allocation suggestions
PUT    /ai/model                  Change active AI model
GET    /ai/models                 List available Ollama models
```

### Reports -- `/api/v1/reports`
```
GET    /reports/summary            Monthly income/expense/savings summary
GET    /reports/income-expense     Income vs expense breakdown by category
GET    /reports/trend              Spending trend over time
```

### Services -- `/api/v1/services`
```
GET    /services                   All services health status
GET    /services/ai               AI service status + available models
GET    /services/banking          Banking service status
GET    /services/notification      Notification service status
GET    /services/sync              Sync service status
POST   /services/sync/trigger      Manual sync trigger
POST   /services/notification/test  Test notification channel
```

### Demo -- `/api/v1/demo`
```
GET    /demo/scenarios             Pre-built demo scenarios
POST   /demo/create               Create demo data
POST   /demo/quick-start          Create quick-start demo
GET    /demo/stats                Demo data statistics
```

## Database Schema

```
USERS
├── id (UUID, PK)
├── email (UNIQUE)
├── password_hash
├── full_name
├── phone
├── avatar_url
├── is_active
├── is_verified
├── created_at
└── updated_at

ACCOUNTS
├── id (UUID, PK)
├── user_id (FK → USERS)
├── name
├── type (cash | bank | e_wallet | credit_card)
├── balance
├── currency (VND default)
├── icon
├── color
├── is_active
├── created_at
└── updated_at

TRANSACTIONS
├── id (UUID, PK)
├── user_id (FK → USERS)
├── account_id (FK → ACCOUNTS)
├── category_id (FK → CATEGORIES)
├── type (income | expense | transfer)
├── amount
├── description
├── date
├── is_recurring
├── recurring_id (FK → RECURRING, nullable)
├── created_at
└── updated_at

CATEGORIES
├── id (UUID, PK)
├── user_id (FK → USERS, nullable for system categories)
├── name
├── icon
├── color
├── type (income | expense)
├── is_system
└── updated_at

FUNDS (Savings Goals)
├── id (UUID, PK)
├── user_id (FK → USERS)
├── name
├── target_amount
├── current_amount
├── icon
├── color
├── deadline
├── created_at
└── updated_at

BUDGETS
├── id (UUID, PK)
├── user_id (FK → USERS)
├── category_id (FK → CATEGORIES)
├── amount
├── spent_amount
├── period (monthly)
├── start_date
└── end_date

SAVINGS_GOALS
├── id (UUID, PK)
├── user_id (FK → USERS)
├── name
├── target_amount
├── current_amount
├── deadline
├── status (active | completed | cancelled)
├── created_at
└── updated_at

INVESTMENTS
├── id (UUID, PK)
├── user_id (FK → USERS)
├── symbol
├── name
├── type (stock | crypto | bond | fund | other)
├── quantity
├── purchase_price
├── current_price
├── purchase_date
├── notes
├── created_at
└── updated_at

SIMULATED_BANKS
├── id (UUID, PK)
├── code
├── name
├── short_name
├── vietqr_prefix
├── swift_code
├── logo_url
└── is_active

USER_BANK_ACCOUNTS
├── id (UUID, PK)
├── user_id (FK → USERS)
├── bank_id (FK → SIMULATED_BANKS)
├── account_number
├── account_holder_name
├── balance
├── created_at
└── updated_at

AI_CHAT_LOGS
├── id (UUID, PK)
├── user_id (FK → USERS)
├── message
├── response
├── model_used
├── tokens_used
├── created_at
└── updated_at
```

## External Services Reference

See `THIRD_PARTY_SERVICES.md` for full details. Summary:

| Service | Status | Auth | Purpose |
|---------|--------|------|---------|
| Ollama | ACTIVE | None | AI chat, invoice analysis, fund allocation |
| CoinGecko | ACTIVE | None | Live crypto prices |
| iTick | ACTIVE (needs token) | Token | Vietnam stock indices |
| QRServer | ACTIVE | None | QR code image generation |
| Firebase FCM | NOT_CONFIGURED | Key | Push notifications |
| Email | STUB | API Key | Email sending (not implemented) |
| SMS | STUB | API Key | SMS sending (not implemented) |
| Google Drive | STUB | Credentials | Cloud backup (not implemented) |
| iCloud | STUB | None | Cloud backup (not implemented) |
| Google ML Kit | ACTIVE | None | On-device OCR |

## Data Flow

```
User Action
    │
    ▼
Android Compose UI
    │
    ▼
ViewModel (MVI State)
    │
    ▼
Repository
    ├──► Room DB (offline cache, write-through)
    │
    └──► Retrofit → Backend API (HTTP/REST)
               │
               ▼
          Fastify Routes
               │
               ├──► Service Registry
               │        │
               │        ├──► AIService → Ollama (AI)
               │        ├──► BankingService → VietQR (banking)
               │        ├──► MarketService → CoinGecko/iTick (market)
               │        ├──► NotificationService → FCM (notifications)
               │        ├──► SyncService → Local/Cloud (sync)
               │        └──► InvestmentService → DB (investments)
               │
               └──► PostgreSQL (fintech_db)
```

## Authentication Flow

```
1. User registers / logs in
2. Backend validates credentials
3. Returns access token (15min) + refresh token (7 days)
4. Android stores tokens in DataStore
5. Every API request includes "Authorization: Bearer <access_token>"
6. Fastify JWT middleware validates on every protected route
7. On 401: Android calls /auth/refresh to get new access token
8. On logout: refresh token invalidated
```

## Service Health & Initialization

All services extend `BaseService` and are managed by `ServiceRegistry`:

```
Server startup
    │
    ▼
ServiceRegistry.initializeAll()
    │
    ├──► AIService.initialize()     → ping Ollama, cache model list
    ├──► BankingService.initialize()→ load simulated banks from DB
    ├──► MarketService.initialize() → set cache duration
    ├──► NotificationService.init() → check FCM/Email/SMS keys
    ├──► SyncService.initialize()  → set provider (LOCAL/GOOGLE_DRIVE/ICLOUD)
    └──► InvestmentService.init()   → ready for portfolio queries

GET /api/v1/services
    │
    └──► Returns health status of all services
             (status, latencyMs, message, lastChecked)
```

## Security

| Layer | Mechanism |
|-------|-----------|
| Transport | HTTPS (production), HTTP (local dev) |
| Auth | JWT access + refresh token pattern |
| API | Rate limiting (100 req/min per IP) |
| CORS | Configured origins (ngrok, localhost, emulator) |
| Database | Parameterized SQL queries (pg) |
| Input | Zod schema validation on all requests |
| Passwords | bcrypt hashing |

## Environment Configuration

```
backend/.env                     # Backend runtime (gitignored)
  ├── DATABASE_URL              # PostgreSQL connection
  ├── JWT_SECRET                # Token signing
  ├── OLLAMA_URL / MODEL        # AI server
  ├── ITICK_API_TOKEN           # Stock data (optional)
  ├── FCM_SERVER_KEY            # Push notifications (optional)
  ├── EMAIL_API_KEY             # Email sending (stub)
  ├── SMS_API_KEY               # SMS sending (stub)
  └── SYNC_PROVIDER             # local | google_drive | icloud

.env (root)                     # Android reference + template
.env.example                    # New developer template
```
