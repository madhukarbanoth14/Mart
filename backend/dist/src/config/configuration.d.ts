declare const _default: () => {
    nodeEnv: string;
    port: number;
    database: {
        url: string | undefined;
    };
    jwt: {
        secret: string;
        expiresIn: string;
    };
    razorpay: {
        keyId: string;
        keySecret: string;
        webhookSecret: string;
    };
    fcm: {
        serverKey: string;
    };
    firebase: {
        projectId: string;
    };
    email: {
        brevoApiKey: string;
        brevoSmtpLogin: string;
        smtpHost: string;
        smtpPort: number;
        smtpSecure: boolean;
        smtpUser: string;
        smtpPass: string;
        from: string;
        appName: string;
    };
    ordering: {
        maxOrderQuantity: number;
    };
    sms: {
        twilioAccountSid: string;
        twilioAuthToken: string;
        twilioVerifyServiceSid: string;
        twilioMessagingServiceSid: string;
        twilioSmsFrom: string;
        otpExpiryMinutes: number;
        otpMessageTemplate: string;
    };
};
export default _default;
