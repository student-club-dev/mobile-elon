# O'zgartirish so'rovi — Google bilan kirish va raqamni OTP orqali bog'lash

> Backendga beriladigan topshiriq. Klient (QS Business, Android) tomoni **tayyor va sinovdan
> o'tgan**: Google ID token muvaffaqiyatli olinadi va `POST /auth/business/oauth/google` ga
> yuboriladi. Hozir o'sha so'rov **401** qaytaryapti va Google bilan kirgan foydalanuvchi
> o'zining mavjud (telefon bilan ro'yxatdan o'tgan) hisobiga tusha olmayapti.

---

## PROMPT BOSHLANISHI

### 0. Kirish ma'lumotlari

Ilova Google Sign-In (Android Credential Manager) orqali **ID token (JWT)** oladi va uni
backendga yuboradi. Authorization code oqimi ishlatilmaydi, shuning uchun **client secret
kerak emas** — uni so'ramang va hech qayerga yozmang.

```
Audience (Web OAuth client ID):
458901593049-5sbjrk75aipq5om52e6j9ulf4p5r0js3.apps.googleusercontent.com
```

Bu qiymat ilova ichida ham aynan shunday. Kelajakda iOS qo'shilsa yana bitta client ID
paydo bo'ladi, shuning uchun **audience'ni ro'yxat (array) sifatida saqlang**, bitta satr
sifatida emas.

---

### 1. Muammo — `POST /auth/business/oauth/google` 401 qaytaryapti

So'rov (klient aynan shuni yuboradi):

```jsonc
POST /auth/business/oauth/google
{
  "idToken": "<google id token (JWT)>",
  "deviceName": "SM-S931B",
  "platform": "android"
}
```

Kutilgan javob (`/auth/business/login` bilan bir xil shakl):

```jsonc
{ "accessToken": "<jwt>", "refreshToken": "<jwt>" }
```

> `accessToken` ichida `sub` (foydalanuvchi id) **bo'lishi shart** — klient uni o'qib lokal
> sessiyani yaratadi (`JwtClaims.subject`). `sub` yo'q bo'lsa kirish "Tokenda `sub` yo'q"
> xatosi bilan tugaydi.

Tekshirish (Node / `google-auth-library`):

```js
const { OAuth2Client } = require('google-auth-library');
const client = new OAuth2Client();

const ticket = await client.verifyIdToken({
  idToken,
  audience: ['458901593049-5sbjrk75aipq5om52e6j9ulf4p5r0js3.apps.googleusercontent.com'],
});
const { sub, email, email_verified, name, picture } = ticket.getPayload();
```

Qoidalar:
- `aud` — yuqoridagi ro'yxatdan biri
- `iss` — `accounts.google.com` yoki `https://accounts.google.com` (ikkalasi ham to'g'ri)
- `exp` — o'tmagan
- `email_verified === true` bo'lmasa — rad et (`401 GOOGLE_EMAIL_NOT_VERIFIED`)

401 qaytarayotgan bo'lsa, ehtimoliy sabab: `aud` boshqa client ID bilan solishtirilyapti
yoki endpoint hali tokenni umuman tekshirmayapti. Log'da `verifyIdToken` xatosini ko'rsating.

---

### 2. Muammo — Google bilan kirish har safar **yangi hisob** yaratyapti

Hozirgi xulq: foydalanuvchi Google bilan kiradi → bo'sh yangi hisob yaratiladi → biznes
yaratish uchun telefon raqami so'raladi → foydalanuvchi o'z raqamini kiritadi →
**"bunday hisob allaqachon bor"**. Ya'ni odam o'zining eski hisobiga kira olmaydi va yangi
hisobda ham qolib keta olmaydi — boshi berk ko'cha.

Kerakli algoritm (`POST /auth/business/oauth/google`):

```
1. Tokenni tekshir → { sub, email, name, picture }

2. googleSub bo'yicha qidir:
     topilsa                → shu hisobga kirgiz (tokenlarni qaytar). TAMOM.

3. email bo'yicha qidir (case-insensitive):
     topilsa                → googleSub ni SHU hisobga bog'lab qo'y (link),
                              keyin shu hisobga kirgiz. TAMOM.
                              // Yangi hisob YARATMA.

4. Hech narsa topilmasa   → yangi hisob yarat:
                              { googleSub, email, emailVerified: true, firstName/lastName,
                                avatarUrl, phoneNumber: null, phoneVerified: false }
                              va tokenlarni qaytar.
```

Qo'shimcha talab: `users.googleSub` ustuni **unique**, `users.email` ham **unique**
(case-insensitive). Bitta Google hisobi ikki foydalanuvchiga bog'lanmasin.

---

### 3. Yangi imkoniyat — raqamni OTP orqali bog'lash (merge)

Yuqoridagi 4-holatda (Google emaili bo'yicha eski hisob topilmagan, chunki eski hisob
faqat telefon bilan ochilgan) foydalanuvchi profilga kirib **o'z raqamini** kiritadi.
O'sha raqam boshqa (eski) hisobga tegishli bo'ladi.

Hozir bu holat `PUT /profile/me` da qattiq xato bilan tugaydi. Kerakli xulq — raqam
egasini **SMS kod bilan isbotlash** va ikkita hisobni birlashtirish.

#### 3.1 `PUT /profile/me` — yumshoq rad javobi

Raqam boshqa hisobga tegishli bo'lsa:

