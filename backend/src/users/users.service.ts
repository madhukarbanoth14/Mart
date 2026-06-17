import {
  BadRequestException,
  ConflictException,
  ForbiddenException,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import { Prisma, UserRole, UserStatus } from '@prisma/client';
import * as bcrypt from 'bcrypt';
import { randomBytes } from 'crypto';
import { createReadStream, existsSync, mkdirSync, writeFileSync } from 'fs';
import { join } from 'path';
import type { Response } from 'express';
import { AuthUser } from '../auth/types/auth-user.type';
import { EmailService } from '../email/email.service';
import { PrismaService } from '../prisma/prisma.service';
import { CreateDealerDto } from './dto/create-dealer.dto';
import { CreateEmployeeDto } from './dto/create-employee.dto';
import { CreateShopkeeperDto } from './dto/create-shopkeeper.dto';
import { ListUsersQueryDto } from './dto/list-users-query.dto';
import {
  decryptApprovalPassword,
  encryptApprovalPassword,
} from './credential-vault';

const PASSWORD_RESET_TTL_MS = 7 * 24 * 60 * 60 * 1000;
const ONBOARDING_UPLOAD_DIR = join(process.cwd(), 'uploads', 'onboarding');
const MAX_ONBOARDING_FILE_BYTES = 10 * 1024 * 1024;

function generateLoginPassword(): string {
  const suffix = randomBytes(4).toString('base64url').replace(/[^a-zA-Z0-9]/g, '').slice(0, 6);
  return `Knsr@${suffix}9`;
}

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
  area?: { id: string; name: string } | null;
  loginEmail: string;
  userId: string;
  loginPassword?: string;
  resetPasswordToken?: string;
  resetPasswordExpiresAt?: string;
  message: string;
  emailSent?: boolean;
  emailError?: string | null;
};

@Injectable()
export class UsersService {
  constructor(
    private readonly prisma: PrismaService,
    private readonly email: EmailService,
  ) {}

  findAll(actor: AuthUser, query: ListUsersQueryDto = {}) {
    const where: Prisma.UserWhereInput = {};
    if (actor.companyId) {
      where.companyId = actor.companyId;
    }
    if (query.role) {
      where.role = query.role;
    }
    if (query.status) {
      where.status = query.status;
    }

    return this.prisma.user.findMany({
      where,
      select: this.userRosterSelect,
      orderBy: { createdAt: 'desc' },
    });
  }

  countPendingApprovals(actor: AuthUser) {
    const where: Prisma.UserWhereInput = {
      status: UserStatus.PENDING_APPROVAL,
      role: { in: [UserRole.DEALER, UserRole.SHOPKEEPER] },
    };
    if (actor.companyId) {
      where.companyId = actor.companyId;
    }
    return this.prisma.user.count({ where });
  }

  findByEmail(email: string) {
    return this.prisma.user.findUnique({ where: { email } });
  }

  findByLoginIdentifier(identifier: string) {
    const loginId = identifier.trim();
    return this.prisma.user.findFirst({
      where: {
        OR: [{ email: loginId.toLowerCase() }, { phone: loginId }],
      },
    });
  }

  findByPasswordResetToken(token: string) {
    return this.prisma.user.findFirst({
      where: {
        passwordResetToken: token.trim(),
        passwordResetExpires: { gt: new Date() },
      },
    });
  }

  async issuePasswordResetToken(userId: string) {
    const token = randomBytes(24).toString('hex');
    const expires = new Date(Date.now() + PASSWORD_RESET_TTL_MS);
    await this.prisma.user.update({
      where: { id: userId },
      data: {
        passwordResetToken: token,
        passwordResetExpires: expires,
      },
    });
    return { token, expires };
  }

  private async assignLoginPassword(userId: string): Promise<string> {
    const plain = generateLoginPassword();
    const password = await bcrypt.hash(plain, 10);
    await this.prisma.user.update({
      where: { id: userId },
      data: {
        password,
        passwordResetToken: null,
        passwordResetExpires: null,
      },
    });
    return plain;
  }

