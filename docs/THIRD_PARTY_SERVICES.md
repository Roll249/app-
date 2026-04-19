# Third-Party Services Configuration

This document describes every external service integration in the personal-finance-app.
Status legend: `ACTIVE` = real API calls, `STUB` = no-op (logs only), `INACTIVE` = defined but never called, `NOT_CONFIGURED` = missing credentials.

---

## Quick Reference

| # | Service | Purpose | Status | Auth Required |
|---|---------|---------|--------|--------------|
| 1 | Ollama | AI chat, invoice OCR, fund allocation | ACTIVE | None |
| 2 | CoinGecko | Crypto prices (BTC, ETH, etc.) | ACTIVE | None |
| 3 | iTick | Vietnam stock indices (VN-Index, HNX) | ACTIVE | `ITICK_API_TOKEN` |
| 4 | QRServer / VietQR | QR code image generation | ACTIVE | None |
| 5 | Firebase FCM | Push notifications (Android) | NOT_CONFIGURED | `FCM_SERVER_KEY` |
| 6 | Email (SendGrid/Mailgun) | Email notifications | STUB | `EMAIL_API_KEY` |
| 7 | SMS (Twilio/VNPT) | SMS notifications | STUB | `SMS_API_KEY` |
| 8 | Google Drive | Cloud backup/sync | STUB | Credentials file |
| 9 | iCloud | Cloud backup/sync (Apple) | STUB | None |
| 10 | Google ML Kit | On-device OCR for receipts | ACTIVE | None |
| 11 | PostgreSQL | Primary database | ACTIVE | Password |
| 12 | exchangerate-api.com | Foreign exchange rates | INACTIVE | `MARKET_API_KEY` |
| 13 | Polygon.io | US stock prices | INACTIVE | `STOCK_API_KEY` |

---

## Environment File Priority

This project has TWO env files with different purposes:

| File | Purpose |
|------|---------|
| `.env` (root) | Android app config, .env.example template |
| `backend/.env` | Backend server runtime config |

**Critical rule:** The backend ONLY reads `backend/.env`. Tokens in the root `.env` are NOT available to the backend.

---

## Service Details

### 1. Ollama (AI/LLM) -- ACTIVE

**Purpose:** Local AI inference for:
- Chat with financial AI assistant
- Invoice/receipt OCR parsing (AI analysis)
- Smart fund allocation suggestions
- Transaction categorization

**Backend config** (`backend/.env`):
```
OLLAMA_URL=https://research.neu.edu.vn/ollama
OLLAMA_MODEL=qwen3:8b
```

**Android config** (`android-client/app/build.gradle.kts`): Hardcoded baseUrl.
See "Android Ollama Integration" section below for details.

**API endpoints called:**
- `GET {OLLAMA_URL}/api/tags` -- list available models
- `POST {OLLAMA_URL}/api/generate` -- text completion (backend)
- `POST {OLLAMA_URL}/api/chat` -- chat completion (Android direct)

**Error handling:** Backend returns HTTP 503 when Ollama unavailable. Android catches exceptions silently.

**Files:**
- `backend/src/services/ai/AIService.ts`
- `backend/src/routes/ai.ts`
- `android-client/.../data/remote/ai/AIService.kt`

---

### 2. CoinGecko (Cryptocurrency Prices) -- ACTIVE

**Purpose:** Real-time cryptocurrency price data for the Market screen.

**Config:** None required (free tier, no auth).

**API endpoint called:**
```
GET https://api.coingecko.com/api/v3/simple/price
  ?ids=bitcoin,ethereum,binancecoin,tether,solana,ripple
  &vs_currencies=usd
  &include_24hr_change=true
```

**Currencies tracked:** BTC, ETH, BNB, SOL, XRP, USDT.

**Cache duration:** 5 minutes (controlled by `MARKET_CACHE_DURATION`).

**Fallback:** If API fails, returns simulated data with realistic-looking values.

**Files:**
- `backend/src/services/market/MarketService.ts` (lines 354-455)

---

### 3. iTick (Vietnam Stock Market Indices) -- ACTIVE

**Purpose:** Real-time Vietnamese stock market index data (VN-Index, HNX-Index, UPCOM).

**Config** (`backend/.env`):
```
ITICK_API_TOKEN=<your-token-from-itick.org>
```

Register at https://itick.org/en (free tier available, no credit card required).

