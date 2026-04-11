import { query, queryOne, execute } from '../utils/db.js';

export async function budgetRoutes(fastify: any) {
  fastify.get('/', async (request: any, reply: any) => {
    try {
      const budgets = await query(
        `SELECT b.*, c.name as category_name, c.icon as category_icon, c.color as category_color
         FROM budgets b LEFT JOIN categories c ON b.category_id = c.id
         WHERE b.user_id = $1 AND b.is_active = true ORDER BY b.created_at DESC`,
        [request.user.userId]
      );
      return reply.send({ success: true, data: budgets.map((b: any) => ({
        id: b.id, name: b.name, amount: b.amount, spentAmount: b.spent_amount, remainingAmount: b.remaining_amount,
        progress: b.progress, period: b.period, startDate: b.start_date, endDate: b.end_date,
        categoryId: b.category_id, categoryName: b.category_name, categoryIcon: b.category_icon, categoryColor: b.category_color,
        createdAt: new Date(b.created_at * 1000).toISOString()
      }))});
    } catch (error: any) {
      return reply.status(500).send({ success: false, message: 'Failed to get budgets' });
    }
  });

  fastify.post('/', async (request: any, reply: any) => {
    const { name, amount, period, startDate, endDate, categoryId } = request.body;
    const { v4: uuidv4 } = require('uuid');
    const id = uuidv4();
    const now = Math.floor(Date.now() / 1000);

    try {
      await execute(
        `INSERT INTO budgets (id, user_id, name, amount, spent_amount, remaining_amount, progress, period, start_date, end_date, category_id, created_at)
         VALUES ($1, $2, $3, $4, 0, $4, 0, $5, $6, $7, $8, $9)`,
        [id, request.user.userId, name, amount, period, startDate, endDate || null, categoryId || null, now]
      );
      const budget: any = await queryOne('SELECT * FROM budgets WHERE id = $1', [id]);
      return reply.status(201).send({ success: true, data: { id: budget.id, name: budget.name, amount: budget.amount, period: budget.period, createdAt: new Date(budget.created_at * 1000).toISOString() } });
    } catch (error: any) {
      return reply.status(500).send({ success: false, message: 'Failed to create budget' });
    }
  });
}
