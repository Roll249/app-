import 'dotenv/config';
import Fastify from 'fastify';
import cors from '@fastify/cors';
import jwt from '@fastify/jwt';
import rateLimit from '@fastify/rate-limit';
import { authRoutes } from './routes/auth.js';
import { userRoutes } from './routes/user.js';
import { accountRoutes } from './routes/account.js';
import { transactionRoutes } from './routes/transaction.js';
import { categoryRoutes } from './routes/category.js';
import { bankRoutes } from './routes/bank.js';
import { fundRoutes } from './routes/fund.js';
import { budgetRoutes } from './routes/budget.js';
import { qrRoutes } from './routes/qr.js';
import { reportRoutes } from './routes/report.js';
import { demoRoutes } from './routes/demo.js';
import { aiRoutes } from './routes/ai.js';
import { servicesRoutes } from './routes/services.js';
import { marketRoutes } from './routes/market.js';
import { investmentRoutes } from './routes/investment.js';
import { savingsGoalRoutes } from './routes/savingsGoal.js';
import { initDatabase } from './utils/db.js';
import { getServiceRegistry } from './services/index.js';

declare module 'fastify' {
  interface FastifyInstance {
    authenticate: (request: any, reply: any) => Promise<void>;
  }
}

interface AIModel {
  name: string;
  modified_at: string;
}

const PORT = parseInt(process.env.PORT || '3000');

async function start() {
  const fastify = Fastify({
    logger: { level: 'info', transport: { target: 'pino-pretty', options: { colorize: true } } }
  });

  await fastify.register(cors, { origin: true, credentials: true });
  await fastify.register(jwt, { secret: process.env.JWT_SECRET || 'dev-secret-change-me' });
  await fastify.register(rateLimit, { max: 100, timeWindow: '1 minute' });

  fastify.decorate('authenticate', async (request: any, reply: any) => {
    try {
      await request.jwtVerify();
    } catch (err) {
      reply.status(401).send({ success: false, message: 'Unauthorized' });
    }
  });

  await initDatabase();

  // Initialize services
  const serviceRegistry = getServiceRegistry(fastify.log);
  await serviceRegistry.initialize();

  fastify.get('/health', async () => ({ status: 'ok', timestamp: new Date().toISOString() }));

  // Public routes
  await fastify.register(authRoutes, { prefix: '/api/v1/auth' });
  await fastify.register(demoRoutes, { prefix: '/api/v1/demo' });
  await fastify.register(aiRoutes, { prefix: '/api/v1/ai' });
  await fastify.register(servicesRoutes, { prefix: '/api/v1/services' });
  await fastify.register(marketRoutes, { prefix: '/api/v1/market' });
  await fastify.register(investmentRoutes, { prefix: '/api/v1/investments' });

  // Transaction routes - public (supports demo mode)
  await fastify.register(transactionRoutes, { prefix: '/api/v1/transactions' });

  // Account routes - public for demo mode, protected for authenticated users
  await fastify.register(accountRoutes, { prefix: '/api/v1/accounts' });

  // Fund routes - public for demo mode
  await fastify.register(fundRoutes, { prefix: '/api/v1/funds' });

  // Savings goals - public for demo mode
  await fastify.register(savingsGoalRoutes, { prefix: '/api/v1/savings-goals' });

  // Public AI status endpoint (no auth needed)
  fastify.get('/api/v1/ai/status', async (request: any, reply: any) => {
    try {
      const ollamaUrl = process.env.OLLAMA_URL || 'http://localhost:11434';
      
      const controller = new AbortController();
      const timeout = setTimeout(() => controller.abort(), 3000);

      const response = await fetch(`${ollamaUrl}/api/tags`, {
        signal: controller.signal
      });

      clearTimeout(timeout);

      if (response.ok) {
        const data = await response.json() as { models?: AIModel[] };
        return reply.send({
          success: true,
          data: {
            status: 'online',
            models: data.models?.map((m: AIModel) => m.name) || [],
            url: ollamaUrl
          }
        });
      } else {
        return reply.send({
          success: false,
          data: {
            status: 'error',
            message: `Ollama returned ${response.status}`,
            url: ollamaUrl
          }
        });
      }
    } catch (error: any) {
      return reply.send({
        success: false,
        data: {
          status: 'offline',
          message: error.message,
          url: process.env.OLLAMA_URL || 'http://localhost:11434'
        }
      });
    }
  });

  // Protected routes
  fastify.register(async (app) => {
    app.addHook('preHandler', fastify.authenticate);
    await app.register(userRoutes, { prefix: '/api/v1/users' });
    // Category, Bank, Budget, QR, Report routes are protected
    await app.register(categoryRoutes, { prefix: '/api/v1/categories' });
    await app.register(bankRoutes, { prefix: '/api/v1/banks' });
    await app.register(budgetRoutes, { prefix: '/api/v1/budgets' });
    await app.register(qrRoutes, { prefix: '/api/v1/qr' });
    await app.register(reportRoutes, { prefix: '/api/v1/reports' });
  });

  try {
    await fastify.listen({ port: PORT, host: '0.0.0.0' });
    fastify.log.info(`Server running on http://0.0.0.0:${PORT}`);
  } catch (err) {
    fastify.log.error(err);
    process.exit(1);
  }
}

start();
