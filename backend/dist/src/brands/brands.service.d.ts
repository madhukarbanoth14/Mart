import { Prisma } from '@prisma/client';
import { AuthUser } from '../auth/types/auth-user.type';
import { PrismaService } from '../prisma/prisma.service';
import { CreateBrandDto } from './dto/create-brand.dto';
import { UpdateBrandDto } from './dto/update-brand.dto';
export declare class BrandsService {
    private readonly prisma;
    constructor(prisma: PrismaService);
    findAll(): Prisma.PrismaPromise<{
        name: string;
        id: string;
        manufacturer: string | null;
        logoUrl: string | null;
    }[]>;
    findOne(id: string): Promise<{
        name: string;
        id: string;
        companyId: string;
        category: string | null;
        manufacturer: string | null;
        logoUrl: string | null;
    }>;
    create(actor: AuthUser, dto: CreateBrandDto): Promise<{
        name: string;
        id: string;
        companyId: string;
        createdAt: Date;
        updatedAt: Date;
        category: string | null;
        manufacturer: string | null;
        logoUrl: string | null;
    }>;
    update(id: string, actor: AuthUser, dto: UpdateBrandDto): Promise<{
        name: string;
        id: string;
        companyId: string;
        createdAt: Date;
        updatedAt: Date;
        category: string | null;
        manufacturer: string | null;
        logoUrl: string | null;
    }>;
    remove(id: string, actor: AuthUser): Promise<{
        name: string;
        id: string;
        companyId: string;
        createdAt: Date;
        updatedAt: Date;
        category: string | null;
        manufacturer: string | null;
        logoUrl: string | null;
    }>;
}
