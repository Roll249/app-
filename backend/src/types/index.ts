// ============================================================
// ENTITY TYPES - Database entity definitions
// ============================================================

export interface User {
  id: string;
  email: string;
  passwordHash: string;
  fullName: string;
  avatarUrl?: string;
  phone?: string;
  dateOfBirth?: string;
  isActive: boolean;
  isVerified: boolean;
  createdAt: number;
  updatedAt: number;
}

export interface RefreshToken {
  id: string;
  userId: string;
  token: string;
  deviceInfo?: string;
  ipAddress?: string;
  expiresAt: number;
  createdAt: number;
  revokedAt?: number;
}

export interface SimulatedBank {
  id: string;
  code: string;
  name: string;
  shortName?: string;
  logoUrl?: string;
  vietqrPrefix?: string;
  swiftCode?: string;
  initialBalance: string;
  isActive: boolean;
  createdAt: number;
}

export interface UserBankAccount {
  id: string;
  userId: string;
  bankId: string;
  accountNumber: string;
  accountHolderName: string;
  balance: string;
  isActive: boolean;
  linkedAt: number;
}

export interface Category {
  id: string;
  userId?: string;
  name: string;
  icon?: string;
  color?: string;
  type: 'INCOME' | 'EXPENSE';
  parentId?: string;
  isSystem: boolean;
  isActive: boolean;
  sortOrder: number;
  createdAt: number;
}

export interface Account {
  id: string;
  userId: string;
  name: string;
  type: 'CASH' | 'BANK' | 'WALLET' | 'SAVINGS';
  icon?: string;
  color?: string;
  initialBalance: string;
  currentBalance: string;
  currency: string;
  includeInTotal: boolean;
  isActive: boolean;
  bankAccountId?: string;
  createdAt: number;
  updatedAt: number;
}

export interface Fund {
  id: string;
  userId: string;
  name: string;
  icon?: string;
  color?: string;
  description?: string;
  targetAmount?: string;
  currentAmount: string;
  progress: number;
  startDate?: string;
  endDate?: string;
  isActive: boolean;
  createdAt: number;
  updatedAt: number;
}

export interface Transaction {
  id: string;
  userId: string;
  accountId: string;
  categoryId?: string;
  type: 'INCOME' | 'EXPENSE' | 'TRANSFER';
  amount: string;
  currency: string;
  description?: string;
  note?: string;
  date: number;
  sourceType?: string;
  referenceId?: string;
  relatedTransactionId?: string;
  isActive: boolean;
  createdAt: number;
  updatedAt: number;
}

export interface Budget {
  id: string;
  userId: string;
  categoryId?: string;
  name: string;
  amount: string;
  spentAmount: string;
  remainingAmount: string;
  progress: number;
  period: 'WEEKLY' | 'MONTHLY' | 'YEARLY';
  startDate: string;
  endDate?: string;
  isActive: boolean;
  createdAt: number;
}

export interface QRCode {
  id: string;
  userId: string;
  type: 'RECEIVE' | 'TRANSFER';
  payload: string;
  signature: string;
  amount?: string;
  message?: string;
  accountId?: string;
  bankAccountId?: string;
  expiresAt: number;
  usedAt?: number;
  usedBy?: string;
  isActive: boolean;
  createdAt: number;
}

export interface DemoUser {
  id: string;
  userId: string;
  simulationEnabled: boolean;
  salaryDay: number;
  autoAllocationEnabled: boolean;
  createdAt: number;
  updatedAt: number;
}

// ============================================================
// DTO TYPES
// ============================================================

export interface ApiResponse<T = any> {
  success: boolean;
  data?: T;
  message?: string;
  meta?: {
    page?: number;
    pageSize?: number;
    total?: number;
    totalPages?: number;
  };
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  user: {
    id: string;
    email: string;
    fullName?: string;
    avatarUrl?: string;
    phone?: string;
    isVerified: boolean;
    createdAt: string;
  };
}

// Transaction with joined data
export interface TransactionWithDetails extends Transaction {
  accountName?: string;
  categoryName?: string;
  categoryIcon?: string;
  categoryColor?: string;
}

// Account with bank info
export interface AccountWithBank extends Account {
  bank?: SimulatedBank;
}
