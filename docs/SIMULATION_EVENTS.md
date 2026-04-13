# Hướng Dẫn Tạo Sự Kiện Giả Lập (Simulation Events)

## Tổng Quan

Hệ thống hỗ trợ tạo các sự kiện giả lập để test và demo ứng dụng. Có 3 cách để tạo sự kiện:

1. **CLI Tool** - Dùng command line
2. **API Endpoint** - Gọi HTTP API
3. **Auto-Simulation** - Tự động chạy theo lịch

---

## 1. CLI Tool

### Cài đặt và chạy

```bash
cd backend
node src/scripts/simulation-cli.js <command> [args]
```

### Các Commands

#### Xem thống kê database
```bash
node src/scripts/simulation-cli.js stats
```
Output:
```
=== DATABASE STATS ===

Users: 5
Accounts: 12
INCOME: 45 transactions, 125,000,000 VNĐ
EXPENSE: 156 transactions, 89,500,000 VNĐ
Active Funds: 15
Banks: 10
Demo Users: 5
```

#### Liệt kê các kịch bản có sẵn
```bash
node src/scripts/simulation-cli.js scenarios
```
Output:
```
=== AVAILABLE SCENARIOS ===

office_worker: Người đi làm công ty
  - Lương cứng 15 triệu/tháng, chi tiêu ổn định, tiết kiệm đều đặn
  - Income: 15,000,000 VNĐ/tháng
  - Banks: 3, Funds: 3, Budgets: 4

freelancer: Freelancer
  - Thu nhập bất ổn, có tháng nhiều có tháng ít, chi tiêu linh hoạt
  - Income: 18,000,000 VNĐ/tháng
  - Banks: 2, Funds: 3, Budgets: 3

tech_lead: Kỹ sư công nghệ cao cấp
  - Thu nhập 45 triệu/tháng, có đầu tư, chi tiêu có kế hoạch
  - Income: 45,000,000 VNĐ/tháng
  - Banks: 4, Funds: 4, Budgets: 5
```

#### Tạo demo user với kịch bản
```bash
# Tạo user với kịch bản mặc định (office_worker)
node src/scripts/simulation-cli.js create

# Tạo user với kịch bản cụ thể
node src/scripts/simulation-cli.js create freelancer "Nguyễn Văn Freelance"

# Tạo user với kịch bản tech_lead
node src/scripts/simulation-cli.js create tech_lead "Trần Kỹ Sư"
```

#### Xem các sự kiện gần đây
```bash
# Xem tất cả sự kiện
node src/scripts/simulation-cli.js events

# Xem sự kiện của user cụ thể
node src/scripts/simulation-cli.js events <user_id>
```

#### Kích hoạt sự kiện đơn lẻ
```bash
# Lương
node src/scripts/simulation-cli.js trigger salary 15000000

# Thu nhập khác
node src/scripts/simulation-cli.js trigger income 500000

# Chi tiêu
node src/scripts/simulation-cli.js trigger expense 100000 "Mua cơm"

# Chuyển khoản đến
node src/scripts/simulation-cli.js trigger transfer_in 2000000

# Chuyển khoản đi
node src/scripts/simulation-cli.js trigger transfer_out 500000
```

#### Tạo nhiều demo users
```bash
# Tạo 5 demo users
node src/scripts/simulation-cli.js seed 5

# Tạo 10 demo users
node src/scripts/simulation-cli.js seed 10
```

---

## 2. API Endpoints

### Create Demo User

**POST** `/api/v1/simulation/demo`

Request:
```json
{
  "scenario": "office_worker",
  "fullName": "Test User",
  "salaryDay": 5
}
```

Scenarios có sẵn:
- `office_worker` - Người đi làm công ty (15 triệu/tháng)
- `freelancer` - Freelancer (18 triệu/tháng)
- `tech_lead` - Kỹ sư cao cấp (45 triệu/tháng)

