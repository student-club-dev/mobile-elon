# Chegirmalar — biznes egalari API: backend javobi

`DISCOUNTS_BUSINESS_API.md` bo'yicha javob. **Hujjatdagi narsalarning katta qismi allaqachon
qurilgan va ishlab turibdi** — biz faqat 7 ta farqni yopdik.

> ⚠️ **Eng muhim ikki eslatma:**
> 1. **`MODERATION_ENABLED` bayrog'i** — hozir **o'chirilgan**. Shu sababli `POST /business`
>    darrov `APPROVED`, `POST /listings/{id}/submit` esa darrov `ACTIVE` qaytaradi. Yoqilganda
>    ikkalasi ham `PENDING_REVIEW` ga tushadi. **Ilova qaytgan `status` ni o'qisin**, hech qaysi
>    holatni oldindan taxmin qilmasin (§3).
> 2. **§4 dagi 7 ta biznes turi ishlatilmaydi.** Bizda katalog **27 ta turdan** iborat va
>    `attributes` bazadan boshqariladi (§5.1).

---

## 0. Qisqacha

| | |
|---|---|
| Yangi endpoint'lar | 7 ta |
| Yangi migratsiya | 2 ta (ikkalasi ham qo'shimcha — mavjud ma'lumot o'zgarmaydi) |
| Moderatsiya | Qurildi, lekin **bayroq bilan o'chirilgan** |
| Kelishmovchiliklar | 7 ta, hammasi §5 da sabab bilan |

---

## 1. Allaqachon bor edi (hech narsa qilishimiz shart emas edi)

Hujjatingizdagi quyidagilar **oldindan ishlab turgan**:

| Hujjat bo'limi | Holat |
|---|---|
| §3.1–3.9 modellar (Business, Branch, Listing, OptionGroup, Option, Redemption, Location) | ✅ PostGIS `geo_point` + GiST indeks bilan |
| §5.3 filiallar CRUD | ✅ |
| §5.4 e'lonlar CRUD + `submit`/`withdraw`/`pause`/`activate`/`duplicate`/`stats`/`redemptions` | ✅ |
| §5.5 `POST /media/upload` | ✅ — **§6.4 dagi "100/soat" limiti ham bor edi** |
| §5.6 `redeem/verify` + `redeem/confirm` | ✅ |
| §5.7 `geo/geocode` + `geo/reverse-geocode` | ✅ |
| §6.1 publish shartlari | ✅ |
| §6.5 avtomatik statuslar (`EXPIRED`, `SOLD_OUT`, `SCHEDULED → ACTIVE`) | ✅ cron |
| §6.6 lokatsiya qoidalari (chegara, 10 km tuman, 100 m dublikat) | ✅ |
| §6.4 e'lon ichidagi limitlar (10 rasm, 10 guruh × 30 variant, `PERCENT ≤ 90`) | ✅ |
| §7 xatolik kodlari | ✅ hammasi |

---

## 2. Yangi qo'shildi

### 2.1 Biznes egasi uchun

| Metod | Yo'l | Javob | Izoh |
|---|---|---|---|
| `POST` | `/v1/business/{businessId}/submit` | `BusinessDto` | `DRAFT \| REJECTED → PENDING_REVIEW`, `rejectionReason` tozalanadi |
| `GET` | `/v1/business/types/{type}/attributes-schema` | `AttributesSchemaDto` | Dinamik forma sxemasi (§5.2) |
| `GET` | `/v1/geo/regions` | `RegionDto[]` | Kontrakt yo'li (§5.3) |
| `GET` | `/v1/geo/regions/{regionId}/districts` | `DistrictDto[]` | Kontrakt yo'li (§5.3) |
| `GET` | `/v1/geo/metro-stations` | `MetroStationDto[]` | Toshkent metrosi — 50 bekat, 4 liniya |

`POST /business/{id}/submit` xatoliklari: `404 BUSINESS_NOT_FOUND` · `403 FORBIDDEN` (o'zganiki) ·
`409 INVALID_STATUS_TRANSITION` (`DRAFT`/`REJECTED` emas).

### 2.2 Moderator (admin panel) uchun

| Metod | Yo'l | Izoh |
|---|---|---|
| `POST` | `/v1/admin/businesses/{id}/approve` | `PENDING_REVIEW → APPROVED` |
| `POST` | `/v1/admin/businesses/{id}/reject` | `{ "reason": "..." }` → `REJECTED` |
| `POST` | `/v1/admin/listings/{id}/approve` | `PENDING_REVIEW → ACTIVE`, yoki `validFrom` kelajakda bo'lsa `SCHEDULED` |
| `POST` | `/v1/admin/listings/{id}/reject` | `{ "reason": "..." }` → `REJECTED` |

Bular **qaror**, tahrir emas — mavjud `PUT /admin/...` dan ataylab ajratilgan, aks holda moderator
tasdiqlash bilan bir vaqtda yozuvni jimgina o'zgartira olardi.

### 2.3 `nearestMetro`

`POST /geo/reverse-geocode` javobidagi `nearestMetro` ilgari **doim `null`** edi. Endi metro bekatlari
bazaga seed qilingani uchun haqiqiy qiymat qaytadi — 3 km dan uzoq bo'lsa `null` (uzoqdagi bekat
mo'ljal emas, adashtiruvchi ma'lumot).

---

## 3. `MODERATION_ENABLED` — ilova nimani ko'radi

Bu **env bayrog'i**, hozir `false`. U faqat holat qayerga tushishini o'zgartiradi —
**barcha tekshiruvlar ikkala holatda ham bir xil ishlaydi**.

| Amal | Bayroq `false` (hozir) | Bayroq `true` |
|---|---|---|
| `POST /business` | `status = APPROVED` | `status = DRAFT` |
| `POST /business/{id}/submit` | `APPROVED` | `PENDING_REVIEW` |
| `POST /listings/{id}/submit` | `ACTIVE` (yoki `SCHEDULED`) | `PENDING_REVIEW` |
| `PUT /listings/{id}` (`ACTIVE` e'lon) | holat o'zgarmaydi | §6.3 bo'yicha `PENDING_REVIEW` ga qaytadi |

**Ilova hech qaysi holatni hardcode qilmasin** — javobdagi `status` ni o'qisin. Bayroq admin panel
navbat bilan ishlashga tayyor bo'lganda yoqiladi; shunda ilova kodi o'zgarmasligi kerak.

### §6.3 qayta moderatsiya

`ACTIVE` e'lon tahrirlanganda qayta moderatsiyaga **tushadi**, agar shular o'zgarsa:
`title`, `description`, `images` (tartibi ham — birinchisi muqova), `originalPrice`, `categoryKey`,
`customCategoryName`, `discount` (`type`/`value`/`conditions`).

**Tushmaydi** (darhol saqlanadi): `branchIds`, `validFrom`/`validTo`, `redemption.*`,
`optionGroups`, `attributes` (shu jumladan `stockCount`, `seatsLeft`), `priceUnit`.

---

## 4. Limitlar (§6.4)

| Limit | Qiymat | Xatolik |
|---|---|---|
| Bir foydalanuvchidagi biznes | 5 | `429 RATE_LIMITED` |
| Bir biznesdagi faol e'lon (`ACTIVE` + `PENDING_REVIEW`) | 100 | `429 LISTING_LIMIT_REACHED` |
| Kuniga `submit` (bir egaga, barcha biznesi bo'yicha) | 50 | `429 RATE_LIMITED` |
| `POST /media/upload` | 100/soat | `429` (avvaldan bor edi) |
| Bir biznesdagi filial | **cheklovsiz** | — |

> Filial limiti: sizning nusxangizda 50 deb yozilgan, kelishilgan `provider/` nusxasida
> "cheklovsiz". Biz kelishilgan nusxaga amal qildik.

> ⚠️ **Kunlik `submit` limiti haqida aniqlik:** u `listings.submitted_at` bo'yicha sanaladi, va bu
> ustun har safar qayta yozilади. Ya'ni amalda **"kuniga 50 ta har xil e'lon yuborildi"** degani,
> "kuniga 50 marta `submit` bosildi" degani emas — bitta e'lonni qayta-qayta yuborish limitga
> bir marta sanaladi. Haqiqiy harakat hisoblagichi alohida jurnal talab qiladi; kerak bo'lsa
> keyingi bosqichda qo'shamiz.

---

## 5. Kelishmovchiliklar — hujjatdan farq qiladigan joylar

### 5.1 §4 dagi 7 ta biznes turi ishlatilmaydi ⚠️

Hujjatda `GAME_CLUB`, `GROCERY`, `CLOTHING`, `CAFE_RESTAURANT`, `EDUCATION_CENTER`,
`ENTERTAINMENT`, `ELECTRONICS` — 7 ta tur, har birida qat'iy `attributes` kalitlari.

Bizda **27 ta tur** bor (`TENNIS`, `FITNESS`, `PLAYSTATION`, `CYBER_CLUB`, `NATIONAL_FOOD`,
`FAST_FOOD`, `SOMSA`, `BARBERSHOP`, `BEAUTY_SALON`, `CINEMA`, `KARAOKE`, `EDUCATION_CENTER`,
`RENTAL_HOUSE`, `CLOTHING`, …), `catalog-seed.json` dan seed qilinadi va `attributes` har bir
**kategoriya** uchun `attribute_specs` jadvalidan keladi.

Ya'ni §4 ni so'zma-so'z bajarish **orqaga qadam** bo'lardi. Muhimi: §10.1 da xohlagan natija —
"yangi maydon qo'shilganda na spec, na ilova o'zgaradi" — bizda allaqachon shunday ishlaydi.

**Turlar va kategoriyalarni `GET /business/types` va `GET /business/types/{type}/categories` dan
oling, hech qachon hardcode qilmang.**

### 5.2 `attributes-schema` JSON Schema emas

§10.1 da JSON Schema so'ralgan. Biz `AttributeFieldDto` qaytaramiz — ya'ni
`GET /business/types/{type}/categories` allaqachon qaytaradigan **o'sha formatda**:

```jsonc
{
  "businessType": "PLAYSTATION",
  "common": [ /* AttributeFieldDto[] — turning barcha e'lonlariga tegishli */ ],
  "byCategory": [
    { "categoryKey": "PS5", "fields": [ /* AttributeFieldDto[] */ ] }
  ]
}
```

Sabab: ilovadagi dinamik forma bu formatni allaqachon o'qiydi. JSON Schema qo'shsak, bitta tushuncha
uchun **ikkita parser** kerak bo'lardi. Formani qurish: `common` + tanlangan kategoriyaning
`fields` i.

### 5.3 Geo yo'llari ko'chirilmadi, qo'shildi

`elon-uz.json` da `/geo/regions` va `/geo/regions/{regionId}/districts`. Bizda `/regions` va
`/districts` allaqachon bor va **admin panelga topshirilgan**
(`docs/api/admin-panel/08-geo.md`) — ularni ko'chirsak, admin panel buziladi.

Shuning uchun **ikkalasi ham ishlaydi**, bitta servis ustida. Siz kontrakt yo'lini (`/geo/regions`)
ishlating.

### 5.4 `GET /discounts` qurilmaydi

§5.8 dagi `GET /discounts?lat=&lng=...` — o'rniga **`POST /v1/discounts/search`**
(`docs/api/client/STUDENT_FEED.md` §2). Sabab: GET query-param modeli feed filtrini ko'tara olmaydi
(`attributes[]` operatorlar bilan, `bbox`, id massivlari). Bu qaror `ENDPOINTS_CHECKLIST.md` §8 da
avvalroq kelishilgan.

### 5.5 Autentifikatsiya Firebase emas

§5 da "Authorization: Bearer <Firebase ID token>" deyilgan. Backend **o'z JWT** (access + refresh)
ini ishlatadi, `students` va `business_owners` alohida jadvallar bilan
(`docs/architecture/auth.md`, D6). Bu ancha oldin kelishilgan — bu yerda faqat hujjatda Firebase
qayd etilgani uchun eslatyapmiz.

### 5.6 `FORBIDDEN_ROLE` va `NOT_BUSINESS_OWNER` alohida kod emas

§7 da ikkita alohida 403 kodi bor. Bizda ikkalasi ham `403 FORBIDDEN` — konvert kontraktida
(`CLAUDE.md`) 403 uchun bitta kod belgilangan, va ilova ikkala holatda ham bir xil ish qiladi:
foydalanuvchiga "ruxsat yo'q" deb ko'rsatadi.

Eslatma: boshqa egaga tegishli resurs uchun **404 emas, 403** qaytariladi (bu ham
`CLAUDE.md` da qat'iy belgilangan) — ya'ni id mavjudligi yashirilmaydi. Bu ataylab: egalik
xatosini "topilmadi" deb ko'rsatish ilovada chalkash xatolik oqimini keltirib chiqaradi.

### 5.7 `geohash` hozir hisoblanmaydi

§3.9 va §6.6.7 da `geohash` (7 belgi) server tomonidan hisoblanishi aytilgan. Bizda yaqinlik
qidiruvi **PostGIS `ST_DWithin` + GiST indeks** bilan ishlaydi (§9 ning o'zi ham shuni tavsiya
qiladi), shuning uchun `geohash` kerak emas va to'ldirilmaydi. Agar ilovaga u aynan kerak bo'lsa —
ayting, qo'shamiz.

---

## 6. Ochiq savol

**`GET /geo/metro-stations` `elon-uz.json` da yo'q.** Biz uni qurdik (§2.1), lekin u OpenAPI
kontraktidan tashqarida. Ikki variant:

1. Spec'ga qo'shasiz → generatsiya qilingan klientda `GeoApi.getMetroStations()` paydo bo'ladi;
2. Yoki kontraktdan tashqari qo'lda chaqirasiz.

Qaysi biri qulay — ayting.

---

## 7. Migratsiyalar

Ikkalasi ham **qo'shimcha**: mavjud ustun ham, ma'lumot ham o'zgarmaydi, `DROP` yo'q.

1. `listings.submitted_at` (nullable) — §6.4 kunlik limit va §6.2 navbat tartibi uchun.
2. `metro_stations` jadvali + 50 ta bekat seed.

`Branch.metroStation` **erkin matn bo'lib qoladi** — FK qilmadik, chunki bazaga hali kirmagan yangi
bekat filial saqlashni buzib qo'yardi. `/geo/metro-stations` faqat autocomplete uchun.
