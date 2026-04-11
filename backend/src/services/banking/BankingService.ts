/**
 * Banking Service - External Service Integration
 * Handles communication with banking APIs (simulated)
 * 
 * In production, this would connect to real banking APIs like:
 * - VietQR API
 * - Bank VNPAY
 * - Napas
 */
import { BaseService, ServiceConfig, ServiceHealth } from '../base/BaseService.js';
import { query, queryOne } from '../../utils/db.js';

export interface BankAccount {
  id: string;
  bankCode: string;
  bankName: string;
  accountNumber: string;
  accountHolderName: string;
  balance: number;
  isActive: boolean;
  linkedAt: Date;
}

export interface TransferRequest {
  fromAccountId: string;
  toAccountNumber: string;
  toBankCode: string;
  amount: number;
  message?: string;
}

export interface TransferResult {
  success: boolean;
  transactionId?: string;
  message: string;
  fromBalance?: number;
  toBalance?: number;
}

export interface QRGenerateRequest {
  accountId: string;
  amount?: number;
  message?: string;
}

export interface QRCodeResponse {
  qrData: string;
  qrImage: string;
  expiresAt: Date;
}

export interface BankBalance {
  accountNumber: string;
  balance: number;
  lastUpdated: Date;
}

export class BankingService extends BaseService {
  private banksCache: Map<string, any> = new Map();

  constructor(config: ServiceConfig, logger: any) {
    super(config, logger);
  }

  getName(): string {
    return 'BankingService';
  }

  async initialize(): Promise<void> {
    this.logger.info(`[${this.getName()}] Initializing banking service...`);
    
    try {
      await this.loadBanks();
      this.isInitialized = true;
      this.logger.info(`[${this.getName()}] Banking service initialized with ${this.banksCache.size} banks`);
    } catch (error: any) {
      this.logger.error(`[${this.getName()}] Failed to initialize: ${error.message}`);
    }
  }

  protected async ping(): Promise<boolean> {
    try {
      const banks = await query('SELECT COUNT(*) as count FROM simulated_banks WHERE is_active = true');
      return (banks[0]?.count || 0) > 0;
    } catch {
      return false;
    }
  }

  async loadBanks(): Promise<void> {
    const banks = await query('SELECT * FROM simulated_banks WHERE is_active = true ORDER BY code');
    this.banksCache.clear();
    for (const bank of banks) {
      this.banksCache.set(bank.code, bank);
    }
  }

  async getHealth(): Promise<ServiceHealth> {
    const startTime = Date.now();
    try {
      const banks = await query('SELECT COUNT(*) as count FROM simulated_banks WHERE is_active = true');
      return {
        status: (banks[0]?.count || 0) > 0 ? 'healthy' : 'degraded',
        latencyMs: Date.now() - startTime,
        message: `${banks[0]?.count || 0} banks available`,
        lastChecked: new Date()
      };
    } catch (error: any) {
      return {
        status: 'degraded',
        latencyMs: Date.now() - startTime,
        message: error.message,
        lastChecked: new Date()
      };
    }
  }

  async getBanks(): Promise<Array<{
    id: string;
    code: string;
    name: string;
    shortName: string;
    vietqrPrefix: string;
  }>> {
    return Array.from(this.banksCache.values()).map((bank: any) => ({
      id: bank.id,
      code: bank.code,
      name: bank.name,
      shortName: bank.short_name,
      vietqrPrefix: bank.vietqr_prefix
    }));
  }

  async getUserBankAccounts(userId: string): Promise<BankAccount[]> {
    const accounts = await query(
      `SELECT uba.*, sb.name as bank_name, sb.short_name as bank_short_name, sb.code as bank_code
       FROM user_bank_accounts uba
       JOIN simulated_banks sb ON uba.bank_id = sb.id
       WHERE uba.user_id = $1 AND uba.is_active = true`,
      [userId]
    );

    return accounts.map((a: any) => ({
      id: a.id,
      bankCode: a.bank_code,
      bankName: a.bank_name,
      accountNumber: a.account_number,
      accountHolderName: a.account_holder_name,
      balance: parseFloat(a.balance),
      isActive: a.is_active,
      linkedAt: new Date(a.linked_at * 1000)
    }));
  }

  async linkBankAccount(
    userId: string,
    bankCode: string,
    accountNumber: string,
    accountHolderName: string
  ): Promise<BankAccount> {
    const bank = this.banksCache.get(bankCode);
    if (!bank) {
      throw new Error(`Bank with code ${bankCode} not found`);
    }

    const existing = await queryOne(
      'SELECT * FROM user_bank_accounts WHERE user_id = $1 AND bank_id = $2 AND account_number = $3',
      [userId, bank.id, accountNumber]
    );

    if (existing) {
      throw new Error('Bank account already linked');
    }

    const { v4: uuidv4 } = require('uuid');
    const accountId = uuidv4();
    const now = Math.floor(Date.now() / 1000);

    await query(
      `INSERT INTO user_bank_accounts (id, user_id, bank_id, account_number, account_holder_name, balance, is_active, linked_at)
       VALUES ($1, $2, $3, $4, $5, $6, true, $7)`,
      [accountId, userId, bank.id, accountNumber, accountHolderName, 10000000, now]
    );

    return {
      id: accountId,
      bankCode: bank.code,
      bankName: bank.name,
      accountNumber,
      accountHolderName,
      balance: 10000000,
      isActive: true,
      linkedAt: new Date()
    };
  }

