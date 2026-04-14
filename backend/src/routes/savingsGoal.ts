import { query, queryOne, execute } from '../utils/db.js';

export async function savingsGoalRoutes(fastify: any) {
  // Helper to get user ID (supports demo mode)
  const getUserId = (request: any): string => {
    if (request.user?.userId) return request.user.userId;
    return 'demo';
  };

  // List all savings goals
  fastify.get('/', async (request: any, reply: any) => {
    const userId = getUserId(request);
    try {
      const goals = await query(
        'SELECT * FROM savings_goals WHERE user_id = $1 AND is_active = true ORDER BY created_at DESC',
        [userId]
      );

      return reply.send({
        success: true,
        data: goals.map((g: any) => ({
          id: g.id,
          name: g.name,
          targetAmount: g.target_amount,
          currentAmount: g.current_amount,
          period: g.period,
          amountPerPeriod: g.amount_per_period,
          progress: g.target_amount > 0 ? (parseFloat(g.current_amount) / parseFloat(g.target_amount)) * 100 : 0,
          startDate: g.start_date,
          endDate: g.end_date,
          createdAt: new Date(g.created_at * 1000).toISOString()
        }))
      });
    } catch (error: any) {
      fastify.log.error(error);
      return reply.status(500).send({ success: false, message: 'Failed to get savings goals' });
    }
  });

  // Create a savings goal
  fastify.post('/', async (request: any, reply: any) => {
    const { name, targetAmount, amountPerPeriod, period, startDate, endDate } = request.body;
    const userId = getUserId(request);
    const now = Math.floor(Date.now() / 1000);
    const { v4: uuidv4 } = require('uuid');
    const id = uuidv4();

    if (!name || !targetAmount) {
      return reply.status(400).send({ success: false, message: 'name and targetAmount are required' });
    }

    try {
      await execute(
        `INSERT INTO savings_goals (id, user_id, name, target_amount, amount_per_period, period, start_date, end_date, created_at, updated_at)
         VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9, $9)`,
        [id, userId, name, targetAmount, amountPerPeriod || 0, period || 'MONTHLY', startDate || null, endDate || null, now]
      );

      const goal = await queryOne('SELECT * FROM savings_goals WHERE id = $1', [id]);
      return reply.status(201).send({
        success: true,
        data: {
          id: goal!.id,
          name: goal!.name,
          targetAmount: goal!.target_amount,
          currentAmount: goal!.current_amount,
          period: goal!.period,
          amountPerPeriod: goal!.amount_per_period,
          progress: 0,
          createdAt: new Date(goal!.created_at * 1000).toISOString()
        }
      });
    } catch (error: any) {
      fastify.log.error(error);
      return reply.status(500).send({ success: false, message: 'Failed to create savings goal' });
    }
  });

  // Update a savings goal
  fastify.put('/:id', async (request: any, reply: any) => {
    const { id } = request.params;
    const { currentAmount, amountPerPeriod, targetAmount, name, endDate } = request.body;
    const userId = getUserId(request);
    const now = Math.floor(Date.now() / 1000);

    try {
      // Build dynamic update query
      const updates: string[] = [];
      const values: any[] = [];
      let paramIndex = 1;

      if (currentAmount !== undefined) {
        updates.push(`current_amount = $${paramIndex++}`);
        values.push(currentAmount);
      }
      if (amountPerPeriod !== undefined) {
        updates.push(`amount_per_period = $${paramIndex++}`);
        values.push(amountPerPeriod);
      }
      if (targetAmount !== undefined) {
        updates.push(`target_amount = $${paramIndex++}`);
        values.push(targetAmount);
      }
      if (name !== undefined) {
        updates.push(`name = $${paramIndex++}`);
        values.push(name);
      }
      if (endDate !== undefined) {
        updates.push(`end_date = $${paramIndex++}`);
        values.push(endDate);
      }

      updates.push(`updated_at = $${paramIndex++}`);
      values.push(now);

      values.push(id, userId);

      const result = await execute(
        `UPDATE savings_goals SET ${updates.join(', ')} WHERE id = $${paramIndex++} AND user_id = $${paramIndex}`,
        values
      );

      if (result === 0) {
        return reply.status(404).send({ success: false, message: 'Savings goal not found' });
      }

      const goal = await queryOne('SELECT * FROM savings_goals WHERE id = $1', [id]);
      return reply.send({
        success: true,
        data: {
          id: goal!.id,
          name: goal!.name,
          targetAmount: goal!.target_amount,
          currentAmount: goal!.current_amount,
          period: goal!.period,
          amountPerPeriod: goal!.amount_per_period,
          progress: goal!.target_amount > 0 ? (parseFloat(goal!.current_amount) / parseFloat(goal!.target_amount)) * 100 : 0,
          endDate: goal!.end_date,
          updatedAt: new Date(goal!.updated_at * 1000).toISOString()
        }
      });
    } catch (error: any) {
      fastify.log.error(error);
      return reply.status(500).send({ success: false, message: 'Failed to update savings goal' });
    }
  });

  // Delete a savings goal
  fastify.delete('/:id', async (request: any, reply: any) => {
    const { id } = request.params;
    const userId = getUserId(request);

    try {
      const result = await execute(
        'UPDATE savings_goals SET is_active = false, updated_at = $1 WHERE id = $2 AND user_id = $3',
        [Math.floor(Date.now() / 1000), id, userId]
      );

      if (result === 0) {
        return reply.status(404).send({ success: false, message: 'Savings goal not found' });
      }

      return reply.send({ success: true, message: 'Savings goal deleted' });
    } catch (error: any) {
      fastify.log.error(error);
      return reply.status(500).send({ success: false, message: 'Failed to delete savings goal' });
    }
  });

  // Add to savings goal (contribution)
  fastify.post('/:id/contribute', async (request: any, reply: any) => {
    const { id } = request.params;
    const { amount } = request.body;
    const userId = getUserId(request);

    if (!amount || amount <= 0) {
      return reply.status(400).send({ success: false, message: 'amount must be positive' });
    }

    try {
      // Get current goal
      const goal = await queryOne(
        'SELECT * FROM savings_goals WHERE id = $1 AND user_id = $2 AND is_active = true',
        [id, userId]
      );

      if (!goal) {
        return reply.status(404).send({ success: false, message: 'Savings goal not found' });
      }

      const newAmount = parseFloat(goal.current_amount) + amount;
      const now = Math.floor(Date.now() / 1000);

      await execute(
        'UPDATE savings_goals SET current_amount = $1, updated_at = $2 WHERE id = $3',
        [newAmount, now, id]
      );

      return reply.send({
        success: true,
        data: {
          id: goal.id,
          name: goal.name,
          targetAmount: goal.target_amount,
          currentAmount: newAmount,
          progress: goal.target_amount > 0 ? (newAmount / parseFloat(goal.target_amount)) * 100 : 0
        }
      });
    } catch (error: any) {
      fastify.log.error(error);
      return reply.status(500).send({ success: false, message: 'Failed to contribute to savings goal' });
    }
  });
}
