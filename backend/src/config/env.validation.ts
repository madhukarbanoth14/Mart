import { plainToInstance } from 'class-transformer';
import {
  IsNotEmpty,
  IsNumberString,
  IsOptional,
  IsString,
  validateSync,
} from 'class-validator';
import { assertSafeDatabaseUrl } from './assert-safe-database-url';

/**
 * Validates process.env at startup. Extend as new configuration is added.
 */
class EnvironmentVariables {
  @IsNotEmpty()
  @IsString()
  DATABASE_URL!: string;

  @IsOptional()
  @IsString()
  NODE_ENV?: string;

  @IsOptional()
  @IsNumberString()
  PORT?: string;

  @IsOptional()
  @IsString()
  JWT_SECRET?: string;

  @IsOptional()
  @IsString()
  JWT_EXPIRES_IN?: string;

  @IsOptional()
  @IsString()
  RAZORPAY_KEY_ID?: string;

  @IsOptional()
  @IsString()
  RAZORPAY_KEY_SECRET?: string;

  @IsOptional()
  @IsString()
  RAZORPAY_WEBHOOK_SECRET?: string;

  @IsOptional()
  @IsString()
  FCM_SERVER_KEY?: string;

  @IsOptional()
  @IsString()
  FIREBASE_PROJECT_ID?: string;

  @IsOptional()
  @IsString()
  SMTP_HOST?: string;

  @IsOptional()
  @IsString()
  SMTP_PORT?: string;

  @IsOptional()
  @IsString()
  SMTP_SECURE?: string;

  @IsOptional()
  @IsString()
  SMTP_USER?: string;

  @IsOptional()
  @IsString()
  SMTP_PASS?: string;

  @IsOptional()
  @IsString()
  MAIL_FROM?: string;

  @IsOptional()
  @IsString()
  MAIL_APP_NAME?: string;

  @IsOptional()
  @IsString()
  TWILIO_ACCOUNT_SID?: string;

  @IsOptional()
  @IsString()
  TWILIO_AUTH_TOKEN?: string;

  @IsOptional()
  @IsString()
  TWILIO_VERIFY_SERVICE_SID?: string;

  @IsOptional()
  @IsString()
  TWILIO_MESSAGING_SERVICE_SID?: string;

  @IsOptional()
  @IsString()
  TWILIO_SMS_FROM?: string;

  @IsOptional()
  @IsString()
  TWILIO_SMS_OTP_MESSAGE?: string;

  @IsOptional()
  @IsNumberString()
  SMS_OTP_EXPIRY_MINUTES?: string;
}

export function validateEnv(
  config: Record<string, unknown>,
): Record<string, unknown> {
  const merged = { ...process.env, ...config };
  const validated = plainToInstance(EnvironmentVariables, merged, {
    enableImplicitConversion: true,
  }) as EnvironmentVariables;
  const errors = validateSync(validated, {
    skipMissingProperties: false,
  });
  if (errors.length > 0) {
    const messages = errors
      .map((e) => Object.values(e.constraints ?? {}).join(', '))
      .join('; ');
    throw new Error(`Environment validation failed: ${messages}`);
  }

  assertSafeDatabaseUrl(
    validated.DATABASE_URL,
    validated.NODE_ENV ?? 'development',
  );

  return {
    ...config,
    ...(validated as unknown as Record<string, unknown>),
  } as Record<string, unknown>;
}
