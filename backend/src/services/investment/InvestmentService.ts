/**
 * Investment Service - External Service Integration
 * Handles investment portfolio management and recommendations
 * 
 * This service manages:
 * - Investment portfolio tracking
 * - Investment recommendations
 * - Risk analysis
 * - Performance reporting
 */
import { BaseService, ServiceConfig, ServiceHealth } from '../base/BaseService.js';
import { query } from '../../utils/db.js';

export interface InvestmentType {
  STOCK: 'STOCK';
  BOND: 'BOND';
  MUTUAL_FUND: 'MUTUAL_FUND';
  ETF: 'ETF';
  CRYPTO: 'CRYPTO';
  REAL_ESTATE: 'REAL_ESTATE';
  TERM_DEPOSIT: 'TERM_DEPOSIT';
  SAVINGS_PLAN: 'SAVINGS_PLAN';
}

export interface Investment {
  id: string;
  userId: string;
  name: string;
  type: keyof InvestmentType;
  symbol?: string;
  purchasePrice: number;
  currentPrice: number;
  quantity: number;
  totalValue: number;
  profitLoss: number;
  profitLossPercent: number;
  purchaseDate: Date;
  notes?: string;
}

export interface InvestmentPortfolio {
  totalInvested: number;
  totalCurrentValue: number;
  totalProfitLoss: number;
  totalProfitLossPercent: number;
  investments: Investment[];
  allocation: Allocation[];
}

export interface Allocation {
  type: keyof InvestmentType;
  value: number;
  percentage: number;
}

export interface InvestmentRecommendation {
  id: string;
  type: 'BUY' | 'SELL' | 'HOLD';
  asset: string;
  reason: string;
  expectedReturn: number;
  riskLevel: 'LOW' | 'MEDIUM' | 'HIGH';
  timeframe: string;
}

export interface RiskProfile {
  riskTolerance: 'CONSERVATIVE' | 'MODERATE' | 'AGGRESSIVE';
  recommendedAllocation: Allocation[];
  maxDrawdown: number;
}

export interface InvestmentPerformance {
  period: string;
  totalReturn: number;
  annualizedReturn: number;
  benchmark: number;
  vsBenchmark: number;
}

export class InvestmentService extends BaseService {
  constructor(config: ServiceConfig, logger: any) {
    super(config, logger);
  }

  getName(): string {
    return 'InvestmentService';
  }

  async initialize(): Promise<void> {
    this.logger.info(`[${this.getName()}] Initializing investment service...`);
    this.isInitialized = true;
    this.logger.info(`[${this.getName()}] Investment service initialized`);
  }

  protected async ping(): Promise<boolean> {
    return this.isInitialized;
  }

  async getHealth(): Promise<ServiceHealth> {
    return {
      status: 'healthy',
      latencyMs: 0,
      message: 'Investment service is ready',
      lastChecked: new Date()
    };
  }

  async getUserPortfolio(userId: string): Promise<InvestmentPortfolio> {
    const investments = await query(
      'SELECT * FROM investments WHERE user_id = $1 AND is_active = true',
      [userId]
    );

    let totalInvested = 0;
    let totalCurrentValue = 0;
    const investmentList: Investment[] = [];
    const allocationMap: Record<string, number> = {};

    for (const inv of investments) {
      const currentValue = parseFloat(inv.current_price) * inv.quantity;
      const invested = parseFloat(inv.purchase_price) * inv.quantity;
      const profitLoss = currentValue - invested;
      const profitLossPercent = invested > 0 ? (profitLoss / invested) * 100 : 0;

      totalInvested += invested;
      totalCurrentValue += currentValue;

      investmentList.push({
        id: inv.id,
        userId: inv.user_id,
        name: inv.name,
        type: inv.type,
        symbol: inv.symbol,
        purchasePrice: parseFloat(inv.purchase_price),
        currentPrice: parseFloat(inv.current_price),
        quantity: inv.quantity,
        totalValue: currentValue,
        profitLoss,
        profitLossPercent,
        purchaseDate: new Date(inv.purchase_date * 1000),
        notes: inv.notes
      });

      allocationMap[inv.type] = (allocationMap[inv.type] || 0) + currentValue;
    }

    const totalProfitLoss = totalCurrentValue - totalInvested;
    const totalProfitLossPercent = totalInvested > 0 ? (totalProfitLoss / totalInvested) * 100 : 0;

    const allocation: Allocation[] = Object.entries(allocationMap).map(([type, value]) => ({
      type: type as keyof InvestmentType,
      value,
      percentage: totalCurrentValue > 0 ? (value / totalCurrentValue) * 100 : 0
    }));

    return {
      totalInvested,
      totalCurrentValue,
      totalProfitLoss,
      totalProfitLossPercent,
      investments: investmentList,
      allocation
    };
  }

