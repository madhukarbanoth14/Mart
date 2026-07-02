import type { AuthUser } from '../auth/types/auth-user.type';
import { DealerAssignmentsService } from './dealer-assignments.service';
export declare class DealerAssignmentsController {
    private readonly dealerAssignmentsService;
    constructor(dealerAssignmentsService: DealerAssignmentsService);
    listByArea(user: AuthUser): import("@prisma/client").Prisma.PrismaPromise<({
        dealer: {
            name: string;
            id: string;
            email: string;
        } | null;
    } & {
        name: string;
        id: string;
        companyId: string | null;
        dealerId: string | null;
        state: string | null;
        district: string | null;
        employeeId: string | null;
    })[]>;
}
