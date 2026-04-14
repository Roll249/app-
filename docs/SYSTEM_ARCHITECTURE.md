# Personal Finance App V2 - System Architecture

## Overview

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           PERSONAL FINANCE APP V2                           │
└─────────────────────────────────────────────────────────────────────────────┘

┌──────────────────────┐     ┌──────────────────────┐     ┌──────────────────┐
│    📱 ANDROID APP     │     │   ⚙️ BACKEND API    │     │  🗄️ DATABASE    │
│                      │     │                      │     │                  │
│  ┌────────────────┐  │     │  ┌────────────────┐  │     │  ┌────────────┐ │
│  │  UI Layer      │  │     │  │  Fastify       │  │     │  │ PostgreSQL │ │
│  │  (Compose)     │──┼────▶│  │  Server        │  │     │  └────────────┘ │
│  └────────────────┘  │ HTTP│  └───────┬────────┘  │     │                  │
│  ┌────────────────┐  │     │          │           │     │  Tables:        │
│  │  ViewModel     │  │     │  ┌───────▼────────┐ │     │  - users        │
│  │  (MVI)         │  │◀────│  │ Service        │ │     │  - accounts     │
│  └────────────────┘  │     │  │ Registry       │ │     │  - transactions │
│  ┌────────────────┐  │     │  └───────┬────────┘ │     │  - categories   │
│  │  Repository    │  │     │          │           │     │  - funds        │
│  │                │  │     │  ┌───────▼────────┐ │     │  - budgets      │
│  └────────────────┘  │     │  │ Routes Layer   │ │     │  - ai_chat_logs │
│  ┌────────────────┐  │     │  │                │ │     │  - simulated_   │
│  │  Data Sources  │  │     │  │ - /auth        │ │     │    banks        │
│  │  - Retrofit    │  │     │  │ - /accounts    │ │     │                │
│  │  - Room DB     │  │     │  │ - /transactions│ │     └────────────────┘
│  │  - DataStore   │  │     │  │ - /ai          │
│  └────────────────┘  │     │  │ - /market      │
└──────────────────────┘     │  │ - /services    │◀──────── External APIs
                             │  │ - /investments │
                             │  └───────┬────────┘
                             │          │
                             │  ┌───────▼────────┐
                             │  │ Middleware     │
                             │  │ - JWT Auth     │
                             │  │ - CORS         │
                             │  │ - Rate Limit   │
                             │  └────────────────┘
                             └──────────────────────┘
                                      │
                    ┌─────────────────┼─────────────────┐
                    │                 │                 │
              ┌─────▼─────┐   ┌──────▼──────┐  ┌──────▼──────┐
              │ 🤖 AI     │   │ 🏦 Banking  │  │ 📊 Market   │
              │ Service   │   │ Service     │  │ Service     │
              ├───────────┤   ├────────────┤  ├────────────┤
              │ Ollama    │   │ VietQR API │  │ Exchangerate│
              │ qwen3:8b  │   │ 10 Banks   │  │ Polygon.io  │
              └───────────┘   └────────────┘  └────────────┘
                    │                 │                 │
              ┌─────▼─────┐   ┌──────▼──────┐  ┌──────▼──────┐
              │ 💰 Invest │   │ 🔔 Notif.   │  │ ☁️ Sync     │
              │ Service   │   │ Service     │  │ Service     │
              ├───────────┤   ├────────────┤  ├────────────┤
              │ Stock API │   │ Firebase   │  │ Google     │
              │ Polygon   │   │ SendGrid   │  │ Drive      │
              └───────────┘   │ Twilio     │  └────────────┘
                              └────────────┘
