import type { AuthUser } from '../auth/types/auth-user.type';
import { AreasService } from './areas.service';
export declare class AreasController {
    private readonly areasService;
    constructor(areasService: AreasService);
    findAll(user: AuthUser): import("@prisma/client").Prisma.PrismaPromise<({
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
