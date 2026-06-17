import {
  Body,
  Controller,
  Headers,
  HttpCode,
  Post,
  UseGuards,
} from '@nestjs/common';
import { UserRole } from '@prisma/client';
import { CurrentUser } from '../auth/decorators/current-user.decorator';
import { JwtAuthGuard } from '../auth/guards/jwt-auth.guard';
import type { AuthUser } from '../auth/types/auth-user.type';
import { Roles } from '../roles/decorators/roles.decorator';
import { RolesGuard } from '../roles/guards/roles.guard';
import { CreateRazorpayOrderDto } from './dto/create-razorpay-order.dto';
import { VerifyRazorpayPaymentDto } from './dto/verify-razorpay-payment.dto';
import { PaymentsService } from './payments.service';

@Controller('payments')
export class PaymentsController {
  constructor(private readonly paymentsService: PaymentsService) {}

  @Post('razorpay/order')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(UserRole.SHOPKEEPER, UserRole.ADMIN, UserRole.EMPLOYEE, UserRole.DEALER)
  createRazorpayOrder(
    @Body() dto: CreateRazorpayOrderDto,
    @CurrentUser() user: AuthUser,
  ) {
    return this.paymentsService.createRazorpayOrder(dto, user);
  }

  /** Alias for mobile clients */
  @Post('create-order')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(UserRole.SHOPKEEPER, UserRole.ADMIN, UserRole.EMPLOYEE, UserRole.DEALER)
  createOrderAlias(
    @Body() dto: CreateRazorpayOrderDto,
    @CurrentUser() user: AuthUser,
  ) {
    return this.paymentsService.createRazorpayOrder(dto, user);
  }

  @Post('create')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(UserRole.SHOPKEEPER, UserRole.ADMIN, UserRole.EMPLOYEE, UserRole.DEALER)
  createAliasV2(
    @Body() dto: CreateRazorpayOrderDto,
    @CurrentUser() user: AuthUser,
  ) {
    return this.paymentsService.createRazorpayOrder(dto, user);
  }

  @Post('razorpay/verify')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(UserRole.SHOPKEEPER, UserRole.ADMIN, UserRole.EMPLOYEE, UserRole.DEALER)
  verifyRazorpayPayment(
    @Body() dto: VerifyRazorpayPaymentDto,
    @CurrentUser() user: AuthUser,
  ) {
    return this.paymentsService.verifyRazorpayPayment(dto, user);
  }

  @Post('verify')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(UserRole.SHOPKEEPER, UserRole.ADMIN, UserRole.EMPLOYEE, UserRole.DEALER)
  verifyAlias(
    @Body() dto: VerifyRazorpayPaymentDto,
    @CurrentUser() user: AuthUser,
  ) {
    return this.paymentsService.verifyRazorpayPayment(dto, user);
  }

  @Post('razorpay/webhook')
  @HttpCode(200)
  handleRazorpayWebhook(
    @Headers('x-razorpay-signature') signature: string | undefined,
    @Body() payload: unknown,
  ) {
    return this.paymentsService.handleRazorpayWebhook(signature, payload);
  }
}
