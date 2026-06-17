import { Prisma } from '@prisma/client';
import { AuthUser } from '../auth/types/auth-user.type';
import { PrismaService } from '../prisma/prisma.service';
import { CreateBrandDto } from './dto/create-brand.dto';
import { UpdateBrandDto } from './dto/update-brand.dto';
export declare class BrandsService {
    private readonly prisma;
    constructor(prisma: PrismaService);
    findAll(): Prisma.PrismaPromise<{
        id: string;
        name: string;
        manufacturer: string | null;
        logoUrl: string | null;
    }[]>;
    findOne(id: string): Promise<{
        id: string;
        companyId: string;
        name: string;
        manufacturer: string | null;
        category: string | null;
        logoUrl: string | null;
    }>;
    create(actor: AuthUser, dto: CreateBrandDto): Promise<{
        id: string;
        companyId: string;
        name: string;
        createdAt: Date;
        updatedAt: Date;
        manufacturer: string | null;
        category: string | null;
        logoUrl: string | null;
    }>;
    update(id: string, actor: AuthUser, dto: UpdateBrandDto): Promise<{
        id: string;
        companyId: string;
        name: string;
        createdAt: Date;
        updatedAt: Date;
        manufacturer: string | null;
        category: string | null;
        logoUrl: string | null;
    }>;
    remove(id: string, actor: AuthUser): Promise<{
        id: string;
        companyId: string;
        name: string;
        createdAt: Date;
        updatedAt: Date;
        manufacturer: string | null;
        category: string | null;
        logoUrl: string | null;
    }>;
}
