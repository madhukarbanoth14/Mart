import { IsEmail, IsOptional, IsString, Matches, MinLength } from 'class-validator';

const INDIAN_PHONE = /^[6-9]\d{9}$/;

export class CreateEmployeeDto {
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
}