```

## Component Details

### 📱 Android Client

| Component | Technology |
|-----------|------------|
| UI Framework | Jetpack Compose + Material 3 |
| State Management | MVI Pattern |
| Dependency Injection | Hilt |
| Network | Retrofit + OkHttp |
| Local Database | Room |
| Preferences | DataStore |
| Camera | CameraX + ML Kit |
| Image Loading | Coil |

### ⚙️ Backend API

| Component | Technology |
|-----------|------------|
| Runtime | Node.js 20+ |
| Framework | Fastify |
| Language | TypeScript |
| Database | PostgreSQL 16+ |
| ORM | pg (raw SQL) |
| Authentication | JWT |
| Validation | Zod |
| Logging | Pino |

### 🔧 External Services

| Service | Provider | Purpose |
|---------|----------|---------|
| AI Chat | Ollama (qwen3:8b) | Financial assistant |
| Banking | VietQR API | QR payments, transfers |
| Market Data | Exchangerate-api | Exchange rates |
| Market Data | Polygon.io | Stock prices |
| Notifications | Firebase FCM | Push notifications |
| Notifications | SendGrid | Email |
| Notifications | Twilio | SMS |
| Cloud Sync | Google Drive | Backup |

## API Endpoints

### Authentication
```
POST /api/v1/auth/register    - Register new user
POST /api/v1/auth/login       - Login
POST /api/v1/auth/refresh      - Refresh token
POST /api/v1/auth/logout      - Logout
```

### Core Features
```
GET  /api/v1/accounts         - List accounts
POST /api/v1/accounts         - Create account
GET  /api/v1/transactions     - List transactions
POST /api/v1/transactions     - Create transaction
GET  /api/v1/categories       - List categories
GET  /api/v1/funds            - List funds
POST /api/v1/funds            - Create fund
GET  /api/v1/budgets          - List budgets
POST /api/v1/budgets          - Create budget
```

### Banking
```
GET  /api/v1/banks            - List banks
POST /api/v1/banks/connect     - Connect bank account
POST /api/v1/banks/transfer    - Transfer money
POST /api/v1/qr/generate      - Generate QR code
POST /api/v1/qr/process       - Process QR payment
```

### AI Chatbot
```
GET  /api/v1/ai/status        - Check AI service status
POST /api/v1/ai/chat          - Send chat message
POST /api/v1/ai/analyze-invoice - Analyze invoice OCR
GET  /api/v1/ai/chat/history - Get chat history
```

### Services
```
GET  /api/v1/services         - All services status
GET  /api/v1/services/ai      - AI service status
GET  /api/v1/services/banking - Banking service status
GET  /api/v1/services/sync    - Sync service status
POST /api/v1/services/sync/trigger - Trigger sync
```

### Market Data
```
GET  /api/v1/market           - All market data
GET  /api/v1/market/exchange-rates - Exchange rates
GET  /api/v1/market/gold-prices    - Gold prices
GET  /api/v1/market/stock-indices  - Stock indices
GET  /api/v1/market/crypto-prices  - Crypto prices
```

## Database Schema

```
┌─────────────────┐
│     USERS       │
├─────────────────┤
│ id (UUID)       │
│ email           │
│ password_hash   │
│ full_name       │
│ phone           │
│ avatar_url     │
│ is_active       │
│ is_verified     │
│ created_at      │
│ updated_at      │
└─────────────────┘
        │
        │ 1:N
        ▼
┌─────────────────┐     ┌─────────────────┐
│    ACCOUNTS     │     │  TRANSACTIONS   │
├─────────────────┤     ├─────────────────┤
│ id (UUID)       │────▶│ id (UUID)       │
│ user_id (FK)    │     │ user_id (FK)    │
│ name            │     │ account_id (FK) │
│ type            │     │ category_id (FK) │
│ balance         │     │ type            │
│ currency        │     │ amount          │
│ icon            │     │ description     │
│ color           │     │ date            │
└─────────────────┘     └─────────────────┘

┌─────────────────┐
│   CATEGORIES    │
├─────────────────┤
│ id (UUID)       │
│ name            │
│ icon            │
│ color           │
│ type            │
│ is_system       │
│ user_id (FK)    │
└─────────────────┘
        │
        │ 1:N
        ▼
┌─────────────────┐     ┌─────────────────┐
│     FUNDS       │     │    BUDGETS      │
├─────────────────┤     ├─────────────────┤
│ id (UUID)       │     │ id (UUID)       │
│ user_id (FK)    │     │ user_id (FK)    │
│ name            │     │ category_id(FK)  │
│ target_amount   │     │ amount          │
│ current_amount  │     │ spent_amount    │
│ icon            │     │ period          │
│ color           │     │ start_date      │
└─────────────────┘     │ end_date        │
                        └─────────────────┘

