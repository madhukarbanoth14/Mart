import type { AuthUser } from '../auth/types/auth-user.type';
import { BrandsService } from './brands.service';
import { CreateBrandDto } from './dto/create-brand.dto';
import { UpdateBrandDto } from './dto/update-brand.dto';
export declare class BrandsController {
    private readonly brandsService;
    constructor(brandsService: BrandsService);
    findAll(): import("@prisma/client").Prisma.PrismaPromise<{
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
    create(user: AuthUser, dto: CreateBrandDto): Promise<{
        name: string;
        id: string;
        companyId: string;
        createdAt: Date;
        updatedAt: Date;
        category: string | null;
        manufacturer: string | null;
        logoUrl: string | null;
    }>;
    update(id: string, user: AuthUser, dto: UpdateBrandDto): Promise<{
        name: string;
        id: string;
        companyId: string;
        createdAt: Date;
        updatedAt: Date;
        category: string | null;
        manufacturer: string | null;
        logoUrl: string | null;
    }>;
    remove(id: string, user: AuthUser): Promise<{
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
