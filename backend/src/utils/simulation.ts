import { query, queryOne, execute } from '../utils/db.js';

export interface SimulationScenario {
  name: string;
  description: string;
  monthlyIncome: number;
  salaryDay: number;
  bankAccounts: { bankCode: string; balance: number; accountNumber: string }[];
  funds: { name: string; targetAmount: number; currentAmount: number; icon: string; color: string }[];
  budgets: { name: string; amount: number; categoryName: string; period: string }[];
}

export const SCENARIOS: Record<string, SimulationScenario> = {
  office_worker: {
    name: 'Người đi làm công ty',
    description: 'Lương cứng 15 triệu/tháng, chi tiêu ổn định, tiết kiệm đều đặn',
    monthlyIncome: 15000000,
    salaryDay: 5,
    bankAccounts: [
      { bankCode: 'VCB', balance: 8500000, accountNumber: '1234567890' },
      { bankCode: 'TPB', balance: 3000000, accountNumber: '2009876543' },
      { bankCode: 'MB', balance: 500000, accountNumber: '3501122334' },
    ],
    funds: [
      { name: 'Quỹ du lịch Nhật Bản', targetAmount: 50000000, currentAmount: 18500000, icon: 'flight', color: '#E91E63' },
      { name: 'Quỹ mua xe máy mới', targetAmount: 35000000, currentAmount: 12000000, icon: 'directions_car', color: '#3F51B5' },
      { name: 'Quỹ khẩn cấp', targetAmount: 30000000, currentAmount: 22000000, icon: 'security', color: '#009688' },
    ],
    budgets: [
      { name: 'Ăn uống', amount: 4000000, categoryName: 'Ăn uống', period: 'MONTHLY' },
      { name: 'Di lại', amount: 1500000, categoryName: 'Di lại', period: 'MONTHLY' },
      { name: 'Mua sắm', amount: 2000000, categoryName: 'Mua sắm', period: 'MONTHLY' },
      { name: 'Giải trí', amount: 1000000, categoryName: 'Giải trí', period: 'MONTHLY' },
    ],
  },
  freelancer: {
    name: 'Freelancer',
    description: 'Thu nhập bất ổn, có tháng nhiều có tháng ít, chi tiêu linh hoạt',
    monthlyIncome: 18000000,
    salaryDay: 1,
    bankAccounts: [
      { bankCode: 'ACB', balance: 12000000, accountNumber: '1188223344' },
      { bankCode: 'VPB', balance: 4500000, accountNumber: '2299001122' },
    ],
    funds: [
      { name: 'Quỹ máy MacBook Pro', targetAmount: 80000000, currentAmount: 32000000, icon: 'laptop_mac', color: '#607D8B' },
      { name: 'Quỹ du lịch châu Âu', targetAmount: 100000000, currentAmount: 15000000, icon: 'flight', color: '#E91E63' },
      { name: 'Quỹ khẩn cấp', targetAmount: 50000000, currentAmount: 18000000, icon: 'security', color: '#009688' },
    ],
    budgets: [
      { name: 'Ăn uống', amount: 5000000, categoryName: 'Ăn uống', period: 'MONTHLY' },
      { name: 'Mua sắm', amount: 3000000, categoryName: 'Mua sắm', period: 'MONTHLY' },
      { name: 'Giải trí', amount: 2000000, categoryName: 'Giải trí', period: 'MONTHLY' },
    ],
  },
  tech_lead: {
    name: 'Kỹ sư công nghệ cao cấp',
    description: 'Thu nhập 45 triệu/tháng, có đầu tư, chi tiêu có kế hoạch',
    monthlyIncome: 45000000,
    salaryDay: 10,
    bankAccounts: [
      { bankCode: 'VCB', balance: 25000000, accountNumber: '9988776655' },
      { bankCode: 'MSB', balance: 15000000, accountNumber: '7766554433' },
      { bankCode: 'VIB', balance: 8000000, accountNumber: '5544332211' },
      { bankCode: 'TPB', balance: 2000000, accountNumber: '3322110099' },
    ],
    funds: [
      { name: 'Quỹ mua nhà', targetAmount: 1000000000, currentAmount: 180000000, icon: 'home', color: '#FF5722' },
      { name: 'Quỹ du lịch Nhật Bản', targetAmount: 50000000, currentAmount: 35000000, icon: 'flight', color: '#E91E63' },
      { name: 'Quỹ đầu tư cổ phiếu', targetAmount: 200000000, currentAmount: 85000000, icon: 'trending_up', color: '#4CAF50' },
      { name: 'Quỹ khẩn cấp', targetAmount: 100000000, currentAmount: 100000000, icon: 'security', color: '#009688' },
    ],
    budgets: [
      { name: 'Ăn uống', amount: 8000000, categoryName: 'Ăn uống', period: 'MONTHLY' },
      { name: 'Di lại', amount: 3000000, categoryName: 'Di lại', period: 'MONTHLY' },
      { name: 'Mua sắm', amount: 5000000, categoryName: 'Mua sắm', period: 'MONTHLY' },
      { name: 'Giải trí', amount: 3000000, categoryName: 'Giải trí', period: 'MONTHLY' },
      { name: 'Hóa đơn', amount: 2000000, categoryName: 'Hóa đơn', period: 'MONTHLY' },
    ],
  },
};