┌─────────────────┐
│ SIMULATED_BANKS │
├─────────────────┤
│ id (UUID)       │
│ code            │
│ name            │
│ short_name      │
│ vietqr_prefix   │
│ swift_code      │
│ logo_url        │
│ is_active       │
└─────────────────┘
```

## Data Flow

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              USER ACTION                                     │
│                    (Tap, Swipe, Input, etc.)                                │
└────────────────────────────────────┬────────────────────────────────────────┘
                                     │
                                     ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                          ANDROID UI LAYER                                   │
│                         (Jetpack Compose)                                    │
└────────────────────────────────────┬────────────────────────────────────────┘
                                     │
                                     ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                         VIEWMODEL (MVI)                                     │
│                   State Management + Business Logic                          │
└────────────────────────────────────┬────────────────────────────────────────┘
                                     │
                                     ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                           REPOSITORY                                        │
│                    Data Abstraction Layer                                   │
└─────────────────────┬───────────────────────────────────────────────────────┘
                      │
        ┌─────────────┴─────────────┐
        │                           │
        ▼                           ▼
┌───────────────────┐     ┌───────────────────┐
│   LOCAL SOURCE    │     │  REMOTE SOURCE    │
│   (Room + Cache)  │     │   (Retrofit)     │
└───────────────────┘     └─────────┬─────────┘
                                    │
                                    ▼ HTTP/REST
                        ┌───────────────────────────┐
                        │      BACKEND API         │
                        │      (Fastify)          │
                        └───────────┬───────────────┘
                                    │
              ┌─────────────────────┼─────────────────────┐
              │                     │                     │
              ▼                     ▼                     ▼
    ┌─────────────────┐   ┌─────────────────┐   ┌─────────────────┐
    │   Routes        │   │   Services      │   │   Middleware    │
    │   (Controllers) │   │   (Business)    │   │   (JWT, CORS)   │
    └────────┬────────┘   └────────┬────────┘   └─────────────────┘
             │                     │
             │                     ▼
             │           ┌─────────────────┐
             │           │   External      │
             │           │   Services      │
             │           │   (AI, Banking, │
             │           │    Market...)   │
             │           └────────┬────────┘
             │                    │
             └──────────┬─────────┘
                        │
                        ▼
              ┌─────────────────────────┐
              │      PostgreSQL        │
              │      Database          │
              └─────────────────────────┘
```

## Service Registry Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                          SERVICE REGISTRY                                    │
│                     (Central Service Manager)                               │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│   ┌─────────────────────────────────────────────────────────────────────┐   │
│   │                        AISERVICE                                   │   │
│   │  ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐             │   │
│   │  │ Ollama  │  │  Chat   │  │  OCR    │  │ Allocat │             │   │
│   │  │ Client  │  │ Handler │  │ Invoice │  │ion Algo │             │   │
│   │  └─────────┘  └─────────┘  └─────────┘  └─────────┘             │   │
│   └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
│   ┌─────────────────────────────────────────────────────────────────────┐   │
│   │                      BANKINGSERVICE                                  │   │
│   │  ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐             │   │
│   │  │ VietQR  │  │  Bank   │  │  QR     │  │Transfer │             │   │
│   │  │ API     │  │ Manager │  │ Gen     │  │ Handler │             │   │
│   │  └─────────┘  └─────────┘  └─────────┘  └─────────┘             │   │
│   └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
│   ┌─────────────────────────────────────────────────────────────────────┐   │
│   │                     MARKETSERVICE                                    │   │
│   │  ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐             │   │
│   │  │Exchange │  │  Gold   │  │ Stock   │  │ Crypto  │             │   │
│   │  │ Rates   │  │ Prices  │  │ Indices │  │ Prices  │             │   │
│   │  └─────────┘  └─────────┘  └─────────┘  └─────────┘             │   │
│   └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
│   ┌─────────────────────────────────────────────────────────────────────┐   │
│   │                   NOTIFICATIONSERVICE                               │   │
│   │  ┌─────────┐  ┌─────────┐  ┌─────────┐                           │   │
│   │  │Firebase │  │SendGrid │  │ Twilio  │                           │   │
│   │  │  FCM    │  │ (Email) │  │  (SMS)  │                           │   │
│   │  └─────────┘  └─────────┘  └─────────┘                           │   │
│   └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
│   ┌─────────────────────────────────────────────────────────────────────┐   │
│   │                        SYNCSERVICE                                  │   │
│   │  ┌─────────┐  ┌─────────┐  ┌─────────┐                           │   │
│   │  │ Google  │  │  Local  │  │Conflict │                           │   │
│   │  │ Drive   │  │ Storage │  │Resolver │                           │   │
│   │  └─────────┘  └─────────┘  └─────────┘                           │   │
│   └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
│   ┌─────────────────────────────────────────────────────────────────────┐   │
│   │                     INVESTMENTSERVICE                                │   │
│   │  ┌─────────┐  ┌─────────┐  ┌─────────┐                           │   │
│   │  │Portfolio│  │  Stock  │  │  Risk   │                           │   │
│   │  │ Tracker │  │   API   │  │Analyzer │                           │   │
│   │  └─────────┘  └─────────┘  └─────────┘                           │   │
│   └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

