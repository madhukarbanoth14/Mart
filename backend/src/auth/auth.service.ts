import { ForbiddenException, Injectable, UnauthorizedException, BadRequestException } from '@nestjs/common';
import { JwtService } from '@nestjs/jwt';
import { UserStatus } from '@prisma/client';
import * as bcrypt from 'bcrypt';
import { AreasService } from '../areas/areas.service';
import { AuthUser } from './types/auth-user.type';
import { LoginDto } from './dto/login.dto';
import { SelfRegisterDealerDto, SelfRegisterShopkeeperDto } from './dto/self-register.dto';
import { UsersService } from '../users/users.service';
import { OtpService } from './otp.service';

@Injectable()
export class AuthService {
  constructor(
    private readonly usersService: UsersService,
    private readonly jwtService: JwtService,
    private readonly otpService: OtpService,
    private readonly areasService: AreasService,
  ) {}

  async login(dto: LoginDto) {
    const loginIdentifier = (dto.identifier ?? dto.email ?? '').trim();
    if (!loginIdentifier) {
      throw new UnauthorizedException('Invalid credentials');
    }
    const user = await this.usersService.findByLoginIdentifier(loginIdentifier);
    if (!user) {
      throw new UnauthorizedException('Invalid credentials');
    }

    const passwordMatch = await bcrypt.compare(dto.password, user.password);
    if (!passwordMatch) {
      throw new UnauthorizedException('Invalid credentials');
    }

    if (user.status === UserStatus.PENDING_APPROVAL) {
      throw new ForbiddenException(
        'Your account is pending admin approval. Please try again after approval.',
      );
    }
    if (user.status === UserStatus.REJECTED) {
      throw new ForbiddenException(
        'Your account was not approved. Contact your administrator.',
      );
    }
    if (user.status === UserStatus.DEACTIVATED) {
      throw new ForbiddenException(
        'Your account has been deactivated. Contact your administrator.',
      );
    }

    const payload: AuthUser = {
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

  forgotPassword(email: string) {
    return this.usersService.requestPasswordReset(email);
  }

  resetPassword(token: string, newPassword: string) {
    return this.usersService.resetPasswordWithToken(token, newPassword);
  }

  async me(actor: AuthUser) {
    const user = await this.usersService.getAuthProfile(actor.userId);
    if (!user) {
      throw new UnauthorizedException('User not found');
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

  sendRegisterOtp(phone: string) {
    return this.otpService.sendRegisterOtp(phone);
  }

  verifyRegisterOtp(phone: string, code: string) {
    return this.otpService.verifyRegisterOtp(phone, code);
  }

  registrationAreas(state?: string, district?: string) {
    return this.areasService.findForRegistration(state, district);
  }

  registrationGeo() {
    return this.areasService.listRegistrationGeo();
  }

  async registerShopkeeper(dto: SelfRegisterShopkeeperDto) {
    const created = await this.resolveSelfRegistration(dto, (body, phone) =>
      this.usersService.selfRegisterShopkeeper(body, phone),
    );
    return this.tokenForNewUser(created);
  }

  async registerDealer(dto: SelfRegisterDealerDto) {
    const created = await this.resolveSelfRegistration(dto, (body, phone) =>
      this.usersService.selfRegisterDealer(body, phone),
    );
    return this.tokenForNewUser(created);
  }

  private async resolveSelfRegistration<
    T extends SelfRegisterShopkeeperDto | SelfRegisterDealerDto,
  >(
    dto: T,
    register: (body: T, verifiedPhone: string | null) => Promise<{
      id: string;
      name: string;
      email: string;
      role: string;
      companyId: string | null;
    }>,
  ) {
    const token = dto.verificationToken?.trim();
    const email = dto.email?.trim().toLowerCase() ?? '';
    const password = dto.password?.trim() ?? '';

    if (token) {
      const phone = await this.otpService.assertRegisterVerificationToken(
        token,
        dto.phone ?? '',
      );
      return register(dto, phone);
    }

    if (email && password.length >= 8) {
      return register({ ...dto, email, password } as T, null);
    }

    throw new BadRequestException(
      'Verify your mobile with OTP, or sign up with email and password (min 8 characters).',
    );
  }

  private async tokenForNewUser(user: {
    id: string;
    name: string;
    email: string;
    role: string;
    companyId: string | null;
  }) {
    const payload: AuthUser = {
      userId: user.id,
      email: user.email,
      role: user.role as AuthUser['role'],
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
}
