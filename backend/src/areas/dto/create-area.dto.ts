import { IsString, MinLength } from 'class-validator';

export class CreateAreaDto {
  @IsString()
  @MinLength(2)
  name!: string;
}
