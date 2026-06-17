import { JwtService } from '@nestjs/jwt';
import { LoginDto } from './dto/login.dto';
import { UsersService } from '../users/users.service';
export declare class AuthService {
    private readonly usersService;
    private readonly jwtService;
    constructor(usersService: UsersService, jwtService: JwtService);
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
    forgotPassword(email: string): Promise<{
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
    resetPassword(token: string, newPassword: string): Promise<{
        message: string;
    }>;
}
