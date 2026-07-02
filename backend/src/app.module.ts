import { Module } from '@nestjs/common';
import { ConfigModule } from '@nestjs/config';
import configuration from './config/configuration';
import { validateEnv } from './config/env.validation';
import { PrismaModule } from './prisma/prisma.module';
import { HealthModule } from './health/health.module';
import { AuthModule } from './auth/auth.module';
import { UsersModule } from './users/users.module';
import { RolesModule } from './roles/roles.module';
import { ProductsModule } from './products/products.module';
import { AreasModule } from './areas/areas.module';
import { DealerAssignmentsModule } from './dealer-assignments/dealer-assignments.module';
import { OrdersModule } from './orders/orders.module';
import { StockModule } from './stock/stock.module';
import { InvoicesModule } from './invoices/invoices.module';
import { BrandsModule } from './brands/brands.module';
import { PaymentsModule } from './payments/payments.module';
import { NotificationsModule } from './notifications/notifications.module';
import { EmailModule } from './email/email.module';
import { AppConfigModule } from './config/app-config.module';
import { FinanceModule } from './finance/finance.module';
import { ReturnsModule } from './returns/returns.module';

@Module({
  imports: [
    ConfigModule.forRoot({
      isGlobal: true,
      load: [configuration],
      validate: validateEnv,
    }),
    PrismaModule,
    EmailModule,
    AppConfigModule,
    HealthModule,
    AuthModule,
    UsersModule,
    RolesModule,
    ProductsModule,
    AreasModule,
    DealerAssignmentsModule,
    OrdersModule,
    StockModule,
    InvoicesModule,
    BrandsModule,
    PaymentsModule,
    NotificationsModule,
    FinanceModule,
    ReturnsModule,
  ],
})
export class AppModule {}
