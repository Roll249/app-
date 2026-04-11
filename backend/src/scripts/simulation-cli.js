/**
 * CLI Tool để trigger simulation events và kiểm tra dữ liệu
 * Usage: node scripts/simulation-cli.js <command> [args]
 * 
 * Commands:
 *   stats      - Xem thống kê database
 *   scenarios  - Liệt kê các kịch bản có sẵn
 *   create    - Tạo demo user với kịch bản
 *   events    - Xem các sự kiện gần đây
 *   trigger   - Kích hoạt sự kiện (salary/income/expense/transfer)
 *   seed      - Tạo nhiều demo users
 */

require('dotenv/config');
const { query, queryOne, execute } = require('../utils/db');
const { SCENARIOS } = require('../utils/simulation');

const args = process.argv.slice(2);
const command = args[0] || 'help';

async function showStats() {
  console.log('\n=== DATABASE STATS ===\n');
  
  const users = await query('SELECT COUNT(*) as count FROM users');
  console.log(`Users: ${users[0].count}`);
  
  const accounts = await query('SELECT COUNT(*) as count FROM accounts');
  console.log(`Accounts: ${accounts[0].count}`);
  
  const transactions = await query('SELECT type, COUNT(*) as count, COALESCE(SUM(amount), 0) as total FROM transactions GROUP BY type');
  transactions.forEach(t => console.log(`${t.type}: ${t.count} transactions, ${formatCurrency(t.total)} VNĐ`));
  
  const funds = await query('SELECT COUNT(*) as count FROM funds WHERE is_active = true');
  console.log(`Active Funds: ${funds[0].count}`);
  
  const banks = await query('SELECT COUNT(*) as count FROM simulated_banks');
  console.log(`Banks: ${banks[0].count}`);
  
  const demoUsers = await query('SELECT COUNT(*) as count FROM demo_users');
  console.log(`Demo Users: ${demoUsers[0].count}`);
}

async function listScenarios() {
  console.log('\n=== AVAILABLE SCENARIOS ===\n');
  Object.entries(SCENARIOS).forEach(([key, s]) => {
    console.log(`${key}: ${s.name}`);
    console.log(`  - ${s.description}`);
    console.log(`  - Income: ${formatCurrency(s.monthlyIncome)} VNĐ/tháng`);
    console.log(`  - Banks: ${s.bankAccounts.length}, Funds: ${s.funds.length}, Budgets: ${s.budgets.length}`);
    console.log();
  });
}

async function createDemo(scenarioKey = 'office_worker', name = 'CLI Demo User') {
  console.log(`\n=== CREATING DEMO USER: ${scenarioKey} ===\n`);
  
  const bcrypt = require('bcryptjs');
  const { v4: uuidv4 } = require('uuid');
  const now = Math.floor(Date.now() / 1000);
  
  const email = `cli_${scenarioKey}_${now}@fintech.cli`;
  const passwordHash = await bcrypt.hash('Cli123!@#', 12);
  const userId = uuidv4();
  
  await execute(
    `INSERT INTO users (id, email, password_hash, full_name, is_active, is_verified, created_at, updated_at)
     VALUES ($1, $2, $3, $4, true, true, $5, $5)`,
    [userId, email, passwordHash, name, now]
  );
  
  const accountId = uuidv4();
  await execute(
    `INSERT INTO accounts (id, user_id, name, type, initial_balance, current_balance, currency, created_at, updated_at)
     VALUES ($1, $2, 'Ví CLI', 'WALLET', 5000000, 5000000, 'VND', $3, $3)`,
    [accountId, userId, now]
  );
  
  const scenario = SCENARIOS[scenarioKey] || SCENARIOS.office_worker;
  
  // Tạo bank accounts
  for (const bankAcc of scenario.bankAccounts) {
    const bankAccountId = uuidv4();
    const bank = await queryOne('SELECT id FROM simulated_banks WHERE code = $1', [bankAcc.bankCode]);
    if (bank) {
      await execute(
        `INSERT INTO user_bank_accounts (id, user_id, bank_id, account_number, account_holder_name, balance, is_active, linked_at)
         VALUES ($1, $2, $3, $4, $5, $6, true, $7)`,
        [bankAccountId, userId, bank.id, bankAcc.accountNumber, name, bankAcc.balance, now]
      );
    }
  }
  
  // Tạo funds
  for (const fund of scenario.funds) {
    const fundId = uuidv4();
    const progress = fund.currentAmount > 0 && fund.targetAmount > 0
      ? Math.round((fund.currentAmount / fund.targetAmount) * 10000) / 100
      : 0;
    await execute(
      `INSERT INTO funds (id, user_id, name, icon, color, target_amount, current_amount, progress, is_active, created_at, updated_at)
       VALUES ($1, $2, $3, $4, $5, $6, $7, $8, true, $9, $9)`,
      [fundId, userId, fund.name, fund.icon, fund.color, fund.targetAmount, fund.currentAmount, progress, now]
    );
  }
  
  // Tạo transactions mẫu
  await createSampleTransactions(userId, accountId, scenario);
  
  console.log(`✓ Created user: ${email}`);
  console.log(`✓ User ID: ${userId}`);
  console.log(`✓ Account ID: ${accountId}`);
  console.log(`✓ Password: Cli123!@#`);
}

