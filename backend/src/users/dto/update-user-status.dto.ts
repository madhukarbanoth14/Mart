import { IsOptional, IsString, MaxLength } from 'class-validator';

export class UpdateUserStatusDto {
  @IsOptional()
  @IsString()
  @MaxLength(500)
  reason?: string;
}
