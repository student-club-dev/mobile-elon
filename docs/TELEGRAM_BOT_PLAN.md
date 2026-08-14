# ElonUz Telegram boti — reja

> Maqsad: biznes egasi **Telegramning o'zidan** biznes ochsin, e'lon qo'ysin, rasm yuklasin,
> statistikani ko'rsin va chegirma kodini tekshirsin — ya'ni ilovaning to'liq muqobili.
>
> Backend talabi (yagona yetishmayotgan qism):
> [`backend/TELEGRAM_BOT_BACKEND.md`](backend/TELEGRAM_BOT_BACKEND.md)

---

## 1. Xulosa

**Mumkin.** API'ning 49 ta yo'lidan biznes oqimi uchun keraklilarining **hammasi tayyor**:
biznes, filial, e'lon, media, geo, statistika, kassir. Bot ularni ilova bilan bir xil
`Authorization: Bearer` bilan chaqiradi.

Yagona printsipial to'siq — **Telegram hisobini ElonUz hisobiga bog'lash**. U backendda
4 ta kichik endpoint bilan yopiladi.

Ikkinchi muhim nuqta: bot — **alohida server xizmati**. Bu repoga qo'shilmaydi; o'z repo'si,
o'z deploy'i va doimiy ishlab turadigan hosti bo'ladi.

---

## 2. Nima uchun bot arziydi

Bot ilovaning nusxasi bo'lgani uchun emas, ayrim ishlarni **ilovadan yaxshiroq** qilgani
uchun kerak:

| Ish | Nega botda qulayroq |
|---|---|
| **Kassir: chegirma kodini tekshirish** | Kassada telefon ilovasini ochib, hisobga kirib o'tirmaydi — kodni chatga yozadi, tamom. Xodimga alohida ilova ham, qurilma ham kerak emas |
| **Rasm yuklash** | Telegram albomi — 10 tagacha rasm bitta xabarda, tayyor siqilgan holda |
| **Joylashuv** | Telegramning "lokatsiya yuborish" tugmasi xaritadan tezroq |
| **Raqamni tasdiqlash** | `request_contact` raqamni Telegramning o'zi tasdiqlaydi — **SMS xarajati yo'q** |
| **Bildirishnoma** | Moderatsiya natijasi, yangi foydalanish — push emas, oddiy xabar |

Aksincha, botda **noqulay** bo'ladigan joylar ham bor va buni oldindan bilib qo'yish kerak:
27 ta biznes turi va uzun kategoriya ro'yxatlari inline tugmalarda sahifalanadi, 7 kunlik
ish vaqti esa qadamma-qadam so'raladi (§5.2 da yechimi bor).

---

## 3. Arxitektura

```
Telegram  ──webhook──▶  Bot xizmati  ──HTTPS + Bearer──▶  api.studentclub.uz/v1
                            │
                            ├── PostgreSQL: telegramId → {access, refresh}, dialog holati
                            └── X-Service-Token bilan: /auth/business/telegram/*
```

**Texnologiya tavsiyasi: Kotlin (JVM) + Ktor.** Sabab shaxsiy did emas:

