import { Prisma } from '@prisma/client';
import { AuthUser } from '../auth/types/auth-user.type';
import { PrismaService } from '../prisma/prisma.service';
import { CreateAreaDto } from './dto/create-area.dto';
import { UpdateAreaDto } from './dto/update-area.dto';
export declare class AreasService {
    private readonly prisma;
    constructor(prisma: PrismaService);
    findAll(actor: AuthUser): Prisma.PrismaPromise<({
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
    create(actor: AuthUser, dto: CreateAreaDto): Promise<{
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
    update(actor: AuthUser, areaId: string, dto: UpdateAreaDto): Promise<{
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
    private defaultCompanyId;
    private ensureDefaultCompanyId;
    private ensureRegistrationAreas;
    findForRegistration(state?: string, district?: string): Promise<{
        name: string;
        id: string;
        dealerId: string | null;
        state: string | null;
        district: string | null;
    }[]>;
    listRegistrationGeo(): Promise<{
        states: {
            name: string;
            districts: string[];
        }[];
    }>;
}
