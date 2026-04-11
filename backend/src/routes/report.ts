import { query } from '../utils/db.js';

export async function reportRoutes(fastify: any) {
  // Summary - tổng quan tài chính
  fastify.get('/summary', async (request: any, reply: any) => {
    const { startDate, endDate } = request.query;
    const userId = request.user.userId;

    try {
      const start = startDate ? parseInt(startDate as string) : Math.floor(Date.now() / 1000) - 2592000;
      const end = endDate ? parseInt(endDate as string) : Math.floor(Date.now() / 1000);

      const accountBalances: any[] = await query('SELECT SUM(current_balance) as total FROM accounts WHERE user_id = $1 AND is_active = true', [userId]);
      const totalBalance = accountBalances[0]?.total || 0;

      const incomeResult: any[] = await query(
        `SELECT COALESCE(SUM(amount), 0) as total FROM transactions WHERE user_id = $1 AND type = 'INCOME' AND date >= $2 AND date <= $3`,
        [userId, start, end]
      );
      const expenseResult: any[] = await query(
        `SELECT COALESCE(SUM(amount), 0) as total FROM transactions WHERE user_id = $1 AND type = 'EXPENSE' AND date >= $2 AND date <= $3`,
        [userId, start, end]
      );

      const categoryBreakdown: any[] = await query(
        `SELECT c.id, c.name, c.icon, c.color, SUM(t.amount) as total
         FROM transactions t JOIN categories c ON t.category_id = c.id
         WHERE t.user_id = $1 AND t.type = 'EXPENSE' AND t.date >= $2 AND t.date <= $3
         GROUP BY c.id, c.name, c.icon, c.color ORDER BY total DESC LIMIT 10`,
        [userId, start, end]
      );

      const totalExpenseAmount = categoryBreakdown.reduce((sum: number, c: any) => sum + parseFloat(c.total || 0), 0);

      return reply.send({
        success: true,
        data: {
          totalBalance: totalBalance.toString(),
          totalIncome: incomeResult[0]?.total?.toString() || '0',
          totalExpense: expenseResult[0]?.total?.toString() || '0',
          netSavings: (parseFloat(incomeResult[0]?.total || '0') - parseFloat(expenseResult[0]?.total || '0')).toString(),
          categoryBreakdown: categoryBreakdown.map((c: any) => ({
            categoryId: c.id,
            categoryName: c.name,
            categoryIcon: c.icon,
            categoryColor: c.color,
            amount: c.total.toString(),
            percentage: totalExpenseAmount > 0 ? Math.round((parseFloat(c.total) / totalExpenseAmount) * 10000) / 100 : 0
          }))
        }
      });
    } catch (error: any) {
      fastify.log.error(error);
      return reply.status(500).send({ success: false, message: 'Failed to get report' });
    }
  });

  // Income/Expense report theo period
  fastify.get('/income-expense', async (request: any, reply: any) => {
    const { period = 'MONTHLY', startDate, endDate } = request.query;
    const userId = request.user.userId;

    try {
      let dateFilter = '';
      const now = Math.floor(Date.now() / 1000);
      if (period === 'MONTHLY') {
        const startOfMonth = new Date();
        startOfMonth.setDate(1);
        startOfMonth.setHours(0, 0, 0, 0);
        dateFilter = `date >= ${Math.floor(startOfMonth.getTime() / 1000)}`;
      } else if (period === 'WEEKLY') {
        const weekAgo = now - 7 * 86400;
        dateFilter = `date >= ${weekAgo}`;
      } else if (period === 'YEARLY') {
        const startOfYear = new Date(new Date().getFullYear(), 0, 1).getTime() / 1000;
        dateFilter = `date >= ${startOfYear}`;
      }

      const incomeResult: any[] = await query(
        `SELECT COALESCE(SUM(amount), 0) as total FROM transactions WHERE user_id = $1 AND type = 'INCOME' AND ${dateFilter}`,
        [userId]
      );
      const expenseResult: any[] = await query(
        `SELECT COALESCE(SUM(amount), 0) as total FROM transactions WHERE user_id = $1 AND type = 'EXPENSE' AND ${dateFilter}`,
        [userId]
      );

      const incomeByCategory: any[] = await query(
        `SELECT c.id, c.name, c.icon, c.color, COALESCE(SUM(t.amount), 0) as total
         FROM transactions t JOIN categories c ON t.category_id = c.id
         WHERE t.user_id = $1 AND t.type = 'INCOME' AND ${dateFilter}
         GROUP BY c.id, c.name, c.icon, c.color ORDER BY total DESC`,
        [userId]
      );

      const expenseByCategory: any[] = await query(
        `SELECT c.id, c.name, c.icon, c.color, COALESCE(SUM(t.amount), 0) as total
         FROM transactions t JOIN categories c ON t.category_id = c.id
         WHERE t.user_id = $1 AND t.type = 'EXPENSE' AND ${dateFilter}
         GROUP BY c.id, c.name, c.icon, c.color ORDER BY total DESC`,
        [userId]
      );

      const incomeTotal = parseFloat(incomeResult[0]?.total || '0');
      const expenseTotal = parseFloat(expenseResult[0]?.total || '0');

      return reply.send({
        success: true,
        data: {
          period,
          totalIncome: incomeResult[0]?.total?.toString() || '0',
          totalExpense: expenseResult[0]?.total?.toString() || '0',
          netAmount: (incomeTotal - expenseTotal).toString(),
          incomeByCategory: incomeByCategory.map((c: any) => ({
            categoryId: c.id, categoryName: c.name, categoryIcon: c.icon, categoryColor: c.color,
            amount: c.total.toString(),
            percentage: incomeTotal > 0 ? Math.round((parseFloat(c.total) / incomeTotal) * 10000) / 100 : 0
          })),
          expenseByCategory: expenseByCategory.map((c: any) => ({
            categoryId: c.id, categoryName: c.name, categoryIcon: c.icon, categoryColor: c.color,
            amount: c.total.toString(),
            percentage: expenseTotal > 0 ? Math.round((parseFloat(c.total) / expenseTotal) * 10000) / 100 : 0
          }))
        }
      });
    } catch (error: any) {
      fastify.log.error(error);
      return reply.status(500).send({ success: false, message: 'Failed to get income-expense report' });
    }
  });

  // Trend report - biểu đồ đường (6 tháng gần nhất)
  fastify.get('/trend', async (request: any, reply: any) => {
    const { months = 6 } = request.query;
    const userId = request.user.userId;

    try {
      const data: { date: string; income: string; expense: string; savings: string }[] = [];

      for (let i = parseInt(months as string) - 1; i >= 0; i--) {
        const d = new Date();
        d.setMonth(d.getMonth() - i);
        const year = d.getFullYear();
        const month = d.getMonth();
        const startOfMonth = new Date(year, month, 1).getTime() / 1000;
        const endOfMonth = new Date(year, month + 1, 0, 23, 59, 59).getTime() / 1000;

        const incomeRes: any[] = await query(
          `SELECT COALESCE(SUM(amount), 0) as total FROM transactions WHERE user_id = $1 AND type = 'INCOME' AND date >= $2 AND date <= $3`,
          [userId, startOfMonth, endOfMonth]
        );
        const expenseRes: any[] = await query(
          `SELECT COALESCE(SUM(amount), 0) as total FROM transactions WHERE user_id = $1 AND type = 'EXPENSE' AND date >= $2 AND date <= $3`,
          [userId, startOfMonth, endOfMonth]
        );

        const incomeAmt = parseFloat(incomeRes[0]?.total || '0');
        const expenseAmt = parseFloat(expenseRes[0]?.total || '0');

        data.push({
          date: `${year}-${String(month + 1).padStart(2, '0')}`,
          income: incomeAmt.toString(),
          expense: expenseAmt.toString(),
          savings: (incomeAmt - expenseAmt).toString()
        });
      }

      return reply.send({
        success: true,
        data: {
          period: 'MONTHLY',
          months: parseInt(months as string),
          income: data.map(d => ({ date: d.date, amount: d.income })),
          expense: data.map(d => ({ date: d.date, amount: d.expense })),
          savings: data.map(d => ({ date: d.date, amount: d.savings }))
        }
      });
    } catch (error: any) {
      fastify.log.error(error);
      return reply.status(500).send({ success: false, message: 'Failed to get trend report' });
    }
  });
}
