import { query, queryOne, execute } from '../utils/db.js';
import { getServiceRegistry } from '../services/index.js';

export async function fundRoutes(fastify: any) {
  const getUserId = (request: any): string => {
    if (request.user?.userId) return request.user.userId;
    return 'demo';
  };

  // List all funds
  fastify.get('/', async (request: any, reply: any) => {
    const userId = getUserId(request);
    try {
      const funds = await query(
        'SELECT * FROM funds WHERE user_id = $1 AND is_active = true ORDER BY created_at DESC',
        [userId]
      );

      return reply.send({
        success: true,
        data: funds.map((f: any) => ({
          id: f.id,
          userId: f.user_id,
          name: f.name,
          icon: f.icon,
          color: f.color,
          description: f.description,
          targetAmount: f.target_amount,
          currentAmount: f.current_amount,
          progress: f.progress,
          startDate: f.start_date,
          endDate: f.end_date,
          createdAt: new Date(f.created_at * 1000).toISOString()
        }))
      });
    } catch (error: any) {
      fastify.log.error(error);
      return reply.status(500).send({ success: false, message: 'Failed to get funds' });
    }
  });

  // Create fund
  fastify.post('/', async (request: any, reply: any) => {
    const { name, icon, color, description, targetAmount, startDate, endDate } = request.body;
    const userId = getUserId(request);
    const now = Math.floor(Date.now() / 1000);
    const { v4: uuidv4 } = require('uuid');
    const id = uuidv4();

    if (!name || !targetAmount) {
      return reply.status(400).send({ success: false, message: 'name and targetAmount are required' });
    }

    try {
      await execute(
        `INSERT INTO funds (id, user_id, name, icon, color, description, target_amount, current_amount, progress, start_date, end_date, created_at, updated_at)
         VALUES ($1, $2, $3, $4, $5, $6, $7, 0, 0, $8, $9, $10, $10)`,
        [id, userId, name, icon || 'savings', color || '#4CAF50', description || '', targetAmount, startDate || null, endDate || null, now]
      );

      const fund = await queryOne('SELECT * FROM funds WHERE id = $1', [id]);
      return reply.status(201).send({
        success: true,
        data: {
          id: fund!.id,
          name: fund!.name,
          targetAmount: fund!.target_amount,
          currentAmount: fund!.current_amount,
          progress: fund!.progress
        }
      });
    } catch (error: any) {
      fastify.log.error(error);
      return reply.status(500).send({ success: false, message: 'Failed to create fund' });
    }
  });

  // Get fund by ID
  fastify.get('/:id', async (request: any, reply: any) => {
    const userId = getUserId(request);
    try {
      const fund = await queryOne(
        'SELECT * FROM funds WHERE id = $1 AND user_id = $2 AND is_active = true',
        [request.params.id, userId]
      );

      if (!fund) {
        return reply.status(404).send({ success: false, message: 'Fund not found' });
      }

      return reply.send({
        success: true,
        data: {
          id: fund.id,
          name: fund.name,
          icon: fund.icon,
          color: fund.color,
          description: fund.description,
          targetAmount: fund.target_amount,
          currentAmount: fund.current_amount,
          progress: fund.progress,
          startDate: fund.start_date,
          endDate: fund.end_date
        }
      });
    } catch (error: any) {
      fastify.log.error(error);
      return reply.status(500).send({ success: false, message: 'Failed to get fund' });
    }
  });

  // Contribute to fund
  fastify.post('/:id/contribute', async (request: any, reply: any) => {
    const { id } = request.params;
    const { amount } = request.body;
    const userId = getUserId(request);

    if (!amount || amount <= 0) {
      return reply.status(400).send({ success: false, message: 'amount must be positive' });
    }

    try {
      const fund = await queryOne(
        'SELECT * FROM funds WHERE id = $1 AND user_id = $2 AND is_active = true',
        [id, userId]
      );

      if (!fund) {
        return reply.status(404).send({ success: false, message: 'Fund not found' });
      }

      const newAmount = parseFloat(fund.current_amount) + amount;
      const progress = (newAmount / parseFloat(fund.target_amount)) * 100;
      const now = Math.floor(Date.now() / 1000);

      await execute(
        'UPDATE funds SET current_amount = $1, progress = $2, updated_at = $3 WHERE id = $4',
        [newAmount, progress, now, id]
      );

      // Also create a transaction
      const txId = require('uuid').v4();
      await execute(
        `INSERT INTO transactions (id, user_id, type, amount, description, date, created_at, updated_at)
         VALUES ($1, $2, 'EXPENSE', $3, $4, $5, $6, $7)`,
        [txId, userId, amount, `Nạp tiền vào quỹ: ${fund.name}`, now, now, now]
      );

      return reply.send({
        success: true,
        data: {
          id: fund.id,
          name: fund.name,
          targetAmount: fund.target_amount,
          currentAmount: newAmount,
          progress: progress,
          transactionId: txId
        }
      });
    } catch (error: any) {
      fastify.log.error(error);
      return reply.status(500).send({ success: false, message: 'Failed to contribute to fund' });
    }
  });

  // Delete fund
  fastify.delete('/:id', async (request: any, reply: any) => {
    const userId = getUserId(request);
    try {
      const result = await execute(
        'UPDATE funds SET is_active = false, updated_at = $1 WHERE id = $2 AND user_id = $3',
        [Math.floor(Date.now() / 1000), request.params.id, userId]
      );

      if (result === 0) {
        return reply.status(404).send({ success: false, message: 'Fund not found' });
      }

      return reply.send({ success: true, message: 'Fund deleted' });
    } catch (error: any) {
      fastify.log.error(error);
      return reply.status(500).send({ success: false, message: 'Failed to delete fund' });
    }
  });

  // AI Auto-allocation endpoint
  fastify.post('/ai-allocate', async (request: any, reply: any) => {
    const { amount, description } = request.body;
    const userId = getUserId(request);

    if (!amount || amount <= 0) {
      return reply.status(400).send({ success: false, message: 'amount must be positive' });
    }

    try {
      // Get user's funds
      const funds = await query(
        'SELECT * FROM funds WHERE user_id = $1 AND is_active = true ORDER BY created_at ASC',
        [userId]
      );

      // Get monthly income/expense for context
      const now = Math.floor(Date.now() / 1000);
      const startOfMonth = new Date();
      startOfMonth.setDate(1);
      startOfMonth.setHours(0, 0, 0, 0);
      const startTimestamp = Math.floor(startOfMonth.getTime() / 1000);

      const monthlyIncomeResult: any = await queryOne(
        `SELECT COALESCE(SUM(amount), 0) as total FROM transactions WHERE user_id = $1 AND type = 'INCOME' AND date >= $2`,
        [userId, startTimestamp]
      );
      const monthlyExpenseResult: any = await queryOne(
        `SELECT COALESCE(SUM(amount), 0) as total FROM transactions WHERE user_id = $1 AND type = 'EXPENSE' AND date >= $2`,
        [userId, startTimestamp]
      );

      const monthlyIncome = parseFloat(monthlyIncomeResult?.total || '0');
      const monthlyExpense = parseFloat(monthlyExpenseResult?.total || '0');

      // Call AI service
      const serviceRegistry = getServiceRegistry(fastify.log);
      const aiService = serviceRegistry.getAIService();

      const fundData = funds.map((f: any) => ({
        id: f.id,
        name: f.name,
        currentAmount: parseFloat(f.current_amount),
        targetAmount: parseFloat(f.target_amount)
      }));

      const allocation = await aiService.suggestFundAllocation(
        amount,
        monthlyIncome,
        monthlyExpense,
        fundData
      );

      if (!allocation) {
        // Fallback: distribute evenly
        const evenAmount = amount / Math.max(funds.length, 1);
        const allocations = funds.map((f: any) => ({
          fundId: f.id,
          fundName: f.name,
          amount: evenAmount,
          reason: 'Chia đều cho các quỹ'
        }));

        return reply.send({
          success: true,
          data: {
            totalAmount: amount,
            allocations,
            suggestion: 'Chia đều cho các quỹ vì AI không trả lời kịp thời'
          }
        });
      }

      return reply.send({
        success: true,
        data: {
          totalAmount: amount,
          allocations: allocation.allocations || [],
          totalSavings: allocation.totalSavings,
          suggestion: allocation.suggestion
        }
      });
    } catch (error: any) {
      fastify.log.error(error);
      return reply.status(500).send({ success: false, message: 'Failed to get AI allocation suggestion' });
    }
  });

  // Execute allocation
  fastify.post('/execute-allocation', async (request: any, reply: any) => {
    const { allocations } = request.body;
    const userId = getUserId(request);

    if (!allocations || !Array.isArray(allocations) || allocations.length === 0) {
      return reply.status(400).send({ success: false, message: 'allocations array is required' });
    }

    const results = [];
    const now = Math.floor(Date.now() / 1000);

    try {
      for (const allocation of allocations) {
        const { fundId, amount } = allocation;

        if (!fundId || !amount || amount <= 0) continue;

        const fund = await queryOne(
          'SELECT * FROM funds WHERE id = $1 AND user_id = $2 AND is_active = true',
          [fundId, userId]
        );

        if (!fund) continue;

        const newAmount = parseFloat(fund.current_amount) + amount;
        const progress = (newAmount / parseFloat(fund.target_amount)) * 100;

        await execute(
          'UPDATE funds SET current_amount = $1, progress = $2, updated_at = $3 WHERE id = $4',
          [newAmount, progress, now, fundId]
        );

        // Create transaction
        const txId = require('uuid').v4();
        await execute(
          `INSERT INTO transactions (id, user_id, type, amount, description, date, created_at, updated_at)
           VALUES ($1, $2, 'EXPENSE', $3, $4, $5, $6, $7)`,
          [txId, userId, amount, `Chia quỹ: ${fund.name}`, now, now, now]
        );

        results.push({
          fundId,
          fundName: fund.name,
          amount,
          newBalance: newAmount,
          transactionId: txId
        });
      }

      return reply.send({
        success: true,
        data: {
          totalAllocated: results.reduce((sum, r) => sum + r.amount, 0),
          results
        }
      });
    } catch (error: any) {
      fastify.log.error(error);
      return reply.status(500).send({ success: false, message: 'Failed to execute allocation' });
    }
  });
}
