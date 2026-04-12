import { Injectable } from '@nestjs/common';
import { AuthUser } from '../auth/types/auth-user.type';
import { AreasService } from '../areas/areas.service';

@Injectable()
export class DealerAssignmentsService {
  constructor(private readonly areasService: AreasService) {}

  listAssignments(actor: AuthUser) {
    return this.areasService.findAll(actor);
  }
}
