# Email 6 xonali kod — sozlash

Email ro'yxatdan o'tishда 6 xonali kod **Cloud Function** orqali yuboriladi va tekshiriladi
(`requestEmailSignup` / `confirmEmailSignup`). Kod Gmail SMTP (nodemailer) orqali jo'natiladi.

**Muhim:** akkaunt **faqat kod tasdiqlangandan keyin** yaratiladi (kod kelmasa Firebase'да
foydalanuvchi umuman saqlanmaydi).

## Oqim
```
Register(email+parol) → requestEmailSignup(email)  [akkaunt HALI yo'q]
   → email'ga 6 xonali kod → EmailVerifyScreen (6 katak)
   → confirmEmailSignup(email, kod, parol)  → server kod to'g'ri bo'lsa
        admin.auth().createUser(emailVerified:true)  [akkaunt SHU YERDA yaratiladi]
   → ilova email/parol bilan kiradi → rol → profil → Home
```

## 1. Gmail App Password olish
Kod yuboradigan Gmail hisobi uchun:
1. Google Account → **Security** → **2-Step Verification** yoqilган bo'lsin.
2. **App passwords** → yangi parol yarating (masalan "StudentClubs") → 16 belgilik parolni saqlang.

> Oddiy Gmail parol emas — aynan **App Password** kerak.

## 2. Maxfiylarni o'rnatish
```bash
cd functions && npm install && cd ..     # nodemailer o'rnatiladi

firebase functions:secrets:set GMAIL_EMAIL          # kimdan yuboriladi (masalan sizniki@gmail.com)
firebase functions:secrets:set GMAIL_APP_PASSWORD   # yuqoridagi 16 belgilik App Password
```

## 3. Deploy
```bash
firebase deploy --only functions
```
`sendEmailCode`, `verifyEmailCode` (va `telegramAuth`) `us-central1` regionига chiqadi.
Client GitLive `Firebase.functions` (default `us-central1`) bilan ularni chaqiradi — mos.

## 4. iOS Xcode (SPM)
GitLive `firebase-functions` iOS link uchun **FirebaseFunctions** kerak:
- File → Add Package Dependencies… → `firebase-ios-sdk` → **FirebaseFunctions** ni ham belgilang
  (FirebaseAuth + FirebaseFirestore bilan birga) → target **iosApp**.

## 5. Firebase Console
- **Authentication → Email/Password → Enable**.
- **Firestore** yaratilган bo'lsin (kodlar `emailSignups/{sha256(email)}` da vaqtincha saqlanadi).

## Xavfsizlik
- Kod Firestore'да `emailSignups/{sha256(email)}` da saqlanadi, 10 daqiqa, 5 urinish; keyin o'chiriladi.
- Email/App Password **faqat** Cloud Function secret'ida — client'да yo'q.
- Akkaunt (parol bilan) **faqat** kod to'g'ri bo'lganда server tomonда yaratiladi (`emailVerified=true`).
