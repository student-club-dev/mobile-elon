const { onRequest, onCall, HttpsError } = require("firebase-functions/v2/https");
const { defineSecret } = require("firebase-functions/params");
const admin = require("firebase-admin");
const crypto = require("crypto");
const nodemailer = require("nodemailer");

admin.initializeApp();
const db = admin.firestore();

// Maxfiylar:
//   firebase functions:secrets:set TELEGRAM_BOT_TOKEN
//   firebase functions:secrets:set GMAIL_EMAIL
//   firebase functions:secrets:set GMAIL_APP_PASSWORD   (Gmail "App password")
const TELEGRAM_BOT_TOKEN = defineSecret("TELEGRAM_BOT_TOKEN");
const GMAIL_EMAIL = defineSecret("GMAIL_EMAIL");
const GMAIL_APP_PASSWORD = defineSecret("GMAIL_APP_PASSWORD");

const REGION = "us-central1";

function sixDigitCode() {
  return String(crypto.randomInt(0, 1000000)).padStart(6, "0");
}
function emailKey(email) {
  return crypto.createHash("sha256").update(email).digest("hex");
}
async function sendCodeEmail(toEmail, code) {
  const transporter = nodemailer.createTransport({
    service: "gmail",
    auth: { user: GMAIL_EMAIL.value(), pass: GMAIL_APP_PASSWORD.value() },
  });
  await transporter.sendMail({
    from: `Student Super App <${GMAIL_EMAIL.value()}>`,
    to: toEmail,
    subject: `Tasdiqlash kodi: ${code}`,
    html: `
      <div style="font-family:sans-serif;max-width:420px;margin:auto">
        <h2 style="color:#6C47FF;">Student Super App</h2>
        <p>Ro'yxatdan o'tishni yakunlash uchun kod:</p>
        <div style="font-size:32px;font-weight:800;letter-spacing:8px;color:#1E1B4B">${code}</div>
        <p style="color:#6E698C;font-size:13px">Kod 10 daqiqa amal qiladi. Agar so'ramagan bo'lsangiz, e'tiborsiz qoldiring.</p>
      </div>`,
  });
}

// ===========================================================================
// Email ro'yxatdan o'tish — akkaunt FAQAT kod tasdiqlangandan keyin yaratiladi
// ===========================================================================

/** 1-qadam: emailga 6 xonali kod yuboradi. Akkaunt hali yaratilmaydi. */
exports.requestEmailSignup = onCall(
  { secrets: [GMAIL_EMAIL, GMAIL_APP_PASSWORD], region: REGION },
  async (request) => {
    const email = String(request.data?.email || "").trim().toLowerCase();
    if (!email || !email.includes("@")) throw new HttpsError("invalid-argument", "Email noto'g'ri");

    // Allaqachon ro'yxatdan o'tganmi?
    try {
      await admin.auth().getUserByEmail(email);
      throw new HttpsError("already-exists", "Bu email allaqachon ro'yxatdan o'tgan");
    } catch (e) {
      if (e instanceof HttpsError) throw e;
      if (e.code !== "auth/user-not-found") throw e; // yo'q bo'lsa — davom etamiz
    }

    const code = sixDigitCode();
    await db.collection("emailSignups").doc(emailKey(email)).set({
      email, code, expiresAt: Date.now() + 10 * 60 * 1000, attempts: 0,
    });
    await sendCodeEmail(email, code);
    return { sent: true };
  }
);

/** 2-qadam: kodni tekshiradi va faqat to'g'ri bo'lsa akkaunt yaratadi. */
exports.confirmEmailSignup = onCall({ region: REGION }, async (request) => {
  const email = String(request.data?.email || "").trim().toLowerCase();
  const code = String(request.data?.code || "").trim();
  const password = String(request.data?.password || "");
  if (password.length < 6) throw new HttpsError("invalid-argument", "Parol kamida 6 belgi");

  const ref = db.collection("emailSignups").doc(emailKey(email));
  const snap = await ref.get();
  if (!snap.exists) throw new HttpsError("failed-precondition", "Kod topilmadi — qayta yuboring");

  const data = snap.data();
  if (Date.now() > data.expiresAt) {
    await ref.delete();
    throw new HttpsError("deadline-exceeded", "Kod muddati o'tgan");
  }
  if ((data.attempts || 0) >= 5) {
    await ref.delete();
    throw new HttpsError("resource-exhausted", "Urinishlar tugadi — qayta yuboring");
  }
  if (code !== data.code) {
    await ref.update({ attempts: (data.attempts || 0) + 1 });
    throw new HttpsError("invalid-argument", "Kod noto'g'ri");
  }

  // Kod to'g'ri → akkaunt yaratamiz (emailVerified=true)
  try {
    await admin.auth().createUser({ email, password, emailVerified: true });
  } catch (e) {
    if (e.code === "auth/email-already-exists") throw new HttpsError("already-exists", "Bu email band");
    throw e;
  }
  await ref.delete();
  return { ok: true };
});

// ===========================================================================
// Telegram Login — Firebase custom token
// ===========================================================================

exports.telegramAuth = onRequest(
  { secrets: [TELEGRAM_BOT_TOKEN], cors: true, region: REGION },
  async (req, res) => {
    try {
      if (req.method !== "POST") {
        res.status(405).json({ error: "POST so'rovi kerak" });
        return;
      }
      const data = req.body || {};
      const { hash, ...fields } = data;
      if (!hash || !fields.id) {
        res.status(400).json({ error: "Telegram ma'lumoti to'liq emas" });
        return;
      }

      const botToken = TELEGRAM_BOT_TOKEN.value();
      const secretKey = crypto.createHash("sha256").update(botToken).digest();
      const checkString = Object.keys(fields).sort().map((k) => `${k}=${fields[k]}`).join("\n");
      const expectedHash = crypto.createHmac("sha256", secretKey).update(checkString).digest("hex");
      if (expectedHash !== hash) {
        res.status(401).json({ error: "Imzo noto'g'ri" });
        return;
      }

      const authDate = Number(fields.auth_date || 0);
      if (!authDate || Date.now() / 1000 - authDate > 86400) {
        res.status(401).json({ error: "Login muddati o'tgan" });
        return;
      }

      const uid = `telegram:${fields.id}`;
      const displayName =
        [fields.first_name, fields.last_name].filter(Boolean).join(" ") ||
        fields.username || "Telegram foydalanuvchi";
      const photoURL = fields.photo_url || undefined;
      try {
        await admin.auth().updateUser(uid, { displayName, photoURL });
      } catch (e) {
        if (e.code === "auth/user-not-found") {
          await admin.auth().createUser({ uid, displayName, photoURL });
        } else { throw e; }
      }

      const token = await admin.auth().createCustomToken(uid, {
        provider: "telegram",
        telegramId: String(fields.id),
        username: fields.username || null,
      });
      res.json({ token });
    } catch (err) {
      console.error("telegramAuth xatosi:", err);
      res.status(500).json({ error: err.message || "Server xatosi" });
    }
  }
);
