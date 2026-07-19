# Chegirmalar — Biznes egalari uchun e'lon yuklash (Backend spetsifikatsiyasi)

Bu hujjat Elon Uz super-app'ining **Chegirmalar** bo'limida biznes egalari
(`BUSINESS` roli) e'lon (offer) yuklashi uchun kerak bo'lgan backend modellari,
oqimlari va endpoint'larini tavsiflaydi.

Talaba tomoni (ko'rish, qidirish, chegirmani ishlatish) alohida hujjatda —
bu yerda faqat **biznes egasi nima yuklaydi va u qanday saqlanadi** yoritiladi.

Yakuniy API kontrakti `dev/api-client-generator/elon-uz.json` (OpenAPI v1) ga
qo'shiladi — u yagona manba, Kotlin klienti o'sha yerdan generatsiya qilinadi.

---

## 1. Umumiy tushunchalar

Uch qatlamli model — Yandex Eda mantiqi barcha biznes turlariga umumlashtirilgan:

```
Business (biznes)          — kafe / game club / do'kon ...
  └── Branch (filial)      — manzil, ish vaqti, telefon
  └── Category (bo'lim)    — "Pitsa", "PS5 zali", "IELTS kurslari" ...
        └── Listing (e'lon)— aynan bitta mahsulot/xizmat + student chegirmasi
              └── OptionGroup → Option (qo'shimchalar: hajm, ziravor, zal turi ...)
```

- **Business** — biznes egasining profili. Bitta foydalanuvchi bir nechta biznesga ega bo'lishi mumkin.
- **Branch** — filial. E'lon bir yoki bir nechta filialda amal qiladi.
- **Category** — biznes turiga bog'liq bo'lim. Tizim tayyor kategoriyalar ro'yxatini beradi
  (`GET /business/types/{type}/categories`), biznes egasi shundan tanlaydi yoki
  o'zining maxsus bo'limini qo'shadi (`customCategoryName`).
- **Listing** — e'lonning o'zi. **Har doim student chegirmasi bilan** (chegirmasiz e'lon bo'lmaydi —
  bu bo'limning butun mazmuni shu).
- **Option** — qo'shimcha tanlovlar (Yandex Eda'dagi "hajm: kichik/o'rta/katta" kabi).

---

## 2. Biznes turlari (`BusinessType`)

| Enum | Nomi (UI) | Nima yuklaydi | Narx birligi (odatiy) |
|---|---|---|---|
| `GAME_CLUB` | Game Club | Sessiya, zal, konsol vaqti | `PER_HOUR` |
| `GROCERY` | Oziq-ovqat | Mahsulot (dona/kg) | `PER_ITEM`, `PER_KG` |
| `CLOTHING` | Kiyim-kechak | Kiyim, poyabzal, aksessuar | `PER_ITEM` |
| `CAFE_RESTAURANT` | Kafe va Restoran | Taom, ichimlik, set | `PER_ITEM` |
| `EDUCATION_CENTER` | O'quv markazlar | Kurs, dars, master-klass | `PER_MONTH`, `PER_COURSE` |
| `ENTERTAINMENT` | Kino va ko'ngilochar joylar | Chipta, seans, kvest | `PER_TICKET`, `PER_PERSON` |
| `ELECTRONICS` | Texnikalar | Telefon, noutbuk, texnika | `PER_ITEM` |

Biznes turi **biznes yaratilganda tanlanadi va keyin o'zgarmaydi** — chunki kategoriyalar
va e'lonning maxsus maydonlari (`attributes`) shunga bog'liq.

---

## 3. Modellar

### 3.1 Business

```json
{
  "id": "biz_01H8X...",
  "ownerUserId": "firebase_uid",
  "type": "CAFE_RESTAURANT",
  "name": "Chaykhana Navruz",
  "legalName": "OOO Navruz Servis",
  "inn": "301234567",
  "description": "Milliy taomlar va choyxona. Talabalarga har kuni chegirma.",
  "logoUrl": "https://cdn.../logo.png",
  "coverUrl": "https://cdn.../cover.jpg",
  "phone": "+998901234567",
  "contacts": {
    "telegram": "@navruz_cafe",
    "instagram": "navruz.cafe",
    "website": "https://navruz.uz"
  },
  "status": "APPROVED",
  "rating": 4.6,
  "reviewsCount": 128,
  "listingsCount": 24,
  "createdAt": "2026-07-14T10:00:00Z"
}
```

| Maydon | Tur | Majburiy | Izoh |
|---|---|---|---|
| `id` | string | server | ULID/UUID |
| `ownerUserId` | string | server | Bearer token'dan olinadi |
| `type` | `BusinessType` | ✅ | O'zgarmas |
| `name` | string(2..80) | ✅ | |
| `legalName`, `inn` | string | ❌ | Moderatsiya uchun; `inn` — 9 raqam |
| `description` | string(0..1000) | ❌ | |
| `logoUrl`, `coverUrl` | string(url) | ❌ | `POST /media/upload` orqali |
| `phone` | string | ✅ | E.164 |
| `status` | `BusinessStatus` | server | pastda |
| `rating`, `reviewsCount` | number | server | Talabalar bahosidan |

**`BusinessStatus`:** `DRAFT` → `PENDING_REVIEW` → `APPROVED` \| `REJECTED` → (`BLOCKED`)

- `APPROVED` bo'lmaguncha biznes e'lonni **publish qila olmaydi** (draft yaratishi mumkin).
- `REJECTED` va `BLOCKED` da `rejectionReason` (string) qaytariladi.

### 3.2 Branch (filial)

```json
{
  "id": "br_01H8X...",
  "businessId": "biz_01H8X...",
  "name": "Chilonzor filiali",
  "location": {
    "regionId": "TOSHKENT_SHAHRI",
    "districtId": "CHILONZOR",
    "address": "Chilonzor 9-kvartal, 42-uy",
    "landmark": "Chilonzor metrosi yonida, Korzinka ro'parasida",
    "entranceNote": "Ikkinchi qavat, lift bilan",
    "lat": 41.2856,
    "lng": 69.2034,
    "geohash": "tskbdc7",
    "mapUrl": "https://yandex.uz/maps/-/CDf1IK",
    "metroStation": "Chilonzor"
  },
  "phone": "+998901234567",
  "workingHours": [
    { "day": "MON", "open": "09:00", "close": "23:00", "isClosed": false },
    { "day": "SUN", "open": null, "close": null, "isClosed": true }
  ],
  "deliveryZone": {
    "enabled": true,
    "radiusMeters": 5000,
    "minOrderAmount": 40000,
    "deliveryFee": 10000,
    "freeDeliveryFrom": 100000
  },
  "isActive": true
}
```

- `day`: `MON|TUE|WED|THU|FRI|SAT|SUN` — 7 tasi ham yuborilishi shart.
- `close` < `open` bo'lsa — tungacha ishlaydi deb qabul qilinadi (masalan `20:00`–`04:00`).
- Har bir biznesda **kamida 1 ta faol filial** bo'lishi kerak, aks holda e'lon publish qilinmaydi.
- Onlayn-only biznes uchun (masalan onlayn kurs) `isOnlineOnly: true` — u holda filial va `location` talab qilinmaydi.
- `location` — majburiy (onlayn-only'dan tashqari). Batafsil: **3.9-bo'lim**.

### 3.3 Category (bo'lim)

```json
{
  "id": "cat_pizza",
  "businessType": "CAFE_RESTAURANT",
  "key": "PIZZA",
  "nameUz": "Pitsa",
  "nameRu": "Пицца",
  "iconUrl": "https://cdn.../pizza.svg",
  "sortOrder": 3
}
```

Tizim kategoriyalari **tayyor ro'yxat** (4-bo'limda). Biznes egasi o'z bo'limini
qo'shsa — e'londa `customCategoryName` maydoni to'ldiriladi va moderator uni
tizim kategoriyasiga bog'lashi mumkin.

### 3.4 Listing (e'lon) — asosiy model

```json
{
  "id": "lst_01H8X...",
  "businessId": "biz_01H8X...",
  "branchIds": ["br_01H8X...", "br_01H8Y..."],
  "categoryKey": "PIZZA",
  "customCategoryName": null,

  "title": "Pepperoni pitsa (30 sm)",
  "description": "Mozzarella, pepperoni kolbasa, tomat sousi.",
  "images": ["https://cdn.../1.jpg", "https://cdn.../2.jpg"],

  "priceUnit": "PER_ITEM",
  "originalPrice": 55000,
  "currency": "UZS",

  "discount": {
    "type": "PERCENT",
    "value": 20,
    "finalPrice": 44000,
    "conditions": "Faqat talaba ID bilan, 12:00–17:00 oralig'ida"
  },

  "redemption": {
    "method": "QR",
    "promoCode": null,
    "perUserLimit": 1,
    "perUserPeriod": "DAY",
    "totalLimit": 200,
    "usedCount": 37
  },

  "validFrom": "2026-07-15T00:00:00Z",
  "validTo": "2026-09-01T00:00:00Z",

  "attributes": { "portionGrams": 550, "isHalal": true, "cookingMinutes": 20 },
  "optionGroups": [ /* 3.6 */ ],

  "status": "ACTIVE",
  "viewsCount": 1420,
  "createdAt": "2026-07-14T10:20:00Z",
  "updatedAt": "2026-07-14T10:20:00Z"
}
```

| Maydon | Tur | Majburiy | Qoida |
|---|---|---|---|
| `branchIds` | string[] | ✅* | Bo'sh bo'lsa — biznesning barcha faol filiallari. `isOnlineOnly` da bo'sh |
| `categoryKey` | string | ✅ | Biznes turiga tegishli bo'lishi shart |
| `customCategoryName` | string | ❌ | `categoryKey = "OTHER"` bo'lganda majburiy |
| `title` | string(3..120) | ✅ | |
| `description` | string(0..2000) | ❌ | |
| `images` | string[] | ✅ | 1..10 ta. Birinchisi — muqova |
| `originalPrice` | integer | ✅ | Tiyinsiz, butun so'm |
| `discount` | object | ✅ | pastda |
| `validFrom`, `validTo` | date-time | ✅ | `validTo > validFrom`, `validTo` max +1 yil |
| `attributes` | object | tur bo'yicha | 4-bo'limda har bir tur uchun |

### 3.5 Discount (chegirma)

```json
{ "type": "PERCENT", "value": 20, "finalPrice": 44000, "conditions": "..." }
```

| `type` | `value` ma'nosi | Hisoblash |
|---|---|---|
| `PERCENT` | 1..90 (foiz) | `finalPrice = originalPrice * (100 - value) / 100` |
| `FIXED_AMOUNT` | so'mda chegirma | `finalPrice = originalPrice - value` |
| `SPECIAL_PRICE` | yangi narx | `finalPrice = value` |
| `FREE_ITEM` | — | 1+1, `conditions` da tushuntiriladi. `finalPrice = originalPrice` |

**Backend qoidalari:**
- `finalPrice` ni **server hisoblaydi**, klientdan qabul qilinmaydi (kelsa — e'tiborsiz qoldiriladi).
- `finalPrice >= 0` va `finalPrice < originalPrice` (`FREE_ITEM` dan tashqari).
- `PERCENT` da `value > 90` → `422 DISCOUNT_TOO_HIGH` (firibgarlikdan himoya).

### 3.6 Redemption (chegirmani ishlatish usuli)

```json
{
  "method": "QR",
  "promoCode": "NAVRUZ20",
  "perUserLimit": 1,
  "perUserPeriod": "DAY",
  "totalLimit": 200,
  "usedCount": 37
}
```

| `method` | Talaba nima qiladi |
|---|---|
| `QR` | Ilovada QR chiqadi, kassir skanerlaydi (yoki aksincha) |
| `PROMO_CODE` | `promoCode` ni kassirga aytadi / saytga kiritadi |
| `STUDENT_ID` | Talaba ID/ilovadagi profilini ko'rsatadi — kod kerak emas |
| `ONLINE_LINK` | Tashqi havola (`redemption.url`) orqali |

- `perUserPeriod`: `DAY` \| `WEEK` \| `MONTH` \| `TOTAL`.
- `totalLimit = null` → cheksiz. `usedCount >= totalLimit` bo'lsa e'lon avtomatik `SOLD_OUT` ga o'tadi.

### 3.7 OptionGroup / Option (qo'shimchalar)

Yandex Eda'dagi "Hajmni tanlang" / "Qo'shimcha pishloq" mantiqidagi tuzilma.
Barcha biznes turlarida ishlaydi (Game Club'da "Zal turi", Kiyimda "O'lcham",
Texnikada "Xotira hajmi", Kinoda "Seans vaqti").

```json
{
  "id": "og_size",
  "name": "Hajmni tanlang",
  "selectionType": "SINGLE",
  "isRequired": true,
  "minSelect": 1,
  "maxSelect": 1,
  "options": [
    { "id": "op_30", "name": "30 sm",  "priceDelta": 0,     "isAvailable": true },
    { "id": "op_35", "name": "35 sm",  "priceDelta": 12000, "isAvailable": true },
    { "id": "op_40", "name": "40 sm",  "priceDelta": 25000, "isAvailable": false }
  ]
}
```

- `selectionType`: `SINGLE` (radio) \| `MULTIPLE` (checkbox).
- `priceDelta` — asosiy narxga qo'shiladigan (yoki manfiy — ayiriladigan) summa.
  **Chegirma `priceDelta` ga tarqalmaydi** — faqat `originalPrice` ga (`discount.appliesToOptions: false`, odatiy).
  Agar chegirma butun summaga tarqalishi kerak bo'lsa — `discount.appliesToOptions: true`.
- Bitta e'londa maksimal 10 ta guruh, har birida 30 tagacha variant.

### 3.8 ListingStatus (e'lon holati)

```
DRAFT ──submit──> PENDING_REVIEW ──approve──> ACTIVE ⇄ PAUSED
                        │                        │
                        └──reject──> REJECTED    ├── vaqt tugadi ──> EXPIRED
                                                 └── limit tugadi ──> SOLD_OUT
                                                              ARCHIVED (o'chirilgan)
```

| Status | Talabaga ko'rinadimi | Biznes tahrirlay oladimi |
|---|---|---|
| `DRAFT` | ❌ | ✅ |
| `PENDING_REVIEW` | ❌ | ❌ (avval `withdraw` qilinadi) |
| `REJECTED` | ❌ | ✅ (`rejectionReason` bilan) |
| `ACTIVE` | ✅ | ✅ (tahrirdan keyin qayta moderatsiya — 6.3-band) |
| `PAUSED` | ❌ | ✅ |
| `EXPIRED` / `SOLD_OUT` | ❌ (arxivda) | ✅ (nusxalab qayta chiqarish) |
| `ARCHIVED` | ❌ | ❌ |

### 3.9 Location (lokatsiya)

Lokatsiya **filialga** biriktiriladi, e'longa emas — e'lon `branchIds` orqali bir yoki
bir nechta manzilda amal qiladi. Talaba e'lonlar ro'yxatini ko'rganda backend uning
koordinatasiga eng yaqin filialni tanlab, masofani (`distanceMeters`) qaytaradi.

```json
{
  "regionId": "TOSHKENT_SHAHRI",
  "districtId": "CHILONZOR",
  "address": "Chilonzor 9-kvartal, 42-uy",
  "landmark": "Chilonzor metrosi yonida, Korzinka ro'parasida",
  "entranceNote": "Ikkinchi qavat, lift bilan",
  "lat": 41.2856,
  "lng": 69.2034,
  "geohash": "tskbdc7",
  "mapUrl": "https://yandex.uz/maps/-/CDf1IK",
  "metroStation": "Chilonzor"
}
```

| Maydon | Tur | Majburiy | Qoida |
|---|---|---|---|
| `regionId` | string | ✅ | `GET /geo/regions` dan (14 ta: 12 viloyat + Toshkent shahri + Qoraqalpog'iston) |
| `districtId` | string | ✅ | `GET /geo/regions/{id}/districts` dan. Viloyatga tegishli bo'lishi shart |
| `address` | string(5..200) | ✅ | Ko'cha, uy. Viloyat/tuman nomi takrorlanmaydi |
| `landmark` | string(0..200) | ❌ | Mo'ljal — talabalar uchun eng foydali maydon |
| `entranceNote` | string(0..120) | ❌ | Kirish, qavat, podez |
| `lat` | number | ✅ | 37.0..46.0 (O'zbekiston chegarasi) |
| `lng` | number | ✅ | 55.0..74.0 |
| `geohash` | string(7) | server | `lat/lng` dan hisoblanadi, indeks uchun |
| `mapUrl` | string(url) | ❌ | Yandex/Google Maps havolasi |
| `metroStation` | string | ❌ | Faqat Toshkent shahri uchun |

**Koordinata FAQAT xaritadan olinadi** — biznes egasi manzilni qo'lda yozmaydi.

Ilovada oqim shunday: e'lon formasidagi **"+"** → xarita ochiladi → egasi nuqtani bosadi →
koordinata olinadi → manzil **teskari geokodlash** bilan avtomatik to'ladi → filial ro'yxatga
qo'shiladi. "+" ni yana bosib **keyingi filial** qo'shiladi (bitta e'lon bir nechta manzilda
amal qiladi).

Xarita — **OpenStreetMap + Leaflet**, WebView ichida (`feature:discounts` → `map/MapPicker`).
Tekin, API kalit va hisob talab qilmaydi; Android va iOS'da bitta HTML ishlaydi. Teskari
geokodlash hozir **Nominatim** (OSM, tekin) orqali; backend tayyor bo'lganda
`POST /geo/reverse-geocode` (Yandex Geocoder proksisi) o'rniga qo'yiladi — u O'zbekiston
manzillarida aniqroq.

Geokodlash ishlamasa (internet yo'q) filial baribir qo'shiladi: manzil o'rniga koordinata
yoziladi va egasi uni tahrirlay oladi. Tanlangan nuqta yo'qolib qolmasligi kerak.

**Teskari geokodlash:** `POST /geo/reverse-geocode` — koordinatadan `regionId`,
`districtId` va taxminiy `address` ni to'ldiradi (forma avtomatik to'ladi).

**Delivery zone (yetkazib berish zonasi)** — `GROCERY` va `CAFE_RESTAURANT` uchun asosiy:

```json
{
  "enabled": true,
  "radiusMeters": 5000,
  "minOrderAmount": 40000,
  "deliveryFee": 10000,
  "freeDeliveryFrom": 100000,
  "polygon": null
}
```

- `radiusMeters` — filial atrofidagi doira (1000..30000). Oddiy va yetarli usul.
- `polygon` — murakkabroq zona kerak bo'lsa, GeoJSON `Polygon` (ixtiyoriy, v2 da).
  `polygon` berilsa `radiusMeters` e'tiborsiz qoldiriladi.
- Talaba manzili zonadan tashqarida bo'lsa, e'lon ro'yxatda `deliveryAvailable: false` bilan chiqadi.

---

## 4. Biznes turlari bo'yicha kategoriyalar va maxsus maydonlar

Har bir tur uchun: **kategoriyalar ro'yxati** (`categoryKey`) va **`attributes`** obyekti sxemasi.
`attributes` — turga bog'liq; backend uni tur bo'yicha JSON Schema bilan validatsiya qiladi.
Har bir turda `OTHER` kategoriyasi mavjud (u holda `customCategoryName` majburiy).

### 4.1 `GAME_CLUB` — Game Club

**Kategoriyalar:** `PS5`, `PS4`, `XBOX`, `PC_GAMING` (kiberklub), `VR`, `BILLIARDS` (bilyard),
`BOWLING_TABLE` (stol o'yinlari), `BOARD_GAMES`, `TOURNAMENT` (turnir), `OTHER`

```json
"attributes": {
  "hallType": "VIP",                     // STANDARD | VIP | PRIVATE_ROOM
  "seatsCount": 4,                       // nechta o'rin/joystik
  "deviceModel": "PlayStation 5 Slim",
  "sessionMinutes": 60,                  // bitta sessiya davomiyligi
  "gamesList": ["FIFA 25", "Mortal Kombat 1", "GTA V"],
  "hasNightPackage": true,               // tungi paket (masalan 22:00–08:00)
  "nightPackagePrice": 120000,
  "availableTimeSlots": ["10:00-14:00", "14:00-18:00"],  // ixtiyoriy
  "minPlayers": 1,
  "maxPlayers": 4,
  "hasSnacksIncluded": false
}
```
- `priceUnit`: `PER_HOUR` (odatiy) \| `PER_SESSION` \| `PER_PERSON`.
- OptionGroup misoli: "Zal turi" (Standart / VIP `+15 000`), "Qo'shimcha joystik".

### 4.2 `GROCERY` — Oziq-ovqat

**Kategoriyalar:** `BAKERY` (non va yopilgan mahsulotlar), `DAIRY` (sut mahsulotlari),
`MEAT_FISH` (go'sht va baliq), `FRUITS_VEGETABLES` (meva-sabzavot), `DRINKS` (ichimliklar),
`SWEETS` (shirinliklar, tortlar), `GROCERY_BASICS` (bakaleya: un, guruch, yog'),
`READY_MEALS` (tayyor ovqat), `SNACKS` (gazaklar), `OTHER`

```json
"attributes": {
  "brand": "Nestlé",
  "weightGrams": 500,          // yoki volumeMl
  "volumeMl": null,
  "packageType": "BOX",        // BOX | BOTTLE | PACK | WEIGHT (tarozida)
  "expiryDate": "2026-12-01",
  "storageCondition": "Salqin joyda, +2..+6 °C",
  "composition": "Un, shakar, sut kukuni, kakao",
  "isHalal": true,
  "hasDelivery": true,
  "minOrderAmount": 50000,     // yetkazib berish uchun minimal summa
  "stockCount": 40             // qoldiq (null = cheksiz)
}
```
- `priceUnit`: `PER_ITEM` \| `PER_KG`.

### 4.3 `CLOTHING` — Kiyim-kechak

**Kategoriyalar:** `MEN` (erkaklar), `WOMEN` (ayollar), `OUTERWEAR` (ustki kiyim),
`SHOES` (poyabzal), `SPORTSWEAR` (sport kiyim), `BAGS` (sumkalar),
`ACCESSORIES` (aksessuarlar), `UNDERWEAR`, `OTHER`

```json
"attributes": {
  "brand": "Zara",
  "gender": "UNISEX",                    // MALE | FEMALE | UNISEX | KIDS
  "sizes": ["S", "M", "L", "XL"],        // yoki poyabzal: ["39","40","41"]
  "sizeSystem": "INTERNATIONAL",         // INTERNATIONAL | EU | US | NUMERIC
  "colors": ["Qora", "Oq", "Bej"],
  "material": "100% paxta",
  "season": "SUMMER",                    // WINTER | SPRING | SUMMER | AUTUMN | ALL_SEASON
  "hasFittingRoom": true,                // kiyib ko'rish mumkinmi
  "returnDays": 14,                      // qaytarish muddati
  "stockCount": 12
}
```
- OptionGroup misoli: "O'lcham" (`SINGLE`, majburiy), "Rang" (`SINGLE`).

### 4.4 `CAFE_RESTAURANT` — Kafe va Restoran

**Kategoriyalar (menyu bo'limlari):** `NATIONAL` (milliy taomlar), `PIZZA`, `BURGER`,
`LAVASH_SHAWARMA`, `SUSHI`, `FAST_FOOD`, `SOUPS` (sho'rvalar), `SALADS` (salatlar),
`GRILL_BBQ` (kabob, grill), `BREAKFAST` (nonushta), `DESSERTS` (shirinliklar),
`HOT_DRINKS` (choy, kofe), `COLD_DRINKS` (sovuq ichimliklar), `COMBO_SETS` (setlar), `OTHER`

```json
"attributes": {
  "portionGrams": 550,
  "calories": 780,
  "ingredients": ["Mozzarella", "Pepperoni", "Tomat sousi"],
  "allergens": ["GLUTEN", "MILK"],       // GLUTEN|MILK|EGG|NUTS|SEAFOOD|SOY
  "spicyLevel": "MEDIUM",                // NONE | MILD | MEDIUM | HOT
  "isVegetarian": false,
  "isHalal": true,
  "cookingMinutes": 20,
  "servingType": ["DINE_IN", "TAKEAWAY", "DELIVERY"],
  "hasDelivery": true,
  "deliveryMinutes": 45,
  "minOrderAmount": 40000,
  "availableHours": "12:00-17:00"        // faqat shu oraliqda chegirma amal qiladi
}
```
- **Bu tur — asosiy namuna.** Menyu tuzilishi: `Category` (Pitsa) → `Listing` (Pepperoni) →
  `OptionGroup` (Hajm: 30/35/40 sm; Qo'shimchalar: pishloq, sous).
- Boshqa turlar ham xuddi shu tuzilmani ishlatadi — faqat kategoriya nomlari va
  `attributes` sxemasi farq qiladi.

### 4.5 `EDUCATION_CENTER` — O'quv markazlar

**Kategoriyalar:** `FOREIGN_LANGUAGES` (chet tillari), `IELTS_CEFR`, `IT_PROGRAMMING`,
`DESIGN` (grafik dizayn, UX/UI), `MATH_SCIENCE` (matematika va aniq fanlar),
`UNIVERSITY_PREP` (abituriyent tayyorlash), `BUSINESS_MARKETING`, `SOFT_SKILLS`,
`MASTER_CLASS` (bir martalik master-klass), `OTHER`

```json
"attributes": {
  "subject": "Ingliz tili — IELTS 6.5+",
  "level": "INTERMEDIATE",               // BEGINNER | ELEMENTARY | INTERMEDIATE | ADVANCED
  "format": "OFFLINE",                   // OFFLINE | ONLINE | HYBRID
  "groupType": "GROUP",                  // GROUP | INDIVIDUAL | MINI_GROUP
  "groupSize": 12,
  "durationMonths": 3,
  "lessonsPerWeek": 3,
  "lessonMinutes": 90,
  "teacherName": "Sardor Aliyev",
  "teacherBio": "IELTS 8.0, 5 yil tajriba",
  "startDate": "2026-08-01",
  "schedule": "Du, Chor, Ju — 18:00",
  "hasFreeTrialLesson": true,
  "hasCertificate": true,
  "hasInstallment": true,                // muddatli to'lov
  "seatsLeft": 4
}
```
- `priceUnit`: `PER_MONTH` (odatiy) \| `PER_COURSE` \| `PER_LESSON`.
- `SPECIAL_PRICE` chegirmasi bu yerda ko'p ishlatiladi (masalan: oyiga 500 000 → 350 000).

### 4.6 `ENTERTAINMENT` — Kino va ko'ngilochar joylar

**Kategoriyalar:** `CINEMA` (kino seans), `THEATER_CONCERT` (teatr, konsert),
`ESCAPE_ROOM` (kvest), `TRAMPOLINE_PARK` (batut park), `BOWLING`, `AQUAPARK`,
`AMUSEMENT_PARK` (attraksionlar), `MUSEUM_EXPO` (muzey, ko'rgazma),
`KARAOKE`, `SPORT_ACTIVITY` (paintball, kartlar), `OTHER`

```json
"attributes": {
  "eventTitle": "Dune: Part Three",
  "format": "IMAX_3D",                   // 2D | 3D | IMAX | IMAX_3D | 4DX | VR
  "language": "UZ",                      // UZ | RU | EN | ORIGINAL_SUB
  "durationMinutes": 155,
  "ageLimit": 16,                        // 0, 6, 12, 16, 18
  "sessionTimes": ["12:30", "16:00", "19:40"],
  "eventDate": "2026-07-20",             // bir martalik tadbir uchun
  "hallName": "IMAX zali",
  "seatsTotal": 180,
  "seatsLeft": 42,
  "minPersons": 2,                       // kvest uchun
  "maxPersons": 6,
  "includesEquipment": true,             // inventar narxga kiradimi
  "ticketType": "STANDARD"               // STANDARD | VIP | FAMILY | GROUP
}
```
- `priceUnit`: `PER_TICKET` (odatiy) \| `PER_PERSON` \| `PER_SESSION` (butun kvest guruhi uchun).
- Chegirma odatda ish kunlari yoki muayyan seanslarga — `discount.conditions` va `availableHours` da.

### 4.7 `ELECTRONICS` — Texnikalar

**Kategoriyalar:** `PHONES` (telefonlar), `LAPTOPS` (noutbuklar), `TABLETS` (planshetlar),
`AUDIO` (quloqchin, kolonka), `WEARABLES` (smart-soat, bilaguzuk),
`GAMING_GEAR` (gaming qurilmalar), `HOME_APPLIANCES` (maishiy texnika),
`ACCESSORIES` (aksessuar, kabel, chexol), `COMPONENTS` (ehtiyot qismlar), `OTHER`

```json
"attributes": {
  "brand": "Apple",
  "model": "MacBook Air M3 13\"",
  "condition": "NEW",                    // NEW | REFURBISHED | USED
  "warrantyMonths": 12,
  "color": "Midnight",
  "specs": {                             // erkin key-value, 20 tagacha
    "Protsessor": "Apple M3",
    "Operativ xotira": "16 GB",
    "Xotira": "512 GB SSD",
    "Ekran": "13.6\" Liquid Retina"
  },
  "hasInstallment": true,                // muddatli to'lov
  "installmentMonths": [6, 12, 24],
  "installmentMonthlyPrice": 1250000,    // 12 oy uchun
  "isOfficialSeller": true,
  "stockCount": 3
}
```
- OptionGroup misoli: "Xotira hajmi" (256 GB `+0`, 512 GB `+2 000 000`), "Rang".

---

## 5. API endpoint'lari (biznes egasi)

Barcha so'rovlarda: `Authorization: Bearer <Firebase ID token>`.
Foydalanuvchi roli `BUSINESS` bo'lishi shart, aks holda `403 FORBIDDEN_ROLE`.

### 5.1 Ma'lumotnoma (reference)

| Metod | Yo'l | Tavsif |
|---|---|---|
| `GET` | `/business/types` | 7 ta biznes turi + ularning narx birliklari |
| `GET` | `/business/types/{type}/categories` | Tur bo'yicha kategoriyalar ro'yxati |
| `GET` | `/business/types/{type}/attributes-schema` | `attributes` uchun JSON Schema (dinamik forma qurish uchun) |
| `GET` | `/geo/regions` | Viloyatlar (14 ta) |
| `GET` | `/geo/regions/{regionId}/districts` | Tumanlar |
| `GET` | `/geo/metro-stations` | Metro bekatlari (Toshkent) |

> `attributes-schema` — klient (Compose) formani **hardcode qilmasdan** quradi.
> Yangi maydon qo'shilsa, ilovani yangilamasdan ishlaydi.

### 5.2 Biznes profili

| Metod | Yo'l | Tavsif |
|---|---|---|
| `POST` | `/business` | Biznes yaratish (`status = DRAFT`) |
| `GET` | `/business/my` | Egasining biznesalari ro'yxati |
| `GET` | `/business/{id}` | Bitta biznes |
| `PUT` | `/business/{id}` | Tahrirlash (`APPROVED` da — qayta moderatsiya) |
| `POST` | `/business/{id}/submit` | Moderatsiyaga yuborish → `PENDING_REVIEW` |
| `DELETE` | `/business/{id}` | Arxivlash (e'lonlari `ARCHIVED` bo'ladi) |

### 5.3 Filiallar

| Metod | Yo'l | Tavsif |
|---|---|---|
| `GET` | `/business/{id}/branches` | Ro'yxat |
| `POST` | `/business/{id}/branches` | Qo'shish |
| `PUT` | `/business/{id}/branches/{branchId}` | Tahrirlash |
| `DELETE` | `/business/{id}/branches/{branchId}` | O'chirish (oxirgi faol filialni o'chirib bo'lmaydi) |

### 5.4 E'lonlar

| Metod | Yo'l | Tavsif |
|---|---|---|
| `POST` | `/business/{id}/listings` | E'lon yaratish (`status = DRAFT`) |
| `GET` | `/business/{id}/listings` | Ro'yxat. Filtr: `?status=&categoryKey=&page=&size=` |
| `GET` | `/listings/{listingId}` | Bitta e'lon |
| `PUT` | `/listings/{listingId}` | Tahrirlash |
| `POST` | `/listings/{listingId}/submit` | Moderatsiyaga → `PENDING_REVIEW` |
| `POST` | `/listings/{listingId}/withdraw` | Moderatsiyadan qaytarib olish → `DRAFT` |
| `POST` | `/listings/{listingId}/pause` | `ACTIVE` → `PAUSED` |
| `POST` | `/listings/{listingId}/activate` | `PAUSED` → `ACTIVE` |
| `POST` | `/listings/{listingId}/duplicate` | Nusxa (`EXPIRED` e'lonni qayta chiqarish uchun) |
| `DELETE` | `/listings/{listingId}` | `ARCHIVED` |
| `GET` | `/listings/{listingId}/stats` | Ko'rishlar, ishlatilishlar, konversiya |
| `GET` | `/listings/{listingId}/redemptions` | Kim, qachon ishlatgani (sahifalangan) |

### 5.5 Media

| Metod | Yo'l | Tavsif |
|---|---|---|
| `POST` | `/media/upload` | `multipart/form-data`, `file` + `purpose` (`LOGO`\|`COVER`\|`LISTING`) → `{ "url": "..." }` |

- Ruxsat etilgan: `image/jpeg`, `image/png`, `image/webp`. Maks **5 MB**, min `600×600`.
- Server WebP ga o'giradi va 3 o'lchamda saqlaydi (`thumb` 200px, `card` 800px, `full` 1600px).

### 5.6 Chegirmani tasdiqlash (kassir tomoni)

| Metod | Yo'l | Tavsif |
|---|---|---|
| `POST` | `/listings/{listingId}/redeem/verify` | QR/promokodni tekshirish. Body: `{ "code": "..." }` |
| `POST` | `/listings/{listingId}/redeem/confirm` | Ishlatilgan deb belgilash. Body: `{ "code": "...", "amount": 44000 }` |

- `verify` → `{ "isValid": true, "student": { "fullName": "...", "university": "..." }, "discount": {...} }`
- `confirm` → `usedCount++`, talaba tarixiga yoziladi. Bir kod ikki marta ishlatilsa → `409 ALREADY_REDEEMED`.

### 5.7 Lokatsiya (geokodlash)

| Metod | Yo'l | Tavsif |
|---|---|---|
| `POST` | `/geo/geocode` | Manzil matnidan koordinata. Body: `{ "query": "Chilonzor 9-kvartal 42" }` |
| `POST` | `/geo/reverse-geocode` | Koordinatadan manzil. Body: `{ "lat": 41.2856, "lng": 69.2034 }` |

**`geocode` javobi** — bir nechta variant, ilova xaritada tasdiqlatadi:

```json
{
  "results": [
    {
      "lat": 41.2856, "lng": 69.2034,
      "regionId": "TOSHKENT_SHAHRI", "districtId": "CHILONZOR",
      "formattedAddress": "Chilonzor 9-kvartal, 42-uy",
      "confidence": 0.92
    }
  ]
}
```

**`reverse-geocode` javobi** — filial formasini avtomatik to'ldirish uchun:

```json
{
  "regionId": "TOSHKENT_SHAHRI",
  "districtId": "CHILONZOR",
  "address": "Chilonzor 9-kvartal, 42-uy",
  "nearestMetro": "Chilonzor"
}
```

> Provayder: Yandex Geocoder API (O'zbekiston manzillari uchun eng aniq).
> Backend uni proksi qiladi — API kalit klientga chiqmaydi. Natijalar 30 kun keshlanadi.

### 5.8 Talaba tomoni — geo-filtr (ma'lumot uchun)

Bu endpoint biznes egasi uchun emas, lekin lokatsiya modeli aynan shuning uchun kerak:

```
GET /discounts?lat=41.29&lng=69.20&radiusMeters=3000&type=CAFE_RESTAURANT&sort=DISTANCE
```

Har bir e'lon javobda eng yaqin filial va masofa bilan qaytadi:

```json
{
  "id": "lst_01H8XZ",
  "title": "Pepperoni pitsa",
  "nearestBranch": {
    "id": "br_01H8X",
    "name": "Chilonzor filiali",
    "address": "Chilonzor 9-kvartal, 42-uy",
    "distanceMeters": 640,
    "isOpenNow": true
  },
  "deliveryAvailable": true
}
```

- `sort`: `DISTANCE` \| `DISCOUNT_DESC` \| `NEWEST` \| `POPULAR`.
- Filtrlar: `regionId`, `districtId`, `radiusMeters`, `isOpenNow`, `hasDelivery`.
- `lat/lng` berilmasa — `regionId` bo'yicha filtr, masofa `null`.

**Masofa qanday hisoblanadi:** e'lonning **barcha filiallari** ichidan talabaga eng yaqini
tanlanadi va faqat o'sha ko'rsatiladi ("📍 640 m · Chilonzor filiali"). Serverda buni PostGIS
`ST_Distance` qiladi (§9); ilovada esa xuddi shu narsa haversine formulasi bilan hisoblanadi
(`Geo.distanceMeters`) — shu sabab backend yo'q bo'lganda ham masofa to'g'ri chiqadi.
Joylashuvga ruxsat berilmasa masofa ko'rsatilmaydi, lekin ro'yxat baribir ishlaydi.

---

## 6. Biznes-qoidalar

### 6.1 E'lon publish qilish shartlari

`submit` chaqirilganda quyidagilar tekshiriladi, biror biri buzilsa `422`:

1. Biznes `status = APPROVED`.
2. Kamida 1 ta faol filial mavjud va uning `location` i to'liq (`regionId`, `districtId`,
   `address`, `lat`, `lng`) — yoki `isOnlineOnly = true`.
3. `images` da kamida 1 ta rasm.
4. `discount.finalPrice < originalPrice`.
5. `validTo` kelajakda.
6. `categoryKey` biznes turiga tegishli.
7. `attributes` shu turning JSON Schema'siga mos.
8. Faol (`ACTIVE` + `PENDING_REVIEW`) e'lonlar soni limitdan oshmagan (6.4).

### 6.2 Moderatsiya

- Moderator `PENDING_REVIEW` dagi e'lonni `APPROVED` yoki `REJECTED` qiladi.
- SLA: **24 soat**. O'tib ketsa e'lon avtomatik tasdiqlanmaydi (xavfsizlik uchun), lekin
  biznesga push yuboriladi va moderatorga eskalatsiya bo'ladi.
- Rad etish sabablari (`rejectionReason`): `FAKE_DISCOUNT` (narx sun'iy oshirilgan),
  `POOR_IMAGE`, `PROHIBITED_CONTENT`, `WRONG_CATEGORY`, `INCOMPLETE_INFO`, `OTHER` + izoh.
- Taqiqlangan: alkogol, tamaki, qimor, retsept bo'yicha dorilar, 18+ kontent.

### 6.3 Tahrirlash va qayta moderatsiya

`ACTIVE` e'lon tahrirlanganda:
- **Qayta moderatsiya talab qilinadi** (`ACTIVE` → `PENDING_REVIEW`), agar o'zgargan bo'lsa:
  `title`, `description`, `images`, `discount`, `originalPrice`, `categoryKey`.
- **Talab qilinmaydi** (darhol saqlanadi), agar faqat shular o'zgarsa:
  `branchIds`, `validTo` (uzaytirish), `redemption.totalLimit`, `optionGroups[].isAvailable`,
  `attributes.stockCount`, `attributes.seatsLeft`.

### 6.4 Limitlar

| Nima | Limit |
|---|---|
| Bir foydalanuvchidagi biznes | 5 |
| Bir biznesdagi filial | 50 |
| Bir biznesdagi faol e'lon | 100 (`ACTIVE` + `PENDING_REVIEW`) |
| Bir e'londagi rasm | 10 |
| Bir e'londagi OptionGroup | 10, har birida 30 variant |
| Kuniga `submit` | 50 |
| `POST /media/upload` | 100/soat |

### 6.5 Avtomatik statuslar (cron, har 5 daqiqada)

- `validTo < now` → `EXPIRED`.
- `usedCount >= totalLimit` → `SOLD_OUT`.
- `validFrom > now` bo'lgan `APPROVED` e'lon → `SCHEDULED`, vaqti kelganda `ACTIVE`.

### 6.6 Lokatsiya qoidalari

1. **Koordinata O'zbekiston chegarasida** bo'lishi shart (`lat` 37..46, `lng` 55..74),
   aks holda `422 LOCATION_OUT_OF_BOUNDS`.
2. **`districtId` `regionId` ga tegishli** bo'lishi shart → `422 DISTRICT_REGION_MISMATCH`.
3. **Koordinata va tuman mos kelishi** tekshiriladi: `lat/lng` tanlangan tuman
   chegarasidan **10 km dan uzoq** bo'lsa — `422 LOCATION_DISTRICT_MISMATCH`.
   Bu "Toshkentda" deb yozib, nuqtani Samarqandga qo'yishning oldini oladi.
4. **Dublikat filial:** bitta biznesda 100 metr radiusda ikkita filial bo'lmaydi → `409 DUPLICATE_BRANCH_LOCATION`.
5. **Moderatsiya:** filial manzili o'zgarsa (`lat`/`lng`/`address`) — biznes qayta moderatsiyaga
   tushmaydi, lekin moderatorga signal boradi (firibgarlik monitoringi uchun).
6. **Maxfiylik:** talabaning `lat/lng` si **saqlanmaydi** — faqat so'rov paytida masofa
   hisoblash uchun ishlatiladi. Log'larga ham yozilmaydi.
7. **`geohash`** ni server hisoblaydi (7 belgi ≈ 150 m aniqlik), klientdan qabul qilinmaydi.
8. `deliveryZone.enabled = true` bo'lsa, `radiusMeters` majburiy (1000..30000).

---

## 7. Xatolik formati va kodlari

```json
{
  "code": "DISCOUNT_TOO_HIGH",
  "message": "Chegirma 90% dan oshmasligi kerak",
  "field": "discount.value",
  "traceId": "01H8X..."
}
```

| HTTP | `code` | Sabab |
|---|---|---|
| 400 | `VALIDATION_ERROR` | Maydon formati noto'g'ri |
| 401 | `UNAUTHORIZED` | Token yaroqsiz / muddati o'tgan |
| 403 | `FORBIDDEN_ROLE` | Roli `BUSINESS` emas |
| 403 | `NOT_BUSINESS_OWNER` | Boshqa biznesning e'loniga tegmoqchi |
| 403 | `BUSINESS_NOT_APPROVED` | Biznes hali tasdiqlanmagan |
| 404 | `BUSINESS_NOT_FOUND` / `LISTING_NOT_FOUND` | — |
| 409 | `INVALID_STATUS_TRANSITION` | Masalan `DRAFT` ni `pause` qilish |
| 409 | `ALREADY_REDEEMED` | Kod allaqachon ishlatilgan |
| 413 | `FILE_TOO_LARGE` | Rasm 5 MB dan katta |
| 422 | `DISCOUNT_TOO_HIGH` | `PERCENT > 90` |
| 422 | `FINAL_PRICE_INVALID` | `finalPrice >= originalPrice` |
| 422 | `INVALID_CATEGORY_FOR_TYPE` | Kategoriya biznes turiga mos emas |
| 422 | `ATTRIBUTES_SCHEMA_MISMATCH` | `attributes` sxemaga mos emas |
| 422 | `NO_ACTIVE_BRANCH` | Faol filial yo'q |
| 422 | `LOCATION_OUT_OF_BOUNDS` | Koordinata O'zbekiston chegarasidan tashqarida |
| 422 | `DISTRICT_REGION_MISMATCH` | Tuman tanlangan viloyatga tegishli emas |
| 422 | `LOCATION_DISTRICT_MISMATCH` | Nuqta tanlangan tumandan 10 km dan uzoq |
| 409 | `DUPLICATE_BRANCH_LOCATION` | 100 m radiusda shu biznesning filiali bor |
| 429 | `RATE_LIMIT_EXCEEDED` | Limit oshdi |

---

## 8. To'liq misol — kafe e'loni yaratish

**So'rov:** `POST /business/biz_01H8X/listings`

```json
{
  "branchIds": ["br_01H8X"],
  "categoryKey": "PIZZA",
  "title": "Pepperoni pitsa",
  "description": "Mozzarella, pepperoni kolbasa, firma sousi. O'tin pechida pishiriladi.",
  "images": ["https://cdn.elon.uz/l/abc1.webp"],
  "priceUnit": "PER_ITEM",
  "originalPrice": 55000,
  "currency": "UZS",
  "discount": {
    "type": "PERCENT",
    "value": 20,
    "conditions": "Faqat talaba ID bilan, dush–juma 12:00–17:00",
    "appliesToOptions": false
  },
  "redemption": {
    "method": "QR",
    "perUserLimit": 1,
    "perUserPeriod": "DAY",
    "totalLimit": 200
  },
  "validFrom": "2026-07-15T00:00:00Z",
  "validTo": "2026-09-01T00:00:00Z",
  "attributes": {
    "portionGrams": 550,
    "ingredients": ["Mozzarella", "Pepperoni", "Tomat sousi"],
    "allergens": ["GLUTEN", "MILK"],
    "spicyLevel": "MILD",
    "isHalal": true,
    "cookingMinutes": 20,
    "servingType": ["DINE_IN", "TAKEAWAY", "DELIVERY"],
    "hasDelivery": true,
    "deliveryMinutes": 45,
    "availableHours": "12:00-17:00"
  },
  "optionGroups": [
    {
      "name": "Hajmni tanlang",
      "selectionType": "SINGLE",
      "isRequired": true,
      "options": [
        { "name": "30 sm", "priceDelta": 0 },
        { "name": "35 sm", "priceDelta": 12000 }
      ]
    },
    {
      "name": "Qo'shimchalar",
      "selectionType": "MULTIPLE",
      "isRequired": false,
      "maxSelect": 3,
      "options": [
        { "name": "Qo'shimcha pishloq", "priceDelta": 8000 },
        { "name": "Achchiq sous", "priceDelta": 3000 }
      ]
    }
  ]
}
```

**Javob:** `201 Created`

```json
{
  "id": "lst_01H8XZ",
  "status": "DRAFT",
  "discount": { "type": "PERCENT", "value": 20, "finalPrice": 44000, "conditions": "..." },
  "...": "qolgan maydonlar so'rovdagidek"
}
```

So'ng: `POST /listings/lst_01H8XZ/submit` → `status = PENDING_REVIEW` → moderator tasdiqlaydi → `ACTIVE` → talabalarga ko'rinadi.

---

## 9. Ma'lumotlar bazasi (taxminiy sxema)

```
businesses        (id, owner_user_id, type, name, legal_name, inn, description,
                   logo_url, cover_url, phone, contacts_json, status, rejection_reason,
                   is_online_only, rating, reviews_count, created_at, updated_at)

branches          (id, business_id, name, phone, working_hours_json, is_active,
                   -- lokatsiya
                   region_id, district_id, address, landmark, entrance_note,
                   lat, lng, geo_point GEOGRAPHY(POINT,4326), geohash,
                   map_url, metro_station,
                   -- yetkazib berish zonasi
                   delivery_enabled, delivery_radius_meters, delivery_min_order,
                   delivery_fee, free_delivery_from, delivery_polygon GEOGRAPHY(POLYGON,4326))

regions           (id, name_uz, name_ru, center_lat, center_lng)
districts         (id, region_id, name_uz, name_ru, center_lat, center_lng,
                   boundary GEOGRAPHY(POLYGON,4326))

categories        (id, business_type, key, name_uz, name_ru, icon_url, sort_order)

listings          (id, business_id, category_key, custom_category_name, title, description,
                   images_json, price_unit, original_price, currency,
                   discount_type, discount_value, final_price, discount_conditions,
                   applies_to_options, redemption_method, promo_code, per_user_limit,
                   per_user_period, total_limit, used_count,
                   valid_from, valid_to, attributes_json, status, rejection_reason,
                   views_count, created_at, updated_at)

listing_branches  (listing_id, branch_id)                     -- ko'p-ko'pga

option_groups     (id, listing_id, name, selection_type, is_required,
                   min_select, max_select, sort_order)
options           (id, option_group_id, name, price_delta, is_available, sort_order)

redemptions       (id, listing_id, student_user_id, branch_id, code, amount,
                   status, redeemed_at)                        -- PENDING | CONFIRMED | EXPIRED
```

**Indekslar:** `listings(status, valid_to)`, `listings(business_id, status)`,
`listings(category_key, status)`, `redemptions(listing_id, student_user_id, redeemed_at)`.

**Geo-indeks (eng muhimi):**

```sql
CREATE EXTENSION IF NOT EXISTS postgis;
CREATE INDEX idx_branches_geo ON branches USING GIST (geo_point);
CREATE INDEX idx_branches_region ON branches (region_id, district_id, is_active);
```

`geo_point` — `lat/lng` dan trigger bilan avtomatik to'ldiriladi
(`ST_SetSRID(ST_MakePoint(lng, lat), 4326)::geography`).

Yaqinlik bo'yicha qidiruv (`GET /discounts?lat=&lng=&radiusMeters=`):

```sql
SELECT l.*, b.id AS branch_id,
       ST_Distance(b.geo_point, :userPoint) AS distance_meters
FROM listings l
JOIN listing_branches lb ON lb.listing_id = l.id
JOIN branches b ON b.id = lb.branch_id
WHERE l.status = 'ACTIVE'
  AND b.is_active
  AND ST_DWithin(b.geo_point, :userPoint, :radiusMeters)   -- GIST indeksdan foydalanadi
ORDER BY distance_meters;
```

> PostGIS ishlatilmasa — `geohash` prefiksi bo'yicha filtr + Haversine formulasi bilan
> aniq masofa hisoblash ham ishlaydi, lekin sekinroq. PostGIS tavsiya qilinadi.

`attributes_json` — PostgreSQL `JSONB`, GIN indeks bilan (turga oid filtrlar uchun:
"faqat halol", "muddatli to'lov bor", "IELTS kurslari").

---

## 10. OpenAPI spec va Kotlin klienti

Bu hujjatdagi hamma narsa **`dev/api-client-generator/elon-uz.json`** ga qo'shildi —
tag'lar: `Business`, `Branches`, `Listings`, `Redemptions`, `Discounts`, `Geo`, `Media`.

Kotlin klientini yangilash:

```bash
./gradlew :dev:api-client-generator:generateAllApi
```

Natijada `dev.core.network.generated.api.*` da paydo bo'ladi:
`BusinessApi`, `BranchesApi`, `ListingsApi`, `RedemptionsApi`, `DiscountsApi`, `GeoApi`, `MediaApi`
va `dev.core.network.generated.model.*` da 57 ta DTO.

**Ikki eslatma spec bo'yicha:**

1. **`attributes`** — spec'da erkin `Map<String, String>` (`additionalProperties: string`).
   Turga xos kalitlar sxema tavsifida ro'yxatlangan (to'liq izoh — §4), lekin qat'iy tuzilma
   YO'Q. Sabab: OpenAPI `oneOf` dan Kotlin uchun ishlatib bo'ladigan kod chiqmaydi, tekis
   (flat) DTO esa klientni har bir kalitni 70+ maydonga qo'lda tarjima qilishga majbur qiladi.
   Haqiqiy validatsiya **backendda, tur bo'yicha JSON Schema bilan** bo'ladi
   (`GET /business/types/{type}/attributes-schema`), forma esa o'sha sxemadan dinamik quriladi.
   Shuning uchun yangi maydon qo'shilganda na spec, na ilova o'zgaradi.

2. **`POST /media/upload`** da `purpose` — `string`, enum emas. Codegen multipart'ga enum
   qo'yganda Ktor'ning internal API'siga tayanadi va kompilyatsiya buziladi. Ruxsat etilgan
   qiymatlar (`LOGO`, `COVER`, `LISTING`) tavsifda qoldirildi.

- Talaba tomonining qolgan qismi (sevimlilar, tarix, xarita ekrani, sharhlar) — alohida hujjatda.
