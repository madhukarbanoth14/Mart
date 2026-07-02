"use strict";
var __createBinding = (this && this.__createBinding) || (Object.create ? (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    var desc = Object.getOwnPropertyDescriptor(m, k);
    if (!desc || ("get" in desc ? !m.__esModule : desc.writable || desc.configurable)) {
      desc = { enumerable: true, get: function() { return m[k]; } };
    }
    Object.defineProperty(o, k2, desc);
}) : (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    o[k2] = m[k];
}));
var __setModuleDefault = (this && this.__setModuleDefault) || (Object.create ? (function(o, v) {
    Object.defineProperty(o, "default", { enumerable: true, value: v });
}) : function(o, v) {
    o["default"] = v;
});
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __importStar = (this && this.__importStar) || (function () {
    var ownKeys = function(o) {
        ownKeys = Object.getOwnPropertyNames || function (o) {
            var ar = [];
            for (var k in o) if (Object.prototype.hasOwnProperty.call(o, k)) ar[ar.length] = k;
            return ar;
        };
        return ownKeys(o);
    };
    return function (mod) {
        if (mod && mod.__esModule) return mod;
        var result = {};
        if (mod != null) for (var k = ownKeys(mod), i = 0; i < k.length; i++) if (k[i] !== "default") __createBinding(result, mod, k[i]);
        __setModuleDefault(result, mod);
        return result;
    };
})();
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.AuthService = void 0;
const common_1 = require("@nestjs/common");
const jwt_1 = require("@nestjs/jwt");
const client_1 = require("@prisma/client");
const bcrypt = __importStar(require("bcrypt"));
const areas_service_1 = require("../areas/areas.service");
const users_service_1 = require("../users/users.service");
const otp_service_1 = require("./otp.service");
let AuthService = class AuthService {
    usersService;
    jwtService;
    otpService;
    areasService;
    constructor(usersService, jwtService, otpService, areasService) {
        this.usersService = usersService;
        this.jwtService = jwtService;
        this.otpService = otpService;
        this.areasService = areasService;
    }
    async login(dto) {
        const loginIdentifier = (dto.identifier ?? dto.email ?? '').trim();
        if (!loginIdentifier) {
            throw new common_1.UnauthorizedException('Invalid credentials');
        }
        const user = await this.usersService.findByLoginIdentifier(loginIdentifier);
        if (!user) {
            throw new common_1.UnauthorizedException('Invalid credentials');
        }
        const passwordMatch = await bcrypt.compare(dto.password, user.password);
        if (!passwordMatch) {
            throw new common_1.UnauthorizedException('Invalid credentials');
        }
        if (user.status === client_1.UserStatus.PENDING_APPROVAL) {
            throw new common_1.ForbiddenException('Your account is pending admin approval. Please try again after approval.');
        }
        if (user.status === client_1.UserStatus.REJECTED) {
            throw new common_1.ForbiddenException('Your account was not approved. Contact your administrator.');
        }
        if (user.status === client_1.UserStatus.DEACTIVATED) {
            throw new common_1.ForbiddenException('Your account has been deactivated. Contact your administrator.');
        }
        const payload = {
            userId: user.id,
            email: user.email,
            role: user.role,
            companyId: user.companyId ?? null,
        };
        return {
            accessToken: await this.jwtService.signAsync(payload),
            user: {
                id: user.id,
                name: user.name,
                email: user.email,
                role: user.role,
                companyId: user.companyId ?? null,
            },
        };
    }
    forgotPassword(email) {
        return this.usersService.requestPasswordReset(email);
    }
    resetPassword(token, newPassword) {
        return this.usersService.resetPasswordWithToken(token, newPassword);
    }
    async me(actor) {
        const user = await this.usersService.getAuthProfile(actor.userId);
        if (!user) {
            throw new common_1.UnauthorizedException('User not found');
        }
        return {
            userId: user.id,
            name: user.name,
            email: user.email,
            role: user.role,
            companyId: user.companyId,
            phone: user.phone,
            status: user.status,
            shopName: user.shopName,
            address: user.address,
            state: user.state,
            district: user.district,
            documentUploaded: user.documentUploaded,
            canPlaceOrders: user.canPlaceOrders,
            documentStatus: user.documentStatus,
            documents: user.onboardingDocuments,
            area: user.area ? { id: user.area.id, name: user.area.name } : null,
            assignedDealer: user.area?.dealer ?? null,
        };
    }
    sendRegisterOtp(phone) {
        return this.otpService.sendRegisterOtp(phone);
    }
    verifyRegisterOtp(phone, code) {
        return this.otpService.verifyRegisterOtp(phone, code);
    }
    registrationAreas(state, district) {
        return this.areasService.findForRegistration(state, district);
    }
    registrationGeo() {
        return this.areasService.listRegistrationGeo();
    }
    async registerShopkeeper(dto) {
        const created = await this.resolveSelfRegistration(dto, (body, phone) => this.usersService.selfRegisterShopkeeper(body, phone));
        return this.tokenForNewUser(created);
    }
    async registerDealer(dto) {
        const created = await this.resolveSelfRegistration(dto, (body, phone) => this.usersService.selfRegisterDealer(body, phone));
        return this.tokenForNewUser(created);
    }
    async resolveSelfRegistration(dto, register) {
        const token = dto.verificationToken?.trim();
        const email = dto.email?.trim().toLowerCase() ?? '';
        const password = dto.password?.trim() ?? '';
        if (token) {
            const phone = await this.otpService.assertRegisterVerificationToken(token, dto.phone ?? '');
            return register(dto, phone);
        }
        if (email && password.length >= 8) {
            return register({ ...dto, email, password }, null);
        }
        throw new common_1.BadRequestException('Verify your mobile with OTP, or sign up with email and password (min 8 characters).');
    }
    async tokenForNewUser(user) {
        const payload = {
            userId: user.id,
            email: user.email,
            role: user.role,
            companyId: user.companyId ?? null,
        };
        return {
            accessToken: await this.jwtService.signAsync(payload),
            user: {
                id: user.id,
                name: user.name,
                email: user.email,
                role: user.role,
                companyId: user.companyId ?? null,
            },
            userId: user.id,
            message: 'Registration complete',
        };
    }
};
exports.AuthService = AuthService;
exports.AuthService = AuthService = __decorate([
    (0, common_1.Injectable)(),
    __metadata("design:paramtypes", [users_service_1.UsersService,
        jwt_1.JwtService,
        otp_service_1.OtpService,
        areas_service_1.AreasService])
], AuthService);
//# sourceMappingURL=auth.service.js.map