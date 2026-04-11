# API Documentation

Base URL: `https://acrobat-equate-emphasize.ngrok-free.dev/api/v1/`

## Authentication

### Register
```
POST /auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "Password123",
  "fullName": "Nguyen Van A",
  "phone": "0912345678"
}
```

Response:
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
    "user": {
      "id": "uuid",
      "email": "user@example.com",
      "fullName": "Nguyen Van A"
    }
  }
}
```

### Login
```
POST /auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "Password123"
}
```

### Refresh Token
```
POST /auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzI1NiIs..."
}
```

## Accounts

### Get Accounts
```
GET /accounts
Authorization: Bearer <token>
```

### Create Account
```
POST /accounts
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Tiền mặt",
  "type": "CASH",
  "icon": "account_balance_wallet",
  "color": "#4CAF50",
  "initialBalance": "5000000",
  "includeInTotal": true
}
```

## Transactions

### Get Transactions
```
GET /transactions?page=1&pageSize=20
Authorization: Bearer <token>
```

### Create Transaction
```
POST /transactions
Authorization: Bearer <token>
Content-Type: application/json

{
  "accountId": "uuid",
  "categoryId": "uuid",
  "type": "EXPENSE",
  "amount": "150000",
  "description": "Mua sắm",
  "date": "1712800000000"
}
```

## Banks

### Get All Banks
```
GET /banks
```

### Connect Bank Account
```
POST /banks/connect
Authorization: Bearer <token>
Content-Type: application/json

{
  "bankId": "uuid",
  "accountNumber": "1234567890",
  "accountHolderName": "NGUYEN VAN A"
}
```

### Transfer
```
POST /banks/transfer
Authorization: Bearer <token>
Content-Type: application/json

{
  "fromBankAccountId": "uuid",
  "toAccountNumber": "0987654321",
  "toBankCode": "TPB",
  "amount": "1000000",
  "description": "Chuyển tiền"
}
```

## QR Codes

### Generate Receive QR
```
POST /qr/generate-receive
Authorization: Bearer <token>
Content-Type: application/json

{
  "amount": "500000",
  "message": "Thanh toán"
}
```

## Reports

### Get Summary
```
GET /reports/summary
Authorization: Bearer <token>
```

### Get Income/Expense
```
GET /reports/income-expense?period=MONTHLY
Authorization: Bearer <token>
```

## Error Response

```json
{
  "success": false,
  "message": "Error description",
  "error": {
    "code": "ERROR_CODE",
    "message": "Detailed error message"
  }
}
```

## Status Codes

- `200 OK` - Success
- `201 Created` - Resource created
- `400 Bad Request` - Invalid input
- `401 Unauthorized` - Authentication required
- `403 Forbidden` - Access denied
- `404 Not Found` - Resource not found
- `429 Too Many Requests` - Rate limit exceeded
- `500 Internal Server Error` - Server error
