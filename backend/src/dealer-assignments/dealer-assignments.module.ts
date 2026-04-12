import { Module } from '@nestjs/common';
import { AreasModule } from '../areas/areas.module';
import { RolesModule } from '../roles/roles.module';
import { DealerAssignmentsController } from './dealer-assignments.controller';
import { DealerAssignmentsService } from './dealer-assignments.service';

/**
 * Dealer ↔ area assignment is modeled on `Area.dealerId`.
 * This module owns mutations and validation for that relationship.
 */
@Module({
  imports: [AreasModule, RolesModule],
  controllers: [DealerAssignmentsController],
  providers: [DealerAssignmentsService],
  exports: [DealerAssignmentsService],
})
export class DealerAssignmentsModule {}