function generateRandomTransactions(scenario: SimulationScenario, daysBack: number = 30): { type: string; categoryName: string; amount: number; description: string; daysAgo: number }[] {
  const transactions: { type: string; categoryName: string; amount: number; description: string; daysAgo: number }[] = [];

  for (let day = 0; day <= daysBack; day++) {
    const date = new Date(Date.now() - day * 24 * 60 * 60 * 1000);
    const dayOfMonth = date.getDate();

    if (dayOfMonth === scenario.salaryDay) {
      transactions.push({
        type: 'INCOME',
        categoryName: 'Lương',
        amount: scenario.monthlyIncome,
        description: `Lương tháng ${date.toLocaleDateString('vi-VN', { month: 'long', year: 'numeric' })}`,
        daysAgo: day,
      });

      const thuong = scenario.monthlyIncome * (0.1 + Math.random() * 0.15);
      transactions.push({
        type: 'INCOME',
        categoryName: 'Thưởng',
        amount: Math.round(thuong / 1000) * 1000,
        description: 'Thưởng hiệu suất',
        daysAgo: day + Math.floor(Math.random() * 3),
      });
    }

    if (dayOfMonth >= 1 && dayOfMonth <= 28 && Math.random() < 0.4) {
      const categories: { name: string; min: number; max: number }[] = [
        { name: 'Ăn uống', min: 50000, max: 300000 },
        { name: 'Di lại', min: 20000, max: 150000 },
        { name: 'Mua sắm', min: 100000, max: 2000000 },
        { name: 'Giải trí', min: 50000, max: 500000 },
        { name: 'Hóa đơn', min: 150000, max: 800000 },
        { name: 'Sức khỏe', min: 100000, max: 1500000 },
        { name: 'Giáo dục', min: 200000, max: 3000000 },
      ];

      const cat = categories[Math.floor(Math.random() * categories.length)];
      const amount = Math.round((cat.min + Math.random() * (cat.max - cat.min)) / 1000) * 1000;

      transactions.push({
        type: 'EXPENSE',
        categoryName: cat.name,
        amount,
        description: getRealisticExpenseDescription(cat.name),
        daysAgo: day,
      });
    }

    if (Math.random() < 0.08) {
      const groceryAmount = Math.round((150000 + Math.random() * 350000) / 1000) * 1000;
      transactions.push({
        type: 'EXPENSE',
        categoryName: 'Ăn uống',
        amount: groceryAmount,
        description: 'Mua thực phẩm tại siêu thị',
        daysAgo: day,
      });
    }

    if (dayOfMonth >= 15 && Math.random() < 0.15) {
      const electricBill = Math.round((280000 + Math.random() * 220000) / 1000) * 1000;
      transactions.push({
        type: 'EXPENSE',
        categoryName: 'Hóa đơn',
        amount: electricBill,
        description: 'Tiền điện sinh hoạt',
        daysAgo: day,
      });
    }

    if (dayOfMonth === 20 && Math.random() < 0.8) {
      const waterBill = Math.round((80000 + Math.random() * 120000) / 1000) * 1000;
      transactions.push({
        type: 'EXPENSE',
        categoryName: 'Hóa đơn',
        amount: waterBill,
        description: 'Tiền nước sinh hoạt',
        daysAgo: day,
      });
    }

    if (dayOfMonth === 25 && Math.random() < 0.7) {
      const internetBill = 250000 + Math.floor(Math.random() * 3) * 50000;
      transactions.push({
        type: 'EXPENSE',
        categoryName: 'Hóa đơn',
        amount: internetBill,
        description: 'Internet FPT',
        daysAgo: day,
      });
    }

    if (Math.random() < 0.12) {
      const transportAmount = Math.round((25000 + Math.random() * 75000) / 1000) * 1000;
      transactions.push({
        type: 'EXPENSE',
        categoryName: 'Di lại',
        amount: transportAmount,
        description: Math.random() < 0.5 ? 'Grab đi làm' : 'Xăng xe máy',
        daysAgo: day,
      });
    }

    if (dayOfMonth >= 28 && Math.random() < 0.3) {
      const savingsAmount = Math.round((500000 + Math.random() * 1500000) / 1000) * 1000;
      transactions.push({
        type: 'EXPENSE',
        categoryName: 'Chi tiêu khác',
        amount: savingsAmount,
        description: 'Chuyển vào quỹ tiết kiệm',
        daysAgo: day,
      });
    }

    if (Math.random() < 0.05) {
      const transferAmount = Math.round((200000 + Math.random() * 800000) / 1000) * 1000;
      transactions.push({
        type: 'EXPENSE',
        categoryName: 'Chuyển khoản đi',
        amount: transferAmount,
        description: 'Chuyển tiền cho người thân',
        daysAgo: day,
      });

      transactions.push({
        type: 'INCOME',
        categoryName: 'Chuyển khoản đến',
        amount: transferAmount,
        description: 'Nhận tiền từ gia đình',
        daysAgo: day,
      });
    }
  }

  return transactions.sort((a, b) => a.daysAgo - b.daysAgo);
}

