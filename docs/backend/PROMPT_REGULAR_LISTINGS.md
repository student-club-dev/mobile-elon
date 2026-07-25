# O'zgartirish so'rovi — feed chegirmasiz e'lonlarni ham qamrasin

> Backendga beriladigan qisqa topshiriq. `DISCOUNTS_SEARCH_PROMPT.md` ga qo'shimcha:
> o'sha spetsifikatsiya kuchida qoladi, bu fayl faqat shu bitta xatoni tuzatadi.

---

## PROMPT BOSHLANISHI

Chegirmalar qidiruvida (`POST /v1/discounts/search` va unga bog'liq endpointlar) bitta
asosiy noto'g'ri taxmin bor: go'yo har bir e'lon chegirmali. Bu **noto'g'ri**.

### Haqiqiy holat

Biznes egasi e'lon qo'yayotganda **rejimni o'zi tanlaydi**:

| Rejim | Nima saqlanadi | Chegirma |
|---|---|---|
| **Chegirma e'loni** | `attributes` da `_regular` yo'q | `discount` to'ldirilgan va validatsiyadan o'tgan |
| **Oddiy e'lon** | `attributes._regular = "1"` | `discount` **ma'nosiz** — validatsiya umuman o'tkazilmaydi |

Klientda bu allaqachon shunday ishlaydi:
- `PostListingViewModel.onListingMode(discount: Boolean)` — foydalanuvchi tanlaydi
- `PostListingViewModel` e'lonni yig'ayotganda: `if (!isDiscount) attributes += ("_regular" to "1")`
- `ListingValidator.kt:83` — `_regular == "1"` bo'lsa chegirma tekshiruvi butunlay o'tkazib yuboriladi
- `Listing.isDiscount` = `attributes["_regular"] != "1"`
- "Mening e'lonlarim" ekrani ikkalasini alohida filtrlab ko'rsatadi

Ya'ni bazada allaqachon **ikki xil e'lon bor** va talaba ikkalasini ham topa olishi kerak:
"PS5 1 soat — 20 000 so'm" (oddiy narx) ham, "PS5 1 soat — −30%" (chegirma) ham.

> Eslatma: `DISCOUNTS_BUSINESS_API.md` §1 da "chegirmasiz e'lon bo'lmaydi" deb yozilgan —
> bu **eskirgan**. Klient kodi ustun.

### Nima o'zgartirilsin

**1. Yangi filtr maydoni — `filter.listingKind`**

```jsonc
"listingKind": "ALL"    // ALL (default) | DISCOUNT | REGULAR
```

| Qiymat | SQL sharti |
|---|---|
| `ALL` (default) | filtrsiz — ikkalasi ham |
| `DISCOUNT` | `attributes->>'_regular' IS DISTINCT FROM '1'` |
| `REGULAR` | `attributes->>'_regular' = '1'` |

Eski `filter.discount.onlyDiscounted` maydonini olib tashlang — uning o'rnini shu egalladi.
**Default `ALL`** — qidiruv hech qachon jimgina oddiy e'lonlarni yashirmasin.

**2. Javob sxemasi chegirmasiz e'lonni ko'tarsin**

Karta modelida quyidagilar oddiy e'londa `null` bo'ladi — `0` yoki soxta qiymat qo'ymang:

```jsonc
{
  "isDiscount": false,
  "originalPrice": 20000,
  "finalPrice": 20000,        // = originalPrice
  "savedAmount": null,
  "discount": null,           // butun obyekt null
  "priceUnit": "PER_HOUR"
}
```

Chegirmalisida esa avvalgidek to'liq: `discount: { type, value, badge, conditions }`,
`savedAmount`, `finalPrice < originalPrice`.

**3. Saralash oddiy e'lonlarni tashlab yubormasin**

`DISCOUNT_PERCENT` va `SAVED_AMOUNT` oddiy e'lonlarga tegishli emas. Ular tanlanganda
oddiy e'lonlar **ro'yxatdan chiqib ketmasin**, balki oxiriga tushsin — `ORDER BY ... DESC
NULLS LAST, id ASC`. Qolgan saralashlar (`DISTANCE`, `PRICE_FINAL`, `NEWEST`,
`ENDING_SOON`, `POPULAR`) ikkalasi uchun ham bir xil ishlaydi.

**4. `filter-schema` va `facets` da e'lon turi ko'rinsin**

`POST /v1/catalog/filter-schema` javobiga qo'shing:

```jsonc
"listingKind": {
  "label": "E'lon turi",
  "values": [
    { "key": "ALL",      "label": "Hammasi",     "count": 312 },
    { "key": "DISCOUNT", "label": "Chegirmali",  "count": 188 },
    { "key": "REGULAR",  "label": "Chegirmasiz", "count": 124 }
  ]
}
```

Xuddi shunday `mode: "COUNT"` javobining `facets` iga `byListingKind` qo'shilsin.

**5. Xaritada farqlansin**

`mode: "MAP"` markerlarida `isDiscount` bayrog'i bo'lsin. Chegirmalisi `discountBadge`
("−30%") bilan, oddiysi faqat narx yorlig'i bilan ("20k") chiqadi. Ikkalasi ham xaritada
ko'rinadi — oddiy e'lonlar xaritadan tushib qolmasin.

**6. Nomlash (ixtiyoriy, lekin tavsiya)**

`discounts` nomi endi noto'g'ri — endpoint ikkala turni beradi. Agar backend hali
ishga tushmagan bo'lsa `/v1/feed/search`, `/v1/feed/detail`, `/v1/feed/suggest` ga
o'tkazing (`/v1/listings/*` bilan to'qnashmaydi — u biznes tomoni). Agar allaqachon
ishlab turgan bo'lsa, yo'lni o'zgartirmang: **muhimi xatti-harakat, nom emas.**

### Qabul mezonlari

1. `listingKind` berilmagan so'rov chegirmali **va** chegirmasiz e'lonlarni birga qaytaradi.
2. `listingKind: "REGULAR"` faqat `_regular = "1"` bo'lganlarni beradi; ularning
   `discount` maydoni `null`.
3. `listingKind: "DISCOUNT"` faqat chegirmalilarni beradi.
4. Uchala rejimning `total` yig'indisi: `DISCOUNT + REGULAR = ALL`.
5. `sort=DISCOUNT_PERCENT` da oddiy e'lonlar yo'qolmaydi — oxirida turadi.
6. `mode=MAP` da oddiy e'lonlar ham marker sifatida chiqadi.
7. `filter-schema` dagi uchta `count` real sonlarga to'g'ri keladi.

## PROMPT TUGADI
