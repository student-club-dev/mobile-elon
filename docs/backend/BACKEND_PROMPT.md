# ElonUz — Node.js backend qurish uchun PROMPT

> Bu fayl **AI'ga (yoki dasturchiga) beriladigan topshiriq**. Pastdagi "PROMPT BOSHLANISHI"
> dan oxirigacha bo'lgan matnni to'liq nusxalab bering. Ikkita fayl ilova qilinadi:
> - `dev/api-client-generator/student-clubs.json` — OpenAPI 3.0.3 shartnoma (30 endpoint, 58 schema)
> - `docs/backend/catalog-seed.json` — katalog seed (9 tur, 95 kategoriya, atributlar)

---

## PROMPT BOSHLANISHI

Sen tajribali Node.js backend arxitektorisan. **ElonUz** uchun to'liq, ishlab chiqarishga tayyor
REST backend yoz. ElonUz — O'zbekistondagi talabalar uchun chegirmalar ilovasi (Kotlin
Multiplatform: Android + iOS). Biznes egalari o'z bizneslarini va e'lonlarini (chegirmali yoki
oddiy) joylaydi, talabalar ularni yaqinlik bo'yicha topadi va QR/promokod orqali ishlatadi.

Hozir ilova ma'lumotni **Firebase Firestore**'dan oladi va biznes turlari/kategoriyalari
klientда **qattiq kodlangan**. Vazifang — bularning **barchasini backendga ko'chirish**:
har bir ekrandagi har bir ma'lumot (turlar, kategoriyalar, atributlar, e'lonlar, bizneslar,
profil) shu backenddan kelsin.

### Kiritma fayllar (majburiy o'qi)

1. **`student-clubs.json`** — OpenAPI 3.0.3 shartnomasi. Bu **haqiqat manbai**. Barcha yo'llar,
   schema'lar, maydon nomlari, majburiylik va enum'lar aynan shu spec'ga mos bo'lishi shart.
   Klient (Ktor) allaqachon shu spec'dan generatsiya qilingan — **shartnomani buzma**.
2. **`catalog-seed.json`** — biznes turlari, kategoriyalar va atributlar katalogi. Migratsiya/seed
   orqali bazaga yukla va katalog endpoint'lari aynan shuni qaytarsin.

---

### 1. Texnologiya steki (qat'iy)