**API endpoint called:**
```
GET https://api.itick.org/stock/quote?region=VN&code=VNINDEX,HNX,UPCOM
Headers: { "token": "<ITICK_API_TOKEN>" }
```

**Fallback:** If token is missing or API fails, returns simulated time-based data with small random variations around realistic base values.

**Files:**
- `backend/src/services/market/MarketService.ts` (lines 286-343)

---

### 4. QRServer / VietQR -- ACTIVE

**Purpose:** Generate QR code images for bank transfers and payment requests.

**Config:** None required (free tier, no auth).

**API endpoint called:**
```
GET https://api.qrserver.com/v1/create-qr-code/
  ?size=300x300
  &data=<vietqr-payload-base64>
```

**Timeout:** 10 seconds.
**Retry:** None (current implementation).

**Files:**
- `backend/src/services/banking/BankingService.ts` (line 319)

---

### 5. Firebase Cloud Messaging (Push Notifications) -- NOT_CONFIGURED

**Purpose:** Send push notifications to Android devices.

**Config** (`backend/.env`):
```
FCM_SERVER_KEY=<your-firebase-server-key>
```

**API endpoint called:**
```
POST https://fcm.googleapis.com/fcm/send
Headers: { "Authorization": "key=<FCM_SERVER_KEY>" }
```

**Status:** `FCM_SERVER_KEY` is empty. Notifications are silently skipped when not configured.

**Files:**
- `backend/src/services/notification/NotificationService.ts` (lines 160-198)

---

### 6. Email Service -- STUB

**Purpose:** Send transactional emails (password reset, transaction alerts, etc.).

**Config** (`backend/.env`):
```
EMAIL_API_KEY=<your-sendgrid-or-mailgun-key>
EMAIL_FROM=noreply@fintech.app
```

