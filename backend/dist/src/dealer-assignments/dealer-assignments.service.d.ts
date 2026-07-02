import { AuthUser } from '../auth/types/auth-user.type';
import { AreasService } from '../areas/areas.service';
export declare class DealerAssignmentsService {
    private readonly areasService;
    constructor(areasService: AreasService);
    listAssignments(actor: AuthUser): import("@prisma/client").Prisma.PrismaPromise<({
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
