import { IsString, MinLength } from 'class-validator';

export class UpdateAreaDto {
  @IsString()
  @MinLength(2)
  name!: string;
}
