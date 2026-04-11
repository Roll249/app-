import { query, queryOne, execute } from '../utils/db.js';
import bcrypt from 'bcryptjs';
import { v4 as uuidv4 } from 'uuid';

export async function authRoutes(fastify: any) {
  // Register
  fastify.post('/register', async (request: any, reply: any) => {
    const { email, password, fullName, phone } = request.body;

    if (!email || !password) {
      return reply.status(400).send({ success: false, message: 'Email and password are required' });
    }

    try {
      const existing: any = await queryOne('SELECT * FROM users WHERE email = $1', [email]);
      if (existing) {
        return reply.status(400).send({ success: false, message: 'Email already registered' });
      }

      const passwordHash = await bcrypt.hash(password, 12);
      const userId = uuidv4();
      const now = Math.floor(Date.now() / 1000);

      await execute(
        `INSERT INTO users (id, email, password_hash, full_name, phone, is_active, is_verified, created_at, updated_at)
         VALUES ($1, $2, $3, $4, $5, true, false, $6, $6)`,
        [userId, email, passwordHash, fullName || email.split('@')[0], phone || null, now]
      );

      const accessToken = fastify.jwt.sign({ userId, email }, { expiresIn: '1h' });
      const refreshToken = uuidv4();
      const refreshExpiry = now + 604800;

      await execute(
        `INSERT INTO refresh_tokens (id, user_id, token, expires_at, created_at) VALUES ($1, $2, $3, $4, $5)`,
        [uuidv4(), userId, refreshToken, refreshExpiry, now]
      );

      return reply.status(201).send({
        success: true,
        data: {
          accessToken,
          refreshToken,
          user: { id: userId, email, fullName: fullName || email.split('@')[0], phone, isVerified: false, createdAt: new Date().toISOString() }
        }
      });
    } catch (error: any) {
      fastify.log.error(error);
      return reply.status(500).send({ success: false, message: 'Registration failed' });
    }
  });

  // Login
  fastify.post('/login', async (request: any, reply: any) => {
    const { email, password } = request.body;

    if (!email || !password) {
      return reply.status(400).send({ success: false, message: 'Email and password are required' });
    }

    try {
      const user: any = await queryOne('SELECT * FROM users WHERE email = $1', [email]);
      if (!user) {
        return reply.status(401).send({ success: false, message: 'Invalid email or password' });
      }

      const valid = await bcrypt.compare(password, user.password_hash);
      if (!valid) {
        return reply.status(401).send({ success: false, message: 'Invalid email or password' });
      }

      const accessToken = fastify.jwt.sign({ userId: user.id, email: user.email }, { expiresIn: '1h' });
      const refreshToken = uuidv4();
      const now = Math.floor(Date.now() / 1000);
      const refreshExpiry = now + 604800;

      await execute(
        `INSERT INTO refresh_tokens (id, user_id, token, expires_at, created_at) VALUES ($1, $2, $3, $4, $5)`,
        [uuidv4(), user.id, refreshToken, refreshExpiry, now]
      );

      return reply.send({
        success: true,
        data: {
          accessToken,
          refreshToken,
          user: {
            id: user.id, email: user.email, fullName: user.full_name, avatarUrl: user.avatar_url,
            phone: user.phone, isVerified: user.is_verified,
            createdAt: new Date(user.created_at * 1000).toISOString()
          }
        }
      });
    } catch (error: any) {
      fastify.log.error(error);
      return reply.status(500).send({ success: false, message: 'Login failed' });
    }
  });

  // Refresh token
  fastify.post('/refresh', async (request: any, reply: any) => {
    const { refreshToken } = request.body;
    if (!refreshToken) {
      return reply.status(400).send({ success: false, message: 'Refresh token required' });
    }

    try {
      const now = Math.floor(Date.now() / 1000);
      const tokenRecord: any = await queryOne(
        `SELECT * FROM refresh_tokens WHERE token = $1 AND revoked_at IS NULL AND expires_at > $2`,
        [refreshToken, now]
      );

      if (!tokenRecord) {
        return reply.status(401).send({ success: false, message: 'Invalid or expired refresh token' });
      }

      const user: any = await queryOne('SELECT * FROM users WHERE id = $1', [tokenRecord.user_id]);
      if (!user) {
        return reply.status(401).send({ success: false, message: 'User not found' });
      }

      await execute('UPDATE refresh_tokens SET revoked_at = $1 WHERE token = $2', [now, refreshToken]);

      const newAccessToken = fastify.jwt.sign({ userId: user.id, email: user.email }, { expiresIn: '1h' });
      const newRefreshToken = uuidv4();
      const refreshExpiry = now + 604800;

      await execute(
        `INSERT INTO refresh_tokens (id, user_id, token, expires_at, created_at) VALUES ($1, $2, $3, $4, $5)`,
        [uuidv4(), user.id, newRefreshToken, refreshExpiry, now]
      );

      return reply.send({
        success: true,
        data: {
          accessToken: newAccessToken,
          refreshToken: newRefreshToken,
          user: {
            id: user.id, email: user.email, fullName: user.full_name, avatarUrl: user.avatar_url,
            phone: user.phone, isVerified: user.is_verified,
            createdAt: new Date(user.created_at * 1000).toISOString()
          }
        }
      });
    } catch (error: any) {
      fastify.log.error(error);
      return reply.status(500).send({ success: false, message: 'Token refresh failed' });
    }
  });

  // Logout
  fastify.post('/logout', async (request: any, reply: any) => {
    const { refreshToken } = request.body;
    if (refreshToken) {
      const now = Math.floor(Date.now() / 1000);
      await execute('UPDATE refresh_tokens SET revoked_at = $1 WHERE token = $2', [now, refreshToken]);
    }
    return reply.send({ success: true, message: 'Logged out' });
  });
}