  async unlinkBankAccount(userId: string, accountId: string): Promise<void> {
    await query(
      'UPDATE user_bank_accounts SET is_active = false WHERE id = $1 AND user_id = $2',
      [accountId, userId]
    );
  }

  async transfer(request: TransferRequest): Promise<TransferResult> {
    const { fromAccountId, toAccountNumber, toBankCode, amount, message } = request;
    const { v4: uuidv4 } = require('uuid');

    this.logger.info(`[${this.getName()}] Processing transfer: ${amount} from ${fromAccountId} to ${toAccountNumber}`);

    // Get source account
    const fromAccount = await queryOne(
      'SELECT * FROM user_bank_accounts WHERE id = $1 AND is_active = true',
      [fromAccountId]
    );

    if (!fromAccount) {
      return {
        success: false,
        message: 'Source account not found'
      };
    }

    const currentBalance = parseFloat(fromAccount.balance);
    if (currentBalance < amount) {
      return {
        success: false,
        message: 'Insufficient balance',
        fromBalance: currentBalance
      };
    }

    // Get destination bank
    const toBank = this.banksCache.get(toBankCode);
    if (!toBank) {
      return {
        success: false,
        message: 'Destination bank not found'
      };
    }

    // Find or create destination account
    let toAccount = await queryOne(
      'SELECT * FROM user_bank_accounts WHERE account_number = $1 AND bank_id = $2 AND is_active = true',
      [toAccountNumber, toBank.id]
    );

    if (!toAccount) {
      const { v4: uuidv4 } = require('uuid');
      const newAccountId = uuidv4();
      const now = Math.floor(Date.now() / 1000);

      await query(
        `INSERT INTO user_bank_accounts (id, user_id, bank_id, account_number, account_holder_name, balance, is_active, linked_at)
         VALUES ($1, $2, $3, $4, $5, $6, true, $7)`,
        [newAccountId, fromAccount.user_id, toBank.id, toAccountNumber, 'Unknown', 0, now]
      );

      toAccount = await queryOne('SELECT * FROM user_bank_accounts WHERE id = $1', [newAccountId]);
    }

    // Perform transfer
    const newFromBalance = currentBalance - amount;
    const newToBalance = parseFloat(toAccount.balance) + amount;

    await query(
      'UPDATE user_bank_accounts SET balance = $1 WHERE id = $2',
      [newFromBalance, fromAccountId]
    );

    await query(
      'UPDATE user_bank_accounts SET balance = $1 WHERE id = $2',
      [newToBalance, toAccount.id]
    );

    // Create transaction records
    const transactionId = uuidv4();
    const now = Math.floor(Date.now() / 1000);

    await query(
      `INSERT INTO transactions (id, user_id, account_id, type, amount, description, date, source_type, created_at, updated_at)
       VALUES ($1, $2, $3, 'TRANSFER', $4, $5, $6, 'BANK_TRANSFER', $7, $7)`,
      [uuidv4(), fromAccount.user_id, fromAccountId, amount, message || 'Chuyển khoản', now, now]
    );

    return {
      success: true,
      transactionId,
      message: 'Transfer completed successfully',
      fromBalance: newFromBalance,
      toBalance: newToBalance
    };
  }

  async generateQRCode(request: QRGenerateRequest): Promise<QRCodeResponse> {
    const { accountId, amount, message } = request;

    const account = await queryOne(
      'SELECT uba.*, sb.vietqr_prefix, sb.short_name FROM user_bank_accounts uba JOIN simulated_banks sb ON uba.bank_id = sb.id WHERE uba.id = $1',
      [accountId]
    );

    if (!account) {
      throw new Error('Bank account not found');
    }

    // Generate VietQR payload
    const payload = this.generateVietQRPayload(
      account.vietqr_prefix,
      account.account_number,
      account.account_holder_name,
      amount,
      message
    );

    const expiresAt = new Date(Date.now() + 15 * 60 * 1000); // 15 minutes

    return {
      qrData: payload,
      qrImage: `https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=${encodeURIComponent(payload)}`,
      expiresAt
    };
  }

  private generateVietQRPayload(
    bankPrefix: string,
    accountNumber: string,
    accountHolderName: string,
    amount?: number,
    message?: string
  ): string {
    const format = '011200';
    const clientId = '970436'; // VietQR default
    const crc = '6304'; // CRC16 for VietQR
    
    const amountStr = amount ? amount.toString().padStart(12, '0') : '000000000000';
    const messageStr = message ? message.substring(0, 50).padEnd(50, ' ') : ' '.repeat(50);

    return [
      format,
      clientId,
      bankPrefix,
      accountNumber.padEnd(20, '0'),
      amountStr,
      messageStr,
      crc
    ].join('');
  }

  async getAccountBalance(accountId: string): Promise<BankBalance | null> {
    const account = await queryOne(
      'SELECT account_number, balance FROM user_bank_accounts WHERE id = $1 AND is_active = true',
      [accountId]
    );

    if (!account) {
      return null;
    }

    return {
      accountNumber: account.account_number,
      balance: parseFloat(account.balance),
      lastUpdated: new Date()
    };
  }
}
