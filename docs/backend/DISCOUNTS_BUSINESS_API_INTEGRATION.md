# Chegirmalar — biznes API integratsiyasi: ilova tomonining javobi

`DISCOUNTS_BUSINESS_API_RESPONSE.md` va yangi `business.json` spec'i bo'yicha ilovada
bajarilgan ish. Qisqasi: **yangi endpointlarning hammasi ulandi**, §6 dagi ochiq savolga
javob berildi va bitta yangi kelishmovchilik topildi (§5).

---

## 1. Spec va klient

`dev/api-client-generator/elon-uz.json` yangi `business.json` bilan almashtirildi va klient
qaytadan generatsiya qilindi (`./gradlew :dev:api-client-generator:generateAllApi`).

Generatsiya quvuriga (`cleanSwagger`) uchta o'zgarish kirdi:

| # | Nima | Nega |
|---|---|---|
| 1 | **Ishlatilmaydigan sxemalar kesiladi** | Yangi spec'da `components.schemas` ichida admin panel va talaba ilovasining 200 dan ortiq modeli bor (`Admin*`, `Story*`, `Search*`…), 48 ta yo'lning birortasi ularga tegmaydi. Kesilmasa klient shuncha keraksiz fayl bilan shishardi. Endi yo'llardan tranzitiv yopilma olinadi: **279 → 81 model**. |
| 2 | **Kasrli `example` `double` ni ustun qiladi** | `MetroStationDto.lat/lng` — formatsiz `number`, lekin qiymati `41.27436`. Eski qoida ("formatsiz `number` = butun son") ularni `Int` qilib qo'yardi va **javob parse bo'lmasdi**. Endi `example` kasrli bo'lsa maydon `Double` bo'lib qoladi. |
| 3 | **To'qnashgan `operationId` larga qo'lda nom** | Yangi spec'da `submit`, `verify`, `getRegions`, `getDistricts` ikki martadan uchraydi. Avtomatik prefikslash `listingSubmitSubmit` kabi nomlar berardi va mavjud chaqiruv joylarini bekorga sindirardi. Endi ular `build.gradle.kts` dagi `operationNames` jadvalida: `submit` (e'lon) / `businessSubmit`, `verify` (OTP) / `redeemVerify`, `getRegions` (eski) / `getGeoRegions`. |

---

## 2. Ulangan endpointlar

