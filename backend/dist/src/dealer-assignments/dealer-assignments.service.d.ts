import { AuthUser } from '../auth/types/auth-user.type';
import { AreasService } from '../areas/areas.service';
export declare class DealerAssignmentsService {
    private readonly areasService;
    constructor(areasService: AreasService);
    listAssignments(actor: AuthUser): import("@prisma/client").Prisma.PrismaPromise<({
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
