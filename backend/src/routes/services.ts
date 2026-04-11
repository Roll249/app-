/**
 * Services Routes - External Services Management API
 * 
 * This route provides endpoints to:
 * - Get health status of all services
 * - Get specific service status
 * - Configure service parameters
 */
import { getServiceRegistry } from '../services/index.js';

export async function servicesRoutes(fastify: any) {
  const getRegistry = () => getServiceRegistry(fastify.log);

  // ========================================
  // Get all services health status
  // ========================================
  fastify.get('/', async (request: any, reply: any) => {
    try {
      const registry = getRegistry();
      const healthSummary = await registry.getHealthSummary();

      return reply.send({
        success: true,
        data: healthSummary
      });
    } catch (error: any) {
      fastify.log.error('Error getting services status:', error.message);
      return reply.status(500).send({
        success: false,
        message: `Failed to get services status: ${error.message}`
      });
    }
  });

  // ========================================
  // Get AI service status
  // ========================================
  fastify.get('/ai', async (request: any, reply: any) => {
    try {
      const registry = getRegistry();
      const aiService = registry.getAIService();
      const status = await aiService.checkStatus();
      const health = await aiService.getHealth();

      return reply.send({
        success: true,
        data: {
          name: aiService.getName(),
          status: status.status,
          models: status.models,
          url: status.url,
          message: status.message,
          health
        }
      });
    } catch (error: any) {
      fastify.log.error('Error getting AI status:', error.message);
      return reply.status(500).send({
        success: false,
        message: `Failed to get AI status: ${error.message}`
      });
    }
  });

  // ========================================
  // Get Banking service status
  // ========================================
  fastify.get('/banking', async (request: any, reply: any) => {
    try {
      const registry = getRegistry();
      const bankingService = registry.getBankingService();
      const health = await bankingService.getHealth();
      const banks = await bankingService.getBanks();

      return reply.send({
        success: true,
        data: {
          name: bankingService.getName(),
          health,
          banks: banks.map(b => ({
            code: b.code,
            name: b.name,
            shortName: b.shortName
          }))
        }
      });
    } catch (error: any) {
      fastify.log.error('Error getting banking status:', error.message);
      return reply.status(500).send({
        success: false,
        message: `Failed to get banking status: ${error.message}`
      });
    }
  });

  // ========================================
  // Get Notification service status
  // ========================================
  fastify.get('/notification', async (request: any, reply: any) => {
    try {
      const registry = getRegistry();
      const notificationService = registry.getNotificationService();
      const health = await notificationService.getHealth();

      return reply.send({
        success: true,
        data: {
          name: notificationService.getName(),
          health,
          capabilities: {
            push: true,
            email: true,
            sms: true
          }
        }
      });
    } catch (error: any) {
      fastify.log.error('Error getting notification status:', error.message);
      return reply.status(500).send({
        success: false,
        message: `Failed to get notification status: ${error.message}`
      });
    }
  });

  // ========================================
  // Get Sync service status
  // ========================================
  fastify.get('/sync', async (request: any, reply: any) => {
    try {
      const registry = getRegistry();
      const syncService = registry.getSyncService();
      const health = await syncService.getHealth();
      const userId = request.query.userId || 'demo';
      const syncStatus = await syncService.getSyncStatus(userId);

      return reply.send({
        success: true,
        data: {
          name: syncService.getName(),
          health,
          userStatus: syncStatus
        }
      });
    } catch (error: any) {
      fastify.log.error('Error getting sync status:', error.message);
      return reply.status(500).send({
        success: false,
        message: `Failed to get sync status: ${error.message}`
      });
    }
  });

  // ========================================
  // Manual sync trigger
  // ========================================
  fastify.post('/sync/trigger', async (request: any, reply: any) => {
    try {
      const { userId } = request.body;
      if (!userId) {
        return reply.status(400).send({
          success: false,
          message: 'userId is required'
        });
      }

      const registry = getRegistry();
      const syncService = registry.getSyncService();
      const result = await syncService.sync(userId);

      return reply.send({
        success: true,
        data: result
      });
    } catch (error: any) {
      fastify.log.error('Error triggering sync:', error.message);
      return reply.status(500).send({
        success: false,
        message: `Failed to trigger sync: ${error.message}`
      });
    }
  });

  // ========================================
  // Send test notification
  // ========================================
  fastify.post('/notification/test', async (request: any, reply: any) => {
    try {
      const { userId, type, title, body } = request.body;
      if (!userId) {
        return reply.status(400).send({
          success: false,
          message: 'userId is required'
        });
      }

      const registry = getRegistry();
      const notificationService = registry.getNotificationService();
      const result = await notificationService.sendNotification(
        { userId },
        {
          title: title || 'Test Notification',
          body: body || 'This is a test notification from the services API',
          priority: 'HIGH'
        }
      );

      return reply.send({
        success: true,
        data: result
      });
    } catch (error: any) {
      fastify.log.error('Error sending test notification:', error.message);
      return reply.status(500).send({
        success: false,
        message: `Failed to send notification: ${error.message}`
      });
    }
  });
}
