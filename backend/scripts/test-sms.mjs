import { config } from 'dotenv';
import { resolve, dirname } from 'path';
import { fileURLToPath } from 'url';

const __dirname = dirname(fileURLToPath(import.meta.url));
config({ path: resolve(__dirname, '../.env') });
config({ path: resolve(__dirname, '../.env.production') });

const phoneArg = process.argv[2]?.replace(/\D/g, '') ?? '';
const phone10 =
  phoneArg.length === 12 && phoneArg.startsWith('91')
    ? phoneArg.slice(2)
    : phoneArg.length === 11 && phoneArg.startsWith('0')
      ? phoneArg.slice(1)
      : phoneArg;

const accountSid = process.env.TWILIO_ACCOUNT_SID?.trim();
const authToken = process.env.TWILIO_AUTH_TOKEN?.trim();
const verifySid =
  process.env.TWILIO_VERIFY_SERVICE_SID?.trim() ||
  (process.env.TWILIO_MESSAGING_SERVICE_SID?.startsWith('VA')
    ? process.env.TWILIO_MESSAGING_SERVICE_SID.trim()
    : '');

if (!phone10 || phone10.length !== 10) {
  console.error('Usage: npm run test:sms -- 9876543210');
  process.exit(1);
}

if (!accountSid || !authToken || !verifySid) {
  console.error(
    'Set TWILIO_ACCOUNT_SID, TWILIO_AUTH_TOKEN, and TWILIO_VERIFY_SERVICE_SID (VA…) in .env or .env.production',
  );
  process.exit(1);
}

const to = `+91${phone10}`;
const credentials = Buffer.from(`${accountSid}:${authToken}`).toString('base64');
const body = new URLSearchParams({ To: to, Channel: 'sms' });

const response = await fetch(
  `https://verify.twilio.com/v2/Services/${verifySid}/Verifications`,
  {
    method: 'POST',
    headers: {
      Authorization: `Basic ${credentials}`,
      'Content-Type': 'application/x-www-form-urlencoded',
      Accept: 'application/json',
    },
    body: body.toString(),
  },
);

const raw = await response.text();
console.log(`HTTP ${response.status}`);
console.log(raw);

if (!response.ok) {
  console.error('\nVerify test failed. Trial accounts must verify the recipient number in Twilio Console.');
  process.exit(1);
}

console.log(`\nOTP sent via Twilio Verify to ${to}. Check SMS and enter the code in the app.`);
