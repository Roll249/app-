/**
 * Notification Service - External Service Integration
 * Handles push notifications via Firebase Cloud Messaging (FCM)
 * 
 * This service abstracts notification delivery and can be extended to support:
 * - Firebase Cloud Messaging (FCM)
 * - Apple Push Notification Service (APNs)
 * - Email services (SendGrid, Mailgun)
 * - SMS services (Twilio, VNPT)
 */
import { BaseService, ServiceConfig, ServiceHealth } from '../base/BaseService.js';

export type NotificationType = 
  | 'TRANSACTION' 
  | 'BUDGET_ALERT' 
  | 'FUND_GOAL' 
  | 'BILL_REMINDER' 
  | 'SECURITY' 
  | 'MARKETING';

export type NotificationChannel = 'PUSH' | 'EMAIL' | 'SMS';

export interface NotificationPayload {
  title: string;
  body: string;
  data?: Record<string, string>;
  priority?: 'HIGH' | 'NORMAL' | 'LOW';
  badge?: number;
  sound?: string;
}

export interface NotificationRecipient {
  userId: string;
  fcmToken?: string;
  email?: string;
  phone?: string;
}

export interface NotificationResult {
  success: boolean;
  messageId?: string;
  error?: string;
}

export interface UserNotificationPreferences {
  userId: string;
  pushEnabled: boolean;
  emailEnabled: boolean;
  smsEnabled: boolean;
  typesEnabled: NotificationType[];
  quietHoursStart?: string; // HH:mm format
  quietHoursEnd?: string;
}

export interface ScheduledNotification {
  id: string;
  userId: string;
  notificationType: NotificationType;
  payload: NotificationPayload;
  scheduledAt: Date;
  sent: boolean;
}

export class NotificationService extends BaseService {
  private fcmEnabled: boolean = false;
  private fcmServerKey?: string;
  private emailEnabled: boolean = false;
  private smsEnabled: boolean = false;
  private userPreferences: Map<string, UserNotificationPreferences> = new Map();

  constructor(config: ServiceConfig, logger: any) {
    super(config, logger);
  }

  getName(): string {
    return 'NotificationService';
  }

  async initialize(): Promise<void> {
    this.logger.info(`[${this.getName()}] Initializing notification service...`);

    // Load FCM configuration
    this.fcmServerKey = process.env.FCM_SERVER_KEY;
    this.fcmEnabled = !!this.fcmServerKey;

    // Load email configuration
    this.emailEnabled = !!process.env.EMAIL_API_KEY;

    // Load SMS configuration
    this.smsEnabled = !!process.env.SMS_API_KEY;

    this.isInitialized = true;
    this.logger.info(`[${this.getName()}] Notification service initialized. FCM: ${this.fcmEnabled}, Email: ${this.emailEnabled}, SMS: ${this.smsEnabled}`);
  }

  protected async ping(): Promise<boolean> {
    return this.fcmEnabled || this.emailEnabled || this.smsEnabled;
  }

  async getHealth(): Promise<ServiceHealth> {
    const startTime = Date.now();
    const services: string[] = [];
    
    if (this.fcmEnabled) services.push('FCM');
    if (this.emailEnabled) services.push('Email');
    if (this.smsEnabled) services.push('SMS');

    return {
      status: services.length > 0 ? 'healthy' : 'degraded',
      latencyMs: Date.now() - startTime,
      message: `Enabled channels: ${services.join(', ') || 'none'}`,
      lastChecked: new Date()
    };
  }

  async sendNotification(
    recipient: NotificationRecipient,
    payload: NotificationPayload,
    channels: NotificationChannel[] = ['PUSH']
  ): Promise<NotificationResult> {
    this.logger.info(`[${this.getName()}] Sending notification to user ${recipient.userId}: ${payload.title}`);

    const results: NotificationResult[] = [];

    for (const channel of channels) {
      try {
        switch (channel) {
          case 'PUSH':
            if (recipient.fcmToken) {
              results.push(await this.sendPushNotification(recipient.fcmToken, payload));
            }
            break;
          case 'EMAIL':
            if (recipient.email) {
              results.push(await this.sendEmail(recipient.email, payload));
            }
            break;
          case 'SMS':
            if (recipient.phone) {
              results.push(await this.sendSMS(recipient.phone, payload.body));
            }
            break;
        }
      } catch (error: any) {
        this.logger.error(`[${this.getName()}] Error sending via ${channel}: ${error.message}`);
        results.push({ success: false, error: error.message });
      }
    }

    const success = results.some(r => r.success);
    const firstSuccess = results.find(r => r.success);

    return {
      success,
      messageId: firstSuccess?.messageId,
      error: success ? undefined : results.map(r => r.error).join(', ')
    };
  }

  private async sendPushNotification(fcmToken: string, payload: NotificationPayload): Promise<NotificationResult> {
    if (!this.fcmEnabled) {
      this.logger.warn(`[${this.getName()}] FCM not enabled, skipping push notification`);
      return { success: false, error: 'FCM not configured' };
    }

    try {
      const response = await fetch('https://fcm.googleapis.com/fcm/send', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `key=${this.fcmServerKey}`
        },
        body: JSON.stringify({
          to: fcmToken,
          notification: {
            title: payload.title,
            body: payload.body,
            badge: payload.badge || 0,
            sound: payload.sound || 'default'
          },
          data: payload.data || {},
          priority: payload.priority || 'NORMAL'
        })
      });

