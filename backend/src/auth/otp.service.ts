import {
  BadRequestException,
  Injectable,
  Logger,
  ServiceUnavailableException,
} from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { JwtService } from '@nestjs/jwt';
import { randomInt } from 'crypto';
import { PrismaService } from '../prisma/prisma.service';
import { SmsService } from '../sms/sms.service';

const OTP_TTL_MS = 10 * 60 * 1000;
const OTP_VERIFY_TTL = '15m';

@Injectable()
export class OtpService {
  private readonly logger = new Logger(OtpService.name);

  constructor(
    private readonly prisma: PrismaService,
    private readonly jwtService: JwtService,
    private readonly sms: SmsService,
    private readonly config: ConfigService,
  ) {}

  normalizePhone(phone: string): string {
    let digits = phone.replace(/\D/g, '');
    if (digits.length === 12 && digits.startsWith('91')) {
      digits = digits.slice(2);
    } else if (digits.length === 11 && digits.startsWith('0')) {
      digits = digits.slice(1);
    }
    if (digits.length !== 10) {
      throw new BadRequestException('Enter a valid 10-digit mobile number');
    }
    return digits;
  }

  async sendRegisterOtp(phone: string) {
    const normalized = this.normalizePhone(phone);
    const existing = await this.prisma.user.findUnique({
      where: { phone: normalized },
      select: { id: true },
    });
    if (existing) {
      throw new BadRequestException('This mobile number is already registered');
    }

    const nodeEnv = this.config.get<string>('nodeEnv', 'development');
    const isProduction = nodeEnv === 'production';
    const useVerify = this.sms.isVerifyConfigured();
    const useMessaging = !useVerify && this.sms.isMessagingConfigured();
    const smsReady = useVerify || useMessaging;

    if (isProduction && !smsReady) {
      this.logger.error(
        'Registration OTP requested but Twilio is not configured in production',
      );
      throw new ServiceUnavailableException(
        'OTP service is temporarily unavailable. Please try again later.',
      );
    }

    await this.prisma.phoneOtp.deleteMany({
      where: { phone: normalized, purpose: 'REGISTER' },
    });

    if (useVerify) {
      const verifyResult = await this.sms.startVerification(normalized);
      if (!verifyResult.sent) {
        this.logger.error(
          `Failed to start Twilio Verify for ${normalized}: ${verifyResult.reason}`,
        );
        throw new ServiceUnavailableException(
          'Unable to send OTP to this number. Please try again in a moment.',
        );
      }

      return {
        success: true,
        message: 'OTP sent to your mobile number',
        expiresInSeconds: OTP_TTL_MS / 1000,
      };
    }

    const code =
      !smsReady && !isProduction
        ? '123456'
        : String(randomInt(100000, 999999));
    const expiresAt = new Date(Date.now() + OTP_TTL_MS);

    await this.prisma.phoneOtp.create({
      data: {
        phone: normalized,
        code,
        purpose: 'REGISTER',
        expiresAt,
      },
    });

    if (useMessaging) {
      const smsResult = await this.sms.sendRegisterOtp({
        phone10: normalized,
        code,
      });
      if (!smsResult.sent) {
        await this.prisma.phoneOtp.deleteMany({
          where: { phone: normalized, purpose: 'REGISTER' },
        });
        this.logger.error(
          `Failed to send OTP SMS to ${normalized}: ${smsResult.reason}`,
        );
        throw new ServiceUnavailableException(
          'Unable to send OTP to this number. Please try again in a moment.',
        );
      }
    } else {
      this.logger.log(`Dev OTP for ${normalized}: ${code}`);
    }

    return {
      success: true,
      message: 'OTP sent to your mobile number',
      expiresInSeconds: OTP_TTL_MS / 1000,
      ...(!smsReady && !isProduction ? { devOtp: code } : {}),
    };
  }

  async verifyRegisterOtp(phone: string, code: string) {
    const normalized = this.normalizePhone(phone);
    const trimmedCode = code.trim();

    if (this.sms.isVerifyConfigured()) {
      const check = await this.sms.checkVerification(normalized, trimmedCode);
      if (!check.approved) {
        throw new BadRequestException('Invalid or expired OTP');
      }
    } else {
      const row = await this.prisma.phoneOtp.findFirst({
        where: {
          phone: normalized,
          purpose: 'REGISTER',
          expiresAt: { gt: new Date() },
        },
        orderBy: { createdAt: 'desc' },
      });
      if (!row || row.code !== trimmedCode) {
        throw new BadRequestException('Invalid or expired OTP');
      }

      await this.prisma.phoneOtp.deleteMany({
        where: { phone: normalized, purpose: 'REGISTER' },
      });
    }

    const verificationToken = await this.jwtService.signAsync(
      { phone: normalized, purpose: 'REGISTER' },
      { expiresIn: OTP_VERIFY_TTL },
    );

    return { success: true, verificationToken, phone: normalized };
  }

  async assertRegisterVerificationToken(
    verificationToken: string,
    phone: string,
  ): Promise<string> {
    const normalized = this.normalizePhone(phone);
    let payload: { phone?: string; purpose?: string };
    try {
      payload = await this.jwtService.verifyAsync(verificationToken);
    } catch {
      throw new BadRequestException('Phone verification expired. Request a new OTP.');
    }
    if (payload.purpose !== 'REGISTER' || payload.phone !== normalized) {
      throw new BadRequestException('Phone verification mismatch');
    }
    return normalized;
  }
}
