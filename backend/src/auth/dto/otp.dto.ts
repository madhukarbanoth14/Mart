import { IsIn, IsNotEmpty, IsString, Length, Matches } from 'class-validator';

export class SendOtpDto {
  @IsString()
  @IsNotEmpty()
  phone!: string;

  @IsIn(['REGISTER'])
  purpose: 'REGISTER' = 'REGISTER';
}

export class VerifyOtpDto {
  @IsString()
  @IsNotEmpty()
  phone!: string;

  @IsString()
  @Length(6, 6)
  @Matches(/^\d{6}$/)
  code!: string;
}
