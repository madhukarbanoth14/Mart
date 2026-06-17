"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.encryptApprovalPassword = encryptApprovalPassword;
exports.decryptApprovalPassword = decryptApprovalPassword;
const crypto_1 = require("crypto");
function deriveKey() {
    const secret = process.env.JWT_SECRET?.trim() || 'change-me-in-production';
    return (0, crypto_1.scryptSync)(secret, 'mart-approval-credential-v1', 32);
}
function encryptApprovalPassword(plain) {
    const iv = (0, crypto_1.randomBytes)(12);
    const cipher = (0, crypto_1.createCipheriv)('aes-256-gcm', deriveKey(), iv);
    const encrypted = Buffer.concat([
        cipher.update(plain, 'utf8'),
        cipher.final(),
    ]);
    const tag = cipher.getAuthTag();
    return Buffer.concat([iv, tag, encrypted]).toString('base64');
}
function decryptApprovalPassword(blob) {
    if (!blob?.trim())
        return null;
    try {
        const buf = Buffer.from(blob, 'base64');
        const iv = buf.subarray(0, 12);
        const tag = buf.subarray(12, 28);
        const encrypted = buf.subarray(28);
        const decipher = (0, crypto_1.createDecipheriv)('aes-256-gcm', deriveKey(), iv);
        decipher.setAuthTag(tag);
        return Buffer.concat([decipher.update(encrypted), decipher.final()]).toString('utf8');
    }
    catch {
        return null;
    }
}
//# sourceMappingURL=credential-vault.js.map