# Chegirmalar qidiruvi — Backend uchun PROMPT

> Bu fayl **AI'ga yoki backend dasturchiga beriladigan topshiriq**. "PROMPT BOSHLANISHI" dan
> oxirigacha bo'lgan matnni to'liq nusxalab bering. Ilova qilinadigan fayllar:
> - `dev/api-client-generator/elon-uz.json` — mavjud OpenAPI shartnomasi (biznes tomoni)
> - `dev/feature/discounts/domain/.../ListingCatalog.kt` — **27 tur, ~172 kategoriya, 74 atribut kaliti**
> - `DISCOUNTS_BUSINESS_API.md` — e'lon qanday yaratiladi (§3 modellar)
> - `API_RESPONSE_FORMAT.md` — javob konverti (`BaseResponse`)
>
> ⚠️ **Haqiqat manbai — 27 tur.** Butun spetsifikatsiya `ListingCatalog.kt` dagi **27 biznes
> turi** bo'yicha yozilgan; to'liq ro'yxat **Ilova A** da (har tur → barcha kategoriyalar va
> atributlar, variantlari bilan). `docs/backend/catalog-seed.json` (v1.2.0) hali eski — **7 tur
> / 74 kategoriya** — uni birinchi ish sifatida Ilova A bo'yicha qayta yarating. StudentClub
> (talaba ilovasi) shu kontraktga moslashadi, teskarisi emas.

---

## PROMPT BOSHLANISHI

Sen tajribali Node.js + PostgreSQL backend arxitektorisan. **ElonUz / QS Business** platformasi
uchun **chegirma e'lonlarini qidirish, filtrlash, saralash va xaritada ko'rsatish** qismini yoz.

Biznes egalari QS Business ilovasida e'lon joylaydi (`POST /v1/business/{id}/listings` allaqachon
bor). Sening vazifang — **talaba tomoni**: o'sha e'lonlarni topib beruvchi qism. U StudentClub
ilovasining "Siz uchun" bo'limi va **xaritasi** uchun ishlaydi.

---

### 0. Qat'iy arxitektura qoidalari — buzilmaydi

**Q1. Yagona endpoint.** Filtr, qidiruv, saralash, sahifalash va xarita — **bitta**
`POST /v1/discounts/search` endpoint'ida. Har bir filtr uchun alohida endpoint yasama.

**Q2. Hech qachon URL'da id yo'q.** Biror obyekt id'si (`listingId`, `businessId`, `branchId`,
`tradeCenterId`, `regionId`…) **hech qachon** path yoki query'ga qo'yilmaydi — **doim so'rov
tanasida (request body)**. Shuning uchun barcha o'qish endpoint'lari ham `POST`. Bu jumladan
tafsilot va sevimlilar endpoint'lariga ham tegishli.

**Q3. Hech qachon "hammasi" qaytmaydi.** `filter.types` **yoki** `filter.groupKeys` — kamida
bittasi majburiy, kamida 1 ta element bilan. Ikkalasi ham bo'sh bo'lsa → `422 TYPE_REQUIRED`.
Klient hech qachon barcha turdagi e'lonni birdan so'ramaydi; foydalanuvchi doim kamida bitta
guruh yoki tur tanlagan bo'ladi. `groupKeys` berilsa server uni **o'sha guruhdagi turlarga
yoyadi** (`FOOD` → `NATIONAL_FOOD`, `FAST_FOOD`, `SOMSA`) — ya'ni "Ovqat" ni tanlash ham
Q3 ni qanoatlantiradi. `size` ning maksimumi ham qat'iy chegaralanadi.

**Q0. Feed ikkala turdagi e'lonni qamraydi — chegirmali va chegirmasiz.** Bu eng ko'p
adashiladigan joy, shuning uchun birinchi qoida. Biznes egasi e'lon qo'yayotganda **rejimni
o'zi tanlaydi**: "chegirma" yoki "oddiy e'lon" (`PostListingViewModel.onListingMode`).
Oddiy e'londa `attributes._regular = "1"` yoziladi va chegirma validatsiyasi umuman
o'tkazilmaydi (`ListingValidator.kt:83`) — ya'ni `discount` obyekti ma'nosiz/bo'sh bo'ladi.

Demak:
- Qidiruvning **odatiy holati — ikkalasi ham** (`filter.listingKind: "ALL"`).
- Chegirmaga oid maydonlar (`discount`, `savedAmount`, `badge`) oddiy e'londa `null` bo'ladi —
  javob sxemasi buni ko'tarishi shart, `0` yoki soxta qiymat qo'yilmaydi.
- `DISCOUNT_PERCENT` va `SAVED_AMOUNT` saralashlari oddiy e'lonlarga tegishli emas — ular
  tanlanganda oddiy e'lonlar ro'yxat **oxiriga** tushadi (`NULLS LAST`), tashlab yuborilmaydi.

> ⚠️ `DISCOUNTS_BUSINESS_API.md` §1 da "chegirmasiz e'lon bo'lmaydi" deb yozilgan — bu
> **eskirgan**, klient kodi allaqachon ikkala rejimni qo'llab-quvvatlaydi. Ikkilanilganda
> kod ustun.

**Q6. Filtrni server e'lon qiladi, klient qattiq kodlamaydi.** Qaysi turda qanday filtr
mumkinligini **backend aytadi** (`POST /v1/catalog/filter-schema`), klient esa o'sha sxemani
umumiy tarzda chizadi va tanlangan qiymatlarni **aynan o'sha kalitlar bilan** qaytaradi.
Klientda "agar tur PLAYSTATION bo'lsa, model maydonini ko'rsat" degan shart bo'lmasin — bir
turdan ikkinchisiga o'tganda ekran o'zi qayta quriladi. Yangi tur yoki yangi atribut qo'shilsa
ilova yangilanmasdan ishlaydi.

**Q4. Faqat ko'rinadigan e'lon.** Javobga faqat shu shart bajarilganlari tushadi:
`listing.status = 'ACTIVE'` **va** `business.status = 'APPROVED'` **va**
`validFrom <= now() <= validTo`. `DRAFT`, `PENDING_REVIEW`, `REJECTED`, `PAUSED`, `EXPIRED`,
`SOLD_OUT`, `ARCHIVED` — hech qanday holatda chiqmaydi, hatto id bo'yicha so'ralsa ham.

**Q5. Konvert.** Har bir javob `API_RESPONSE_FORMAT.md` dagi `BaseResponse` konvertida:
`{ success, status, code, message, result, error }`. Foydali yuk — `result` ichida.

---

### 1. Endpoint'lar ro'yxati

| Metod | Yo'l | Vazifasi |
|---|---|---|
| `POST` | `/v1/catalog/groups` | **Guruhlar** — 27 turni 8 bo'limga jamlagan yuqori qatlam |
| `POST` | `/v1/catalog/types` | Tanlangan guruhdagi turlar (e'lon soni bilan) |
| `POST` | `/v1/catalog/filter-schema` | **Qaysi filtrlar mumkin** — tanlangan tur/guruh uchun to'liq sxema |
| `POST` | `/v1/discounts/search` | **Asosiy** — filtr + qidiruv + sort + sahifa + xarita |
| `POST` | `/v1/discounts/suggest` | Qidiruv taklifi: "osh" → Milliy taomlar › Palov |
| `POST` | `/v1/discounts/detail` | Bitta e'lonning to'liq holati (`{ "listingId": "..." }`) |
| `POST` | `/v1/discounts/favorites/toggle` | Saqlash / saqlashni bekor qilish |
| `POST` | `/v1/discounts/favorites/search` | Saqlanganlar ro'yxati (o'sha filtr modeli bilan) |

Boshqa endpoint qo'shma. Hammasi `POST`, hammasida id'lar tanada (Q2).

**Klientdagi oqim** — uchta qadam, har birida server yetakchi:

```
1) /catalog/groups          → "Ovqat 🍽 (312 e'lon)", "Sport", "O'yin", ...
   foydalanuvchi "Ovqat" ni bosadi
2) /catalog/filter-schema   → { groupKeys: ["FOOD"] }
   javob: FOOD ichidagi 3 tur, ularning kategoriyalari (Osh, Kabob, Lag'mon...),
          atributlar (Halol, Yetkazib berish, Porsiya, O'tkirlik), narx oralig'i,
          har bir variant yonida real e'lon soni
   foydalanuvchi "Osh" + "Halol" + "50 000 gacha" ni tanlaydi
3) /discounts/search        → o'sha kalitlarni qaytarib yuboradi, natija LIST yoki MAP
```

Klient 2-qadamda **nima ko'rsatishni o'ylamaydi** — sxemani qanday kelsa shunday chizadi (Q6).

---

### 1.1. Katalog ierarxiyasi — 4 qatlam

```
Guruh (8 ta)          FOOD — "Ovqatlanish"
  └── Tur (27 ta)       NATIONAL_FOOD — "Milliy taomlar"
        └── Kategoriya    PALOV — "Osh"
              └── Atribut   isHalal, portionGrams, spicyLevel, hasDelivery
```

Guruh — **yangi qatlam**, hozir na klientda, na `catalog-seed.json` da bor. Uni siz
yaratasiz. To'liq moslama — **Ilova B**. Muhim shart: guruh → tur bog'lanishi **bazada**
tursin (`business_type.group_key`), kodda emas — adminka orqali tur boshqa guruhga
ko'chirilganda ilova yangilanmasligi kerak.

`POST /v1/catalog/groups` → `{}` yoki `{ "geo": {...} }` (yaqinlikdagi sonlar uchun)

```jsonc
{ "result": { "groups": [
  { "key": "FOOD", "nameUz": "Ovqatlanish", "emoji": "🍽", "icon": "cafe",
    "accentColor": "#F97316", "typesCount": 3, "listingsCount": 312, "sortOrder": 1,
    "types": ["NATIONAL_FOOD", "FAST_FOOD", "SOMSA"] },
  { "key": "SPORT", "nameUz": "Sport", "emoji": "⚽", "icon": "ball",
    "accentColor": "#16A34A", "typesCount": 10, "listingsCount": 145, "sortOrder": 2, "types": [...] }
] } }
```

`listingsCount` — **faqat ko'rinadigan** e'lonlar (Q4), `geo` berilsa radius ichida. Bo'sh
guruh ham qaytadi, lekin `listingsCount: 0` bilan — klient uni xiralashtiradi.

`POST /v1/catalog/types` → `{ "groupKeys": ["FOOD"], "geo": {...} }`

```jsonc
{ "result": { "types": [
  { "key": "NATIONAL_FOOD", "groupKey": "FOOD", "nameUz": "Milliy taomlar", "emoji": "🍛",
    "accentColor": "#F97316", "defaultPriceUnit": "PER_ITEM",
    "priceUnits": ["PER_ITEM", "PER_KG", "PER_PERSON"],
    "availableForGenders": ["MALE", "FEMALE"],
    "allCategoryLabel": "Butun menyuga", "optionGroupHint": "Porsiya, qo'shimcha",
    "categoriesCount": 8, "listingsCount": 187 }
] } }
```

---

### 2. `POST /v1/discounts/search` — so'rov tanasi

