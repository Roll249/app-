import { query, queryOne, execute } from '../utils/db.js';

export async function transactionRoutes(fastify: any) {

  // Helper to get user ID (supports demo mode)
  const getUserId = (request: any): string => {
    if (request.user?.userId) return request.user.userId;
    // Demo mode: return 'demo' user
    return 'demo';
  };

  // Ensure demo account exists
  const ensureDemoAccount = async (userId: string): Promise<string> => {
    if (userId !== 'demo') return userId;

    let account = await queryOne(
      'SELECT id FROM accounts WHERE user_id = $1 LIMIT 1',
      [userId]
    );

    if (!account) {
      // Create demo account
      const { v4: uuidv4 } = require('uuid');
      const accountId = uuidv4();
      const now = Math.floor(Date.now() / 1000);
      await execute(
        `INSERT INTO accounts (id, user_id, name, type, current_balance, currency, icon, color, is_active, created_at, updated_at)
         VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11)`,
        [accountId, userId, 'Demo Account', 'CASH', '100000000', 'VND', 'account_balance_wallet', '#4CAF50', true, now, now]
      );
      return accountId;
    }
    return account.id;
  };

  // List transactions
  fastify.get('/', async (request: any, reply: any) => {
    const { accountId, categoryId, type, startDate, endDate, page = '1', pageSize = '20' } = request.query;
    const userId = getUserId(request);
    const offset = (parseInt(page as string) - 1) * parseInt(pageSize as string);

    try {
      let where = `t.user_id = $1`;
      const params: any[] = [userId];
      let paramIdx = 2;

      if (accountId) { where += ` AND t.account_id = $${paramIdx++}`; params.push(accountId); }
      if (categoryId) { where += ` AND t.category_id = $${paramIdx++}`; params.push(categoryId); }
      if (type) { where += ` AND t.type = $${paramIdx++}`; params.push(type); }
      if (startDate) { where += ` AND t.date >= $${paramIdx++}`; params.push(parseInt(startDate as string)); }
      if (endDate) { where += ` AND t.date <= $${paramIdx++}`; params.push(parseInt(endDate as string)); }

      const countResult: any = await queryOne(
        `SELECT COUNT(*) as count FROM transactions t WHERE ${where}`, params
      );
      const total = parseInt(countResult?.count || '0');

      const transactions = await query(
        `SELECT t.*, a.name as account_name, c.name as category_name, c.icon as category_icon, c.color as category_color
         FROM transactions t
         LEFT JOIN accounts a ON t.account_id = a.id
         LEFT JOIN categories c ON t.category_id = c.id
         WHERE ${where}
         ORDER BY t.date DESC
         LIMIT $${paramIdx++} OFFSET $${paramIdx}`,
        [...params, parseInt(pageSize as string), offset]
      );

      return reply.send({
        success: true,
        data: transactions.map((t: any) => ({
          id: t.id, userId: t.user_id, accountId: t.account_id, accountName: t.account_name,
          categoryId: t.category_id, categoryName: t.category_name, categoryIcon: t.category_icon,
          categoryColor: t.category_color, type: t.type, amount: t.amount, currency: t.currency,
          description: t.description, note: t.note, date: t.date.toString(),
          sourceType: t.source_type, referenceId: t.reference_id,
          relatedTransactionId: t.related_transaction_id, createdAt: new Date(t.created_at * 1000).toISOString()
        })),
        meta: { page: parseInt(page as string), pageSize: parseInt(pageSize as string), total, totalPages: Math.ceil(total / parseInt(pageSize as string)) }
      });
    } catch (error: any) {
      fastify.log.error(error);
      return reply.status(500).send({ success: false, message: 'Failed to get transactions' });
    }
  });

  // Create transaction
  fastify.post('/', async (request: any, reply: any) => {
    const { accountId: reqAccountId, categoryId, type, amount, currency, description, note, date, sourceType } = request.body;
    const userId = getUserId(request);
    const now = Math.floor(Date.now() / 1000);
    const { v4: uuidv4 } = require('uuid');
    const id = uuidv4();

    fastify.log.info(`[Transaction] Creating transaction for user: ${userId}, accountId: ${reqAccountId}`);

    try {
      // Get or create account
      let accountId = reqAccountId;
      if (!accountId || accountId === 'default') {
        accountId = await ensureDemoAccount(userId);
        fastify.log.info(`[Transaction] Using demo account: ${accountId}`);
      }

      const account: any = await queryOne('SELECT * FROM accounts WHERE id = $1', [accountId]);
      if (!account) {
        fastify.log.error(`[Transaction] Account not found: ${accountId}`);
        return reply.status(400).send({ success: false, message: 'Account not found' });
      }

      const balance = parseFloat(account.current_balance || '0');
      const amt = parseFloat(amount);
      const newBalance = type === 'INCOME' ? balance + amt : balance - amt;
      fastify.log.info(`[Transaction] Balance: ${balance}, Amount: ${amt}, New Balance: ${newBalance}`);

      await execute(
        `INSERT INTO transactions (id, user_id, account_id, category_id, type, amount, currency, description, note, date, source_type, reference_id, created_at, updated_at)
         VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, $14)`,
        [id, userId, accountId, categoryId || null, type, amount, currency || 'VND', description || null, note || null, date || now, sourceType || null, `TXN_${id}`, now, now]
      );

      await execute('UPDATE accounts SET current_balance = $1, updated_at = $2 WHERE id = $3', [newBalance, now, accountId]);

      const transaction: any = await queryOne(
        `SELECT t.*, a.name as account_name, c.name as category_name, c.icon as category_icon, c.color as category_color
         FROM transactions t LEFT JOIN accounts a ON t.account_id = a.id LEFT JOIN categories c ON t.category_id = c.id
         WHERE t.id = $1`, [id]
      );

      fastify.log.info(`[Transaction] Created successfully: ${id}`);

      return reply.status(201).send({
        success: true,
        data: {
          id: transaction.id, userId: transaction.user_id, accountId: transaction.account_id, accountName: transaction.account_name,
          categoryId: transaction.category_id, categoryName: transaction.category_name, categoryIcon: transaction.category_icon,
          categoryColor: transaction.category_color, type: transaction.type, amount: transaction.amount, currency: transaction.currency,
          description: transaction.description, note: transaction.note, date: transaction.date.toString(),
          sourceType: transaction.source_type, referenceId: transaction.reference_id, createdAt: new Date(transaction.created_at * 1000).toISOString()
        }
      });
    } catch (error: any) {
      fastify.log.error(`[Transaction] Error: ${error.message}`, error.stack);
      return reply.status(500).send({ success: false, message: `Failed to create transaction: ${error.message}` });
    }
  });

  // Delete transaction
  fastify.delete('/:id', async (request: any, reply: any) => {
    try {
      const transaction: any = await queryOne(
        'SELECT * FROM transactions WHERE id = $1 AND user_id = $2',
        [request.params.id, request.user.userId]
      );
      if (!transaction) return reply.status(404).send({ success: false, message: 'Transaction not found' });

      const account: any = await queryOne('SELECT current_balance FROM accounts WHERE id = $1', [transaction.account_id]);
      const amt = parseFloat(transaction.amount);
      const newBalance = transaction.type === 'INCOME'
        ? parseFloat(account.current_balance) - amt
        : parseFloat(account.current_balance) + amt;

      const now = Math.floor(Date.now() / 1000);
      await execute('UPDATE accounts SET current_balance = $1, updated_at = $2 WHERE id = $3', [newBalance, now, transaction.account_id]);
      await execute('UPDATE transactions SET is_active = false, updated_at = $1 WHERE id = $2', [now, request.params.id]);

      return reply.send({ success: true, message: 'Transaction deleted' });
    } catch (error: any) {
      fastify.log.error(error);
      return reply.status(500).send({ success: false, message: 'Failed to delete transaction' });
    }
  });
}