`dev/api-client` — bu repodagi **generatsiya qilingan** API klienti (OpenAPI'dan). Unga
bitta qator bilan `jvm()` target qo'shilsa, bot **aynan shu klientni va DTO'larni** ishlatadi.
Natijada API o'zgarganda bot bilan ilova bir vaqtda, bir manbadan yangilanadi — qo'lda yozilgan
DTO'lar bilan bo'ladigan "ilovada ishlaydi, botda ishlamaydi" holati umuman chiqmaydi.

Boshqa tilda (Node/Python) yozilsa DTO'lar qo'lda takrorlanadi — bu birinchi oyda sezilmaydi,
keyin doimiy nomuvofiqlik manbasiga aylanadi.

**Holat (state) saqlash.** Suhbat ko'p qadamli, shuning uchun "foydalanuvchi hozir qaysi
qadamda" degan holat DB'da turishi shart — xotirada saqlansa, bot qayta ishga tushganda
hamma yarim to'ldirilgan forma yo'qoladi.

**Tokenlar.** `access` + `refresh` `telegramId` bo'yicha saqlanadi, `TOKEN_EXPIRED` da
`/auth/business/refresh` bilan yangilanadi — ilovadagi bilan bir xil mantiq.

---

## 4. Bosqichlar

Bosqichlar ataylab shunday tartibda: **har biri o'zi alohida foyda beradi** va oldingisini
kutmasdan ishga tushirilishi mumkin.

### 1-bosqich — Kirish + kassir (eng katta foyda, eng kam ish)

- `/start` → raqam so'rash (`request_contact`) → `telegram/contact` → sessiya
- Ilovadan bog'lash kodi bilan kirish (Google hisoblari uchun)
- **Kod tekshirish:** biznes tanlash → kodni yozish → `redeem/verify` → tasdiqlash →
  `redeem/confirm`
- `/logout`

Shu bosqichning o'zi kassirlar muammosini butunlay yopadi. Backenddan faqat §4 endpointlari
kerak.

### 2-bosqich — Ko'rish va boshqarish

- `/business` — bizneslar ro'yxati (`GET /business/my`)
- `/listings` — e'lonlar (`GET /business/{id}/listings`), sahifalash
- E'lonni to'xtatish / yoqish / o'chirish (`pause` · `activate` · `DELETE`)
- `/stats` — statistika (`GET /listings/{id}/stats` · `/redemptions`)

Yangi backend talab qilinmaydi.

### 3-bosqich — E'lon qo'yish

- Chegirma/oddiy → kategoriya → nom → tavsif → **rasmlar (albom)** → narx → telefon
- `POST /business/{id}/listings` → `POST /listings/{id}/submit`
- Rasm: Telegram `file_id` → yuklab olish → `POST /media/upload`
- Tahrirlash: `PUT /listings/{id}`

Yangi backend talab qilinmaydi.

### 4-bosqich — Biznes ochish

- Tur → nom → telefon → viloyat/tuman → **lokatsiya** → filial nomi → ish vaqti → logo
- `POST /business` → `POST /business/{id}/branches` → `POST /business/{id}/submit`

Yangi backend talab qilinmaydi.

### 5-bosqich — Bildirishnomalar (ixtiyoriy)

- Moderatsiya natijasi, yangi foydalanish, limitga yaqinlashuv
- Backendda webhook yoki navbat kerak — alohida kelishiladi

---

## 5. Qiyin joylar va yechimlari

### 5.1. Uzun ro'yxatlar (27 tur, kategoriyalar, 14 viloyat, tumanlar)

Inline tugmalar 8 tadan sahifalanadi + **matn bilan qidirish**: foydalanuvchi "barber" deb
yozsa, mos turlar ko'rsatiladi. Bu ilovadagi qidiruvli sheet bilan bir xil naqsh.

### 5.2. Ish vaqti — 7 kun

Qadamma-qadam so'rash charchatadi. Yechim — **tayyor shablonlar**:

- «Har kuni 09:00–22:00»
- «Du–Sha 09:00–18:00, Yakshanba yopiq»
- «Qo'lda kiritish» — faqat shu variantda 7 qadam

Amalda bizneslarning aksariyati birinchi ikkitasidan birini tanlaydi.

### 5.3. Rasmlar albomi

Telegram albomni **bir nechta alohida xabar** qilib yuboradi (`media_group_id` bilan). Ularni
~1 soniya kutib yig'ish va keyin birdaniga qayta ishlash kerak — aks holda har rasm alohida
e'lon bo'lib ketadi.

### 5.4. Biznes nomi — faqat lotin

Ilovada `AppFieldType.LatinText` kirill harflarini kiritishga qo'ymaydi. Botda esa
foydalanuvchi istalgan narsani yozadi — shuning uchun **bot tomonida tekshirish** va
tushunarli xabar berish kerak, aks holda server `422` qaytaradi va sabab noaniq qoladi.

### 5.5. Narx

Ilovada summa `50 000` bo'lib formatlanadi. Botda foydalanuvchi `50000`, `50 000`, `50.000`
deb yozishi mumkin — hammasidan raqamlarni ajratib olish kerak.

---

## 6. Nima **qilinmaydi**

Aniqlik uchun, bot qamroviga kirmaydiganlar:

- **Talaba tomoni** (chegirmalarni ko'rish, qidirish) — alohida bot yoki alohida bosqich
- **Moderatsiya** — u admin panelda
- **Xarita ko'rinishi** — Telegram statik lokatsiya beradi, filtrli xarita bermaydi
- **Ilovani almashtirish** — bot uni to'ldiradi, o'rnini bosmaydi

---

## 7. Keyingi qadam

1. Backend jamoasiga [`backend/TELEGRAM_BOT_BACKEND.md`](backend/TELEGRAM_BOT_BACKEND.md)
   beriladi — u yerdagi 4 ta endpoint va xizmat kaliti.
2. Bot uchun **alohida repo** ochiladi (masalan `student-club-dev/elonuz-bot`).
3. `dev/api-client` ga `jvm()` target qo'shiladi — bot generatsiya qilingan klientni
   qayta ishlatishi uchun.
4. 1-bosqich yoziladi va sinov botida tekshiriladi.

> 1-bosqich backend endpointlari tayyor bo'lgach boshlanadi. 2–4-bosqichlar esa **hozirdanoq**
> boshlanishi mumkin — ularga faqat mavjud API kerak, sinov uchun token qo'lda qo'yiladi.