Response:
```json
{
  "success": true,
  "data": {
    "userId": "uuid-here",
    "accountId": "uuid-here",
    "email": "demo_office_worker_123@fintech.demo",
    "password": "Demo123!@#"
  }
}
```

### Get Scenarios

**GET** `/api/v1/simulation/scenarios`

Response:
```json
{
  "success": true,
  "data": [
    {
      "key": "office_worker",
      "name": "Người đi làm công ty",
      "description": "Lương cứng 15 triệu/tháng",
      "monthlyIncome": 15000000,
      "salaryDay": 5,
      "bankAccountCount": 3,
      "fundCount": 3,
      "budgetCount": 4
    }
  ]
}
```

### Trigger Event

**POST** `/api/v1/simulation/trigger`

Request:
```json
{
  "eventType": "salary",
  "amount": 15000000,
  "description": "Lương tháng"
}
```

Event types:
- `salary` - Lương (mặc định 15 triệu)
- `income` - Thu nhập khác (mặc định 500k)
- `expense` - Chi tiêu (mặc định 100k)
- `transfer_in` - Chuyển khoản đến (mặc định 1 triệu)
- `transfer_out` - Chuyển khoản đi (mặc định 500k)

### Get Simulation Stats

**GET** `/api/v1/simulation/stats`

Response:
```json
{
  "success": true,
  "data": {
    "totalUsers": 10,
    "totalTransactions": 201,
    "totalIncome": 156000000,
    "totalExpense": 89000000,
    "activeFunds": 15,
    "demoUsers": 5
  }
}
```

---

## 3. Auto-Simulation (Webhook)

### Setup Auto-Salary

Để tự động trigger lương hàng tháng, setup webhook với cron job:

```bash
# Tạo cron job chạy vào ngày 5 hàng tháng lúc 8h sáng
0 8 5 * * curl -X POST https://your-api.com/api/v1/simulation/trigger \
  -H "Content-Type: application/json" \
  -d '{"eventType": "salary", "amount": 15000000}'
```

### Scheduled Events

| Sự kiện | Ngày | Mô tả |
|---------|------|-------|
| Lương | 5 hàng tháng | Lương tháng |
| Thưởng | 10-15 hàng tháng | Thưởng hiệu suất |
| Tiền điện | 10-15 hàng tháng | Hóa đơn điện |
| Tiền nước | 20-25 hàng tháng | Hóa đơn nước |
| Internet | 25-28 hàng tháng | Internet FPT |

---

## 4. Các Kịch Bản Chi Tiết

### Scenario: office_worker

```yaml
name: "Người đi làm công ty"
monthlyIncome: 15,000,000 VND
salaryDay: 5

bankAccounts:
  - VCB: 8,500,000 VND
  - TPB: 3,000,000 VND
  - MB: 500,000 VND

funds:
  - Quỹ du lịch Nhật Bản: 18.5/50 triệu
  - Quỹ mua xe máy: 12/35 triệu
  - Quỹ khẩn cấp: 22/30 triệu

budgets:
  - Ăn uống: 4 triệu/tháng
  - Di lại: 1.5 triệu/tháng
  - Mua sắm: 2 triệu/tháng
  - Giải trí: 1 triệu/tháng
```

### Scenario: freelancer

```yaml
name: "Freelancer"
monthlyIncome: 18,000,000 VND (trung bình)
salaryDay: 1

bankAccounts:
  - ACB: 12,000,000 VND
  - VPB: 4,500,000 VND

funds:
  - Quỹ máy MacBook Pro: 32/80 triệu
  - Quỹ du lịch châu Âu: 15/100 triệu
  - Quỹ khẩn cấp: 18/50 triệu

budgets:
  - Ăn uống: 5 triệu/tháng
  - Mua sắm: 3 triệu/tháng
  - Giải trí: 2 triệu/tháng
```

### Scenario: tech_lead

