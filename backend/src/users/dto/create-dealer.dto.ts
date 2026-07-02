import {
  IsEmail,
  IsLatitude,
  IsLongitude,
  IsOptional,
  IsString,
  Matches,
  MinLength,
} from 'class-validator';

const INDIAN_PHONE = /^[6-9]\d{9}$/;

export class CreateDealerDto {
  @IsString()
  @MinLength(2)
  name!: string;

  @IsEmail()
  email!: string;

  @IsOptional()
  @IsString()
  @Matches(INDIAN_PHONE, { message: 'Phone must be a 10-digit Indian mobile number' })
  phone?: string;

  @IsOptional()
  @IsString()
  @MinLength(8)
  password?: string;

  @IsString()
  @MinLength(1)
  areaId!: string;

  @IsOptional()
  @IsString()
  onboardingNotes?: string;

  /** Shop / business name shown to counterparties (personal name stays private). */
  @IsOptional()
  @IsString()
  shopName?: string;

  /** Postal address used for delivery. */
  @IsOptional()
  @IsString()
  address?: string;

  @IsOptional()
  @IsLatitude()
  latitude?: number;

  @IsOptional()
  @IsLongitude()
  longitude?: number;
}
