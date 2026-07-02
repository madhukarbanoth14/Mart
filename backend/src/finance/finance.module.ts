import { Module } from '@nestjs/common';
import { RolesModule } from '../roles/roles.module';
import { CommissionService } from './commission.service';
import { FinanceAuditService } from './finance-audit.service';
import { FinanceController } from './finance.controller';
import { FinanceService } from './finance.service';

@Module({
  imports: [RolesModule],
  controllers: [FinanceController],
  providers: [FinanceService, CommissionService, FinanceAuditService],
  exports: [FinanceService, CommissionService, FinanceAuditService],
})
export class FinanceModule {}
