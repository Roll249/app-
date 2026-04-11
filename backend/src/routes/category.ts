import { query, queryOne } from '../utils/db.js';

export async function categoryRoutes(fastify: any) {
  // List categories
  fastify.get('/', async (request: any, reply: any) => {
    const { type } = request.query;
    try {
      let sql = 'SELECT * FROM categories WHERE is_active = true AND (user_id IS NULL OR user_id = $1)';
      const params: any[] = [request.user.userId];
      if (type) { sql += ' AND type = $2'; params.push(type); }
      sql += ' ORDER BY is_system DESC, sort_order ASC';

      const categories = await query(sql, params);
      return reply.send({
        success: true,
        data: categories.map((c: any) => ({
          id: c.id, userId: c.user_id, name: c.name, icon: c.icon, color: c.color,
          type: c.type, parentId: c.parent_id, isSystem: c.is_system, isActive: c.is_active,
          sortOrder: c.sort_order, createdAt: new Date(c.created_at * 1000).toISOString()
        }))
      });
    } catch (error: any) {
      fastify.log.error(error);
      return reply.status(500).send({ success: false, message: 'Failed to get categories' });
    }
  });

  // Create category
  fastify.post('/', async (request: any, reply: any) => {
    const { name, type, icon, color, parentId } = request.body;
    const { v4: uuidv4 } = require('uuid');
    const id = uuidv4();
    const now = Math.floor(Date.now() / 1000);

    try {
      await query(
        `INSERT INTO categories (id, user_id, name, type, icon, color, parent_id, is_system, is_active, sort_order, created_at)
         VALUES ($1, $2, $3, $4, $5, $6, $7, false, true, 0, $8)`,
        [id, request.user.userId, name, type, icon || null, color || null, parentId || null, now]
      );
      const category: any = await queryOne('SELECT * FROM categories WHERE id = $1', [id]);
      return reply.status(201).send({
        success: true,
        data: { id: category.id, name: category.name, type: category.type, icon: category.icon, color: category.color, isSystem: false, createdAt: new Date(category.created_at * 1000).toISOString() }
      });
    } catch (error: any) {
      fastify.log.error(error);
      return reply.status(500).send({ success: false, message: 'Failed to create category' });
    }
  });
}
