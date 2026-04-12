import { Module } from '@nestjs/common';
import { RolesModule } from '../roles/roles.module';
import { StockController } from './stock.controller';
import { StockService } from './stock.service';

@Module({
  imports: [RolesModule],
  controllers: [StockController],
  providers: [StockService],
  exports: [StockService],
})
export class StockModule {}