**Recommended providers:**
- **SendGrid** (https://sendgrid.com) -- free tier: 100 emails/day
- **Mailgun** (https://mailgun.com) -- free tier: 5,000 emails/month
- **Vietguys** (https://vietguys.vn) -- for Vietnam SMTP

**Status:** `sendEmail()` in NotificationService.ts is a STUB -- it logs the email content but does NOT actually send it. The method returns success without making any HTTP call.

**Files:**
- `backend/src/services/notification/NotificationService.ts` (lines 201-208)

**To implement:** Replace the stub with actual SendGrid/Mailgun HTTP API call.

---

### 7. SMS Service -- STUB

**Purpose:** Send SMS notifications (OTPs, alerts, etc.).

**Config** (`backend/.env`):
```
SMS_API_KEY=<your-twilio-or-vnpt-key>
SMS_FROM=+1234567890
```

**Recommended providers:**
- **Twilio** (https://twilio.com) -- international SMS
- **VNPT** (https://vnmpt.vn) -- Vietnam SMS gateway
- **Esms** (https://esms.vn) -- Vietnam bulk SMS

**Status:** `sendSMS()` in NotificationService.ts is a STUB -- it logs the message but does NOT actually send it.

**Files:**
- `backend/src/services/notification/NotificationService.ts` (lines 211-218)

**To implement:** Replace the stub with actual SMS gateway API call.

---

### 8. Google Drive Sync -- STUB

**Purpose:** Cloud backup of financial data for cross-device access.

**Config** (`backend/.env`):
```
SYNC_PROVIDER=local   # Change to "google_drive" to enable
GOOGLE_DRIVE_CREDENTIALS_PATH=./config/google-drive-credentials.json
```

**Status:** `syncToGoogleDrive()` is a STUB -- it logs the sync intent but does NOT actually upload anything to Google Drive.

**Files:**
- `backend/src/services/sync/SyncService.ts` (lines 261-264)

**To implement:** Requires Google Cloud Console project with Drive API enabled and OAuth2 service account credentials.

---

### 9. iCloud Sync -- STUB

**Purpose:** Cloud backup for Apple device users.

**Config** (`backend/.env`):
```
SYNC_PROVIDER=local   # Change to "icloud" to enable
```

**Status:** `syncToICloud()` is a STUB -- it logs the sync intent but does NOT actually use iCloud.

**Files:**
- `backend/src/services/sync/SyncService.ts` (lines 266-269)

**To implement:** Requires Apple Developer Program membership and CloudKit API integration.

---

### 10. Google ML Kit (OCR) -- ACTIVE

**Purpose:** On-device OCR for scanning receipts and invoices on Android.

**Config:** None required (runs locally on device).

**Status:** Fully functional. Falls back to regex-based parsing if ML Kit fails.

**Files:**
- `android-client/.../data/local/ocr/OCRService.kt`

---

### 11. PostgreSQL -- ACTIVE

**Purpose:** Primary relational database for all application data.

**Config** (`backend/.env`):
```
DATABASE_URL=postgres://postgres:postgres@localhost:5432/fintech_db
POSTGRES_PASSWORD=postgres123
POSTGRES_PORT=5432
```

**Status:** Working. Data includes transactions, accounts, budgets, funds, investment portfolios.

**Files:**
- `backend/src/utils/db.ts`
- `backend/src/services/{banking,ai,sync}/`

---

### 12. exchangerate-api.com (Exchange Rates) -- INACTIVE

**Purpose:** Real-time foreign exchange rate data.

**Config** (`backend/.env`):
```
MARKET_API_URL=https://api.exchangerate-api.com
MARKET_API_KEY=50a54b77a95cfac7ceac7297
```

**Status:** NOT CALLED. The `getExchangeRates()` method in MarketService.ts always returns simulated data. `MARKET_API_KEY` is defined but never used.

**Fallback:** Uses realistic simulated exchange rates for USD, EUR, JPY, GBP, SGD, AUD, CAD, CHF, HKD, CNY (based on approximate VND rates).

**Files:**
- `backend/src/services/market/MarketService.ts` (lines 138-219)

**To use real data:** Replace the simulated data block with an axios call to exchangerate-api.com using the configured key.

---

### 13. Polygon.io (US Stocks) -- INACTIVE

**Purpose:** Real-time US stock market prices for investment tracking.

**Config** (`backend/.env`):
```
STOCK_API_URL=https://api.polygon.io
STOCK_API_KEY=ETAUJkd505_lWiTRezOiP_ybX4BzRvRT
```

**Status:** NOT CALLED. The InvestmentService reads portfolio data from the local PostgreSQL database only. `STOCK_API_KEY` is defined but never used.

**Files:**
- `backend/src/services/investment/InvestmentService.ts`

**To use real data:** Add Polygon.io API calls to fetch current prices for tracked symbols.

---

## Android Ollama Integration

The Android app calls Ollama directly (bypassing the backend) for AI features.

**Config** (`android-client/app/build.gradle.kts`):
```kotlin
// OLLAMA_BASE_URL is hardcoded here
buildConfigField("String", "OLLAMA_BASE_URL", "\"http://10.0.2.2:11434\"")
```

**Issue:** Android uses `http://10.0.2.2:11434` (Android emulator localhost mapping), while backend uses `https://research.neu.edu.vn/ollama`. If the user wants Android to use the same Ollama server as the backend, the `OLLAMA_BASE_URL` should match.

For physical device on same network: use the server's local IP address (e.g., `http://192.168.1.x:11434`).

**Files:**
- `android-client/.../data/remote/ai/AIService.kt` (lines 170-191)

---

## Configuration Checklist

### Required for production

- [ ] `ITICK_API_TOKEN` -- add to `backend/.env` for real stock data
- [ ] `FCM_SERVER_KEY` -- add to `backend/.env` for push notifications
- [ ] `OLLAMA_URL` in Android -- update to match backend's Ollama server
- [ ] Strong `JWT_SECRET` and `JWT_REFRESH_SECRET` in both env files

### Optional enhancements

- [ ] `EMAIL_API_KEY` -- implement SendGrid/Mailgun in NotificationService
- [ ] `SMS_API_KEY` -- implement Twilio/VNPT in NotificationService
- [ ] `SYNC_PROVIDER=google_drive` -- implement Google Drive backup
- [ ] `SYNC_PROVIDER=icloud` -- implement iCloud backup
- [ ] `MARKET_API_KEY` -- implement real exchange rate fetching
- [ ] `STOCK_API_KEY` -- implement real US stock price fetching

### Clean up (if not needed)

- [ ] Remove `MARKET_API_KEY` / `MARKET_API_URL` if not using exchange rate API
- [ ] Remove `STOCK_API_KEY` / `STOCK_API_URL` if not using Polygon.io
- [ ] Remove stub sync methods if cloud sync is not planned
