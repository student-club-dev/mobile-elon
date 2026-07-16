# Telegram Login — sozlash (backendsiz, Cloud Function bilan)

Telegram Firebase'ning tayyor provideri emas, shuning uchun oqim quyidagicha:

```
Ilova → Telegram Login sahifasi (Firebase Hosting) → foydalanuvchi Telegram'da tasdiqlaydi
      → Cloud Function imzoni bot token bilan tekshiradi → Firebase custom token
      → sahifa `studentclubs://telegram?token=...` deep-link'iga qaytadi
      → ilova custom token bilan Firebase'ga kiradi
```

## 1. Telegram bot yaratish
1. Telegram'da **@BotFather** → `/newbot` → bot nomi va **username** oling.
2. Bot **token**ini saqlang (masalan `123456:ABC...`).
3. `/setdomain` → botga hosting domeningizni bering (masalan `your-project.web.app`).
   > Telegram Login Widget faqat shu domendan ishlaydi.

## 2. Kod'dagi placeholder'larni to'ldirish
- `public/telegram-login.html` → `data-telegram-login="BOT_USERNAME"` ni bot username'ingizga.
- `.firebaserc` → `YOUR_FIREBASE_PROJECT_ID` ni loyiha ID'ingizga.
- `dev/feature/auth/.../social/TelegramConfig.kt` → `LOGIN_URL` ni
  `https://<loyiha>.web.app/telegram-login.html` ga.

## 3. Firebase deploy
```bash
# Bir marta
firebase login
cd functions && npm install && cd ..

# Bot tokenni maxfiy sifatida saqlash (repositoriyaga tushmaydi)
firebase functions:secrets:set TELEGRAM_BOT_TOKEN
#   → BotFather bergan tokenni kiriting

# Function + hosting sahifani deploy qilish
firebase deploy --only functions,hosting
```

## 4. Deep-link (allaqachon kodda)
- **Android**: `TelegramAuthActivity` + `studentclubs://telegram` intent-filter
  (`dev/feature/auth/src/androidMain/AndroidManifest.xml`) — tayyor.
- **iOS**: `ASWebAuthenticationSession` callback scheme `studentclubs` — `SocialAuthBridge.swift`da tayyor.
  Ixtiyoriy: `Info.plist`ga `studentclubs` URL scheme qo'shsangiz, universal ishlaydi.

## 5. Tekshirish
Ilovada **Welcome / Telefon** ekranidagi Telegram tugmasini bosing → Telegram tasdiq →
avtomatik ravishda ilovaga qaytib, Firebase sessiyasi ochiladi (`uid = telegram:<id>`).

## Xavfsizlik
- Bot token **faqat** Cloud Function'da (`TELEGRAM_BOT_TOKEN` secret) — client'da yo'q.
- Imzo (`hash`) va `auth_date` (24 soat) server tomonda tekshiriladi.
- Firestore `users/{uid}` qoidalari Telegram uid'lariga ham tegishli (`telegram:<id>`).
