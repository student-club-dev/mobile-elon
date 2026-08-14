# Telegram bot — backend talabi

> Kim uchun: **backend jamoasi**. Bu hujjat ElonUz Telegram boti ishlashi uchun backendga
> qo'shilishi kerak bo'lgan **yagona yetishmayotgan qism** — hisobni bog'lash va sessiya —
> ni belgilaydi. Botning o'z rejasi: [`../TELEGRAM_BOT_PLAN.md`](../TELEGRAM_BOT_PLAN.md).
>
> - **Base URL:** `https://api.studentclub.uz/v1`
> - **Javob formati:** `BaseResponse` konverti — `API_RESPONSE_FORMAT.md` (o'zgarmaydi)
> - **Mavjud auth oilasi:** `POST /v1/auth/business/{login,register,refresh,oauth/google,…}`

---

## 0. Qisqacha: nima kerak

| # | Endpoint | Kim chaqiradi | Holati |
|---|---|---|---|
| 1 | `POST /auth/business/telegram/contact` | **bot** (xizmat) | ❌ yo'q |
| 2 | `POST /auth/business/telegram/link/start` | **ilova** (foydalanuvchi) | ❌ yo'q |
| 3 | `POST /auth/business/telegram/link/confirm` | **bot** (xizmat) | ❌ yo'q |
| 4 | `DELETE /auth/business/telegram/link` | **ilova** (foydalanuvchi) | ❌ yo'q |

Boshqa hech narsa kerak emas: biznes, filial, e'lon, media, geo, statistika va kassir
endpointlari **allaqachon bor** va bot ularni oddiy `Authorization: Bearer` bilan chaqiradi.

---

## 1. Nega mavjud `TELEGRAM_LOGIN.md` yetarli emas

`TELEGRAM_LOGIN.md` da tavsiflangan `POST /auth/business/oauth/telegram` — **Telegram Login
Widget** uchun. Widget veb-sahifada ishlaydi, domen ro'yxatdan o'tkaziladi va u qaytargan
`hash` foydalanuvchi ma'lumotini imzolaydi.

**Botda widget ham, domen ham yo'q.** Bot foydalanuvchini Telegram'ning o'zidan biladi
(`message.from.id`), lekin bu ma'lumot imzolanmagan — uni backend tekshira olmaydi. Shuning
uchun botga boshqa naqsh kerak: **bot backend uchun ishonchli xizmat** bo'ladi va so'rovni
xizmat kaliti bilan imzolaydi (§2).

O'sha widget endpointi kelajakda vebda kerak bo'lsa qoladi — bu hujjat unga tegmaydi.

---

## 2. Xizmat autentifikatsiyasi (MAJBURIY, birinchi qilinadigan ish)

Quyidagi endpointlar **parolsiz token beradi**. Ular ochiq internetdan chaqirilsa, har kim
istalgan hisobga kira oladi. Shu sabab:

- Bot va backend orasida **umumiy sir** bo'lsin: `X-Service-Token: <uzun tasodifiy satr>`.
  Sir faqat backend va bot serverida turadi, hech qachon klientga ketmaydi.
- Endpointlar **faqat** shu sarlavha bilan qabul qilinsin; aks holda `401`.
- Iloji bo'lsa bot serverining IP'si oq ro'yxatga olinsin yoki mTLS ishlatilsin.
- Bu endpointlar `/v1/auth/business/telegram/*` prefiksida — reverse proxy darajasida ham
  cheklash oson bo'lsin.

> Agar shu bo'lim bajarilmasa, qolgan hammasi **xavfsizlik teshigi**. Iltimos, buni
> endpointlardan oldin qiling.

---

## 3. Ma'lumotlar bazasi

Yangi jadval (yoki mavjud `oauth_identities` ga `TELEGRAM` provayderi sifatida qo'shish):

| Ustun | Turi | Izoh |
|---|---|---|
| `userId` | FK → users | Kimga bog'langan |
| `telegramId` | bigint, **unique** | Telegram foydalanuvchi id'si |
| `telegramUsername` | text, null | `@username` — faqat ko'rsatish uchun, o'zgarishi mumkin |
| `linkedAt` | timestamptz | Qachon bog'langan |

⚠️ **`telegramId` bo'yicha unique** shart: bitta Telegram hisobi bir vaqtda faqat bitta
ElonUz hisobiga bog'lansin. Aks holda bot qaysi hisob nomidan ishlashini bilmay qoladi.

---

## 4. Endpointlar

### 4.1. `POST /auth/business/telegram/contact` — raqam orqali kirish

Telegram'ning `request_contact` tugmasi foydalanuvchining **o'z raqamini** beradi va uni
Telegram tasdiqlaydi — ya'ni **SMS kerak emas**. (`TELEGRAM_LOGIN.md` oxiridagi "SMS'siz OTP"
talabi aynan shu bilan yopiladi.)

```
POST /v1/auth/business/telegram/contact
X-Service-Token: <xizmat siri>
Content-Type: application/json

{
  "telegramId":       123456789,
  "telegramUsername": "sherzod",        // ixtiyoriy
  "phone":            "+998901234567",  // Telegram bergan, tasdiqlangan raqam
  "firstName":        "Sherzod",        // ixtiyoriy — yangi hisob uchun
  "lastName":         "Axadov",         // ixtiyoriy
  "deviceName":       "Telegram bot",
  "platform":         "TELEGRAM"
}
```

**Mantiq:**

1. `telegramId` allaqachon bog'langan bo'lsa → o'sha `userId` ga token beriladi.
2. Bog'lanmagan bo'lsa, `phone` bo'yicha hisob qidiriladi:
   - **topildi** → `telegramId` shu hisobga bog'lanadi va token beriladi;
   - **topilmadi** → yangi biznes hisobi yaratiladi (raqam allaqachon tasdiqlangan, `otp`
     talab qilinmaydi) va token beriladi.
3. `telegramId` **boshqa** hisobga bog'langan bo'lsa → `409 TELEGRAM_ALREADY_LINKED`.

**Javob:**

```
200 → AuthTokensDto { accessToken, refreshToken }   // Google/Apple bilan bir xil
401 → xizmat siri noto'g'ri
409 → TELEGRAM_ALREADY_LINKED
422 → raqam formati noto'g'ri
```

> ⚠️ Bot tomonida tekshiriladi (backend ishonch qilishi mumkin, lekin bilib qo'ying):
> Telegram'da **begona kontaktni ham** yuborish mumkin. Bot faqat
> `contact.user_id == message.from.id` bo'lgan kontaktni qabul qiladi — ya'ni foydalanuvchi
> o'z raqamini yuborgan holatni. Bu shart botda amalga oshiriladi.

### 4.2. `POST /auth/business/telegram/link/start` — ilovadan bog'lash kodi

Google bilan kirgan hisoblarda raqam boshqa bo'lishi mumkin (yoki umuman yo'q). Bunday
foydalanuvchi Telegramni **ilova ichidan** bog'laydi.

```
POST /v1/auth/business/telegram/link/start
Authorization: Bearer <foydalanuvchi access token>

→ 200 { "code": "AB7K2Q", "expiresAt": "2026-08-14T12:05:00Z", "botUsername": "elonuz_bot" }
```

- `code` — qisqa (6 belgi), **bir martalik**, amal qilish muddati **5 daqiqa**.
- Ilova foydalanuvchiga `https://t.me/<botUsername>?start=<code>` havolasini ochib beradi.

### 4.3. `POST /auth/business/telegram/link/confirm` — bot kodni tasdiqlaydi

```
POST /v1/auth/business/telegram/link/confirm
X-Service-Token: <xizmat siri>

{ "code": "AB7K2Q", "telegramId": 123456789, "telegramUsername": "sherzod" }

→ 200 AuthTokensDto     // kod egasining hisobiga sessiya
→ 404 LINK_CODE_INVALID // topilmadi yoki muddati o'tdi (ikkisi bir xil javob — kod terib topilmasin)
→ 409 TELEGRAM_ALREADY_LINKED
```

Kod ishlatilgach **darhol o'chiriladi**.

### 4.4. `DELETE /auth/business/telegram/link` — bog'lashni bekor qilish

```
DELETE /v1/auth/business/telegram/link
Authorization: Bearer <foydalanuvchi access token>

→ 200, result: null
```

Bog'lash o'chiriladi va **shu Telegram sessiyasining refresh tokenlari bekor qilinadi** —
aks holda bot "chiqarib yuborildim" degan holatda ham ishlashda davom etardi.

---

## 5. Sessiya va ko'rinuvchanlik

Bot sessiyasi ham oddiy sessiya: `GET /v1/auth/business/sessions` ro'yxatida
`deviceName: "Telegram bot"`, `platform: "TELEGRAM"` bilan **ko'rinsin**. Shunda
foydalanuvchi uni ilovadan bekor qila oladi (`DELETE /sessions/{id}`).

Bu muhim: bot uzoq yashaydigan sessiya, va uni faqat bot ichidan boshqarish mumkin bo'lsa,
telefonini yo'qotgan odamda uni uzish yo'li qolmaydi.

---

## 6. Limitlar

Mavjud limitlar (5 biznes / foydalanuvchi, 100 faol e'lon / biznes, 50 `submit` / kun,
100 media / soat) **o'zgarmaydi** — bot ham xuddi shu foydalanuvchi nomidan ishlaydi va
`429` ni ilova bilan bir xil oladi.

Qo'shimcha ravishda kirish endpointlariga alohida limit qo'ying:

| Endpoint | Limit |
|---|---|
| `telegram/contact` | 5 / daqiqa / `telegramId` |
| `telegram/link/confirm` | 10 / daqiqa / `telegramId` |
| `telegram/link/start` | 3 / daqiqa / foydalanuvchi |

---

## 7. Ixtiyoriy (keyingi bosqich): OTP'ni Telegram orqali yuborish

Foydalanuvchi Telegramni bog'lagan bo'lsa, ro'yxat/tiklash kodini SMS o'rniga bot orqali
yuborish mumkin — SMS xarajati tushadi.

`POST /auth/business/otp/request` ga ixtiyoriy maydon:

```json
{ "phone": "+998901234567", "channel": "TELEGRAM" }   // default: "SMS"
```

Backend kodni bot orqali yuboradi (bot API'siga chaqiruv). Telegram bog'lanmagan bo'lsa —
jimgina SMS'ga qaytsin, xato qaytarmasin.

---

## 8. Qabul qilish mezoni

- [ ] `X-Service-Token` bo'lmagan so'rov `401` oladi (§2)
- [ ] Raqam bilan kirish: mavjud hisob topiladi, yangi hisob yaratiladi, ikkinchi marta
      kirishda **o'sha** hisob qaytadi
- [ ] Bir Telegram hisobi ikkinchi ElonUz hisobiga bog'lanmaydi (`409`)
- [ ] Bog'lash kodi 5 daqiqadan keyin ishlamaydi va bir marta ishlatiladi
- [ ] Bot sessiyasi `GET /sessions` da ko'rinadi va `DELETE /sessions/{id}` bilan uziladi
- [ ] `DELETE /telegram/link` dan keyin botdagi token ishlamaydi
