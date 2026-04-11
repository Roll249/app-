import { query, queryOne, execute } from '../utils/db.js';

export async function userRoutes(fastify: any) {
  fastify.get('/me', async (request: any, reply: any) => {
    try {
      const user: any = await queryOne(
        `SELECT id, email, full_name, avatar_url, phone, is_verified, created_at FROM users WHERE id = $1`,
        [request.user.userId]
      );
      if (!user) return reply.status(404).send({ success: false, message: 'User not found' });

      return reply.send({
        success: true,
        data: {
          id: user.id, email: user.email, fullName: user.full_name, avatarUrl: user.avatar_url,
          phone: user.phone, isVerified: user.is_verified, createdAt: new Date(user.created_at * 1000).toISOString()
        }
      });
    } catch (error: any) {
      fastify.log.error(error);
      return reply.status(500).send({ success: false, message: 'Failed to get user' });
    }
  });

  fastify.put('/me', async (request: any, reply: any) => {
    const { fullName, avatarUrl, phone } = request.body;
    const now = Math.floor(Date.now() / 1000);

    try {
      await execute(
        `UPDATE users SET full_name = COALESCE($1, full_name), avatar_url = COALESCE($2, avatar_url), phone = COALESCE($3, phone), updated_at = $4 WHERE id = $5`,
        [fullName, avatarUrl, phone, now, request.user.userId]
      );

      const user: any = await queryOne(
        `SELECT id, email, full_name, avatar_url, phone, is_verified, created_at FROM users WHERE id = $1`,
        [request.user.userId]
      );

      return reply.send({
        success: true,
        data: {
          id: user!.id, email: user!.email, fullName: user!.full_name, avatarUrl: user!.avatar_url,
          phone: user!.phone, isVerified: user!.is_verified, createdAt: new Date(user!.created_at * 1000).toISOString()
        }
      });
    } catch (error: any) {
      fastify.log.error(error);
      return reply.status(500).send({ success: false, message: 'Failed to update profile' });
    }
  });
}
