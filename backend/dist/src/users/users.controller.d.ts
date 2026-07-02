import type { Response } from 'express';
import type { AuthUser } from '../auth/types/auth-user.type';
import { CreateDealerDto } from './dto/create-dealer.dto';
import { CreateEmployeeDto } from './dto/create-employee.dto';
import { CreateShopkeeperDto } from './dto/create-shopkeeper.dto';
import { ListUsersQueryDto } from './dto/list-users-query.dto';
import { RegisterFcmTokenDto } from './dto/register-fcm-token.dto';
import { UpdateUserStatusDto } from './dto/update-user-status.dto';
import { UploadMyDocumentDto } from './dto/document.dto';
import { RejectDocumentDto } from './dto/document.dto';
import { UsersService } from './users.service';
export declare class UsersController {
    private readonly usersService;
    constructor(usersService: UsersService);
    registerFcmToken(user: AuthUser, dto: RegisterFcmTokenDto): Promise<{
        success: boolean;
    }>;
    clearFcmToken(user: AuthUser): Promise<{
        success: boolean;
    }>;
    findAll(user: AuthUser, query: ListUsersQueryDto): Promise<{
        totalOrders: number;
        name: string;
        id: string;
        companyId: string | null;
        status: import("@prisma/client").$Enums.UserStatus;
        createdAt: Date;
        _count: {
            shopkeeperOrders: number;
            dealerOrders: number;
        };
        role: import("@prisma/client").$Enums.UserRole;
        email: string;
        phone: string | null;
        onboardedById: string | null;
        onboardingNotes: string | null;
        statusReason: string | null;
        approvedAt: Date | null;
        state: string | null;
        district: string | null;
        documentUploaded: boolean;
        canPlaceOrders: boolean;
        documentStatus: import("@prisma/client").$Enums.UserDocumentStatus;
        lastFollowUpAt: Date | null;
        area: {
            name: string;
            id: string;
            state: string | null;
            district: string | null;
        } | null;
        onboardedBy: {
            name: string;
            id: string;
            email: string;
        } | null;
        approvedBy: {
            name: string;
            id: string;
        } | null;
        onboardingDocuments: {
            id: string;
            label: string;
            documentType: import("@prisma/client").$Enums.BusinessDocumentType | null;
            fileName: string;
            mimeType: string | null;
            fileSize: number | null;
            uploadedAt: Date;
            verificationStatus: import("@prisma/client").$Enums.DocumentVerificationStatus;
            verifiedAt: Date | null;
            rejectionReason: string | null;
            verifiedBy: {
                name: string;
                id: string;
            } | null;
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
        name: string;
        id: string;
        companyId: string | null;
        status: import("@prisma/client").$Enums.UserStatus;
        createdAt: Date;
        _count: {
            shopkeeperOrders: number;
            dealerOrders: number;
        };
        role: import("@prisma/client").$Enums.UserRole;
        email: string;
        phone: string | null;
        onboardedById: string | null;
        onboardingNotes: string | null;
        statusReason: string | null;
        approvedAt: Date | null;
        state: string | null;
        district: string | null;
        documentUploaded: boolean;
        canPlaceOrders: boolean;
        documentStatus: import("@prisma/client").$Enums.UserDocumentStatus;
        lastFollowUpAt: Date | null;
        area: {
            name: string;
            id: string;
            state: string | null;
            district: string | null;
        } | null;
        onboardedBy: {
            name: string;
            id: string;
            email: string;
        } | null;
        approvedBy: {
            name: string;
            id: string;
        } | null;
        onboardingDocuments: {
            id: string;
            label: string;
            documentType: import("@prisma/client").$Enums.BusinessDocumentType | null;
            fileName: string;
            mimeType: string | null;
            fileSize: number | null;
            uploadedAt: Date;
            verificationStatus: import("@prisma/client").$Enums.DocumentVerificationStatus;
            verifiedAt: Date | null;
            rejectionReason: string | null;
            verifiedBy: {
                name: string;
                id: string;
            } | null;
        }[];
    }>;
    createShopkeeper(user: AuthUser, dto: CreateShopkeeperDto): Promise<{
        loginEmail: string;
        loginPassword: string | undefined;
        message: string;
        emailSent: boolean | undefined;
        emailError: string | null | undefined;
        name: string;
        id: string;
        companyId: string | null;
        status: import("@prisma/client").$Enums.UserStatus;
        createdAt: Date;
        _count: {
            shopkeeperOrders: number;
            dealerOrders: number;
        };
        role: import("@prisma/client").$Enums.UserRole;
        email: string;
        phone: string | null;
        onboardedById: string | null;
        onboardingNotes: string | null;
        statusReason: string | null;
        approvedAt: Date | null;
        state: string | null;
        district: string | null;
        documentUploaded: boolean;
        canPlaceOrders: boolean;
        documentStatus: import("@prisma/client").$Enums.UserDocumentStatus;
        lastFollowUpAt: Date | null;
        area: {
            name: string;
            id: string;
            state: string | null;
            district: string | null;
        } | null;
        onboardedBy: {
            name: string;
            id: string;
            email: string;
        } | null;
        approvedBy: {
            name: string;
            id: string;
        } | null;
        onboardingDocuments: {
            id: string;
            label: string;
            documentType: import("@prisma/client").$Enums.BusinessDocumentType | null;
            fileName: string;
            mimeType: string | null;
            fileSize: number | null;
            uploadedAt: Date;
            verificationStatus: import("@prisma/client").$Enums.DocumentVerificationStatus;
            verifiedAt: Date | null;
            rejectionReason: string | null;
            verifiedBy: {
                name: string;
                id: string;
            } | null;
        }[];
    }>;
    createDealer(user: AuthUser, dto: CreateDealerDto): Promise<import("./users.service").CreateDealerResult>;
    uploadOnboardingDocument(user: AuthUser, id: string, label: string, file: Express.Multer.File, documentType?: string): Promise<{
        id: string;
        label: string;
        documentType: import("@prisma/client").$Enums.BusinessDocumentType | null;
        fileName: string;
        mimeType: string | null;
        fileSize: number | null;
        uploadedAt: Date;
        verificationStatus: import("@prisma/client").$Enums.DocumentVerificationStatus;
    }>;
    listMyDocuments(user: AuthUser): import("@prisma/client").Prisma.PrismaPromise<{
        id: string;
        label: string;
        documentType: import("@prisma/client").$Enums.BusinessDocumentType | null;
        fileName: string;
        mimeType: string | null;
        fileSize: number | null;
        uploadedAt: Date;
        verificationStatus: import("@prisma/client").$Enums.DocumentVerificationStatus;
        verifiedAt: Date | null;
        rejectionReason: string | null;
        verifiedBy: {
            name: string;
            id: string;
        } | null;
    }[]>;
    uploadMyDocument(user: AuthUser, dto: UploadMyDocumentDto, file: Express.Multer.File): Promise<{
        id: string;
        label: string;
        documentType: import("@prisma/client").$Enums.BusinessDocumentType | null;
        fileName: string;
        mimeType: string | null;
        fileSize: number | null;
        uploadedAt: Date;
        verificationStatus: import("@prisma/client").$Enums.DocumentVerificationStatus;
    }>;
    downloadMyDocument(user: AuthUser, documentId: string, res: Response): Promise<void>;
    verifyDocument(user: AuthUser, userId: string, documentId: string): Promise<{
        id: string;
        userId: string;
        label: string;
        documentType: import("@prisma/client").$Enums.BusinessDocumentType | null;
        fileName: string;
        mimeType: string | null;
        storageKey: string;
        fileSize: number | null;
        uploadedAt: Date;
        verificationStatus: import("@prisma/client").$Enums.DocumentVerificationStatus;
        verifiedById: string | null;
        verifiedAt: Date | null;
        rejectionReason: string | null;
    }>;
    rejectDocument(user: AuthUser, userId: string, documentId: string, dto: RejectDocumentDto): Promise<{
        id: string;
        userId: string;
        label: string;
        documentType: import("@prisma/client").$Enums.BusinessDocumentType | null;
        fileName: string;
        mimeType: string | null;
        storageKey: string;
        fileSize: number | null;
        uploadedAt: Date;
        verificationStatus: import("@prisma/client").$Enums.DocumentVerificationStatus;
        verifiedById: string | null;
        verifiedAt: Date | null;
        rejectionReason: string | null;
    }>;
    recordFollowUp(user: AuthUser, id: string): Promise<{
        name: string;
        id: string;
        companyId: string | null;
        status: import("@prisma/client").$Enums.UserStatus;
        createdAt: Date;
        _count: {
            shopkeeperOrders: number;
            dealerOrders: number;
        };
        role: import("@prisma/client").$Enums.UserRole;
        email: string;
        phone: string | null;
        onboardedById: string | null;
        onboardingNotes: string | null;
        statusReason: string | null;
        approvedAt: Date | null;
        state: string | null;
        district: string | null;
        documentUploaded: boolean;
        canPlaceOrders: boolean;
        documentStatus: import("@prisma/client").$Enums.UserDocumentStatus;
        lastFollowUpAt: Date | null;
        area: {
            name: string;
            id: string;
            state: string | null;
            district: string | null;
        } | null;
        onboardedBy: {
            name: string;
            id: string;
            email: string;
        } | null;
        approvedBy: {
            name: string;
            id: string;
        } | null;
        onboardingDocuments: {
            id: string;
            label: string;
            documentType: import("@prisma/client").$Enums.BusinessDocumentType | null;
            fileName: string;
            mimeType: string | null;
            fileSize: number | null;
            uploadedAt: Date;
            verificationStatus: import("@prisma/client").$Enums.DocumentVerificationStatus;
            verifiedAt: Date | null;
            rejectionReason: string | null;
            verifiedBy: {
                name: string;
                id: string;
            } | null;
        }[];
    }>;
    downloadOnboardingDocument(user: AuthUser, userId: string, documentId: string, res: Response): Promise<void>;
    approve(user: AuthUser, id: string): Promise<{
        loginEmail: string;
        message: string;
        loginPassword: string | undefined;
        emailSent: boolean;
        emailError: string | null;
        name: string;
        id: string;
        companyId: string | null;
        status: import("@prisma/client").$Enums.UserStatus;
        createdAt: Date;
        _count: {
            shopkeeperOrders: number;
            dealerOrders: number;
        };
        role: import("@prisma/client").$Enums.UserRole;
        email: string;
        phone: string | null;
        onboardedById: string | null;
        onboardingNotes: string | null;
        statusReason: string | null;
        approvedAt: Date | null;
        state: string | null;
        district: string | null;
        documentUploaded: boolean;
        canPlaceOrders: boolean;
        documentStatus: import("@prisma/client").$Enums.UserDocumentStatus;
        lastFollowUpAt: Date | null;
        area: {
            name: string;
            id: string;
            state: string | null;
            district: string | null;
        } | null;
        onboardedBy: {
            name: string;
            id: string;
            email: string;
        } | null;
        approvedBy: {
            name: string;
            id: string;
        } | null;
        onboardingDocuments: {
            id: string;
            label: string;
            documentType: import("@prisma/client").$Enums.BusinessDocumentType | null;
            fileName: string;
            mimeType: string | null;
            fileSize: number | null;
            uploadedAt: Date;
            verificationStatus: import("@prisma/client").$Enums.DocumentVerificationStatus;
            verifiedAt: Date | null;
            rejectionReason: string | null;
            verifiedBy: {
                name: string;
                id: string;
            } | null;
        }[];
    }>;
    reject(user: AuthUser, id: string, dto: UpdateUserStatusDto): Promise<{
        name: string;
        id: string;
        companyId: string | null;
        status: import("@prisma/client").$Enums.UserStatus;
        createdAt: Date;
        _count: {
            shopkeeperOrders: number;
            dealerOrders: number;
        };
        role: import("@prisma/client").$Enums.UserRole;
        email: string;
        phone: string | null;
        onboardedById: string | null;
        onboardingNotes: string | null;
        statusReason: string | null;
        approvedAt: Date | null;
        state: string | null;
        district: string | null;
        documentUploaded: boolean;
        canPlaceOrders: boolean;
        documentStatus: import("@prisma/client").$Enums.UserDocumentStatus;
        lastFollowUpAt: Date | null;
        area: {
            name: string;
            id: string;
            state: string | null;
            district: string | null;
        } | null;
        onboardedBy: {
            name: string;
            id: string;
            email: string;
        } | null;
        approvedBy: {
            name: string;
            id: string;
        } | null;
        onboardingDocuments: {
            id: string;
            label: string;
            documentType: import("@prisma/client").$Enums.BusinessDocumentType | null;
            fileName: string;
            mimeType: string | null;
            fileSize: number | null;
            uploadedAt: Date;
            verificationStatus: import("@prisma/client").$Enums.DocumentVerificationStatus;
            verifiedAt: Date | null;
            rejectionReason: string | null;
            verifiedBy: {
                name: string;
                id: string;
            } | null;
        }[];
    }>;
    deactivate(user: AuthUser, id: string, dto: UpdateUserStatusDto): Promise<{
        name: string;
        id: string;
        companyId: string | null;
        status: import("@prisma/client").$Enums.UserStatus;
        createdAt: Date;
        _count: {
            shopkeeperOrders: number;
            dealerOrders: number;
        };
        role: import("@prisma/client").$Enums.UserRole;
        email: string;
        phone: string | null;
        onboardedById: string | null;
        onboardingNotes: string | null;
        statusReason: string | null;
        approvedAt: Date | null;
        state: string | null;
        district: string | null;
        documentUploaded: boolean;
        canPlaceOrders: boolean;
        documentStatus: import("@prisma/client").$Enums.UserDocumentStatus;
        lastFollowUpAt: Date | null;
        area: {
            name: string;
            id: string;
            state: string | null;
            district: string | null;
        } | null;
        onboardedBy: {
            name: string;
            id: string;
            email: string;
        } | null;
        approvedBy: {
            name: string;
            id: string;
        } | null;
        onboardingDocuments: {
            id: string;
            label: string;
            documentType: import("@prisma/client").$Enums.BusinessDocumentType | null;
            fileName: string;
            mimeType: string | null;
            fileSize: number | null;
            uploadedAt: Date;
            verificationStatus: import("@prisma/client").$Enums.DocumentVerificationStatus;
            verifiedAt: Date | null;
            rejectionReason: string | null;
            verifiedBy: {
                name: string;
                id: string;
            } | null;
        }[];
    }>;
    reactivate(user: AuthUser, id: string): Promise<{
        name: string;
        id: string;
        companyId: string | null;
        status: import("@prisma/client").$Enums.UserStatus;
        createdAt: Date;
        _count: {
            shopkeeperOrders: number;
            dealerOrders: number;
        };
        role: import("@prisma/client").$Enums.UserRole;
        email: string;
        phone: string | null;
        onboardedById: string | null;
        onboardingNotes: string | null;
        statusReason: string | null;
        approvedAt: Date | null;
        state: string | null;
        district: string | null;
        documentUploaded: boolean;
        canPlaceOrders: boolean;
        documentStatus: import("@prisma/client").$Enums.UserDocumentStatus;
        lastFollowUpAt: Date | null;
        area: {
            name: string;
            id: string;
            state: string | null;
            district: string | null;
        } | null;
        onboardedBy: {
            name: string;
            id: string;
            email: string;
        } | null;
        approvedBy: {
            name: string;
            id: string;
        } | null;
        onboardingDocuments: {
            id: string;
            label: string;
            documentType: import("@prisma/client").$Enums.BusinessDocumentType | null;
            fileName: string;
            mimeType: string | null;
            fileSize: number | null;
            uploadedAt: Date;
            verificationStatus: import("@prisma/client").$Enums.DocumentVerificationStatus;
            verifiedAt: Date | null;
            rejectionReason: string | null;
            verifiedBy: {
                name: string;
                id: string;
            } | null;
        }[];
    }>;
}
