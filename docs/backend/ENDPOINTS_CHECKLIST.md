# Backend endpointlari — ustuvor checklist

> Manba: `dev/api-client-generator/elon-uz.json` (OpenAPI). Ilova klienti aynan shundan
> generatsiya qilingan — **path, method va DTO nomlari o'zgarmasligi kerak**, aks holda
> klientni qayta generatsiya qilish shart bo'ladi. To'liq sxema uchun spec'ga qarang.
>
> Bu hujjat — ilova **hozir chaqiradigan** endpointlar (1-daraja) uchun bajariladigan ro'yxat.
> Har bir DTO maydoni yonidagi `*` — majburiy (required).

- **Base URL:** `https://api.studentclub.uz/v1`
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

- [x] ~~**Firebase ID token tekshiruvi (middleware).**~~ ⚠️ **Bekor qilindi.** Ilova Firebase'dan
      **voz kechdi** va backendning o'z JWT'sini (access + refresh) ishlatadi — bu
      `DISCOUNTS_BUSINESS_API_RESPONSE.md` §5.5 da ham qayd etilgan. Kirish
      `POST /auth/business/login` · `/register` · `/otp/*` · `/oauth/google` orqali;
      `Authorization: Bearer <access token>`, `TOKEN_EXPIRED` da `/auth/business/refresh`.
      Ilovadagi manba: `dev/feature/auth/data/ApiAuthRepository.kt`.
- [x] **Egalik (ownership) tekshiruvi.** Begona resurs uchun **404 emas, 403** qaytadi
      (`DISCOUNTS_BUSINESS_API_RESPONSE.md` §5.6) — ilova shunga moslangan.
- [x] **Xato formati** — `BaseResponse` konverti (pastga qarang); ilova uni
      `EnvelopeUnwrapPlugin` + `AppException` bilan bir joyда ochadi.
- [x] **Rate-limit** — `429` javob ilovada **alohida** tur (`AppException.LimitReached`),
      validatsiya xatosi emas: unda tuzatiladigan maydon yo'q. `error.code`
      (`RATE_LIMITED`, `LISTING_LIMIT_REACHED`) o'qiladi va foydalanuvchiga nima qilish
      kerakligi aytiladi (`LimitHint.kt`).

---

## 1. Profil (`ProfileApi`)

- [ ] **`GET /profile/me`** → `result: UserProfileDto` · **profil yo'q → HTTP `404` + `error.code: "PROFILE_NOT_FOUND"`**
  - ⚠️ Muhim: profil yaratilmagan bo'lsa **aniq HTTP 404** qaytaring (konvert bilan). Ilova 404'ni
    "yangi foydalanuvchi → ro'yxat" deb, boshqa xatoni "qayta urin" deb ajratadi.
- [ ] **`PUT /profile/me`** — body `UpdateProfileRequestDto` → `200 UserProfileDto`
  - Maydonlar: `firstName, lastName, phoneNumber, gender, role, universityId,
    universityEmail, birthYear, courseYear, avatarUrl` (barchasi ixtiyoriy — upsert).
  - `uid` mavjud bo'lmasa profil **yaratsin**, bo'lsa **yangilasин**.
  - [ ] ⚠️ **`email` maydoni yetishmayapti.** Ro'yxatdan o'tishning oxirgi qadamida ("Hisob
        yaratish" ekrani) foydalanuvchi ism-familiya bilan birga **aloqa emailini** kiritadi,
        lekin `UpdateProfileDto` da bunday maydon yo'q — `RegisterDto.email` esa ro'yxatning
        BIRINCHI qadamida, email hali so'ralmagan paytда ketadi. Hozircha email faqat
        ilovaning local keshida saqlanadi va serverga umuman bormaydi. Iltimos
        `UpdateProfileDto` va `UserProfileDto` ga `email` (nullable) qo'shing.
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

- [ ] **`POST /media/upload`** — `multipart/form-data` (rasm `file` + `purpose` — ikkalasi ham body maydoni, masalan `LISTING`)
      → `200 MediaUploadResponseDto { url*, thumbUrl, cardUrl }`

## 7. Geo (`GeoApi`)

> Ilovada **Nominatim (OSM) zaxirasi** bor — backend javob bermasa ishlaydi. Ammo backend
> `regionId`/`districtId` ni ham qaytargani uchun filial viloyat/tumanga avtomatik bog'lanadi.

- [ ] **`POST /geo/geocode`** — body `GeocodeRequestDto { query*, regionId }` → `200 [GeocodeResultDto]`
- [ ] **`POST /geo/reverse-geocode`** — body `ReverseGeocodeRequestDto { lat*, lng* }` → `200 ReverseGeocodeResponseDto`

## 8. Chegirma feed — talaba tomoni

> ⛔️ **`GET /discounts` qurilmaydi.** U bu yerda Level-1 sifatida sanalgan edi, lekin hech
> qachon implement qilinmagan va o'rnini `POST /v1/discounts/search` egalladi — spec:
> `docs/api/client/STUDENT_FEED.md` §2. `elon-uz.json` da yo'l `deprecated` deb belgilangan
> holda, tarix uchun qoladi.
>
> Sabab: GET query-param modeli feed filtrini ko'tara olmaydi (`attributes[]` operatorlar
> bilan, `bbox`, `attributesMatch`, id massivlari — id hech qachon URL'da bo'lmaydi), va ikki
> yo'lni saqlash bir ma'lumot ustida ikki sort lug'ati (4 va 9 qiymat) demakdir.

