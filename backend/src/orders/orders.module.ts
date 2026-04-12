import { Module } from '@nestjs/common';
import { InvoicesModule } from '../invoices/invoices.module';
import { OrderItemsModule } from '../order-items/order-items.module';
import { RolesModule } from '../roles/roles.module';
import { OrdersController } from './orders.controller';
import { OrdersService } from './orders.service';

@Module({
  imports: [OrderItemsModule, InvoicesModule, RolesModule],
  controllers: [OrdersController],
  providers: [OrdersService],
  exports: [OrdersService],
})
export class OrdersModule {}
