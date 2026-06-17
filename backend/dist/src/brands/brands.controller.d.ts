import type { AuthUser } from '../auth/types/auth-user.type';
import { BrandsService } from './brands.service';
import { CreateBrandDto } from './dto/create-brand.dto';
import { UpdateBrandDto } from './dto/update-brand.dto';
export declare class BrandsController {
    private readonly brandsService;
    constructor(brandsService: BrandsService);
    findAll(): import("@prisma/client").Prisma.PrismaPromise<{
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
    create(user: AuthUser, dto: CreateBrandDto): Promise<{
        id: string;
        companyId: string;
        name: string;
        createdAt: Date;
        updatedAt: Date;
        manufacturer: string | null;
        category: string | null;
        logoUrl: string | null;
    }>;
    update(id: string, user: AuthUser, dto: UpdateBrandDto): Promise<{
        id: string;
        companyId: string;
        name: string;
        createdAt: Date;
        updatedAt: Date;
        manufacturer: string | null;
        category: string | null;
        logoUrl: string | null;
    }>;
    remove(id: string, user: AuthUser): Promise<{
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
