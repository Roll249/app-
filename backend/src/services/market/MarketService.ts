/**
 * Market Service - External Service Integration
 * Handles communication with financial market data APIs
 * 
 * This service fetches:
 * - Exchange rates (USD, EUR, JPY, etc.)
 * - Gold prices
 * - Stock market indices
 * - Cryptocurrency prices
 */
import { BaseService, ServiceConfig, ServiceHealth } from '../base/BaseService.js';

export interface ExchangeRate {
  currency: string;
  currencyName: string;
  buyRate: number;
  sellRate: number;
  transferRate: number;
  lastUpdated: Date;
}

export interface GoldPrice {
  type: string;
  buyPrice: number;
  sellPrice: number;
  lastUpdated: Date;
}

export interface StockIndex {
  name: string;
  code: string;
  currentValue: number;
  change: number;
  changePercent: number;
  lastUpdated: Date;
}

export interface CryptoPrice {
  symbol: string;
  name: string;
  price: number;
  change24h: number;
  changePercent24h: number;
  lastUpdated: Date;
}

export interface MarketData {
  exchangeRates: ExchangeRate[];
  goldPrices: GoldPrice[];
  stockIndices: StockIndex[];
  cryptoPrices: CryptoPrice[];
}

export class MarketService extends BaseService {
  private cache: Map<string, { data: any; expiresAt: number }> = new Map();
  private cacheDuration = 5 * 60 * 1000; // 5 minutes default

  constructor(config: ServiceConfig, logger: any) {
    super(config, logger);
  }

  getName(): string {
    return 'MarketService';
  }

  async initialize(): Promise<void> {
    this.logger.info(`[${this.getName()}] Initializing market service...`);
    
    // Load cache duration from config
    const cacheDurationEnv = process.env.MARKET_CACHE_DURATION;
    if (cacheDurationEnv) {
      this.cacheDuration = parseInt(cacheDurationEnv) * 1000;
    }

    this.isInitialized = true;
    this.logger.info(`[${this.getName()}] Market service initialized with ${this.cacheDuration / 1000}s cache`);
  }

  protected async ping(): Promise<boolean> {
    return true;
  }