  private async sendOnboardingCredentials(params: {
    to: string;
    name: string;
    role: 'DEALER' | 'SHOPKEEPER' | 'EMPLOYEE';
    loginEmail: string;
    loginPassword: string;
    context: 'approved' | 'created';
  }) {
    return this.email.sendOnboardingCredentialsEmail(params);
  }

  async resetPasswordWithToken(token: string, newPassword: string) {
    const user = await this.findByPasswordResetToken(token);
    if (!user) {
      throw new BadRequestException('Invalid or expired reset link');
    }
    if (user.status !== UserStatus.ACTIVE) {
      throw new BadRequestException(
        'Account is not active. Contact your administrator.',
      );
    }
    const password = await bcrypt.hash(newPassword, 10);
    await this.prisma.user.update({
      where: { id: user.id },
      data: {
        password,
        passwordResetToken: null,
        passwordResetExpires: null,
      },
    });
    return { message: 'Password updated. You can sign in now.' };
  }

  async requestPasswordReset(email: string) {
    const normalized = email.trim().toLowerCase();
    const user = await this.prisma.user.findUnique({ where: { email: normalized } });
    if (!user) {
      return {
        message: 'If an account exists for this email, a reset link has been sent.',
        emailSent: false,
      };
    }
    if (user.status !== UserStatus.ACTIVE) {
      return {
        message: 'If an account exists for this email, a reset link has been sent.',
        emailSent: false,
      };
    }
    const { token, expires } = await this.issuePasswordResetToken(user.id);
    const mailResult = await this.email.sendPasswordResetEmail({
      to: user.email,
      name: user.name,
      resetToken: token,
      resetExpiresAt: expires,
    });
    return {
      message: mailResult.sent
        ? 'Password reset email sent. Check your inbox.'
        : 'Password reset link generated. Share it with the dealer to set their password.',
      userId: user.id,
      loginEmail: user.email,
      resetPasswordToken: mailResult.sent ? undefined : token,
      resetPasswordExpiresAt: expires.toISOString(),
      emailSent: mailResult.sent,
      emailError: mailResult.sent ? null : mailResult.reason,
    };
  }

  findById(id: string) {
    return this.prisma.user.findUnique({
      where: { id },
      select: {
        id: true,
        name: true,
        email: true,
        role: true,
        companyId: true,
        status: true,
      },
    });
  }

  private readonly userRosterSelect = {
    id: true,
    name: true,
    email: true,
    phone: true,
    role: true,
    companyId: true,
    createdAt: true,
    onboardedById: true,
    onboardingNotes: true,
    status: true,
    statusReason: true,
    approvedAt: true,
    area: {
      select: { id: true, name: true },
    },
    onboardedBy: {
      select: { id: true, name: true, email: true },
    },
    approvedBy: {
      select: { id: true, name: true },
    },
    onboardingDocuments: {
      select: {
        id: true,
        label: true,
        fileName: true,
        mimeType: true,
        fileSize: true,
        uploadedAt: true,
      },
      orderBy: { uploadedAt: 'asc' },
    },
  } as const;

  private initialStatusForOnboarding(actor: AuthUser): UserStatus {
    return actor.role === UserRole.ADMIN
      ? UserStatus.ACTIVE
      : UserStatus.PENDING_APPROVAL;
  }

  private resolveOnboardingPassword(
    actor: AuthUser,
    password?: string | null,
  ): string {
    const plain = password?.trim();
    const employeeOnboard = actor.role === UserRole.EMPLOYEE;
    if (!plain || plain.length < 8) {
      if (employeeOnboard) {
        throw new BadRequestException(
          'Sign-in password is required (minimum 8 characters)',
        );
      }
      return generateLoginPassword();
    }
    return plain;
  }

  private pendingCredentialEnc(
    status: UserStatus,
    plainPassword: string,
  ): string | null {
    return status === UserStatus.PENDING_APPROVAL
      ? encryptApprovalPassword(plainPassword)
      : null;
  }

  private assertAdmin(actor: AuthUser) {
    if (actor.role !== UserRole.ADMIN) {
      throw new ForbiddenException('Admin access required');
    }
  }

  private normalizePhone(phone?: string | null): string | null {
    if (!phone) return null;
    const digits = phone.replace(/\D/g, '');
    return digits.length > 0 ? digits : null;
  }

