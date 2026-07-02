import { Controller, Get } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';

@Controller('config')
export class AppConfigController {
  constructor(private readonly config: ConfigService) {}

  @Get('ordering')
  ordering() {
    return {
      minOrderQuantity: 1,
      maxOrderQuantity: this.config.get<number>('ordering.maxOrderQuantity', 10000),
      quickQuantityChips: [10, 25, 50, 100, 250, 500, 1000],
    };
  }
}