      if (response.ok) {
        const result = await response.json();
        return {
          success: true,
          messageId: result.results?.[0]?.message_id
        };
      } else {
        const error = await response.text();
        return { success: false, error };
      }
    } catch (error: any) {
      return { success: false, error: error.message };
    }
  }

  private async sendEmail(email: string, payload: NotificationPayload): Promise<NotificationResult> {
    if (!this.emailEnabled) {
      return { success: false, error: 'Email service not configured' };
    }

    // STUB: This is a placeholder. To enable real email sending:
    // 1. Add EMAIL_API_KEY to backend/.env (SendGrid, Mailgun, etc.)
    // 2. Replace this block with actual HTTP call to your email provider.
    // See docs/THIRD_PARTY_SERVICES.md for provider recommendations.
    this.logger.warn(`[${this.getName()}] [STUB] Would send email to ${email}: ${payload.title}`);
    return { success: true, messageId: `email_stub_${Date.now()}` };
  }

  private async sendSMS(phone: string, message: string): Promise<NotificationResult> {
    if (!this.smsEnabled) {
      return { success: false, error: 'SMS service not configured' };
    }

    // STUB: This is a placeholder. To enable real SMS sending:
    // 1. Add SMS_API_KEY and SMS_FROM to backend/.env (Twilio, VNPT, etc.)
    // 2. Replace this block with actual HTTP call to your SMS provider.
    // See docs/THIRD_PARTY_SERVICES.md for provider recommendations.
    this.logger.warn(`[${this.getName()}] [STUB] Would send SMS to ${phone}: ${message.substring(0, 50)}...`);
    return { success: true, messageId: `sms_stub_${Date.now()}` };
  }

  async setUserPreferences(preferences: UserNotificationPreferences): Promise<void> {
    this.userPreferences.set(preferences.userId, preferences);
    this.logger.info(`[${this.getName()}] Updated preferences for user ${preferences.userId}`);
  }

  async getUserPreferences(userId: string): Promise<UserNotificationPreferences | null> {
    return this.userPreferences.get(userId) || null;
  }

  // Pre-defined notification templates
  async notifyTransaction(userId: string, transactionData: {
    amount: number;
    type: 'INCOME' | 'EXPENSE';
    categoryName: string;
    accountName: string;
  }): Promise<NotificationResult> {
    const isIncome = transactionData.type === 'INCOME';
    const prefix = isIncome ? '+' : '-';
    const formattedAmount = new Intl.NumberFormat('vi-VN').format(transactionData.amount);

    return this.sendNotification(
      { userId },
      {
        title: isIncome ? 'Đã nhận tiền' : 'Đã chi tiêu',
        body: `${prefix}${formattedAmount} VNĐ - ${transactionData.categoryName}`,
        data: {
          type: 'TRANSACTION',
          transactionType: transactionData.type
        },
        priority: 'HIGH'
      }
    );
  }

  async notifyBudgetAlert(userId: string, data: {
    budgetName: string;
    percentUsed: number;
    remainingAmount: number;
  }): Promise<NotificationResult> {
    const formattedAmount = new Intl.NumberFormat('vi-VN').format(data.remainingAmount);

    return this.sendNotification(
      { userId },
      {
        title: 'Cảnh báo ngân sách',
        body: `${data.budgetName} đã sử dụng ${data.percentUsed}%. Còn lại: ${formattedAmount} VNĐ`,
        data: {
          type: 'BUDGET_ALERT'
        },
        priority: 'HIGH'
      }
    );
  }

  async notifyFundGoal(userId: string, data: {
    fundName: string;
    progress: number;
    targetAmount: number;
  }): Promise<NotificationResult> {
    const formattedAmount = new Intl.NumberFormat('vi-VN').format(data.targetAmount);

    return this.sendNotification(
      { userId },
      {
        title: 'Mục tiêu tiết kiệm',
        body: `${data.fundName} đã đạt ${data.progress}%! Mục tiêu: ${formattedAmount} VNĐ`,
        data: {
          type: 'FUND_GOAL'
        },
        priority: 'NORMAL'
      }
    );
  }

  async notifyBillReminder(userId: string, data: {
    billName: string;
    amount: number;
    dueDate: string;
  }): Promise<NotificationResult> {
    const formattedAmount = new Intl.NumberFormat('vi-VN').format(data.amount);

    return this.sendNotification(
      { userId },
      {
        title: 'Nhắc hóa đơn',
        body: `${data.billName} sắp đến hạn: ${formattedAmount} VNĐ. Hạn: ${data.dueDate}`,
        data: {
          type: 'BILL_REMINDER'
        },
        priority: 'HIGH'
      }
    );
  }

  async notifySecurityAlert(userId: string, data: {
    alertType: string;
    description: string;
    timestamp: string;
  }): Promise<NotificationResult> {
    return this.sendNotification(
      { userId },
      {
        title: 'Cảnh báo bảo mật',
        body: `${data.alertType}: ${data.description}`,
        data: {
          type: 'SECURITY',
          alertType: data.alertType
        },
        priority: 'HIGH',
        sound: 'alarm'
      }
    );
  }
}