  async getHealth(): Promise<ServiceHealth> {
    const startTime = Date.now();

    try {
      // Check if we have cached data
      const cacheKey = 'market_data';
      const cached = this.cache.get(cacheKey);
      const hasData = cached && cached.expiresAt > Date.now();

      return {
        status: 'healthy',
        latencyMs: Date.now() - startTime,
        message: hasData ? 'Data available' : 'Cache empty, will fetch',
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

  private getCached<T>(key: string): T | null {
    const cached = this.cache.get(key);
    if (cached && cached.expiresAt > Date.now()) {
      return cached.data as T;
    }
    return null;
  }

  private setCache(key: string, data: any): void {
    this.cache.set(key, {
      data,
      expiresAt: Date.now() + this.cacheDuration
    });
  }

  async getExchangeRates(): Promise<ExchangeRate[]> {
    // Check cache first
    const cached = this.getCached<ExchangeRate[]>('exchange_rates');
    if (cached) {
      return cached;
    }

    try {
      // In production, this would call a real API like:
      // - Vietcombank API
      // - Free Currency Converter API
      // - Open Exchange Rates API
      
      // For now, return simulated data
      const rates: ExchangeRate[] = [
        {
          currency: 'USD',
          currencyName: 'US Dollar',
          buyRate: 25450,
          sellRate: 25750,
          transferRate: 25600,
          lastUpdated: new Date()
        },
        {
          currency: 'EUR',
          currencyName: 'Euro',
          buyRate: 27800,
          sellRate: 28500,
          transferRate: 28150,
          lastUpdated: new Date()
        },
        {
          currency: 'JPY',
          currencyName: 'Japanese Yen',
          buyRate: 168,
          sellRate: 175,
          transferRate: 172,
          lastUpdated: new Date()
        },
        {
          currency: 'GBP',
          currencyName: 'British Pound',
          buyRate: 32500,
          sellRate: 33500,
          transferRate: 33000,
          lastUpdated: new Date()
        },
        {
          currency: 'SGD',
          currencyName: 'Singapore Dollar',
          buyRate: 18500,
          sellRate: 19200,
          transferRate: 18850,
          lastUpdated: new Date()
        },
        {
          currency: 'AUD',
          currencyName: 'Australian Dollar',
          buyRate: 16500,
          sellRate: 17200,
          transferRate: 16850,
          lastUpdated: new Date()
        },
        {
          currency: 'CAD',
          currencyName: 'Canadian Dollar',
          buyRate: 18500,
          sellRate: 19300,
          transferRate: 18900,
          lastUpdated: new Date()
        },
        {
          currency: 'CHF',
          currencyName: 'Swiss Franc',
          buyRate: 28500,
          sellRate: 29500,
          transferRate: 29000,
          lastUpdated: new Date()
        },
        {
          currency: 'HKD',
          currencyName: 'Hong Kong Dollar',
          buyRate: 3200,
          sellRate: 3350,
          transferRate: 3275,
          lastUpdated: new Date()
        },
        {
          currency: 'CNY',
          currencyName: 'Chinese Yuan',
          buyRate: 3450,
          sellRate: 3600,
          transferRate: 3525,
          lastUpdated: new Date()
        }
      ];

      this.setCache('exchange_rates', rates);
      return rates;
    } catch (error: any) {
      this.logger.error(`[${this.getName()}] Error fetching exchange rates: ${error.message}`);
      throw error;
    }
  }

  async getGoldPrices(): Promise<GoldPrice[]> {
    const cached = this.getCached<GoldPrice[]>('gold_prices');
    if (cached) {
      return cached;
    }

    try {
      // Simulated gold prices
      const prices: GoldPrice[] = [
        {
          type: 'SJC 1L',
          buyPrice: 78500000,
          sellPrice: 79300000,
          lastUpdated: new Date()
        },
        {
          type: 'SJC 5 cây',
          buyPrice: 392500000,
          sellPrice: 396500000,
          lastUpdated: new Date()
        },
        {
          type: 'SJC 1 chỉ',
          buyPrice: 7850000,
          sellPrice: 7930000,
          lastUpdated: new Date()
        },
        {
          type: 'Vàng 24K',
          buyPrice: 73500000,
          sellPrice: 75500000,
          lastUpdated: new Date()
        },
        {
          type: 'Vàng 18K',
          buyPrice: 54000000,
          sellPrice: 56000000,
          lastUpdated: new Date()
        }
      ];

      this.setCache('gold_prices', prices);
      return prices;
    } catch (error: any) {
      this.logger.error(`[${this.getName()}] Error fetching gold prices: ${error.message}`);
      throw error;
    }
  }

  async getStockIndices(): Promise<StockIndex[]> {
    const cached = this.getCached<StockIndex[]>('stock_indices');
    if (cached) {
      return cached;
    }

    try {
      // Simulated stock indices
      const indices: StockIndex[] = [
        {
          name: 'VN-Index',
          code: 'VNINDEX',
          currentValue: 1285.67,
          change: 15.32,
          changePercent: 1.21,
          lastUpdated: new Date()
        },
        {
          name: 'HNX-Index',
          code: 'HNX',
          currentValue: 235.89,
          change: -2.45,
          changePercent: -1.03,
          lastUpdated: new Date()
        },
        {
          name: 'UPCOM',
          code: 'UPCOM',
          currentValue: 92.34,
          change: 0.56,
          changePercent: 0.61,
          lastUpdated: new Date()
        }
      ];

      this.setCache('stock_indices', indices);
      return indices;
    } catch (error: any) {
      this.logger.error(`[${this.getName()}] Error fetching stock indices: ${error.message}`);
      throw error;
    }
  }

  async getCryptoPrices(): Promise<CryptoPrice[]> {
    const cached = this.getCached<CryptoPrice[]>('crypto_prices');
    if (cached) {
      return cached;
    }

    try {
      // Simulated crypto prices (prices in USD)
      const prices: CryptoPrice[] = [
        {
          symbol: 'BTC',
          name: 'Bitcoin',
          price: 67500,
          change24h: 1250,
          changePercent24h: 1.89,
          lastUpdated: new Date()
        },
        {
          symbol: 'ETH',
          name: 'Ethereum',
          price: 3450,
          change24h: -45,
          changePercent24h: -1.29,
          lastUpdated: new Date()
        },
        {
          symbol: 'BNB',
          name: 'Binance Coin',
          price: 580,
          change24h: 12,
          changePercent24h: 2.11,
          lastUpdated: new Date()
        },
        {
          symbol: 'SOL',
          name: 'Solana',
          price: 145,
          change24h: 5.5,
          changePercent24h: 3.94,
          lastUpdated: new Date()
        },
        {
          symbol: 'XRP',
          name: 'Ripple',
          price: 0.52,
          change24h: -0.02,
          changePercent24h: -3.70,
          lastUpdated: new Date()
        }
      ];

      this.setCache('crypto_prices', prices);
      return prices;
    } catch (error: any) {
      this.logger.error(`[${this.getName()}] Error fetching crypto prices: ${error.message}`);
      throw error;
    }
  }

  async getAllMarketData(): Promise<MarketData> {
    const [exchangeRates, goldPrices, stockIndices, cryptoPrices] = await Promise.all([
      this.getExchangeRates(),
      this.getGoldPrices(),
      this.getStockIndices(),
      this.getCryptoPrices()
    ]);

    return {
      exchangeRates,
      goldPrices,
      stockIndices,
      cryptoPrices
    };
  }

  async convertCurrency(
    amount: number,
    fromCurrency: string,
    toCurrency: string
  ): Promise<number> {
    const rates = await this.getExchangeRates();
    
    // Find the exchange rate for the target currency
    const targetRate = rates.find(r => r.currency === toCurrency);
    if (!targetRate) {
      throw new Error(`Exchange rate for ${toCurrency} not found`);
    }

    // If converting from VND, use sell rate
    if (fromCurrency === 'VND') {
      return amount / targetRate.sellRate;
    }

    // If converting to VND, use buy rate
    if (toCurrency === 'VND') {
      return amount * targetRate.buyRate;
    }

    // Cross-currency conversion through VND
    const amountInVND = amount * 
      (fromCurrency === 'USD' ? rates.find(r => r.currency === 'USD')!.transferRate : 1);
    return amountInVND / targetRate.transferRate;
  }

  clearCache(): void {
    this.cache.clear();
    this.logger.info(`[${this.getName()}] Cache cleared`);
  }
}
