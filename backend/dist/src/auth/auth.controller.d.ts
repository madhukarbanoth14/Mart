import { AuthService } from './auth.service';
import { ForgotPasswordDto } from './dto/forgot-password.dto';
import { LoginDto } from './dto/login.dto';
import { SendOtpDto, VerifyOtpDto } from './dto/otp.dto';
import { ResetPasswordDto } from './dto/reset-password.dto';
import { SelfRegisterDealerDto, SelfRegisterShopkeeperDto } from './dto/self-register.dto';
import type { AuthUser } from './types/auth-user.type';
export declare class AuthController {
    private readonly authService;
    constructor(authService: AuthService);
    login(dto: LoginDto): Promise<{
        accessToken: string;
        user: {
            id: string;
            name: string;
            email: string;
            role: import("@prisma/client").$Enums.UserRole;
            companyId: string | null;
        };
    }>;
    sendOtp(dto: SendOtpDto): Promise<{
        devOtp?: string | undefined;
        success: boolean;
        message: string;
        expiresInSeconds: number;
    }>;
    verifyOtp(dto: VerifyOtpDto): Promise<{
        success: boolean;
        verificationToken: string;
        phone: string;
    }>;
    registrationAreas(state?: string, district?: string): Promise<{
        name: string;
        id: string;
        dealerId: string | null;
        state: string | null;
        district: string | null;
    }[]>;
    registrationGeo(): Promise<{
        states: {
            name: string;
            districts: string[];
        }[];
    }>;
    registerShopkeeper(dto: SelfRegisterShopkeeperDto): Promise<{
        accessToken: string;
        user: {
            id: string;
            name: string;
            email: string;
            role: string;
            companyId: string | null;
        };
        userId: string;
        message: string;
    }>;
    registerDealer(dto: SelfRegisterDealerDto): Promise<{
        accessToken: string;
        user: {
            id: string;
            name: string;
            email: string;
            role: string;
            companyId: string | null;
        };
        userId: string;
        message: string;
    }>;
    forgotPassword(dto: ForgotPasswordDto): Promise<{
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
    resetPassword(dto: ResetPasswordDto): Promise<{
        message: string;
    }>;
    me(user: AuthUser): Promise<{
        userId: string;
        name: string;
        email: string;
        role: import("@prisma/client").$Enums.UserRole;
        companyId: string | null;
        phone: string | null;
        status: import("@prisma/client").$Enums.UserStatus;
        shopName: string | null;
        address: string | null;
        state: string | null;
        district: string | null;
        documentUploaded: boolean;
        canPlaceOrders: boolean;
        documentStatus: import("@prisma/client").$Enums.UserDocumentStatus;
        documents: {
            id: string;
            label: string;
            documentType: import("@prisma/client").$Enums.BusinessDocumentType | null;
            fileName: string;
            uploadedAt: Date;
            verificationStatus: import("@prisma/client").$Enums.DocumentVerificationStatus;
            rejectionReason: string | null;
        }[];
        area: {
            id: string;
            name: string;
        } | null;
        assignedDealer: {
            name: string;
            id: string;
            email: string;
            phone: string | null;
        } | null;
    }>;
}
