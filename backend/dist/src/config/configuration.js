"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.default = () => ({
    nodeEnv: process.env.NODE_ENV ?? 'development',
    port: parseInt(process.env.PORT ?? '3000', 10),
    database: {
        url: process.env.DATABASE_URL,
    },
    jwt: {
        secret: process.env.JWT_SECRET ?? 'change-me-in-production',
        expiresIn: process.env.JWT_EXPIRES_IN ?? '7d',
    },
    razorpay: {
        keyId: process.env.RAZORPAY_KEY_ID ?? '',
        keySecret: process.env.RAZORPAY_KEY_SECRET ?? '',
        webhookSecret: process.env.RAZORPAY_WEBHOOK_SECRET ?? '',
    },
    fcm: {
        serverKey: process.env.FCM_SERVER_KEY ?? '',
    },
    email: {
        brevoApiKey: process.env.BREVO_API_KEY ?? '',
        brevoSmtpLogin: process.env.BREVO_SMTP_LOGIN ?? '',
        smtpHost: process.env.SMTP_HOST ?? '',
        smtpPort: parseInt(process.env.SMTP_PORT ?? '587', 10),
        smtpSecure: process.env.SMTP_SECURE === 'true',
        smtpUser: process.env.SMTP_USER ?? '',
        smtpPass: process.env.SMTP_PASS ?? '',
        from: process.env.MAIL_FROM ?? '',
        appName: process.env.MAIL_APP_NAME ?? 'KNSR Mart',
    },
});
//# sourceMappingURL=configuration.js.map