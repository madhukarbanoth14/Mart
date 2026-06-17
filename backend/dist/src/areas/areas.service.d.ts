import { Prisma } from '@prisma/client';
import { AuthUser } from '../auth/types/auth-user.type';
import { PrismaService } from '../prisma/prisma.service';
export declare class AreasService {
    private readonly prisma;
    constructor(prisma: PrismaService);
    findAll(actor: AuthUser): Prisma.PrismaPromise<({
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
