import { IsEmail, IsOptional, IsString, Matches, MinLength } from 'class-validator';

const INDIAN_PHONE = /^[6-9]\d{9}$/;

export class CreateShopkeeperDto {
  @IsString()
  @MinLength(2)
  name!: string;

  @IsEmail()
  email!: string;

  @IsOptional()
  @IsString()
  @Matches(INDIAN_PHONE, { message: 'Phone must be a 10-digit Indian mobile number' })
  phone?: string;

  /** If omitted, a secure random password is generated. Min 8 when provided. */
  @IsOptional()
  @IsString()
  @MinLength(8)
  password?: string;

  @IsString()
  @MinLength(1)
  areaId!: string;

  /** Free text: KYC notes, comma-separated attachment file names, etc. */
  @IsOptional()
  @IsString()
  onboardingNotes?: string;
}
