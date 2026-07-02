import { ConfigService } from '@nestjs/config';
import { PrismaService } from '../prisma/prisma.service';
type PushData = Record<string, string | number | null | undefined>;
export declare class NotificationsService {
    private readonly config;
    private readonly prisma;
    private readonly logger;
    private readonly projectId;
    private tokenCache;
    constructor(config: ConfigService, prisma: PrismaService);
    sendToUser(userId: string, title: string, body: string, data?: PushData): Promise<void>;
    sendToUsers(userIds: string[], title: string, body: string, data?: PushData): Promise<void>;
    private sendToToken;
    private isInvalidTokenError;
    private getAccessToken;
    private stringifyData;
}
export {};
