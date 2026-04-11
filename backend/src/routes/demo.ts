import { query, queryOne, execute } from '../utils/db.js';
import { createDemoUserWithScenario, getSimulationScenarios, SCENARIOS } from '../utils/simulation.js';

export async function demoRoutes(fastify: any) {
  // Get available scenarios
  fastify.get('/scenarios', async (request: any, reply: any) => {
    try {
      const scenarios = await getSimulationScenarios();
      return reply.send({ success: true, data: scenarios });
    } catch (error: any) {
      fastify.log.error(error);
      return reply.status(500).send({ success: false, message: 'Failed to get scenarios' });
    }
  });

  // Create demo user with specific scenario
  fastify.post('/create', async (request: any, reply: any) => {
    const { email, password, fullName, scenario } = request.body;

    try {
      const scenarioKey = scenario || 'office_worker';
      const result = await createDemoUserWithScenario(fastify, scenarioKey, fullName);

      const user: any = await queryOne('SELECT * FROM users WHERE id = $1', [result.userId]);
      if (!user) {
        return reply.status(500).send({ success: false, message: 'User creation failed' });
      }

      const accessToken = fastify.jwt.sign({ userId: user.id, email: user.email }, { expiresIn: '1h' });

      const scenarioInfo = SCENARIOS[scenarioKey];

      return reply.status(201).send({
        success: true,
        data: {
          accessToken,
          user: {
            id: user.id,
            email: user.email,
            fullName: user.full_name,
            isVerified: user.is_verified,
            createdAt: new Date(user.created_at * 1000).toISOString()
          },
          demoAccount: {
            scenario: scenarioKey,
            scenarioName: scenarioInfo?.name,
            accountId: result.accountId,
            bankAccountIds: result.bankAccountIds,
            message: `Demo account created with ${scenarioInfo?.name} scenario`
          }
        }
      });
    } catch (error: any) {
      fastify.log.error(error);
      return reply.status(500).send({ success: false, message: 'Failed to create demo account' });
    }
  });

  // Quick demo - create with random scenario
  fastify.post('/quick-start', async (request: any, reply: any) => {
    const { scenario } = request.body;
    const scenarioKey = scenario || ['office_worker', 'freelancer', 'tech_lead'][Math.floor(Math.random() * 3)];

    try {
      const result = await createDemoUserWithScenario(fastify, scenarioKey, 'Demo User');
      const user: any = await queryOne('SELECT * FROM users WHERE id = $1', [result.userId]);

      const accessToken = fastify.jwt.sign({ userId: user.id, email: user.email }, { expiresIn: '1h' });
      const scenarioInfo = SCENARIOS[scenarioKey];

      return reply.status(201).send({
        success: true,
        data: {
          accessToken,
          user: {
            id: user.id,
            email: user.email,
            fullName: user.full_name,
            isVerified: user.is_verified
          },
          demoAccount: {
            scenario: scenarioKey,
            scenarioName: scenarioInfo?.name,
            accountId: result.accountId,
            bankAccountIds: result.bankAccountIds
          }
        }
      });
    } catch (error: any) {
      fastify.log.error(error);
      return reply.status(500).send({ success: false, message: 'Failed to create quick demo' });
    }
  });

  // Get demo stats for current user
  fastify.get('/stats', async (request: any, reply: any) => {
    const userId = request.user.userId;

    try {
      const totalTransactions: any = await queryOne(
        `SELECT COUNT(*) as count FROM transactions WHERE user_id = $1`,
        [userId]
      );
      const totalIncome: any = await queryOne(
        `SELECT COALESCE(SUM(amount), 0) as total FROM transactions WHERE user_id = $1 AND type = 'INCOME'`,
        [userId]
      );
      const totalExpense: any = await queryOne(
        `SELECT COALESCE(SUM(amount), 0) as total FROM transactions WHERE user_id = $1 AND type = 'EXPENSE'`,
        [userId]
      );
      const accountsCount: any = await queryOne(
        `SELECT COUNT(*) as count FROM accounts WHERE user_id = $1`,
        [userId]
      );
      const bankAccountsCount: any = await queryOne(
        `SELECT COUNT(*) as count FROM user_bank_accounts WHERE user_id = $1`,
        [userId]
      );
      const fundsCount: any = await queryOne(
        `SELECT COUNT(*) as count FROM funds WHERE user_id = $1 AND is_active = true`,
        [userId]
      );

      return reply.send({
        success: true,
        data: {
          totalTransactions: parseInt(totalTransactions?.count || '0'),
          totalIncome: totalIncome?.total || '0',
          totalExpense: totalExpense?.total || '0',
          accountsCount: parseInt(accountsCount?.count || '0'),
          bankAccountsCount: parseInt(bankAccountsCount?.count || '0'),
          fundsCount: parseInt(fundsCount?.count || '0')
        }
      });
    } catch (error: any) {
      return reply.status(500).send({ success: false, message: 'Failed to get demo stats' });
    }
  });
}
