# ElonUz — API Response Format (Backend uchun)

Bu hujjat **backenddan qanday javob kelishi kerakligini** belgilaydi. ElonUz ilovasi
(KMP klient) har bir API javobини **yagona standart konvertда** kutadi — `BaseResponse`.
Klient hech qachon "yalang'och" JSON kutmaydi: status va xato bir joyда — **`ResponseChecker`**
(Checker) — tekshiriladi.

> Klient tomonda: `dev/core/network/response/` — `BaseResponse.kt`, `ResponseChecker.kt`, `SafeApiCall.kt`.

---

## 1. Umumiy qoida

**Har bir endpoint** (muvaffaqiyat ham, xato ham) shu konvertда qaytaradi:

```json
{
  "success": true,
  "status": 200,
  "code": null,
  "message": "OK",
  "result": { },
  "error": null
}
```

- **Foydali yuk** har doim `result` ichида (yoki `data` — ikkalasи ham qabul qilinadi).
- **Xato** `error` ichида yoki `status` orqali bildiriladi.
- Klient hech qachon HTTP tanasини to'g'ridan-to'g'ri o'qimaydi — avval konvertни tekshiradi.

---

## 2. Konvert maydonlari

| Maydon | Turi | Majburiy | Ma'nosi |
|--------|------|:--------:|---------|
| `success` | boolean | ✔️ | Amaliyot muvaffaqiyatlimi |
| `status` | int | ✔️ | Status kodi (HTTP kod bilan mos bo'lsin) |
| `code` | string \| null | ➖ | Mashina uchun kod (`"TOKEN_EXPIRED"` va h.k.) |
| `message` | string \| null | ➖ | Umumiy, foydalanuvchiga ko'rsatiladigan matn |
| `result` | T \| null | muvaffaqiyатда | Foydali yuk (obyekt/massiv). `data` deb ham nomlash mumkin |
| `error` | object \| null | xatoда | `{ code, message, fields }` |

**`error` obyekti:**

| Maydon | Turi | Ma'nosi |
|--------|------|---------|
| `code` | string \| null | Xato kodi (`"VALIDATION_ERROR"`) |
| `message` | string \| null | Foydalanuvchiga matn |
| `fields` | map<string,string> | Maydonga bog'langan validatsiya xatolari |

---

## 3. Muvaffaqiyatли javoblar

### 3.1. Obyekt qaytarish

```json
{
  "success": true,
  "status": 200,
  "message": "OK",
  "result": {
    "id": "biz_123",
    "name": "Kafe Aurora",
    "type": "CAFE_RESTAURANT"
  }
}
```

### 3.2. Massiv (ro'yxat) qaytarish

```json
{
  "success": true,
  "status": 200,
  "result": {
    "categories": [
      { "id": "food", "name": "Ovqat", "emoji": "🍕", "offerCount": 12 }
    ],
    "offers": [
      { "id": "o1", "merchant": "Evos", "title": "Barcha lavashlarga", "discountPercent": 30 }
    ]
  }
}
```

### 3.3. Bo'sh natija (204 mazmuni)

```json
{ "success": true, "status": 200, "result": null, "message": "O'chirildi" }
```

---

## 4. Xato javoblar

Xatoда ham **HTTP status kodi** mos bo'lsin (masalan 401 → HTTP 401), `status` maydoni ham
o'sha kodni takrorlaydi. Klient `status` bo'yicha typed xatoga aylantiradi.

### 4.1. Avtorizatsiya (401)

```json
{
  "success": false,
  "status": 401,
  "message": "Sessiya tugagan",
  "error": { "code": "TOKEN_EXPIRED", "message": "Qaytadan kiring", "fields": {} }
}
```

### 4.2. Ruxsat yo'q (403)

```json
{
  "success": false,
  "status": 403,
  "error": { "code": "FORBIDDEN", "message": "Bu amal uchun ruxsat yo'q" }
}
```

### 4.3. Topilmadi (404)

```json
{
  "success": false,
  "status": 404,
  "error": { "code": "NOT_FOUND", "message": "E'lon topilmadi" }
}
```

### 4.4. Validatsiya (422 / 400) — maydon xatolari bilan

```json
{
  "success": false,
  "status": 422,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Ma'lumot noto'g'ri",
    "fields": {
      "phone": "Telefon raqami noto'g'ri",
      "price": "Narx 0 dan katta bo'lsin"
    }
  }
}
```

### 4.5. Server xatosi (5xx)

```json
{
  "success": false,
  "status": 500,
  "error": { "code": "INTERNAL", "message": "Serverда xatolik" }
}
```

---

## 5. `status` → klient xatti-harakati (Checker mantiqi)

| Backend `status` | Klient natijasi (`AppException`) | UI |
|------------------|----------------------------------|----|
| `200–299` + result | ✅ `result` ishlatiladi | Kontent |
| `401` | `Unauthorized` | "Qaytadan kiring" → login |
| `403` | `PermissionDenied` | "Ruxsat yo'q" |
| `404` | `NotFound` | "Topilmadi" |
| `408` | `Timeout` | "Qayta urinish" |
| `400–499` (boshqa) | `Validation(message)` | Xato matni / maydon xatolari |
| `500–599` | `Server` | "Birozdan so'ng qayta urining" |
| tarmoq yo'q | `NoInternet` (klient aniqlaydi) | Offline banner + retry |

---

## 6. Avtorizatsiya

Har so'rovga **Firebase ID token** `Authorization` sarlavhasида qo'shiladi:

```
Authorization: Bearer <firebase_id_token>
```

- Backend tokenni Firebase Admin SDK bilan tekshiradi (`verifyIdToken`).
- Token muddati o'tган bo'lsa `401` + `code: "TOKEN_EXPIRED"` qaytaring — klient tokenni
  avtomatik yangilab, so'rovni takrorlaydi (Ktor `Auth` plagini).
- `uid` tokendan olinadi (`ownerId` sifatida ishlatiladi).

---

## 7. Sahifalash (list endpoint'lar uchun tavsiya)

Ro'yxatlar uchun `result` ichида meta bilan qaytaring:

```json
{
  "success": true,
  "status": 200,
  "result": {
    "items": [ /* ... */ ],
    "page": 1,
    "pageSize": 20,
    "total": 137,
    "hasMore": true
  }
}
```

So'rov: `GET /listings?page=1&pageSize=20`.

---

## 8. Klient bu javobni qanday iste'mol qiladi

Data-source **hech qachon** `if (status == 200)` yozmaydi — `safeApiCall` + Checker hal qiladi:

```kotlin
suspend fun fetchDiscounts(): Resource<DiscountsResponseDto> =
    safeApiCall(connectivity) {
        client.get("discounts").body<BaseResponse<DiscountsResponseDto>>()
        //                            ↑ konvert          ↑ result turi
    }
// → Resource.Success(result)  yoki  Resource.Error(typed AppException)
```

Oqim: **so'rov → internet tekshiruvi → `BaseResponse` → Checker (`status`/`result`) →
`Resource<T>` (typed xato bilan) → ViewModel `UiState` (loading/content/empty/error) → UI.**

---

## 9. Backend dasturchi uchun qisqa qoidalar

1. **Har javob** `BaseResponse` konvertида — istisno yo'q.
2. Muvaffaqiyат: `success: true`, `status: 2xx`, foydali yuk `result` ичида.
3. Xato: `success: false`, `status` mos kod, sabab `error.message` (foydalanuvchiga o'zbekcha).
4. HTTP status kodi **va** `status` maydoni bir xil bo'lsin.
5. Validatsiya xatolari `error.fields` да (maydon nomi → xato matni).
6. `message` — doim **foydalanuvchi ko'radigan**, tushunarli matn (log matni emas).
7. Sana/vaqt — **epoch millisekund** (klient shunday kutadi).
8. Pul — **butun son, tiyinsiz** (so'mда), `currency: "UZS"`.

---

_Klient referens implementatsiya: `KtorDiscountRemoteDataSource` — boshqa endpoint'lar shundan
nusxa oladi. Konvert/Checker: `dev/core/network/response/`._
