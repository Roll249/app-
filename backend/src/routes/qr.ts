import { query, queryOne, execute } from '../utils/db.js';
import QRCode from 'qrcode';
import crypto from 'crypto';

function crc16(data: string): string {
  let crc = 0xFFFF;
  for (let i = 0; i < data.length; i++) {
    crc ^= data.charCodeAt(i) << 8;
    for (let j = 0; j < 8; j++) {
      crc = (crc << 1) ^ (crc & 0x8000 ? 0x1021 : 0);
    }
  }
  return (crc & 0xFFFF).toString(16).toUpperCase().padStart(4, '0');
}

function buildVietQR(params: {
  bankCode: string;
  accountNumber: string;
  amount: number;
  message: string;
  merchantId?: string;
}): string {
  const { bankCode, accountNumber, amount, message } = params;

  const bankInfoMap: Record<string, { vietqrPrefix: string; name: string }> = {
    VCB: { vietqrPrefix: '970436', name: 'Vietcombank' },
    VTB: { vietqrPrefix: '970415', name: 'VietinBank' },
    BIDV: { vietqrPrefix: '970418', name: 'BIDV' },
    TPB: { vietqrPrefix: '970423', name: 'TPBank' },
    ACB: { vietqrPrefix: '970416', name: 'ACB' },
    MB: { vietqrPrefix: '970422', name: 'MBBank' },
    SHB: { vietqrPrefix: '970429', name: 'SHB' },
    OCB: { vietqrPrefix: '970448', name: 'OCB' },
    HDB: { vietqrPrefix: '970437', name: 'HDBank' },
    VIB: { vietqrPrefix: '970441', name: 'VIB' },
  };

  const bankInfo = bankInfoMap[bankCode] || { vietqrPrefix: '970436', name: bankCode };

  const gui = '00';
  const guiField = gui + '01';

  const VisaPayloadFormat = '000201';
  const VisaMerchantIdentifier = '38';
  const visaGuidValue = guiField + '0' + VisaMerchantIdentifier.length + (gui ?? '');

  const atm = '00';
  const atmField = atm + '01';

  const VisaVisaToken = '380';

  const providerConcat = guiField + VisaMerchantIdentifier + '0' + VisaVisaToken.length + VisaVisaToken;

  const merchantData = providerConcat;

  const VisaMerchantName = '02';
  const VisaMerchantCity = '03';
  const VisaCategoryCode = '52';
  const VisaTransactionCurrency = '53';
  const VisaTransactionAmount = '54';
  const VisaCountryCode = '58';
  const VisaChecksum = '63';

  const fixGUI = '000201010212';
  const visaTag = '38580010';
  const guiData = 'A0000007270123';

  const accountPart = bankInfo.vietqrPrefix.padEnd(10, '0');
  const accountField = '0016' + accountPart;

  const bankNamePart = bankInfo.name.toUpperCase();
  const bankNameField = String(bankNamePart.length).padStart(2, '0') + bankNamePart;

  const addDataTemplate = '0202';
  const refId = '08TRAN';
  const refField = refId.length.toString().padStart(2, '0') + refId;

  let rawData = fixGUI + guiData + '0010' + VisaVisaToken;
  rawData += '38' + '06' + bankInfo.vietqrPrefix;
  rawData += '52045399';
  rawData += '5802VN';

  if (amount > 0) {
    const amtStr = Math.round(amount).toString();
    rawData += '54' + String(amtStr.length).padStart(2, '0') + amtStr;
  }

  rawData += '530303704';
  rawData += '5802VN';
  rawData += '6008VN';
  rawData += '0808TRAN0009';
  rawData += addDataTemplate + refField;
  rawData += '0208TRAN0009';

  if (message) {
    const msgBytes = message.length;
    rawData += '08' + String(msgBytes).padStart(2, '0') + message;
  }

  const crc = crc16(rawData);
  return rawData + '6304' + crc;
}