  async addInvestment(
    userId: string,
    investment: Omit<Investment, 'id' | 'userId' | 'totalValue' | 'profitLoss' | 'profitLossPercent'>
  ): Promise<Investment> {
    const { v4: uuidv4 } = require('uuid');
    const id = uuidv4();

    await query(
      `INSERT INTO investments (id, user_id, name, type, symbol, purchase_price, current_price, quantity, purchase_date, notes, is_active)
       VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, true)`,
      [
        id,
        userId,
        investment.name,
        investment.type,
        investment.symbol || null,
        investment.purchasePrice,
        investment.currentPrice,
        investment.quantity,
        Math.floor(investment.purchaseDate.getTime() / 1000),
        investment.notes || null
      ]
    );

    return {
      id,
      userId,
      ...investment,
      totalValue: investment.purchasePrice * investment.quantity,
      profitLoss: 0,
      profitLossPercent: 0
    };
  }

  async updateInvestmentPrice(investmentId: string, currentPrice: number): Promise<void> {
    await query(
      'UPDATE investments SET current_price = $1 WHERE id = $2',
      [currentPrice, investmentId]
    );
  }

  async deleteInvestment(investmentId: string): Promise<void> {
    await query(
      'UPDATE investments SET is_active = false WHERE id = $1',
      [investmentId]
    );
  }

  async getRecommendations(userId: string): Promise<InvestmentRecommendation[]> {
    const portfolio = await this.getUserPortfolio(userId);

    // Simple recommendation logic based on allocation
    const recommendations: InvestmentRecommendation[] = [];

    // Check for high-risk concentration
    const cryptoAllocation = portfolio.allocation.find(a => a.type === 'CRYPTO');
    if (cryptoAllocation && cryptoAllocation.percentage > 20) {
      recommendations.push({
        id: '1',
        type: 'SELL',
        asset: 'Cryptocurrency',
        reason: 'Phân bổ crypto quá cao (>20%). Cân nhắc chốt lời một phần để giảm rủi ro.',
        expectedReturn: -5,
        riskLevel: 'HIGH',
        timeframe: '1-3 tháng'
      });
    }

    // Check for missing diversification
    if (portfolio.allocation.length < 3) {
      recommendations.push({
        id: '2',
        type: 'BUY',
        asset: 'Đa dạng hóa',
        reason: 'Danh mục thiếu đa dạng. Cân nhắc thêm các loại tài sản khác như trái phiếu hoặc vàng.',
        expectedReturn: 8,
        riskLevel: 'MEDIUM',
        timeframe: '3-6 tháng'
      });
    }

    // Savings plan recommendation
    const hasSavingsPlan = portfolio.allocation.some(a => a.type === 'SAVINGS_PLAN');
    if (!hasSavingsPlan && portfolio.totalCurrentValue > 50000000) {
      recommendations.push({
        id: '3',
        type: 'BUY',
        asset: 'Savings Plan',
        reason: 'Bạn có thể bắt đầu một kế hoạch tiết kiệm định kỳ để tích lũy dài hạn.',
        expectedReturn: 6,
        riskLevel: 'LOW',
        timeframe: '>12 tháng'
      });
    }

    return recommendations;
  }