```jsonc
{
  "mode": "LIST",                    // LIST | MAP | COUNT — majburiy

  "filter": {
    // --- MAJBURIY: `types` yoki `groupKeys` dan kamida bittasi ---
    "groupKeys": ["FOOD"],                    // guruh → o'z turlariga yoyiladi
    "types": ["NATIONAL_FOOD"],               // <=10. Berilsa `groupKeys` ni toraytiradi

    // --- Katalog ---
    "categoryKeys": ["PALOV"],                // tanlangan turlarga tegishli bo'lishi shart
    "includeAllCategory": true,               // `categoryKey="ALL"` li e'lonlar ham chiqsinmi
    "includeCustomCategories": true,          // customCategoryName bo'lganlar ham chiqsinmi

    // --- Id'lar (Q2: doim shu yerda, hech qachon URL'da) ---
    "businessIds": [],
    "branchIds": [],
    "tradeCenterIds": [],
    "listingIds": [],                         // aynan shu e'lonlar (saqlanganlarni yuklash)
    "excludeListingIds": [],                  // allaqachon ko'rsatilganlarni takrorlamaslik

    // --- Matnli qidiruv ---
    "query": "ps5 vip",

    // --- Geografiya ---
    "geo": {
      "lat": 41.3111, "lng": 69.2797,
      "radiusMeters": 5000,                   // 100..50000, default 5000
      "bbox": { "minLat": 41.28, "minLng": 69.20, "maxLat": 41.35, "maxLng": 69.31 },
      "regionIds": ["TOSHKENT_SHAHRI"],
      "districtIds": ["CHILONZOR", "YUNUSOBOD"],
      "inTradeCenterOnly": false,
      "onlineOnly": false                     // business.isOnlineOnly — filialsiz e'lonlar
    },

    // --- Narx ---
    "price": {
      "min": 0, "max": 150000,
      "basis": "FINAL",                       // FINAL (chegirmadan keyin) | ORIGINAL
      "units": ["PER_HOUR", "PER_SESSION"],   // PriceUnit enum
      "currency": "UZS"
    },

    // --- E'lon turi (Q0) ---
    "listingKind": "ALL",                     // ALL (default) | DISCOUNT | REGULAR

    // --- Chegirma (faqat listingKind ALL yoki DISCOUNT bo'lganda ma'noli) ---
    "discount": {
      "types": ["PERCENT", "SPECIAL_PRICE"],  // PERCENT | FIXED_AMOUNT | SPECIAL_PRICE | FREE_ITEM
      "minPercent": 20, "maxPercent": 90,     // faqat PERCENT uchun ma'noli
      "minSavedAmount": 10000                 // originalPrice - finalPrice
    },

    // --- Chegirmani ishlatish ---
    "redemption": {
      "methods": ["QR", "PROMO_CODE"],        // QR | PROMO_CODE | STUDENT_ID | ONLINE_LINK
      "hasPromoCode": true,
      "onlyAvailable": true                   // totalLimit tugamaganlari (usedCount < totalLimit)
    },

    // --- Vaqt va ish rejimi ---
    "availability": {
      "openNow": true,                        // filial ish vaqti (7 kunlik jadval, tungi smena bilan)
      "onDay": "SAT",                         // MON..SUN
      "atTime": "19:30",
      "validAt": "2026-07-25T15:00:00Z",      // default: now()
      "endingWithinHours": 24                 // "bugun tugaydi" filtri
    },

    // --- Turga xos atributlar (eng muhim qism, 3-bo'limga qara) ---
    "attributes": [
      { "key": "model",          "op": "IN",      "values": ["PS5", "PS4 Pro"] },
      { "key": "sessionMinutes", "op": "BETWEEN", "min": 30, "max": 120 },
      { "key": "hasWifi",        "op": "EQ",      "boolean": true },
      { "key": "games",          "op": "ANY",     "values": ["CS2", "Dota 2"] },
      { "key": "brand",          "op": "CONTAINS","text": "zara" }
    ],
    "attributesMatch": "ALL",                 // ALL (default) | ANY

    // --- Qo'shimchalar (OptionGroup / Option) ---
    "options": [
      { "groupName": "O'lcham", "values": ["M", "L"] }
    ],

    // --- Bayroqlar ---
    "flags": {
      "withImagesOnly": false,
      "favoritesOnly": false,
      "hasDeliveryOnly": false,               // attributes.hasDelivery = true
      "newOnly": false                        // createdAt oxirgi 7 kun
    }
  },

  "sort": { "by": "DISTANCE", "direction": "ASC" },

  "page": { "number": 0, "size": 20, "cursor": null },

  "map": { "zoom": 13, "clusterize": true, "maxMarkers": 500 },

  "locale": "uz"
}
```

**Barcha `filter` maydonlari `types`/`groupKeys` dan tashqari ixtiyoriy.** Yo'q maydon = shu
bo'yicha filtr yo'q. `null` va bo'sh massiv bir xil ma'noda — filtrni qo'llama.

**`includeAllCategory` haqida — muhim.** Choyxona "butun menyuga −20%" deb e'lon qo'yganda
uning `categoryKey = "ALL"` bo'ladi. Talaba "Osh" ni tanlaganda bu e'lon ham chiqishi kerak —
chegirma oshga ham tegishli. Shuning uchun default `true`, lekin bunday e'lonlar javobda
`matchedVia: "ALL"` deb belgilanadi (aniq mos kelganlarida `matchedVia: "CATEGORY"`), va
`RELEVANCE` saralashda aniq moslar tepada turadi. `false` berilsa faqat aniq moslar qaytadi.

---

### 3. Atribut filtrlari — 27 tur, 74 kalit

E'lon `attributes` maydonini `jsonb` sifatida saqlaydi (`Listing.attributes: Map<String,String>`).
Filtr **umumiy** bo'lishi shart — har bir kalit uchun alohida kod yozma, operatorlar orqali ishlat.

**Operatorlar:**

| `op` | Ishlatiladigan maydon | Ma'nosi |
|---|---|---|
| `EQ` | `text` \| `number` \| `boolean` | Aynan teng |
| `NEQ` | shu | Teng emas |
| `IN` | `values[]` | Ro'yxatdagilardan biri (SELECT uchun) |
| `NOT_IN` | `values[]` | Ro'yxatdagilardan hech biri emas |
| `BETWEEN` | `min`, `max` | Raqamli oraliq (NUMBER uchun; biri `null` bo'lishi mumkin) |
| `GTE` / `LTE` | `number` | Katta-teng / kichik-teng |
| `CONTAINS` | `text` | Ichida bor (TEXT uchun, registrsiz, normallashtirilgan) |
| `ANY` | `values[]` | TAGS ichida kamida bittasi bor |
| `ALL` | `values[]` | TAGS ichida hammasi bor |
| `EXISTS` | — | Kalit umuman to'ldirilganmi |

**Turi bo'yicha moslik (`AttributeKind` → ruxsat etilgan `op`):**

- `TEXT` → `EQ`, `NEQ`, `CONTAINS`, `EXISTS`
- `NUMBER` → `EQ`, `NEQ`, `BETWEEN`, `GTE`, `LTE`, `EXISTS`
- `BOOLEAN` → `EQ`, `EXISTS` (qiymat bazada `"true"`/`"false"` matn — solishtirishda kastla)
- `SELECT` → `EQ`, `NEQ`, `IN`, `NOT_IN`, `EXISTS`
- `MULTI_SELECT`, `TAGS` → `ANY`, `ALL`, `EXISTS` (bazada vergul bilan ajratilgan matn — normallashtir)

Mos kelmasa → `422 ATTRIBUTE_OP_MISMATCH`, `error.fields` da `"attributes[0].op"`.

**Kalit tanlangan turga tegishli bo'lishi shart.** `filter.types` dagi turlarning hech birida
bunday atribut bo'lmasa → `422 UNKNOWN_ATTRIBUTE`. Katalog — haqiqat manbai, kalitlarni
qattiq kodlama.

**Maxsus (yashirin) kalitlar** — e'lon yaratishda ishlatiladi, filtrda ham qo'llab-quvvatlansin:

| Kalit | Ma'nosi |
|---|---|
| `_regular` | `"1"` → e'lon **chegirmasiz** oddiy e'lon (Q0). `filter.listingKind` shu bilan ishlaydi: `DISCOUNT` → `_regular != '1'`, `REGULAR` → `_regular = '1'`, `ALL` → filtrsiz |
| `_phone` | Aloqa telefoni — filtrlanmaydi, lekin javobda qaytadi |
| `_gender` | `"MALE"` / `"FEMALE"` — asosan `CLOTHING` uchun |

**27 tur va ularning atribut kalitlari** — qisqa jadval. Har turning **barcha kategoriyalari,
atribut variantlari, ranglari va narx birliklari** bilan to'liq ro'yxat → **Ilova A**:

| Tur | Narx birliklari | Atributlar |
|---|---|---|
| `TENNIS` | HOUR, SESSION, PERSON | courtSurface, sessionMinutes, gearIncluded, coachAvailable |
| `TABLE_TENNIS` | HOUR, SESSION | tables, sessionMinutes, racketsIncluded |
| `FITNESS` | MONTH, SESSION, PERSON | durationMonths, sessionsPerWeek, hasTrainer, hasLocker, hasFreeTrial |
| `BOXING` | MONTH, SESSION | level, durationMonths, sessionsPerWeek, gearIncluded, hasFreeTrial |
| `FOOTBALL_FIELD` | HOUR, SESSION | surface, fieldSize, sessionMinutes, hasLighting, hasShower |
| `FOOTBALL_TRAINING` | MONTH, SESSION | ageGroup, durationMonths, sessionsPerWeek, hasUniform, hasFreeTrial |
| `BASKETBALL` | HOUR, SESSION | courtType, sessionMinutes, hasLighting, gearIncluded |
| `VOLLEYBALL` | HOUR, SESSION | sessionMinutes, hasLighting, gearIncluded |
| `SWIMMING_POOL` | SESSION, TICKET, MONTH | poolType, sessionMinutes, hasCoach, gearIncluded |
| `WRESTLING_MMA` | MONTH, SESSION | discipline, level, durationMonths, sessionsPerWeek, hasFreeTrial |
| `BOWLING` | HOUR, SESSION, PERSON | lanes, sessionMinutes, shoesIncluded, maxPlayers |
| `BILLIARDS` | HOUR, SESSION | tableType, tables, sessionMinutes, hallType |
| `PLAYSTATION` | HOUR, SESSION, PERSON | **model\***, joysticks, sessionMinutes, hallType, games |
| `CYBER_CLUB` | HOUR, SESSION | **pcTier\***, sessionMinutes, hasHeadset, games |
| `CINEMA` | TICKET, PERSON, SESSION | eventTitle, format, language, ageLimit, sessionTimes |
| `KARAOKE` | HOUR, PERSON, SESSION | roomCapacity, sessionMinutes, hasFood, languages |
| `EDUCATION_CENTER` | MONTH, COURSE, LESSON | subject, level, format, durationMonths, lessonsPerWeek, hasFreeTrialLesson |
| `LIBRARY` | MONTH, HOUR, TICKET | seats, openHours, hasWifi, hasQuietZone, hasPrinting |
| `TUTOR` | LESSON, MONTH, COURSE | subject, level, format, lessonMinutes, hasFreeTrialLesson |
| `PRINTING` | ITEM, KG | colorMode, paperSize, minOrder, express |
| `NATIONAL_FOOD` | ITEM, KG, PERSON | portionGrams, spicyLevel, isHalal, hasDelivery |
| `FAST_FOOD` | ITEM, PERSON | portionGrams, ingredients, isHalal, hasDelivery |
| `SOMSA` | ITEM, KG | filling, ovenType, isHalal, hasDelivery |
| `BARBERSHOP` | ITEM, SESSION, PERSON | master, masterLevel, durationMinutes, byAppointment |
| `BEAUTY_SALON` | ITEM, SESSION, PERSON | master, masterLevel, durationMinutes, byAppointment |
| `RENTAL_HOUSE` | MONTH, PERSON | rooms, capacity, forGender, furnished, hasWifi, utilitiesIncluded, forStudents |
| `CLOTHING` | ITEM | brand, gender, material, season |

`*` — e'lon yaratishda majburiy.

`BARBERSHOP` faqat erkaklarga, `BEAUTY_SALON` faqat ayollarga ko'rsatiladi (`genders` to'plami) —
foydalanuvchi profilidagi jins bo'yicha ro'yxatdan chiqarilishi mumkin, lekin **filtrda emas,
katalog endpointda** hal qilinadi.

---

### 4. Saralash (`sort.by`)

