import {
  IsEmail,
  IsIn,
  IsNotEmpty,
  IsNumber,
  IsOptional,
  IsString,
  MinLength,
  ValidateIf,
} from 'class-validator';

class SelfRegisterBaseDto {
  /** Present when signing up with mobile + OTP. */
  @ValidateIf((o: SelfRegisterBaseDto) => !o.email?.trim())
  @IsString()
  @IsNotEmpty()
  verificationToken?: string;

  /** Required with verificationToken (mobile path). */
  @ValidateIf((o: SelfRegisterBaseDto) => Boolean(o.verificationToken?.trim()))
  @IsString()
  @IsNotEmpty()
  phone?: string;

  @IsString()
  @MinLength(2)
  name!: string;

  /** Optional on mobile path; required on email path. */
  @ValidateIf((o: SelfRegisterBaseDto) => !o.verificationToken?.trim())
  @IsEmail()
  email?: string;

  /** Required on email path; optional on mobile path. */
  @ValidateIf((o: SelfRegisterBaseDto) => !o.verificationToken?.trim())
  @IsString()
  @MinLength(8)
  password?: string;

  @IsString()
  @IsNotEmpty()
  areaId!: string;

  @IsString()
  @IsNotEmpty()
  state!: string;

  @IsString()
  @IsNotEmpty()
  district!: string;

  @IsString()
  @IsNotEmpty()
  address!: string;

  @IsOptional()
  @IsNumber()
  latitude?: number;

  @IsOptional()
  @IsNumber()
  longitude?: number;

  @IsOptional()
  @IsString()
  referralCode?: string;
}

export class SelfRegisterShopkeeperDto extends SelfRegisterBaseDto {
  @IsString()
  @IsNotEmpty()
  shopName!: string;
}

export class SelfRegisterDealerDto extends SelfRegisterBaseDto {
  @IsString()
  @IsNotEmpty()
  shopName!: string;
}

export class SelectRoleDto {
  @IsIn(['SHOPKEEPER', 'DEALER'])
  role!: 'SHOPKEEPER' | 'DEALER';
}
