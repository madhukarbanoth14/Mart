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
exports.UsersService = void 0;
const common_1 = require("@nestjs/common");
const client_1 = require("@prisma/client");
const bcrypt = __importStar(require("bcrypt"));
const crypto_1 = require("crypto");
const fs_1 = require("fs");
const path_1 = require("path");
const email_service_1 = require("../email/email.service");
const notifications_service_1 = require("../notifications/notifications.service");
const prisma_service_1 = require("../prisma/prisma.service");
const credential_vault_1 = require("./credential-vault");
const document_eligibility_util_1 = require("./document-eligibility.util");
const PASSWORD_RESET_TTL_MS = 7 * 24 * 60 * 60 * 1000;
const ONBOARDING_UPLOAD_DIR = (0, path_1.join)(process.cwd(), 'uploads', 'onboarding');
const MAX_ONBOARDING_FILE_BYTES = 10 * 1024 * 1024;
function generateLoginPassword() {
    const suffix = (0, crypto_1.randomBytes)(4).toString('base64url').replace(/[^a-zA-Z0-9]/g, '').slice(0, 6);
    return `Knsr@${suffix}9`;
}
let UsersService = class UsersService {
    prisma;
    email;
    notifications;
    constructor(prisma, email, notifications) {
        this.prisma = prisma;
        this.email = email;
        this.notifications = notifications;
    }
    async registerFcmToken(actor, token) {
        const trimmed = token.trim();
        await this.prisma.user.updateMany({
            where: { fcmToken: trimmed, NOT: { id: actor.userId } },
            data: { fcmToken: null },
        });
        await this.prisma.user.update({
            where: { id: actor.userId },
            data: { fcmToken: trimmed },
        });
        return { success: true };
    }
    async clearFcmToken(actor) {
        await this.prisma.user.update({
            where: { id: actor.userId },
            data: { fcmToken: null },
        });
        return { success: true };
    }
    async notifyAdminsOfPendingApproval(companyId, pendingName, roleLabel, pendingUserId) {
        const admins = await this.prisma.user.findMany({
            where: {
                companyId,
                role: client_1.UserRole.ADMIN,
                status: client_1.UserStatus.ACTIVE,
            },
            select: { id: true },
        });
        await this.notifications.sendToUsers(admins.map((a) => a.id), 'Approval needed', `${pendingName} (${roleLabel}) is awaiting your approval`, { type: 'APPROVAL', userId: pendingUserId });
    }
    findAll(actor, query = {}) {
        const where = {};
        if (actor.companyId) {
            where.companyId = actor.companyId;
        }
        if (query.role) {
            where.role = query.role;
        }
        if (query.status) {
            where.status = query.status;
        }
        return this.prisma.user
            .findMany({
            where,
            select: this.userRosterSelect,
            orderBy: { createdAt: 'desc' },
        })
            .then((rows) => rows.map((row) => ({
            ...row,
            totalOrders: row.role === client_1.UserRole.SHOPKEEPER
                ? row._count.shopkeeperOrders
                : row.role === client_1.UserRole.DEALER
                    ? row._count.dealerOrders
                    : 0,
        })));
    }
    countPendingApprovals(actor) {
        const where = {
            status: client_1.UserStatus.PENDING_APPROVAL,
            role: { in: [client_1.UserRole.DEALER, client_1.UserRole.SHOPKEEPER] },
        };
        if (actor.companyId) {
            where.companyId = actor.companyId;
        }
        return this.prisma.user.count({ where });
    }
    findByEmail(email) {
        return this.prisma.user.findUnique({ where: { email } });
    }
    findByLoginIdentifier(identifier) {
        const loginId = identifier.trim();
        const phone = this.normalizePhone(loginId);
        const or = [{ email: loginId.toLowerCase() }];
        if (phone) {
            or.push({ phone });
        }
        return this.prisma.user.findFirst({
            where: { OR: or },
        });
    }
    findByPasswordResetToken(token) {
        return this.prisma.user.findFirst({
            where: {
                passwordResetToken: token.trim(),
                passwordResetExpires: { gt: new Date() },
            },
        });
    }
    async issuePasswordResetToken(userId) {
        const token = (0, crypto_1.randomBytes)(24).toString('hex');
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
    async assignLoginPassword(userId) {
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
    async sendOnboardingCredentials(params) {
        return this.email.sendOnboardingCredentialsEmail(params);
    }
    async resetPasswordWithToken(token, newPassword) {
        const user = await this.findByPasswordResetToken(token);
        if (!user) {
            throw new common_1.BadRequestException('Invalid or expired reset link');
        }
        if (user.status !== client_1.UserStatus.ACTIVE) {
            throw new common_1.BadRequestException('Account is not active. Contact your administrator.');
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
    async requestPasswordReset(email) {
        const normalized = email.trim().toLowerCase();
        const user = await this.prisma.user.findUnique({ where: { email: normalized } });
        if (!user) {
            return {
                message: 'If an account exists for this email, a reset link has been sent.',
                emailSent: false,
            };
        }
        if (user.status !== client_1.UserStatus.ACTIVE) {
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
    findById(id) {
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
    getAuthProfile(userId) {
        return this.prisma.user.findUnique({
            where: { id: userId },
            select: {
                id: true,
                name: true,
                email: true,
                role: true,
                companyId: true,
                phone: true,
                status: true,
                shopName: true,
                address: true,
                state: true,
                district: true,
                documentUploaded: true,
                canPlaceOrders: true,
                documentStatus: true,
                area: {
                    select: {
                        id: true,
                        name: true,
                        dealer: {
                            select: { id: true, name: true, email: true, phone: true },
                        },
                    },
                },
                onboardingDocuments: {
                    select: {
                        id: true,
                        label: true,
                        documentType: true,
                        fileName: true,
                        verificationStatus: true,
                        uploadedAt: true,
                        rejectionReason: true,
                    },
                    orderBy: { uploadedAt: 'desc' },
                },
            },
        });
    }
    userRosterSelect = {
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
        state: true,
        district: true,
        documentUploaded: true,
        canPlaceOrders: true,
        documentStatus: true,
        lastFollowUpAt: true,
        area: {
            select: { id: true, name: true, state: true, district: true },
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
                documentType: true,
                fileName: true,
                mimeType: true,
                fileSize: true,
                uploadedAt: true,
                verificationStatus: true,
                verifiedAt: true,
                verifiedBy: { select: { id: true, name: true } },
                rejectionReason: true,
            },
            orderBy: { uploadedAt: 'asc' },
        },
        _count: {
            select: {
                shopkeeperOrders: true,
                dealerOrders: true,
            },
        },
    };
    initialStatusForOnboarding(actor) {
        return actor.role === client_1.UserRole.ADMIN
            ? client_1.UserStatus.ACTIVE
            : client_1.UserStatus.PENDING_APPROVAL;
    }
    resolveOnboardingPassword(actor, password) {
        const plain = password?.trim();
        const employeeOnboard = actor.role === client_1.UserRole.EMPLOYEE;
        if (!plain || plain.length < 8) {
            if (employeeOnboard) {
                throw new common_1.BadRequestException('Sign-in password is required (minimum 8 characters)');
            }
            return generateLoginPassword();
        }
        return plain;
    }
    pendingCredentialEnc(status, plainPassword) {
        return status === client_1.UserStatus.PENDING_APPROVAL
            ? (0, credential_vault_1.encryptApprovalPassword)(plainPassword)
            : null;
    }
    assertAdmin(actor) {
        if (actor.role !== client_1.UserRole.ADMIN) {
            throw new common_1.ForbiddenException('Admin access required');
        }
    }
    normalizePhone(phone) {
        if (!phone)
            return null;
        let digits = phone.replace(/\D/g, '');
        if (digits.length === 0)
            return null;
        if (digits.length === 12 && digits.startsWith('91')) {
            digits = digits.slice(2);
        }
        else if (digits.length === 11 && digits.startsWith('0')) {
            digits = digits.slice(1);
        }
        return digits;
    }
    async assertPhoneAvailable(phone) {
        const normalized = this.normalizePhone(phone);
        if (!normalized)
            return;
        const existing = await this.prisma.user.findUnique({
            where: { phone: normalized },
            select: { email: true },
        });
        if (existing) {
            throw new common_1.ConflictException(`This phone number is already registered${existing.email ? ` to ${existing.email}` : ''}`);
        }
    }
    rethrowUniqueConstraint(error) {
        if (error instanceof client_1.Prisma.PrismaClientKnownRequestError) {
            if (error.code === 'P2002') {
                const target = error.meta?.target;
                const fields = Array.isArray(target) ? target : [];
                if (fields.includes('phone')) {
                    throw new common_1.ConflictException('This phone number is already registered');
                }
                if (fields.includes('email')) {
                    throw new common_1.ConflictException('A user with this email already exists');
                }
                throw new common_1.ConflictException('A user with these details already exists');
            }
            if (error.code === 'P2003') {
                throw new common_1.BadRequestException('Invalid area or session. Choose an area from the list and sign in again if the problem continues.');
            }
        }
        throw error;
    }
    async findManagedUser(actor, userId) {
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
            throw new common_1.NotFoundException('User not found');
        }
        if (actor.companyId && user.companyId !== actor.companyId) {
            throw new common_1.ForbiddenException('User is outside your company');
        }
        if (user.role !== client_1.UserRole.DEALER && user.role !== client_1.UserRole.SHOPKEEPER) {
            throw new common_1.BadRequestException('Only dealers and shopkeepers can be managed here');
        }
        return user;
    }
    async approveUser(actor, userId) {
        this.assertAdmin(actor);
        const user = await this.findManagedUser(actor, userId);
        if (user.status !== client_1.UserStatus.PENDING_APPROVAL) {
            throw new common_1.BadRequestException('User is not awaiting approval');
        }
        let loginPassword;
        let emailSent = false;
        let emailError = null;
        if (user.role === client_1.UserRole.DEALER || user.role === client_1.UserRole.SHOPKEEPER) {
            const pending = await this.prisma.user.findUnique({
                where: { id: userId },
                select: { approvalLoginPasswordEnc: true },
            });
            loginPassword =
                (0, credential_vault_1.decryptApprovalPassword)(pending?.approvalLoginPasswordEnc) ??
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
                status: client_1.UserStatus.ACTIVE,
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
    async uploadOnboardingDocument(actor, userId, label, file, documentType) {
        const trimmedLabel = label?.trim();
        if (!trimmedLabel) {
            throw new common_1.BadRequestException('Document label is required');
        }
        if (!file?.buffer?.length) {
            throw new common_1.BadRequestException('File is required');
        }
        if (file.size > MAX_ONBOARDING_FILE_BYTES) {
            throw new common_1.BadRequestException('File exceeds 10 MB limit');
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
            throw new common_1.NotFoundException('User not found');
        }
        if (user.role !== client_1.UserRole.DEALER && user.role !== client_1.UserRole.SHOPKEEPER) {
            throw new common_1.BadRequestException('Documents can only be attached to dealers or shopkeepers');
        }
        if (actor.companyId && user.companyId !== actor.companyId) {
            throw new common_1.ForbiddenException('User is outside your company');
        }
        const isAdmin = actor.role === client_1.UserRole.ADMIN;
        const isSelf = actor.userId === userId;
        const isOnboarder = actor.role === client_1.UserRole.EMPLOYEE && user.onboardedById === actor.userId;
        if (!isAdmin && !isOnboarder && !isSelf) {
            throw new common_1.ForbiddenException('You cannot upload documents for this user');
        }
        if (user.status !== client_1.UserStatus.PENDING_APPROVAL &&
            !isAdmin &&
            !isSelf) {
            throw new common_1.BadRequestException('Documents can only be added while awaiting approval');
        }
        const resolvedType = documentType ??
            (0, document_eligibility_util_1.resolveDocumentTypeFromLabel)(trimmedLabel) ??
            null;
        (0, fs_1.mkdirSync)(ONBOARDING_UPLOAD_DIR, { recursive: true });
        const storageKey = `${userId}/${(0, crypto_1.randomBytes)(16).toString('hex')}-${file.originalname.replace(/[^\w.\-]+/g, '_')}`;
        const absolutePath = (0, path_1.join)(ONBOARDING_UPLOAD_DIR, storageKey);
        (0, fs_1.mkdirSync)((0, path_1.join)(ONBOARDING_UPLOAD_DIR, userId), { recursive: true });
        (0, fs_1.writeFileSync)(absolutePath, file.buffer);
        const created = await this.prisma.onboardingDocument.create({
            data: {
                userId,
                label: trimmedLabel,
                documentType: resolvedType,
                fileName: file.originalname || 'document',
                mimeType: file.mimetype || null,
                storageKey,
                fileSize: file.size,
                verificationStatus: client_1.DocumentVerificationStatus.PENDING_VERIFICATION,
            },
            select: {
                id: true,
                label: true,
                documentType: true,
                fileName: true,
                mimeType: true,
                fileSize: true,
                uploadedAt: true,
                verificationStatus: true,
            },
        });
        await this.syncUserDocumentEligibility(userId);
        if (isSelf) {
            void this.notifyEmployeesOfDocumentUpload(userId);
            void this.notifications.sendToUser(userId, 'Document uploaded', 'Your document was uploaded successfully and is pending verification.', { type: 'DOCUMENT', status: 'UPLOADED' });
        }
        return created;
    }
    async uploadMyDocument(actor, documentType, file) {
        if (actor.role !== client_1.UserRole.DEALER && actor.role !== client_1.UserRole.SHOPKEEPER) {
            throw new common_1.ForbiddenException('Only dealers and shopkeepers can upload documents');
        }
        const label = document_eligibility_util_1.ACCEPTED_DOCUMENT_LABELS[documentType] ?? documentType;
        return this.uploadOnboardingDocument(actor, actor.userId, label, file, documentType);
    }
    listMyDocuments(actor) {
        return this.prisma.onboardingDocument.findMany({
            where: { userId: actor.userId },
            select: {
                id: true,
                label: true,
                documentType: true,
                fileName: true,
                mimeType: true,
                fileSize: true,
                uploadedAt: true,
                verificationStatus: true,
                verifiedAt: true,
                rejectionReason: true,
                verifiedBy: { select: { id: true, name: true } },
            },
            orderBy: { uploadedAt: 'desc' },
        });
    }
    async syncUserDocumentEligibility(userId) {
        const user = await this.prisma.user.findUnique({
            where: { id: userId },
            select: {
                status: true,
                onboardingDocuments: {
                    select: { verificationStatus: true },
                },
            },
        });
        if (!user)
            return;
        const flags = (0, document_eligibility_util_1.computeDocumentEligibility)({
            status: user.status,
            documents: user.onboardingDocuments,
        });
        await this.prisma.user.update({
            where: { id: userId },
            data: flags,
        });
    }
    async assertCanPlaceOrders(userId) {
        const user = await this.prisma.user.findUnique({
            where: { id: userId },
            select: {
                status: true,
                canPlaceOrders: true,
                documentStatus: true,
                documentUploaded: true,
            },
        });
        if (!user) {
            throw new common_1.NotFoundException('User not found');
        }
        if (user.status !== client_1.UserStatus.ACTIVE) {
            throw new common_1.ForbiddenException('Your account is not active');
        }
        if (!user.canPlaceOrders) {
            throw new common_1.ForbiddenException({
                message: document_eligibility_util_1.ORDER_DOCUMENT_BLOCK_MESSAGE,
                code: 'DOCUMENT_REQUIRED',
                documentStatus: user.documentStatus,
            });
        }
    }
    async resolveSelfRegistrationCompanyId() {
        const fromEnv = process.env.MART_COMPANY_ID?.trim();
        if (fromEnv)
            return fromEnv;
        const company = await this.prisma.company.findFirst({
            orderBy: { createdAt: 'asc' },
            select: { id: true },
        });
        if (!company) {
            throw new common_1.BadRequestException('Registration is not configured');
        }
        return company.id;
    }
    async resolveOnboardedByForArea(areaId, referralCode) {
        if (referralCode?.trim()) {
            const employee = await this.prisma.user.findFirst({
                where: {
                    referralCode: referralCode.trim().toUpperCase(),
                    role: client_1.UserRole.EMPLOYEE,
                    status: client_1.UserStatus.ACTIVE,
                },
                select: { id: true },
            });
            if (employee)
                return employee.id;
        }
        const area = await this.prisma.area.findUnique({
            where: { id: areaId },
            select: { employeeId: true },
        });
        return area?.employeeId ?? null;
    }
    async selfRegisterShopkeeper(dto, verifiedPhone) {
        const companyId = await this.resolveSelfRegistrationCompanyId();
        const creds = this.resolveSelfRegisterCredentials(dto, verifiedPhone);
        const existing = await this.prisma.user.findUnique({ where: { email: creds.email } });
        if (existing) {
            throw new common_1.ConflictException('A user with this email already exists');
        }
        if (creds.phone) {
            await this.assertPhoneAvailable(creds.phone);
        }
        const area = await this.prisma.area.findFirst({
            where: { id: dto.areaId, companyId },
        });
        if (!area) {
            throw new common_1.NotFoundException('Area not found');
        }
        const onboardedById = await this.resolveOnboardedByForArea(dto.areaId, dto.referralCode);
        const password = await bcrypt.hash(creds.plainPassword, 10);
        let created;
        try {
            created = await this.prisma.user.create({
                data: {
                    name: dto.name.trim(),
                    email: creds.email,
                    phone: creds.phone,
                    password,
                    role: client_1.UserRole.SHOPKEEPER,
                    companyId,
                    areaId: dto.areaId,
                    onboardedById,
                    shopName: dto.shopName.trim(),
                    address: dto.address.trim(),
                    state: dto.state.trim(),
                    district: dto.district.trim(),
                    latitude: dto.latitude ?? null,
                    longitude: dto.longitude ?? null,
                    status: client_1.UserStatus.ACTIVE,
                    approvedAt: new Date(),
                    documentUploaded: false,
                    canPlaceOrders: false,
                    documentStatus: 'NOT_UPLOADED',
                },
                select: this.userRosterSelect,
            });
        }
        catch (error) {
            this.rethrowUniqueConstraint(error);
        }
        if (onboardedById) {
            void this.notifications.sendToUser(onboardedById, 'New shopkeeper registered', `${created.name} registered in ${area.name}`, { type: 'ONBOARDING', userId: created.id });
        }
        return {
            ...created,
            message: 'Registration complete. You can browse products after signing in.',
        };
    }
    async selfRegisterDealer(dto, verifiedPhone) {
        const companyId = await this.resolveSelfRegistrationCompanyId();
        const creds = this.resolveSelfRegisterCredentials(dto, verifiedPhone);
        const existing = await this.prisma.user.findUnique({ where: { email: creds.email } });
        if (existing) {
            throw new common_1.ConflictException('A user with this email already exists');
        }
        if (creds.phone) {
            await this.assertPhoneAvailable(creds.phone);
        }
        const area = await this.prisma.area.findFirst({
            where: { id: dto.areaId, companyId },
        });
        if (!area) {
            throw new common_1.NotFoundException('Area not found');
        }
        const onboardedById = await this.resolveOnboardedByForArea(dto.areaId, dto.referralCode);
        const password = await bcrypt.hash(creds.plainPassword, 10);
        let dealer;
        try {
            dealer = await this.prisma.$transaction(async (tx) => {
                const created = await tx.user.create({
                    data: {
                        name: dto.name.trim(),
                        email: creds.email,
                        phone: creds.phone,
                        password,
                        role: client_1.UserRole.DEALER,
                        companyId,
                        onboardedById,
                        shopName: dto.shopName.trim(),
                        address: dto.address.trim(),
                        state: dto.state.trim(),
                        district: dto.district.trim(),
                        latitude: dto.latitude ?? null,
                        longitude: dto.longitude ?? null,
                        status: client_1.UserStatus.ACTIVE,
                        approvedAt: new Date(),
                        documentUploaded: false,
                        canPlaceOrders: false,
                        documentStatus: 'NOT_UPLOADED',
                    },
                    select: this.userRosterSelect,
                });
                await tx.area.update({
                    where: { id: dto.areaId },
                    data: { dealerId: created.id },
                });
                return created;
            });
        }
        catch (error) {
            this.rethrowUniqueConstraint(error);
        }
        if (onboardedById) {
            void this.notifications.sendToUser(onboardedById, 'New dealer registered', `${dealer.name} registered in ${area.name}`, { type: 'ONBOARDING', userId: dealer.id });
        }
        return {
            ...dealer,
            message: 'Registration complete. You can sign in and manage your business.',
        };
    }
    resolveSelfRegisterCredentials(dto, verifiedPhone) {
        if (verifiedPhone) {
            const email = dto.email?.trim().toLowerCase() ||
                `${verifiedPhone}@phone.flashmart.app`;
            const plainPassword = dto.password?.trim() || (0, crypto_1.randomBytes)(18).toString('base64url');
            return { email, plainPassword, phone: verifiedPhone };
        }
        return {
            email: dto.email.trim().toLowerCase(),
            plainPassword: dto.password.trim(),
            phone: dto.phone?.trim() || null,
        };
    }
    async notifyEmployeesOfDocumentUpload(userId) {
        const user = await this.prisma.user.findUnique({
            where: { id: userId },
            select: {
                name: true,
                companyId: true,
                onboardedById: true,
                area: { select: { employeeId: true, name: true } },
            },
        });
        if (!user?.companyId)
            return;
        const targetIds = new Set();
        if (user.onboardedById)
            targetIds.add(user.onboardedById);
        if (user.area?.employeeId)
            targetIds.add(user.area.employeeId);
        if (targetIds.size === 0) {
            const employees = await this.prisma.user.findMany({
                where: {
                    companyId: user.companyId,
                    role: client_1.UserRole.EMPLOYEE,
                    status: client_1.UserStatus.ACTIVE,
                },
                select: { id: true },
            });
            employees.forEach((e) => targetIds.add(e.id));
        }
        await this.notifications.sendToUsers([...targetIds], 'Document pending verification', `${user.name} uploaded a document for review`, { type: 'DOCUMENT', userId, status: 'PENDING_VERIFICATION' });
    }
    async verifyOnboardingDocument(actor, userId, documentId) {
        if (actor.role !== client_1.UserRole.ADMIN && actor.role !== client_1.UserRole.EMPLOYEE) {
            throw new common_1.ForbiddenException('Only staff can verify documents');
        }
        const doc = await this.prisma.onboardingDocument.findFirst({
            where: { id: documentId, userId },
            include: { user: { select: { companyId: true, name: true } } },
        });
        if (!doc)
            throw new common_1.NotFoundException('Document not found');
        if (actor.companyId && doc.user.companyId !== actor.companyId) {
            throw new common_1.ForbiddenException('Document is outside your company');
        }
        const updated = await this.prisma.onboardingDocument.update({
            where: { id: documentId },
            data: {
                verificationStatus: client_1.DocumentVerificationStatus.VERIFIED,
                verifiedById: actor.userId,
                verifiedAt: new Date(),
                rejectionReason: null,
            },
        });
        await this.syncUserDocumentEligibility(userId);
        void this.notifications.sendToUser(userId, 'Document verified', 'Your business document has been verified.', { type: 'DOCUMENT', status: 'VERIFIED', documentId });
        return updated;
    }
    async rejectOnboardingDocument(actor, userId, documentId, reason) {
        if (actor.role !== client_1.UserRole.ADMIN && actor.role !== client_1.UserRole.EMPLOYEE) {
            throw new common_1.ForbiddenException('Only staff can reject documents');
        }
        const doc = await this.prisma.onboardingDocument.findFirst({
            where: { id: documentId, userId },
            include: { user: { select: { companyId: true } } },
        });
        if (!doc)
            throw new common_1.NotFoundException('Document not found');
        if (actor.companyId && doc.user.companyId !== actor.companyId) {
            throw new common_1.ForbiddenException('Document is outside your company');
        }
        const updated = await this.prisma.onboardingDocument.update({
            where: { id: documentId },
            data: {
                verificationStatus: client_1.DocumentVerificationStatus.REJECTED,
                verifiedById: actor.userId,
                verifiedAt: new Date(),
                rejectionReason: reason.trim() || 'Rejected by reviewer',
            },
        });
        await this.syncUserDocumentEligibility(userId);
        void this.notifications.sendToUser(userId, 'Document rejected', 'Your uploaded document was rejected. Please upload a valid document.', { type: 'DOCUMENT', status: 'REJECTED', documentId });
        return updated;
    }
    async recordFollowUp(actor, userId) {
        if (actor.role !== client_1.UserRole.ADMIN && actor.role !== client_1.UserRole.EMPLOYEE) {
            throw new common_1.ForbiddenException('Only staff can record follow-ups');
        }
        const user = await this.findManagedUser(actor, userId);
        return this.prisma.user.update({
            where: { id: user.id },
            data: { lastFollowUpAt: new Date() },
            select: this.userRosterSelect,
        });
    }
    async streamMyOnboardingDocument(actor, documentId, res) {
        const doc = await this.prisma.onboardingDocument.findFirst({
            where: { id: documentId, userId: actor.userId },
        });
        if (!doc)
            throw new common_1.NotFoundException('Document not found');
        const absolutePath = (0, path_1.join)(ONBOARDING_UPLOAD_DIR, doc.storageKey);
        if (!(0, fs_1.existsSync)(absolutePath)) {
            throw new common_1.NotFoundException('File not found on server');
        }
        res.setHeader('Content-Type', doc.mimeType || 'application/octet-stream');
        res.setHeader('Content-Disposition', `inline; filename="${doc.fileName.replace(/"/g, '')}"`);
        (0, fs_1.createReadStream)(absolutePath).pipe(res);
    }
    async streamOnboardingDocument(actor, userId, documentId, res) {
        if (actor.role !== client_1.UserRole.ADMIN && actor.role !== client_1.UserRole.EMPLOYEE) {
            throw new common_1.ForbiddenException('Access denied');
        }
        const doc = await this.prisma.onboardingDocument.findFirst({
            where: { id: documentId, userId },
            include: {
                user: { select: { companyId: true, role: true } },
            },
        });
        if (!doc) {
            throw new common_1.NotFoundException('Document not found');
        }
        if (actor.companyId && doc.user.companyId !== actor.companyId) {
            throw new common_1.ForbiddenException('Document is outside your company');
        }
        const absolutePath = (0, path_1.join)(ONBOARDING_UPLOAD_DIR, doc.storageKey);
        if (!(0, fs_1.existsSync)(absolutePath)) {
            throw new common_1.NotFoundException('File not found on server');
        }
        res.setHeader('Content-Type', doc.mimeType || 'application/octet-stream');
        res.setHeader('Content-Disposition', `inline; filename="${doc.fileName.replace(/"/g, '')}"`);
        (0, fs_1.createReadStream)(absolutePath).pipe(res);
    }
    async rejectUser(actor, userId, reason) {
        this.assertAdmin(actor);
        const user = await this.findManagedUser(actor, userId);
        if (user.status !== client_1.UserStatus.PENDING_APPROVAL) {
            throw new common_1.BadRequestException('User is not awaiting approval');
        }
        return this.prisma.user.update({
            where: { id: userId },
            data: {
                status: client_1.UserStatus.REJECTED,
                statusReason: reason?.trim() || 'Rejected by admin',
                passwordResetToken: null,
                passwordResetExpires: null,
            },
            select: this.userRosterSelect,
        });
    }
    async deactivateUser(actor, userId, reason) {
        this.assertAdmin(actor);
        const user = await this.findManagedUser(actor, userId);
        if (user.status === client_1.UserStatus.DEACTIVATED) {
            throw new common_1.BadRequestException('User is already deactivated');
        }
        return this.prisma.user.update({
            where: { id: userId },
            data: {
                status: client_1.UserStatus.DEACTIVATED,
                statusReason: reason?.trim() || 'Deactivated by admin',
                passwordResetToken: null,
                passwordResetExpires: null,
            },
            select: this.userRosterSelect,
        });
    }
    async reactivateUser(actor, userId) {
        this.assertAdmin(actor);
        const user = await this.findManagedUser(actor, userId);
        if (user.status !== client_1.UserStatus.DEACTIVATED) {
            throw new common_1.BadRequestException('Only deactivated users can be reactivated');
        }
        return this.prisma.user.update({
            where: { id: userId },
            data: {
                status: client_1.UserStatus.ACTIVE,
                statusReason: null,
                approvedById: actor.userId,
                approvedAt: new Date(),
            },
            select: this.userRosterSelect,
        });
    }
    async createEmployee(actor, dto) {
        this.assertAdmin(actor);
        if (!actor.companyId) {
            throw new common_1.BadRequestException('Company scope is required');
        }
        const email = dto.email.trim().toLowerCase();
        const existing = await this.prisma.user.findUnique({ where: { email } });
        if (existing) {
            throw new common_1.ConflictException('A user with this email already exists');
        }
        const phone = this.normalizePhone(dto.phone);
        await this.assertPhoneAvailable(phone);
        const plain = dto.password?.trim() && dto.password.length >= 8
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
                    role: client_1.UserRole.EMPLOYEE,
                    companyId: actor.companyId,
                    status: client_1.UserStatus.ACTIVE,
                    approvedById: actor.userId,
                    approvedAt: new Date(),
                },
                select: this.userRosterSelect,
            });
        }
        catch (error) {
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
    async createShopkeeper(actor, dto) {
        if (!actor.companyId) {
            throw new common_1.BadRequestException('Company scope is required to onboard shopkeepers');
        }
        const email = dto.email.trim().toLowerCase();
        const existing = await this.prisma.user.findUnique({
            where: { email },
        });
        if (existing) {
            throw new common_1.ConflictException('A user with this email already exists');
        }
        const phone = this.normalizePhone(dto.phone);
        await this.assertPhoneAvailable(phone);
        const area = await this.prisma.area.findFirst({
            where: { id: dto.areaId, companyId: actor.companyId },
        });
        if (!area) {
            throw new common_1.NotFoundException('Area not found or not in your company');
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
                    role: client_1.UserRole.SHOPKEEPER,
                    companyId: actor.companyId,
                    areaId: dto.areaId,
                    onboardedById: actor.userId,
                    onboardingNotes: dto.onboardingNotes?.trim() || null,
                    shopName: dto.shopName?.trim() || null,
                    address: dto.address?.trim() || null,
                    latitude: dto.latitude ?? null,
                    longitude: dto.longitude ?? null,
                    approvalLoginPasswordEnc: credentialEnc,
                    status,
                    approvedById: status === client_1.UserStatus.ACTIVE ? actor.userId : null,
                    approvedAt: status === client_1.UserStatus.ACTIVE ? new Date() : null,
                },
                select: this.userRosterSelect,
            });
        }
        catch (error) {
            this.rethrowUniqueConstraint(error);
        }
        let emailSent = false;
        let emailError = null;
        if (status === client_1.UserStatus.ACTIVE) {
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
        if (status === client_1.UserStatus.PENDING_APPROVAL) {
            void this.notifyAdminsOfPendingApproval(actor.companyId, created.name, 'Shopkeeper', created.id);
        }
        const pendingMessage = 'Shopkeeper submitted for admin approval. Login details will be emailed after approval.';
        const activeMessage = emailSent
            ? `Shopkeeper created. Login credentials emailed to ${email}.`
            : 'Shopkeeper created. Share the login credentials below (email could not be sent).';
        return {
            ...created,
            loginEmail: email,
            loginPassword: status === client_1.UserStatus.ACTIVE && !emailSent ? plain : undefined,
            message: status === client_1.UserStatus.PENDING_APPROVAL ? pendingMessage : activeMessage,
            emailSent: status === client_1.UserStatus.ACTIVE ? emailSent : undefined,
            emailError: status === client_1.UserStatus.ACTIVE ? emailError : undefined,
        };
    }
    async createDealer(actor, dto) {
        if (!actor.companyId) {
            throw new common_1.BadRequestException('Company scope is required to onboard dealers');
        }
        const email = dto.email.trim().toLowerCase();
        const existing = await this.prisma.user.findUnique({
            where: { email },
        });
        if (existing) {
            throw new common_1.ConflictException('A user with this email already exists');
        }
        const phone = this.normalizePhone(dto.phone);
        await this.assertPhoneAvailable(phone);
        const area = await this.prisma.area.findFirst({
            where: { id: dto.areaId, companyId: actor.companyId },
        });
        if (!area) {
            throw new common_1.NotFoundException('Area not found or not in your company');
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
                        role: client_1.UserRole.DEALER,
                        companyId: actor.companyId,
                        onboardedById: actor.userId,
                        onboardingNotes: dto.onboardingNotes?.trim() || null,
                        shopName: dto.shopName?.trim() || null,
                        address: dto.address?.trim() || null,
                        latitude: dto.latitude ?? null,
                        longitude: dto.longitude ?? null,
                        approvalLoginPasswordEnc: credentialEnc,
                        status,
                        approvedById: status === client_1.UserStatus.ACTIVE ? actor.userId : null,
                        approvedAt: status === client_1.UserStatus.ACTIVE ? new Date() : null,
                    },
                    select: this.userRosterSelect,
                });
                await tx.area.update({
                    where: { id: dto.areaId },
                    data: { dealerId: created.id },
                });
                return created;
            });
        }
        catch (error) {
            this.rethrowUniqueConstraint(error);
        }
        let emailSent = false;
        let emailError = null;
        if (status === client_1.UserStatus.ACTIVE) {
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
        if (status === client_1.UserStatus.PENDING_APPROVAL) {
            void this.notifyAdminsOfPendingApproval(actor.companyId, dealer.name, 'Dealer', dealer.id);
        }
        const pendingMessage = 'Dealer submitted for admin approval. Login details will be emailed after approval.';
        const activeMessage = emailSent
            ? `Dealer created. Login credentials emailed to ${email}.`
            : 'Dealer created. Share the login credentials below (email could not be sent).';
        return {
            ...dealer,
            loginEmail: email,
            userId: dealer.id,
            loginPassword: status === client_1.UserStatus.ACTIVE && !emailSent ? plain : undefined,
            message: status === client_1.UserStatus.PENDING_APPROVAL ? pendingMessage : activeMessage,
            emailSent: status === client_1.UserStatus.ACTIVE ? emailSent : undefined,
            emailError: status === client_1.UserStatus.ACTIVE ? emailError : undefined,
        };
    }
};
exports.UsersService = UsersService;
exports.UsersService = UsersService = __decorate([
    (0, common_1.Injectable)(),
    __metadata("design:paramtypes", [prisma_service_1.PrismaService,
        email_service_1.EmailService,
        notifications_service_1.NotificationsService])
], UsersService);
//# sourceMappingURL=users.service.js.map