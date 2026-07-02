"use strict";
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.OrdersModule = void 0;
const common_1 = require("@nestjs/common");
const invoices_module_1 = require("../invoices/invoices.module");
const notifications_module_1 = require("../notifications/notifications.module");
const order_items_module_1 = require("../order-items/order-items.module");
const payments_module_1 = require("../payments/payments.module");
const roles_module_1 = require("../roles/roles.module");
const users_module_1 = require("../users/users.module");
const finance_module_1 = require("../finance/finance.module");
const returns_module_1 = require("../returns/returns.module");
const orders_controller_1 = require("./orders.controller");
const orders_service_1 = require("./orders.service");
let OrdersModule = class OrdersModule {
};
exports.OrdersModule = OrdersModule;
exports.OrdersModule = OrdersModule = __decorate([
    (0, common_1.Module)({
        imports: [
            order_items_module_1.OrderItemsModule,
            invoices_module_1.InvoicesModule,
            notifications_module_1.NotificationsModule,
            payments_module_1.PaymentsModule,
            roles_module_1.RolesModule,
            users_module_1.UsersModule,
            finance_module_1.FinanceModule,
            returns_module_1.ReturnsModule,
        ],
        controllers: [orders_controller_1.OrdersController],
        providers: [orders_service_1.OrdersService],
        exports: [orders_service_1.OrdersService],
    })
], OrdersModule);
//# sourceMappingURL=orders.module.js.map