async function createSampleTransactions(userId, accountId, scenario) {
  const { v4: uuidv4 } = require('uuid');
  const now = Math.floor(Date.now() / 1000);
  const categoryMap = {};
  const categories = await query('SELECT id, name FROM categories WHERE is_system = true');
  for (const cat of categories) categoryMap[cat.name] = cat.id;
  
  const sampleTransactions = [
    { type: 'INCOME', categoryName: 'Lương', amount: scenario.monthlyIncome, description: 'Lương tháng', daysAgo: 5 },
    { type: 'INCOME', categoryName: 'Thưởng', amount: 2000000, description: 'Thưởng hiệu suất', daysAgo: 3 },
    { type: 'EXPENSE', categoryName: 'Ăn uống', amount: 150000, description: 'Cơm trưa văn phòng', daysAgo: 4 },
    { type: 'EXPENSE', categoryName: 'Di lại', amount: 50000, description: 'Grab đi làm', daysAgo: 3 },
    { type: 'EXPENSE', categoryName: 'Mua sắm', amount: 500000, description: 'Quần áo mùa hè', daysAgo: 2 },
    { type: 'EXPENSE', categoryName: 'Giải trí', amount: 200000, description: 'Vé xem phim', daysAgo: 1 },
    { type: 'EXPENSE', categoryName: 'Hóa đơn', amount: 350000, description: 'Tiền điện sinh hoạt', daysAgo: 10 },
    { type: 'EXPENSE', categoryName: 'Chi tiêu khác', amount: 1000000, description: 'Chuyển vào quỹ tiết kiệm', daysAgo: 7 },
  ];
  
  for (const txn of sampleTransactions) {
    const categoryId = categoryMap[txn.categoryName] || null;
    const timestamp = Math.floor((Date.now() - txn.daysAgo * 24 * 60 * 60 * 1000) / 1000);
    await execute(
      `INSERT INTO transactions (id, user_id, account_id, category_id, type, amount, currency, description, date, source_type, reference_id, created_at, updated_at)
       VALUES ($1, $2, $3, $4, $5, $6, 'VND', $7, $8, 'CLI', $9, $10, $10)`,
      [uuidv4(), userId, accountId, categoryId, txn.type, txn.amount, txn.description, timestamp, `CLI_${uuidv4()}`, now]
    );
  }
  console.log(`✓ Created ${sampleTransactions.length} sample transactions`);
}

async function showRecentEvents(userId = null) {
  console.log('\n=== RECENT TRANSACTION EVENTS ===\n');
  
  let query_str = `
    SELECT t.id, t.type, t.amount, t.description, t.date, t.source_type,
           u.full_name as user_name, c.name as category_name
    FROM transactions t
    LEFT JOIN users u ON t.user_id = u.id
    LEFT JOIN categories c ON t.category_id = c.id
  `;
  const params = [];
  if (userId) {
    query_str += ' WHERE t.user_id = $1';
    params.push(userId);
  }
  query_str += ' ORDER BY t.date DESC LIMIT 20';
  
  const transactions = await query(query_str, params);
  
  if (transactions.length === 0) {
    console.log('No transactions found');
    return;
  }
  
  transactions.forEach(t => {
    const date = new Date(t.date * 1000);
    const icon = t.type === 'INCOME' ? '↑' : '↓';
    console.log(`${icon} [${date.toLocaleDateString('vi-VN')}] ${formatCurrency(t.amount)} - ${t.description} (${t.category_name || 'N/A'})`);
    console.log(`   Source: ${t.source_type}, User: ${t.user_name || 'N/A'}`);
    console.log();
  });
}

