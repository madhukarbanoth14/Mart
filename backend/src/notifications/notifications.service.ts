import { Injectable, Logger } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { PrismaService } from '../prisma/prisma.service';

/**
 * Legacy HTTP FCM (server key). For production, prefer FCM HTTP v1 with a service account.
 */
@Injectable()
export class NotificationsService {
  private readonly logger = new Logger(NotificationsService.name);

  constructor(
    private readonly config: ConfigService,
    private readonly prisma: PrismaService,
  ) {}

  async sendToUser(userId: string, title: string, body: string): Promise<void> {
    const key = this.config.get<string>('fcm.serverKey', '');
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
    } catch (err) {
      this.logger.warn(`FCM send error: ${(err as Error).message}`);
    }
  }
}
