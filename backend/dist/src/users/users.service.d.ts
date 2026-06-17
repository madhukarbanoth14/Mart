import { Prisma, UserRole, UserStatus } from '@prisma/client';
import type { Response } from 'express';
import { AuthUser } from '../auth/types/auth-user.type';
import { EmailService } from '../email/email.service';
import { PrismaService } from '../prisma/prisma.service';
import { CreateDealerDto } from './dto/create-dealer.dto';
import { CreateEmployeeDto } from './dto/create-employee.dto';
import { CreateShopkeeperDto } from './dto/create-shopkeeper.dto';
import { ListUsersQueryDto } from './dto/list-users-query.dto';
export type CreateDealerResult = {
    id: string;
    name: string;
    email: string;
    phone: string | null;
    role: UserRole;
    companyId: string | null;
    createdAt: Date;
    onboardedById: string | null;
    onboardingNotes: string | null;
    status: UserStatus;
    area?: {
        id: string;
        name: string;
    } | null;
    loginEmail: string;
    userId: string;
    loginPassword?: string;
    resetPasswordToken?: string;
    resetPasswordExpiresAt?: string;
    message: string;
    emailSent?: boolean;
    emailError?: string | null;
};
export declare class UsersService {
    private readonly prisma;
    private readonly email;
    constructor(prisma: PrismaService, email: EmailService);
    findAll(actor: AuthUser, query?: ListUsersQueryDto): Prisma.PrismaPromise<{
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
    countPendingApprovals(actor: AuthUser): Prisma.PrismaPromise<number>;
    findByEmail(email: string): Prisma.Prisma__UserClient<{
        id: string;
        companyId: string | null;
        name: string;
        createdAt: Date;
        email: string;
        phone: string | null;
        password: string;
        role: import("@prisma/client").$Enums.UserRole;
        passwordResetToken: string | null;
        passwordResetExpires: Date | null;
        fcmToken: string | null;
        areaId: string | null;
        onboardedById: string | null;
        onboardingNotes: string | null;
        approvalLoginPasswordEnc: string | null;
        status: import("@prisma/client").$Enums.UserStatus;
        statusReason: string | null;
        approvedById: string | null;
        approvedAt: Date | null;
    } | null, null, import("@prisma/client/runtime/client").DefaultArgs, Prisma.PrismaClientOptions>;
    findByLoginIdentifier(identifier: string): Prisma.Prisma__UserClient<{
        id: string;
        companyId: string | null;
        name: string;
        createdAt: Date;
        email: string;
        phone: string | null;
        password: string;
        role: import("@prisma/client").$Enums.UserRole;
        passwordResetToken: string | null;
        passwordResetExpires: Date | null;
        fcmToken: string | null;
        areaId: string | null;
        onboardedById: string | null;
        onboardingNotes: string | null;
        approvalLoginPasswordEnc: string | null;
        status: import("@prisma/client").$Enums.UserStatus;
        statusReason: string | null;
        approvedById: string | null;
        approvedAt: Date | null;
    } | null, null, import("@prisma/client/runtime/client").DefaultArgs, Prisma.PrismaClientOptions>;
    findByPasswordResetToken(token: string): Prisma.Prisma__UserClient<{
        id: string;
        companyId: string | null;
        name: string;
        createdAt: Date;
        email: string;
        phone: string | null;
        password: string;
        role: import("@prisma/client").$Enums.UserRole;
        passwordResetToken: string | null;
        passwordResetExpires: Date | null;
        fcmToken: string | null;
        areaId: string | null;
        onboardedById: string | null;
        onboardingNotes: string | null;
        approvalLoginPasswordEnc: string | null;
        status: import("@prisma/client").$Enums.UserStatus;
        statusReason: string | null;
        approvedById: string | null;
        approvedAt: Date | null;
    } | null, null, import("@prisma/client/runtime/client").DefaultArgs, Prisma.PrismaClientOptions>;
    issuePasswordResetToken(userId: string): Promise<{
        token: string;
        expires: Date;
    }>;
    private assignLoginPassword;
    private sendOnboardingCredentials;
    resetPasswordWithToken(token: string, newPassword: string): Promise<{
        message: string;
    }>;
    requestPasswordReset(email: string): Promise<{
        message: string;
        emailSent: boolean;
        userId?: undefined;
        loginEmail?: undefined;
        resetPasswordToken?: undefined;
        resetPasswordExpiresAt?: undefined;
        emailError?: undefined;
    } | {
        message: string;
        userId: string;
        loginEmail: string;
        resetPasswordToken: string | undefined;
        resetPasswordExpiresAt: string;
        emailSent: boolean;
        emailError: string | null;
    }>;
    findById(id: string): Prisma.Prisma__UserClient<{
        id: string;
        companyId: string | null;
        name: string;
        email: string;
        role: import("@prisma/client").$Enums.UserRole;
        status: import("@prisma/client").$Enums.UserStatus;
    } | null, null, import("@prisma/client/runtime/client").DefaultArgs, Prisma.PrismaClientOptions>;
    private readonly userRosterSelect;
    private initialStatusForOnboarding;
    private resolveOnboardingPassword;
    private pendingCredentialEnc;
    private assertAdmin;
    private normalizePhone;
    private assertPhoneAvailable;
    private rethrowUniqueConstraint;
    private findManagedUser;
    approveUser(actor: AuthUser, userId: string): Promise<{
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
    uploadOnboardingDocument(actor: AuthUser, userId: string, label: string, file: Express.Multer.File): Promise<{
        id: string;
        label: string;
        fileName: string;
        mimeType: string | null;
        fileSize: number | null;
        uploadedAt: Date;
    }>;
    streamOnboardingDocument(actor: AuthUser, userId: string, documentId: string, res: Response): Promise<void>;
    rejectUser(actor: AuthUser, userId: string, reason?: string): Promise<{
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
    deactivateUser(actor: AuthUser, userId: string, reason?: string): Promise<{
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
    reactivateUser(actor: AuthUser, userId: string): Promise<{
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
    createEmployee(actor: AuthUser, dto: CreateEmployeeDto): Promise<{
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
    createShopkeeper(actor: AuthUser, dto: CreateShopkeeperDto): Promise<{
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
    createDealer(actor: AuthUser, dto: CreateDealerDto): Promise<CreateDealerResult>;
}
