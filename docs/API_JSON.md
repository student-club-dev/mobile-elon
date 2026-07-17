# Adminkadan keladigan JSON

**Bitta jumlada:** ilovadagi to'ldiriladigan hamma narsa — biznes turlari, kategoriyalar,
PS5/PS4/PS3 kabi tanlovlar — backenddan keladi. Kodда hech narsa qattiq yozilmaydi.

## Faqat 2 ta endpoint kerak

| Ekran | So'rov | Nima qaytadi |
|---|---|---|
| 🏪 **Biznes qo'shish** | `GET /business/types` | 7 ta biznes turi |
| 📝 **E'lon qo'shish** | `GET /business/types/{type}/categories` | Kategoriyalar + ичидаgi barcha tanlovlar |

Javob har doim `BaseResponse` konvertида: `{ "success", "status", "result", "error" }`

---

# 1️⃣ Biznes qo'shish

```http
GET /business/types
```

**Nima qaytadi — 7 ta chip:**

```
[🎮 Game Club]  [👕 Kiyim]  [🍕 Kafe]  [📚 O'quv markaz]
[🎬 Kino]  [💈 Sartaroshxona]  [💅 Go'zallik saloni]
```

**JSON — bu TEKIS ro'yxat** (biri ikkinchisining ичида emas):

```json
{
  "success": true,
  "status": 200,
  "result": [
    { "type": "GAME_CLUB",  "nameUz": "Game Club",  "emoji": "🎮", "accentColor": "#7C5CFF",
      "defaultPriceUnit": "PER_HOUR", "priceUnits": ["PER_HOUR","PER_SESSION","PER_PERSON"] },

    { "type": "CLOTHING",   "nameUz": "Kiyim-kechak", "emoji": "👕", "accentColor": "#EC4899",
      "defaultPriceUnit": "PER_ITEM", "priceUnits": ["PER_ITEM"] },

    { "type": "BARBERSHOP", "nameUz": "Sartaroshxona", "emoji": "💈", "accentColor": "#14B8A6",
      "defaultPriceUnit": "PER_ITEM", "priceUnits": ["PER_ITEM","PER_SESSION","PER_PERSON"] }

    // … qolgan 4 tur ham xuddi shu shaklда
  ],
  "error": null
}
```

📄 [`GET_business-types.json`](api/responses/GET_business-types.json)

## Jins bo'yicha filtr

| So'rov | Nechta | Nima farqi |
|---|---|---|
| `GET /business/types` | 7 | Hammasi |
| `GET /business/types?gender=MALE` | 6 | 💅 Go'zallik saloni yo'q |
| `GET /business/types?gender=FEMALE` | 6 | 💈 Sartaroshxona yo'q |

📄 [`_MALE`](api/responses/GET_business-types_MALE.json) · [`_FEMALE`](api/responses/GET_business-types_FEMALE.json)

---

# 2️⃣ E'lon qo'shish — ichma-ich listlar

Bu asosiy qism. **Bitta so'rov** — hamma narsa keladi.

## Tuzilma — 3 qavat

```
GET /business/types/GAME_CLUB/categories
                    ↓
result[]  ─────────────────────► 1-LIST: o'yin turlari
   │                              PlayStation · Billiard · Tennis · Polya
   │
   └── fields[]  ──────────────► 2-LIST: shu turning maydonlari
          │                       Model · Joystiklar · Zal turi
          │
          └── options[]  ──────► 3-LIST: tanlov variantlari
                                  PS5 · PS4 Pro · PS4 · PS3
```

## To'liq misol — PlayStation

