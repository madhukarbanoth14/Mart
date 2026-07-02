import {
  Body,
  Controller,
  Get,
  Header,
  Param,
  Post,
  Query,
  Res,
  UseGuards,
} from '@nestjs/common';
import type { Response } from 'express';
import { UserRole } from '@prisma/client';
import { JwtAuthGuard } from '../auth/guards/jwt-auth.guard';
import { RolesGuard } from '../roles/guards/roles.guard';
import { Roles } from '../roles/decorators/roles.decorator';
import { CurrentUser } from '../auth/decorators/current-user.decorator';
import type { AuthUser } from '../auth/types/auth-user.type';
import { CommissionService } from './commission.service';
import { FinanceAuditService } from './finance-audit.service';
import { FinanceService } from './finance.service';
import {
  FinanceQueryDto,
  GenerateSettlementDto,
  RecordSettlementPaymentDto,
  UpsertCommissionRuleDto,
} from './dto/finance.dto';

@Controller('finance')
@UseGuards(JwtAuthGuard, RolesGuard)
@Roles(UserRole.ADMIN)
export class FinanceController {
  constructor(
    private readonly finance: FinanceService,
    private readonly commission: CommissionService,
    private readonly audit: FinanceAuditService,
  ) {}

  @Get('dashboard/overview')
  overview(@CurrentUser() user: AuthUser, @Query() q: FinanceQueryDto) {
    return this.finance.dashboardOverview(
      user.companyId,
      q.period ?? 'month',
      q.startDate,
      q.endDate,
    );
  }

  @Get('dashboard/investor')
  investor(@CurrentUser() user: AuthUser) {
    return this.finance.investorDashboard(user.companyId);
  }

  @Get('dashboard/collections')
  collections(@CurrentUser() user: AuthUser, @Query() q: FinanceQueryDto) {
    return this.finance.dashboardOverview(
      user.companyId,
      q.period ?? 'today',
      q.startDate,
      q.endDate,
    ).then((r) => r.collections);
  }

  @Get('commission-rules')
  commissionRules(@CurrentUser() user: AuthUser) {
    return this.commission.listRules(user.companyId);
  }

  @Post('commission-rules')
  upsertCommissionRule(@CurrentUser() user: AuthUser, @Body() dto: UpsertCommissionRuleDto) {
    return this.commission.upsertRule(user.companyId, user.userId, dto);
  }

  @Get('settlements')
  listSettlements(@CurrentUser() user: AuthUser, @Query() q: FinanceQueryDto) {
    return this.finance.listSettlements(user.companyId, {
      dealerId: q.dealerId,
      status: q.status,
      period: q.period,
      start: q.startDate,
      end: q.endDate,
    });
  }

  @Post('settlements/generate')
  generateSettlement(@CurrentUser() user: AuthUser, @Body() dto: GenerateSettlementDto) {
    return this.finance.generateSettlement(user, dto);
  }

  @Get('settlements/:id')
  getSettlement(@Param('id') id: string) {
    return this.finance.getSettlement(id);
  }

  @Post('settlements/:id/payments')
  recordPayment(
    @CurrentUser() user: AuthUser,
    @Param('id') id: string,
    @Body() dto: RecordSettlementPaymentDto,
  ) {
    return this.finance.recordSettlementPayment(user, id, dto);
  }

  @Get('dealers/:dealerId/performance')
  dealerPerformance(
    @Param('dealerId') dealerId: string,
    @Query() q: FinanceQueryDto,
  ) {
    return this.finance.dealerPerformance(
      dealerId,
      q.period ?? 'month',
      q.startDate,
      q.endDate,
    );
  }

  @Get('audit')
  auditLog(@CurrentUser() user: AuthUser) {
    return this.audit.list(user.companyId);
  }

  @Post('backfill-revenues')
  backfill(@CurrentUser() user: AuthUser) {
    return this.finance.backfillRevenues(user.companyId);
  }

  @Get('reports/:type')
  @Header('Access-Control-Expose-Headers', 'Content-Disposition')
  async exportReport(
    @CurrentUser() user: AuthUser,
    @Param('type') type: string,
    @Query('format') format: 'csv' | 'xlsx' = 'csv',
    @Query() q: FinanceQueryDto,
    @Res() res: Response,
  ) {
    const file = await this.finance.exportReport(
      user,
      type,
      format,
      q.period ?? 'month',
      q.startDate,
      q.endDate,
    );
    res.setHeader('Content-Type', file.contentType);
    res.setHeader('Content-Disposition', `attachment; filename="${file.filename}"`);
    res.send(file.body);
  }
}