| Qiymat | Mantiq |
|---|---|
| `DISTANCE` | Eng yaqin filialgacha masofa. `geo.lat/lng` bo'lmasa → `422 GEO_REQUIRED_FOR_SORT` |
| `DISCOUNT_PERCENT` | Chegirma foizi. `FIXED_AMOUNT`/`SPECIAL_PRICE` uchun ekvivalent foizga keltir: `(original-final)*100/original`. `FREE_ITEM` → 50 deb hisobla |
| `PRICE_FINAL` | Chegirmadan keyingi narx |
| `PRICE_ORIGINAL` | Asl narx |
| `SAVED_AMOUNT` | `originalPrice - finalPrice` |
| `NEWEST` | `createdAt` |
| `ENDING_SOON` | `validTo` — tugash vaqti yaqinlari |
| `POPULAR` | `viewsCount` (+ `redemptionsCount` bo'lsa) |
| `RELEVANCE` | Matnli qidiruv reytingi. `query` bo'lmasa → `NEWEST` ga tush |

`direction`: `ASC` | `DESC`. Har bir sort uchun mantiqiy default: `DISTANCE`→ASC,
`PRICE_*`→ASC, qolganlari→DESC.

**Barqaror tartib majburiy.** Har bir `ORDER BY` oxiriga `, id ASC` qo'sh — aks holda teng
qiymatlarda sahifalar orasida e'lonlar takrorlanadi yoki tushib qoladi.

**Narx solishtirish faqat bir xil `priceUnit` ichida ma'noli** — "soatiga 20 000" va "oyiga
300 000" ni bir ro'yxatda saralash foydalanuvchini chalg'itadi. Shuning uchun `PRICE_*` sort
tanlanganda `filter.price.units` bo'sh bo'lsa, javobning `meta.warnings` iga
`MIXED_PRICE_UNITS` qo'sh (xato emas, ogohlantirish).

---

### 5. Matnli qidiruv (`filter.query`)

Qidiriladigan joylar (og'irlik kamayish tartibida):
1. `listing.title`
2. `business.name`
3. `category.label` va `customCategoryName`
4. `listing.description`
5. `attributes` ning TEXT/TAGS qiymatlari (`brand`, `subject`, `games`, `eventTitle`, `ingredients`…)
6. `optionGroups[].name` va `options[].name`

**O'zbek tilini normallashtirish majburiy** — bularning hammasi bir xil natija bersin:
- `o'` / `oʻ` / `oʼ` / `o` / `ў` va `g'` / `gʻ` / `g` / `ғ`
- lotin ↔ kirill (`Тошкент` ↔ `Toshkent`)
- registr va ortiqcha probel

Amalga oshirish: `unaccent` + maxsus `translate()` bilan **normallashtirilgan ustun** yarat
(`search_vector tsvector` yoki `search_text text` + `pg_trgm` GIN indeks). Har yozuvda trigger
bilan yangilanadi.

**So'z chegarasi majburiy.** Moslik **so'z boshidan** bo'lsin (`to_tsquery('osh:*')`), ixtiyoriy
substring emas. Aks holda `"osh"` → `"T**osh**kent"`, `"B**osh**qa"`, `"P**osh**sha"` ga tushadi
va natija axlatga aylanadi. `"ps5"` → `"PS5 VIP zal"` esa ishlashi shart (prefiks).

**Kategoriya nomi ham qidiriladi.** `"osh"` yozilganda `PALOV` kategoriyasining nomi
(`"Osh"`) mos keladi → o'sha kategoriyadagi **barcha** e'lon topiladi, hatto sarlavhasida
"osh" so'zi bo'lmasa ham (`"Choyxona seti — 2 kishiga"`). Buning uchun kategoriya nomi va
sinonimlari e'lonning qidiruv ustuniga qo'shiladi.

**Sinonimlar jadvali** (`catalog_synonym`: `categoryKey`, `term`, `weight`) — adminkadan
boshqariladi, kodda emas. Boshlang'ich to'plam kamida:
`PALOV` ← osh, palov, plov, o'sh · `SOMSA` ← somsa, самса · `LAGMON` ← lag'mon, lagmon, laghmon ·
`MANTI_CHUCHVARA` ← manti, chuchvara, pelmen · `KABOB` ← kabob, shashlik, kebab ·
`LAVASH_SHAWARMA` ← lavash, shaurma, shawarma, донер · `PIZZA` ← pitsa, pizza ·
`BURGER` ← burger, gamburger. Sinonim orqali topilgan e'lon `matchedVia: "SYNONYM"` oladi.

Qidiruvda ham `filter.types`/`groupKeys` qo'llaniladi — qidiruv turdan qochib ketmaydi (Q3).

---

### 6. Javob — `mode` bo'yicha

#### 6.1 `mode: "LIST"`

```jsonc
{
  "success": true, "status": 200, "result": {
    "items": [ /* DiscountCard — pastda */ ],
    "page": 0, "size": 20, "total": 137, "hasNext": true,
    "cursor": "eyJkIjoxMjM0...",
    "meta": { "appliedFilters": 6, "warnings": [] }
  }
}
```

**`DiscountCard`** — ro'yxat uchun yengil model (klientda `DiscountCard.kt` bor):

```jsonc
{
  "id": "lst_01H8X...",
  "businessId": "biz_01H8X...",
  "businessName": "Choyxona Navruz",
  "businessLogoUrl": "https://cdn/.../logo.png",
  "businessType": "NATIONAL_FOOD",
  "groupKey": "FOOD",
  "categoryKey": "PALOV",
  "categoryLabel": "Osh",
  "matchedVia": "CATEGORY",          // CATEGORY | ALL | SYNONYM | TEXT | TYPE
  "title": "Osh (1 porsiya)",
  "imageUrl": "https://cdn/.../cover.jpg",
  "imagesCount": 4,
  "priceUnit": "PER_ITEM",
  "originalPrice": 30000,
  "finalPrice": 21000,
  "savedAmount": 9000,
  "currency": "UZS",
  "discount": { "type": "PERCENT", "value": 30, "badge": "−30%", "conditions": "Talaba ID bilan" },
  "redemptionMethod": "STUDENT_ID",
  "hasPromoCode": false,
  "nearestBranch": {
    "branchId": "br_01H8X...", "name": "Yunusobod filiali",
    "address": "Yunusobod 5-kvartal, 12-uy",
    "lat": 41.352, "lng": 69.273,
    "distanceMeters": 640,
    "isOpenNow": true, "closesAt": "23:00",
    "tradeCenterName": null
  },
  "branchesCount": 3,
  "validTo": "2026-08-01T18:59:59Z",
  "isFavorite": false,
  "isNew": true,
  "viewsCount": 412,
  "attributes": { "isHalal": "true", "portionGrams": "450", "spicyLevel": "Yengil" }
}
```

`finalPrice`, `savedAmount`, `badge`, `distanceMeters`, `isOpenNow` — **server hisoblaydi**.
Klient hech qachon narx hisoblamaydi (formula `DISCOUNTS_BUSINESS_API.md` §3.5 da).

**`nearestBranch` har doim to'ldiriladi** (filialsiz onlayn biznesdan tashqari) — ro'yxatdagi
karta ham manzilni, ham koordinatani ko'rsatadi, foydalanuvchi ro'yxatdan to'g'ridan-to'g'ri
xaritaga o'tishi mumkin. Koordinata bo'lmasa `null`, lekin `address` baribir bo'ladi.
`geo.lat/lng` berilmasa `distanceMeters: null`, `nearestBranch` esa birinchi filial bo'ladi.

#### 6.2 `mode: "MAP"`

Xarita uchun **markerlar** — ro'yxat kartasidan ancha yengil, chunki bir ekranda yuzlab bo'ladi.

```jsonc
{ "result": {
  "markers": [
    { "listingId": "lst_...", "branchId": "br_...", "lat": 41.352, "lng": 69.273,
      "priceLabel": "21k", "finalPrice": 21000, "discountBadge": "−30%",
      "businessType": "PLAYSTATION", "accentColor": "#7C5CFF",
      "isDiscount": true, "isFavorite": false }
  ],
  "clusters": [
    { "lat": 41.31, "lng": 69.24, "count": 42,
      "bbox": {"minLat":..,"minLng":..,"maxLat":..,"maxLng":..},
      "minPrice": 15000, "maxDiscountPercent": 45 }
  ],
  "bounds": { "minLat":.., "minLng":.., "maxLat":.., "maxLng":.. },
  "total": 137, "truncated": false
} }
```

Qoidalar:
- `MAP` rejimida `geo.bbox` **yoki** `geo.lat+lng+radiusMeters` majburiy → yo'q bo'lsa `422 GEO_REQUIRED`.
- Bitta e'lonning bir nechta filiali bo'lsa — **har filial alohida marker**, lekin `listingId` bir xil.
  Klient bosilganda tafsilotni `listingId` bo'yicha oladi.
- `map.clusterize=true` va marker soni `map.maxMarkers` dan oshsa: yaqin nuqtalarni `map.zoom`
  ga qarab geohash bo'yicha guruhla, `markers` ni qisqartir, `truncated: true` qo'y.
  **Jimgina kesib tashlama** — `truncated` bayrog'i majburiy.
- Sahifalash yo'q; chegara — `maxMarkers` (default 500, maksimum 2000).

#### 6.3 `mode: "COUNT"`

Filtr ekranidagi "Qo'llash · N ta e'lon" tugmasi uchun — og'ir ma'lumotsiz, tez.

```jsonc
{ "result": {
  "total": 137,
  "facets": {
    "byCategory":  [ { "key": "PS5", "label": "PS5", "count": 54 } ],
    "byType":      [ { "key": "PLAYSTATION", "count": 96 } ],
    "byDistrict":  [ { "key": "CHILONZOR", "count": 31 } ],
    "byDiscountType": [ { "key": "PERCENT", "count": 88 } ],
    "byAttribute": { "hallType": [ { "value": "VIP", "count": 22 } ] },
    "priceRange":  { "min": 8000, "max": 240000 },
    "discountRange": { "minPercent": 5, "maxPercent": 70 }
  }
} }
```

`facets` **qolgan filtrlar qo'llangandan keyin**, lekin o'sha o'lchovning o'zini hisobga olmay
sanaladi (klassik faceted search) — shunda foydalanuvchi "VIP (22)" ni ko'rib tanlaydi va
natija 0 chiqmaydi. `LIST` javobiga ham `facets` qo'shish mumkin (`page.number === 0` bo'lganda).

---

### 7. Yordamchi endpoint'lar

#### `POST /v1/discounts/detail`
```jsonc
{ "listingId": "lst_01H8X...", "geo": { "lat": 41.31, "lng": 69.27 } }
```
Javob — **to'liq** `Listing`: `DiscountCard` dagi hamma narsa + `description`, `images[]` (to'liq),
`attributes` (to'liq, `_phone` bilan), `optionGroups[]` → `options[]` (`name`, `priceDelta`,
`isAvailable`, `sortOrder`), `redemption` (`method`, `promoCode`, `url`, `perUserLimit`,
`perUserPeriod`, `totalLimit`, `usedCount`, `remainingForUser`), **barcha** `branches[]`
(manzil, `landmark`, koordinata, savdo markazi + `tradeCenterFields`, 7 kunlik `workingHours`,
masofa), `business` (nom, logo, telefon, kontaktlar, reyting), `validFrom`/`validTo`,
`viewsCount`, `createdAt`.

