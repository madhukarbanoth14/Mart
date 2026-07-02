import { Module } from '@nestjs/common';
import { FinanceModule } from '../finance/finance.module';
import { NotificationsModule } from '../notifications/notifications.module';
import { PaymentsModule } from '../payments/payments.module';
import { RolesModule } from '../roles/roles.module';
import { DealerFinanceController } from './dealer-finance.controller';
import { RefundsController, ReturnsController } from './returns.controller';
import { ReturnsService } from './returns.service';

@Module({
  imports: [RolesModule, NotificationsModule, PaymentsModule, FinanceModule],
  controllers: [ReturnsController, RefundsController, DealerFinanceController],
  providers: [ReturnsService],
  exports: [ReturnsService],
})
export class ReturnsModule {}
