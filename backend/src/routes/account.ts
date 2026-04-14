import { query, queryOne, execute } from '../utils/db.js';

export async function accountRoutes(fastify: any) {
  // Helper to get user ID (supports demo mode)
  const getUserId = (request: any): string => {
    if (request.user?.userId) return request.user.userId;
    return 'demo';
  };

  // List accounts
  fastify.get('/', async (request: any, reply: any) => {
    const userId = getUserId(request);
    try {
      const accounts = await query(
        'SELECT * FROM accounts WHERE user_id = $1 AND is_active = true ORDER BY created_at DESC',
        [userId]
      );

      return reply.send({
        success: true,
        data: accounts.map((a: any) => ({
          id: a.id, userId: a.user_id, name: a.name, type: a.type, icon: a.icon, color: a.color,
          initialBalance: a.initial_balance, currentBalance: a.current_balance, currency: a.currency,
          includeInTotal: a.include_in_total, isActive: a.is_active, bankAccountId: a.bank_account_id,
          createdAt: new Date(a.created_at * 1000).toISOString(), updatedAt: new Date(a.updated_at * 1000).toISOString()
        }))
      });
    } catch (error: any) {
      fastify.log.error(error);
      return reply.status(500).send({ success: false, message: 'Failed to get accounts' });
    }
  });

  // Get total balance summary
  fastify.get('/summary', async (request: any, reply: any) => {
    const userId = getUserId(request);
    try {
      const result = await queryOne(
        `SELECT 
          COALESCE(SUM(CASE WHEN include_in_total = true THEN current_balance ELSE 0 END), 0) as total_balance,
          COUNT(*) as account_count,
          COALESCE(SUM(CASE WHEN type = 'BANK' AND include_in_total = true THEN current_balance ELSE 0 END), 0) as linked_balance,
          COALESCE(SUM(CASE WHEN type = 'CASH' AND include_in_total = true THEN current_balance ELSE 0 END), 0) as cash_balance
        FROM accounts WHERE user_id = $1 AND is_active = true`,
        [userId]
      );

      return reply.send({
        success: true,
        data: {
          totalBalance: parseFloat(result.total_balance || '0'),
          accountCount: parseInt(result.account_count || '0'),
          linkedBalance: parseFloat(result.linked_balance || '0'),
          cashBalance: parseFloat(result.cash_balance || '0'),
          currency: 'VND'
        }
      });
    } catch (error: any) {
      fastify.log.error(error);
      return reply.status(500).send({ success: false, message: 'Failed to get balance summary' });
    }
  });

  // Create account
  fastify.post('/', async (request: any, reply: any) => {
    const { name, type, icon, color, initialBalance, currency, includeInTotal } = request.body;
    const userId = getUserId(request);
    const now = Math.floor(Date.now() / 1000);
    const { v4: uuidv4 } = require('uuid');
    const id = uuidv4();

    try {
      await execute(
        `INSERT INTO accounts (id, user_id, name, type, icon, color, initial_balance, current_balance, currency, include_in_total, created_at, updated_at)
         VALUES ($1, $2, $3, $4, $5, $6, $7, $7, $8, $9, $10, $10)`,
        [id, userId, name, type, icon || null, color || null, initialBalance || 0, currency || 'VND', includeInTotal !== false, now]
      );

      const account = await queryOne('SELECT * FROM accounts WHERE id = $1', [id]);
      return reply.status(201).send({
        success: true,
        data: {
          id: account!.id, userId: account!.user_id, name: account!.name, type: account!.type,
          initialBalance: account!.initial_balance, currentBalance: account!.current_balance,
          currency: account!.currency, isActive: account!.is_active,
          createdAt: new Date(account!.created_at * 1000).toISOString()
        }
      });
    } catch (error: any) {
      fastify.log.error(error);
      return reply.status(500).send({ success: false, message: 'Failed to create account' });
    }
  });

  // Get account by ID
  fastify.get('/:id', async (request: any, reply: any) => {
    const userId = getUserId(request);
    try {
      const account = await queryOne(
        'SELECT * FROM accounts WHERE id = $1 AND user_id = $2',
        [request.params.id, userId]
      );
      if (!account) return reply.status(404).send({ success: false, message: 'Account not found' });

      return reply.send({
        success: true,
        data: {
          id: account.id, userId: account.user_id, name: account.name, type: account.type,
          initialBalance: account.initial_balance, currentBalance: account.current_balance,
          currency: account.currency, includeInTotal: account.include_in_total,
          createdAt: new Date(account.created_at * 1000).toISOString()
        }
      });
    } catch (error: any) {
      fastify.log.error(error);
      return reply.status(500).send({ success: false, message: 'Failed to get account' });
    }
  });

  // Delete account
  fastify.delete('/:id', async (request: any, reply: any) => {
    const userId = getUserId(request);
    try {
      const result = await execute(
        'UPDATE accounts SET is_active = false, updated_at = $1 WHERE id = $2 AND user_id = $3',
        [Math.floor(Date.now() / 1000), request.params.id, userId]
      );
      if (result === 0) return reply.status(404).send({ success: false, message: 'Account not found' });
      return reply.send({ success: true, message: 'Account deleted' });
    } catch (error: any) {
      fastify.log.error(error);
      return reply.status(500).send({ success: false, message: 'Failed to delete account' });
    }
  });
}
