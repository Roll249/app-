/**
 * Market Routes - External Market Data API
 * 
 * This route provides endpoints to:
 * - Get exchange rates
 * - Get gold prices
 * - Get stock indices
 * - Get crypto prices
 */
import { getServiceRegistry } from '../services/index.js';

export async function marketRoutes(fastify: any) {
  const getMarketService = () => getServiceRegistry(fastify.log).getMarketService();

  // ========================================
  // Get all market data
  // ========================================
  fastify.get('/', async (request: any, reply: any) => {
    try {
      const marketService = getMarketService();
      const data = await marketService.getAllMarketData();

      return reply.send({
        success: true,
        data
      });
    } catch (error: any) {
      fastify.log.error('Error getting market data:', error.message);
      return reply.status(500).send({
        success: false,
        message: `Failed to get market data: ${error.message}`
      });
    }
  });

  // ========================================
  // Get exchange rates
  // ========================================
  fastify.get('/exchange-rates', async (request: any, reply: any) => {
    try {
      const marketService = getMarketService();
      const rates = await marketService.getExchangeRates();

      return reply.send({
        success: true,
        data: rates
      });
    } catch (error: any) {
      fastify.log.error('Error getting exchange rates:', error.message);
      return reply.status(500).send({
        success: false,
        message: `Failed to get exchange rates: ${error.message}`
      });
    }
  });

  // ========================================
  // Get gold prices
  // ========================================
  fastify.get('/gold-prices', async (request: any, reply: any) => {
    try {
      const marketService = getMarketService();
      const prices = await marketService.getGoldPrices();

      return reply.send({
        success: true,
        data: prices
      });
    } catch (error: any) {
      fastify.log.error('Error getting gold prices:', error.message);
      return reply.status(500).send({
        success: false,
        message: `Failed to get gold prices: ${error.message}`
      });
    }
  });

  // ========================================
  // Get stock indices
  // ========================================
  fastify.get('/stock-indices', async (request: any, reply: any) => {
    try {
      const marketService = getMarketService();
      const indices = await marketService.getStockIndices();

      return reply.send({
        success: true,
        data: indices
      });
    } catch (error: any) {
      fastify.log.error('Error getting stock indices:', error.message);
      return reply.status(500).send({
        success: false,
        message: `Failed to get stock indices: ${error.message}`
      });
    }
  });

  // ========================================
  // Get crypto prices
  // ========================================
  fastify.get('/crypto-prices', async (request: any, reply: any) => {
    try {
      const marketService = getMarketService();
      const prices = await marketService.getCryptoPrices();

      return reply.send({
        success: true,
        data: prices
      });
    } catch (error: any) {
      fastify.log.error('Error getting crypto prices:', error.message);
      return reply.status(500).send({
        success: false,
        message: `Failed to get crypto prices: ${error.message}`
      });
    }
  });

  // ========================================
  // Convert currency
  // ========================================
  fastify.get('/convert', async (request: any, reply: any) => {
    try {
      const { amount, from, to } = request.query;

      if (!amount || !from || !to) {
        return reply.status(400).send({
          success: false,
          message: 'amount, from, and to are required'
        });
      }

      const marketService = getMarketService();
      const result = await marketService.convertCurrency(
        parseFloat(amount),
        from.toUpperCase(),
        to.toUpperCase()
      );

      return reply.send({
        success: true,
        data: {
          amount: parseFloat(amount),
          from: from.toUpperCase(),
          to: to.toUpperCase(),
          result
        }
      });
    } catch (error: any) {
      fastify.log.error('Error converting currency:', error.message);
      return reply.status(500).send({
        success: false,
        message: `Failed to convert currency: ${error.message}`
      });
    }
  });
}