  private async assertPhoneAvailable(phone?: string | null): Promise<void> {
    const normalized = this.normalizePhone(phone);
    if (!normalized) return;
    const existing = await this.prisma.user.findUnique({
      where: { phone: normalized },
      select: { email: true },
    });
    if (existing) {
      throw new ConflictException(
        `This phone number is already registered${existing.email ? ` to ${existing.email}` : ''}`,
      );
    }
  }

  private rethrowUniqueConstraint(error: unknown): never {
    if (error instanceof Prisma.PrismaClientKnownRequestError) {
      if (error.code === 'P2002') {
        const target = error.meta?.target;
        const fields = Array.isArray(target) ? target : [];
        if (fields.includes('phone')) {
          throw new ConflictException('This phone number is already registered');
        }
        if (fields.includes('email')) {
          throw new ConflictException('A user with this email already exists');
        }
        throw new ConflictException('A user with these details already exists');
      }
      if (error.code === 'P2003') {
        throw new BadRequestException(
          'Invalid area or session. Choose an area from the list and sign in again if the problem continues.',
        );
      }
    }
    throw error;
  }

  private async findManagedUser(actor: AuthUser, userId: string) {
    const user = await this.prisma.user.findUnique({
      where: { id: userId },
      select: {
        id: true,
        name: true,
        email: true,
        role: true,
        companyId: true,
        status: true,
        passwordResetToken: true,
        passwordResetExpires: true,
      },
    });
    if (!user) {
      throw new NotFoundException('User not found');
    }
    if (actor.companyId && user.companyId !== actor.companyId) {
      throw new ForbiddenException('User is outside your company');
    }
    if (user.role !== UserRole.DEALER && user.role !== UserRole.SHOPKEEPER) {
      throw new BadRequestException('Only dealers and shopkeepers can be managed here');
    }
    return user;
  }

  async approveUser(actor: AuthUser, userId: string) {
    this.assertAdmin(actor);
    const user = await this.findManagedUser(actor, userId);
    if (user.status !== UserStatus.PENDING_APPROVAL) {
      throw new BadRequestException('User is not awaiting approval');
    }

    let loginPassword: string | undefined;
    let emailSent = false;
    let emailError: string | null = null;

    if (user.role === UserRole.DEALER || user.role === UserRole.SHOPKEEPER) {
      const pending = await this.prisma.user.findUnique({
        where: { id: userId },
        select: { approvalLoginPasswordEnc: true },
      });
      loginPassword =
        decryptApprovalPassword(pending?.approvalLoginPasswordEnc) ??
        undefined;
      if (!loginPassword) {
        loginPassword = await this.assignLoginPassword(userId);
      }
      const mailResult = await this.sendOnboardingCredentials({
        to: user.email,
        name: user.name,
        role: user.role,
        loginEmail: user.email,
        loginPassword,
        context: 'approved',
      });
      emailSent = mailResult.sent;
      emailError = mailResult.sent ? null : mailResult.reason;
    }

    const updated = await this.prisma.user.update({
      where: { id: userId },
      data: {
        status: UserStatus.ACTIVE,
        statusReason: null,
        approvedById: actor.userId,
        approvedAt: new Date(),
        approvalLoginPasswordEnc: null,
      },
      select: this.userRosterSelect,
    });

    return {
      ...updated,
      loginEmail: user.email,
      message: emailSent
        ? `User approved. Login confirmation emailed to ${user.email}.`
        : loginPassword
          ? 'User approved. Share the login credentials below (email could not be sent).'
          : 'User approved. They can sign in now.',
      loginPassword: emailSent ? undefined : loginPassword,
      emailSent,
      emailError,
    };
  }