- **Node.js 20+** va **TypeScript** (strict mode)
- **Fastify** (yoki Express — lekin Fastify afzal: tezroq, schema validatsiyasi built-in)
- **PostgreSQL 16** + **Prisma ORM** (migratsiyalar bilan)
- **Zod** — so'rov validatsiyasi (har bir endpoint uchun)
- **firebase-admin** — ID token tekshirish (autentifikatsiya Firebase Auth'да qoladi)
- **Pino** — strukturali log; har so'rovга `traceId`
- **Vitest** + **Supertest** — testlar
- **Docker Compose** — postgres + app
- **PostGIS** yoki `earthdistance`/haversine — geo qidiruv uchun

---

### 2. ⚠️ ENG MUHIM: javob konverti (BaseResponse)

**Har bir** endpoint — muvaffaqiyat ham, xato ham — shu konvertда qaytadi. Istisno YO'Q.
Klient buni `dev/core/network/response/BaseResponse.kt` да shunday kutadi:

```ts
{
  "success": boolean,        // true = muvaffaqiyat
  "status": number,          // HTTP status aks-sadosi (200, 404, 422...)
  "code": string | null,     // mashina o'qiydigan kod: "TOKEN_EXPIRED", "LISTING_NOT_FOUND"
  "message": string | null,  // foydalanuvchiga ko'rsatiladigan matn (O'ZBEK tilida)
  "result": T | null,        // foydali yuk (payload)
  "data": T | null,          // ishlatma — faqat `result` ishlat (klient ikkalasini ham qabul qiladi)
  "error": {                 // xatoда to'ldiriladi, aks holda null
    "code": string,
    "message": string,
    "fields": { "<maydon>": "<xato matni>" }   // validatsiya xatolari
  } | null
}
```

Klientning muvaffaqiyat qoidasi (aynan shunday):
`isSuccessful = error == null && (status != null ? 200 <= status <= 299 : success)`

**Demak:**
- Muvaffaqiyat: `{"success":true,"status":200,"result":{...},"error":null}`
- Xato: `{"success":false,"status":404,"error":{"code":"LISTING_NOT_FOUND","message":"E'lon topilmadi"}}`
- Xatoда `result` **har doim null**, `error` **har doim to'ldirilgan**.
- `message` — **o'zbek tilida**, foydalanuvchiga ko'rsatish uchun.

Buni **global serializer + error handler** bilan amalga oshir — har controller'да qo'lda o'rash
mumkin emas. Validatsiya xatosi (422) `error.fields` ni to'ldirsin:
```json
{"success":false,"status":422,"error":{"code":"VALIDATION_ERROR","message":"Ma'lumotlar noto'g'ri",
 "fields":{"phone":"Telefon +998 bilan boshlanishi kerak","originalPrice":"Narx 0 dan katta bo'lsin"}}}
```

**Ro'yxat (paging) endpoint'lari** — `result` ичида:
```json
{"result": {"items": [...], "page": 0, "size": 20, "total": 137, "hasNext": true}}
```
(Aynan `size`/`hasNext` — generatsiya qilingan klient shuni kutadi, `pageSize`/`hasMore` EMAS.)

---

### 3. Autentifikatsiya

- Har bir so'rov: `Authorization: Bearer <firebase_id_token>` (istisno: `/auth/login`).
- `firebase-admin.auth().verifyIdToken(token)` bilan tekshir → `req.user = { uid, phone, email }`.
- Token muddati tugagan → **401** va `code: "TOKEN_EXPIRED"` (klient avtomatik yangilab, so'rovni
  qayta yuboradi — shu kod bo'lmasa qayta urinmaydi).
- Token yo'q/yaroqsiz → 401 `code: "UNAUTHORIZED"`, `message: "Qaytadan kiring"`.
- Foydalanuvchi birinchi so'rovда bazada yo'q bo'lsa — `users` jadvalida **avtomatik yarat**
  (uid = Firebase uid). Alohida ro'yxatdan o'tish endpoint'i kerak emas.
- **Egalik (ownership)**: biznes/e'lon ustidagi har qanday yozish amali `business.ownerUserId ==
  req.user.uid` bo'lgandagina ruxsat. Aks holda **403** `code: "FORBIDDEN"`,
  `message: "Bu biznes sizga tegishli emas"`. Boshqaning resursiga 404 emas, 403 qaytar.

---

### 4. Ma'lumotlar modeli (Prisma)

Spec'даgi schema'lardan kelib chiqib yoz. Asosiy jadvallar:

- **User**: `id` (Firebase uid, PK, string), `firstName?`, `lastName?`, `phoneNumber?` (E.164),
  `gender?` (MALE|FEMALE), `role?` (STUDENT|BUSINESS|EMPLOYER|UNIVERSITY), `universityId?`,
  `universityEmail?`, `birthYear?`, `courseYear?` (1|2|3|4|MASTER), `avatarUrl?`, `email?`,
  `createdAt`, `updatedAt`
- **Business**: `id` (cuid), `ownerUserId` → User, `type` (BusinessType, **yaratilgach
  o'zgarmaydi**), `name`, `phone` (E.164), `status` (BusinessStatus), `legalName?`, `inn?` (9 raqam),
  `description?`, `logoUrl?`, `coverUrl?`, `contacts?` (jsonb: telegram/instagram/website),
  `isOnlineOnly` (default false), `rejectionReason?`, `rating?`, `reviewsCount`, `listingsCount`,
  `createdAt`, `updatedAt`
- **Branch**: `id`, `businessId` → Business, `name`, `phone?`, `isActive` (default true),
  lokatsiya (`regionId`, `districtId`, `address`, `lat`, `lng`, `landmark?`, `entranceNote?`,
  `geohash?`, `mapUrl?`, `metroStation?`), `workingHours` (jsonb: [{day,isClosed,open?,close?}]),
  `deliveryZone?` (jsonb)
- **Listing**: `id`, `businessId` → Business, `categoryKey`, `customCategoryName?`, `title`,
  `description?`, `images` (string[]), `priceUnit`, `originalPrice` (BigInt), `currency`
  (default "UZS"), `discountType`, `discountValue` (BigInt), `discountConditions?`,
  `appliesToOptions`, `finalPrice` (BigInt, **server hisoblaydi**), redemption maydonlari
  (`method`, `promoCode?`, `url?`, `perUserLimit?`, `perUserPeriod?`, `totalLimit?`, `usedCount`),
  `attributes` (jsonb: Map<string,string>), `validFrom`, `validTo` (timestamptz),
  `status` (ListingStatus), `rejectionReason?`, `viewsCount`, `createdAt`, `updatedAt`
- **ListingBranch** — Listing↔Branch bog'lovchi (spec'да `branchIds`)
- **OptionGroup**: `id`, `listingId`, `name`, `selectionType`, `isRequired`, `minSelect?`,
  `maxSelect?`, `sortOrder` → **Option**: `id`, `groupId`, `name`, `priceDelta`, `isAvailable`, `sortOrder`
- **Redemption**: `id`, `listingId`, `studentUserId`, `branchId?`, `amount?`, `redeemedAt`
- **Katalog jadvallari** (seed'dan): **BusinessTypeInfo**, **Category**, **AttributeSpec**,
  **Region**, **District**

**Muhim:** `originalPrice`, `discountValue`, `finalPrice` — **butun son, so'm** (tiyin yo'q,
kasr yo'q). JSON'ga `number` sifatida ber (JS `BigInt` ni serializer'да `Number`ga o'gir).

**Sanalar:** REST API'да **ISO-8601** (`"2026-07-16T10:30:00Z"`) — generatsiya qilingan klient
`kotlinx.datetime.Instant` kutadi. (Eslatma: Firestore DTO'ларида epoch-ms ishlatilgan, lekin
REST uchun ISO-8601 — shartnoma shunday.)

---

### 5. Enum'lar (spec'даgi bilan bir xil, aynan shu qiymatlar)

```
BusinessType: GAME_CLUB, CLOTHING, CAFE_RESTAURANT, EDUCATION_CENTER,
              ENTERTAINMENT, BARBERSHOP, BEAUTY_SALON                  ← 7 ta
Gender: MALE, FEMALE
BusinessStatus: DRAFT, PENDING_REVIEW, APPROVED, REJECTED, BLOCKED
ListingStatus: DRAFT, PENDING_REVIEW, REJECTED, SCHEDULED, ACTIVE, PAUSED, EXPIRED, SOLD_OUT, ARCHIVED
PriceUnit: PER_ITEM, PER_HOUR, PER_KG, PER_MONTH, PER_COURSE, PER_LESSON, PER_TICKET, PER_PERSON, PER_SESSION
DiscountType: PERCENT, FIXED_AMOUNT, SPECIAL_PRICE, FREE_ITEM
RedemptionMethod: QR, PROMO_CODE, STUDENT_ID, ONLINE_LINK
RedemptionPeriod: DAY, WEEK, MONTH, TOTAL
SelectionType: SINGLE, MULTIPLE
ProfileRole: STUDENT, BUSINESS, EMPLOYER, UNIVERSITY
CourseYear: 1, 2, 3, 4, MASTER
DayOfWeek: MON, TUE, WED, THU, FRI, SAT, SUN
DiscountSort: DISTANCE, DISCOUNT_DESC, NEWEST, POPULAR
AttributeKind: TEXT, NUMBER, BOOLEAN, SELECT, TAGS
```

---

### 6. Endpoint'lar (30 ta — spec'даgi barchasi)

**Katalog (shaxsiylashtirilgan — jins bo'yicha):**
| Method | Path | Izoh |
|---|---|---|
| GET | `/business/types?gender=` | 9 tur. `gender=MALE` → BEAUTY_SALON chiqmaydi; `gender=FEMALE` → BARBERSHOP chiqmaydi; berilmasa — 9 ta |
| GET | `/business/types/{type}/categories?gender=` | Kategoriyalar. CLOTHING + `gender` → jinsga xos ro'yxat (`categoriesByGender`) |
| GET | `/business/types/{type}/attributes-schema` | Turга xos atributlar (dinamik forma uchun) |

**Biznes:** `POST /business` · `GET /business/my` · `GET|PUT|DELETE /business/{businessId}` ·
`POST /business/{businessId}/submit`
**Filiallar:** `GET|POST /business/{businessId}/branches` · `PUT|DELETE /business/{businessId}/branches/{branchId}`
**E'lonlar:** `GET|POST /business/{businessId}/listings` · `GET|PUT|DELETE /listings/{listingId}` ·
`POST /listings/{listingId}/{submit|withdraw|pause|activate|duplicate}` · `GET /listings/{listingId}/stats`
**Redemption:** `POST /listings/{listingId}/redeem/verify` · `POST /listings/{listingId}/redeem/confirm` ·
`GET /listings/{listingId}/redemptions`
**Talaba tomoni:** `GET /discounts?lat&lng&radiusMeters&type&categoryKey&regionId&districtId&isOpenNow&hasDelivery&query&sort&page&size`
**Profil:** `GET|PUT /profile/me` · `POST /profile/me/avatar`
**Geo:** `GET /geo/regions` · `GET /geo/regions/{regionId}/districts` · `POST /geo/geocode` · `POST /geo/reverse-geocode`
**Media:** `POST /media/upload` (multipart: `file` + `purpose=LOGO|COVER|LISTING`)
**Auth:** `POST /auth/login`
**Klublar:** `GET /clubs`

---

### 7. Biznes qoidalari (buzilmasin)

**`finalPrice` — har doim server hisoblaydi** (klient yubormaydi, yuborsa e'tiborsiz qoldir):
```
PERCENT       → finalPrice = originalPrice - (originalPrice * value / 100)   // value: 1..90
FIXED_AMOUNT  → finalPrice = max(0, originalPrice - value)                   // value: so'm
SPECIAL_PRICE → finalPrice = value                                           // value: yangi narx
FREE_ITEM     → finalPrice = originalPrice                                   // 1+1 aksiya
```

**Status o'tishlari** (boshqasi → 409 `code:"INVALID_STATUS_TRANSITION"`):
```
Listing:  yaratilganда → DRAFT
  submit:   DRAFT|REJECTED      → PENDING_REVIEW
  withdraw: PENDING_REVIEW      → DRAFT
  (moderator approve)           → ACTIVE (validFrom kelajakда bo'lsa → SCHEDULED)
  pause:    ACTIVE              → PAUSED
  activate: PAUSED              → ACTIVE
  DELETE:   har qanday          → ARCHIVED (fizik o'chirish YO'Q — soft delete)
  duplicate: EXPIRED|SOLD_OUT   → yangi DRAFT nusxa
Business: DRAFT → (submit) PENDING_REVIEW → APPROVED | REJECTED; BLOCKED — admin
```
**Cron/scheduler:** `validTo < now` → `EXPIRED`; `SCHEDULED` + `validFrom <= now` → `ACTIVE`;
`redemption.totalLimit` ga yetganда → `SOLD_OUT`.
**Biznes o'chirilsa** — uning e'lonlari ham `ARCHIVED` bo'lsin (kaskad).
**Faqat `ACTIVE`** e'lonlar `GET /discounts` да talabaga ko'rinadi.

**Validatsiya (klientdagi `ListingValidator` bilan bir xil):**
```
MAX_IMAGES = 5              MAX_PERCENT = 90            MAX_BRANCHES = 20
MAX_OPTION_GROUPS = 10      MAX_OPTIONS_PER_GROUP = 30  MIN_BRANCH_DISTANCE_METERS = 100
title: 3..120 belgi         originalPrice > 0           validTo > validFrom
koordinata: lat 37.0..46.0, lng 55.0..74.0  (O'zbekiston chegarasi — tashqarisi 422)
phone: E.164, +998 bilan, 9 raqam (+998901234567)
inn: aniq 9 raqam
promoCode: RedemptionMethod == PROMO_CODE bo'lsa MAJBURIY
customCategoryName: categoryKey == "OTHER" bo'lsa MAJBURIY
categoryKey: shu businessType katalogида mavjud bo'lishi shart (aks holda 422)
attributes: kalitlar shu turning attributes-schema'sида bo'lsin; required=true bo'lganlar majburiy
```

**Maxsus atribut kalitlari** (`catalog-seed.json` → `constants`):
- `_regular = "1"` → **oddiy e'lon** (chegirmasiz, bitta narx). Yo'q bo'lsa — chegirma e'loni.
- `_gender = "MALE"|"FEMALE"` → CLOTHING e'lonида kiyim jinsi.
- `_phone` → e'lonга xos aloqa raqami.
Bular `attributes` map'ида saqlanadi, alohida ustun kerak emas.

**`GET /discounts` (talaba qidiruvi)** — eng murakkab endpoint:
- `lat`/`lng` berilsa — har e'lonning **eng yaqin filiali** haversine bilan hisoblanadi →
  `nearestBranch: {id,name,address,landmark,lat,lng,distanceMeters,isOpenNow}`
- `radiusMeters` (default 5000) — undan uzoqlari chiqmaydi
- `isOpenNow` — filialning `workingHours` va **Toshkent vaqti (UTC+5)** bo'yicha
- `sort`: DISTANCE (yaqinlik) | DISCOUNT_DESC (chegirma %) | NEWEST | POPULAR (viewsCount)
- `query` — `title` bo'yicha (trigram/ILIKE)
- Faqat `status=ACTIVE` va `validFrom <= now <= validTo`
- Ishlash uchun geo-indeks (PostGIS `GEOGRAPHY` + GiST) ishlat

**Redemption:**
- `verify` — kodni tekshiradi, **hech narsani o'zgartirmaydi**: `{isValid, invalidReason?, student?, discount?}`
- `confirm` — **idempotent** bo'lsin; `usedCount++`, `perUserLimit`/`perUserPeriod` va `totalLimit`
  ni tekshir; limit tugasa 409 `code:"REDEMPTION_LIMIT_REACHED"`
- Ikkalasi ham faqat biznes egasiga ruxsat (talaba emas)

---

### 8. Xato kodlari (`error.code`)

```
UNAUTHORIZED (401) · TOKEN_EXPIRED (401) · FORBIDDEN (403)
BUSINESS_NOT_FOUND · LISTING_NOT_FOUND · BRANCH_NOT_FOUND · PROFILE_NOT_FOUND (404)
VALIDATION_ERROR (422, fields bilan) · INVALID_STATUS_TRANSITION (409)
REDEMPTION_LIMIT_REACHED (409) · REDEMPTION_INVALID_CODE (422)
BUSINESS_TYPE_IMMUTABLE (422) · CATEGORY_NOT_IN_CATALOG (422)
RATE_LIMITED (429) · INTERNAL_ERROR (500)
```
Har bir `message` — **o'zbek tilида** ("E'lon topilmadi", "Bu biznes sizga tegishli emas").

---

### 9. Yetkazib berish (deliverables)

```
src/
  app.ts                 # Fastify, plugin'lar, global error handler + envelope serializer
  config/env.ts          # Zod bilan tekshirilgan env
  plugins/{auth,logging,errors}.ts
  modules/
    business/{routes,service,schema}.ts
    listings/…  branches/…  catalog/…  discounts/…  profile/…
    redemptions/…  geo/…  media/…
  lib/{envelope,pricing,geo,status-machine}.ts
prisma/{schema.prisma, migrations/, seed.ts}   # seed → catalog-seed.json ni yuklaydi
test/…                   # har modul uchun
docker-compose.yml  Dockerfile  .env.example  README.md
```

**Shart:**
1. `docker compose up` → migratsiya + seed avtomatik, backend ishga tushadi.
2. `catalog-seed.json` seed orqali yuklanadi — katalog **kodда qattiq yozilmaydi**.
3. Har endpoint uchun Zod schema + integratsiya testi.
4. `README.md` — ishga tushirish, env, seed, `/discounts` misollari.
5. `student-clubs.json` bilan **shartnoma testi** — javob shakli spec'ga mos ekanini tekshirsin.

### 10. Qabul mezonlari (acceptance)

- [ ] **Har bir** javob (200 ham, 500 ham) `BaseResponse` konvertида
- [ ] `GET /business/types?gender=FEMALE` → 6 tur (BARBERSHOP yo'q); `gender=MALE` → 6 tur (BEAUTY_SALON yo'q); parametrsiz → 7
- [ ] `GET /business/types/CLOTHING/categories?gender=FEMALE` → DRESSES/SKIRTS/BLOUSES… (+ ALL, OTHER)
- [ ] `finalPrice` 4 ta `DiscountType` uchun ham to'g'ri hisoblanadi; klient yuborgan `finalPrice` e'tiborsiz
- [ ] Begona biznesga yozishga urinish → 403 `FORBIDDEN` (404 emas)
- [ ] Noto'g'ri status o'tishi → 409 `INVALID_STATUS_TRANSITION`
- [ ] `DELETE /listings/{id}` → `ARCHIVED` (bazadan o'chmaydi)
- [ ] `GET /discounts?lat&lng` → masofa bo'yicha saralangan, `nearestBranch.distanceMeters` to'g'ri
- [ ] Validatsiya xatosi → 422 + `error.fields` to'ldirilgan
- [ ] Muddati o'tgan token → 401 + `code:"TOKEN_EXPIRED"`
- [ ] `redeem/confirm` idempotent, limitlarni hurmat qiladi

**Boshla:** `prisma/schema.prisma` + envelope plugin + auth plugin'dan. Keyin katalog modulini
(seed bilan) yoz — qolgan hamma narsa shunga tayanadi. Har modулdan keyin testlarni ishga tushir.

## PROMPT TUGADI
