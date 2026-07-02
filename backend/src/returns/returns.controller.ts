import {
  Body,
  Controller,
  Get,
  Header,
  Param,
  Patch,
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
import {
  CreateReturnRequestDto,
  ProcessRefundDto,
  RaiseRefundRequestDto,
  RefundRejectDto,
  ReturnActionDto,
  ReturnsQueryDto,
  RefundsQueryDto,
} from './dto/returns.dto';
import { ReturnsService } from './returns.service';

@Controller('returns')
@UseGuards(JwtAuthGuard, RolesGuard)
export class ReturnsController {
  constructor(private readonly returns: ReturnsService) {}

  @Post('orders/:orderId')
  @Roles(UserRole.SHOPKEEPER)
  create(
    @Param('orderId') orderId: string,
    @Body() dto: CreateReturnRequestDto,
    @CurrentUser() user: AuthUser,
  ) {
    return this.returns.createReturn(orderId, dto, user);
  }

  @Get()
  @Roles(UserRole.ADMIN, UserRole.EMPLOYEE, UserRole.DEALER, UserRole.SHOPKEEPER)
  list(@CurrentUser() user: AuthUser, @Query() q: ReturnsQueryDto) {
    return this.returns.listReturns(user, q);
  }

  @Get(':id')
  @Roles(UserRole.ADMIN, UserRole.EMPLOYEE, UserRole.DEALER, UserRole.SHOPKEEPER)
  getOne(@Param('id') id: string, @CurrentUser() user: AuthUser) {
    return this.returns.getReturn(id, user);
  }

  @Patch(':id/approve')
  @Roles(UserRole.ADMIN, UserRole.EMPLOYEE, UserRole.DEALER)
  approve(
    @Param('id') id: string,
    @Body() dto: ReturnActionDto,
    @CurrentUser() user: AuthUser,
  ) {
    return this.returns.approveReturn(id, dto, user);
  }

  @Patch(':id/reject')
  @Roles(UserRole.ADMIN, UserRole.EMPLOYEE, UserRole.DEALER)
  reject(
    @Param('id') id: string,
    @Body() dto: ReturnActionDto,
    @CurrentUser() user: AuthUser,
  ) {
    return this.returns.rejectReturn(id, dto, user);
  }

  @Post(':id/refund-request')
  @Roles(UserRole.DEALER)
  raiseRefund(
    @Param('id') id: string,
    @Body() dto: RaiseRefundRequestDto,
    @CurrentUser() user: AuthUser,
  ) {
    return this.returns.raiseRefundRequest(id, dto, user);
  }
}

@Controller('refunds')
@UseGuards(JwtAuthGuard, RolesGuard)
export class RefundsController {
  constructor(private readonly returns: ReturnsService) {}

  @Get()
  @Roles(UserRole.ADMIN, UserRole.EMPLOYEE, UserRole.DEALER, UserRole.SHOPKEEPER)
  list(@CurrentUser() user: AuthUser, @Query() q: RefundsQueryDto) {
    return this.returns.listRefunds(user, q);
  }

  @Get(':id')
  @Roles(UserRole.ADMIN, UserRole.EMPLOYEE, UserRole.DEALER, UserRole.SHOPKEEPER)
  getOne(@Param('id') id: string, @CurrentUser() user: AuthUser) {
    return this.returns.getRefund(id, user);
  }

  @Patch(':id/approve')
  @Roles(UserRole.ADMIN, UserRole.EMPLOYEE)
  approve(
    @Param('id') id: string,
    @Body() dto: RefundRejectDto,
    @CurrentUser() user: AuthUser,
  ) {
    return this.returns.approveRefund(id, dto, user);
  }

  @Patch(':id/reject')
  @Roles(UserRole.ADMIN, UserRole.EMPLOYEE)
  reject(
    @Param('id') id: string,
    @Body() dto: RefundRejectDto,
    @CurrentUser() user: AuthUser,
  ) {
    return this.returns.rejectRefund(id, dto, user);
  }

  @Post(':id/process')
  @Roles(UserRole.ADMIN, UserRole.EMPLOYEE)
  process(
    @Param('id') id: string,
    @Body() dto: ProcessRefundDto,
    @CurrentUser() user: AuthUser,
  ) {
    return this.returns.processRefund(id, dto, user);
  }
}
