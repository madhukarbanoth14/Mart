import { Injectable, Logger } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';

export type SendSmsResult =
  | { sent: true; requestId: string }
  | { sent: false; reason: string };

export type VerifyCheckResult =
  | { approved: true }
  | { approved: false; reason: string };

@Injectable()
export class SmsService {
  private readonly logger = new Logger(SmsService.name);

  constructor(private readonly config: ConfigService) {}

  /** Twilio Verify (VA…) — recommended for OTP. */
  isVerifyConfigured(): boolean {
    return Boolean(
      this.accountSid() && this.authToken() && this.verifyServiceSid(),
    );
  }

  /** Legacy Programmable Messaging (MG… / phone number). */
  isMessagingConfigured(): boolean {
    return Boolean(
      this.accountSid() && this.authToken() && this.senderConfigured(),
    );
  }

  isConfigured(): boolean {
    return this.isVerifyConfigured() || this.isMessagingConfigured();
  }

  private accountSid(): string {
    return this.config.get<string>('sms.twilioAccountSid', '')?.trim() ?? '';
  }

  private authToken(): string {
    return this.config.get<string>('sms.twilioAuthToken', '')?.trim() ?? '';
  }

  private verifyServiceSid(): string {
    const explicit =
      this.config.get<string>('sms.twilioVerifyServiceSid', '')?.trim() ?? '';
    if (explicit) return explicit;

    const legacyMessaging =
      this.config.get<string>('sms.twilioMessagingServiceSid', '')?.trim() ?? '';
    if (legacyMessaging.startsWith('VA')) return legacyMessaging;
    return '';
  }

  private messagingServiceSid(): string {
    const sid =
      this.config.get<string>('sms.twilioMessagingServiceSid', '')?.trim() ?? '';
    return sid.startsWith('MG') ? sid : '';
  }

  private smsFrom(): string {
    return this.config.get<string>('sms.twilioSmsFrom', '')?.trim() ?? '';
  }

  private senderConfigured(): boolean {
    return Boolean(this.messagingServiceSid() || this.smsFrom());
  }

  private authHeader(): string {
    const accountSid = this.accountSid();
    const authToken = this.authToken();
    return `Basic ${Buffer.from(`${accountSid}:${authToken}`).toString('base64')}`;
  }

  private otpExpiryMinutes(): number {
    const raw = this.config.get<number>('sms.otpExpiryMinutes', 10);
    return Number.isFinite(raw) && raw > 0 ? raw : 10;
  }

  private appName(): string {
    return (
      this.config.get<string>('email.appName', '')?.trim() || 'KNSR Mart'
    );
  }

  /** 10-digit Indian mobile → E.164 +91XXXXXXXXXX. */
  toE164(phone10: string): string {
    return `+91${phone10}`;
  }

  /**
   * Start OTP via Twilio Verify — Twilio generates, sends, and tracks the code.
   * Service SID must start with VA (Verify → My New Verify Service).
   */
  async startVerification(phone10: string): Promise<SendSmsResult> {
    const serviceSid = this.verifyServiceSid();
    const accountSid = this.accountSid();
    const authToken = this.authToken();
    if (!accountSid || !authToken || !serviceSid) {
      return {
        sent: false,
        reason:
          'Twilio Verify not configured. Set TWILIO_VERIFY_SERVICE_SID (VA… from Verify console).',
      };
    }

    const to = this.toE164(phone10);
    const body = new URLSearchParams();
    body.set('To', to);
    body.set('Channel', 'sms');

    const url = `https://verify.twilio.com/v2/Services/${serviceSid}/Verifications`;

    try {
      const response = await fetch(url, {
        method: 'POST',
        headers: {
          Authorization: this.authHeader(),
          'Content-Type': 'application/x-www-form-urlencoded',
          Accept: 'application/json',
        },
        body: body.toString(),
      });

      const raw = await response.text();
      let payload: { sid?: string; status?: string; message?: string } = {};
      try {
        payload = raw ? (JSON.parse(raw) as typeof payload) : {};
      } catch {
        payload = { message: raw.slice(0, 200) };
      }

      if (!response.ok) {
        const reason =
          payload.message?.trim() ||
          `Twilio Verify HTTP ${response.status}: ${raw.slice(0, 120)}`;
        this.logger.error(`Twilio Verify start failed for ${to}: ${reason}`);
        return { sent: false, reason };
      }

      const requestId = payload.sid ?? payload.status ?? 'pending';
      this.logger.log(`Twilio Verify OTP started for ${to} (${requestId})`);
      return { sent: true, requestId: String(requestId) };
    } catch (err) {
      const reason = (err as Error).message;
      this.logger.error(`Twilio Verify start error for ${to}: ${reason}`);
      return { sent: false, reason };
    }
  }

