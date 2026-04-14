import { Pool } from 'pg';

const pool = new Pool({
  connectionString: process.env.DATABASE_URL || 'postgres://postgres:postgres@localhost:5432/fintech_db',
  max: 20,
  idleTimeoutMillis: 30000,
  connectionTimeoutMillis: 2000,
});

pool.on('error', (err) => {
  console.error('Unexpected error on idle client', err);
});

export async function query(text: string, params?: any[]): Promise<any[]> {
  const result = await pool.query(text, params);
  return result.rows;
}

export async function queryOne(text: string, params?: any[]): Promise<any | null> {
  const result = await pool.query(text, params);
  return result.rows[0] || null;
}

export async function execute(text: string, params?: any[]): Promise<number> {
  const result = await pool.query(text, params);
  return result.rowCount || 0;
}

export async function initDatabase(): Promise<void> {
  console.log('Initializing database schema...');
  
  await query(`
    CREATE TABLE IF NOT EXISTS users (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      email VARCHAR(255) UNIQUE NOT NULL,
      password_hash VARCHAR(255) NOT NULL,
      full_name VARCHAR(255) NOT NULL,
      avatar_url TEXT,
      phone VARCHAR(20),
      date_of_birth DATE,
      is_active BOOLEAN DEFAULT true,
      is_verified BOOLEAN DEFAULT false,
      created_at BIGINT DEFAULT EXTRACT(EPOCH FROM NOW())::BIGINT,
      updated_at BIGINT DEFAULT EXTRACT(EPOCH FROM NOW())::BIGINT
    )
  `);

  await query(`
    CREATE TABLE IF NOT EXISTS refresh_tokens (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      user_id UUID REFERENCES users(id) ON DELETE CASCADE,
      token VARCHAR(512) UNIQUE NOT NULL,
      device_info VARCHAR(255),
      ip_address VARCHAR(50),
      expires_at BIGINT NOT NULL,
      created_at BIGINT DEFAULT EXTRACT(EPOCH FROM NOW())::BIGINT,
      revoked_at BIGINT
    )
  `);

  await query(`
    CREATE TABLE IF NOT EXISTS simulated_banks (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      code VARCHAR(20) UNIQUE NOT NULL,
      name VARCHAR(255) NOT NULL,
      short_name VARCHAR(50),
      logo_url TEXT,
      vietqr_prefix VARCHAR(20),
      swift_code VARCHAR(20),
      initial_balance DECIMAL(18,2) DEFAULT 10000000,
      is_active BOOLEAN DEFAULT true,
      created_at BIGINT DEFAULT EXTRACT(EPOCH FROM NOW())::BIGINT
    )
  `);

  await query(`
    CREATE TABLE IF NOT EXISTS user_bank_accounts (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      user_id UUID REFERENCES users(id) ON DELETE CASCADE,
      bank_id UUID REFERENCES simulated_banks(id),
      account_number VARCHAR(50) NOT NULL,
      account_holder_name VARCHAR(255) NOT NULL,
      balance DECIMAL(18,2) DEFAULT 10000000,
      is_active BOOLEAN DEFAULT true,
      linked_at BIGINT DEFAULT EXTRACT(EPOCH FROM NOW())::BIGINT
    )
  `);

  await query(`
    CREATE TABLE IF NOT EXISTS categories (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      user_id UUID,
      name VARCHAR(255) NOT NULL,
      icon VARCHAR(100),
      color VARCHAR(20),
      type VARCHAR(20) NOT NULL,
      parent_id UUID,
      is_system BOOLEAN DEFAULT false,
      is_active BOOLEAN DEFAULT true,
      sort_order INTEGER DEFAULT 0,
      created_at BIGINT DEFAULT EXTRACT(EPOCH FROM NOW())::BIGINT
    )
  `);

  await query(`
    CREATE TABLE IF NOT EXISTS accounts (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      user_id VARCHAR(100) NOT NULL,
      name VARCHAR(255) NOT NULL,
      type VARCHAR(50) NOT NULL,
      icon VARCHAR(100),
      color VARCHAR(20),
      initial_balance DECIMAL(18,2) DEFAULT 0,
      current_balance DECIMAL(18,2) DEFAULT 0,
      currency VARCHAR(10) DEFAULT 'VND',
      include_in_total BOOLEAN DEFAULT true,
      is_active BOOLEAN DEFAULT true,
      bank_account_id UUID,
      created_at BIGINT DEFAULT EXTRACT(EPOCH FROM NOW())::BIGINT,
      updated_at BIGINT DEFAULT EXTRACT(EPOCH FROM NOW())::BIGINT
    )
  `);

  await query(`
    CREATE TABLE IF NOT EXISTS funds (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      user_id VARCHAR(100) NOT NULL,
      name VARCHAR(255) NOT NULL,
      icon VARCHAR(100),
      color VARCHAR(20),
      description TEXT,
      target_amount DECIMAL(18,2),
      current_amount DECIMAL(18,2) DEFAULT 0,
      progress DECIMAL(5,2) DEFAULT 0,
      start_date VARCHAR(20),
      end_date VARCHAR(20),
      is_active BOOLEAN DEFAULT true,
      created_at BIGINT DEFAULT EXTRACT(EPOCH FROM NOW())::BIGINT,
      updated_at BIGINT DEFAULT EXTRACT(EPOCH FROM NOW())::BIGINT
    )
  `);

  await query(`
    CREATE TABLE IF NOT EXISTS transactions (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      user_id VARCHAR(100) NOT NULL,
      account_id UUID REFERENCES accounts(id),
      category_id UUID,
      type VARCHAR(20) NOT NULL,
      amount DECIMAL(18,2) NOT NULL,
      currency VARCHAR(10) DEFAULT 'VND',
      description TEXT,
      note TEXT,
      date BIGINT NOT NULL,
      source_type VARCHAR(50),
      reference_id VARCHAR(255),
      related_transaction_id UUID,
      is_active BOOLEAN DEFAULT true,
      created_at BIGINT DEFAULT EXTRACT(EPOCH FROM NOW())::BIGINT,
      updated_at BIGINT DEFAULT EXTRACT(EPOCH FROM NOW())::BIGINT
    )
  `);

  await query(`
    CREATE TABLE IF NOT EXISTS budgets (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      user_id UUID REFERENCES users(id) ON DELETE CASCADE,
      category_id UUID,
      name VARCHAR(255) NOT NULL,
      amount DECIMAL(18,2) NOT NULL,
      spent_amount DECIMAL(18,2) DEFAULT 0,
      remaining_amount DECIMAL(18,2) DEFAULT 0,
      progress DECIMAL(5,2) DEFAULT 0,
      period VARCHAR(20) NOT NULL,
      start_date VARCHAR(20) NOT NULL,
      end_date VARCHAR(20),
      is_active BOOLEAN DEFAULT true,
      created_at BIGINT DEFAULT EXTRACT(EPOCH FROM NOW())::BIGINT
    )
  `);

  await query(`
    CREATE TABLE IF NOT EXISTS qr_codes (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      user_id UUID REFERENCES users(id) ON DELETE CASCADE,
      type VARCHAR(20) NOT NULL,
      payload TEXT NOT NULL,
      signature VARCHAR(512) NOT NULL,
      amount DECIMAL(18,2),
      message TEXT,
      account_id UUID,
      bank_account_id UUID,
      expires_at BIGINT NOT NULL,
      used_at BIGINT,
      used_by UUID,
      is_active BOOLEAN DEFAULT true,
      created_at BIGINT DEFAULT EXTRACT(EPOCH FROM NOW())::BIGINT
    )
  `);

  await query(`
    CREATE TABLE IF NOT EXISTS demo_users (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      user_id UUID REFERENCES users(id) ON DELETE CASCADE,
      simulation_enabled BOOLEAN DEFAULT true,
      salary_day INTEGER DEFAULT 5,
      auto_allocation_enabled BOOLEAN DEFAULT true,
      created_at BIGINT DEFAULT EXTRACT(EPOCH FROM NOW())::BIGINT,
      updated_at BIGINT DEFAULT EXTRACT(EPOCH FROM NOW())::BIGINT
    )
  `);

  await query(`
    CREATE TABLE IF NOT EXISTS ai_chat_logs (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      user_id VARCHAR(100) DEFAULT 'anonymous',
      session_id VARCHAR(100) DEFAULT 'default',
      messages JSONB DEFAULT '[]',
      created_at BIGINT DEFAULT EXTRACT(EPOCH FROM NOW())::BIGINT
    )
  `);

  await query(`
    CREATE TABLE IF NOT EXISTS savings_goals (
      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
      user_id VARCHAR(100) NOT NULL,
      name VARCHAR(255) NOT NULL,
      target_amount DECIMAL(18,2) NOT NULL,
      current_amount DECIMAL(18,2) DEFAULT 0,
      period VARCHAR(20) DEFAULT 'MONTHLY',
      amount_per_period DECIMAL(18,2) DEFAULT 0,
      start_date VARCHAR(20),
      end_date VARCHAR(20),
      is_active BOOLEAN DEFAULT true,
      created_at BIGINT DEFAULT EXTRACT(EPOCH FROM NOW())::BIGINT,
      updated_at BIGINT DEFAULT EXTRACT(EPOCH FROM NOW())::BIGINT
    )
  `);

  // Create indexes
  await query(`CREATE INDEX IF NOT EXISTS idx_users_email ON users(email)`);
  await query(`CREATE INDEX IF NOT EXISTS idx_transactions_user_id ON transactions(user_id)`);
  await query(`CREATE INDEX IF NOT EXISTS idx_transactions_date ON transactions(date)`);
  await query(`CREATE INDEX IF NOT EXISTS idx_accounts_user_id ON accounts(user_id)`);
  await query(`CREATE INDEX IF NOT EXISTS idx_categories_user_id ON categories(user_id)`);
  await query(`CREATE INDEX IF NOT EXISTS idx_ai_chat_logs_user_id ON ai_chat_logs(user_id)`);

  // Seed default banks
  const banks = [
    { code: 'VCB', name: 'Ngân hàng TMCP Ngoại Thương Việt Nam', shortName: 'Vietcombank', vietqrPrefix: '970436', swiftCode: 'ICBVVNVX' },
    { code: 'VTB', name: 'Ngân hàng TMCP Công Thương Việt Nam', shortName: 'VietinBank', vietqrPrefix: '970415', swiftCode: 'ICBVVNVX' },
    { code: 'BIDV', name: 'Ngân hàng TMCP Đầu tư và Phát triển Việt Nam', shortName: 'BIDV', vietqrPrefix: '970418', swiftCode: 'BIDVVNVX' },
    { code: 'TPB', name: 'Ngân hàng TMCP Tiên Phong', shortName: 'TPBank', vietqrPrefix: '970423', swiftCode: 'TPBVVNVX' },
    { code: 'ACB', name: 'Ngân hàng TMCP Á Châu', shortName: 'ACB', vietqrPrefix: '970416', swiftCode: 'ASCBVNVX' },
    { code: 'MB', name: 'Ngân hàng TMCP Quân đội', shortName: 'MB Bank', vietqrPrefix: '970422', swiftCode: 'MSCBVNVX' },
    { code: 'SHB', name: 'Ngân hàng TMCP Sài Gòn - Hà Nội', shortName: 'SHB', vietqrPrefix: '970429', swiftCode: 'SHBAVNVX' },
    { code: 'OCB', name: 'Ngân hàng TMCP Phương Đông', shortName: 'OCB', vietqrPrefix: '970448', swiftCode: 'ORCOVNVX' },
    { code: 'HDB', name: 'Ngân hàng TMCP Phát triển TP.HCM', shortName: 'HDBank', vietqrPrefix: '970437', swiftCode: 'HDBCVNVX' },
    { code: 'VIB', name: 'Ngân hàng TMCP Quốc tế Việt Nam', shortName: 'VIB', vietqrPrefix: '970441', swiftCode: 'VNIBVNVX' },
  ];

  for (const bank of banks) {
    await query(
      `INSERT INTO simulated_banks (code, name, short_name, vietqr_prefix, swift_code) VALUES ($1, $2, $3, $4, $5) ON CONFLICT (code) DO NOTHING`,
      [bank.code, bank.name, bank.shortName, bank.vietqrPrefix, bank.swiftCode]
    );
  }

  // Seed default categories
  const incomeCategories = [
    { name: 'Lương', icon: 'cash', color: '#4CAF50' },
    { name: 'Thưởng', icon: 'card_giftcard', color: '#8BC34A' },
    { name: 'Đầu tư', icon: 'trending_up', color: '#009688' },
    { name: 'Chuyển khoản đến', icon: 'call_received', color: '#2196F3' },
    { name: 'Thu nhập khác', icon: 'attach_money', color: '#607D8B' },
  ];

  const expenseCategories = [
    { name: 'Ăn uống', icon: 'restaurant', color: '#FF5722' },
    { name: 'Di lại', icon: 'directions_car', color: '#795548' },
    { name: 'Mua sắm', icon: 'shopping_cart', color: '#9C27B0' },
    { name: 'Giải trí', icon: 'movie', color: '#E91E63' },
    { name: 'Hóa đơn', icon: 'receipt', color: '#F44336' },
    { name: 'Sức khỏe', icon: 'local_hospital', color: '#E53935' },
    { name: 'Giáo dục', icon: 'school', color: '#3F51B5' },
    { name: 'Nhà ở', icon: 'home', color: '#009688' },
    { name: 'Chuyển khoản đi', icon: 'call_made', color: '#9E9E9E' },
    { name: 'Chi tiêu khác', icon: 'more_horiz', color: '#607D8B' },
  ];

  for (let i = 0; i < incomeCategories.length; i++) {
    const cat = incomeCategories[i];
    await query(
      `INSERT INTO categories (name, icon, color, type, is_system, sort_order) VALUES ($1, $2, $3, 'INCOME', true, $4) ON CONFLICT DO NOTHING`,
      [cat.name, cat.icon, cat.color, i + 1]
    );
  }

  for (let i = 0; i < expenseCategories.length; i++) {
    const cat = expenseCategories[i];
    await query(
      `INSERT INTO categories (name, icon, color, type, is_system, sort_order) VALUES ($1, $2, $3, 'EXPENSE', true, $4) ON CONFLICT DO NOTHING`,
      [cat.name, cat.icon, cat.color, i + 1]
    );
  }

  console.log('Database schema initialized successfully');
}

export default pool;