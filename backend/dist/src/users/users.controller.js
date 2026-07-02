"use strict";
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
var __param = (this && this.__param) || function (paramIndex, decorator) {
    return function (target, key) { decorator(target, key, paramIndex); }
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.UsersController = void 0;
const common_1 = require("@nestjs/common");
const platform_express_1 = require("@nestjs/platform-express");
const client_1 = require("@prisma/client");
const multer_1 = require("multer");
const current_user_decorator_1 = require("../auth/decorators/current-user.decorator");
const jwt_auth_guard_1 = require("../auth/guards/jwt-auth.guard");
const roles_decorator_1 = require("../roles/decorators/roles.decorator");
const roles_guard_1 = require("../roles/guards/roles.guard");
const create_dealer_dto_1 = require("./dto/create-dealer.dto");
const create_employee_dto_1 = require("./dto/create-employee.dto");
const create_shopkeeper_dto_1 = require("./dto/create-shopkeeper.dto");
const list_users_query_dto_1 = require("./dto/list-users-query.dto");
const register_fcm_token_dto_1 = require("./dto/register-fcm-token.dto");
const update_user_status_dto_1 = require("./dto/update-user-status.dto");
const document_dto_1 = require("./dto/document.dto");
const document_dto_2 = require("./dto/document.dto");
const users_service_1 = require("./users.service");
let UsersController = class UsersController {
    usersService;
    constructor(usersService) {
        this.usersService = usersService;
    }
    registerFcmToken(user, dto) {
        return this.usersService.registerFcmToken(user, dto.token);
    }
    clearFcmToken(user) {
        return this.usersService.clearFcmToken(user);
    }
    findAll(user, query) {
        return this.usersService.findAll(user, query);
    }
    async pendingCount(user) {
        const count = await this.usersService.countPendingApprovals(user);
        return { count };
    }
    createEmployee(user, dto) {
        return this.usersService.createEmployee(user, dto);
    }
    createShopkeeper(user, dto) {
        return this.usersService.createShopkeeper(user, dto);
    }
    createDealer(user, dto) {
        return this.usersService.createDealer(user, dto);
    }
    uploadOnboardingDocument(user, id, label, file, documentType) {
        return this.usersService.uploadOnboardingDocument(user, id, label, file, documentType);
    }
    listMyDocuments(user) {
        return this.usersService.listMyDocuments(user);
    }
    uploadMyDocument(user, dto, file) {
        return this.usersService.uploadMyDocument(user, dto.documentType, file);
    }
    async downloadMyDocument(user, documentId, res) {
        await this.usersService.streamMyOnboardingDocument(user, documentId, res);
    }
    verifyDocument(user, userId, documentId) {
        return this.usersService.verifyOnboardingDocument(user, userId, documentId);
    }
    rejectDocument(user, userId, documentId, dto) {
        return this.usersService.rejectOnboardingDocument(user, userId, documentId, dto.reason);
    }
    recordFollowUp(user, id) {
        return this.usersService.recordFollowUp(user, id);
    }
    async downloadOnboardingDocument(user, userId, documentId, res) {
        await this.usersService.streamOnboardingDocument(user, userId, documentId, res);
    }
    approve(user, id) {
        return this.usersService.approveUser(user, id);
    }
    reject(user, id, dto) {
        return this.usersService.rejectUser(user, id, dto.reason);
    }
    deactivate(user, id, dto) {
        return this.usersService.deactivateUser(user, id, dto.reason);
    }
    reactivate(user, id) {
        return this.usersService.reactivateUser(user, id);
    }
};
exports.UsersController = UsersController;
__decorate([
    (0, common_1.Post)('me/fcm-token'),
    (0, common_1.UseGuards)(jwt_auth_guard_1.JwtAuthGuard),
    __param(0, (0, current_user_decorator_1.CurrentUser)()),
    __param(1, (0, common_1.Body)()),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [Object, register_fcm_token_dto_1.RegisterFcmTokenDto]),
    __metadata("design:returntype", void 0)
], UsersController.prototype, "registerFcmToken", null);
__decorate([
    (0, common_1.Delete)('me/fcm-token'),
    (0, common_1.UseGuards)(jwt_auth_guard_1.JwtAuthGuard),
    __param(0, (0, current_user_decorator_1.CurrentUser)()),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [Object]),
    __metadata("design:returntype", void 0)
], UsersController.prototype, "clearFcmToken", null);
__decorate([
    (0, common_1.Get)(),
    (0, common_1.UseGuards)(jwt_auth_guard_1.JwtAuthGuard, roles_guard_1.RolesGuard),
    (0, roles_decorator_1.Roles)(client_1.UserRole.ADMIN, client_1.UserRole.EMPLOYEE),
    __param(0, (0, current_user_decorator_1.CurrentUser)()),
    __param(1, (0, common_1.Query)()),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [Object, list_users_query_dto_1.ListUsersQueryDto]),
    __metadata("design:returntype", void 0)
], UsersController.prototype, "findAll", null);
__decorate([
    (0, common_1.Get)('pending-count'),
    (0, common_1.UseGuards)(jwt_auth_guard_1.JwtAuthGuard, roles_guard_1.RolesGuard),
    (0, roles_decorator_1.Roles)(client_1.UserRole.ADMIN),
    __param(0, (0, current_user_decorator_1.CurrentUser)()),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [Object]),
    __metadata("design:returntype", Promise)
], UsersController.prototype, "pendingCount", null);
__decorate([
    (0, common_1.Post)('employees'),
    (0, common_1.UseGuards)(jwt_auth_guard_1.JwtAuthGuard, roles_guard_1.RolesGuard),
    (0, roles_decorator_1.Roles)(client_1.UserRole.ADMIN),
    __param(0, (0, current_user_decorator_1.CurrentUser)()),
    __param(1, (0, common_1.Body)()),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [Object, create_employee_dto_1.CreateEmployeeDto]),
    __metadata("design:returntype", void 0)
], UsersController.prototype, "createEmployee", null);
__decorate([
    (0, common_1.Post)('shopkeepers'),
    (0, common_1.UseGuards)(jwt_auth_guard_1.JwtAuthGuard, roles_guard_1.RolesGuard),
    (0, roles_decorator_1.Roles)(client_1.UserRole.ADMIN, client_1.UserRole.EMPLOYEE),
    __param(0, (0, current_user_decorator_1.CurrentUser)()),
    __param(1, (0, common_1.Body)()),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [Object, create_shopkeeper_dto_1.CreateShopkeeperDto]),
    __metadata("design:returntype", void 0)
], UsersController.prototype, "createShopkeeper", null);
__decorate([
    (0, common_1.Post)('dealers'),
    (0, common_1.UseGuards)(jwt_auth_guard_1.JwtAuthGuard, roles_guard_1.RolesGuard),
    (0, roles_decorator_1.Roles)(client_1.UserRole.ADMIN, client_1.UserRole.EMPLOYEE),
    __param(0, (0, current_user_decorator_1.CurrentUser)()),
    __param(1, (0, common_1.Body)()),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [Object, create_dealer_dto_1.CreateDealerDto]),
    __metadata("design:returntype", void 0)
], UsersController.prototype, "createDealer", null);
__decorate([
    (0, common_1.Post)(':id/onboarding-documents'),
    (0, common_1.UseGuards)(jwt_auth_guard_1.JwtAuthGuard, roles_guard_1.RolesGuard),
    (0, roles_decorator_1.Roles)(client_1.UserRole.ADMIN, client_1.UserRole.EMPLOYEE, client_1.UserRole.DEALER, client_1.UserRole.SHOPKEEPER),
    (0, common_1.UseInterceptors)((0, platform_express_1.FileInterceptor)('file', {
        storage: (0, multer_1.memoryStorage)(),
        limits: { fileSize: 10 * 1024 * 1024 },
    })),
    __param(0, (0, current_user_decorator_1.CurrentUser)()),
    __param(1, (0, common_1.Param)('id')),
    __param(2, (0, common_1.Body)('label')),
    __param(3, (0, common_1.UploadedFile)()),
    __param(4, (0, common_1.Body)('documentType')),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [Object, String, String, Object, String]),
    __metadata("design:returntype", void 0)
], UsersController.prototype, "uploadOnboardingDocument", null);
__decorate([
    (0, common_1.Get)('me/documents'),
    (0, common_1.UseGuards)(jwt_auth_guard_1.JwtAuthGuard, roles_guard_1.RolesGuard),
    (0, roles_decorator_1.Roles)(client_1.UserRole.DEALER, client_1.UserRole.SHOPKEEPER),
    __param(0, (0, current_user_decorator_1.CurrentUser)()),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [Object]),
    __metadata("design:returntype", void 0)
], UsersController.prototype, "listMyDocuments", null);
__decorate([
    (0, common_1.Post)('me/documents'),
    (0, common_1.UseGuards)(jwt_auth_guard_1.JwtAuthGuard, roles_guard_1.RolesGuard),
    (0, roles_decorator_1.Roles)(client_1.UserRole.DEALER, client_1.UserRole.SHOPKEEPER),
    (0, common_1.UseInterceptors)((0, platform_express_1.FileInterceptor)('file', {
        storage: (0, multer_1.memoryStorage)(),
        limits: { fileSize: 10 * 1024 * 1024 },
    })),
    __param(0, (0, current_user_decorator_1.CurrentUser)()),
    __param(1, (0, common_1.Body)()),
    __param(2, (0, common_1.UploadedFile)()),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [Object, document_dto_1.UploadMyDocumentDto, Object]),
    __metadata("design:returntype", void 0)
], UsersController.prototype, "uploadMyDocument", null);
__decorate([
    (0, common_1.Get)('me/documents/:documentId/file'),
    (0, common_1.UseGuards)(jwt_auth_guard_1.JwtAuthGuard, roles_guard_1.RolesGuard),
    (0, roles_decorator_1.Roles)(client_1.UserRole.DEALER, client_1.UserRole.SHOPKEEPER),
    __param(0, (0, current_user_decorator_1.CurrentUser)()),
    __param(1, (0, common_1.Param)('documentId')),
    __param(2, (0, common_1.Res)()),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [Object, String, Object]),
    __metadata("design:returntype", Promise)
], UsersController.prototype, "downloadMyDocument", null);
__decorate([
    (0, common_1.Patch)(':userId/documents/:documentId/verify'),
    (0, common_1.UseGuards)(jwt_auth_guard_1.JwtAuthGuard, roles_guard_1.RolesGuard),
    (0, roles_decorator_1.Roles)(client_1.UserRole.ADMIN, client_1.UserRole.EMPLOYEE),
    __param(0, (0, current_user_decorator_1.CurrentUser)()),
    __param(1, (0, common_1.Param)('userId')),
    __param(2, (0, common_1.Param)('documentId')),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [Object, String, String]),
    __metadata("design:returntype", void 0)
], UsersController.prototype, "verifyDocument", null);
__decorate([
    (0, common_1.Patch)(':userId/documents/:documentId/reject'),
    (0, common_1.UseGuards)(jwt_auth_guard_1.JwtAuthGuard, roles_guard_1.RolesGuard),
    (0, roles_decorator_1.Roles)(client_1.UserRole.ADMIN, client_1.UserRole.EMPLOYEE),
    __param(0, (0, current_user_decorator_1.CurrentUser)()),
    __param(1, (0, common_1.Param)('userId')),
    __param(2, (0, common_1.Param)('documentId')),
    __param(3, (0, common_1.Body)()),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [Object, String, String, document_dto_2.RejectDocumentDto]),
    __metadata("design:returntype", void 0)
], UsersController.prototype, "rejectDocument", null);
__decorate([
    (0, common_1.Patch)(':id/follow-up'),
    (0, common_1.UseGuards)(jwt_auth_guard_1.JwtAuthGuard, roles_guard_1.RolesGuard),
    (0, roles_decorator_1.Roles)(client_1.UserRole.ADMIN, client_1.UserRole.EMPLOYEE),
    __param(0, (0, current_user_decorator_1.CurrentUser)()),
    __param(1, (0, common_1.Param)('id')),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [Object, String]),
    __metadata("design:returntype", void 0)
], UsersController.prototype, "recordFollowUp", null);
__decorate([
    (0, common_1.Get)(':userId/onboarding-documents/:documentId/file'),
    (0, common_1.UseGuards)(jwt_auth_guard_1.JwtAuthGuard, roles_guard_1.RolesGuard),
    (0, roles_decorator_1.Roles)(client_1.UserRole.ADMIN, client_1.UserRole.EMPLOYEE),
    __param(0, (0, current_user_decorator_1.CurrentUser)()),
    __param(1, (0, common_1.Param)('userId')),
    __param(2, (0, common_1.Param)('documentId')),
    __param(3, (0, common_1.Res)()),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [Object, String, String, Object]),
    __metadata("design:returntype", Promise)
], UsersController.prototype, "downloadOnboardingDocument", null);
__decorate([
    (0, common_1.Patch)(':id/approve'),
    (0, common_1.UseGuards)(jwt_auth_guard_1.JwtAuthGuard, roles_guard_1.RolesGuard),
    (0, roles_decorator_1.Roles)(client_1.UserRole.ADMIN),
    __param(0, (0, current_user_decorator_1.CurrentUser)()),
    __param(1, (0, common_1.Param)('id')),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [Object, String]),
    __metadata("design:returntype", void 0)
], UsersController.prototype, "approve", null);
__decorate([
    (0, common_1.Patch)(':id/reject'),
    (0, common_1.UseGuards)(jwt_auth_guard_1.JwtAuthGuard, roles_guard_1.RolesGuard),
    (0, roles_decorator_1.Roles)(client_1.UserRole.ADMIN),
    __param(0, (0, current_user_decorator_1.CurrentUser)()),
    __param(1, (0, common_1.Param)('id')),
    __param(2, (0, common_1.Body)()),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [Object, String, update_user_status_dto_1.UpdateUserStatusDto]),
    __metadata("design:returntype", void 0)
], UsersController.prototype, "reject", null);
__decorate([
    (0, common_1.Patch)(':id/deactivate'),
    (0, common_1.UseGuards)(jwt_auth_guard_1.JwtAuthGuard, roles_guard_1.RolesGuard),
    (0, roles_decorator_1.Roles)(client_1.UserRole.ADMIN),
    __param(0, (0, current_user_decorator_1.CurrentUser)()),
    __param(1, (0, common_1.Param)('id')),
    __param(2, (0, common_1.Body)()),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [Object, String, update_user_status_dto_1.UpdateUserStatusDto]),
    __metadata("design:returntype", void 0)
], UsersController.prototype, "deactivate", null);
__decorate([
    (0, common_1.Patch)(':id/reactivate'),
    (0, common_1.UseGuards)(jwt_auth_guard_1.JwtAuthGuard, roles_guard_1.RolesGuard),
    (0, roles_decorator_1.Roles)(client_1.UserRole.ADMIN),
    __param(0, (0, current_user_decorator_1.CurrentUser)()),
    __param(1, (0, common_1.Param)('id')),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [Object, String]),
    __metadata("design:returntype", void 0)
], UsersController.prototype, "reactivate", null);
exports.UsersController = UsersController = __decorate([
    (0, common_1.Controller)('users'),
    __metadata("design:paramtypes", [users_service_1.UsersService])
], UsersController);
//# sourceMappingURL=users.controller.js.map