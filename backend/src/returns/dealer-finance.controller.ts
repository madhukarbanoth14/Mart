import {
  Controller,
  Get,
  Header,
  Param,
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
import { FinanceService } from '../finance/finance.service';
import { DealerFinanceQueryDto } from '../returns/dto/returns.dto';

@Controller('finance/dealer')
@UseGuards(JwtAuthGuard, RolesGuard)
@Roles(UserRole.DEALER)
export class DealerFinanceController {
  constructor(private readonly finance: FinanceService) {}

  @Get('dashboard')
  dashboard(@CurrentUser() user: AuthUser, @Query() q: DealerFinanceQueryDto) {
    return this.finance.dealerRevenueDashboard(
      user.userId,
      user.companyId,
      q.period ?? 'month',
      q.startDate,
      q.endDate,
    );
  }

  @Get('shopkeepers')
  shopkeepers(@CurrentUser() user: AuthUser, @Query() q: DealerFinanceQueryDto) {
    return this.finance.dealerShopkeeperRevenue(user.userId, user.companyId, q);
  }

  @Get('reports/:type')
  @Header('Access-Control-Expose-Headers', 'Content-Disposition')
  async exportReport(
    @CurrentUser() user: AuthUser,
    @Param('type') type: string,
    @Query('format') format: 'csv' | 'xlsx' = 'csv',
    @Query() q: DealerFinanceQueryDto,
    @Res() res: Response,
  ) {
    const file = await this.finance.exportDealerReport(
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
