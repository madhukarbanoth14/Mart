import { BusinessDocumentType, Prisma, UserRole, UserStatus } from '@prisma/client';
import type { Response } from 'express';
import { SelfRegisterDealerDto, SelfRegisterShopkeeperDto } from '../auth/dto/self-register.dto';
import { AuthUser } from '../auth/types/auth-user.type';
import { EmailService } from '../email/email.service';
import { NotificationsService } from '../notifications/notifications.service';
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
    private readonly notifications;
    constructor(prisma: PrismaService, email: EmailService, notifications: NotificationsService);
    registerFcmToken(actor: AuthUser, token: string): Promise<{
        success: boolean;
    }>;
    clearFcmToken(actor: AuthUser): Promise<{
        success: boolean;
    }>;
    private notifyAdminsOfPendingApproval;
    findAll(actor: AuthUser, query?: ListUsersQueryDto): Promise<{
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
    countPendingApprovals(actor: AuthUser): Prisma.PrismaPromise<number>;
    findByEmail(email: string): Prisma.Prisma__UserClient<{
        name: string;
        id: string;
        companyId: string | null;
        status: import("@prisma/client").$Enums.UserStatus;
        createdAt: Date;
        role: import("@prisma/client").$Enums.UserRole;
        email: string;
        phone: string | null;
        password: string;
        passwordResetToken: string | null;
        passwordResetExpires: Date | null;
        fcmToken: string | null;
        areaId: string | null;
        onboardedById: string | null;
        onboardingNotes: string | null;
        shopName: string | null;
        address: string | null;
        latitude: number | null;
        longitude: number | null;
        approvalLoginPasswordEnc: string | null;
        statusReason: string | null;
        approvedById: string | null;
        approvedAt: Date | null;
        state: string | null;
        district: string | null;
        referralCode: string | null;
        documentUploaded: boolean;
        canPlaceOrders: boolean;
        documentStatus: import("@prisma/client").$Enums.UserDocumentStatus;
        lastFollowUpAt: Date | null;
    } | null, null, import("@prisma/client/runtime/client").DefaultArgs, Prisma.PrismaClientOptions>;
    findByLoginIdentifier(identifier: string): Prisma.Prisma__UserClient<{
        name: string;
        id: string;
        companyId: string | null;
        status: import("@prisma/client").$Enums.UserStatus;
        createdAt: Date;
        role: import("@prisma/client").$Enums.UserRole;
        email: string;
        phone: string | null;
        password: string;
        passwordResetToken: string | null;
        passwordResetExpires: Date | null;
        fcmToken: string | null;
        areaId: string | null;
        onboardedById: string | null;
        onboardingNotes: string | null;
        shopName: string | null;
        address: string | null;
        latitude: number | null;
        longitude: number | null;
        approvalLoginPasswordEnc: string | null;
        statusReason: string | null;
        approvedById: string | null;
        approvedAt: Date | null;
        state: string | null;
        district: string | null;
        referralCode: string | null;
        documentUploaded: boolean;
        canPlaceOrders: boolean;
        documentStatus: import("@prisma/client").$Enums.UserDocumentStatus;
        lastFollowUpAt: Date | null;
    } | null, null, import("@prisma/client/runtime/client").DefaultArgs, Prisma.PrismaClientOptions>;
    findByPasswordResetToken(token: string): Prisma.Prisma__UserClient<{
        name: string;
        id: string;
        companyId: string | null;
        status: import("@prisma/client").$Enums.UserStatus;
        createdAt: Date;
        role: import("@prisma/client").$Enums.UserRole;
        email: string;
        phone: string | null;
        password: string;
        passwordResetToken: string | null;
        passwordResetExpires: Date | null;
        fcmToken: string | null;
        areaId: string | null;
        onboardedById: string | null;
        onboardingNotes: string | null;
        shopName: string | null;
        address: string | null;
        latitude: number | null;
        longitude: number | null;
        approvalLoginPasswordEnc: string | null;
        statusReason: string | null;
        approvedById: string | null;
        approvedAt: Date | null;
        state: string | null;
        district: string | null;
        referralCode: string | null;
        documentUploaded: boolean;
        canPlaceOrders: boolean;
        documentStatus: import("@prisma/client").$Enums.UserDocumentStatus;
        lastFollowUpAt: Date | null;
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
        name: string;
        id: string;
        companyId: string | null;
        status: import("@prisma/client").$Enums.UserStatus;
        role: import("@prisma/client").$Enums.UserRole;
        email: string;
    } | null, null, import("@prisma/client/runtime/client").DefaultArgs, Prisma.PrismaClientOptions>;
    getAuthProfile(userId: string): Prisma.Prisma__UserClient<{
        name: string;
        id: string;
        companyId: string | null;
        status: import("@prisma/client").$Enums.UserStatus;
        role: import("@prisma/client").$Enums.UserRole;
        email: string;
        phone: string | null;
        shopName: string | null;
        address: string | null;
        state: string | null;
        district: string | null;
        documentUploaded: boolean;
        canPlaceOrders: boolean;
        documentStatus: import("@prisma/client").$Enums.UserDocumentStatus;
        area: {
            name: string;
            id: string;
            dealer: {
                name: string;
                id: string;
                email: string;
                phone: string | null;
            } | null;
        } | null;
        onboardingDocuments: {
            id: string;
            label: string;
            documentType: import("@prisma/client").$Enums.BusinessDocumentType | null;
            fileName: string;
            uploadedAt: Date;
            verificationStatus: import("@prisma/client").$Enums.DocumentVerificationStatus;
            rejectionReason: string | null;
        }[];
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
    uploadOnboardingDocument(actor: AuthUser, userId: string, label: string, file: Express.Multer.File, documentType?: BusinessDocumentType | null): Promise<{
        id: string;
        label: string;
        documentType: import("@prisma/client").$Enums.BusinessDocumentType | null;
        fileName: string;
        mimeType: string | null;
        fileSize: number | null;
        uploadedAt: Date;
        verificationStatus: import("@prisma/client").$Enums.DocumentVerificationStatus;
    }>;
    uploadMyDocument(actor: AuthUser, documentType: BusinessDocumentType, file: Express.Multer.File): Promise<{
        id: string;
        label: string;
        documentType: import("@prisma/client").$Enums.BusinessDocumentType | null;
        fileName: string;
        mimeType: string | null;
        fileSize: number | null;
        uploadedAt: Date;
        verificationStatus: import("@prisma/client").$Enums.DocumentVerificationStatus;
    }>;
    listMyDocuments(actor: AuthUser): Prisma.PrismaPromise<{
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
    syncUserDocumentEligibility(userId: string): Promise<void>;
    assertCanPlaceOrders(userId: string): Promise<void>;
    private resolveSelfRegistrationCompanyId;
    private resolveOnboardedByForArea;
    selfRegisterShopkeeper(dto: SelfRegisterShopkeeperDto, verifiedPhone: string | null): Promise<{
        message: string;
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
    selfRegisterDealer(dto: SelfRegisterDealerDto, verifiedPhone: string | null): Promise<{
        message: string;
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
    private resolveSelfRegisterCredentials;
    private notifyEmployeesOfDocumentUpload;
    verifyOnboardingDocument(actor: AuthUser, userId: string, documentId: string): Promise<{
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
    rejectOnboardingDocument(actor: AuthUser, userId: string, documentId: string, reason: string): Promise<{
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
    recordFollowUp(actor: AuthUser, userId: string): Promise<{
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
    streamMyOnboardingDocument(actor: AuthUser, documentId: string, res: Response): Promise<void>;
    streamOnboardingDocument(actor: AuthUser, userId: string, documentId: string, res: Response): Promise<void>;
    rejectUser(actor: AuthUser, userId: string, reason?: string): Promise<{
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
    deactivateUser(actor: AuthUser, userId: string, reason?: string): Promise<{
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
    reactivateUser(actor: AuthUser, userId: string): Promise<{
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
    createEmployee(actor: AuthUser, dto: CreateEmployeeDto): Promise<{
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
    createShopkeeper(actor: AuthUser, dto: CreateShopkeeperDto): Promise<{
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
    createDealer(actor: AuthUser, dto: CreateDealerDto): Promise<CreateDealerResult>;
}