  /** Check OTP code with Twilio Verify. */
  async checkVerification(
    phone10: string,
    code: string,
  ): Promise<VerifyCheckResult> {
    const serviceSid = this.verifyServiceSid();
    if (!this.accountSid() || !this.authToken() || !serviceSid) {
      return { approved: false, reason: 'Twilio Verify not configured' };
    }

    const to = this.toE164(phone10);
    const body = new URLSearchParams();
    body.set('To', to);
    body.set('Code', code.trim());

    const url = `https://verify.twilio.com/v2/Services/${serviceSid}/VerificationCheck`;

    try {
      const response = await fetch(url, {
        method: 'POST',
        headers: {
          Authorization: this.authHeader(),
          'Content-Type': 'application/x-www-form-urlencoded',
          Accept: 'application/json',
        },
        body: body.toString(),
      });

      const raw = await response.text();
      let payload: { status?: string; message?: string } = {};
      try {
        payload = raw ? (JSON.parse(raw) as typeof payload) : {};
      } catch {
        payload = { message: raw.slice(0, 200) };
      }

      if (payload.status === 'approved') {
        return { approved: true };
      }

      const reason =
        payload.message?.trim() ||
        (payload.status ? `status: ${payload.status}` : `HTTP ${response.status}`);
      this.logger.warn(`Twilio Verify check failed for ${to}: ${reason}`);
      return { approved: false, reason };
    } catch (err) {
      return { approved: false, reason: (err as Error).message };
    }
  }

  private buildOtpMessage(code: string): string {
    const template =
      this.config.get<string>('sms.otpMessageTemplate', '')?.trim() ||
      'Your {{appName}} verification code is {{code}}. Valid for {{expiry}} minutes. Do not share it with anyone.';
    return template
      .replace(/\{\{appName\}\}/g, this.appName())
      .replace(/\{\{code\}\}/g, code)
      .replace(/\{\{expiry\}\}/g, String(this.otpExpiryMinutes()));
  }

  /**
   * Legacy: send a custom OTP via Programmable Messaging (MG… or TWILIO_SMS_FROM).
   */
  async sendRegisterOtp(params: {
    phone10: string;
    code: string;
  }): Promise<SendSmsResult> {
    const accountSid = this.accountSid();
    const authToken = this.authToken();
    if (!accountSid || !authToken || !this.senderConfigured()) {
      return {
        sent: false,
        reason:
          'SMS not configured. Set TWILIO_MESSAGING_SERVICE_SID (MG…) or TWILIO_SMS_FROM.',
      };
    }

    const to = this.toE164(params.phone10);
    const body = new URLSearchParams();
    body.set('To', to);
    body.set('Body', this.buildOtpMessage(params.code));

    const messagingServiceSid = this.messagingServiceSid();
    if (messagingServiceSid) {
      body.set('MessagingServiceSid', messagingServiceSid);
    } else {
      body.set('From', this.smsFrom());
    }

    const url = `https://api.twilio.com/2010-04-01/Accounts/${accountSid}/Messages.json`;

    try {
      const response = await fetch(url, {
        method: 'POST',
        headers: {
          Authorization: this.authHeader(),
          'Content-Type': 'application/x-www-form-urlencoded',
          Accept: 'application/json',
        },
        body: body.toString(),
      });

      const raw = await response.text();
      let payload: { sid?: string; message?: string } = {};
      try {
        payload = raw ? (JSON.parse(raw) as typeof payload) : {};
      } catch {
        payload = { message: raw.slice(0, 200) };
      }

      if (!response.ok) {
        const reason =
          payload.message?.trim() ||
          `Twilio HTTP ${response.status}: ${raw.slice(0, 120)}`;
        this.logger.error(`Twilio SMS failed for ${to}: ${reason}`);
        return { sent: false, reason };
      }

      const requestId = payload.sid ?? 'ok';
      this.logger.log(`OTP SMS sent via Twilio Messaging to ${to} (${requestId})`);
      return { sent: true, requestId };
    } catch (err) {
      const reason = (err as Error).message;
      this.logger.error(`Twilio SMS error for ${to}: ${reason}`);
      return { sent: false, reason };
    }
  }
}
