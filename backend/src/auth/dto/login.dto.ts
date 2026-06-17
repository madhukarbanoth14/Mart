import { IsOptional, IsString, MinLength } from 'class-validator';

export class LoginDto {
  /** Login id can be email or phone. */
  @IsOptional()
  @IsString()
  identifier?: string;

  /** Backward compatibility for older clients still posting { email }. */
  @IsOptional()
  @IsString()
  email?: string;

  @IsString()
  @MinLength(6)
  password!: string;
}
