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
exports.PaymentsController = void 0;
const common_1 = require("@nestjs/common");
const client_1 = require("@prisma/client");
const current_user_decorator_1 = require("../auth/decorators/current-user.decorator");
const jwt_auth_guard_1 = require("../auth/guards/jwt-auth.guard");
const roles_decorator_1 = require("../roles/decorators/roles.decorator");
const roles_guard_1 = require("../roles/guards/roles.guard");
const create_razorpay_order_dto_1 = require("./dto/create-razorpay-order.dto");
const verify_razorpay_payment_dto_1 = require("./dto/verify-razorpay-payment.dto");
const payments_service_1 = require("./payments.service");
let PaymentsController = class PaymentsController {
    paymentsService;
    constructor(paymentsService) {
        this.paymentsService = paymentsService;
    }
    createRazorpayOrder(dto, user) {
        return this.paymentsService.createRazorpayOrder(dto, user);
    }
    createOrderAlias(dto, user) {
        return this.paymentsService.createRazorpayOrder(dto, user);
    }
    createAliasV2(dto, user) {
        return this.paymentsService.createRazorpayOrder(dto, user);
    }
    verifyRazorpayPayment(dto, user) {
        return this.paymentsService.verifyRazorpayPayment(dto, user);
    }
    verifyAlias(dto, user) {
        return this.paymentsService.verifyRazorpayPayment(dto, user);
    }
    handleRazorpayWebhook(signature, payload) {
        return this.paymentsService.handleRazorpayWebhook(signature, payload);
    }
};
exports.PaymentsController = PaymentsController;
__decorate([
    (0, common_1.Post)('razorpay/order'),
    (0, common_1.UseGuards)(jwt_auth_guard_1.JwtAuthGuard, roles_guard_1.RolesGuard),
    (0, roles_decorator_1.Roles)(client_1.UserRole.SHOPKEEPER, client_1.UserRole.ADMIN, client_1.UserRole.EMPLOYEE, client_1.UserRole.DEALER),
    __param(0, (0, common_1.Body)()),
    __param(1, (0, current_user_decorator_1.CurrentUser)()),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [create_razorpay_order_dto_1.CreateRazorpayOrderDto, Object]),
    __metadata("design:returntype", void 0)
], PaymentsController.prototype, "createRazorpayOrder", null);
__decorate([
    (0, common_1.Post)('create-order'),
    (0, common_1.UseGuards)(jwt_auth_guard_1.JwtAuthGuard, roles_guard_1.RolesGuard),
    (0, roles_decorator_1.Roles)(client_1.UserRole.SHOPKEEPER, client_1.UserRole.ADMIN, client_1.UserRole.EMPLOYEE, client_1.UserRole.DEALER),
    __param(0, (0, common_1.Body)()),
    __param(1, (0, current_user_decorator_1.CurrentUser)()),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [create_razorpay_order_dto_1.CreateRazorpayOrderDto, Object]),
    __metadata("design:returntype", void 0)
], PaymentsController.prototype, "createOrderAlias", null);
__decorate([
    (0, common_1.Post)('create'),
    (0, common_1.UseGuards)(jwt_auth_guard_1.JwtAuthGuard, roles_guard_1.RolesGuard),
    (0, roles_decorator_1.Roles)(client_1.UserRole.SHOPKEEPER, client_1.UserRole.ADMIN, client_1.UserRole.EMPLOYEE, client_1.UserRole.DEALER),
    __param(0, (0, common_1.Body)()),
    __param(1, (0, current_user_decorator_1.CurrentUser)()),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [create_razorpay_order_dto_1.CreateRazorpayOrderDto, Object]),
    __metadata("design:returntype", void 0)
], PaymentsController.prototype, "createAliasV2", null);
__decorate([
    (0, common_1.Post)('razorpay/verify'),
    (0, common_1.UseGuards)(jwt_auth_guard_1.JwtAuthGuard, roles_guard_1.RolesGuard),
    (0, roles_decorator_1.Roles)(client_1.UserRole.SHOPKEEPER, client_1.UserRole.ADMIN, client_1.UserRole.EMPLOYEE, client_1.UserRole.DEALER),
    __param(0, (0, common_1.Body)()),
    __param(1, (0, current_user_decorator_1.CurrentUser)()),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [verify_razorpay_payment_dto_1.VerifyRazorpayPaymentDto, Object]),
    __metadata("design:returntype", void 0)
], PaymentsController.prototype, "verifyRazorpayPayment", null);
__decorate([
    (0, common_1.Post)('verify'),
    (0, common_1.UseGuards)(jwt_auth_guard_1.JwtAuthGuard, roles_guard_1.RolesGuard),
    (0, roles_decorator_1.Roles)(client_1.UserRole.SHOPKEEPER, client_1.UserRole.ADMIN, client_1.UserRole.EMPLOYEE, client_1.UserRole.DEALER),
    __param(0, (0, common_1.Body)()),
    __param(1, (0, current_user_decorator_1.CurrentUser)()),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [verify_razorpay_payment_dto_1.VerifyRazorpayPaymentDto, Object]),
    __metadata("design:returntype", void 0)
], PaymentsController.prototype, "verifyAlias", null);
__decorate([
    (0, common_1.Post)('razorpay/webhook'),
    (0, common_1.HttpCode)(200),
    __param(0, (0, common_1.Headers)('x-razorpay-signature')),
    __param(1, (0, common_1.Body)()),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [Object, Object]),
    __metadata("design:returntype", void 0)
], PaymentsController.prototype, "handleRazorpayWebhook", null);
exports.PaymentsController = PaymentsController = __decorate([
    (0, common_1.Controller)('payments'),
    __metadata("design:paramtypes", [payments_service_1.PaymentsService])
], PaymentsController);
//# sourceMappingURL=payments.controller.js.map