import { ForbiddenException, Injectable, UnauthorizedException } from '@nestjs/common';
import { JwtService } from '@nestjs/jwt';
import { UserStatus } from '@prisma/client';
import * as bcrypt from 'bcrypt';
import { AuthUser } from './types/auth-user.type';
import { LoginDto } from './dto/login.dto';
import { UsersService } from '../users/users.service';

@Injectable()
export class AuthService {
  constructor(
    private readonly usersService: UsersService,
    private readonly jwtService: JwtService,
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
}
