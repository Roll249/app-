import { query, execute } from '../utils/db.js';

async function setupDatabase() {
  console.log('Starting database setup...');
  
  try {
    // Create tables
    console.log('Creating tables...');

    // Users table
    await execute(`
      CREATE TABLE IF NOT EXISTS users (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        email VARCHAR(255) UNIQUE NOT NULL,
        password_hash VARCHAR(255) NOT NULL,
        full_name VARCHAR(255),
        phone VARCHAR(50),
        avatar_url TEXT,
        is_active BOOLEAN DEFAULT true,
        is_verified BOOLEAN DEFAULT false,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
      )
    `);

    // Accounts table
    await execute(`
      CREATE TABLE IF NOT EXISTS accounts (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        user_id UUID REFERENCES users(id) ON DELETE CASCADE,
        name VARCHAR(255) NOT NULL,
        type VARCHAR(50) NOT NULL,
        initial_balance DECIMAL(15,2) DEFAULT 0,
        current_balance DECIMAL(15,2) DEFAULT 0,
        currency VARCHAR(10) DEFAULT 'VND',
        icon VARCHAR(100),
        color VARCHAR(50),
        is_active BOOLEAN DEFAULT true,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
      )
    `);

    // Categories table
    await execute(`
      CREATE TABLE IF NOT EXISTS categories (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        name VARCHAR(100) NOT NULL,
        icon VARCHAR(100),
        color VARCHAR(50),
        type VARCHAR(20) NOT NULL,
        is_system BOOLEAN DEFAULT false,
        user_id UUID REFERENCES users(id) ON DELETE CASCADE,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
      )
    `);

    // Transactions table
    await execute(`
      CREATE TABLE IF NOT EXISTS transactions (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        user_id UUID REFERENCES users(id) ON DELETE CASCADE,
        account_id UUID REFERENCES accounts(id) ON DELETE CASCADE,
        category_id UUID REFERENCES categories(id) ON DELETE SET NULL,
        type VARCHAR(20) NOT NULL,
        amount DECIMAL(15,2) NOT NULL,
        currency VARCHAR(10) DEFAULT 'VND',
        description TEXT,
        date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        source_type VARCHAR(50),
        reference_id VARCHAR(255),
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
      )
    `);

    // Funds table
    await execute(`
      CREATE TABLE IF NOT EXISTS funds (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        user_id UUID REFERENCES users(id) ON DELETE CASCADE,
        name VARCHAR(255) NOT NULL,
        icon VARCHAR(100),
        color VARCHAR(50),
        target_amount DECIMAL(15,2) NOT NULL,
        current_amount DECIMAL(15,2) DEFAULT 0,
        progress DECIMAL(5,2) DEFAULT 0,
        is_active BOOLEAN DEFAULT true,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
      )
    `);

    // Budgets table
    await execute(`
      CREATE TABLE IF NOT EXISTS budgets (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        user_id UUID REFERENCES users(id) ON DELETE CASCADE,
        category_id UUID REFERENCES categories(id) ON DELETE CASCADE,
        name VARCHAR(255) NOT NULL,
        amount DECIMAL(15,2) NOT NULL,
        spent_amount DECIMAL(15,2) DEFAULT 0,
        remaining_amount DECIMAL(15,2),
        progress DECIMAL(5,2) DEFAULT 0,
        period VARCHAR(20) DEFAULT 'MONTHLY',
        start_date DATE,
        end_date DATE,
        is_active BOOLEAN DEFAULT true,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
      )
    `);

    // Simulated banks table
    await execute(`
      CREATE TABLE IF NOT EXISTS simulated_banks (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        code VARCHAR(20) UNIQUE NOT NULL,
        name VARCHAR(255) NOT NULL,
        short_name VARCHAR(50),
        vietqr_prefix VARCHAR(50),
        swift_code VARCHAR(20),
        logo_url TEXT,
        is_active BOOLEAN DEFAULT true,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
      )
    `);

    // User bank accounts table
    await execute(`
      CREATE TABLE IF NOT EXISTS user_bank_accounts (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        user_id UUID REFERENCES users(id) ON DELETE CASCADE,
        bank_id UUID REFERENCES simulated_banks(id),
        account_number VARCHAR(50) NOT NULL,
        account_holder_name VARCHAR(255),
        balance DECIMAL(15,2) DEFAULT 0,
        is_active BOOLEAN DEFAULT true,
        linked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
      )
    `);

    // Demo users table
    await execute(`
      CREATE TABLE IF NOT EXISTS demo_users (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        user_id UUID REFERENCES users(id) ON DELETE CASCADE,
        simulation_enabled BOOLEAN DEFAULT true,
        salary_day INTEGER DEFAULT 5,
        auto_allocation_enabled BOOLEAN DEFAULT false,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
      )
    `);

    // AI chat logs table
    await execute(`
      CREATE TABLE IF NOT EXISTS ai_chat_logs (
        id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
        user_id VARCHAR(255),
        session_id VARCHAR(255),
        role VARCHAR(20) NOT NULL,
        content TEXT,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
      )
    `);

    console.log('Tables created successfully!');

    // Seed system categories
    console.log('Seeding categories...');
    
    const categories = [
      { name: 'Lương', icon: 'payments', color: '#4CAF50', type: 'INCOME' },
      { name: 'Thu nhập khác', icon: 'attach_money', color: '#8BC34A', type: 'INCOME' },
      { name: 'Thưởng', icon: 'card_giftcard', color: '#FF9800', type: 'INCOME' },
      { name: 'Chuyển khoản đến', icon: 'call_received', color: '#2196F3', type: 'INCOME' },
      { name: 'Ăn uống', icon: 'restaurant', color: '#FF5722', type: 'EXPENSE' },
      { name: 'Di lại', icon: 'directions_car', color: '#607D8B', type: 'EXPENSE' },
      { name: 'Mua sắm', icon: 'shopping_bag', color: '#E91E63', type: 'EXPENSE' },
      { name: 'Giải trí', icon: 'sports_esports', color: '#9C27B0', type: 'EXPENSE' },
      { name: 'Hóa đơn', icon: 'receipt', color: '#795548', type: 'EXPENSE' },
      { name: 'Sức khỏe', icon: 'medical_services', color: '#F44336', type: 'EXPENSE' },
      { name: 'Giáo dục', icon: 'school', color: '#3F51B5', type: 'EXPENSE' },
      { name: 'Chi tiêu khác', icon: 'more_horiz', color: '#9E9E9E', type: 'EXPENSE' },
      { name: 'Chuyển khoản đi', icon: 'call_made', color: '#F44336', type: 'EXPENSE' },
    ];

    for (const cat of categories) {
      await execute(
        `INSERT INTO categories (name, icon, color, type, is_system)
         VALUES ($1, $2, $3, $4, true)
         ON CONFLICT DO NOTHING`,
        [cat.name, cat.icon, cat.color, cat.type]
      );
    }

    console.log('Categories seeded!');

    // Seed simulated banks
    console.log('Seeding banks...');
    
    const banks = [
      { code: 'VCB', name: 'Ngân hàng TMCP Ngoại Thương Việt Nam', short: 'Vietcombank', prefix: '970436', swift: 'VCBVVNVX' },
      { code: 'VTB', name: 'Ngân hàng TMCP Công Thương Việt Nam', short: 'VietinBank', prefix: '970415', swift: 'ICBVVNVX' },
      { code: 'BIDV', name: 'Ngân hàng TMCP Đầu tư và Phát triển Việt Nam', short: 'BIDV', prefix: '970418', swift: 'BIDVVNVX' },
      { code: 'TPB', name: 'Ngân hàng TMCP Tiên Phong', short: 'TPBank', prefix: '970459', swift: 'TPBVVNVX' },
      { code: 'ACB', name: 'Ngân hàng TMCP Á Châu', short: 'ACB', prefix: '970416', swift: 'ASCBVNVX' },
      { code: 'MB', name: 'Ngân hàng TMCP Quân đội', short: 'MB Bank', prefix: '970422', swift: 'MSCBVNVX' },
      { code: 'SHB', name: 'Ngân hàng TMCP Sài Gòn - Hà Nội', short: 'SHB', prefix: '970443', swift: 'SHBAVNVX' },
      { code: 'OCB', name: 'Ngân hàng TMCP Phương Đông', short: 'OCB', prefix: '970448', swift: 'ORCOVNVX' },
      { code: 'HDB', name: 'Ngân hàng TMCP Phát triển Thành phố Hồ Chí Minh', short: 'HDBank', prefix: '970437', swift: 'HDBCVNVX' },
      { code: 'VIB', name: 'Ngân hàng TMCP Quốc tế Việt Nam', short: 'VIB', prefix: '970441', swift: 'VBICVNVX' },
    ];

    for (const bank of banks) {
      await execute(
        `INSERT INTO simulated_banks (code, name, short_name, vietqr_prefix, swift_code)
         VALUES ($1, $2, $3, $4, $5)
         ON CONFLICT (code) DO NOTHING`,
        [bank.code, bank.name, bank.short, bank.prefix, bank.swift]
      );
    }

    console.log('Banks seeded!');
    console.log('Database setup completed successfully!');
    
  } catch (error) {
    console.error('Database setup failed:', error);
    process.exit(1);
  }
}

setupDatabase();
