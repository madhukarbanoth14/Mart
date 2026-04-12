import { Controller, Get, UseGuards } from '@nestjs/common';
import { UserRole } from '@prisma/client';
import { CurrentUser } from '../auth/decorators/current-user.decorator';
import { JwtAuthGuard } from '../auth/guards/jwt-auth.guard';
import { AuthUser } from '../auth/types/auth-user.type';
import { Roles } from '../roles/decorators/roles.decorator';
import { RolesGuard } from '../roles/guards/roles.guard';
import { DealerAssignmentsService } from './dealer-assignments.service';

@Controller('dealer-assignments')
export class DealerAssignmentsController {
  constructor(
    private readonly dealerAssignmentsService: DealerAssignmentsService,
  ) {}

  /** Read model: one row per area with its assigned dealer */
  @Get()
  @UseGuards(JwtAuthGuard, RolesGuard)
  @Roles(UserRole.ADMIN, UserRole.EMPLOYEE, UserRole.DEALER)
  listByArea(@CurrentUser() user: AuthUser) {
    return this.dealerAssignmentsService.listAssignments(user);
  }
}