  async uploadOnboardingDocument(
    actor: AuthUser,
    userId: string,
    label: string,
    file: Express.Multer.File,
  ) {
    const trimmedLabel = label?.trim();
    if (!trimmedLabel) {
      throw new BadRequestException('Document label is required');
    }
    if (!file?.buffer?.length) {
      throw new BadRequestException('File is required');
    }
    if (file.size > MAX_ONBOARDING_FILE_BYTES) {
      throw new BadRequestException('File exceeds 10 MB limit');
    }

    const user = await this.prisma.user.findUnique({
      where: { id: userId },
      select: {
        id: true,
        companyId: true,
        role: true,
        status: true,
        onboardedById: true,
      },
    });
    if (!user) {
      throw new NotFoundException('User not found');
    }
    if (user.role !== UserRole.DEALER && user.role !== UserRole.SHOPKEEPER) {
      throw new BadRequestException('Documents can only be attached to dealers or shopkeepers');
    }
    if (actor.companyId && user.companyId !== actor.companyId) {
      throw new ForbiddenException('User is outside your company');
    }
    const isAdmin = actor.role === UserRole.ADMIN;
    const isOnboarder =
      actor.role === UserRole.EMPLOYEE && user.onboardedById === actor.userId;
    if (!isAdmin && !isOnboarder) {
      throw new ForbiddenException('You cannot upload documents for this user');
    }
    if (user.status !== UserStatus.PENDING_APPROVAL && !isAdmin) {
      throw new BadRequestException('Documents can only be added while awaiting approval');
    }

    mkdirSync(ONBOARDING_UPLOAD_DIR, { recursive: true });
    const storageKey = `${userId}/${randomBytes(16).toString('hex')}-${file.originalname.replace(/[^\w.\-]+/g, '_')}`;
    const absolutePath = join(ONBOARDING_UPLOAD_DIR, storageKey);
    mkdirSync(join(ONBOARDING_UPLOAD_DIR, userId), { recursive: true });
    writeFileSync(absolutePath, file.buffer);

    return this.prisma.onboardingDocument.create({
      data: {
        userId,
        label: trimmedLabel,
        fileName: file.originalname || 'document',
        mimeType: file.mimetype || null,
        storageKey,
        fileSize: file.size,
      },
      select: {
        id: true,
        label: true,
        fileName: true,
        mimeType: true,
        fileSize: true,
        uploadedAt: true,
      },
    });
  }

  async streamOnboardingDocument(
    actor: AuthUser,
    userId: string,
    documentId: string,
    res: Response,
  ) {
    if (actor.role !== UserRole.ADMIN && actor.role !== UserRole.EMPLOYEE) {
      throw new ForbiddenException('Access denied');
    }
    const doc = await this.prisma.onboardingDocument.findFirst({
      where: { id: documentId, userId },
      include: {
        user: { select: { companyId: true, role: true } },
      },
    });
    if (!doc) {
      throw new NotFoundException('Document not found');
    }
    if (actor.companyId && doc.user.companyId !== actor.companyId) {
      throw new ForbiddenException('Document is outside your company');
    }

    const absolutePath = join(ONBOARDING_UPLOAD_DIR, doc.storageKey);
    if (!existsSync(absolutePath)) {
      throw new NotFoundException('File not found on server');
    }

    res.setHeader(
      'Content-Type',
      doc.mimeType || 'application/octet-stream',
    );
    res.setHeader(
      'Content-Disposition',
      `inline; filename="${doc.fileName.replace(/"/g, '')}"`,
    );
    createReadStream(absolutePath).pipe(res);
  }

  async rejectUser(actor: AuthUser, userId: string, reason?: string) {
    this.assertAdmin(actor);
    const user = await this.findManagedUser(actor, userId);
    if (user.status !== UserStatus.PENDING_APPROVAL) {
      throw new BadRequestException('User is not awaiting approval');
    }
    return this.prisma.user.update({
      where: { id: userId },
      data: {
        status: UserStatus.REJECTED,
        statusReason: reason?.trim() || 'Rejected by admin',
        passwordResetToken: null,
        passwordResetExpires: null,
      },
      select: this.userRosterSelect,
    });
  }

  async deactivateUser(actor: AuthUser, userId: string, reason?: string) {
    this.assertAdmin(actor);
    const user = await this.findManagedUser(actor, userId);
    if (user.status === UserStatus.DEACTIVATED) {
      throw new BadRequestException('User is already deactivated');
    }
    return this.prisma.user.update({
      where: { id: userId },
      data: {
        status: UserStatus.DEACTIVATED,
        statusReason: reason?.trim() || 'Deactivated by admin',
        passwordResetToken: null,
        passwordResetExpires: null,
      },
      select: this.userRosterSelect,
    });
  }