  async getRiskProfile(userId: string): Promise<RiskProfile> {
    const portfolio = await this.getUserPortfolio(userId);

    // Calculate risk based on allocation
    let riskScore = 0;
    for (const alloc of portfolio.allocation) {
      switch (alloc.type) {
        case 'CRYPTO':
          riskScore += alloc.percentage * 1.5;
          break;
        case 'STOCK':
        case 'ETF':
          riskScore += alloc.percentage * 1.2;
          break;
        case 'MUTUAL_FUND':
          riskScore += alloc.percentage * 1.0;
          break;
        case 'BOND':
        case 'TERM_DEPOSIT':
        case 'SAVINGS_PLAN':
          riskScore += alloc.percentage * 0.3;
          break;
      }
    }

    let riskTolerance: 'CONSERVATIVE' | 'MODERATE' | 'AGGRESSIVE';
    let recommendedAllocation: Allocation[];

    if (riskScore < 30) {
      riskTolerance = 'CONSERVATIVE';
      recommendedAllocation = [
        { type: 'TERM_DEPOSIT', value: 0, percentage: 40 },
        { type: 'BOND', value: 0, percentage: 30 },
        { type: 'SAVINGS_PLAN', value: 0, percentage: 20 },
        { type: 'STOCK', value: 0, percentage: 10 }
      ];
    } else if (riskScore < 60) {
      riskTolerance = 'MODERATE';
      recommendedAllocation = [
        { type: 'STOCK', value: 0, percentage: 30 },
        { type: 'ETF', value: 0, percentage: 20 },
        { type: 'BOND', value: 0, percentage: 25 },
        { type: 'SAVINGS_PLAN', value: 0, percentage: 15 },
        { type: 'CRYPTO', value: 0, percentage: 10 }
      ];
    } else {
      riskTolerance = 'AGGRESSIVE';
      recommendedAllocation = [
        { type: 'STOCK', value: 0, percentage: 40 },
        { type: 'CRYPTO', value: 0, percentage: 20 },
        { type: 'ETF', value: 0, percentage: 20 },
        { type: 'BOND', value: 0, percentage: 10 },
        { type: 'REAL_ESTATE', value: 0, percentage: 10 }
      ];
    }

    return {
      riskTolerance,
      recommendedAllocation,
      maxDrawdown: riskScore > 60 ? 30 : riskScore > 30 ? 15 : 5
    };
  }

  async getPerformance(
    userId: string,
    period: '1M' | '3M' | '6M' | '1Y' | 'ALL' = '1Y'
  ): Promise<InvestmentPerformance> {
    const portfolio = await this.getUserPortfolio(userId);

    // Simulated performance data
    let totalReturn = portfolio.totalProfitLossPercent;
    let annualizedReturn: number;
    const benchmark = 8.5; // Assume 8.5% annual benchmark

    switch (period) {
      case '1M':
        totalReturn = portfolio.totalProfitLossPercent * 0.083;
        annualizedReturn = totalReturn * 12;
        break;
      case '3M':
        totalReturn = portfolio.totalProfitLossPercent * 0.25;
        annualizedReturn = totalReturn * 4;
        break;
      case '6M':
        totalReturn = portfolio.totalProfitLossPercent * 0.5;
        annualizedReturn = totalReturn * 2;
        break;
      case '1Y':
        annualizedReturn = portfolio.totalProfitLossPercent;
        break;
      case 'ALL':
        annualizedReturn = portfolio.totalProfitLossPercent * 0.8;
        break;
    }

    return {
      period,
      totalReturn,
      annualizedReturn,
      benchmark: benchmark * (period === '1M' ? 0.083 : period === '3M' ? 0.25 : period === '6M' ? 0.5 : period === '1Y' ? 1 : 3),
      vsBenchmark: annualizedReturn - benchmark
    };
  }
}
