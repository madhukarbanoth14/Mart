"use strict";
var __createBinding = (this && this.__createBinding) || (Object.create ? (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    var desc = Object.getOwnPropertyDescriptor(m, k);
    if (!desc || ("get" in desc ? !m.__esModule : desc.writable || desc.configurable)) {
      desc = { enumerable: true, get: function() { return m[k]; } };
    }
    Object.defineProperty(o, k2, desc);
}) : (function(o, m, k, k2) {
    if (k2 === undefined) k2 = k;
    o[k2] = m[k];
}));
var __setModuleDefault = (this && this.__setModuleDefault) || (Object.create ? (function(o, v) {
    Object.defineProperty(o, "default", { enumerable: true, value: v });
}) : function(o, v) {
    o["default"] = v;
});
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __importStar = (this && this.__importStar) || (function () {
    var ownKeys = function(o) {
        ownKeys = Object.getOwnPropertyNames || function (o) {
            var ar = [];
            for (var k in o) if (Object.prototype.hasOwnProperty.call(o, k)) ar[ar.length] = k;
            return ar;
        };
        return ownKeys(o);
    };
    return function (mod) {
        if (mod && mod.__esModule) return mod;
        var result = {};
        if (mod != null) for (var k = ownKeys(mod), i = 0; i < k.length; i++) if (k[i] !== "default") __createBinding(result, mod, k[i]);
        __setModuleDefault(result, mod);
        return result;
    };
})();
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
var EmailService_1;
Object.defineProperty(exports, "__esModule", { value: true });
exports.EmailService = void 0;
const common_1 = require("@nestjs/common");
const config_1 = require("@nestjs/config");
const node_dns_1 = require("node:dns");
const nodemailer = __importStar(require("nodemailer"));
(0, node_dns_1.setDefaultResultOrder)('ipv4first');
let EmailService = EmailService_1 = class EmailService {
    config;
    logger = new common_1.Logger(EmailService_1.name);
    transporter = null;
    transporterChecked = false;
    constructor(config) {
        this.config = config;
    }
    isConfigured() {
        return this.isBrevoApiConfigured() || this.isSmtpConfigured();
    }
    isBrevoApiConfigured() {
        return Boolean(this.config.get('email.brevoApiKey', '')?.trim());
    }
    isSmtpConfigured() {
        const host = this.config.get('email.smtpHost', '');
        const user = this.smtpLogin();
        const pass = this.config.get('email.smtpPass', '');
        return Boolean(host?.trim() && user?.trim() && pass?.trim());
    }
    smtpLogin() {
        return (this.config.get('email.brevoSmtpLogin', '')?.trim() ||
            this.config.get('email.smtpUser', '')?.trim() ||
            '');
    }
    async getTransporter() {
        if (this.transporterChecked) {
            return this.transporter;
        }
        this.transporterChecked = true;
        if (!this.isSmtpConfigured()) {
            if (!this.isBrevoApiConfigured()) {
                this.logger.warn('Email not configured. Set BREVO_API_KEY (recommended) or SMTP_HOST + BREVO_SMTP_LOGIN + SMTP_PASS.');
            }
            return null;
        }
        const host = this.config.get('email.smtpHost', '');
        const port = this.config.get('email.smtpPort', 587);
        const secure = this.config.get('email.smtpSecure', false);
        const user = this.smtpLogin();
        const pass = this.config.get('email.smtpPass', '');
        this.transporter = nodemailer.createTransport({
            host,
            port,
            secure,
            auth: { user, pass },
        });
        try {
            await this.transporter.verify();
            this.logger.log(`SMTP ready (${host}:${port}, login ${user})`);
        }
        catch (err) {
            this.logger.error(`SMTP verify failed: ${err.message}`);
            this.transporter = null;
        }
        return this.transporter;
    }
    fromAddress() {
        return (this.config.get('email.from', '')?.trim() ||
            this.smtpLogin() ||
            'noreply@knsrmart.com');
    }
    appName() {
        return this.config.get('email.appName', 'KNSR Mart') ?? 'KNSR Mart';
    }
    parseSender() {
        const raw = this.fromAddress();
        const match = raw.match(/^(.+?)\s*<([^>]+)>$/);
        if (match) {
            return {
                name: match[1].trim().replace(/^["']|["']$/g, ''),
                email: match[2].trim(),
            };
        }
        return { name: this.appName(), email: raw };
    }
    async sendViaBrevoApi(params) {
        const apiKey = this.config.get('email.brevoApiKey', '')?.trim();
        if (!apiKey) {
            return { sent: false, reason: 'BREVO_API_KEY not configured' };
        }
        try {
            const response = await fetch('https://api.brevo.com/v3/smtp/email', {
                method: 'POST',
                headers: {
                    'api-key': apiKey,
                    'Content-Type': 'application/json',
                    Accept: 'application/json',
                },
                body: JSON.stringify({
                    sender: this.parseSender(),
                    to: [{ email: params.to }],
                    subject: params.subject,
                    htmlContent: params.html,
                    textContent: params.text,
                }),
            });
            const bodyText = await response.text();
            if (!response.ok) {
                return {
                    sent: false,
                    reason: `Brevo API ${response.status}: ${bodyText.slice(0, 240)}`,
                };
            }
            let messageId = 'brevo';
            try {
                const parsed = JSON.parse(bodyText);
                messageId = parsed.messageId ?? messageId;
            }
            catch {
            }
            this.logger.log(`Email sent via Brevo API to ${params.to}`);
            return { sent: true, messageId };
        }
        catch (err) {
            const reason = err.message;
            this.logger.error(`Brevo API to ${params.to} failed: ${reason}`);
            return { sent: false, reason };
        }
    }
    async sendViaSmtp(params) {
        const transport = await this.getTransporter();
        if (!transport) {
            return { sent: false, reason: 'SMTP not configured or verification failed' };
        }
        const from = this.fromAddress();
        try {
            const info = await transport.sendMail({
                from: from.includes('<') ? from : `"${this.appName()}" <${from}>`,
                to: params.to,
                subject: params.subject,
                text: params.text,
                html: params.html,
            });
            this.logger.log(`Email sent via SMTP to ${params.to}`);
            return { sent: true, messageId: info.messageId ?? 'unknown' };
        }
        catch (err) {
            const reason = err.message;
            this.logger.error(`SMTP email to ${params.to} failed: ${reason}`);
            return { sent: false, reason };
        }
    }
    async sendMail(params) {
        if (this.isBrevoApiConfigured()) {
            const apiResult = await this.sendViaBrevoApi(params);
            if (apiResult.sent) {
                return apiResult;
            }
            this.logger.warn(`Brevo API failed (${apiResult.reason}); trying SMTP fallback…`);
        }
        return this.sendViaSmtp(params);
    }
    async sendOnboardingCredentialsEmail(params) {
        const app = this.appName();
        const roleLabel = params.role === 'DEALER'
            ? 'dealer'
            : params.role === 'SHOPKEEPER'
                ? 'shopkeeper'
                : 'employee';
        const headline = params.context === 'approved'
            ? `Your ${roleLabel} account is approved`
            : `Welcome to ${app}`;
        const intro = params.context === 'approved'
            ? `Your ${app} ${roleLabel} account has been approved by the administrator. You can sign in now.`
            : `Your ${app} ${roleLabel} account has been created.`;
        const subject = params.context === 'approved'
            ? `${app} — your ${roleLabel} account is approved`
            : `${app} — your ${roleLabel} login details`;
        const signInSteps = [
            '',
            'Steps:',
            `1. Install or open the ${app} app on your phone`,
            '2. Enter your login email and password on the sign-in screen',
            '3. Use Forgot password in the app if you want to change your password later',
        ];
        const text = [
            `Hi ${params.name},`,
            '',
            intro,
            '',
            'Sign in with these credentials:',
            `Login email: ${params.loginEmail}`,
            `Password: ${params.loginPassword}`,
            ...signInSteps,
            '',
            'Keep this email private. Do not share your password.',
            '',
            `— ${app} Team`,
        ].join('\n');
        const html = `
      <div style="font-family:system-ui,sans-serif;max-width:520px;color:#1a1a2e">
        <h2 style="color:#2F48D4">${headline}</h2>
        <p>Hi <strong>${params.name}</strong>,</p>
        <p>${intro}</p>
        <p><strong>Sign in with:</strong></p>
        <table style="background:#f4f6fb;border-radius:8px;padding:12px;width:100%;border-collapse:collapse">
          <tr>
            <td style="padding:6px 8px;color:#666;width:38%">Login email</td>
            <td style="padding:6px 8px"><strong>${params.loginEmail}</strong></td>
          </tr>
          <tr>
            <td style="padding:6px 8px;color:#666">Password</td>
            <td style="padding:6px 8px"><code style="font-size:14px">${params.loginPassword}</code></td>
          </tr>
        </table>
        <ol style="padding-left:20px;line-height:1.6">
          <li>Install or open <strong>${app}</strong> on your phone</li>
          <li>Sign in with the email and password above</li>
          <li>Use <em>Forgot password</em> if you want to change your password later</li>
        </ol>
        <p style="color:#666;font-size:13px">Keep this email private. Do not share your password.</p>
        <p style="color:#888;font-size:12px">— ${app} Team</p>
      </div>`;
        return this.sendMail({ to: params.to, subject, text, html });
    }
    async sendDealerWelcomeEmail(params) {
        return this.sendOnboardingCredentialsEmail({
            to: params.to,
            name: params.dealerName,
            role: 'DEALER',
            loginEmail: params.to,
            loginPassword: params.loginPassword,
            context: 'created',
        });
    }
    async sendOnboardingApprovedEmail(params) {
        return this.sendOnboardingCredentialsEmail({
            to: params.to,
            name: params.name,
            role: params.role,
            loginEmail: params.loginEmail,
            loginPassword: params.loginPassword,
            context: 'approved',
        });
    }
    async sendPasswordResetEmail(params) {
        const app = this.appName();
        const expires = params.resetExpiresAt.toLocaleString('en-IN', {
            dateStyle: 'medium',
            timeStyle: 'short',
        });
        const subject = `${app} — password reset`;
        const text = [
            `Hi ${params.name},`,
            '',
            'You requested a password reset.',
            '',
            `Open the ${app} app → Reset password → paste this token:`,
            params.resetToken,
            '',
            `Expires: ${expires}`,
            '',
            `— ${app} Team`,
        ].join('\n');
        const html = `
      <div style="font-family:system-ui,sans-serif;max-width:520px">
        <h2 style="color:#2F48D4">${app} password reset</h2>
        <p>Hi <strong>${params.name}</strong>,</p>
        <p>Open the app → <em>Reset password</em> → paste this token:</p>
        <p style="background:#eef1ff;padding:12px;border-radius:8px;word-break:break-all;font-family:monospace">${params.resetToken}</p>
        <p style="color:#666;font-size:13px">Expires: ${expires}</p>
      </div>`;
        return this.sendMail({ to: params.to, subject, text, html });
    }
};
exports.EmailService = EmailService;
exports.EmailService = EmailService = EmailService_1 = __decorate([
    (0, common_1.Injectable)(),
    __metadata("design:paramtypes", [config_1.ConfigService])
], EmailService);
//# sourceMappingURL=email.service.js.map