`promoCode` faqat autentifikatsiyadan o'tgan foydalanuvchiga qaytadi. Ko'rish hisoblagichi shu
yerda oshadi (idempotent: bir foydalanuvchi + bir e'lon + 1 soat).

#### `POST /v1/catalog/filter-schema` — **qaysi filtrlar mumkin**

Bu endpoint Q6 ning yuragi: klient filtr ekranini **shu javobdan** quradi.

```jsonc
{ "groupKeys": ["FOOD"], "types": [], "categoryKeys": [], "geo": { "lat": .., "lng": .., "radiusMeters": 5000 } }
```

`types` bo'sh bo'lsa — guruhning barcha turlari bo'yicha birlashtirilgan sxema. Bir nechta tur
tanlansa, atributlar **birlashtiriladi** va har birida qaysi turlarga tegishli ekani ko'rsatiladi
(klient "faqat Milliy taomlarda" deb izoh chiqara oladi).

```jsonc
{ "result": {
  "types": [
    { "key": "NATIONAL_FOOD", "nameUz": "Milliy taomlar", "emoji": "🍛", "listingsCount": 187 },
    { "key": "FAST_FOOD", "nameUz": "Fast food", "emoji": "🍔", "listingsCount": 98 },
    { "key": "SOMSA", "nameUz": "Somsa / Nonvoyxona", "emoji": "🥟", "listingsCount": 27 }
  ],

  "categories": [
    { "key": "PALOV", "label": "Osh", "typeKey": "NATIONAL_FOOD", "count": 54 },
    { "key": "KABOB", "label": "Kabob", "typeKey": "NATIONAL_FOOD", "count": 31 },
    { "key": "BURGER", "label": "Burger", "typeKey": "FAST_FOOD", "count": 44 }
  ],

  "attributes": [
    { "key": "isHalal", "label": "Halol", "kind": "BOOLEAN",
      "appliesToTypes": ["NATIONAL_FOOD", "FAST_FOOD", "SOMSA"],
      "operators": ["EQ", "EXISTS"],
      "values": [ { "value": "true", "count": 241 }, { "value": "false", "count": 12 } ] },

    { "key": "spicyLevel", "label": "O'tkirlik", "kind": "SELECT",
      "appliesToTypes": ["NATIONAL_FOOD"],
      "operators": ["EQ", "NEQ", "IN", "NOT_IN", "EXISTS"],
      "values": [ { "value": "Yo'q", "count": 88 }, { "value": "Yengil", "count": 41 },
                  { "value": "O'rtacha", "count": 33 }, { "value": "O'tkir", "count": 9 } ] },

    { "key": "portionGrams", "label": "Porsiya", "kind": "NUMBER", "suffix": "gramm",
      "appliesToTypes": ["NATIONAL_FOOD", "FAST_FOOD"],
      "operators": ["EQ", "BETWEEN", "GTE", "LTE", "EXISTS"],
      "range": { "min": 150, "max": 800, "step": 50 } },

    { "key": "ingredients", "label": "Tarkibi", "kind": "TAGS",
      "appliesToTypes": ["FAST_FOOD"], "operators": ["ANY", "ALL", "EXISTS"],
      "values": [ { "value": "Mol go'shti", "count": 61 }, { "value": "Tovuq", "count": 55 } ] }
  ],

  "price": { "min": 8000, "max": 240000,
             "units": [ { "key": "PER_ITEM", "label": "Dona", "count": 268 },
                        { "key": "PER_KG", "label": "Kilogramm", "count": 31 } ] },
  "discount": { "types": [ { "key": "PERCENT", "count": 188 }, { "key": "FREE_ITEM", "count": 14 } ],
                "percentRange": { "min": 5, "max": 60 } },
  "redemption": { "methods": [ { "key": "STUDENT_ID", "count": 201 }, { "key": "QR", "count": 88 } ] },
  "geo": { "regions": [...], "districts": [ { "id": "CHILONZOR", "name": "Chilonzor", "count": 47 } ],
           "tradeCenters": [ { "id": "tc_...", "name": "Compass Mall", "count": 9 } ] },
  "sorts": [ { "key": "DISTANCE", "label": "Yaqinlik", "requiresGeo": true },
             { "key": "PRICE_FINAL", "label": "Arzon" },
             { "key": "DISCOUNT_PERCENT", "label": "Chegirma %" } ],
  "total": 312
} }
```

**Qat'iy qoidalar:**
- **Faqat haqiqiy ma'lumotda uchraydigan qiymatlar.** Katalogda `spicyLevel` da 4 variant
  bo'lsa-yu, bazada faqat 2 tasi ishlatilgan bo'lsa — 2 tasini ber. Foydalanuvchi 0 natija
  beradigan filtrni tanlay olmasin.
- **Har variant yonida `count`** — `geo` va berilgan `categoryKeys` hisobga olingan holda.
- **`operators`** ni server aytadi — klient `AttributeKind` dan operatorni o'zi chiqarmasin.
- **`appliesToTypes`** — ko'p tur tanlanganda qaysi atribut qayerga tegishli ekani.
- Javob 5 daqiqaga keshlanadi (`geo` ni koordinatani ~1 km gacha yaxlitlab kesh kalitiga qo'sh).

#### `POST /v1/discounts/suggest` — qidiruv taklifi

```jsonc
{ "query": "osh", "groupKeys": ["FOOD"], "limit": 8 }
```
```jsonc
{ "result": { "suggestions": [
  { "kind": "CATEGORY", "label": "Osh", "typeKey": "NATIONAL_FOOD", "categoryKey": "PALOV", "count": 54 },
  { "kind": "TYPE",     "label": "Milliy taomlar", "typeKey": "NATIONAL_FOOD", "count": 187 },
  { "kind": "BUSINESS", "label": "Besh Qozon", "businessId": "biz_...", "count": 6 },
  { "kind": "LISTING",  "label": "Osh (1 porsiya) — Choyxona Navruz", "listingId": "lst_...", "count": 1 }
] } }
```

Klient taklifni bosganda **matn qidiruvi emas**, aniq filtr yuboriladi
(`categoryKeys: ["PALOV"]`) — bu ancha aniqroq natija beradi.

#### `POST /v1/discounts/favorites/toggle`
```jsonc
{ "listingId": "lst_...", "saved": true }
```
→ `{ "listingId": "...", "saved": true, "favoritesCount": 12 }`

#### `POST /v1/discounts/favorites/search`
Tanasi `search` bilan **bir xil** (`filter` + `sort` + `page`), faqat `filter.types` bu yerda
**ixtiyoriy** — sevimlilar allaqachon cheklangan to'plam.

---

### 7.1. To'liq misol — "Ovqat › Osh" izlash

Bu ssenariy uchidan uchigacha ishlashi shart. Har qadam alohida test bilan qoplansin.

**1-qadam. Guruhlar** — foydalanuvchi bosh ekranda 8 ta bo'limni ko'radi.

```jsonc
POST /v1/catalog/groups
{ "geo": { "lat": 41.3111, "lng": 69.2797, "radiusMeters": 5000 } }
→ [ { "key": "FOOD", "nameUz": "Ovqatlanish", "emoji": "🍽", "listingsCount": 312 }, ... ]
```

**2-qadam. "Ovqat" bosildi → filtr sxemasi.**

```jsonc
POST /v1/catalog/filter-schema
{ "groupKeys": ["FOOD"], "geo": { "lat": 41.3111, "lng": 69.2797, "radiusMeters": 5000 } }
→ types:      Milliy taomlar (187), Fast food (98), Somsa (27)
  categories: Osh (54), Kabob (31), Lag'mon (22), Burger (44), ...
  attributes: Halol, Yetkazib berish, Porsiya (150–800 g), O'tkirlik
  price:      8 000 – 240 000 so'm
```

Klient shu javobdan filtr ekranini quradi. Kodda "ovqat bo'lsa halol maydonini ko'rsat"
degan shart **yo'q** (Q6).

**3-qadam. "Osh" + "Halol" tanlandi → ro'yxat, restoran manzili bilan.**

```jsonc
POST /v1/discounts/search
{
  "mode": "LIST",
  "filter": {
    "groupKeys": ["FOOD"],
    "categoryKeys": ["PALOV"],
    "includeAllCategory": true,
    "attributes": [ { "key": "isHalal", "op": "EQ", "boolean": true } ],
    "geo": { "lat": 41.3111, "lng": 69.2797, "radiusMeters": 5000 }
  },
  "sort": { "by": "DISTANCE", "direction": "ASC" },
  "page": { "number": 0, "size": 20 }
}
```

Javobdagi har bir element restoran nomi **va joylashuvi** bilan keladi:

```jsonc
{ "items": [
  { "id": "lst_a1", "businessName": "Choyxona Navruz", "categoryLabel": "Osh",
    "matchedVia": "CATEGORY", "title": "Osh (1 porsiya)",
    "originalPrice": 30000, "finalPrice": 21000, "discount": { "badge": "−30%" },
    "nearestBranch": { "branchId": "br_9", "name": "Chilonzor filiali",
      "address": "Chilonzor 9-kvartal, 42-uy", "lat": 41.2856, "lng": 69.2034,
      "distanceMeters": 640, "isOpenNow": true, "closesAt": "23:00" },
    "branchesCount": 3 },

  { "id": "lst_b7", "businessName": "Besh Qozon", "categoryLabel": "Butun menyuga",
    "matchedVia": "ALL", "title": "Butun menyuga chegirma",
    "nearestBranch": { "address": "Yunusobod 5-kvartal", "lat": 41.352, "lng": 69.273,
      "distanceMeters": 2100, "isOpenNow": true } }
], "total": 54, "hasNext": true }
```

Ikkinchi element `matchedVia: "ALL"` — "butun menyuga chegirma" e'loni, oshga ham tegishli,
shuning uchun chiqdi, lekin aniq moslardan keyin turadi.

**4-qadam. Xaritaga o'tish** — foydalanuvchi o'sha filtrni saqlab xarita rejimiga o'tadi.
`filter` **o'zgarmaydi**, faqat `mode` va `geo.bbox`:

```jsonc
POST /v1/discounts/search
{
  "mode": "MAP",
  "filter": { /* 3-qadamdagi filtr, so'zma-so'z */
    "geo": { "bbox": { "minLat": 41.26, "minLng": 69.16, "maxLat": 41.38, "maxLng": 69.34 } } },
  "map": { "zoom": 12, "clusterize": true }
}
→ markers: har bir restoran filiali uchun alohida nuqta, "21k" narx yorlig'i va "−30%" bilan
```

Bitta `filter` obyekti ro'yxat va xaritada **bir xil** ishlaydi — bu Q1 ning maqsadi.

**5-qadam (muqobil). Foydalanuvchi shunchaki "osh" deb yozdi.**

```jsonc
POST /v1/discounts/suggest   { "query": "osh", "limit": 8 }
→ [ { "kind": "CATEGORY", "label": "Osh", "typeKey": "NATIONAL_FOOD", "categoryKey": "PALOV", "count": 54 }, ... ]
```

Taklif bosilganda klient `categoryKeys: ["PALOV"]` yuboradi — matn qidiruvidan aniqroq.
Agar foydalanuvchi taklifni tanlamay to'g'ridan-to'g'ri qidirsa, `query: "osh"` sinonim
jadvali orqali baribir `PALOV` ni topadi (§5), lekin `types`/`groupKeys` baribir majburiy —
klient bunda oxirgi tanlangan guruhni yuboradi.

---

### 8. Validatsiya va xatolar

Barcha xatolar `BaseResponse.error` da: `{ code, message, fields }`.

| Kod | Status | Qachon |
|---|---|---|
| `TYPE_REQUIRED` | 422 | `filter.types` ham, `filter.groupKeys` ham bo'sh |
| `TOO_MANY_TYPES` | 422 | 10 tadan ko'p tur (guruh yoyilgandan **keyin** ham tekshiriladi) |
| `UNKNOWN_TYPE` | 422 | Katalogda yo'q tur kaliti |
| `UNKNOWN_GROUP` | 422 | Katalogda yo'q guruh kaliti |
| `TYPE_GROUP_MISMATCH` | 422 | `types` dagi tur `groupKeys` dagi guruhlarga kirmaydi |
| `UNKNOWN_CATEGORY` | 422 | Kategoriya tanlangan turlarga tegishli emas |
| `UNKNOWN_ATTRIBUTE` | 422 | Atribut kaliti tanlangan turlarda yo'q |
| `ATTRIBUTE_OP_MISMATCH` | 422 | Operator atribut turiga mos emas |
| `GEO_REQUIRED` | 422 | `mode=MAP`, lekin `bbox` ham `lat/lng` ham yo'q |
| `GEO_REQUIRED_FOR_SORT` | 422 | `sort.by=DISTANCE`, lekin koordinata yo'q |
| `INVALID_BBOX` | 422 | `minLat > maxLat` yoki O'zbekiston chegarasidan tashqarida (lat 37..46, lng 55..74) |
| `PAGE_SIZE_EXCEEDED` | 422 | `size > 50` (MAP: `maxMarkers > 2000`) |
| `INVALID_PRICE_RANGE` | 422 | `min > max` |
| `LISTING_NOT_FOUND` | 404 | detail: yo'q **yoki** ko'rinmaydigan holatda (holatni oshkor qilma) |

`fields` da aniq yo'l ko'rsatilsin: `"filter.attributes[2].op"`, `"filter.geo.bbox.minLat"`.

Chegaralar: `size` default 20 / max 50; `radiusMeters` 100..50 000; `query` max 100 belgi;
`types` max 10; `categoryKeys` max 30; `attributes` max 20 shart; id massivlari max 200 element.

---

### 9. Ma'lumotlar bazasi va unumdorlik

- **PostGIS**: `branch.geom geography(Point,4326)`, `GIST` indeks. Masofa — `ST_Distance`,
  radius — `ST_DWithin`, bbox — `ST_MakeEnvelope`. Klientdagi haversine (`Geo.distanceMeters`)
  bilan bir xil natija bersin.
- **`GIN` indeks** `listing.attributes jsonb_path_ops` ustida.
- **`pg_trgm` GIN** normallashtirilgan qidiruv ustuni ustida.
- **Kompozit indeks**: `(business_type, status, valid_to)` va `(status, valid_from, valid_to)`.
- **Sahifalash**: `page.number` (oddiy) va `cursor` (keyset) — ikkalasi ham. Cheksiz skroll va
  xarita uchun **keyset afzal**; `cursor` berilsa `page.number` e'tiborsiz qoladi.
- **Materiallashtirilgan ko'rinish** (`discount_card_mv`): e'lon + biznes + eng yaqin filial +
  hisoblangan `finalPrice`. `listing`/`branch`/`business` o'zgarganda yangilanadi. Bu `LIST` va
  `MAP` ni bitta jadval skanida bajarishga imkon beradi.
- `COUNT` rejimi hech qachon to'liq qatorlarni yuklamasin — faqat agregatlar.
- Har javobda `meta.tookMs`; sekin so'rovlarni (>300ms) `traceId` bilan logla.

---

### 10. Qabul mezonlari (test bilan tasdiqlansin)

0. **"Ovqat › Osh" ssenariysi** (§7.1) beshta qadamda ham ishlaydi: guruh → filtr sxemasi →
   ro'yxat (restoran manzili va koordinatasi bilan) → xarita (o'sha filtr, o'zgarishsiz) → taklif.
1. `filter.types` **va** `groupKeys` bo'sh → `422 TYPE_REQUIRED`; **hech qanday** yo'l bilan
   hamma e'lonni olib bo'lmaydi. `groupKeys: ["FOOD"]` esa o'tadi va 3 turga yoyiladi.
2. Hech bir endpoint URL'ida id yo'q — barchasi `POST` va tanada.
3. `PAUSED`/`DRAFT`/`REJECTED` e'lon `search` da ham, `detail` da ham chiqmaydi.
4. Muddati tugagan (`validTo < now`) e'lon chiqmaydi.
5. `sort=DISTANCE` + koordinata → natija haqiqatan yaqindan uzoqqa; `distanceMeters` PostGIS va
   klient haversine'ida ±1 m farq.
6. Bir xil so'rov 1- va 2-sahifada takrorlangan e'lon bermaydi (barqaror tartib).
7. `mode=MAP` bbox ichidagi hamma filialni beradi; `maxMarkers` oshsa `truncated: true`.
8. Ko'p filialli e'lon `LIST` da **bir marta** (eng yaqin filial bilan), `MAP` da **har filial uchun** chiqadi.
9. `o'quv` / `oquv` / `oʻquv` qidiruvi bir xil natija beradi.
10. `attributes` filtri 27 turning har biri uchun ishlaydi — har tur uchun kamida bitta test.
11. `COUNT` dagi `total` aynan `LIST` dagi `total` ga teng.
12. `facets` va `filter-schema` dagi har bir variantni tanlash 0 dan katta natija beradi.
13. Barcha javoblar `BaseResponse` konvertida.
14. `"osh"` qidiruvi `PALOV` kategoriyasidagi e'lonlarni topadi (sinonim), lekin
    `"Toshkent"`, `"boshqa"` so'zlariga **tushmaydi** (so'z chegarasi).
15. `filter-schema` faqat bazada haqiqatan uchraydigan variantlarni beradi — katalogda bor,
    lekin ishlatilmagan variant qaytmaydi.
16. `includeAllCategory: true` bilan `categoryKey="ALL"` li e'lon chiqadi va
    `matchedVia: "ALL"` belgisini oladi; `false` bilan chiqmaydi.
17. `/catalog/groups` dagi `listingsCount` yig'indisi `/catalog/types` dagi sonlar
    yig'indisiga teng.

---

### 11. Yetkazib berish

1. Migratsiyalar (PostGIS, indekslar, materiallashtirilgan ko'rinish, `business_type.group_key`,
   `catalog_synonym` jadvali)
2. `catalog-seed.json` ni **Ilova A bo'yicha 27 turga** qayta yaratish (hozir 7 ta) va
   **Ilova B bo'yicha 8 guruhga** bog'lash — bu birinchi qadam, qolgan hamma narsa shunga tayanadi
3. Zod sxemalari — har bir so'rov tanasi uchun
4. OpenAPI: yangi endpoint'larni `dev/api-client-generator/elon-uz.json` ga qo'sh (klient
   shundan generatsiya qilinadi — schema nomlari: `CatalogGroupDto`, `CatalogTypeDto`,
   `FilterSchemaDto`, `FilterAttributeSchemaDto`, `DiscountSearchRequestDto`,
   `DiscountFilterDto`, `DiscountSortDto`, `DiscountPageDto`, `DiscountCardDto`,
   `DiscountMarkerDto`, `DiscountClusterDto`, `DiscountFacetsDto`, `AttributeFilterDto`,
   `DiscountSuggestionDto`)
5. Integratsion testlar — 10-bo'limdagi 17 mezon
6. Seed ma'lumot: kamida 27 turning har birida 5 e'lon, turli filial va koordinatalar bilan;
   `FOOD` guruhida "Osh" ssenariysi (§7.1) to'liq sinaladigan darajada ma'lumot bo'lsin

## PROMPT TUGADI

---

## Ilova A — 27 biznes turining to'liq katalogi

Manba: `ListingCatalog.kt`. Backend `catalog-seed.json` ni **aynan shu tarkibda** qayta yaratsin.

### 1. `TENNIS` — Katta tennis 🎾

- **Rang:** `0xFF16A34A` · **Odatiy narx birligi:** `PER_HOUR`
- **Ruxsat etilgan narx birliklari:** `PER_HOUR`, `PER_SESSION`, `PER_PERSON`
- **Jins:** MALE, FEMALE · **"Hammasiga" yozuvi:** "Barcha kortlar" · **Qo'shimcha guruh taklifi:** "Kort turi, qoplama"

**Kategoriyalar (6):** `ALL` Barcha kortlar, `OUTDOOR` Ochiq kort, `INDOOR` Yopiq kort, `TRAINING` Trening / darslar, `KIDS` Bolalar guruhi, `OTHER` Boshqa

**Atributlar (4):**

| Kalit | Nomi | Turi | Variantlar / birlik | Majburiy |
|---|---|---|---|---|
| `courtSurface` | Qoplama | `SELECT` | Gruntli, Sun'iy o't, Qattiq (hard) | — |
| `sessionMinutes` | Sessiya davomiyligi | `NUMBER` | birlik: daqiqa | — |
| `gearIncluded` | Raketka beriladi | `BOOLEAN` | — | — |
| `coachAvailable` | Murabbiy bor | `BOOLEAN` | — | — |

### 2. `TABLE_TENNIS` — Stol tennis 🏓

- **Rang:** `0xFF0EA5E9` · **Odatiy narx birligi:** `PER_HOUR`
- **Ruxsat etilgan narx birliklari:** `PER_HOUR`, `PER_SESSION`
- **Jins:** MALE, FEMALE · **"Hammasiga" yozuvi:** "Barcha stollar" · **Qo'shimcha guruh taklifi:** "Stollar soni"

**Kategoriyalar (4):** `ALL` Barcha stollar, `RENT` Stol ijarasi, `TRAINING` Trening, `OTHER` Boshqa

**Atributlar (3):**

| Kalit | Nomi | Turi | Variantlar / birlik | Majburiy |
|---|---|---|---|---|
| `tables` | Stollar soni | `NUMBER` | birlik: ta | — |
| `sessionMinutes` | Sessiya davomiyligi | `NUMBER` | birlik: daqiqa | — |
| `racketsIncluded` | Raketka va koptok beriladi | `BOOLEAN` | — | — |

### 3. `FITNESS` — Fitnes / Trenajyor zali 🏋️

- **Rang:** `0xFF22C55E` · **Odatiy narx birligi:** `PER_MONTH`
- **Ruxsat etilgan narx birliklari:** `PER_MONTH`, `PER_SESSION`, `PER_PERSON`
- **Jins:** MALE, FEMALE · **"Hammasiga" yozuvi:** "Barcha xizmatlar" · **Qo'shimcha guruh taklifi:** "Abonement, mashg'ulot turi"

**Kategoriyalar (7):** `ALL` Barcha xizmatlar, `GYM` Trenajyor zali, `GROUP` Guruh mashg'ulotlari, `PERSONAL` Shaxsiy murabbiy, `CROSSFIT` Crossfit, `YOGA` Yoga / Pilates, `OTHER` Boshqa

**Atributlar (5):**

| Kalit | Nomi | Turi | Variantlar / birlik | Majburiy |
|---|---|---|---|---|
| `durationMonths` | Abonement muddati | `NUMBER` | birlik: oy | — |
| `sessionsPerWeek` | Haftada | `NUMBER` | birlik: marta | — |
| `hasTrainer` | Murabbiy bor | `BOOLEAN` | — | — |
| `hasLocker` | Shkaf / dush bor | `BOOLEAN` | — | — |
| `hasFreeTrial` | Sinov mashg'uloti bepul | `BOOLEAN` | — | — |

### 4. `BOXING` — Boks / Yakkakurash zali 🥊

- **Rang:** `0xFFDC2626` · **Odatiy narx birligi:** `PER_MONTH`
- **Ruxsat etilgan narx birliklari:** `PER_MONTH`, `PER_SESSION`
- **Jins:** MALE, FEMALE · **"Hammasiga" yozuvi:** "Barcha yo'nalishlar" · **Qo'shimcha guruh taklifi:** "Daraja, guruh"

**Kategoriyalar (6):** `ALL` Barcha yo'nalishlar, `BOXING` Boks, `KICKBOXING` Kikboksing, `KIDS` Bolalar guruhi, `PERSONAL` Shaxsiy trening, `OTHER` Boshqa

**Atributlar (5):**

| Kalit | Nomi | Turi | Variantlar / birlik | Majburiy |
|---|---|---|---|---|
| `level` | Daraja | `SELECT` | Boshlang'ich, O'rta, Professional | — |
| `durationMonths` | Abonement muddati | `NUMBER` | birlik: oy | — |
| `sessionsPerWeek` | Haftada | `NUMBER` | birlik: marta | — |
| `gearIncluded` | Jihoz beriladi | `BOOLEAN` | — | — |
| `hasFreeTrial` | Sinov mashg'uloti bepul | `BOOLEAN` | — | — |

### 5. `FOOTBALL_FIELD` — Futbol maydoni ⚽

- **Rang:** `0xFF15803D` · **Odatiy narx birligi:** `PER_HOUR`
- **Ruxsat etilgan narx birliklari:** `PER_HOUR`, `PER_SESSION`
- **Jins:** MALE, FEMALE · **"Hammasiga" yozuvi:** "Barcha maydonlar" · **Qo'shimcha guruh taklifi:** "Qoplama, o'lcham"

**Kategoriyalar (5):** `ALL` Barcha maydonlar, `INDOOR` Yopiq (manej), `OUTDOOR` Ochiq maydon, `MINI` Mini-futbol, `OTHER` Boshqa

**Atributlar (5):**

| Kalit | Nomi | Turi | Variantlar / birlik | Majburiy |
|---|---|---|---|---|
| `surface` | Qoplama | `SELECT` | Sun'iy o't, Tabiiy o't, Zal parketi | — |
| `fieldSize` | Maydon o'lchami | `SELECT` | 5x5, 6x6, 8x8, 11x11 | — |
| `sessionMinutes` | Sessiya davomiyligi | `NUMBER` | birlik: daqiqa | — |
| `hasLighting` | Yoritish bor | `BOOLEAN` | — | — |
| `hasShower` | Dush / kiyinish xonasi | `BOOLEAN` | — | — |

### 6. `FOOTBALL_TRAINING` — Futbol maktabi 🥅

- **Rang:** `0xFF65A30D` · **Odatiy narx birligi:** `PER_MONTH`
- **Ruxsat etilgan narx birliklari:** `PER_MONTH`, `PER_SESSION`
- **Jins:** MALE, FEMALE · **"Hammasiga" yozuvi:** "Barcha guruhlar" · **Qo'shimcha guruh taklifi:** "Yosh guruhi"

**Kategoriyalar (6):** `ALL` Barcha guruhlar, `KIDS` Bolalar guruhi, `TEEN` O'smirlar, `ADULT` Kattalar, `GOALKEEPER` Darvozabon maktabi, `OTHER` Boshqa

**Atributlar (5):**

| Kalit | Nomi | Turi | Variantlar / birlik | Majburiy |
|---|---|---|---|---|
| `ageGroup` | Yosh guruhi | `SELECT` | 5-8 yosh, 9-12 yosh, 13-16 yosh, Kattalar | — |
| `durationMonths` | Abonement muddati | `NUMBER` | birlik: oy | — |
| `sessionsPerWeek` | Haftada | `NUMBER` | birlik: marta | — |
| `hasUniform` | Forma beriladi | `BOOLEAN` | — | — |
| `hasFreeTrial` | Sinov mashg'uloti bepul | `BOOLEAN` | — | — |

### 7. `BASKETBALL` — Basketbol maydoni 🏀

- **Rang:** `0xFFEA580C` · **Odatiy narx birligi:** `PER_HOUR`
- **Ruxsat etilgan narx birliklari:** `PER_HOUR`, `PER_SESSION`
- **Jins:** MALE, FEMALE · **"Hammasiga" yozuvi:** "Barcha maydonlar" · **Qo'shimcha guruh taklifi:** "Maydon turi"

**Kategoriyalar (5):** `ALL` Barcha maydonlar, `INDOOR` Yopiq zal, `OUTDOOR` Ochiq maydon, `TRAINING` Trening, `OTHER` Boshqa

**Atributlar (4):**

| Kalit | Nomi | Turi | Variantlar / birlik | Majburiy |
|---|---|---|---|---|
| `courtType` | Maydon turi | `SELECT` | To'liq maydon, Yarim maydon (3x3) | — |
| `sessionMinutes` | Sessiya davomiyligi | `NUMBER` | birlik: daqiqa | — |
| `hasLighting` | Yoritish bor | `BOOLEAN` | — | — |
| `gearIncluded` | To'p beriladi | `BOOLEAN` | — | — |

### 8. `VOLLEYBALL` — Voleybol maydoni 🏐

- **Rang:** `0xFFEAB308` · **Odatiy narx birligi:** `PER_HOUR`
- **Ruxsat etilgan narx birliklari:** `PER_HOUR`, `PER_SESSION`
- **Jins:** MALE, FEMALE · **"Hammasiga" yozuvi:** "Barcha maydonlar" · **Qo'shimcha guruh taklifi:** "Maydon turi"

**Kategoriyalar (5):** `ALL` Barcha maydonlar, `INDOOR` Yopiq zal, `BEACH` Plyaj voleybol, `TRAINING` Trening, `OTHER` Boshqa

**Atributlar (3):**

| Kalit | Nomi | Turi | Variantlar / birlik | Majburiy |
|---|---|---|---|---|
| `sessionMinutes` | Sessiya davomiyligi | `NUMBER` | birlik: daqiqa | — |
| `hasLighting` | Yoritish bor | `BOOLEAN` | — | — |
| `gearIncluded` | To'p / to'r beriladi | `BOOLEAN` | — | — |

### 9. `SWIMMING_POOL` — Suzish havzasi 🏊

- **Rang:** `0xFF06B6D4` · **Odatiy narx birligi:** `PER_SESSION`
- **Ruxsat etilgan narx birliklari:** `PER_SESSION`, `PER_TICKET`, `PER_MONTH`
- **Jins:** MALE, FEMALE · **"Hammasiga" yozuvi:** "Barcha xizmatlar" · **Qo'shimcha guruh taklifi:** "Havza turi, sessiya"

**Kategoriyalar (6):** `ALL` Barcha xizmatlar, `FREE_SWIM` Erkin suzish, `TRAINING` Suzish darslari, `KIDS` Bolalar guruhi, `AQUA_AEROBICS` Aqua-aerobika, `OTHER` Boshqa

**Atributlar (4):**

| Kalit | Nomi | Turi | Variantlar / birlik | Majburiy |
|---|---|---|---|---|
| `poolType` | Havza turi | `SELECT` | Yopiq, Ochiq | — |
| `sessionMinutes` | Sessiya davomiyligi | `NUMBER` | birlik: daqiqa | — |
| `hasCoach` | Murabbiy bor | `BOOLEAN` | — | — |
| `gearIncluded` | Jihoz beriladi | `BOOLEAN` | — | — |

### 10. `WRESTLING_MMA` — Kurash / MMA 🤼

- **Rang:** `0xFFB91C1C` · **Odatiy narx birligi:** `PER_MONTH`
- **Ruxsat etilgan narx birliklari:** `PER_MONTH`, `PER_SESSION`
- **Jins:** MALE, FEMALE · **"Hammasiga" yozuvi:** "Barcha yo'nalishlar" · **Qo'shimcha guruh taklifi:** "Yo'nalish, daraja"

**Kategoriyalar (6):** `ALL` Barcha yo'nalishlar, `WRESTLING` Kurash, `MMA` MMA, `JUDO` Dzyudo, `KIDS` Bolalar guruhi, `OTHER` Boshqa

**Atributlar (5):**

| Kalit | Nomi | Turi | Variantlar / birlik | Majburiy |
|---|---|---|---|---|
| `discipline` | Yo'nalish | `SELECT` | Kurash, MMA, Dzyudo, Sambo | — |
| `level` | Daraja | `SELECT` | Boshlang'ich, O'rta, Professional | — |
| `durationMonths` | Abonement muddati | `NUMBER` | birlik: oy | — |
| `sessionsPerWeek` | Haftada | `NUMBER` | birlik: marta | — |
| `hasFreeTrial` | Sinov mashg'uloti bepul | `BOOLEAN` | — | — |

### 11. `BOWLING` — Bouling 🎳

- **Rang:** `0xFFA855F7` · **Odatiy narx birligi:** `PER_HOUR`
- **Ruxsat etilgan narx birliklari:** `PER_HOUR`, `PER_SESSION`, `PER_PERSON`
- **Jins:** MALE, FEMALE · **"Hammasiga" yozuvi:** "Barcha yo'laklar" · **Qo'shimcha guruh taklifi:** "Yo'lak turi"

**Kategoriyalar (4):** `ALL` Barcha yo'laklar, `STANDARD` Oddiy yo'lak, `VIP` VIP yo'lak, `OTHER` Boshqa

**Atributlar (4):**

| Kalit | Nomi | Turi | Variantlar / birlik | Majburiy |
|---|---|---|---|---|
| `lanes` | Yo'laklar soni | `NUMBER` | birlik: yo'lak | — |
| `sessionMinutes` | Sessiya davomiyligi | `NUMBER` | birlik: daqiqa | — |
| `shoesIncluded` | Poyabzal beriladi | `BOOLEAN` | — | — |
| `maxPlayers` | Maksimal o'yinchi | `NUMBER` | birlik: kishi | — |

### 12. `BILLIARDS` — Billiard 🎱

- **Rang:** `0xFF7C3AED` · **Odatiy narx birligi:** `PER_HOUR`
- **Ruxsat etilgan narx birliklari:** `PER_HOUR`, `PER_SESSION`
- **Jins:** MALE, FEMALE · **"Hammasiga" yozuvi:** "Barcha stollar" · **Qo'shimcha guruh taklifi:** "Stol turi, zal"

**Kategoriyalar (5):** `ALL` Barcha stollar, `POOL` Pul (Amerika), `RUSSIAN` Rus billiard, `SNOOKER` Snuker, `OTHER` Boshqa

**Atributlar (4):**

| Kalit | Nomi | Turi | Variantlar / birlik | Majburiy |
|---|---|---|---|---|
| `tableType` | Stol turi | `SELECT` | Pul (Amerika), Rus, Snuker | — |
| `tables` | Stollar soni | `NUMBER` | birlik: ta | — |
| `sessionMinutes` | Sessiya davomiyligi | `NUMBER` | birlik: daqiqa | — |
| `hallType` | Zal turi | `SELECT` | Standart, VIP, Alohida xona | — |

### 13. `PLAYSTATION` — PlayStation 🎮

- **Rang:** `0xFF7C5CFF` · **Odatiy narx birligi:** `PER_HOUR`
- **Ruxsat etilgan narx birliklari:** `PER_HOUR`, `PER_SESSION`, `PER_PERSON`
- **Jins:** MALE, FEMALE · **"Hammasiga" yozuvi:** "Barcha zallar" · **Qo'shimcha guruh taklifi:** "Model, zal turi"

**Kategoriyalar (5):** `ALL` Barcha zallar, `PS5` PlayStation 5, `PS4` PlayStation 4, `VIP` VIP xona, `OTHER` Boshqa

**Atributlar (5):**

| Kalit | Nomi | Turi | Variantlar / birlik | Majburiy |
|---|---|---|---|---|
| `model` | Model | `SELECT` | PS5, PS4 Pro, PS4, PS3 | ✅ |
| `joysticks` | Joystiklar | `SELECT` | 2 ta, 4 ta | — |
| `sessionMinutes` | Sessiya davomiyligi | `NUMBER` | birlik: daqiqa | — |
| `hallType` | Zal turi | `SELECT` | Standart, VIP, Alohida xona | — |
| `games` | Mashhur o'yinlar | `TAGS` | — | — |

### 14. `CYBER_CLUB` — Kompyuter klubi 🖥️

- **Rang:** `0xFF6366F1` · **Odatiy narx birligi:** `PER_HOUR`
- **Ruxsat etilgan narx birliklari:** `PER_HOUR`, `PER_SESSION`
- **Jins:** MALE, FEMALE · **"Hammasiga" yozuvi:** "Barcha joylar" · **Qo'shimcha guruh taklifi:** "PC quvvati"

**Kategoriyalar (5):** `ALL` Barcha joylar, `STANDARD` Standart PC, `VIP` VIP / Gaming PC, `PRO` Pro / e-sport, `OTHER` Boshqa

**Atributlar (4):**

| Kalit | Nomi | Turi | Variantlar / birlik | Majburiy |
|---|---|---|---|---|
| `pcTier` | Kompyuter quvvati | `SELECT` | Standart, Gaming, Pro / e-sport | ✅ |
| `sessionMinutes` | Sessiya davomiyligi | `NUMBER` | birlik: daqiqa | — |
| `hasHeadset` | Garnitura bor | `BOOLEAN` | — | — |
| `games` | O'yinlar | `TAGS` | — | — |

### 15. `CINEMA` — Kinoteatr 🎬

- **Rang:** `0xFFEF4444` · **Odatiy narx birligi:** `PER_TICKET`
- **Ruxsat etilgan narx birliklari:** `PER_TICKET`, `PER_PERSON`, `PER_SESSION`
- **Jins:** MALE, FEMALE · **"Hammasiga" yozuvi:** "Barcha seanslar" · **Qo'shimcha guruh taklifi:** "Format, zal turi"

**Kategoriyalar (5):** `ALL` Barcha seanslar, `STANDARD` Oddiy zal, `VIP` VIP zal, `KIDS` Bolalar seansi, `OTHER` Boshqa

**Atributlar (5):**

| Kalit | Nomi | Turi | Variantlar / birlik | Majburiy |
|---|---|---|---|---|
| `eventTitle` | Film nomi | `TEXT` | — | — |
| `format` | Format | `SELECT` | 2D, 3D, IMAX, 4DX, VR | — |
| `language` | Til | `SELECT` | O'zbek, Rus, Ingliz, Original (subtitr) | — |
| `ageLimit` | Yosh chegarasi | `SELECT` | 0+, 6+, 12+, 16+, 18+ | — |
| `sessionTimes` | Seans vaqtlari | `TAGS` | — | — |

### 16. `KARAOKE` — Karaoke 🎤

- **Rang:** `0xFFEC4899` · **Odatiy narx birligi:** `PER_HOUR`
- **Ruxsat etilgan narx birliklari:** `PER_HOUR`, `PER_PERSON`, `PER_SESSION`
- **Jins:** MALE, FEMALE · **"Hammasiga" yozuvi:** "Barcha xonalar" · **Qo'shimcha guruh taklifi:** "Xona turi"

**Kategoriyalar (4):** `ALL` Barcha xonalar, `STANDARD` Oddiy xona, `VIP` VIP xona, `OTHER` Boshqa

**Atributlar (4):**

| Kalit | Nomi | Turi | Variantlar / birlik | Majburiy |
|---|---|---|---|---|
| `roomCapacity` | Xona sig'imi | `NUMBER` | birlik: kishi | — |
| `sessionMinutes` | Sessiya davomiyligi | `NUMBER` | birlik: daqiqa | — |
| `hasFood` | Taom / ichimlik bor | `BOOLEAN` | — | — |
| `languages` | Qo'shiq tillari | `TAGS` | — | — |

### 17. `EDUCATION_CENTER` — O'quv markaz 📚

- **Rang:** `0xFF3B82F6` · **Odatiy narx birligi:** `PER_MONTH`
- **Ruxsat etilgan narx birliklari:** `PER_MONTH`, `PER_COURSE`, `PER_LESSON`
- **Jins:** MALE, FEMALE · **"Hammasiga" yozuvi:** "Barcha kurslar" · **Qo'shimcha guruh taklifi:** "Yo'nalish, format"

**Kategoriyalar (10):** `ALL` Barcha kurslar, `FOREIGN_LANGUAGES` Chet tillari, `IELTS_CEFR` IELTS / CEFR, `IT_PROGRAMMING` IT va dasturlash, `DESIGN` Dizayn, `MATH_SCIENCE` Matematika va fanlar, `UNIVERSITY_PREP` Abituriyent tayyorlash, `BUSINESS_MARKETING` Biznes va marketing, `MASTER_CLASS` Master-klass, `OTHER` Boshqa

**Atributlar (6):**

| Kalit | Nomi | Turi | Variantlar / birlik | Majburiy |
|---|---|---|---|---|
| `subject` | Yo'nalish | `TEXT` | — | — |
| `level` | Daraja | `SELECT` | Boshlang'ich, O'rta, Yuqori | — |
| `format` | Format | `SELECT` | Offline, Online, Aralash | — |
| `durationMonths` | Davomiyligi | `NUMBER` | birlik: oy | — |
| `lessonsPerWeek` | Haftada | `NUMBER` | birlik: marta | — |
| `hasFreeTrialLesson` | Birinchi dars bepul | `BOOLEAN` | — | — |

### 18. `LIBRARY` — Kutubxona / Co-working 📖

- **Rang:** `0xFF2563EB` · **Odatiy narx birligi:** `PER_MONTH`
- **Ruxsat etilgan narx birliklari:** `PER_MONTH`, `PER_HOUR`, `PER_TICKET`
- **Jins:** MALE, FEMALE · **"Hammasiga" yozuvi:** "Barcha xizmatlar" · **Qo'shimcha guruh taklifi:** "Zona turi"

**Kategoriyalar (6):** `ALL` Barcha xizmatlar, `READING_HALL` O'qish zali, `COWORKING` Co-working, `BOOK_RENT` Kitob ijarasi, `KIDS` Bolalar zonasi, `OTHER` Boshqa

**Atributlar (5):**

| Kalit | Nomi | Turi | Variantlar / birlik | Majburiy |
|---|---|---|---|---|
| `seats` | Joylar soni | `NUMBER` | birlik: joy | — |
| `openHours` | Ish vaqti | `TEXT` | — | — |
| `hasWifi` | Wi-Fi bor | `BOOLEAN` | — | — |
| `hasQuietZone` | Sokin zona bor | `BOOLEAN` | — | — |
| `hasPrinting` | Chop etish xizmati bor | `BOOLEAN` | — | — |

### 19. `TUTOR` — Repetitor 🧑‍🏫

- **Rang:** `0xFF0284C7` · **Odatiy narx birligi:** `PER_LESSON`
- **Ruxsat etilgan narx birliklari:** `PER_LESSON`, `PER_MONTH`, `PER_COURSE`
- **Jins:** MALE, FEMALE · **"Hammasiga" yozuvi:** "Barcha fanlar" · **Qo'shimcha guruh taklifi:** "Fan, format"

**Kategoriyalar (8):** `ALL` Barcha fanlar, `MATH` Matematika, `ENGLISH` Ingliz tili, `PHYSICS` Fizika, `NATIVE_LANG` Ona tili / adabiyot, `IT` Informatika / IT, `EXAM_PREP` Imtihonga tayyorlash, `OTHER` Boshqa

**Atributlar (5):**

| Kalit | Nomi | Turi | Variantlar / birlik | Majburiy |
|---|---|---|---|---|
| `subject` | Fan | `TEXT` | — | — |
| `level` | Daraja | `SELECT` | Boshlang'ich, O'rta, Yuqori, Abituriyent | — |
| `format` | Format | `SELECT` | Offline, Online, O'quvchi uyida | — |
| `lessonMinutes` | Dars davomiyligi | `NUMBER` | birlik: daqiqa | — |
| `hasFreeTrialLesson` | Birinchi dars bepul | `BOOLEAN` | — | — |

### 20. `PRINTING` — Bosmaxona / Tipografiya 🖨️

- **Rang:** `0xFF64748B` · **Odatiy narx birligi:** `PER_ITEM`
- **Ruxsat etilgan narx birliklari:** `PER_ITEM`, `PER_KG`
- **Jins:** MALE, FEMALE · **"Hammasiga" yozuvi:** "Barcha xizmatlar" · **Qo'shimcha guruh taklifi:** "Xizmat turi"

**Kategoriyalar (7):** `ALL` Barcha xizmatlar, `DOCUMENT_PRINT` Hujjat chop etish, `COPY` Nusxa (kseroks), `BANNER` Banner / Nakleyka, `BOOK_BINDING` Muqova / tikish, `PHOTO` Foto chop etish, `OTHER` Boshqa

**Atributlar (4):**

| Kalit | Nomi | Turi | Variantlar / birlik | Majburiy |
|---|---|---|---|---|
| `colorMode` | Rang | `SELECT` | Oq-qora, Rangli | — |
| `paperSize` | Qog'oz o'lchami | `SELECT` | A4, A3, A5, Boshqa | — |
| `minOrder` | Minimal buyurtma | `NUMBER` | birlik: dona | — |
| `express` | Tezkor (express) | `BOOLEAN` | — | — |

### 21. `NATIONAL_FOOD` — Milliy taomlar 🍲

- **Rang:** `0xFFEA580C` · **Odatiy narx birligi:** `PER_ITEM`
- **Ruxsat etilgan narx birliklari:** `PER_ITEM`, `PER_KG`, `PER_PERSON`
- **Jins:** MALE, FEMALE · **"Hammasiga" yozuvi:** "Butun menyu" · **Qo'shimcha guruh taklifi:** "Porsiya, tarkib"

**Kategoriyalar (8):** `ALL` Butun menyu, `PALOV` Osh / Palov, `KABOB` Kabob, `SHORVA` Sho'rva, `MANTI_CHUCHVARA` Manti / Chuchvara, `LAGMON` Lag'mon, `SALAD` Salatlar, `OTHER` Boshqa

**Atributlar (4):**

| Kalit | Nomi | Turi | Variantlar / birlik | Majburiy |
|---|---|---|---|---|
| `portionGrams` | Porsiya | `NUMBER` | birlik: gramm | — |
| `spicyLevel` | O'tkirlik | `SELECT` | Yo'q, Yengil, O'rtacha, O'tkir | — |
| `isHalal` | Halol | `BOOLEAN` | — | — |
| `hasDelivery` | Yetkazib berish bor | `BOOLEAN` | — | — |

### 22. `FAST_FOOD` — Fast food 🍔

- **Rang:** `0xFFF97316` · **Odatiy narx birligi:** `PER_ITEM`
- **Ruxsat etilgan narx birliklari:** `PER_ITEM`, `PER_PERSON`
- **Jins:** MALE, FEMALE · **"Hammasiga" yozuvi:** "Butun menyu" · **Qo'shimcha guruh taklifi:** "Porsiya, tarkib"

**Kategoriyalar (9):** `ALL` Butun menyu, `BURGER` Burger, `PIZZA` Pitsa, `HOTDOG` Hot-dog, `LAVASH_SHAWARMA` Lavash / Shaurma, `FRIES` Kartoshka fri, `COMBO` Combo setlar, `DRINKS` Ichimliklar, `OTHER` Boshqa

**Atributlar (4):**

| Kalit | Nomi | Turi | Variantlar / birlik | Majburiy |
|---|---|---|---|---|
| `portionGrams` | Porsiya | `NUMBER` | birlik: gramm | — |
| `ingredients` | Tarkibi | `TAGS` | — | — |
| `isHalal` | Halol | `BOOLEAN` | — | — |
| `hasDelivery` | Yetkazib berish bor | `BOOLEAN` | — | — |

### 23. `SOMSA` — Somsa / Nonvoyxona 🥟

- **Rang:** `0xFFD97706` · **Odatiy narx birligi:** `PER_ITEM`
- **Ruxsat etilgan narx birliklari:** `PER_ITEM`, `PER_KG`
- **Jins:** MALE, FEMALE · **"Hammasiga" yozuvi:** "Barcha mahsulot" · **Qo'shimcha guruh taklifi:** "To'ldirma, tandir"

**Kategoriyalar (7):** `ALL` Barcha mahsulot, `MEAT_SOMSA` Go'shtli somsa, `POTATO_SOMSA` Kartoshkali somsa, `GREENS_SOMSA` Ko'k somsa, `TANDIR_NON` Tandir non, `PATIR` Patir / Non, `OTHER` Boshqa

**Atributlar (4):**

| Kalit | Nomi | Turi | Variantlar / birlik | Majburiy |
|---|---|---|---|---|
| `filling` | To'ldirma | `SELECT` | Mol go'shti, Qo'y go'shti, Tovuq, Kartoshka, Ko'k | — |
| `ovenType` | Pishirish | `SELECT` | Tandir, Pech | — |
| `isHalal` | Halol | `BOOLEAN` | — | — |
| `hasDelivery` | Yetkazib berish bor | `BOOLEAN` | — | — |

### 24. `BARBERSHOP` — Sartaroshxona 💈

- **Rang:** `0xFF14B8A6` · **Odatiy narx birligi:** `PER_ITEM`
- **Ruxsat etilgan narx birliklari:** `PER_ITEM`, `PER_SESSION`, `PER_PERSON`
- **Jins:** MALE · **"Hammasiga" yozuvi:** "Barcha xizmatlar" · **Qo'shimcha guruh taklifi:** "Usta darajasi"

**Kategoriyalar (8):** `ALL` Barcha xizmatlar, `HAIRCUT_MEN` Erkaklar soch olish, `KIDS` Bolalar soch olish, `BEARD` Soqol / ustara, `HAIR_COLOR` Soch bo'yash, `STYLING` Ukladka / styling, `HAIR_CARE` Parvarish (spa), `OTHER` Boshqa

**Atributlar (4):**

| Kalit | Nomi | Turi | Variantlar / birlik | Majburiy |
|---|---|---|---|---|
| `master` | Usta | `TEXT` | — | — |
| `masterLevel` | Usta darajasi | `SELECT` | Junior, Usta, Top-usta | — |
| `durationMinutes` | Davomiyligi | `NUMBER` | birlik: daqiqa | — |
| `byAppointment` | Oldindan yozilish | `BOOLEAN` | — | — |

### 25. `BEAUTY_SALON` — Go'zallik saloni 💅

- **Rang:** `0xFFF472B6` · **Odatiy narx birligi:** `PER_ITEM`
- **Ruxsat etilgan narx birliklari:** `PER_ITEM`, `PER_SESSION`, `PER_PERSON`
- **Jins:** FEMALE · **"Hammasiga" yozuvi:** "Barcha xizmatlar" · **Qo'shimcha guruh taklifi:** "Usta darajasi"

**Kategoriyalar (10):** `ALL` Barcha xizmatlar, `HAIR` Soch turmagi / bo'yash, `MAKEUP` Makiyaj, `MANICURE` Manikyur, `PEDICURE` Pedikyur, `EYEBROWS_LASHES` Qosh / kiprik, `COSMETOLOGY` Kosmetologiya, `SPA_MASSAGE` SPA / massaj, `EPILATION` Epilyatsiya, `OTHER` Boshqa

**Atributlar (4):**

| Kalit | Nomi | Turi | Variantlar / birlik | Majburiy |
|---|---|---|---|---|
| `master` | Usta | `TEXT` | — | — |
| `masterLevel` | Usta darajasi | `SELECT` | Junior, Usta, Top-usta | — |
| `durationMinutes` | Davomiyligi | `NUMBER` | birlik: daqiqa | — |
| `byAppointment` | Oldindan yozilish | `BOOLEAN` | — | — |

### 26. `RENTAL_HOUSE` — Ijara uy-joy 🏠

- **Rang:** `0xFF0891B2` · **Odatiy narx birligi:** `PER_MONTH`
- **Ruxsat etilgan narx birliklari:** `PER_MONTH`, `PER_PERSON`
- **Jins:** MALE, FEMALE · **"Hammasiga" yozuvi:** "Barcha variantlar" · **Qo'shimcha guruh taklifi:** "Uy turi, xonalar"

**Kategoriyalar (6):** `ALL` Barcha variantlar, `ROOM` Xona (bo'lib turish), `APARTMENT` Kvartira, `HOSTEL` Hostel / Yotoqxona, `STUDIO` Studiya, `OTHER` Boshqa

**Atributlar (7):**

| Kalit | Nomi | Turi | Variantlar / birlik | Majburiy |
|---|---|---|---|---|
| `rooms` | Xonalar soni | `NUMBER` | birlik: xona | — |
| `capacity` | Necha kishiga | `NUMBER` | birlik: kishi | — |
| `forGender` | Kimlar uchun | `SELECT` | Erkaklar, Ayollar, Aralash | — |
| `furnished` | Jihozlangan | `BOOLEAN` | — | — |
| `hasWifi` | Wi-Fi bor | `BOOLEAN` | — | — |
| `utilitiesIncluded` | Kommunal narxga kiritilgan | `BOOLEAN` | — | — |
| `forStudents` | Talabalar uchun | `BOOLEAN` | — | — |

### 27. `CLOTHING` — Kiyim-kechak 👕

- **Rang:** `0xFFDB2777` · **Odatiy narx birligi:** `PER_ITEM`
- **Ruxsat etilgan narx birliklari:** `PER_ITEM`
- **Jins:** MALE, FEMALE · **"Hammasiga" yozuvi:** "Butun assortimentga" · **Qo'shimcha guruh taklifi:** "O'lcham, rang"

**Kategoriyalar (27):** `ALL` Butun assortimentga, `MEN` Erkaklar, `WOMEN` Ayollar, `OUTERWEAR` Ustki kiyim, `SHOES` Poyabzal, `SPORTSWEAR` Sport kiyim, `BAGS` Sumkalar, `ACCESSORIES` Aksessuarlar, `OTHER` Boshqa, `ALL` Butun assortimentga, `SHIRTS` Ko'ylak / futbolka, `PANTS` Shim / jinsi, `SUITS` Kostyum, `OUTERWEAR` Ustki kiyim, `SHOES` Poyabzal, `SPORTSWEAR` Sport kiyim, `ACCESSORIES` Aksessuar, `OTHER` Boshqa, `ALL` Butun assortimentga, `DRESSES` Ko'ylak / libos, `SKIRTS` Yubka, `BLOUSES` Bluzka, `OUTERWEAR` Ustki kiyim, `SHOES` Poyabzal, `BAGS` Sumka, `ACCESSORIES` Aksessuar, `OTHER` Boshqa

**Atributlar (4):**

| Kalit | Nomi | Turi | Variantlar / birlik | Majburiy |
|---|---|---|---|---|
| `brand` | Brend | `TEXT` | — | — |
| `gender` | Kimlar uchun | `SELECT` | Erkaklar, Ayollar, Uniseks, Bolalar | — |
| `material` | Material | `TEXT` | — | — |
| `season` | Mavsum | `SELECT` | Qish, Bahor, Yoz, Kuz, Barcha mavsum | — |

### Jinsga xos kategoriyalar — faqat `CLOTHING`

`CLOTHING` da kategoriya ro'yxati foydalanuvchi jinsiga qarab **almashadi** (umumiy ro'yxat o'rniga):

- **MALE** (9 ta): `ALL` Butun assortimentga, `SHIRTS` Ko'ylak / futbolka, `PANTS` Shim / jinsi, `SUITS` Kostyum, `OUTERWEAR` Ustki kiyim, `SHOES` Poyabzal, `SPORTSWEAR` Sport kiyim, `ACCESSORIES` Aksessuar, `OTHER` Boshqa
- **FEMALE** (9 ta): `ALL` Butun assortimentga, `DRESSES` Ko'ylak / libos, `SKIRTS` Yubka, `BLOUSES` Bluzka, `OUTERWEAR` Ustki kiyim, `SHOES` Poyabzal, `BAGS` Sumka, `ACCESSORIES` Aksessuar, `OTHER` Boshqa

> **Jami:** 27 tur · 174 kategoriya (+ `CLOTHING` uchun jinsga xos 18 ta) · 120 atribut ta'rifi · **74 noyob atribut kaliti**

---

## Ilova B — Guruh → tur moslamasi (8 guruh, 27 tur)

Guruh qatlami **yangi** — hozir na klientda, na seed'da bor. Quyidagi taqsimot klientdagi
`BusinessTypeLabels.kt` ikonka moslamalaridagi de-fakto guruhlashdan olingan va mahsulot
mantiqi bo'yicha biroz aniqlashtirilgan.

⚠️ Bu moslama **bazada** saqlanadi (`business_type.group_key`), kodda emas — adminka orqali
turni boshqa guruhga ko'chirganda ilova yangilanmasligi kerak.

| # | Guruh | `key` | Emoji | Ikonka | Rang | Turlar |
|---|---|---|---|---|---|---|
| 1 | Ovqatlanish | `FOOD` | 🍽 | `cafe` | `#F97316` | `NATIONAL_FOOD`, `FAST_FOOD`, `SOMSA` |
| 2 | Sport | `SPORT` | ⚽ | `ball` | `#16A34A` | `TENNIS`, `TABLE_TENNIS`, `FOOTBALL_FIELD`, `FOOTBALL_TRAINING`, `BASKETBALL`, `VOLLEYBALL`, `SWIMMING_POOL`, `FITNESS`, `BOXING`, `WRESTLING_MMA` |
| 3 | O'yin va bo'sh vaqt | `GAMES` | 🎮 | `gamepad` | `#7C5CFF` | `PLAYSTATION`, `CYBER_CLUB`, `BOWLING`, `BILLIARDS` |
| 4 | Ko'ngilochar | `ENTERTAINMENT` | 🎬 | `camera` | `#EF4444` | `CINEMA`, `KARAOKE` |
| 5 | Ta'lim | `EDUCATION` | 📚 | `book` | `#3B82F6` | `EDUCATION_CENTER`, `LIBRARY`, `TUTOR` |
| 6 | Go'zallik | `BEAUTY` | 💇 | `star` | `#EC4899` | `BARBERSHOP`, `BEAUTY_SALON` |
| 7 | Savdo va xizmat | `SHOPPING` | 🛍 | `cart` | `#06B6D4` | `CLOTHING`, `PRINTING` |
| 8 | Ijara | `HOUSING` | 🏠 | `home` | `#14B8A6` | `RENTAL_HOUSE` |

**Jami: 3 + 10 + 4 + 2 + 3 + 2 + 2 + 1 = 27 ✅**

**Mahsulot qarorlari** — kelishilsa o'zgartirilishi mumkin, lekin bazada, kodda emas:

- `BOWLING` va `BILLIARDS` — klientdagi ikonka moslamasida "sport" ostida, lekin bu yerda
  `GAMES` ga qo'yildi: foydalanuvchi ularni PlayStation bilan bir qatorda, futbol maydoni
  bilan emas, izlaydi.
- `SWIMMING_POOL` — `SPORT` da. Agar dam olish sifatida ko'rilsa `ENTERTAINMENT` ga ko'chsin.
- `LIBRARY` (kutubxona / co-working) — `EDUCATION` da, chunki talaba uni o'qish uchun izlaydi.
- `CLOTHING` + `PRINTING` — ikkalasi ham "xizmat/savdo", lekin bir-biriga uzoq. Agar
  `SHOPPING` ichida bir-biriga yaqin bo'lmasa, `PRINTING` ni alohida `SERVICES` guruhiga
  ajratish mumkin (u holda 9 guruh bo'ladi).

**Jins bo'yicha ko'rinish:** `BARBERSHOP` faqat erkaklarga, `BEAUTY_SALON` faqat ayollarga
ko'rsatiladi (`availableForGenders`). Ya'ni `BEAUTY` guruhi ikkala jins uchun ham ko'rinadi,
lekin ichidagi turlar ro'yxati farq qiladi — `/catalog/types` buni profil jinsiga qarab
filtrlab bersin, `/discounts/search` esa **filtrlamasin** (foydalanuvchi ataylab so'rasa
topsin).

**Guruh sonlarini hisoblash:** `listingsCount` — guruhdagi barcha turlarning ko'rinadigan
e'lonlari yig'indisi (Q4 shartlari bilan). Bu qiymat har so'rovda `COUNT(*)` qilinmasin —
5 daqiqalik kesh yoki materiallashtirilgan ko'rinish ishlatilsin, `geo` berilganda esa
koordinata ~1 km gacha yaxlitlanib kesh kalitiga qo'shilsin.
