# Backend endpointlari — ustuvor checklist

> Manba: `dev/api-client-generator/elon-uz.json` (OpenAPI). Ilova klienti aynan shundan
> generatsiya qilingan — **path, method va DTO nomlari o'zgarmasligi kerak**, aks holda
> klientni qayta generatsiya qilish shart bo'ladi. To'liq sxema uchun spec'ga qarang.
>
> Bu hujjat — ilova **hozir chaqiradigan** endpointlar (1-daraja) uchun bajariladigan ro'yxat.
> Har bir DTO maydoni yonidagi `*` — majburiy (required).

- **Base URL:** `https://api.elon.uz/v1`
- **Format:** JSON (media yuklash — `multipart/form-data`)
- **Sana/vaqt:** ISO-8601 (`date-time`), millisekund emas

> ⚠️ **Javob formati — `BaseResponse` konvert (majburiy).** Quyida har endpoint yonida
> ko'rsatilgan DTO (masalan `UserProfileDto`) — bu konvertning **`result`** maydonidagi yuk.
> To'liq qoida: `BACKEND_PROMPT.md` §2. Qisqacha:
> - Muvaffaqiyat: `{"success":true,"status":200,"result":<DTO>,"error":null}`
> - Xato: `{"success":false,"status":404,"result":null,"error":{"code":"...","message":"<o'zbekcha>"}}`
> - Ro'yxat: `result: {"items":[...],"page":0,"size":20,"total":N,"hasNext":bool}`
> - `message` — doim o'zbek tilida; validatsiya (422) → `error.fields`.

---

## 0. Barcha endpointlarga umumiy (avval shuni qiling)

- [ ] **Firebase ID token tekshiruvi (middleware).** Kirish Firebase orqali — backend
      autentifikatsiya qilmaydi, faqat **tokenни tasdiqlaydi**. Har so'rovda:
      `Authorization: Bearer <Firebase ID token>` → **Firebase Admin SDK** `verifyIdToken()`.
      Token'dan `uid`, `phone_number`, `email` olinadi; token yaroqsiz/yo'q → **401**.
- [ ] **Egalik (ownership) tekshiruvi.** `uid` → foydalanuvchi. Biznes/e'lon/filial faqat
      egasига tegishli bo'lsa o'zgartirilsin (aks holda **403**).
- [ ] **Xato formati** — barcha endpointlarда bir xil JSON (masalan `{ "message": "...", "code": "..." }`).
- [ ] **CORS/rate-limit** — mos ravishда.

---

## 1. Profil (`ProfileApi`)

- [ ] **`GET /profile/me`** → `result: UserProfileDto` · **profil yo'q → HTTP `404` + `error.code: "PROFILE_NOT_FOUND"`**
  - ⚠️ Muhim: profil yaratilmagan bo'lsa **aniq HTTP 404** qaytaring (konvert bilan). Ilova 404'ni
    "yangi foydalanuvchi → ro'yxat" deb, boshqa xatoni "qayta urin" deb ajratadi.
- [ ] **`PUT /profile/me`** — body `UpdateProfileRequestDto` → `200 UserProfileDto`
  - Maydonlar: `firstName, lastName, phoneNumber, gender, role, universityId,
    universityEmail, birthYear, courseYear, avatarUrl` (barchasi ixtiyoriy — upsert).
  - `uid` mavjud bo'lmasa profil **yaratsin**, bo'lsa **yangilasин**.
- [ ] **`POST /profile/me/avatar`** — `multipart/form-data` (rasm) → `200 AvatarUploadResponseDto { avatarUrl* }`

## 2. Biznes (`BusinessApi`)

- [ ] **`GET /business/my`** → `200 [BusinessDto]` — joriy `uid` egasining bizneslari
- [ ] **`GET /business/{businessId}`** → `200 BusinessDto` (egalik tekshiruvi)
- [ ] **`POST /business`** — body `CreateBusinessRequestDto` → `201 BusinessDto`
  - Majburiy: `type*, name*, phone*`. Boshqa: `legalName, inn, description, logoUrl,
    coverUrl, contacts, isOnlineOnly`. Yangi biznes `status = DRAFT` bilan yaratilsин.
- [ ] **`PUT /business/{businessId}`** — body `UpdateBusinessRequestDto` → `200 BusinessDto`
- [ ] **`DELETE /business/{businessId}`** → `200` + `result:null` (arxivlash — hard delete emas, `status=ARCHIVED`)

## 3. Filiallar (`BranchesApi`)