```yaml
name: "Kỹ sư công nghệ cao cấp"
monthlyIncome: 45,000,000 VND
salaryDay: 10

bankAccounts:
  - VCB: 25,000,000 VND
  - MSB: 15,000,000 VND
  - VIB: 8,000,000 VND
  - TPB: 2,000,000 VND

funds:
  - Quỹ mua nhà: 180/1000 triệu
  - Quỹ du lịch Nhật Bản: 35/50 triệu
  - Quỹ đầu tư cổ phiếu: 85/200 triệu
  - Quỹ khẩn cấp: 100/100 triệu (đạt mục tiêu)

budgets:
  - Ăn uống: 8 triệu/tháng
  - Di lại: 3 triệu/tháng
  - Mua sắm: 5 triệu/tháng
  - Giải trí: 3 triệu/tháng
  - Hóa đơn: 2 triệu/tháng
```

---

## 5. Mẫu Transactions Được Tạo

### Ngày lương (ngày 5, 10, hoặc 1 tùy scenario)

| Loại | Số tiền | Mô tả |
|------|---------|-------|
| INCOME | Lương tháng | Lương tháng (15-45 triệu) |
| INCOME | 1.5-2.25 triệu | Thưởng hiệu suất (10-15% lương) |

### Chi tiêu hàng ngày (20-40% chance mỗi ngày)

| Danh mục | Số tiền | Mô tả |
|----------|---------|-------|
| Ăn uống | 50k-300k | Cơm trưa, cà phê, trà sữa |
| Di lại | 20k-150k | Grab, xăng xe |
| Mua sắm | 100k-2M | Quần áo, đồ gia dụng |
| Giải trí | 50k-500k | Phim, Netflix, game |
| Hóa đơn | 150k-800k | Điện, nước, internet |

### Các sự kiện định kỳ

| Ngày | Sự kiện | Số tiền |
|------|---------|---------|
| 15-20 | Tiền điện | 280k-500k |
| 20-25 | Tiền nước | 80k-200k |
| 25-28 | Internet | 250k-400k |
| 28+ | Chuyển quỹ | 500k-2M |

---

## 6. Debug và Troubleshooting

### Kiểm tra logs

```bash
# Xem logs simulation
tail -f logs/app.log | grep -i simulation

# Xem tất cả events gần đây
node src/scripts/simulation-cli.js events
```

### Reset demo data

```bash
# Xóa tất cả demo users và tạo lại
psql -U postgres -d fintech < scripts/reset-demo.sql

# Hoặc xóa thủ công
DELETE FROM transactions WHERE source_type IN ('SIMULATION', 'CLI', 'CLI_EVENT');
DELETE FROM demo_users;
```

### Kiểm tra kết nối Ollama

```bash
curl http://localhost:11434/api/tags
```

---

## 7. Ví Dụ Sử Dụng Trong Testing

### Tạo test environment

```bash
# 1. Tạo 5 demo users
node src/scripts/simulation-cli.js seed 5

# 2. Trigger một số events
node src/scripts/simulation-cli.js trigger salary 15000000
node src/scripts/simulation-cli.js trigger expense 150000
node src/scripts/simulation-cli.js trigger expense 50000

# 3. Kiểm tra dữ liệu
node src/scripts/simulation-cli.js events

# 4. Xem stats
node src/scripts/simulation-cli.js stats
```

### Test AI Chat

1. Tạo demo user
2. Login vào app
3. Mở AI Chat Fina
4. Hỏi về chi tiêu hoặc chia quỹ
5. Kiểm tra response từ Ollama

---

## 8. Environment Variables

```bash
# Backend .env
OLLAMA_BASE_URL=http://localhost:11434
OLLAMA_MODEL=qwen3:8b

# Simulation settings
AUTO_SALARY_ENABLED=true
AUTO_SALARY_DAY=5
```

---

## Liên Hệ và Hỗ Trợ

- **Bug reports**: Tạo issue trên GitHub
- **Feature requests**: Liên hệ qua email
- **Documentation**: Xem `/docs` folder