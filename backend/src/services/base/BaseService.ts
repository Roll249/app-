/**
 * Base Service Interface
 * All external services should implement this interface
 */
export interface IService {
  /** Initialize the service (connect to external API, authenticate, etc.) */
  initialize(): Promise<void>;
  
  /** Check if service is available */
  isAvailable(): Promise<boolean>;
  
  /** Get service health status */
  getHealth(): Promise<ServiceHealth>;
  
  /** Get service name */
  getName(): string;
}

export interface ServiceHealth {
  status: 'healthy' | 'degraded' | 'offline';
  latencyMs?: number;
  message?: string;
  lastChecked: Date;
}

export interface ServiceConfig {
  baseUrl: string;
  apiKey?: string;
  timeout: number;
  retryAttempts: number;
}

export abstract class BaseService implements IService {
  protected config: ServiceConfig;
  protected logger: any;
  protected isInitialized: boolean = false;

  constructor(config: ServiceConfig, logger: any) {
    this.config = config;
    this.logger = logger;
  }

  abstract getName(): string;
  
  abstract initialize(): Promise<void>;
  
  async isAvailable(): Promise<boolean> {
    try {
      const health = await this.getHealth();
      return health.status !== 'offline';
    } catch {
      return false;
    }
  }

  async getHealth(): Promise<ServiceHealth> {
    const startTime = Date.now();
    try {
      const isUp = await this.ping();
      return {
        status: isUp ? 'healthy' : 'offline',
        latencyMs: Date.now() - startTime,
        lastChecked: new Date()
      };
    } catch (error: any) {
      return {
        status: 'offline',
        latencyMs: Date.now() - startTime,
        message: error.message,
        lastChecked: new Date()
      };
    }
  }

  /** Override this in subclasses to implement health check */
  protected async ping(): Promise<boolean> {
    return true;
  }

  /** Helper method for making HTTP requests with retry logic */
  protected async fetchWithRetry<T>(
    url: string,
    options: RequestInit,
    retryCount: number = 0
  ): Promise<T> {
    const controller = new AbortController();
    const timeout = setTimeout(() => controller.abort(), this.config.timeout);

    try {
      const response = await fetch(url, {
        ...options,
        signal: controller.signal
      });
      clearTimeout(timeout);

      if (!response.ok && retryCount < this.config.retryAttempts) {
        const delay = Math.pow(2, retryCount) * 1000;
        await this.sleep(delay);
        return this.fetchWithRetry<T>(url, options, retryCount + 1);
      }

      return response.json();
    } catch (error: any) {
      clearTimeout(timeout);
      if (retryCount < this.config.retryAttempts) {
        const delay = Math.pow(2, retryCount) * 1000;
        await this.sleep(delay);
        return this.fetchWithRetry<T>(url, options, retryCount + 1);
      }
      throw error;
    }
  }

  protected sleep(ms: number): Promise<void> {
    return new Promise(resolve => setTimeout(resolve, ms));
  }
}
