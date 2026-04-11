import { query, queryOne, execute } from '../utils/db.js';

export async function fundRoutes(fastify: any) {
  fastify.get('/', async (request: any, reply: any) => {
    try {
      const funds = await query('SELECT * FROM funds WHERE user_id = $1 AND is_active = true ORDER BY created_at DESC', [request.user.userId]);
      return reply.send({ success: true, data: funds.map((f: any) => ({
        id: f.id, name: f.name, icon: f.icon, color: f.color, description: f.description,
        targetAmount: f.target_amount, currentAmount: f.current_amount, progress: f.progress,
        startDate: f.start_date, endDate: f.end_date, isActive: f.is_active,
        createdAt: new Date(f.created_at * 1000).toISOString()
      }))});
    } catch (error: any) {
      return reply.status(500).send({ success: false, message: 'Failed to get funds' });
    }
  });

  fastify.post('/', async (request: any, reply: any) => {
    const { name, icon, color, description, targetAmount, startDate, endDate } = request.body;
    const { v4: uuidv4 } = require('uuid');
    const id = uuidv4();
    const now = Math.floor(Date.now() / 1000);

    try {
      await execute(
        `INSERT INTO funds (id, user_id, name, icon, color, description, target_amount, current_amount, progress, start_date, end_date, created_at, updated_at)
         VALUES ($1, $2, $3, $4, $5, $6, $7, 0, 0, $8, $9, $10, $10)`,
        [id, request.user.userId, name, icon || null, color || null, description || null, targetAmount || null, startDate || null, endDate || null, now]
      );
      const fund: any = await queryOne('SELECT * FROM funds WHERE id = $1', [id]);
      return reply.status(201).send({ success: true, data: { id: fund.id, name: fund.name, targetAmount: fund.target_amount, currentAmount: 0, progress: 0, createdAt: new Date(fund.created_at * 1000).toISOString() } });
    } catch (error: any) {
      return reply.status(500).send({ success: false, message: 'Failed to create fund' });
    }
  });
}
