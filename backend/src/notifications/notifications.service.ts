import { Injectable, Logger } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { PrismaService } from '../prisma/prisma.service';

type PushData = Record<string, string | number | null | undefined>;

const METADATA_TOKEN_URL =
  'http://metadata.google.internal/computeMetadata/v1/instance/service-accounts/default/token';

/**
 * Push notifications via FCM HTTP v1, with no heavy SDK dependency.
 *
 * Auth uses Application Default Credentials resolved from the GCP metadata
 * server, i.e. the Cloud Run runtime service account. This works as long as the
 * Firebase project is the same GCP project the service runs in. Outside GCP
 * (local dev) there is no metadata server, so push is a silent no-op.
 */
@Injectable()
export class NotificationsService {
  private readonly logger = new Logger(NotificationsService.name);
  private readonly projectId: string;
  private tokenCache: { accessToken: string; expiresAt: number } | null = null;

  constructor(
    private readonly config: ConfigService,
    private readonly prisma: PrismaService,
  ) {
    this.projectId = this.config.get<string>('firebase.projectId', '');
    if (!this.projectId) {
      this.logger.warn('FIREBASE_PROJECT_ID not set; push notifications disabled');
    }
  }

  /** Send a push to a single user (no-op if FCM unconfigured or no token). */
  async sendToUser(
    userId: string,
    title: string,
    body: string,
    data?: PushData,
  ): Promise<void> {
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

  /** Send the same push to many users (e.g. all admins). */
  async sendToUsers(
    userIds: string[],
    title: string,
    body: string,
    data?: PushData,
  ): Promise<void> {
    if (!this.projectId || userIds.length === 0) {
      return;
    }
    const unique = Array.from(new Set(userIds));
    await Promise.all(unique.map((id) => this.sendToUser(id, title, body, data)));
  }

  private async sendToToken(
    userId: string,
    token: string,
    title: string,
    body: string,
    data?: PushData,
  ): Promise<void> {
    const accessToken = await this.getAccessToken();
    if (!accessToken) {
      return;
    }
    try {
      const res = await fetch(
        `https://fcm.googleapis.com/v1/projects/${this.projectId}/messages:send`,
        {
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
        },
      );
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
    } catch (err) {
      this.logger.warn(`FCM send error: ${(err as Error).message}`);
    }
  }

  private isInvalidTokenError(status: number, body: string): boolean {
    if (status === 404) {
      return true;
    }
    if (status === 400 || status === 403) {
      return /UNREGISTERED|INVALID_ARGUMENT|registration-token-not-registered/i.test(
        body,
      );
    }
    return false;
  }

  /** Fetch (and cache) an OAuth token from the GCP metadata server. */
  private async getAccessToken(): Promise<string | null> {
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
      const json = (await res.json()) as {
        access_token?: string;
        expires_in?: number;
      };
      if (!json.access_token) {
        return null;
      }
      this.tokenCache = {
        accessToken: json.access_token,
        expiresAt: now + (json.expires_in ?? 3000) * 1000,
      };
      return json.access_token;
    } catch (err) {
      this.logger.debug(
        `Could not obtain metadata access token: ${(err as Error).message}`,
      );
      return null;
    }
  }

  /** FCM data values must be strings. */
  private stringifyData(data?: PushData): Record<string, string> | undefined {
    if (!data) {
      return undefined;
    }
    const out: Record<string, string> = {};
    for (const [key, value] of Object.entries(data)) {
      if (value !== null && value !== undefined) {
        out[key] = String(value);
      }
    }
    return Object.keys(out).length > 0 ? out : undefined;
  }
}