| Endpoint | Ilovadagi joyi |
|---|---|
| `POST /business/{id}/submit` | "Bizneslarim" kartasidagi **"Tekshiruvga yuborish"** tugmasi |
| `GET /business/types/{type}/attributes-schema` | E'lon formasi — turning **umumiy** maydonlari kategoriya maydonlariga qo'shiladi |
| `GET /geo/regions` · `/regions/{id}/districts` | Filial formasi (kontrakt yo'llari) |
| `GET /geo/metro-stations` | Filial formasi — metro mo'ljali (faqat Toshkent) |
| `POST /listings/{id}/pause` · `/activate` · `/withdraw` · `/duplicate` | E'lon kartasi va "Boshqa amallar" menyusi |
| `GET /listings/{id}/stats` · `/redemptions` | "Statistika" oynasi |
| `POST /listings/{id}/redeem/verify` · `/redeem/confirm` | Kassir oynasi |

### 2.1 Eng muhim tuzatish: holat endi **serverda** o'zgaradi

Ilgari `pause`/`activate` faqat **local bazaga** yozardi. Oqibati: biznes egasi e'lonni
"to'xtatdim" deb ko'rar, serverда esa u hamon `ACTIVE` bo'lib turar va talabalar uni
ko'rishда davom etardi.

Endi so'rov serverga ketadi va **qaytgan status keshga aynan yoziladi** — kutilgani emas.
Bu §3 dagi "ilova hech qaysi holatni hardcode qilmasin" talabining bevosita bajarilishi:

- `activate` boshlanish sanasi kelajakda bo'lsa `SCHEDULED` qaytaradi — karta shuni ko'rsatadi;
- server rad etsa (muddati o'tgan e'lon) kesh **tegilmaydi** va sabab foydalanuvchiga chiqadi.

Bu `ListingRepositoryFlowTest` bilan qulflangan. (`FallbackListingRemoteDataSourceTest` olib
tashlandi — local zaxira manbasining o'zi endi yo'q, e'lon faqat backendда yaratiladi.)

### 2.2 `MODERATION_ENABLED` — hech qayerda taxmin yo'q

`POST /business/{id}/submit` javobidagi `status` o'qiladi va foydalanuvchiga o'sha
ko'rsatiladi ("Tekshiruvga yuborildi — Tasdiqlangan" yoki "— Moderatsiyada"). Bayroq
yoqilganda **ilova kodi o'zgarmaydi**.

`rejectionReason` ham ulandi: `REJECTED` biznesda sabab kartada ko'rinadi, aks holda
foydalanuvchi nimani tuzatishni bilmasdi.

### 2.3 Chegaralar (§4)

`429` endi ilovada **alohida tur** — `AppException.LimitReached`, validatsiya xatosi emas:
chegara foydalanuvchi kiritgan ma'lumot haqida emas, shuning uchun forma maydonlarni
qizartirmaydi. `error.code` o'qiladi va serverning xabari ostiga amaliy maslahat qo'shiladi
(eskisini arxivlash / e'lonni to'xtatish / ertaga davom ettirish).

---

## 3. §6 dagi ochiq savolga javob: `GET /geo/metro-stations`

**Spec'ga qo'shilgani ma'qul — va u allaqachon qo'shilgan.** Yangi `business.json` da yo'l
bor, klientda `GeoApi.getMetroStations()` paydo bo'ldi. Qo'lda chaqirishga hojat qolmadi.

---

## 4. §5 dagi kelishmovchiliklar — hammasi qabul qilindi

| Bo'lim | Qaror |
|---|---|
| §5.1 27 ta tur, `attributes` bazadan | ✅ Ilova turlarni **hardcode qilmaydi** — `GET /business/types` dan oladi va serverdagi har bir tur o'zgarishsiz o'tadi. |
| §5.2 `AttributeFieldDto`, JSON Schema emas | ✅ Aynan shu format o'qiladi, ikkinchi parser yo'q. |
| §5.3 Geo yo'llari | ✅ Kontrakt yo'li (`/geo/regions`) ishlatiladi. **Bitta istisno** pastda (§5.1). |
| §5.4 `GET /discounts` qurilmaydi | ✅ Bu biznes ilovasi, feed'ni chaqirmaydi. |
| §5.5 Firebase emas, o'z JWT | ✅ Firebase ancha oldin olib tashlangan. `ENDPOINTS_CHECKLIST.md` §0 shunga qarab tuzatildi. |
| §5.6 Bitta `403` kodi, begona resurs uchun ham 403 | ✅ Ilova ikkalasida ham "ruxsat yo'q" deydi. |
| §5.7 `geohash` hisoblanmaydi | ✅ Ilovaga kerak emas — yaqinlik hisoblari xaritada va serverda. |

### 4.1 Tumanlar: bitta joyda eski yo'l ishlatiladi

`RegionRepository.regions()` **hamma** viloyatni tumanlari bilan bir marta oladi. Kontrakt
yo'li (`/geo/regions/{regionId}/districts`) bitta viloyatniki, ya'ni forma ochilishida 14 ta
so'rov ketardi — shuning uchun ommaviy yuklashда `GET /districts` ishlatiladi.

Kontrakt yo'li tashlab qo'yilgani yo'q: kesh bo'sh qolsa (`/districts` uzilsa) yakka viloyat
uchun aynan `/geo/regions/{regionId}/districts` chaqiriladi.

> **So'rov:** `/geo/regions` javobiga tumanlarni ham qo'shsangiz (yoki `/geo/districts`
> qo'shsangiz) bu istisno umuman kerak bo'lmaydi.

---

## 5. Yangi kelishmovchilik: `POST /profile/photos` biznes ilovasida ishlamaydi ⛔️

`AddProfilePhotoDto.mediaId` — hujjatga ko'ra `POST /v1/media/chat-upload` (`kind=PROFILE_PHOTO`)
qaytaradigan id. Lekin **`/media/chat-upload` biznes spec'ida yo'q**: `business.json` da faqat
`POST /media/upload` bor, u esa `{ url, thumbUrl, cardUrl }` qaytaradi — `id` bermaydi.

Ya'ni biznes ilovasi profil rasmini **qo'sha olmaydi**. Qolgan uchtasi (`GET /profile/photos`,
`PUT /profile/photos/{id}/main`, `DELETE /profile/photos/{id}`) texnik jihatdan ishlaydi, lekin
ular ustida ishlaydigan rasm hech qachon paydo bo'lmaydi.

**Shu sababli bu bo'lim ilovada qurilmadi** — ishlamaydigan galereya qo'shishdan ko'ra ochiq
savol qoldirish to'g'riroq. Hozircha avatar avvalgidek ishlaydi:
`POST /media/upload` → `PUT /profile/me { avatarUrl }`.

**Ikki variantdan qaysi biri to'g'ri, ayting:**

1. `/media/chat-upload` (yoki unga o'xshash, `id` qaytaradigan yo'l) biznes spec'iga ham
   qo'shiladi — shunda galereyani quramiz;
2. yoki `/profile/photos` biznes egalari uchun umuman mo'ljallanmagan (faqat talaba ilovasi
   uchun) — shunda uni biznes spec'idan olib tashlang, chalkashlik bo'lmasin.

Bog'liq savol: `ProfilePhotoListDto` izohида "`avatarUrl` shu ro'yxatdan **hosil qilinadi**,
alohida saqlanmaydi" deyilgan. Agar bu biznes egalariga ham tegishli bo'lsa,
`PUT /profile/me { avatarUrl }` qachondir jimgina ishlamay qolishi mumkin. Hozir u ishlayapti —
shundayligicha qoladimi?

---

## 6. Tekshirilgan

- `./gradlew :elonUzApp:assembleDebug` — ✅
- Barcha unit testlar (142 ta) — ✅, shu jumladan holat o'zgarishi uchun yangi 5 tasi.
- ⚠️ `linkDebugFrameworkIosArm64` yiqiladi (SQLite cinterop / Xcode toolchain) — bu
  **o'zgarishlarimizdan oldin ham** shunday edi, tekshirildi.