- [ ] **`POST /v1/discounts/search`** → `200 { items*, page*, size*, total*, hasNext }`
  - Tana: `mode` (LIST | MAP | COUNT) + `filter` + `sort` + `page`. To'liq model —
    `STUDENT_FEED.md` §4.
  - Talaba tomonining qolgan endpointlari (`/catalog/groups`, `/catalog/types`,
    `/catalog/filter-schema`, `/discounts/suggest`, `/discounts/detail`,
    `/discounts/favorites/*`) ham o'sha hujjatda — ular bu checklistning qamrovidan tashqarida.

---

## ✅ 1-daraja yakuni: **21 endpoint** (`GET /discounts` chiqarilgandan keyin) — ilovaning
hozirgi funksiyalari uchun shart. Talaba feed'i alohida yo'l xaritasi bo'yicha quriladi.

---

## 2-daraja — ✅ ULANDI

Ilgari e'lon **pauza/o'chirish** ilovada faqat local DB orqali ishlardi. Bu jiddiy muammo edi:
biznes egasi e'lonni "to'xtatdim" deb ko'rar, serverда esa u hamon `ACTIVE` bo'lib, talabalar
uni ko'rishда davom etardi. Endi hammasi backend orqali:

- [x] `GET /business/{id}/listings` · `PUT /listings/{id}` · `DELETE /listings/{id}`
- [x] `POST /listings/{id}/pause` · `/activate` · `/withdraw` · `/duplicate`
      — **qaytgan status keshga aynan yoziladi**, kutilgani emas: `activate` boshlanish
      sanasi kelajakda bo'lgan e'lonni `SCHEDULED` qiladi.
- [x] `GET /listings/{id}/stats` · `/redemptions` — e'lon kartasidagi "Statistika" oynasi.
- [x] `POST /listings/{id}/redeem/verify` · `/redeem/confirm` — kassir oqimi (ikki qadam:
      tekshirish hech narsani o'zgartirmaydi, tasdiqlash foydalanishni hisobga oladi).

### ✅ Qurildi (`DISCOUNTS_BUSINESS_API_RESPONSE.md`)

- [x] **`POST /business/{id}/submit`** → `BusinessDto`. `DRAFT | REJECTED → PENDING_REVIEW`;
      moderatsiya o'chirilgan bo'lsa darrov `APPROVED`.
- [x] **`GET /business/types/{type}/attributes-schema`** → `AttributesSchemaDto`
      (`{ businessType, common[], byCategory[] }`). JSON Schema emas — `AttributeFieldDto`,
      ya'ni `…/categories` qaytaradigan o'sha format.
- [x] **`GET /geo/regions`** · **`GET /geo/regions/{regionId}/districts`** — kontrakt yo'llari.
      Mavjud `/regions` va `/districts` ham ishlashda davom etadi (admin panel ularni chaqiradi).
- [x] **`GET /geo/metro-stations`** → `MetroStationDto[]` — Toshkent metrosi, 50 bekat / 4 liniya.
      ✅ **Spec'ga qo'shilgan** (yangi `business.json` da bor) va klientga generatsiya qilingan
      (`GeoApi.getMetroStations()`). Filial formasida (faqat Toshkent shahri) liniya bo'yicha
      guruhlangan tanlov sifatida ishlatiladi; `Branch.metroStation` erkin matn bo'lib qolgani
      uchun ro'yxat yuklanmasa maydon qo'lda yoziladi.
- [x] **Moderatsiya (admin panel):** `POST /admin/businesses/{id}/approve` · `/reject` va
      `POST /admin/listings/{id}/approve` · `/reject`. `MODERATION_ENABLED` bayrog'i bilan
      boshqariladi — hozir **o'chirilgan**, shuning uchun `submit` avvalgidek darrov chop etadi.
- [x] **§6.4 limitlar:** 5 biznes/foydalanuvchi · 100 faol e'lon/biznes · 50 `submit`/kun.
      (`POST /media/upload` 100/soat avvaldan bor edi.)

## Ishlatilmaydi

- **`POST /auth/login`** — spec'da bor, lekin ilova **Firebase** bilan kiradi. Kerak emas.
- **Email-kod ro'yxat** (`requestEmailSignup`, `confirmEmailSignup`) — REST emas,
  **Firebase Cloud Functions** (`functions/`). Backend'да yaratilmaydi.