  async reactivateUser(actor: AuthUser, userId: string) {
    this.assertAdmin(actor);
    const user = await this.findManagedUser(actor, userId);
    if (user.status !== UserStatus.DEACTIVATED) {
      throw new BadRequestException('Only deactivated users can be reactivated');
    }
    return this.prisma.user.update({
      where: { id: userId },
      data: {
        status: UserStatus.ACTIVE,
        statusReason: null,
        approvedById: actor.userId,
        approvedAt: new Date(),
      },
      select: this.userRosterSelect,
    });
  }

  async createEmployee(actor: AuthUser, dto: CreateEmployeeDto) {
    this.assertAdmin(actor);
    if (!actor.companyId) {
      throw new BadRequestException('Company scope is required');
    }
    const email = dto.email.trim().toLowerCase();
    const existing = await this.prisma.user.findUnique({ where: { email } });
    if (existing) {
      throw new ConflictException('A user with this email already exists');
    }
    const phone = this.normalizePhone(dto.phone);
    await this.assertPhoneAvailable(phone);
    const plain =
      dto.password?.trim() && dto.password.length >= 8
        ? dto.password.trim()
        : generateLoginPassword();
    const password = await bcrypt.hash(plain, 10);
    let created;
    try {
      created = await this.prisma.user.create({
        data: {
          name: dto.name.trim(),
          email,
          phone,
          password,
          role: UserRole.EMPLOYEE,
          companyId: actor.companyId,
          status: UserStatus.ACTIVE,
          approvedById: actor.userId,
          approvedAt: new Date(),
        },
        select: this.userRosterSelect,
      });
    } catch (error) {
      this.rethrowUniqueConstraint(error);
    }

    const mailResult = await this.sendOnboardingCredentials({
      to: email,
      name: created.name,
      role: 'EMPLOYEE',
      loginEmail: email,
      loginPassword: plain,
      context: 'created',
    });

    return {
      ...created,
      loginEmail: email,
      loginPassword: mailResult.sent ? undefined : plain,
      message: mailResult.sent
        ? `Employee created. Login credentials emailed to ${email}.`
        : 'Employee created. Share the login credentials below (email could not be sent).',
      emailSent: mailResult.sent,
      emailError: mailResult.sent ? null : mailResult.reason,
    };
  }

  async createShopkeeper(actor: AuthUser, dto: CreateShopkeeperDto) {
    if (!actor.companyId) {
      throw new BadRequestException(
        'Company scope is required to onboard shopkeepers',
      );
    }
    const email = dto.email.trim().toLowerCase();
    const existing = await this.prisma.user.findUnique({
      where: { email },
    });
    if (existing) {
      throw new ConflictException('A user with this email already exists');
    }
    const phone = this.normalizePhone(dto.phone);
    await this.assertPhoneAvailable(phone);
    const area = await this.prisma.area.findFirst({
      where: { id: dto.areaId, companyId: actor.companyId },
    });
    if (!area) {
      throw new NotFoundException(
        'Area not found or not in your company',
      );
    }
    const plain = this.resolveOnboardingPassword(actor, dto.password);
    const password = await bcrypt.hash(plain, 10);
    const status = this.initialStatusForOnboarding(actor);
    const credentialEnc = this.pendingCredentialEnc(status, plain);
    let created;
    try {
      created = await this.prisma.user.create({
        data: {
          name: dto.name.trim(),
          email,
          phone,
          password,
          role: UserRole.SHOPKEEPER,
          companyId: actor.companyId,
          areaId: dto.areaId,
          onboardedById: actor.userId,
          onboardingNotes: dto.onboardingNotes?.trim() || null,
          approvalLoginPasswordEnc: credentialEnc,
          status,
          approvedById: status === UserStatus.ACTIVE ? actor.userId : null,
          approvedAt: status === UserStatus.ACTIVE ? new Date() : null,
        },
        select: this.userRosterSelect,
      });
    } catch (error) {
      this.rethrowUniqueConstraint(error);
    }

    let emailSent = false;
    let emailError: string | null = null;
    if (status === UserStatus.ACTIVE) {
      const mailResult = await this.sendOnboardingCredentials({
        to: email,
        name: created.name,
        role: 'SHOPKEEPER',
        loginEmail: email,
        loginPassword: plain,
        context: 'created',
      });
      emailSent = mailResult.sent;
      emailError = mailResult.sent ? null : mailResult.reason;
    }

    const pendingMessage =
      'Shopkeeper submitted for admin approval. Login details will be emailed after approval.';
    const activeMessage = emailSent
      ? `Shopkeeper created. Login credentials emailed to ${email}.`
      : 'Shopkeeper created. Share the login credentials below (email could not be sent).';

    return {
      ...created,
      loginEmail: email,
      loginPassword: status === UserStatus.ACTIVE && !emailSent ? plain : undefined,
      message:
        status === UserStatus.PENDING_APPROVAL ? pendingMessage : activeMessage,
      emailSent: status === UserStatus.ACTIVE ? emailSent : undefined,
      emailError: status === UserStatus.ACTIVE ? emailError : undefined,
    };
  }

