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
var NotificationsService_1;
Object.defineProperty(exports, "__esModule", { value: true });
exports.NotificationsService = void 0;
const common_1 = require("@nestjs/common");
const config_1 = require("@nestjs/config");
const prisma_service_1 = require("../prisma/prisma.service");
let NotificationsService = NotificationsService_1 = class NotificationsService {
    config;
    prisma;
    logger = new common_1.Logger(NotificationsService_1.name);
    constructor(config, prisma) {
        this.config = config;
        this.prisma = prisma;
    }
    async sendToUser(userId, title, body) {
        const key = this.config.get('fcm.serverKey', '');
        if (!key) {
            this.logger.debug('FCM_SERVER_KEY not set; skip push');
            return;
        }
        const user = await this.prisma.user.findUnique({ where: { id: userId } });
        if (!user?.fcmToken) {
            return;
        }
        try {
            const res = await fetch('https://fcm.googleapis.com/fcm/send', {
                method: 'POST',
                headers: {
                    Authorization: `key=${key}`,
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    to: user.fcmToken,
                    notification: { title, body },
                    data: { userId, title, body },
                }),
            });
            if (!res.ok) {
                const text = await res.text();
                this.logger.warn(`FCM send failed: ${res.status} ${text}`);
            }
        }
        catch (err) {
            this.logger.warn(`FCM send error: ${err.message}`);
        }
    }
};
exports.NotificationsService = NotificationsService;
exports.NotificationsService = NotificationsService = NotificationsService_1 = __decorate([
    (0, common_1.Injectable)(),
    __metadata("design:paramtypes", [config_1.ConfigService,
        prisma_service_1.PrismaService])
], NotificationsService);
//# sourceMappingURL=notifications.service.js.map