async function triggerEvent(eventType, amount = null, description = null) {
  console.log(`\n=== TRIGGERING EVENT: ${eventType} ===\n`);
  
  const users = await query('SELECT id FROM users LIMIT 1');
  if (users.length === 0) {
    console.log('No users in database. Create a demo user first.');
    return;
  }
  const userId = users[0].id;
  const account = await queryOne('SELECT id FROM accounts WHERE user_id = $1 LIMIT 1', [userId]);
  if (!account) {
    console.log('No account found for user');
    return;
  }
  
  const { v4: uuidv4 } = require('uuid');
  const now = Math.floor(Date.now() / 1000);
  const categoryMap = {};
  const categories = await query('SELECT id, name FROM categories WHERE is_system = true');
  for (const cat of categories) categoryMap[cat.name] = cat.id;
  
  const eventDescriptions = {
    salary: { type: 'INCOME', category: 'Lương', amount: amount || 15000000, desc: description || 'Lương tháng' },
    income: { type: 'INCOME', category: 'Thu nhập khác', amount: amount || 500000, desc: description || 'Thu nhập phụ' },
    expense: { type: 'EXPENSE', category: 'Chi tiêu khác', amount: amount || 100000, desc: description || 'Chi tiêu hàng ngày' },
    transfer_in: { type: 'INCOME', category: 'Chuyển khoản đến', amount: amount || 1000000, desc: description || 'Nhận tiền từ người thân' },
    transfer_out: { type: 'EXPENSE', category: 'Chuyển khoản đi', amount: amount || 500000, desc: description || 'Chuyển tiền cho người thân' },
  };
  
  const event = eventDescriptions[eventType] || eventDescriptions.expense;
  
  await execute(
    `INSERT INTO transactions (id, user_id, account_id, category_id, type, amount, currency, description, date, source_type, reference_id, created_at, updated_at)
     VALUES ($1, $2, $3, $4, $5, $6, 'VND', $7, $8, 'CLI_EVENT', $9, $10, $10)`,
    [uuidv4(), userId, account.id, categoryMap[event.category] || null, event.type, event.amount, event.desc, now, `CLI_${uuidv4()}`, now]
  );
  
  console.log(`✓ Created ${event.type} event:`);
  console.log(`  - Amount: ${formatCurrency(event.amount)} VNĐ`);
  console.log(`  - Category: ${event.category}`);
  console.log(`  - Description: ${event.desc}`);
  console.log(`  - Timestamp: ${new Date(now * 1000).toISOString()}`);
}

function formatCurrency(amount) {
  return new Intl.NumberFormat('vi-VN').format(amount);
}

async function main() {
  console.log('\n--- FINTECH SIMULATION CLI ---\n');
  console.log('Available commands:');
  console.log('  stats      - Xem thống kê database');
  console.log('  scenarios  - Liệt kê các kịch bản có sẵn');
  console.log('  create     - Tạo demo user với kịch bản');
  console.log('  events     - Xem các sự kiện gần đây');
  console.log('  trigger    - Kích hoạt sự kiện (salary/income/expense/transfer_in/transfer_out)');
  console.log('  seed       - Tạo nhiều demo users');
  console.log();
  
  switch (command) {
    case 'stats':
      await showStats();
      break;
    case 'scenarios':
      await listScenarios();
      break;
    case 'create':
      await createDemo(args[1] || 'office_worker', args[2] || 'CLI Demo User');
      break;
    case 'events':
      await showRecentEvents(args[1]);
      break;
    case 'trigger':
      await triggerEvent(args[1] || 'expense', args[2] ? parseInt(args[2]) : null, args[3]);
      break;
    case 'seed':
      const count = parseInt(args[1]) || 3;
      console.log(`Creating ${count} demo users...`);
      for (let i = 0; i < count; i++) {
        const scenarios = Object.keys(SCENARIOS);
        await createDemo(scenarios[i % scenarios.length], `Seed User ${i + 1}`);
      }
      break;
    default:
      console.log(`Unknown command: ${command}`);
      console.log('Run without arguments to see help');
  }
}

main().catch(console.error);
