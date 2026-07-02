import { Module } from '@nestjs/common';
import { InvoicesModule } from '../invoices/invoices.module';
import { NotificationsModule } from '../notifications/notifications.module';
import { OrderItemsModule } from '../order-items/order-items.module';
import { PaymentsModule } from '../payments/payments.module';
import { RolesModule } from '../roles/roles.module';
import { UsersModule } from '../users/users.module';
import { FinanceModule } from '../finance/finance.module';
import { ReturnsModule } from '../returns/returns.module';
import { OrdersController } from './orders.controller';
import { OrdersService } from './orders.service';

@Module({
  imports: [
    OrderItemsModule,
    InvoicesModule,
    NotificationsModule,
    PaymentsModule,
    RolesModule,
    UsersModule,
    FinanceModule,
    ReturnsModule,
  ],
  controllers: [OrdersController],
  providers: [OrdersService],
  exports: [OrdersService],
})
export class OrdersModule {}
