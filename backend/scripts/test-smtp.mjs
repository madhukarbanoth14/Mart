import nodemailer from 'nodemailer';
import { setDefaultResultOrder } from 'node:dns';
import { config } from 'dotenv';
import { resolve, dirname } from 'path';
import { fileURLToPath } from 'url';

setDefaultResultOrder('ipv4first');

const __dirname = dirname(fileURLToPath(import.meta.url));
config({ path: resolve(__dirname, '../.env') });

const to = process.argv[2]?.trim() || process.env.SEED_ADMIN_EMAIL?.trim() || 'madhukar@techfylabs.com';
const appName = process.env.MAIL_APP_NAME?.trim() || 'FlashMart';
const fromRaw = process.env.MAIL_FROM?.trim() || 'madhukar@techfylabs.com';
const brevoApiKey = process.env.BREVO_API_KEY?.trim();

function parseSender(raw) {
  const match = raw.match(/^(.+?)\s*<([^>]+)>$/);
  if (match) {
    return { name: match[1].trim().replace(/^["']|["']$/g, ''), email: match[2].trim() };
  }
  return { name: appName, email: raw };
}

async function sendViaBrevoApi() {
  const sender = parseSender(fromRaw);
  const response = await fetch('https://api.brevo.com/v3/smtp/email', {
    method: 'POST',
    headers: {
      'api-key': brevoApiKey,
      'Content-Type': 'application/json',
      Accept: 'application/json',
    },
    body: JSON.stringify({
      sender,
      to: [{ email: to }],
      subject: `${appName} — email test`,
      htmlContent: `<p>Hi,</p><p>This is a <strong>test email</strong> from ${appName} via Brevo API.</p>`,
      textContent: `Hi,\n\nThis is a test email from ${appName} via Brevo API.\n`,
    }),
  });
  const body = await response.text();
  if (!response.ok) {
    throw new Error(`Brevo API ${response.status}: ${body.slice(0, 300)}`);
  }
  console.log(`Brevo API email sent to ${to}`);
  console.log(body);
}

async function sendViaSmtp() {
  const host = process.env.SMTP_HOST?.trim();
  const port = parseInt(process.env.SMTP_PORT ?? '587', 10);
  const secure = process.env.SMTP_SECURE === 'true';
  const user = (process.env.BREVO_SMTP_LOGIN || process.env.SMTP_USER)?.trim();
  const pass = process.env.SMTP_PASS?.trim();
  if (!host || !user || !pass) {
    throw new Error('Missing SMTP_HOST, BREVO_SMTP_LOGIN/SMTP_USER, or SMTP_PASS');
  }
  const transport = nodemailer.createTransport({ host, port, secure, auth: { user, pass } });
  await transport.verify();
  console.log(`SMTP verified (${host}:${port}, login ${user})`);
  const info = await transport.sendMail({
    from: fromRaw.includes('<') ? fromRaw : `"${appName}" <${fromRaw}>`,
    to,
    subject: `${appName} — SMTP test`,
    text: `Hi,\n\nTest email from ${appName} via SMTP.\n`,
    html: `<p>Hi,</p><p>Test email from <strong>${appName}</strong> via SMTP.</p>`,
  });
  console.log(`SMTP email sent to ${to}`);
  console.log(`Message ID: ${info.messageId}`);
}

try {
  if (brevoApiKey) {
    await sendViaBrevoApi();
  } else {
    await sendViaSmtp();
  }
} catch (err) {
  console.error('Email test failed:', err.message);
  console.error('\nSetup help:');
  console.error('1. Open https://app.brevo.com → SMTP & API → API Keys → Create');
  console.error('2. Add to Mart/backend/.env:  BREVO_API_KEY=xkeysib-...');
  console.error('3. Verify sender: Settings → Senders → madhukar@techfylabs.com must be verified');
  console.error('4. Restart backend and run:  npm run test:email');
  process.exit(1);
}
