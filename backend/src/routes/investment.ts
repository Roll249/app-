/**
 * Investment Routes - Investment Portfolio API
 * 
 * This route provides endpoints to:
 * - Get user investment portfolio
 * - Add/update/delete investments
 * - Get recommendations
 * - Get risk profile
 */
import { getServiceRegistry } from '../services/index.js';

export async function investmentRoutes(fastify: any) {
  // ========================================
  // Get user portfolio
  // ========================================
  fastify.get('/portfolio', async (request: any, reply: any) => {
    try {
      const userId = request.user?.userId || 'demo';
      const investmentService = getServiceRegistry(fastify.log).getInvestmentService();
      const portfolio = await investmentService.getUserPortfolio(userId);

      return reply.send({
        success: true,
        data: portfolio
      });
    } catch (error: any) {
      fastify.log.error('Error getting portfolio:', error.message);
      return reply.status(500).send({
        success: false,
        message: `Failed to get portfolio: ${error.message}`
      });
    }
  });

  // ========================================
  // Add investment
  // ========================================
  fastify.post('/investments', async (request: any, reply: any) => {
    try {
      const userId = request.user?.userId || 'demo';
      const investmentService = getServiceRegistry(fastify.log).getInvestmentService();
      
      const { name, type, symbol, purchasePrice, currentPrice, quantity, purchaseDate, notes } = request.body;

      if (!name || !type || !purchasePrice || !quantity) {
        return reply.status(400).send({
          success: false,
          message: 'name, type, purchasePrice, and quantity are required'
        });
      }

      const investment = await investmentService.addInvestment(userId, {
        name,
        type,
        symbol,
        purchasePrice: parseFloat(purchasePrice),
        currentPrice: parseFloat(currentPrice || purchasePrice),
        quantity: parseFloat(quantity),
        purchaseDate: new Date(purchaseDate || Date.now()),
        notes
      });

      return reply.send({
        success: true,
        data: investment
      });
    } catch (error: any) {
      fastify.log.error('Error adding investment:', error.message);
      return reply.status(500).send({
        success: false,
        message: `Failed to add investment: ${error.message}`
      });
    }
  });

  // ========================================
  // Update investment price
  // ========================================
  fastify.put('/investments/:id/price', async (request: any, reply: any) => {
    try {
      const { id } = request.params;
      const { currentPrice } = request.body;

      if (!id || !currentPrice) {
        return reply.status(400).send({
          success: false,
          message: 'id and currentPrice are required'
        });
      }

      const investmentService = getServiceRegistry(fastify.log).getInvestmentService();
      await investmentService.updateInvestmentPrice(id, parseFloat(currentPrice));

      return reply.send({
        success: true,
        message: 'Investment price updated'
      });
    } catch (error: any) {
      fastify.log.error('Error updating investment price:', error.message);
      return reply.status(500).send({
        success: false,
        message: `Failed to update investment price: ${error.message}`
      });
    }
  });

  // ========================================
  // Delete investment
  // ========================================
  fastify.delete('/investments/:id', async (request: any, reply: any) => {
    try {
      const { id } = request.params;

      if (!id) {
        return reply.status(400).send({
          success: false,
          message: 'id is required'
        });
      }

      const investmentService = getServiceRegistry(fastify.log).getInvestmentService();
      await investmentService.deleteInvestment(id);

      return reply.send({
        success: true,
        message: 'Investment deleted'
      });
    } catch (error: any) {
      fastify.log.error('Error deleting investment:', error.message);
      return reply.status(500).send({
        success: false,
        message: `Failed to delete investment: ${error.message}`
      });
    }
  });

  // ========================================
  // Get recommendations
  // ========================================
  fastify.get('/recommendations', async (request: any, reply: any) => {
    try {
      const userId = request.user?.userId || 'demo';
      const investmentService = getServiceRegistry(fastify.log).getInvestmentService();
      const recommendations = await investmentService.getRecommendations(userId);

      return reply.send({
        success: true,
        data: recommendations
      });
    } catch (error: any) {
      fastify.log.error('Error getting recommendations:', error.message);
      return reply.status(500).send({
        success: false,
        message: `Failed to get recommendations: ${error.message}`
      });
    }
  });

  // ========================================
  // Get risk profile
  // ========================================
  fastify.get('/risk-profile', async (request: any, reply: any) => {
    try {
      const userId = request.user?.userId || 'demo';
      const investmentService = getServiceRegistry(fastify.log).getInvestmentService();
      const riskProfile = await investmentService.getRiskProfile(userId);

      return reply.send({
        success: true,
        data: riskProfile
      });
    } catch (error: any) {
      fastify.log.error('Error getting risk profile:', error.message);
      return reply.status(500).send({
        success: false,
        message: `Failed to get risk profile: ${error.message}`
      });
    }
  });

  // ========================================
  // Get performance
  // ========================================
  fastify.get('/performance', async (request: any, reply: any) => {
    try {
      const userId = request.user?.userId || 'demo';
      const { period = '1Y' } = request.query;
      
      const investmentService = getServiceRegistry(fastify.log).getInvestmentService();
      const performance = await investmentService.getPerformance(userId, period);

      return reply.send({
        success: true,
        data: performance
      });
    } catch (error: any) {
      fastify.log.error('Error getting performance:', error.message);
      return reply.status(500).send({
        success: false,
        message: `Failed to get performance: ${error.message}`
      });
    }
  });
}
