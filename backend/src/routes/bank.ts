import { query } from '../utils/db.js';

export async function bankRoutes(fastify: any) {
  // List simulated banks
  fastify.get('/', async (request: any, reply: any) => {
    try {
      const banks = await query('SELECT * FROM simulated_banks WHERE is_active = true ORDER BY code');
      return reply.send({
        success: true,
        data: banks.map((b: any) => ({
          id: b.id, code: b.code, name: b.name, shortName: b.short_name,
          logoUrl: b.logo_url, vietqrPrefix: b.vietqr_prefix, swiftCode: b.swift_code
        }))
      });
    } catch (error: any) {
      fastify.log.error(error);
      return reply.status(500).send({ success: false, message: 'Failed to get banks' });
    }
  });

  // Get user's linked bank accounts
  fastify.get('/accounts', async (request: any, reply: any) => {
    try {
      const accounts = await query(
        `SELECT uba.*, sb.name as bank_name, sb.short_name as bank_short_name, sb.code as bank_code
         FROM user_bank_accounts uba
         JOIN simulated_banks sb ON uba.bank_id = sb.id
         WHERE uba.user_id = $1 AND uba.is_active = true`,
        [request.user.userId]
      );
      return reply.send({
        success: true,
        data: accounts.map((a: any) => ({
          id: a.id, accountNumber: a.account_number, accountHolderName: a.account_holder_name,
          balance: a.balance, isActive: a.is_active, linkedAt: new Date(a.linked_at * 1000).toISOString(),
          bank: { id: a.bank_id, name: a.bank_name, shortName: a.bank_short_name, code: a.bank_code }
        }))
      });
    } catch (error: any) {
      fastify.log.error(error);
      return reply.status(500).send({ success: false, message: 'Failed to get bank accounts' });
    }
  });
}