```jsonc
HTTP 409
{
  "code": "PHONE_TAKEN",
  "message": "Bu raqam boshqa hisobga bog'langan. Egasi ekaningizni SMS kod bilan tasdiqlang.",
  "canLinkViaOtp": true,
  "fields": { "phoneNumber": "Bu raqam band" }
}
```

> `fields` — klient uni aynan shu maydon ostida ko'rsatadi (`AppException.Validation.fields`),
> shuning uchun kalit so'rov tanasidagi maydon nomi bilan bir xil bo'lsin: `phoneNumber`.

#### 3.2 OTP so'rash — mavjud endpoint, o'zgarishsiz

```jsonc
POST /auth/business/otp/request        // Authorization: Bearer <accessToken>
{ "phoneNumber": "+998901234567" }
→ { "expiresInSeconds": 120, "resendCooldownSeconds": 60 }
```

Raqam boshqa hisobga tegishli bo'lsa ham **kod yuborilsin** (aynan shu holat uchun kerak).

#### 3.3 OTP tasdiqlash — merge shu yerda

```jsonc
POST /auth/business/otp/verify         // Authorization: Bearer <accessToken>
{ "phoneNumber": "+998901234567", "code": "123456" }
```

Kod to'g'ri bo'lsa:

| Holat | Nima qilinadi |
|---|---|
| Raqam hech kimga tegishli emas | Joriy hisobga yozib, `phoneVerified = true` qilinadi |
| Raqam **boshqa** hisobga tegishli | **Merge**: joriy (Google) hisobdagi `googleSub` + `email` eski hisobga ko'chiriladi, joriy bo'sh hisob o'chiriladi (yoki `mergedInto` bilan arxivlanadi), va **eski hisobning yangi tokenlari** qaytariladi |

Javob (ikkala holatda ham bir xil shakl):

```jsonc
{
  "verified": true,
  "merged": true,                    // merge bo'lgan-bo'lmagani
  "accessToken": "<jwt>",            // merged=true bo'lsa MAJBURIY (yangi egalik uchun)
  "refreshToken": "<jwt>"
}
```

> Klient hozir `{ "verified": true }` ni kutadi; `accessToken`/`refreshToken` maydonlari
> qo'shilsa, klient tomonida ularni saqlash va sessiyani almashtirish qo'shiladi (bizning
> ishimiz, sizdan faqat shu shakl kerak).

Merge qoidalari:
- E'lonlar, bizneslar, filiallar, sessiyalar — **eski hisobda qoladi** (u yerda haqiqiy ma'lumot bor)
- Google hisobida yaratilgan bo'sh biznes bo'lsa — o'chiriladi
- `email` faqat eski hisobda bo'sh bo'lsa ko'chiriladi; band bo'lsa — teginilmaydi
- Merge **idempotent** bo'lsin: takroriy so'rov ikkinchi marta hech narsa buzmasin
- Merge tranzaksiya ichida; xato bo'lsa to'liq qaytarilsin

Xavfsizlik:
- Kod noto'g'ri/muddati o'tgan → `400 OTP_INVALID`
- Bir raqamga 5 daqiqada 3 martadan ko'p urinish → `429 OTP_RATE_LIMITED`
- Merge faqat **kod to'g'ri kelganда** amalga oshsin; boshqa hech qanday yo'l bilan emas

---

### 4. Klient kutadigan xato kodlari

| HTTP | `code` | Qachon |
|---|---|---|
| 401 | `GOOGLE_TOKEN_INVALID` | `aud`/`iss`/`exp` mos emas |
| 401 | `GOOGLE_EMAIL_NOT_VERIFIED` | `email_verified != true` |
| 409 | `PHONE_TAKEN` | Raqam boshqa hisobda (+ `canLinkViaOtp: true`) |
| 400 | `OTP_INVALID` | Kod noto'g'ri yoki muddati o'tgan |
| 429 | `OTP_RATE_LIMITED` | Juda ko'p urinish |
| 403 | `PHONE_NOT_VERIFIED` | Tasdiqlanmagan raqam bilan biznes yaratish/tahrirlash |

Xato tanasi hamma joyda bir xil shaklda bo'lsin:
`{ "code": "...", "message": "<foydalanuvchiga ko'rsatiladigan matn>", "fields": { ... } }`

---

### 5. Qabul qilish mezonlari (test holatlari)

1. **Yangi foydalanuvchi, Google** → hisob yaratiladi, tokenlar qaytadi, `phoneNumber = null`
2. **O'sha foydalanuvchi ikkinchi marta Google bilan kiradi** → **yangi hisob yaratilmaydi**, o'sha hisobga tushadi
3. **Email bo'yicha mos hisob bor** → Google `sub` o'sha hisobga bog'lanadi, dublikat yaratilmaydi
4. **Google hisob + boshqa hisobning raqami** → `PUT /profile/me` → `409 PHONE_TAKEN`, so'ng OTP → `verified: true, merged: true` + yangi tokenlar; eski hisobning e'lonlari joyida
5. **Noto'g'ri OTP** → `400 OTP_INVALID`, merge bo'lmaydi
6. **Merge'dan keyin Google bilan qayta kirish** → to'g'ridan-to'g'ri birlashgan hisobga tushadi
7. **Buzilgan/eskirgan ID token** → `401 GOOGLE_TOKEN_INVALID`

## PROMPT TUGADI