## Deployment Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                            PRODUCTION                                       │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│   ┌───────────────────────────────────────────────────────────────────┐   │
│   │                        CLOUD PROVIDER                               │   │
│   │  ┌─────────────────┐         ┌─────────────────┐                 │   │
│   │  │   PostgreSQL    │         │    Backend      │                 │   │
│   │  │    (RDS)       │         │    (Docker)    │                 │   │
│   │  │   Port: 5432   │◀───────▶│   Port: 3000   │                 │   │
│   │  └─────────────────┘   SQL   └────────┬────────┘                 │   │
│   │                                      │                            │   │
│   │                                      │ HTTP                       │   │
│   │                                      ▼                            │   │
│   │  ┌─────────────────────────────────────────────────────────────┐ │   │
│   │  │                    API GATEWAY / LOAD BALANCER              │ │   │
│   │  └─────────────────────────────────────────────────────────────┘ │   │
│   │                                      │                            │   │
│   └──────────────────────────────────────┼────────────────────────────┘   │
│                                          │                                  │
└──────────────────────────────────────────┼──────────────────────────────────┘
                                           │
                          ┌────────────────┼────────────────┐
                          │                │                │
                          ▼                ▼                ▼
                    ┌───────────┐    ┌───────────┐    ┌───────────┐
                    │  Android │    │   iOS     │    │   Web     │
                    │   App    │    │   App     │    │  Client   │
                    └───────────┘    └───────────┘    └───────────┘
```

## Development Setup

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                          LOCAL DEVELOPMENT                                  │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│   ┌───────────────────────────────────────────────────────────────────┐   │
│   │                      DOCKER COMPOSE                                 │   │
│   │  ┌─────────────────┐         ┌─────────────────┐                 │   │
│   │  │   PostgreSQL    │         │    Backend      │                 │   │
│   │  │   Container     │◀───────▶│    Container    │                 │   │
│   │  │   Port: 5432   │   SQL   │    Port: 3000   │                 │   │
│   │  └─────────────────┘         └─────────────────┘                 │   │
│   │                                                                     │   │
│   │  Optional:                                                        │   │
│   │  ┌─────────────────┐                                              │   │
│   │  │    pgAdmin      │  (Database Management)                        │   │
│   │  │   Port: 5050   │                                              │   │
│   │  └─────────────────┘                                              │   │
│   └───────────────────────────────────────────────────────────────────┘   │
│                                                                             │
│   ┌───────────────────────────────────────────────────────────────────┐   │
│   │                        HOST MACHINE                                │   │
│   │  ┌─────────────────┐         ┌─────────────────┐                 │   │
│   │  │     Ollama     │         │   Android      │                 │   │
│   │  │  (AI Engine)   │         │   Emulator     │                 │   │
│   │  │  Port: 11434  │         │   Port: 5555   │                 │   │
│   │  └─────────────────┘         └─────────────────┘                 │   │
│   └───────────────────────────────────────────────────────────────────┘   │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

## Security

| Layer | Protection |
|-------|------------|
| Transport | HTTPS/TLS |
| Authentication | JWT (Access + Refresh tokens) |
| Authorization | RBAC (Role-Based Access Control) |
| API | Rate Limiting |
| Database | SQL Parameterized Queries |
| Input | Zod Schema Validation |
| CORS | Configured Origins Only |