- [ ] **`GET /business/{businessId}/branches`** → `200 [BranchDto]`
- [ ] **`POST /business/{businessId}/branches`** — body `BranchRequestDto` → `201 BranchDto`
  - Majburiy: `name*, location*` (LocationDto: lat/lng/manzil), `workingHours*`.
    Ixtiyoriy: `phone, deliveryZone, isActive`.
- [ ] **`PUT /business/{businessId}/branches/{branchId}`** — body `BranchRequestDto` → `200 BranchDto`
- [ ] **`DELETE /business/{businessId}/branches/{branchId}`** → `200` + `result:null`

## 4. Katalog (`BusinessApi` — turlar/kategoriyalar)

> Bu ikkovi **statik ma'lumot** (seed) — DB'ga oldindan to'ldirib qo'yiladi.

- [ ] **`GET /business/types`** — query `gender` (ixtiyoriy) → `200 [BusinessTypeInfoDto]`
  - `type*, nameUz*, nameRu, iconUrl, emoji, accentColor, defaultPriceUnit, priceUnits`
- [ ] **`GET /business/types/{type}/categories`** — path `type*`, query `gender` → `200 [CategoryDto]`
  - `key*, businessType*, nameUz*, nameRu, iconUrl, sortOrder, fields, requiresCustomName`

## 5. E'lon yaratish (`ListingsApi`) — ikki qadam

- [ ] **`POST /business/{businessId}/listings`** — body `CreateListingRequestDto` → `201 ListingDto`
  - `status = DRAFT` bilan yaratilsин. ⚠️ **`finalPrice` ni server hisoblaydi** — klient
    yubormaydi (`originalPrice` + `discount` dan hisoblang).
- [ ] **`POST /listings/{listingId}/submit`** → `200 ListingDto`
  - `DRAFT → PENDING_REVIEW`. (Moderatsiya oqimi 2-darajада.)

## 6. Media (`MediaApi`)

- [ ] **`POST /media/upload`** — `multipart/form-data` (rasm + `purpose` query, masalan `LISTING`)
      → `200 MediaUploadResponseDto { url*, thumbUrl, cardUrl }`

## 7. Geo (`GeoApi`)

> Ilovada **Nominatim (OSM) zaxirasi** bor — backend javob bermasa ishlaydi. Ammo backend
> `regionId`/`districtId` ni ham qaytargani uchun filial viloyat/tumanga avtomatik bog'lanadi.

- [ ] **`POST /geo/geocode`** — body `GeocodeRequestDto { query*, regionId }` → `200 [GeocodeResultDto]`
- [ ] **`POST /geo/reverse-geocode`** — body `ReverseGeocodeRequestDto { lat*, lng* }` → `200 ReverseGeocodeResponseDto`

## 8. Chegirma feed — talaba tomoni (`DiscountsApi`)

- [ ] **`GET /discounts`** → `200 DiscountPageDto { items*, page*, size*, total*, hasNext }`
  - Query (geo qidiruv): `lat, lng, radiusMeters, type, categoryKey, regionId, districtId,
    isOpenNow, hasDelivery, query, sort, page, size`.

---

## ✅ 1-daraja yakuni: **22 endpoint** — ilovaning hozirgi funksiyalari uchun shart.

---

## 2-daraja — spec'da bor, ilova hali chaqirmaydi (keyin)

Hozir e'lon **tahrirlash/pauza/o'chirish** ilovada local DB orqali ishlaydi; "to'liq" backend
uchun bularni keyin ulash kerak:

- `GET /business/{id}/listings` · `GET /listings/{id}` · `PUT /listings/{id}` · `DELETE /listings/{id}`
- `POST /listings/{id}/pause` · `/activate` · `/duplicate` · `/withdraw`
- `GET /listings/{id}/stats` · `/redemptions`
- `POST /listings/{id}/redeem/verify` · `/redeem/confirm` (kassir QR/promo)
- `POST /business/{id}/submit` (biznesни moderatsiyaga)
- `GET /business/types/{type}/attributes-schema` (dinamik `attributes` uchun JSON Schema)
- `GET /geo/regions` · `GET /geo/regions/{id}/districts`

## Ishlatilmaydi

- **`POST /auth/login`** — spec'da bor, lekin ilova **Firebase** bilan kiradi. Kerak emas.
- **Email-kod ro'yxat** (`requestEmailSignup`, `confirmEmailSignup`) — REST emas,
  **Firebase Cloud Functions** (`functions/`). Backend'да yaratilmaydi.
