import type { AuthUser } from '../auth/types/auth-user.type';
import { AreasService } from './areas.service';
import { CreateAreaDto } from './dto/create-area.dto';
import { UpdateAreaDto } from './dto/update-area.dto';
export declare class AreasController {
    private readonly areasService;
    constructor(areasService: AreasService);
    findAll(user: AuthUser): import("@prisma/client").Prisma.PrismaPromise<({
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
    create(dto: CreateAreaDto, user: AuthUser): Promise<{
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
    }>;
    update(id: string, dto: UpdateAreaDto, user: AuthUser): Promise<{
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
    }>;
}