```json
{
  "success": true,
  "status": 200,
  "result": [

    { "key": "PLAYSTATION", "nameUz": "PlayStation", "sortOrder": 1,
      "fields": [

        { "key": "model", "label": "Model", "type": "SELECT", "required": true,
          "options": [
            { "value": "PS5",     "label": "PS5" },
            { "value": "PS4 Pro", "label": "PS4 Pro" },
            { "value": "PS4",     "label": "PS4" },
            { "value": "PS3",     "label": "PS3" }
          ]
        },

        { "key": "joysticks", "label": "Joystiklar", "type": "SELECT", "required": false,
          "options": [ { "value": "2 ta", "label": "2 ta" }, { "value": "4 ta", "label": "4 ta" } ]
        },

        { "key": "sessionMinutes", "label": "Sessiya davomiyligi", "type": "NUMBER",
          "required": false, "hint": "60", "suffix": "daqiqa" }
      ]
    },

    { "key": "BILLIARDS", "nameUz": "Billiard", "sortOrder": 5,
      "fields": [
        { "key": "tableType", "label": "Stol turi", "type": "SELECT", "required": true,
          "options": [ { "value": "Pul (Amerika)", "label": "Pul (Amerika)" },
                       { "value": "Rus", "label": "Rus" },
                       { "value": "Snuker", "label": "Snuker" } ]
        }
      ]
    }

  ],
  "error": null
}
```

## Foydalanuvchi nima ko'radi

```
1. "+E'lon" bosildi
   └─► GET /business/types/GAME_CLUB/categories        ◄── BITTA so'rov

2. Kategoriya chip'lari (1-list):
   [Barcha zallarga] [PlayStation] [Stol tennis] [Billiard] [Polya]

3. "PlayStation" bosildi                               ◄── yangi so'rov YO'Q
   └─► Maydonlar chiqadi (2-list):
         Model *      [PS5 ▾]     ◄── 3-list: PS5·PS4 Pro·PS4·PS3
         Joystiklar   [4 ta ▾]
         Sessiya      [60] daqiqa

4. Narx:  [30 000] so'm / soat
   ☑ Chegirma  →  Oldingi: 30 000    Hozirgi: [20 000]

5. "E'lon joylash"
   └─► POST /business/{businessId}/listings
```

## Saqlanishi

Tanlovlar `attributes` ичига yoziladi — kalit = `fields[].key`, qiymat = tanlangan `options[].value`:

```json
POST /business/biz_123/listings
{
  "categoryKey": "PLAYSTATION",
  "title": "PlayStation 5 — soatlik o'yin",
  "priceUnit": "PER_HOUR",
  "originalPrice": 30000,
  "discount": { "type": "SPECIAL_PRICE", "value": 20000 },
  "attributes": {
    "model": "PS5",
    "joysticks": "4 ta",
    "sessionMinutes": "60"
  },
  "validFrom": "2026-07-17T00:00:00Z",
  "validTo": "2026-08-16T00:00:00Z"
}
```

> `attributes` — oddiy `Map<String,String>`. Shuning uchun **yangi maydon qo'shilsa baza
> o'zgarmaydi**: adminkada qo'shiladi → formaда chiqadi → saqlanadi.

---

# 3️⃣ Maydon turlari

`type` ga qarab ilova UI'ni o'zi tanlaydi:

