import { Body, Controller, Get, Param, Patch, Post, UseGuards } from '@nestjs/common';
import { UserRole } from '@prisma/client';
import { CurrentUser } from '../auth/decorators/current-user.decorator';
import { JwtAuthGuard } from '../auth/guards/jwt-auth.guard';
import type { AuthUser } from '../auth/types/auth-user.type';
import { Roles } from '../roles/decorators/roles.decorator';
import { RolesGuard } from '../roles/guards/roles.guard';
import { UpsertStockDto } from './dto/upsert-stock.dto';
import { UpdateStockDto } from './dto/update-stock.dto';
import { StockService } from './stock.service';

@Controller('stock')
export class StockController {
  constructor(private readonly stockService: StockService) {}

  @Get()
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(UserRole.ADMIN, UserRole.EMPLOYEE, UserRole.DEALER)
  findAll(@CurrentUser() user: AuthUser) {
    return this.stockService.findAll(user);
  }

  @Get('available')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(UserRole.SHOPKEEPER)
  available(@CurrentUser() user: AuthUser) {
    return this.stockService.availableForShopkeeper(user);
  }

  @Patch(':id')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(UserRole.ADMIN, UserRole.EMPLOYEE, UserRole.DEALER)
  updateQuantity(
    @Param('id') id: string,
    @Body() dto: UpdateStockDto,
    @CurrentUser() user: AuthUser,
  ) {
    return this.stockService.updateQuantity(id, user, dto.quantity);
  }

  @Post('upsert')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(UserRole.DEALER)
  upsert(@Body() dto: UpsertStockDto, @CurrentUser() user: AuthUser) {
    return this.stockService.upsertForDealer(user, dto);
  }
}