function getRealisticExpenseDescription(category: string): string {
  const descriptions: Record<string, string[]> = {
    'Ăn uống': ['Cơm trưa văn phòng', 'Cà phê sáng', 'Cơm tối gia đình', 'Trà sữa', 'Bánh mì'],
    'Di lại': ['Grab đi làm', 'Xăng xe máy', 'Taxi sân bay', 'GrabBike'],
    'Mua sắm': ['Quần áo mùa hè', 'Giày thể thao', 'Mỹ phẩm', 'Đồ gia dụng'],
    'Giải trí': ['Vé xem phim', 'Spotify Premium', 'Netflix', 'Game mobile', 'Karaoke cuối tuần'],
    'Hóa đơn': ['Tiền điện sinh hoạt', 'Tiền nước', 'Internet FPT', 'Điện thoại trả sau'],
    'Sức khỏe': ['Khám sức khỏe định kỳ', 'Thuốc bổ', 'Vitamin'],
    'Giáo dục': ['Khóa học online Udemy', 'Sách kỹ thuật', 'English Premium'],
  };

  const options = descriptions[category] || ['Chi tiêu khác'];
  return options[Math.floor(Math.random() * options.length)];
}

export async function createDemoUserWithScenario(
  fastify: any,
  scenarioKey: string,
  fullName: string,
  salaryDay?: number
): Promise<{ userId: string; accountId: string; bankAccountIds: string[] }> {
  const scenario = SCENARIOS[scenarioKey] || SCENARIOS.office_worker;
  const bcrypt = require('bcryptjs');
  const { v4: uuidv4 } = require('uuid');

  const email = `demo_${scenarioKey}_${Date.now()}@fintech.demo`;
  const passwordHash = await bcrypt.hash('Demo123!@#', 12);

  const userId = uuidv4();
  const now = Math.floor(Date.now() / 1000);

  await execute(
    `INSERT INTO users (id, email, password_hash, full_name, is_active, is_verified, created_at, updated_at)
     VALUES ($1, $2, $3, $4, true, true, $5, $5)`,
    [userId, email, passwordHash, fullName || scenario.name, now]
  );

  const accountId = uuidv4();
  await execute(
    `INSERT INTO accounts (id, user_id, name, type, initial_balance, current_balance, currency, created_at, updated_at)
     VALUES ($1, $2, 'Ví chính', 'WALLET', 5000000, 5000000, 'VND', $3, $3)`,
    [accountId, userId, now]
  );

  const bankAccountIds: string[] = [];
  for (const bankAcc of scenario.bankAccounts) {
    const bankAccountId = uuidv4();
    const bank: any = await queryOne('SELECT id FROM simulated_banks WHERE code = $1', [bankAcc.bankCode]);

    if (bank) {
      await execute(
        `INSERT INTO user_bank_accounts (id, user_id, bank_id, account_number, account_holder_name, balance, is_active, linked_at)
         VALUES ($1, $2, $3, $4, $5, $6, true, $7)`,
        [bankAccountId, userId, bank.id, bankAcc.accountNumber, fullName || scenario.name, bankAcc.balance, now]
      );
      bankAccountIds.push(bankAccountId);
    }
  }

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

  const categoryMap: Record<string, string> = {};
  const categories = await query('SELECT id, name FROM categories WHERE is_system = true');
  for (const cat of categories) {
    categoryMap[cat.name] = cat.id;
  }

  for (const budget of scenario.budgets) {
    const budgetId = uuidv4();
    const categoryId = categoryMap[budget.categoryName] || null;

    await execute(
      `INSERT INTO budgets (id, user_id, category_id, name, amount, spent_amount, remaining_amount, progress, period, start_date, is_active, created_at)
       VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, true, $11)`,
      [
        budgetId, userId, categoryId, budget.name, budget.amount,
        Math.round(budget.amount * (0.3 + Math.random() * 0.5)),
        Math.round(budget.amount * (0.2 + Math.random() * 0.3)),
        Math.round((30 + Math.random() * 50) * 100) / 100,
        budget.period, new Date().toISOString().split('T')[0], now
      ]
    );
  }

  const generatedTransactions = generateRandomTransactions(scenario, 30);
  for (const txn of generatedTransactions) {
    const categoryId = categoryMap[txn.categoryName] || null;
    const timestamp = Math.floor((Date.now() - txn.daysAgo * 24 * 60 * 60 * 1000) / 1000);

    await execute(
      `INSERT INTO transactions (id, user_id, account_id, category_id, type, amount, currency, description, date, source_type, reference_id, created_at, updated_at)
       VALUES ($1, $2, $3, $4, $5, $6, 'VND', $7, $8, 'SIMULATION', $9, $10, $10)`,
      [uuidv4(), userId, accountId, categoryId, txn.type, txn.amount, txn.description, timestamp, `SIM_${uuidv4()}`, now]
    );

    if (txn.type === 'EXPENSE' && txn.categoryName === 'Chi tiêu khác' && txn.description.includes('quỹ')) {
      const fund: any = await queryOne('SELECT id, current_amount, target_amount FROM funds WHERE user_id = $1 LIMIT 1', [userId]);
      if (fund) {
        const newFundAmount = parseFloat(fund.current_amount) + txn.amount;
        const newProgress = Math.round((newFundAmount / parseFloat(fund.target_amount)) * 10000) / 100;
        await execute('UPDATE funds SET current_amount = $1, progress = $2, updated_at = $3 WHERE id = $4', [newFundAmount, newProgress, now, fund.id]);
      }
    }
  }

  await execute(
    `INSERT INTO demo_users (id, user_id, simulation_enabled, salary_day, auto_allocation_enabled, created_at, updated_at)
     VALUES ($1, $2, true, $3, true, $4, $4)`,
    [uuidv4(), userId, salaryDay || scenario.salaryDay, now]
  );

  return { userId, accountId, bankAccountIds };
}

export async function getSimulationScenarios() {
  return Object.entries(SCENARIOS).map(([key, scenario]) => ({
    key,
    name: scenario.name,
    description: scenario.description,
    monthlyIncome: scenario.monthlyIncome,
    salaryDay: scenario.salaryDay,
    bankAccountCount: scenario.bankAccounts.length,
    fundCount: scenario.funds.length,
    budgetCount: scenario.budgets.length,
  }));
}