| `type` | Ilовада | `options` bormi |
|---|---|---|
| `SELECT` | chip'lar — **bittasi** tanlanadi | ✅ **ha** |
| `MULTI_SELECT` | chip'lar — **bir nechtasi** tanlanadi (razmerlar) | ✅ **ha** |
| `TEXT` | matn maydoni | ❌ (`hint` bo'ladi) |
| `NUMBER` | raqam maydoni | ❌ (`hint` + `suffix`) |
| `BOOLEAN` | belgilash katakchasi | ❌ |
| `TAGS` | vergul bilan ko'p qiymat | ❌ (`hint`) |

```json
{
  "key": "model",          // attributes'да shu kalit bilan saqlanadi
  "label": "Model",        // foydalanuvchi ko'radigan nom
  "type": "SELECT",        // UI turi
  "required": true,        // to'ldirilmasa e'lon joylanmaydi
  "hint": "60",            // TEXT/NUMBER uchun namuna
  "suffix": "daqiqa",      // NUMBER uchun o'lchov
  "options": [ { "value": "PS5", "label": "PS5" } ]
}
```

## Ko'p tanlovli maydon — `MULTI_SELECT`

Futbolkaда bitta razmer emas, **mavjud razmerlarning hammasi** tanlanadi:

```json
{
  "key": "sizes",
  "label": "Mavjud razmerlar",
  "type": "MULTI_SELECT",
  "required": true,
  "multiple": true,
  "storedAs": "vergul bilan: \"S,M,L\"",
  "options": [
    { "value": "XS", "label": "XS" }, { "value": "S",  "label": "S" },
    { "value": "M",  "label": "M" },  { "value": "L",  "label": "L" },
    { "value": "XL", "label": "XL" }, { "value": "XXL","label": "XXL" }
  ]
}
```

Ilовада: `[XS] [S✓] [M✓] [L✓] [XL] [XXL]` → `attributes.sizes = "S,M,L"`

## Maydon tartibi

**Kategoriya maydonlari OLDIN, umumiy keyin** — foydalanuvchi endigina tanlagan narsaga
tegishli maydonlar birinchi chiqadi:

```
Futbolka tanlandi
   ↓
Mavjud razmerlar *  [S✓] [M✓] [L✓]      ◄── kategoriya (SHIRTS)
Bichimi             [Regular ▾]          ◄── kategoriya
Yeng                [Kalta ▾]            ◄── kategoriya
Brand nomi          [Zara]               ◄── umumiy (CLOTHING)
Material            [100% paxta]         ◄── umumiy
Mavsum              [Yoz ▾]              ◄── umumiy
Ranglar             [Qora, Oq]           ◄── umumiy
   ↓
Tavsif (Description)
   ↓
Narx
```

---

# 4️⃣ Har bir tur uchun endpoint

Naqsh **hamma turда bir xil** — faqat `{type}` o'zgaradi.


---

## 🎮 Game Club

```http
GET /business/types/GAME_CLUB/categories
```

`8` kategoriya · `21` maydon · narx: PER_HOUR / PER_SESSION / PER_PERSON · 📄 [JSON](api/responses/GET_categories_GAME_CLUB.json)

**1-list — kategoriyalar:**  
**PlayStation** · **Stol tennis** · **Katta tennis** · **Kompyuter o'yinlari** · **Billiard** · **Polya**  _(+ `ALL` va `OTHER`)_

**Har kategoriyaда chiqadi:** Zal turi · Nechta o'yinchi · Sessiya davomiyligi

**3-list — tanlov variantlari:**

| Kategoriya | Maydon | Majburiy | `options[]` |
|---|---|---|---|
| PlayStation | Model | ✅ | `PS5` · `PS4 Pro` · `PS4` · `PS3` |
| PlayStation | Joystiklar | — | `2 ta` · `4 ta` |
| Katta tennis | Kort turi | ✅ | `Ochiq` · `Yopiq` |
| Katta tennis | Qoplama | — | `Gruntli` · `Sun'iy o't` · `Qattiq (hard)` |
| Kompyuter o'yinlari | Kompyuter quvvati | ✅ | `Standart` · `Gaming` · `Pro / e-sport` |
| Kompyuter o'yinlari | Monitor | — | `60Hz` · `144Hz` · `240Hz` |
| Billiard | Stol turi | ✅ | `Pul (Amerika)` · `Rus` · `Snuker` |
| Polya | Maydon turi | — | `Mini-futbol` · `Basketbol` · `Voleybol` · `Boshqa` |
| Polya | Qoplama | — | `Sun'iy o't` · `Tabiiy o't` · `Parket` · `Rezina` |

**Namuna — PlayStation:**

```json
{ "key": "PLAYSTATION", "nameUz": "PlayStation",
  "fields": [
    { "key": "model", "label": "Model", "type": "SELECT", "required": true,
      "options": [
            { "value": "PS5", "label": "PS5" },
            { "value": "PS4 Pro", "label": "PS4 Pro" },
            { "value": "PS4", "label": "PS4" },
            { "value": "PS3", "label": "PS3" }
      ]
    }
  ]
}
```

---

## 👕 Kiyim-kechak

```http
GET /business/types/CLOTHING/categories
```

`9` kategoriya · `34` maydon · narx: PER_ITEM · 📄 [JSON](api/responses/GET_categories_CLOTHING.json)

**1-list — kategoriyalar:**  
**Erkaklar** · **Ayollar** · **Ustki kiyim** · **Poyabzal** · **Sport kiyim** · **Sumkalar** · **Aksessuarlar**  _(+ `ALL` va `OTHER`)_

**Har kategoriyaда chiqadi:** Brend · Material · Mavsum · Ranglar · Qaytarish muddati

**3-list — tanlov variantlari:**

| Kategoriya | Maydon | Majburiy | `options[]` |
|---|---|---|---|
| Erkaklar | O'lcham | ✅ | `XS` · `S` · `M` · `L` · `XL` · `XXL` |
| Ayollar | O'lcham | ✅ | `XS` · `S` · `M` · `L` · `XL` · `XXL` |
| Ustki kiyim | O'lcham | ✅ | `XS` · `S` · `M` · `L` · `XL` · `XXL` |
| Ustki kiyim | Issiqlik | — | `Yengil` · `O'rtacha` · `Qishki` |
| Poyabzal | Razmer | ✅ | `36` · `37` · `38` · `39` · `40` · `41` · `42` · `43` · `44` · `45` · `46` |
| Poyabzal | Taglik | — | `Rezina` · `Poliuretan` · `Charm` |
| Sport kiyim | O'lcham | ✅ | `XS` · `S` · `M` · `L` · `XL` · `XXL` |
| Sport kiyim | Sport turi | — | `Fitnes` · `Yugurish` · `Futbol` · `Yoga` · `Basketbol` |
| Sumkalar | Turi | ✅ | `Yelka` · `Qo'l` · `Ryukzak` · `Klatch` |
| Aksessuarlar | Turi | ✅ | `Kamar` · `Sharf` · `Qo'lqop` · `Bosh kiyim` · `Soat` · `Zargarlik` |

**Namuna — Poyabzal:**

```json
{ "key": "SHOES", "nameUz": "Poyabzal",
  "fields": [
    { "key": "shoeSize", "label": "Razmer", "type": "SELECT", "required": true,
      "options": [
            { "value": "36", "label": "36" },
            { "value": "37", "label": "37" },
            { "value": "38", "label": "38" },
            { "value": "39", "label": "39" }
            // … jami 11 ta
      ]
    }
  ]
}
```

---

## 🍕 Kafe va Restoran

```http
GET /business/types/CAFE_RESTAURANT/categories
```

`16` kategoriya · `35` maydon · narx: PER_ITEM / PER_KG · 📄 [JSON](api/responses/GET_categories_CAFE_RESTAURANT.json)

**1-list — kategoriyalar:**  
**Milliy taomlar** · **Pitsa** · **Burger** · **Lavash / Shaurma** · **Sushi** · **Fast food** · **Sho'rvalar** · **Salatlar** · **Kabob va grill** · **Nonushta** · **Shirinliklar** · **Choy va kofe** · **Sovuq ichimliklar** · **Setlar (combo)**  _(+ `ALL` va `OTHER`)_

**Har kategoriyaда chiqadi:** Porsiya · Tarkibi · O'tkirlik · Halol · Yetkazib berish bor · Tayyorlanish vaqti · Vegetarian

**3-list — tanlov variantlari:**

| Kategoriya | Maydon | Majburiy | `options[]` |
|---|---|---|---|
| Milliy taomlar | Taom | ✅ | `Osh` · `Manti` · `Somsa` · `Lag'mon` · `Shashlik` · `Norin` · `Sho'rva` |
| Pitsa | O'lcham | ✅ | `25 sm` · `30 sm` · `35 sm` · `40 sm` |
| Pitsa | Xamir | — | `Yupqa` · `Qalin` · `To'ldirilgan chekka` |
| Burger | Kotlet | ✅ | `Mol` · `Tovuq` · `Baliq` · `Vegetarian` |
| Burger | Kotlet soni | — | `1` · `2` · `3` |
| Lavash / Shaurma | Go'sht | ✅ | `Tovuq` · `Mol` · `Aralash` |
| Lavash / Shaurma | O'lcham | — | `Standart` · `Katta` · `Mini` |
| Sushi | Baliq | ✅ | `Losos` · `Tunes` · `Ilon baliq` · `Krevetka` · `Vegetarian` |
| Salatlar | Turi | — | `Sabzavotli` · `Go'shtli` · `Sezar` · `Olivye` · `Yunon` |
| Kabob va grill | Go'sht | ✅ | `Mol` · `Qo'y` · `Tovuq` · `Jigar` · `Baliq` |
| Shirinliklar | Turi | — | `Tort` · `Pirojnoe` · `Muzqaymoq` · `Chizkeyk` · `Vafli` |
| Choy va kofe | Hajm | ✅ | `200 ml` · `300 ml` · `400 ml` |
| Choy va kofe | Sut | — | `Oddiy` · `Bodom` · `Soya` · `Sutsiz` |
| Sovuq ichimliklar | Hajm | ✅ | `300 ml` · `400 ml` · `500 ml` · `1 L` |

**Namuna — Pitsa:**

```json
{ "key": "PIZZA", "nameUz": "Pitsa",
  "fields": [
    { "key": "size", "label": "O'lcham", "type": "SELECT", "required": true,
      "options": [
            { "value": "25 sm", "label": "25 sm" },
            { "value": "30 sm", "label": "30 sm" },
            { "value": "35 sm", "label": "35 sm" },
            { "value": "40 sm", "label": "40 sm" }
      ]
    }
  ]
}
```

---

## 📚 O'quv markaz

```http
GET /business/types/EDUCATION_CENTER/categories
```

`10` kategoriya · `27` maydon · narx: PER_MONTH / PER_COURSE / PER_LESSON · 📄 [JSON](api/responses/GET_categories_EDUCATION_CENTER.json)

**1-list — kategoriyalar:**  
**Chet tillari** · **IELTS / CEFR** · **IT va dasturlash** · **Dizayn** · **Matematika va fanlar** · **Abituriyent tayyorlash** · **Biznes va marketing** · **Master-klass**  _(+ `ALL` va `OTHER`)_

**Har kategoriyaда chiqadi:** Yo'nalish · Daraja · Format · Davomiyligi · Haftada · Birinchi dars bepul · Guruhда nechta · Sertifikat beriladi · Dars davomiyligi

**3-list — tanlov variantlari:**

| Kategoriya | Maydon | Majburiy | `options[]` |
|---|---|---|---|
| Chet tillari | Til | ✅ | `Ingliz` · `Rus` · `Koreys` · `Nemis` · `Fransuz` · `Arab` · `Xitoy` · `Turk` |
| IELTS / CEFR | Imtihon | ✅ | `IELTS` · `CEFR` · `TOEFL` · `SAT` |
| Matematika va fanlar | Fan | ✅ | `Matematika` · `Fizika` · `Kimyo` · `Biologiya` |
| Biznes va marketing | Mavzu | ✅ | `SMM` · `Targeting` · `Sotuv` · `Moliya` · `Startap` |

**Namuna — Chet tillari:**

```json
{ "key": "FOREIGN_LANGUAGES", "nameUz": "Chet tillari",
  "fields": [
    { "key": "language", "label": "Til", "type": "SELECT", "required": true,
      "options": [
            { "value": "Ingliz", "label": "Ingliz" },
            { "value": "Rus", "label": "Rus" },
            { "value": "Koreys", "label": "Koreys" },
            { "value": "Nemis", "label": "Nemis" }
            // … jami 8 ta
      ]
    }
  ]
}
```

---

## 🎬 Kino va ko'ngilochar

```http
GET /business/types/ENTERTAINMENT/categories
```

`11` kategoriya · `29` maydon · narx: PER_TICKET / PER_PERSON / PER_SESSION · 📄 [JSON](api/responses/GET_categories_ENTERTAINMENT.json)

**1-list — kategoriyalar:**  
**Kino seans** · **Teatr va konsert** · **Kvest (escape room)** · **Batut park** · **Bouling** · **Akvapark** · **Attraksionlar** · **Muzey va ko'rgazma** · **Karaoke**  _(+ `ALL` va `OTHER`)_

**Har kategoriyaда chiqadi:** Film / tadbir nomi · Format · Til · Yosh chegarasi · Seans vaqtlari · Davomiyligi

**3-list — tanlov variantlari:**

| Kategoriya | Maydon | Majburiy | `options[]` |
|---|---|---|---|
| Kino seans | Zal | — | `Standart` · `VIP` · `Lounge` · `IMAX` |
| Kino seans | Joy | — | `Oddiy` · `Divan` · `Recliner` |
| Kvest (escape room) | Qiyinlik | ✅ | `Oson` · `O'rtacha` · `Qiyin` |
| Batut park | Yosh guruhi | — | `Bolalar` · `Kattalar` · `Barchasi` |
| Karaoke | Xona | ✅ | `Kichik (4)` · `O'rta (8)` · `Katta (15+)` |

**Namuna — Kvest (escape room):**

```json
{ "key": "ESCAPE_ROOM", "nameUz": "Kvest (escape room)",
  "fields": [
    { "key": "difficulty", "label": "Qiyinlik", "type": "SELECT", "required": true,
      "options": [
            { "value": "Oson", "label": "Oson" },
            { "value": "O'rtacha", "label": "O'rtacha" },
            { "value": "Qiyin", "label": "Qiyin" }
      ]
    }
  ]
}
```

---

## 💈 Sartaroshxona

> ⚠️ Faqat **erkaklarga** ko'rinadi

```http
GET /business/types/BARBERSHOP/categories
```

`10` kategoriya · `22` maydon · narx: PER_ITEM / PER_SESSION / PER_PERSON · 📄 [JSON](api/responses/GET_categories_BARBERSHOP.json)

**1-list — kategoriyalar:**  
**Erkaklar soch olish** · **Ayollar soch olish** · **Bolalar soch olish** · **Soqol / ustara** · **Soch bo'yash** · **Ukladka / styling** · **Parvarish (spa)** · **Manikyur-pedikyur**  _(+ `ALL` va `OTHER`)_

**Har kategoriyaда chiqadi:** Usta · Usta darajasi · Kimlar uchun · Davomiyligi · Oldindan yozilish · Yuvish kiradi

**3-list — tanlov variantlari:**

| Kategoriya | Maydon | Majburiy | `options[]` |
|---|---|---|---|
| Erkaklar soch olish | Uslub | — | `Klassik` · `Fade` · `Undercut` · `Mashinka` · `Bolalar` |
| Ayollar soch olish | Soch uzunligi | ✅ | `Kalta` · `O'rta` · `Uzun` |
| Ayollar soch olish | Uslub | — | `To'g'ri` · `Kaskad` · `Kare` · `Chelka` |
| Bolalar soch olish | Yosh | ✅ | `0-3` · `4-7` · `8-12` |
| Soqol / ustara | Xizmat | ✅ | `Qirqish` · `Ustara` · `Shakl berish` · `Bo'yash` |
| Soch bo'yash | Soch uzunligi | ✅ | `Kalta` · `O'rta` · `Uzun` |
| Soch bo'yash | Texnika | — | `To'liq` · `Ombre` · `Balayaj` · `Melirovka` · `Ildiz` |
| Ukladka / styling | Tadbir | — | `Kundalik` · `To'y` · `Bayram` · `Fotosessiya` |
| Ukladka / styling | Soch uzunligi | — | `Kalta` · `O'rta` · `Uzun` |
| Parvarish (spa) | Muolaja | ✅ | `Keratin` · `Botoks` · `Maska` · `Peeling` |
| Manikyur-pedikyur | Qoplama | ✅ | `Oddiy` · `Gel-lak` · `Kengaytma` |

**Namuna — Soch bo'yash:**

```json
{ "key": "HAIR_COLOR", "nameUz": "Soch bo'yash",
  "fields": [
    { "key": "hairLength", "label": "Soch uzunligi", "type": "SELECT", "required": true,
      "options": [
            { "value": "Kalta", "label": "Kalta" },
            { "value": "O'rta", "label": "O'rta" },
            { "value": "Uzun", "label": "Uzun" }
      ]
    }
  ]
}
```

---

## 💅 Go'zallik saloni

> ⚠️ Faqat **ayollarga** ko'rinadi

```http
GET /business/types/BEAUTY_SALON/categories
```

`10` kategoriya · `27` maydon · narx: PER_ITEM / PER_SESSION / PER_PERSON · 📄 [JSON](api/responses/GET_categories_BEAUTY_SALON.json)

**1-list — kategoriyalar:**  
**Soch turmagi / bo'yash** · **Makiyaj** · **Manikyur** · **Pedikyur** · **Qosh / kiprik** · **Kosmetologiya** · **SPA / massaj** · **Epilyatsiya**  _(+ `ALL` va `OTHER`)_

**Har kategoriyaда chiqadi:** Usta · Usta darajasi · Davomiyligi · Oldindan yozilish · Uyга borish mumkin

**3-list — tanlov variantlari:**

| Kategoriya | Maydon | Majburiy | `options[]` |
|---|---|---|---|
| Soch turmagi / bo'yash | Soch uzunligi | ✅ | `Kalta` · `O'rta` · `Uzun` |
| Soch turmagi / bo'yash | Xizmat | ✅ | `Turmak` · `Bo'yash` · `Kesish` · `Keratin` · `Ombre` |
| Makiyaj | Tadbir | ✅ | `Kundalik` · `Kechki` · `To'y` · `Fotosessiya` |
| Manikyur | Qoplama | ✅ | `Oddiy lak` · `Gel-lak` · `Kengaytma (gel)` · `Akril` |
| Pedikyur | Qoplama | ✅ | `Oddiy lak` · `Gel-lak` |
| Pedikyur | Turi | ✅ | `Klassik` · `Apparat` · `SPA` |
| Qosh / kiprik | Muolaja | ✅ | `Qosh shakl` · `Qosh bo'yash` · `Laminatsiya` · `Kiprik yopishtirish` · `Kiprik laminatsiya` |
| Qosh / kiprik | Effekt | — | `Klassik` · `2D` · `3D` · `Volume` |
| Kosmetologiya | Muolaja | ✅ | `Tozalash` · `Peeling` · `Mezoterapiya` · `Biorevitalizatsiya` · `Maska` |
| SPA / massaj | Massaj turi | ✅ | `Klassik` · `Relaks` · `Anticellulit` · `Tosh bilan` · `Tay` |
| Epilyatsiya | Usul | ✅ | `Shakar (shugaring)` · `Vosk` · `Lazer` · `Elektro` |

**Namuna — Manikyur:**

```json
{ "key": "MANICURE", "nameUz": "Manikyur",
  "fields": [
    { "key": "coating", "label": "Qoplama", "type": "SELECT", "required": true,
      "options": [
            { "value": "Oddiy lak", "label": "Oddiy lak" },
            { "value": "Gel-lak", "label": "Gel-lak" },
            { "value": "Kengaytma (gel)", "label": "Kengaytma (gel)" },
            { "value": "Akril", "label": "Akril" }
      ]
    }
  ]
}
```


---

# 5️⃣ Adminka nima qila oladi

**Ilovani qayta chiqarmasdan:**

| Amal | Natija |
|---|---|
| PS6 chiqdi → `options` ga qo'shish | PlayStation formasида darrov ko'rinadi |
| Yangi kategoriya qo'shish | E'lon formasида chip bo'lib chiqadi |
| Yangi biznes turi qo'shish | Biznes qo'shishда chip bo'lib chiqadi |
| Maydonni majburiy qilish | `required: true` → validatsiya avtomatik |
| Tartibni o'zgartirish | `fields[]` tartibi = formadagi tartib |

> ⚠️ **`key` larni o'zgartirmang!** Eski e'lonlarning `attributes` ида saqlangan.
> Yangi `key` qo'shish — xavfsiz. Eskisini qayta nomlash — eski e'lonlarni buzadi.

---

# 6️⃣ Fayllar

```
docs/api/responses/
├── GET_business-types.json          ← biznes qo'shish (7 tur)
├── GET_business-types_MALE.json     ← erkakka (6 tur)
├── GET_business-types_FEMALE.json   ← ayolga (6 tur)
├── GET_categories_GAME_CLUB.json    ← PlayStation → PS5/PS4/PS3
├── GET_categories_CLOTHING.json     ← + _MALE / _FEMALE variantlari
├── GET_categories_CAFE_RESTAURANT.json
├── GET_categories_EDUCATION_CENTER.json
├── GET_categories_ENTERTAINMENT.json
├── GET_categories_BARBERSHOP.json
├── GET_categories_BEAUTY_SALON.json
└── GET_catalog.json                 ← hammasi birdan (cache uchun)
```

**Manba:** `docs/backend/catalog-seed.json` — backend shuni bazaga yuklaydi.
**Backend qurish:** `docs/backend/BACKEND_PROMPT.md`
