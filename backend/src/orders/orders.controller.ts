import { Body, Controller, Get, Param, Patch, Post, UseGuards } from '@nestjs/common';
import { UserRole } from '@prisma/client';
import { CurrentUser } from '../auth/decorators/current-user.decorator';
import { JwtAuthGuard } from '../auth/guards/jwt-auth.guard';
import type { AuthUser } from '../auth/types/auth-user.type';
import { Roles } from '../roles/decorators/roles.decorator';
import { RolesGuard } from '../roles/guards/roles.guard';
import {
  CreateOrderDto,
  CreateOrderWithPaymentDto,
} from './dto/create-order.dto';
import {
  OrderReturnRejectDto,
  OrderReturnRequestDto,
} from './dto/order-return.dto';
import { OrdersService } from './orders.service';

@Controller('orders')
export class OrdersController {
  constructor(private readonly ordersService: OrdersService) {}

  @Get()
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(UserRole.ADMIN, UserRole.EMPLOYEE, UserRole.DEALER, UserRole.SHOPKEEPER)
  findAll(@CurrentUser() user: AuthUser) {
    return this.ordersService.findAll(user);
  }

  @Get('my')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(UserRole.SHOPKEEPER)
  findMine(@CurrentUser() user: AuthUser) {
    return this.ordersService.findMine(user);
  }

  @Get('my/summary')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(UserRole.SHOPKEEPER)
  mySummary(@CurrentUser() user: AuthUser) {
    return this.ordersService.mySummary(user);
  }

  @Get('dealer')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(UserRole.ADMIN, UserRole.EMPLOYEE, UserRole.DEALER)
  findDealer(@CurrentUser() user: AuthUser) {
    return this.ordersService.findDealer(user);
  }

  @Get('dealer/summary')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(UserRole.ADMIN, UserRole.EMPLOYEE, UserRole.DEALER)
  dealerSummary(@CurrentUser() user: AuthUser) {
    return this.ordersService.dealerSummary(user);
  }

  @Get(':id/reorder-preview')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(UserRole.SHOPKEEPER)
  previewReorder(@Param('id') id: string, @CurrentUser() user: AuthUser) {
    return this.ordersService.previewReorder(id, user);
  }

  @Get(':id')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(UserRole.ADMIN, UserRole.EMPLOYEE, UserRole.DEALER, UserRole.SHOPKEEPER)
  findOne(@Param('id') id: string, @CurrentUser() user: AuthUser) {
    return this.ordersService.findOne(id, user);
  }

  @Post()
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(UserRole.SHOPKEEPER)
  create(@Body() dto: CreateOrderDto, @CurrentUser() user: AuthUser) {
    return this.ordersService.createOrder(dto, user);
  }

  @Post('create')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(UserRole.SHOPKEEPER)
  createWithPayment(
    @Body() dto: CreateOrderWithPaymentDto,
    @CurrentUser() user: AuthUser,
  ) {
    return this.ordersService.createOrderWithPayment(dto, user);
  }

  @Post('dealer-restock')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(UserRole.DEALER)
  createDealerRestock(@Body() dto: CreateOrderDto, @CurrentUser() user: AuthUser) {
    return this.ordersService.createDealerRestockOrder(dto, user);
  }

  @Post('dealer-restock/create')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(UserRole.DEALER)
  createDealerRestockWithPayment(
    @Body() dto: CreateOrderWithPaymentDto,
    @CurrentUser() user: AuthUser,
  ) {
    return this.ordersService.createDealerRestockWithPayment(dto, user);
  }

  @Patch(':id/confirm')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(UserRole.ADMIN, UserRole.EMPLOYEE, UserRole.DEALER)
  confirm(@Param('id') id: string, @CurrentUser() user: AuthUser) {
    return this.ordersService.confirmOrder(id, user);
  }

  @Patch(':id/out-for-delivery')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(UserRole.ADMIN, UserRole.EMPLOYEE, UserRole.DEALER)
  outForDelivery(@Param('id') id: string, @CurrentUser() user: AuthUser) {
    return this.ordersService.markOutForDelivery(id, user);
  }

  @Patch(':id/dispatch')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(UserRole.ADMIN, UserRole.EMPLOYEE, UserRole.DEALER)
  dispatch(@Param('id') id: string, @CurrentUser() user: AuthUser) {
    return this.ordersService.markOutForDelivery(id, user);
  }

  @Patch(':id/deliver')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(UserRole.ADMIN, UserRole.DEALER)
  deliver(@Param('id') id: string, @CurrentUser() user: AuthUser) {
    return this.ordersService.markDelivered(id, user);
  }

  @Patch(':id/cancel')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(UserRole.ADMIN, UserRole.EMPLOYEE, UserRole.DEALER, UserRole.SHOPKEEPER)
  cancel(@Param('id') id: string, @CurrentUser() user: AuthUser) {
    return this.ordersService.cancelOrder(id, user);
  }

  @Patch(':id/return-request')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(UserRole.SHOPKEEPER)
  requestReturn(
    @Param('id') id: string,
    @Body() dto: OrderReturnRequestDto,
    @CurrentUser() user: AuthUser,
  ) {
    return this.ordersService.requestReturn(id, dto, user);
  }

  @Patch(':id/return/approve')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(UserRole.ADMIN, UserRole.EMPLOYEE, UserRole.DEALER)
  approveReturn(@Param('id') id: string, @CurrentUser() user: AuthUser) {
    return this.ordersService.approveReturn(id, user);
  }

  @Patch(':id/return/reject')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(UserRole.ADMIN, UserRole.EMPLOYEE, UserRole.DEALER)
  rejectReturn(
    @Param('id') id: string,
    @Body() dto: OrderReturnRejectDto,
    @CurrentUser() user: AuthUser,
  ) {
    return this.ordersService.rejectReturn(id, dto, user);
  }

  @Post(':id/payment/mock')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(UserRole.SHOPKEEPER, UserRole.ADMIN, UserRole.EMPLOYEE, UserRole.DEALER)
  mockPayment(@Param('id') id: string) {
    return this.ordersService.mockPaymentSuccess(id);
  }
}
