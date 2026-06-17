import type { Response } from 'express';
import type { AuthUser } from '../auth/types/auth-user.type';
import { CreateDealerDto } from './dto/create-dealer.dto';
import { CreateEmployeeDto } from './dto/create-employee.dto';
import { CreateShopkeeperDto } from './dto/create-shopkeeper.dto';
import { ListUsersQueryDto } from './dto/list-users-query.dto';
import { UpdateUserStatusDto } from './dto/update-user-status.dto';
import { UsersService } from './users.service';
export declare class UsersController {
    private readonly usersService;
    constructor(usersService: UsersService);
    findAll(user: AuthUser, query: ListUsersQueryDto): import("@prisma/client").Prisma.PrismaPromise<{
        id: string;
        companyId: string | null;
        name: string;
        createdAt: Date;
        email: string;
        phone: string | null;
        role: import("@prisma/client").$Enums.UserRole;
        onboardedById: string | null;
        onboardingNotes: string | null;
        status: import("@prisma/client").$Enums.UserStatus;
        statusReason: string | null;
        approvedAt: Date | null;
        area: {
            id: string;
            name: string;
        } | null;
        onboardedBy: {
            id: string;
            name: string;
            email: string;
        } | null;
        approvedBy: {
            id: string;
            name: string;
        } | null;
        onboardingDocuments: {
            id: string;
            label: string;
            fileName: string;
            mimeType: string | null;
            fileSize: number | null;
            uploadedAt: Date;
        }[];
    }[]>;
    pendingCount(user: AuthUser): Promise<{
        count: number;
    }>;
    createEmployee(user: AuthUser, dto: CreateEmployeeDto): Promise<{
        loginEmail: string;
        loginPassword: string | undefined;
        message: string;
        emailSent: boolean;
        emailError: string | null;
        id: string;
        companyId: string | null;
        name: string;
        createdAt: Date;
        email: string;
        phone: string | null;
        role: import("@prisma/client").$Enums.UserRole;
        onboardedById: string | null;
        onboardingNotes: string | null;
        status: import("@prisma/client").$Enums.UserStatus;
        statusReason: string | null;
        approvedAt: Date | null;
        area: {
            id: string;
            name: string;
        } | null;
        onboardedBy: {
            id: string;
            name: string;
            email: string;
        } | null;
        approvedBy: {
            id: string;
            name: string;
        } | null;
        onboardingDocuments: {
            id: string;
            label: string;
            fileName: string;
            mimeType: string | null;
            fileSize: number | null;
            uploadedAt: Date;
        }[];
    }>;
    createShopkeeper(user: AuthUser, dto: CreateShopkeeperDto): Promise<{
        loginEmail: string;
        loginPassword: string | undefined;
        message: string;
        emailSent: boolean | undefined;
        emailError: string | null | undefined;
        id: string;
        companyId: string | null;
        name: string;
        createdAt: Date;
        email: string;
        phone: string | null;
        role: import("@prisma/client").$Enums.UserRole;
        onboardedById: string | null;
        onboardingNotes: string | null;
        status: import("@prisma/client").$Enums.UserStatus;
        statusReason: string | null;
        approvedAt: Date | null;
        area: {
            id: string;
            name: string;
        } | null;
        onboardedBy: {
            id: string;
            name: string;
            email: string;
        } | null;
        approvedBy: {
            id: string;
            name: string;
        } | null;
        onboardingDocuments: {
            id: string;
            label: string;
            fileName: string;
            mimeType: string | null;
            fileSize: number | null;
            uploadedAt: Date;
        }[];
    }>;
    createDealer(user: AuthUser, dto: CreateDealerDto): Promise<import("./users.service").CreateDealerResult>;
    uploadOnboardingDocument(user: AuthUser, id: string, label: string, file: Express.Multer.File): Promise<{
        id: string;
        label: string;
        fileName: string;
        mimeType: string | null;
        fileSize: number | null;
        uploadedAt: Date;
    }>;
    downloadOnboardingDocument(user: AuthUser, userId: string, documentId: string, res: Response): Promise<void>;
    approve(user: AuthUser, id: string): Promise<{
        loginEmail: string;
        message: string;
        loginPassword: string | undefined;
        emailSent: boolean;
        emailError: string | null;
        id: string;
        companyId: string | null;
        name: string;
        createdAt: Date;
        email: string;
        phone: string | null;
        role: import("@prisma/client").$Enums.UserRole;
        onboardedById: string | null;
        onboardingNotes: string | null;
        status: import("@prisma/client").$Enums.UserStatus;
        statusReason: string | null;
        approvedAt: Date | null;
        area: {
            id: string;
            name: string;
        } | null;
        onboardedBy: {
            id: string;
            name: string;
            email: string;
        } | null;
        approvedBy: {
            id: string;
            name: string;
        } | null;
        onboardingDocuments: {
            id: string;
            label: string;
            fileName: string;
            mimeType: string | null;
            fileSize: number | null;
            uploadedAt: Date;
        }[];
    }>;
    reject(user: AuthUser, id: string, dto: UpdateUserStatusDto): Promise<{
        id: string;
        companyId: string | null;
        name: string;
        createdAt: Date;
        email: string;
        phone: string | null;
        role: import("@prisma/client").$Enums.UserRole;
        onboardedById: string | null;
        onboardingNotes: string | null;
        status: import("@prisma/client").$Enums.UserStatus;
        statusReason: string | null;
        approvedAt: Date | null;
        area: {
            id: string;
            name: string;
        } | null;
        onboardedBy: {
            id: string;
            name: string;
            email: string;
        } | null;
        approvedBy: {
            id: string;
            name: string;
        } | null;
        onboardingDocuments: {
            id: string;
            label: string;
            fileName: string;
            mimeType: string | null;
            fileSize: number | null;
            uploadedAt: Date;
        }[];
    }>;
    deactivate(user: AuthUser, id: string, dto: UpdateUserStatusDto): Promise<{
        id: string;
        companyId: string | null;
        name: string;
        createdAt: Date;
        email: string;
        phone: string | null;
        role: import("@prisma/client").$Enums.UserRole;
        onboardedById: string | null;
        onboardingNotes: string | null;
        status: import("@prisma/client").$Enums.UserStatus;
        statusReason: string | null;
        approvedAt: Date | null;
        area: {
            id: string;
            name: string;
        } | null;
        onboardedBy: {
            id: string;
            name: string;
            email: string;
        } | null;
        approvedBy: {
            id: string;
            name: string;
        } | null;
        onboardingDocuments: {
            id: string;
            label: string;
            fileName: string;
            mimeType: string | null;
            fileSize: number | null;
            uploadedAt: Date;
        }[];
    }>;
    reactivate(user: AuthUser, id: string): Promise<{
        id: string;
        companyId: string | null;
        name: string;
        createdAt: Date;
        email: string;
        phone: string | null;
        role: import("@prisma/client").$Enums.UserRole;
        onboardedById: string | null;
        onboardingNotes: string | null;
        status: import("@prisma/client").$Enums.UserStatus;
        statusReason: string | null;
        approvedAt: Date | null;
        area: {
            id: string;
            name: string;
        } | null;
        onboardedBy: {
            id: string;
            name: string;
            email: string;
        } | null;
        approvedBy: {
            id: string;
            name: string;
        } | null;
        onboardingDocuments: {
            id: string;
            label: string;
            fileName: string;
            mimeType: string | null;
            fileSize: number | null;
            uploadedAt: Date;
        }[];
    }>;
}
