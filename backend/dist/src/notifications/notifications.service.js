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
const METADATA_TOKEN_URL = 'http://metadata.google.internal/computeMetadata/v1/instance/service-accounts/default/token';
let NotificationsService = NotificationsService_1 = class NotificationsService {
    config;
    prisma;
    logger = new common_1.Logger(NotificationsService_1.name);
    projectId;
    tokenCache = null;
    constructor(config, prisma) {
        this.config = config;
        this.prisma = prisma;
        this.projectId = this.config.get('firebase.projectId', '');
        if (!this.projectId) {
            this.logger.warn('FIREBASE_PROJECT_ID not set; push notifications disabled');
        }
    }
    async sendToUser(userId, title, body, data) {
        if (!this.projectId) {
            return;
        }
        const user = await this.prisma.user.findUnique({
            where: { id: userId },
            select: { fcmToken: true },
        });
        if (!user?.fcmToken) {
            return;
        }
        await this.sendToToken(userId, user.fcmToken, title, body, data);
    }
    async sendToUsers(userIds, title, body, data) {
        if (!this.projectId || userIds.length === 0) {
            return;
        }
        const unique = Array.from(new Set(userIds));
        await Promise.all(unique.map((id) => this.sendToUser(id, title, body, data)));
    }
    async sendToToken(userId, token, title, body, data) {
        const accessToken = await this.getAccessToken();
        if (!accessToken) {
            return;
        }
        try {
            const res = await fetch(`https://fcm.googleapis.com/v1/projects/${this.projectId}/messages:send`, {
                method: 'POST',
                headers: {
                    Authorization: `Bearer ${accessToken}`,
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    message: {
                        token,
                        notification: { title, body },
                        data: this.stringifyData(data),
                        android: {
                            priority: 'HIGH',
                            notification: { channel_id: 'default', sound: 'default' },
                        },
                    },
                }),
            });
            if (res.ok) {
                return;
            }
            const text = await res.text();
            if (this.isInvalidTokenError(res.status, text)) {
                await this.prisma.user
                    .updateMany({
                    where: { id: userId, fcmToken: token },
                    data: { fcmToken: null },
                })
                    .catch(() => undefined);
                this.logger.debug(`Cleared invalid FCM token for user ${userId}`);
                return;
            }
            this.logger.warn(`FCM send failed: ${res.status} ${text}`);
        }
        catch (err) {
            this.logger.warn(`FCM send error: ${err.message}`);
        }
    }
    isInvalidTokenError(status, body) {
        if (status === 404) {
            return true;
        }
        if (status === 400 || status === 403) {
            return /UNREGISTERED|INVALID_ARGUMENT|registration-token-not-registered/i.test(body);
        }
        return false;
    }
    async getAccessToken() {
        const now = Date.now();
        if (this.tokenCache && this.tokenCache.expiresAt > now + 60_000) {
            return this.tokenCache.accessToken;
        }
        try {
            const res = await fetch(METADATA_TOKEN_URL, {
                headers: { 'Metadata-Flavor': 'Google' },
            });
            if (!res.ok) {
                return null;
            }
            const json = (await res.json());
            if (!json.access_token) {
                return null;
            }
            this.tokenCache = {
                accessToken: json.access_token,
                expiresAt: now + (json.expires_in ?? 3000) * 1000,
            };
            return json.access_token;
        }
        catch (err) {
            this.logger.debug(`Could not obtain metadata access token: ${err.message}`);
            return null;
        }
    }
    stringifyData(data) {
        if (!data) {
            return undefined;
        }
        const out = {};
        for (const [key, value] of Object.entries(data)) {
            if (value !== null && value !== undefined) {
                out[key] = String(value);
            }
        }
        return Object.keys(out).length > 0 ? out : undefined;
    }
};
exports.NotificationsService = NotificationsService;
exports.NotificationsService = NotificationsService = NotificationsService_1 = __decorate([
    (0, common_1.Injectable)(),
    __metadata("design:paramtypes", [config_1.ConfigService,
        prisma_service_1.PrismaService])
], NotificationsService);
//# sourceMappingURL=notifications.service.js.map