  async createDealer(actor: AuthUser, dto: CreateDealerDto): Promise<CreateDealerResult> {
    if (!actor.companyId) {
      throw new BadRequestException(
        'Company scope is required to onboard dealers',
      );
    }
    const email = dto.email.trim().toLowerCase();
    const existing = await this.prisma.user.findUnique({
      where: { email },
    });
    if (existing) {
      throw new ConflictException('A user with this email already exists');
    }
    const phone = this.normalizePhone(dto.phone);
    await this.assertPhoneAvailable(phone);
    const area = await this.prisma.area.findFirst({
      where: { id: dto.areaId, companyId: actor.companyId },
    });
    if (!area) {
      throw new NotFoundException('Area not found or not in your company');
    }
    const status = this.initialStatusForOnboarding(actor);
    const plain = this.resolveOnboardingPassword(actor, dto.password);
    const password = await bcrypt.hash(plain, 10);
    const credentialEnc = this.pendingCredentialEnc(status, plain);

    let dealer;
    try {
      dealer = await this.prisma.$transaction(async (tx) => {
        const created = await tx.user.create({
          data: {
            name: dto.name.trim(),
            email,
            phone,
            password,
            role: UserRole.DEALER,
            companyId: actor.companyId,
            onboardedById: actor.userId,
            onboardingNotes: dto.onboardingNotes?.trim() || null,
            approvalLoginPasswordEnc: credentialEnc,
            status,
            approvedById: status === UserStatus.ACTIVE ? actor.userId : null,
            approvedAt: status === UserStatus.ACTIVE ? new Date() : null,
          },
          select: this.userRosterSelect,
        });
        await tx.area.update({
          where: { id: dto.areaId },
          data: { dealerId: created.id },
        });
        return created;
      });
    } catch (error) {
      this.rethrowUniqueConstraint(error);
    }

    let emailSent = false;
    let emailError: string | null = null;
    if (status === UserStatus.ACTIVE) {
      const mailResult = await this.sendOnboardingCredentials({
        to: email,
        name: dealer.name,
        role: 'DEALER',
        loginEmail: email,
        loginPassword: plain,
        context: 'created',
      });
      emailSent = mailResult.sent;
      emailError = mailResult.sent ? null : mailResult.reason;
    }

    const pendingMessage =
      'Dealer submitted for admin approval. Login details will be emailed after approval.';
    const activeMessage = emailSent
      ? `Dealer created. Login credentials emailed to ${email}.`
      : 'Dealer created. Share the login credentials below (email could not be sent).';

    return {
      ...dealer,
      loginEmail: email,
      userId: dealer.id,
      loginPassword:
        status === UserStatus.ACTIVE && !emailSent ? plain : undefined,
      message: status === UserStatus.PENDING_APPROVAL ? pendingMessage : activeMessage,
      emailSent: status === UserStatus.ACTIVE ? emailSent : undefined,
      emailError: status === UserStatus.ACTIVE ? emailError : undefined,
    };
  }
}
