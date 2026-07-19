# Auth'ni Firebase'dan backendga ko'chirish — REJA

> Maqsad: autentifikatsiyani **Firebase Auth**'dan **o'z backend**imizga ko'chirish. Backend
> endi identity provayder bo'ladi: o'z **JWT** tokenини chiqaradi, OTP/parol/OAuth'ni o'zi
> boshqaradi. Bu **reja** — kod yozishдан oldin tasdiqlash uchun.

## Nega mumkin / nega ehtiyot
- **Mumkin:** hamma narsa standart — JWT, parol hash, OTP, OAuth token tekshirish.
- **Ehtiyot:** endi xavfsizlik **to'liq sizда** — parol hash, token muddati/rotatsiya, OTP
  brute-force, rate-limit. Noto'g'ri qilinsa jiddiy zaiflik. Shuning uchun bosqichma-bosqich.

---

## 1. Token modeli (asos)
- **Access token** — JWT, qisqa muddat (~15 min), `uid`+`role` bilan. Har so'rovда `Bearer`.
- **Refresh token** — uzoq muddat (~30 kun), **rotatsiya bilan** (har yangilашда eskisi bekor),
  bazада saqlanadi (bekor qilish/logout uchun). Qurilmада **xavfsiz saqlash** (Keychain/Keystore).
- Backend **imzo kaliti** (JWT secret / RS256 juftlik) — env'да, aylanadigan (rotatable).

## 2. Backend — yangi jadval/modullar
- **users** — `passwordHash?` (argon2id/bcrypt), `phone?`, `email?`, `emailVerified`, `phoneVerified`
  qo'shiladi (mavjud `users` kengaytiriladi).
- **otp_codes** — `phone, codeHash, expiresAt, attempts, purpose (LOGIN|RESET)`; muddat ~5 min.
- **refresh_tokens** — `userId, tokenHash, expiresAt, revoked, deviceId`.
- **oauth_identities** — `userId, provider (GOOGLE|APPLE|TELEGRAM), providerUserId` (bir user + N provayder).

## 3. Backend — auth endpointlari (yangi)
| Endpoint | Vazifa |
|---|---|
| `POST /auth/otp/request` | Telefonга kod yuborish (SMS gateway) — rate-limit |
| `POST /auth/otp/verify` | Kodni tekshirib **JWT (access+refresh)** berish |
| `POST /auth/email/register` | Email+parol ro'yxat (parol hash) |
| `POST /auth/email/login` | Email+parol → JWT |
| `POST /auth/password/reset-request` · `/reset` | Parol tiklash (email/OTP) |
| `POST /auth/oauth/google` · `/apple` · `/telegram` | Provayder tokenini **serverда tekshirib** → JWT |
| `POST /auth/refresh` | Refresh token → yangi access (rotatsiya) |
| `POST /auth/logout` | Refresh tokenни bekor qilish |

## 4. SMS gateway (telefon OTP — UZ)
- **Eskiz.uz** yoki **Play Mobile** — mahalliy, arzon, ishonchli (Firebase SMS'дан afzal).
- Backend kod generatsiya → hash'lab saqlaydi → SMS yuboradi. **Rate-limit majburiy**
  (telefon+IP bo'yicha), brute-force himoyasi (urinishlar soni).

## 5. OAuth (Google / Apple / Telegram)
- **Klient hali ham provayder SDK'sини ishlatadi** (native token olish uchun) — bu o'zgarmaydi.
- Farqi: olingan **ID token / hash backendга yuboriladi**, backend uni **serverда tekshiradi**
  (Google certs, Apple public keys, Telegram `hash` HMAC) va **o'z JWT**ини qaytaradi.
- Firebase bu bosqichда butunlay chiqib ketadi.

## 6. Ilova (klient) o'zgarishlari
- **`FirebaseTokenProvider` → `BackendTokenStore`** — access+refresh'ni xavfsiz saqlaydi.
- **`HttpClientFactory` bearer plugin** — backend access tokenини qo'yadi; 401'да
  `POST /auth/refresh` bilan yangilaydi (hozirgi Firebase refresh o'rniga).
- **`FirebaseAuthRepository` → `BackendAuthRepository`** — login/register/reset backend API'ga.
- **`SocialAuthController`** — OTP va email endi backendга; Google/Apple/Telegram SDK qoladi,
  natija backendга yuboriladi.
- **`AuthFlowViewModel`** — oqim o'zgarmaydi (OTP → verify → profil tekshiruvi), faqat manba backend.
- **Firebase bog'liqliklarини olib tashlash** (`gitlive firebase-auth`, `functions`) — oxirида.

## 7. Mavjud foydalanuvchilar migratsiyasi
- Agar Firebase'да allaqachon foydalanuvchi bo'lsa: `firebase_uid` ni `users`ga saqlab, **birinchi
  kirishда telefon OTP bilan qayta tasdiqlab** backend hisobiga bog'lash (lazy migration).
- Yoki toza start (agar prod'да haqiqiy user yo'q bo'lsa) — eng oson.

---

## Bosqichlar (tavsiya etilgan tartib)
1. **Token infratuzilma** — JWT issue/verify + refresh rotatsiya + `refresh_tokens` jadvali.
2. **Email/parol** — register/login/reset (eng sodda, oqimni sinash uchun).
3. **Telefon OTP + Eskiz** — UZ uchun eng katta yutuq; rate-limit bilan.
4. **OAuth exchange** — Google/Apple/Telegram serverда tekshirish.
5. **Ilova auth qatlami** — token store + bearer + repository/controller almashtirish.
6. **Firebase'ни olib tashlash** + `BACKEND_PROMPT.md` yangilash (Firebase Admin bloki o'rniga
   JWT/OTP/OAuth spetsifikatsiyasi), `elon-uz.json`ga auth endpointlarини qo'shish.
7. (kerak bo'lsa) mavjud foydalanuvchi migratsiyasi.

## Qaror talab qiladigan nuqtalar
- **JWT:** simmetrik (HS256, sodda) yoki asimmetrik (RS256, kalit ajratilgan)?
- **SMS provayder:** Eskiz yoki Play Mobile?
- **Login usullari:** qaysilarni qoldirasiz? (telefon OTP / email-parol / Google / Apple / Telegram)
- **Migratsiya:** toza start yoki mavjud Firebase userlarini ko'chirish?

## Ta'sir / narx (rost gap)
- Backend ishi sezilarli ortadi (auth — mas'uliyatli qism).
- SMS narxi va yetkazish endi **sizning zimmangizда**.
- Ilova auth qatlami qayta yoziladi (o'rta hajm), lekin oqim/ekranlar o'zgarmaydi.
- Firebase loyihasi va Admin credential **kerak bo'lmaydi** — `BACKEND_PROMPT.md` dagi Firebase
  bloki olib tashlanadi.
