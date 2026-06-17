import {
  Body,
  Controller,
  Get,
  Param,
  Patch,
  Post,
  Query,
  Res,
  UploadedFile,
  UseGuards,
  UseInterceptors,
} from '@nestjs/common';
import { FileInterceptor } from '@nestjs/platform-express';
import { UserRole } from '@prisma/client';
import type { Response } from 'express';
import { memoryStorage } from 'multer';
import { CurrentUser } from '../auth/decorators/current-user.decorator';
import { JwtAuthGuard } from '../auth/guards/jwt-auth.guard';
import type { AuthUser } from '../auth/types/auth-user.type';
import { Roles } from '../roles/decorators/roles.decorator';
import { RolesGuard } from '../roles/guards/roles.guard';
import { CreateDealerDto } from './dto/create-dealer.dto';
import { CreateEmployeeDto } from './dto/create-employee.dto';
import { CreateShopkeeperDto } from './dto/create-shopkeeper.dto';
import { ListUsersQueryDto } from './dto/list-users-query.dto';
import { UpdateUserStatusDto } from './dto/update-user-status.dto';
import { UsersService } from './users.service';

@Controller('users')
export class UsersController {
  constructor(private readonly usersService: UsersService) {}

  @Get()
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(UserRole.ADMIN, UserRole.EMPLOYEE)
  findAll(
    @CurrentUser() user: AuthUser,
    @Query() query: ListUsersQueryDto,
  ) {
    return this.usersService.findAll(user, query);
  }

  @Get('pending-count')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(UserRole.ADMIN)
  async pendingCount(@CurrentUser() user: AuthUser) {
    const count = await this.usersService.countPendingApprovals(user);
    return { count };
  }

  @Post('employees')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(UserRole.ADMIN)
  createEmployee(
    @CurrentUser() user: AuthUser,
    @Body() dto: CreateEmployeeDto,
  ) {
    return this.usersService.createEmployee(user, dto);
  }

  @Post('shopkeepers')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(UserRole.ADMIN, UserRole.EMPLOYEE)
  createShopkeeper(
    @CurrentUser() user: AuthUser,
    @Body() dto: CreateShopkeeperDto,
  ) {
    return this.usersService.createShopkeeper(user, dto);
  }

  @Post('dealers')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(UserRole.ADMIN, UserRole.EMPLOYEE)
  createDealer(
    @CurrentUser() user: AuthUser,
    @Body() dto: CreateDealerDto,
  ) {
    return this.usersService.createDealer(user, dto);
  }

  @Post(':id/onboarding-documents')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(UserRole.ADMIN, UserRole.EMPLOYEE)
  @UseInterceptors(
    FileInterceptor('file', {
      storage: memoryStorage(),
      limits: { fileSize: 10 * 1024 * 1024 },
    }),
  )
  uploadOnboardingDocument(
    @CurrentUser() user: AuthUser,
    @Param('id') id: string,
    @Body('label') label: string,
    @UploadedFile() file: Express.Multer.File,
  ) {
    return this.usersService.uploadOnboardingDocument(user, id, label, file);
  }

  @Get(':userId/onboarding-documents/:documentId/file')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(UserRole.ADMIN, UserRole.EMPLOYEE)
  async downloadOnboardingDocument(
    @CurrentUser() user: AuthUser,
    @Param('userId') userId: string,
    @Param('documentId') documentId: string,
    @Res() res: Response,
  ) {
    await this.usersService.streamOnboardingDocument(
      user,
      userId,
      documentId,
      res,
    );
  }

  @Patch(':id/approve')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(UserRole.ADMIN)
  approve(
    @CurrentUser() user: AuthUser,
    @Param('id') id: string,
  ) {
    return this.usersService.approveUser(user, id);
  }

  @Patch(':id/reject')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(UserRole.ADMIN)
  reject(
    @CurrentUser() user: AuthUser,
    @Param('id') id: string,
    @Body() dto: UpdateUserStatusDto,
  ) {
    return this.usersService.rejectUser(user, id, dto.reason);
  }

  @Patch(':id/deactivate')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(UserRole.ADMIN)
  deactivate(
    @CurrentUser() user: AuthUser,
    @Param('id') id: string,
    @Body() dto: UpdateUserStatusDto,
  ) {
    return this.usersService.deactivateUser(user, id, dto.reason);
  }

  @Patch(':id/reactivate')
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(UserRole.ADMIN)
  reactivate(
    @CurrentUser() user: AuthUser,
    @Param('id') id: string,
  ) {
    return this.usersService.reactivateUser(user, id);
  }
}