export async function qrRoutes(fastify: any) {
  // Generate VietQR for receiving money
  fastify.post('/generate', async (request: any, reply: any) => {
    const { amount, message, accountId, bankCode } = request.body;
    const userId = request.user.userId;
    const { v4: uuidv4 } = require('uuid');
    const id = uuidv4();
    const now = Math.floor(Date.now() / 1000);
    const expiresAt = now + 300;

    try {
      let accountNumber = '1234567890';
      let actualBankCode = bankCode || 'VCB';
      let accountHolderName = 'Demo User';

      if (accountId) {
        const bankAccount: any = await queryOne(
          `SELECT uba.*, sb.code as bank_code, sb.short_name as bank_name
           FROM user_bank_accounts uba
           JOIN simulated_banks sb ON uba.bank_id = sb.id
           WHERE uba.id = $1 AND uba.user_id = $2`,
          [accountId, userId]
        );
        if (bankAccount) {
          accountNumber = bankAccount.account_number;
          actualBankCode = bankAccount.bank_code;
          accountHolderName = bankAccount.account_holder_name;
        }
      }

      const vietqrPayload = buildVietQR({
        bankCode: actualBankCode,
        accountNumber,
        amount: amount || 0,
        message: message || 'Thanh toán qua Fintech App',
      });

      const signature = crypto.createHmac('sha256', process.env.JWT_SECRET || 'dev-secret')
        .update(vietqrPayload).digest('hex');

      await execute(
        `INSERT INTO qr_codes (id, user_id, type, payload, signature, amount, message, account_id, expires_at, created_at)
         VALUES ($1, $2, 'RECEIVE', $3, $4, $5, $6, $7, $8, $9)`,
        [id, userId, vietqrPayload, signature, amount || null, message || null, accountId || null, expiresAt, now]
      );

      const qrImage = await QRCode.toDataURL(vietqrPayload, { width: 400, margin: 2, color: { dark: '#000000', light: '#FFFFFF' } });

      return reply.status(201).send({
        success: true,
        data: {
          id,
          type: 'RECEIVE',
          format: 'VIETQR',
          qrImage,
          qrContent: vietqrPayload,
          payload: vietqrPayload,
          signature,
          amount: amount || 0,
          message: message || 'Thanh toán qua Fintech App',
          expiresAt,
          createdAt: new Date(now * 1000).toISOString(),
          bankInfo: {
            code: actualBankCode,
            accountNumber,
            accountHolderName,
            amount: amount || 0
          }
        }
      });
    } catch (error: any) {
      fastify.log.error(error);
      return reply.status(500).send({ success: false, message: 'Failed to generate VietQR' });
    }
  });

  // Generate VietQR for bank transfer (from one bank to another)
  fastify.post('/generate-transfer', async (request: any, reply: any) => {
    const { fromBankAccountId, toBankCode, toAccountNumber, toAccountName, amount, message } = request.body;
    const userId = request.user.userId;
    const { v4: uuidv4 } = require('uuid');
    const id = uuidv4();
    const now = Math.floor(Date.now() / 1000);
    const expiresAt = now + 300;

    try {
      const fromBankAccount: any = await queryOne(
        `SELECT uba.*, sb.code as bank_code, sb.vietqr_prefix
         FROM user_bank_accounts uba
         JOIN simulated_banks sb ON uba.bank_id = sb.id
         WHERE uba.id = $1 AND uba.user_id = $2`,
        [fromBankAccountId, userId]
      );

      if (!fromBankAccount) {
        return reply.status(404).send({ success: false, message: 'Source bank account not found' });
      }

      const toBank: any = await queryOne('SELECT * FROM simulated_banks WHERE code = $1', [toBankCode]);
      if (!toBank) {
        return reply.status(404).send({ success: false, message: 'Destination bank not found' });
      }

      const vietqrPayload = buildVietQR({
        bankCode: toBankCode,
        accountNumber: toAccountNumber,
        amount: amount || 0,
        message: message || `Chuyen khoan tu ${fromBankAccount.bank_code}`,
      });

      const signature = crypto.createHmac('sha256', process.env.JWT_SECRET || 'dev-secret')
        .update(vietqrPayload).digest('hex');

      await execute(
        `INSERT INTO qr_codes (id, user_id, type, payload, signature, amount, message, account_id, expires_at, created_at)
         VALUES ($1, $2, 'TRANSFER', $3, $4, $5, $6, $7, $8, $9)`,
        [id, userId, vietqrPayload, signature, amount || null, message || null, fromBankAccountId, expiresAt, now]
      );

      const qrImage = await QRCode.toDataURL(vietqrPayload, { width: 400, margin: 2 });

      return reply.status(201).send({
        success: true,
        data: {
          id,
          type: 'TRANSFER',
          format: 'VIETQR',
          qrImage,
          qrContent: vietqrPayload,
          payload: vietqrPayload,
          signature,
          amount: amount || 0,
          message,
          expiresAt,
          fromBank: {
            accountId: fromBankAccountId,
            code: fromBankAccount.bank_code,
            accountNumber: fromBankAccount.account_number,
            currentBalance: fromBankAccount.balance.toString()
          },
          toBank: {
            code: toBankCode,
            name: toBank.name,
            accountNumber: toAccountNumber,
            accountHolderName: toAccountName || 'Unknown'
          }
        }
      });
    } catch (error: any) {
      fastify.log.error(error);
      return reply.status(500).send({ success: false, message: 'Failed to generate transfer QR' });
    }
  });

  // Validate and process QR code
  fastify.post('/validate', async (request: any, reply: any) => {
    const { qrContent } = request.body;
    const userId = request.user.userId;
    const now = Math.floor(Date.now() / 1000);

    try {
      if (!qrContent || qrContent.length < 20) {
        return reply.status(400).send({ success: false, message: 'Invalid QR data' });
      }

      const existingQR: any = await queryOne(
        `SELECT * FROM qr_codes WHERE payload = $1 AND user_id = $2 AND is_active = true ORDER BY created_at DESC LIMIT 1`,
        [qrContent, userId]
      );

      if (existingQR && existingQR.expires_at < now) {
        return reply.status(400).send({ success: false, message: 'QR code has expired' });
      }

      const isVietQR = qrContent.startsWith('000201');
      let parsedData: any = { format: 'UNKNOWN', raw: qrContent };

      if (isVietQR) {
        parsedData = {
          format: 'VIETQR',
          raw: qrContent,
          amount: extractVietQRField(qrContent, '54'),
          message: extractVietQRField(qrContent, '08'),
          merchantId: extractVietQRField(qrContent, '06'),
        };
      } else {
        try {
          parsedData = JSON.parse(qrContent);
          parsedData.format = 'APP_QR';
        } catch {
          parsedData.format = 'RAW';
        }
      }

      return reply.send({
        success: true,
        data: {
          valid: true,
          format: parsedData.format,
          amount: parsedData.amount || null,
          message: parsedData.message || null,
          rawData: parsedData.raw || qrContent,
          expiresIn: existingQR ? Math.max(0, existingQR.expires_at - now) : null,
          usedAt: existingQR?.used_at || null
        }
      });
    } catch (error: any) {
      return reply.status(500).send({ success: false, message: 'Failed to validate QR' });
    }
  });

  // Process QR (mark as used and create transaction)
  fastify.post('/process', async (request: any, reply: any) => {
    const { qrContent, accountId } = request.body;
    const userId = request.user.userId;
    const now = Math.floor(Date.now() / 1000);

    try {
      const qrRecord: any = await queryOne(
        `SELECT * FROM qr_codes WHERE payload = $1 AND is_active = true ORDER BY created_at DESC LIMIT 1`,
        [qrContent]
      );

      if (!qrRecord) {
        return reply.status(404).send({ success: false, message: 'QR code not found' });
      }

      if (qrRecord.expires_at < now) {
        return reply.status(400).send({ success: false, message: 'QR code has expired' });
      }

      if (qrRecord.used_at) {
        return reply.status(400).send({ success: false, message: 'QR code already used' });
      }

      const amount = parseFloat(qrRecord.amount || '0');
      const isReceive = qrRecord.user_id === userId;

      await execute('UPDATE qr_codes SET used_at = $1, used_by = $2 WHERE id = $3', [now, userId, qrRecord.id]);

      return reply.send({
        success: true,
        data: {
          processed: true,
          type: isReceive ? 'RECEIVED' : 'PAID',
          amount: amount,
          message: qrRecord.message,
          transactionId: qrRecord.id,
          timestamp: new Date(now * 1000).toISOString()
        }
      });
    } catch (error: any) {
      return reply.status(500).send({ success: false, message: 'Failed to process QR' });
    }
  });
}

function extractVietQRField(data: string, tag: string): string | null {
  const regex = new RegExp(tag + '(\\d{2})(.+?)(?=\\d{2}|$)', 'g');
  const match = regex.exec(data);
  if (match) {
    const len = parseInt(match[1]);
    return match[2].substring(0, len);
  }
  return null;
}
