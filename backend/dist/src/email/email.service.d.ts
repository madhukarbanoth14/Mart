import { ConfigService } from '@nestjs/config';
export type SendMailResult = {
    sent: true;
    messageId: string;
} | {
    sent: false;
    reason: string;
};
type MailPayload = {
    to: string;
    subject: string;
    text: string;
    html: string;
};
export declare class EmailService {
    private readonly config;
    private readonly logger;
    private transporter;
    private transporterChecked;
    constructor(config: ConfigService);
    isConfigured(): boolean;
    private isBrevoApiConfigured;
    private isSmtpConfigured;
    private smtpLogin;
    private getTransporter;
    private fromAddress;
    private appName;
    private parseSender;
    private sendViaBrevoApi;
    private sendViaSmtp;
    sendMail(params: MailPayload): Promise<SendMailResult>;
    sendOnboardingCredentialsEmail(params: {
        to: string;
        name: string;
        role: 'DEALER' | 'SHOPKEEPER' | 'EMPLOYEE';
        loginEmail: string;
        loginPassword: string;
        context: 'approved' | 'created';
    }): Promise<SendMailResult>;
    sendDealerWelcomeEmail(params: {
        to: string;
        dealerName: string;
        userId: string;
        loginPassword: string;
    }): Promise<SendMailResult>;
    sendOnboardingApprovedEmail(params: {
        to: string;
        name: string;
        role: 'DEALER' | 'SHOPKEEPER';
        loginEmail: string;
        loginPassword: string;
    }): Promise<SendMailResult>;
    sendPasswordResetEmail(params: {
        to: string;
        name: string;
        resetToken: string;
        resetExpiresAt: Date;
    }): Promise<SendMailResult>;
}
export {};
