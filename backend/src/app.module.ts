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

@Module({
  imports: [
    ConfigModule.forRoot({
      isGlobal: true,
      load: [configuration],
      validate: validateEnv,
    }),
    PrismaModule,
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
  ],
})
export class AppModule {}
