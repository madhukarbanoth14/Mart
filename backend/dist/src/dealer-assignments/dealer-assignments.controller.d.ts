import type { AuthUser } from '../auth/types/auth-user.type';
import { DealerAssignmentsService } from './dealer-assignments.service';
export declare class DealerAssignmentsController {
    private readonly dealerAssignmentsService;
    constructor(dealerAssignmentsService: DealerAssignmentsService);
    listByArea(user: AuthUser): import("@prisma/client").Prisma.PrismaPromise<({
        dealer: {
            id: string;
            name: string;
            email: string;
        } | null;
    } & {
        id: string;
        companyId: string | null;
        name: string;
        dealerId: string | null;
    })[]>;
}
