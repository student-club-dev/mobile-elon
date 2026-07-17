# E'lon maydonlari — to'liq spetsifikatsiya

**7 biznes turi**, har birining har bir kategoriyasi uchun e'lon
qo'yishда chiqadigan maydonlar. Jami **195 maydon** (45 ✅ mavjud, **150 🆕 yangi**).

- **Umumiy maydonlar** — shu turdagi *har qanday* e'lonда chiqadi
- **Kategoriya maydonlari** — kategoriya tanlangach **qo'shimcha** chiqadi

> 📐 **Model: QO'SHISH** — kategoriya maydonlari umumiy maydonlarni almashtirmaydi, ustiga
> qo'shiladi. Ko'ylakka brend ham (umumiy), o'lcham ham (kategoriya) kerak.
>
> ⚠️ Hozirgi kod **almashtiradi** — `categoryAttributes()` natijasi `attributes()` bilan
> birlashtirilishi kerak (kalit to'qnashsa — kategoriya versiyasi ustun).

| Belgi | Ma'no |
|---|---|
| ✅ bor | `ListingCatalog.kt` да allaqachon bor |
| 🆕 yangi | Taklif — hali yo'q |
| **tanlash** | `SELECT` — ro'yxatdan bittasi (PS5 · PS4 · PS3 kabi) |
| **ha** | Majburiy |

**Har e'londa:** Kategoriya · Sarlavha · Tavsif · Rasmlar (≤5) · **Turga xos maydonlar** ⬇️ ·
Chegirma toggle · **Narx + birlik** · Muddat · Filiallar · Ishlatish usuli

---

# 🎮 Game Club (`GAME_CLUB`)

**Narx:** `PER_HOUR` · `PER_SESSION` · `PER_PERSON` (odatiy `PER_HOUR`) · **21 maydon** · **6 kategoriya**

## Umumiy maydonlar

| Maydon | Nomi | Turi | Qiymatlar | Majburiy | Holat |
|---|---|---|---|---|---|
| `hallType` | Zal turi | **tanlash** | `Standart` · `VIP` · `Alohida xona` | — | ✅ bor |
| `seatsCount` | Nechta o'yinchi | raqam | _kishi_ | — | ✅ bor |
| `sessionMinutes` | Sessiya davomiyligi | raqam | _daqiqa_ | — | ✅ bor |

## Kategoriya maydonlari

### PlayStation (`PLAYSTATION`)

| Maydon | Nomi | Turi | Qiymatlar | Majburiy | Holat |
|---|---|---|---|---|---|
| `model` | Model | **tanlash** | `PS5` · `PS4 Pro` · `PS4` · `PS3` | **ha** | ✅ bor |
| `joysticks` | Joystiklar | **tanlash** | `2 ta` · `4 ta` | — | ✅ bor |
| `games` | Mashhur o'yinlar | teglar | e.g. FIFA 25, Mortal Kombat, UFC | — | ✅ bor |

### Stol tennis (`TABLE_TENNIS`)

| Maydon | Nomi | Turi | Qiymatlar | Majburiy | Holat |
|---|---|---|---|---|---|
| `tables` | Stollar soni | raqam | _ta_ | — | ✅ bor |
| `racketsIncluded` | Raketka va koptok beriladi | belgilash | — | — | ✅ bor |

### Katta tennis (`TENNIS`)

| Maydon | Nomi | Turi | Qiymatlar | Majburiy | Holat |
|---|---|---|---|---|---|
| `courtType` | Kort turi | **tanlash** | `Ochiq` · `Yopiq` | **ha** | ✅ bor |
| `surface` | Qoplama | **tanlash** | `Gruntli` · `Sun'iy o't` · `Qattiq (hard)` | — | ✅ bor |
| `gearIncluded` | Raketka beriladi | belgilash | — | — | ✅ bor |

### Kompyuter o'yinlari (`PC_GAMING`)

| Maydon | Nomi | Turi | Qiymatlar | Majburiy | Holat |
|---|---|---|---|---|---|
| `pcTier` | Kompyuter quvvati | **tanlash** | `Standart` · `Gaming` · `Pro / e-sport` | **ha** | ✅ bor |
| `games` | O'yinlar | teglar | e.g. CS2, Dota 2, Valorant | — | ✅ bor |
| `gpu` | Videokarta | matn | e.g. RTX 4060 | — | 🆕 yangi |
| `monitorHz` | Monitor | **tanlash** | `60Hz` · `144Hz` · `240Hz` | — | 🆕 yangi |

### Billiard (`BILLIARDS`)

| Maydon | Nomi | Turi | Qiymatlar | Majburiy | Holat |
|---|---|---|---|---|---|
| `tableType` | Stol turi | **tanlash** | `Pul (Amerika)` · `Rus` · `Snuker` | **ha** | ✅ bor |
| `tables` | Stollar soni | raqam | _ta_ | — | ✅ bor |

### Polya (`POLYA`)

| Maydon | Nomi | Turi | Qiymatlar | Majburiy | Holat |
|---|---|---|---|---|---|
| `fieldType` | Maydon turi | **tanlash** | `Mini-futbol` · `Basketbol` · `Voleybol` · `Boshqa` | — | ✅ bor |
| `fields` | Maydonlar soni | raqam | _ta_ | — | ✅ bor |
| `surface` | Qoplama | **tanlash** | `Sun'iy o't` · `Tabiiy o't` · `Parket` · `Rezina` | — | 🆕 yangi |
| `hasLighting` | Yoritish bor (kechqurun) | belgilash | — | — | 🆕 yangi |

---

# 👕 Kiyim-kechak (`CLOTHING`)

**Narx:** `PER_ITEM` (odatiy `PER_ITEM`) · **34 maydon** · **13 kategoriya**

## Umumiy maydonlar

| Maydon | Nomi | Turi | Qiymatlar | Majburiy | Holat |
|---|---|---|---|---|---|
| `brand` | Brend | matn | e.g. Zara | — | ✅ bor |
| `material` | Material | matn | e.g. 100% paxta | — | ✅ bor |
| `season` | Mavsum | **tanlash** | `Qish` · `Bahor` · `Yoz` · `Kuz` · `Barcha mavsum` | — | ✅ bor |
| `colors` | Ranglar | teglar | e.g. Qora, Oq, Bej | — | 🆕 yangi |
| `returnDays` | Qaytarish muddati | raqam | _kun_ | — | 🆕 yangi |

## Kategoriya maydonlari

### Ko'ylak / futbolka (`SHIRTS`)

| Maydon | Nomi | Turi | Qiymatlar | Majburiy | Holat |
|---|---|---|---|---|---|
| `size` | O'lcham | **tanlash** | `XS` · `S` · `M` · `L` · `XL` · `XXL` | **ha** | 🆕 yangi |
| `fit` | Bichimi | **tanlash** | `Slim` · `Regular` · `Oversize` | — | 🆕 yangi |
| `sleeve` | Yeng | **tanlash** | `Kalta` · `Uzun` | — | 🆕 yangi |

### Ko'ylak / libos (`DRESSES`)

| Maydon | Nomi | Turi | Qiymatlar | Majburiy | Holat |
|---|---|---|---|---|---|
| `size` | O'lcham | **tanlash** | `XS` · `S` · `M` · `L` · `XL` · `XXL` | **ha** | 🆕 yangi |
| `length` | Uzunligi | **tanlash** | `Mini` · `Midi` · `Maksi` | — | 🆕 yangi |
| `occasion` | Uslub | **tanlash** | `Kundalik` · `Ofis` · `Kechki` · `Milliy` | — | 🆕 yangi |

### Bluzka (`BLOUSES`)

| Maydon | Nomi | Turi | Qiymatlar | Majburiy | Holat |
|---|---|---|---|---|---|
| `size` | O'lcham | **tanlash** | `XS` · `S` · `M` · `L` · `XL` · `XXL` | **ha** | 🆕 yangi |
| `sleeve` | Yeng | **tanlash** | `Kalta` · `Uzun` · `Yengsiz` | — | 🆕 yangi |

### Shim / jinsi (`PANTS`)

| Maydon | Nomi | Turi | Qiymatlar | Majburiy | Holat |
|---|---|---|---|---|---|
| `waistSize` | Bel o'lchami | **tanlash** | `28` · `30` · `32` · `34` · `36` · `…40` | **ha** | 🆕 yangi |
| `cut` | Bichimi | **tanlash** | `Slim` · `Straight` · `Wide` · `Skinny` | — | 🆕 yangi |
| `length` | Uzunligi | raqam | _inch_ | — | 🆕 yangi |

### Yubka (`SKIRTS`)

| Maydon | Nomi | Turi | Qiymatlar | Majburiy | Holat |
|---|---|---|---|---|---|
| `size` | O'lcham | **tanlash** | `XS` · `S` · `M` · `L` · `XL` · `XXL` | **ha** | 🆕 yangi |
| `length` | Uzunligi | **tanlash** | `Mini` · `Midi` · `Maksi` | — | 🆕 yangi |

### Kostyum (`SUITS`)

| Maydon | Nomi | Turi | Qiymatlar | Majburiy | Holat |
|---|---|---|---|---|---|
| `size` | O'lcham | **tanlash** | `XS` · `S` · `M` · `L` · `XL` · `XXL` | **ha** | 🆕 yangi |
| `pieces` | Nechta qism | **tanlash** | `2 (pidjak+shim)` · `3 (+jilet)` | — | 🆕 yangi |
| `occasion` | Uslub | **tanlash** | `Klassik` · `Biznes` · `Kechki` | — | 🆕 yangi |

### Ustki kiyim (`OUTERWEAR`)

| Maydon | Nomi | Turi | Qiymatlar | Majburiy | Holat |
|---|---|---|---|---|---|
| `size` | O'lcham | **tanlash** | `XS` · `S` · `M` · `L` · `XL` · `XXL` | **ha** | 🆕 yangi |
| `insulation` | Issiqlik | **tanlash** | `Yengil` · `O'rtacha` · `Qishki` | — | 🆕 yangi |
| `isWaterproof` | Suv o'tkazmaydi | belgilash | — | — | 🆕 yangi |

### Poyabzal (`SHOES`)

| Maydon | Nomi | Turi | Qiymatlar | Majburiy | Holat |
|---|---|---|---|---|---|
| `shoeSize` | Razmer | **tanlash** | `36` · `37` · `38` · `39` · `40` · `…46` | **ha** | 🆕 yangi |
| `soleType` | Taglik | **tanlash** | `Rezina` · `Poliuretan` · `Charm` | — | 🆕 yangi |
| `heelCm` | Poshna | raqam | _sm_ | — | 🆕 yangi |

### Sport kiyim (`SPORTSWEAR`)

| Maydon | Nomi | Turi | Qiymatlar | Majburiy | Holat |
|---|---|---|---|---|---|
| `size` | O'lcham | **tanlash** | `XS` · `S` · `M` · `L` · `XL` · `XXL` | **ha** | 🆕 yangi |
| `sport` | Sport turi | **tanlash** | `Fitnes` · `Yugurish` · `Futbol` · `Yoga` · `Basketbol` | — | 🆕 yangi |

### Sumka (`BAGS`)

| Maydon | Nomi | Turi | Qiymatlar | Majburiy | Holat |
|---|---|---|---|---|---|
| `bagType` | Turi | **tanlash** | `Yelka` · `Qo'l` · `Ryukzak` · `Klatch` | **ha** | 🆕 yangi |
| `capacityL` | Hajmi | raqam | _litr_ | — | 🆕 yangi |

### Erkaklar (`MEN`)

| Maydon | Nomi | Turi | Qiymatlar | Majburiy | Holat |
|---|---|---|---|---|---|
| `size` | O'lcham | **tanlash** | `XS` · `S` · `M` · `L` · `XL` · `XXL` | **ha** | 🆕 yangi |

### Ayollar (`WOMEN`)

| Maydon | Nomi | Turi | Qiymatlar | Majburiy | Holat |
|---|---|---|---|---|---|
| `size` | O'lcham | **tanlash** | `XS` · `S` · `M` · `L` · `XL` · `XXL` | **ha** | 🆕 yangi |

### Aksessuar (`ACCESSORIES`)

| Maydon | Nomi | Turi | Qiymatlar | Majburiy | Holat |
|---|---|---|---|---|---|
| `accessoryType` | Turi | **tanlash** | `Kamar` · `Sharf` · `Qo'lqop` · `Bosh kiyim` · `Soat` · `Zargarlik` | **ha** | 🆕 yangi |

---

# 🍕 Kafe va Restoran (`CAFE_RESTAURANT`)

**Narx:** `PER_ITEM` · `PER_KG` (odatiy `PER_ITEM`) · **35 maydon** · **14 kategoriya**

## Umumiy maydonlar

| Maydon | Nomi | Turi | Qiymatlar | Majburiy | Holat |
|---|---|---|---|---|---|
| `portionGrams` | Porsiya | raqam | _gramm_ | — | ✅ bor |
| `ingredients` | Tarkibi | teglar | e.g. Mozzarella, Pepperoni, Tomat sousi | — | ✅ bor |
| `spicyLevel` | O'tkirlik | **tanlash** | `Yo'q` · `Yengil` · `O'rtacha` · `O'tkir` | — | ✅ bor |
| `isHalal` | Halol | belgilash | — | — | ✅ bor |
| `hasDelivery` | Yetkazib berish bor | belgilash | — | — | ✅ bor |
| `cookMinutes` | Tayyorlanish vaqti | raqam | _daqiqa_ | — | 🆕 yangi |
| `isVegetarian` | Vegetarian | belgilash | — | — | 🆕 yangi |

## Kategoriya maydonlari

### Pitsa (`PIZZA`)

| Maydon | Nomi | Turi | Qiymatlar | Majburiy | Holat |
|---|---|---|---|---|---|
| `size` | O'lcham | **tanlash** | `25 sm` · `30 sm` · `35 sm` · `40 sm` | **ha** | 🆕 yangi |
| `dough` | Xamir | **tanlash** | `Yupqa` · `Qalin` · `To'ldirilgan chekka` | — | 🆕 yangi |
| `slices` | Bo'laklar | raqam | _ta_ | — | 🆕 yangi |

### Burger (`BURGER`)

| Maydon | Nomi | Turi | Qiymatlar | Majburiy | Holat |
|---|---|---|---|---|---|
| `pattyType` | Kotlet | **tanlash** | `Mol` · `Tovuq` · `Baliq` · `Vegetarian` | **ha** | 🆕 yangi |
| `pattyCount` | Kotlet soni | **tanlash** | `1` · `2` · `3` | — | 🆕 yangi |
| `hasFries` | Kartoshka bilan | belgilash | — | — | 🆕 yangi |

### Lavash / Shaurma (`LAVASH_SHAWARMA`)

| Maydon | Nomi | Turi | Qiymatlar | Majburiy | Holat |
|---|---|---|---|---|---|
| `meatType` | Go'sht | **tanlash** | `Tovuq` · `Mol` · `Aralash` | **ha** | 🆕 yangi |
| `size` | O'lcham | **tanlash** | `Standart` · `Katta` · `Mini` | — | 🆕 yangi |

### Sushi (`SUSHI`)

| Maydon | Nomi | Turi | Qiymatlar | Majburiy | Holat |
|---|---|---|---|---|---|
| `piecesCount` | Bo'laklar | raqam | _ta_ | — | 🆕 yangi |
| `fishType` | Baliq | **tanlash** | `Losos` · `Tunes` · `Ilon baliq` · `Krevetka` · `Vegetarian` | **ha** | 🆕 yangi |
| `isRaw` | Xom (raw) | belgilash | — | — | 🆕 yangi |

### Milliy taomlar (`NATIONAL`)

| Maydon | Nomi | Turi | Qiymatlar | Majburiy | Holat |
|---|---|---|---|---|---|
| `dishType` | Taom | **tanlash** | `Osh` · `Manti` · `Somsa` · `Lag'mon` · `Shashlik` · `…Sho'rva` | **ha** | 🆕 yangi |

### Sho'rvalar (`SOUPS`)

| Maydon | Nomi | Turi | Qiymatlar | Majburiy | Holat |
|---|---|---|---|---|---|
| `volumeMl` | Hajm | raqam | _ml_ | — | 🆕 yangi |
| `hasBread` | Non bilan | belgilash | — | — | 🆕 yangi |

### Salatlar (`SALADS`)

| Maydon | Nomi | Turi | Qiymatlar | Majburiy | Holat |
|---|---|---|---|---|---|
| `saladType` | Turi | **tanlash** | `Sabzavotli` · `Go'shtli` · `Sezar` · `Olivye` · `Yunon` | — | 🆕 yangi |

### Kabob va grill (`GRILL_BBQ`)

| Maydon | Nomi | Turi | Qiymatlar | Majburiy | Holat |
|---|---|---|---|---|---|
| `meatType` | Go'sht | **tanlash** | `Mol` · `Qo'y` · `Tovuq` · `Jigar` · `Baliq` | **ha** | 🆕 yangi |
| `skewers` | Nechta sixda | raqam | _ta_ | — | 🆕 yangi |

### Nonushta (`BREAKFAST`)

| Maydon | Nomi | Turi | Qiymatlar | Majburiy | Holat |
|---|---|---|---|---|---|
| `servedUntil` | Nechagacha | matn | e.g. 11:00 | — | 🆕 yangi |
| `includes` | Tarkibida | teglar | e.g. Tuxum, Non, Choy | — | 🆕 yangi |

### Shirinliklar (`DESSERTS`)

| Maydon | Nomi | Turi | Qiymatlar | Majburiy | Holat |
|---|---|---|---|---|---|
| `dessertType` | Turi | **tanlash** | `Tort` · `Pirojnoe` · `Muzqaymoq` · `Chizkeyk` · `Vafli` | — | 🆕 yangi |
| `sugarFree` | Shakarsiz | belgilash | — | — | 🆕 yangi |

### Choy va kofe (`HOT_DRINKS`)

| Maydon | Nomi | Turi | Qiymatlar | Majburiy | Holat |
|---|---|---|---|---|---|
| `volumeMl` | Hajm | **tanlash** | `200 ml` · `300 ml` · `400 ml` | **ha** | 🆕 yangi |
| `milkType` | Sut | **tanlash** | `Oddiy` · `Bodom` · `Soya` · `Sutsiz` | — | 🆕 yangi |

### Sovuq ichimliklar (`COLD_DRINKS`)

| Maydon | Nomi | Turi | Qiymatlar | Majburiy | Holat |
|---|---|---|---|---|---|
| `volumeMl` | Hajm | **tanlash** | `300 ml` · `400 ml` · `500 ml` · `1 L` | **ha** | 🆕 yangi |
| `hasIce` | Muz bilan | belgilash | — | — | 🆕 yangi |

### Fast food (`FAST_FOOD`)

| Maydon | Nomi | Turi | Qiymatlar | Majburiy | Holat |
|---|---|---|---|---|---|
| `comboIncludes` | Tarkibida | teglar | e.g. Burger, Kartoshka, Ichimlik | — | 🆕 yangi |

### Setlar (combo) (`COMBO_SETS`)

| Maydon | Nomi | Turi | Qiymatlar | Majburiy | Holat |
|---|---|---|---|---|---|
| `comboIncludes` | Setда nima bor | teglar | e.g. 2 pitsa, 2 ichimlik | **ha** | 🆕 yangi |
| `personCount` | Nechta kishiga | raqam | _kishi_ | **ha** | 🆕 yangi |

---

# 📚 O'quv markaz (`EDUCATION_CENTER`)

**Narx:** `PER_MONTH` · `PER_COURSE` · `PER_LESSON` (odatiy `PER_MONTH`) · **27 maydon** · **8 kategoriya**

## Umumiy maydonlar

| Maydon | Nomi | Turi | Qiymatlar | Majburiy | Holat |
|---|---|---|---|---|---|
| `subject` | Yo'nalish | matn | e.g. Ingliz tili — IELTS 6.5+ | — | ✅ bor |
| `level` | Daraja | **tanlash** | `Boshlang'ich` · `O'rta` · `Yuqori` | — | ✅ bor |
| `format` | Format | **tanlash** | `Offline` · `Online` · `Aralash` | — | ✅ bor |
| `durationMonths` | Davomiyligi | raqam | _oy_ | — | ✅ bor |
| `lessonsPerWeek` | Haftada | raqam | _marta_ | — | ✅ bor |
| `hasFreeTrialLesson` | Birinchi dars bepul | belgilash | — | — | ✅ bor |
| `groupSize` | Guruhда nechta | raqam | _kishi_ | — | 🆕 yangi |
| `hasCertificate` | Sertifikat beriladi | belgilash | — | — | 🆕 yangi |
| `lessonMinutes` | Dars davomiyligi | raqam | _daqiqa_ | — | 🆕 yangi |

## Kategoriya maydonlari

### Chet tillari (`FOREIGN_LANGUAGES`)

| Maydon | Nomi | Turi | Qiymatlar | Majburiy | Holat |
|---|---|---|---|---|---|
| `language` | Til | **tanlash** | `Ingliz` · `Rus` · `Koreys` · `Nemis` · `Fransuz` · `…Turk` | **ha** | 🆕 yangi |
| `hasNativeSpeaker` | Native speaker bor | belgilash | — | — | 🆕 yangi |

### IELTS / CEFR (`IELTS_CEFR`)

| Maydon | Nomi | Turi | Qiymatlar | Majburiy | Holat |
|---|---|---|---|---|---|
| `examType` | Imtihon | **tanlash** | `IELTS` · `CEFR` · `TOEFL` · `SAT` | **ha** | 🆕 yangi |
| `targetScore` | Maqsad ball | matn | e.g. 6.5+ | — | 🆕 yangi |
| `hasMockExam` | Mock imtihon bor | belgilash | — | — | 🆕 yangi |

### IT va dasturlash (`IT_PROGRAMMING`)

| Maydon | Nomi | Turi | Qiymatlar | Majburiy | Holat |
|---|---|---|---|---|---|
| `stack` | Texnologiyalar | teglar | e.g. Python, Django, PostgreSQL | **ha** | 🆕 yangi |
| `hasProject` | Real loyiha bor | belgilash | — | — | 🆕 yangi |
| `hasJobHelp` | Ishga joylashishда yordam | belgilash | — | — | 🆕 yangi |

### Dizayn (`DESIGN`)

| Maydon | Nomi | Turi | Qiymatlar | Majburiy | Holat |
|---|---|---|---|---|---|
| `tools` | Dasturlar | teglar | e.g. Figma, Photoshop | **ha** | 🆕 yangi |
| `hasPortfolio` | Portfolio yig'iladi | belgilash | — | — | 🆕 yangi |

### Matematika va fanlar (`MATH_SCIENCE`)

| Maydon | Nomi | Turi | Qiymatlar | Majburiy | Holat |
|---|---|---|---|---|---|
| `subjectName` | Fan | **tanlash** | `Matematika` · `Fizika` · `Kimyo` · `Biologiya` | **ha** | 🆕 yangi |
| `gradeLevel` | Sinf | matn | e.g. 9-11 | — | 🆕 yangi |

### Abituriyent tayyorlash (`UNIVERSITY_PREP`)

| Maydon | Nomi | Turi | Qiymatlar | Majburiy | Holat |
|---|---|---|---|---|---|
| `targetUniversity` | Qaysi OTMga | matn | e.g. TATU | — | 🆕 yangi |
| `subjects` | Fanlar | teglar | e.g. Matematika, Fizika | **ha** | 🆕 yangi |

### Biznes va marketing (`BUSINESS_MARKETING`)

| Maydon | Nomi | Turi | Qiymatlar | Majburiy | Holat |
|---|---|---|---|---|---|
| `topic` | Mavzu | **tanlash** | `SMM` · `Targeting` · `Sotuv` · `Moliya` · `Startap` | **ha** | 🆕 yangi |

### Master-klass (`MASTER_CLASS`)

| Maydon | Nomi | Turi | Qiymatlar | Majburiy | Holat |
|---|---|---|---|---|---|
| `speaker` | Spiker | matn | e.g. Aziz Karimov | — | 🆕 yangi |
| `eventDate` | Sana | matn | e.g. 2026-08-01 | — | 🆕 yangi |
| `hours` | Davomiyligi | raqam | _soat_ | — | 🆕 yangi |

---

# 🎬 Kino va ko'ngilochar (`ENTERTAINMENT`)

**Narx:** `PER_TICKET` · `PER_PERSON` · `PER_SESSION` (odatiy `PER_TICKET`) · **29 maydon** · **9 kategoriya**

## Umumiy maydonlar

| Maydon | Nomi | Turi | Qiymatlar | Majburiy | Holat |
|---|---|---|---|---|---|
| `eventTitle` | Film / tadbir nomi | matn | e.g. Dune: Part Three | — | ✅ bor |
| `format` | Format | **tanlash** | `2D` · `3D` · `IMAX` · `4DX` · `VR` | — | ✅ bor |
| `language` | Til | **tanlash** | `O'zbek` · `Rus` · `Ingliz` · `Original (subtitr)` | — | ✅ bor |
| `ageLimit` | Yosh chegarasi | **tanlash** | `0+` · `6+` · `12+` · `16+` · `18+` | — | ✅ bor |
| `sessionTimes` | Seans vaqtlari | teglar | e.g. 12:30, 16:00, 19:40 | — | ✅ bor |
| `durationMinutes` | Davomiyligi | raqam | _daqiqa_ | — | 🆕 yangi |

## Kategoriya maydonlari

### Kino seans (`CINEMA`)

| Maydon | Nomi | Turi | Qiymatlar | Majburiy | Holat |
|---|---|---|---|---|---|
| `hallType` | Zal | **tanlash** | `Standart` · `VIP` · `Lounge` · `IMAX` | — | 🆕 yangi |
| `seatType` | Joy | **tanlash** | `Oddiy` · `Divan` · `Recliner` | — | 🆕 yangi |

### Teatr va konsert (`THEATER_CONCERT`)

| Maydon | Nomi | Turi | Qiymatlar | Majburiy | Holat |
|---|---|---|---|---|---|
| `artist` | Ijrochi / truppa | matn | e.g. Ozodbek Nazarbekov | **ha** | 🆕 yangi |
| `hasIntermission` | Tanaffus bor | belgilash | — | — | 🆕 yangi |

### Kvest (escape room) (`ESCAPE_ROOM`)

| Maydon | Nomi | Turi | Qiymatlar | Majburiy | Holat |
|---|---|---|---|---|---|
| `players` | Nechta o'yinchi | raqam | _kishi_ | **ha** | 🆕 yangi |
| `difficulty` | Qiyinlik | **tanlash** | `Oson` · `O'rtacha` · `Qiyin` | **ha** | 🆕 yangi |
| `theme` | Mavzu | matn | e.g. Qo'rqinchli | — | 🆕 yangi |
| `hasActor` | Aktyor bor | belgilash | — | — | 🆕 yangi |

### Batut park (`TRAMPOLINE_PARK`)

| Maydon | Nomi | Turi | Qiymatlar | Majburiy | Holat |
|---|---|---|---|---|---|
| `minutes` | Vaqt | raqam | _daqiqa_ | **ha** | 🆕 yangi |
| `ageGroup` | Yosh guruhi | **tanlash** | `Bolalar` · `Kattalar` · `Barchasi` | — | 🆕 yangi |
| `socksIncluded` | Paypoq beriladi | belgilash | — | — | 🆕 yangi |

### Bouling (`BOWLING`)

| Maydon | Nomi | Turi | Qiymatlar | Majburiy | Holat |
|---|---|---|---|---|---|
| `lanes` | Yo'laklar | raqam | _ta_ | — | 🆕 yangi |
| `players` | Nechta o'yinchi | raqam | _kishi_ | — | 🆕 yangi |
| `shoesIncluded` | Poyabzal beriladi | belgilash | — | — | 🆕 yangi |

### Akvapark (`AQUAPARK`)

| Maydon | Nomi | Turi | Qiymatlar | Majburiy | Holat |
|---|---|---|---|---|---|
| `zones` | Zonalar | teglar | e.g. Katta gorka, Bolalar zonasi, Jakuzi | — | 🆕 yangi |
| `hasLocker` | Shkaf beriladi | belgilash | — | — | 🆕 yangi |
| `allDay` | Kun bo'yi | belgilash | — | — | 🆕 yangi |

### Attraksionlar (`AMUSEMENT_PARK`)

| Maydon | Nomi | Turi | Qiymatlar | Majburiy | Holat |
|---|---|---|---|---|---|
| `rideCount` | Attraksionlar | raqam | _ta_ | — | 🆕 yangi |
| `isUnlimited` | Cheksiz | belgilash | — | — | 🆕 yangi |

### Muzey va ko'rgazma (`MUSEUM_EXPO`)

| Maydon | Nomi | Turi | Qiymatlar | Majburiy | Holat |
|---|---|---|---|---|---|
| `expoTitle` | Ko'rgazma | matn | e.g. Zamonaviy san'at | — | 🆕 yangi |
| `hasGuide` | Gid bor | belgilash | — | — | 🆕 yangi |

### Karaoke (`KARAOKE`)

| Maydon | Nomi | Turi | Qiymatlar | Majburiy | Holat |
|---|---|---|---|---|---|
| `roomSize` | Xona | **tanlash** | `Kichik (4)` · `O'rta (8)` · `Katta (15+)` | **ha** | 🆕 yangi |
| `songLanguages` | Qo'shiq tillari | teglar | e.g. O'zbek, Rus, Ingliz | — | 🆕 yangi |

---

# 💈 Sartaroshxona (`BARBERSHOP`)

> ⚠️ Faqat **erkak** foydalanuvchilarga.

**Narx:** `PER_ITEM` · `PER_SESSION` · `PER_PERSON` (odatiy `PER_ITEM`) · **22 maydon** · **8 kategoriya**

## Umumiy maydonlar

| Maydon | Nomi | Turi | Qiymatlar | Majburiy | Holat |
|---|---|---|---|---|---|
| `master` | Usta | matn | e.g. Aziz aka | — | ✅ bor |
| `masterLevel` | Usta darajasi | **tanlash** | `Junior` · `Usta` · `Top-usta` | — | ✅ bor |
| `gender` | Kimlar uchun | **tanlash** | `Erkaklar` · `Ayollar` · `Bolalar` · `Barcha` | — | ✅ bor |
| `durationMinutes` | Davomiyligi | raqam | _daqiqa_ | — | ✅ bor |
| `byAppointment` | Oldindan yozilish | belgilash | — | — | ✅ bor |
| `hasWashing` | Yuvish kiradi | belgilash | — | — | 🆕 yangi |

## Kategoriya maydonlari

### Erkaklar soch olish (`HAIRCUT_MEN`)

| Maydon | Nomi | Turi | Qiymatlar | Majburiy | Holat |
|---|---|---|---|---|---|
| `style` | Uslub | **tanlash** | `Klassik` · `Fade` · `Undercut` · `Mashinka` · `Bolalar` | — | 🆕 yangi |
| `beardIncluded` | Soqol bilan | belgilash | — | — | 🆕 yangi |

### Ayollar soch olish (`HAIRCUT_WOMEN`)

| Maydon | Nomi | Turi | Qiymatlar | Majburiy | Holat |
|---|---|---|---|---|---|
| `hairLength` | Soch uzunligi | **tanlash** | `Kalta` · `O'rta` · `Uzun` | **ha** | 🆕 yangi |
| `style` | Uslub | **tanlash** | `To'g'ri` · `Kaskad` · `Kare` · `Chelka` | — | 🆕 yangi |

### Bolalar soch olish (`KIDS`)

| Maydon | Nomi | Turi | Qiymatlar | Majburiy | Holat |
|---|---|---|---|---|---|
| `ageGroup` | Yosh | **tanlash** | `0-3` · `4-7` · `8-12` | **ha** | 🆕 yangi |
| `hasCartoon` | Multfilm bor | belgilash | — | — | 🆕 yangi |

### Soqol / ustara (`BEARD`)

| Maydon | Nomi | Turi | Qiymatlar | Majburiy | Holat |
|---|---|---|---|---|---|
| `beardService` | Xizmat | **tanlash** | `Qirqish` · `Ustara` · `Shakl berish` · `Bo'yash` | **ha** | 🆕 yangi |

### Soch bo'yash (`HAIR_COLOR`)

| Maydon | Nomi | Turi | Qiymatlar | Majburiy | Holat |
|---|---|---|---|---|---|
| `hairLength` | Soch uzunligi | **tanlash** | `Kalta` · `O'rta` · `Uzun` | **ha** | 🆕 yangi |
| `colorBrand` | Bo'yoq brendi | matn | e.g. L'Oréal | — | 🆕 yangi |
| `technique` | Texnika | **tanlash** | `To'liq` · `Ombre` · `Balayaj` · `Melirovka` · `Ildiz` | — | 🆕 yangi |

### Ukladka / styling (`STYLING`)

| Maydon | Nomi | Turi | Qiymatlar | Majburiy | Holat |
|---|---|---|---|---|---|
| `occasion` | Tadbir | **tanlash** | `Kundalik` · `To'y` · `Bayram` · `Fotosessiya` | — | 🆕 yangi |
| `hairLength` | Soch uzunligi | **tanlash** | `Kalta` · `O'rta` · `Uzun` | — | 🆕 yangi |

### Parvarish (spa) (`HAIR_CARE`)

| Maydon | Nomi | Turi | Qiymatlar | Majburiy | Holat |
|---|---|---|---|---|---|
| `procedure` | Muolaja | **tanlash** | `Keratin` · `Botoks` · `Maska` · `Peeling` | **ha** | 🆕 yangi |
| `courseSessions` | Kurs | raqam | _seans_ | — | 🆕 yangi |

### Manikyur-pedikyur (`MANICURE`)

| Maydon | Nomi | Turi | Qiymatlar | Majburiy | Holat |
|---|---|---|---|---|---|
| `coating` | Qoplama | **tanlash** | `Oddiy` · `Gel-lak` · `Kengaytma` | **ha** | 🆕 yangi |
| `withPedicure` | Pedikyur bilan | belgilash | — | — | 🆕 yangi |

---

# 💅 Go'zallik saloni (`BEAUTY_SALON`)

> ⚠️ Faqat **ayol** foydalanuvchilarga.

**Narx:** `PER_ITEM` · `PER_SESSION` · `PER_PERSON` (odatiy `PER_ITEM`) · **27 maydon** · **8 kategoriya**

## Umumiy maydonlar

| Maydon | Nomi | Turi | Qiymatlar | Majburiy | Holat |
|---|---|---|---|---|---|
| `master` | Usta | matn | e.g. Malika opa | — | ✅ bor |
| `masterLevel` | Usta darajasi | **tanlash** | `Junior` · `Usta` · `Top-usta` | — | ✅ bor |
| `durationMinutes` | Davomiyligi | raqam | _daqiqa_ | — | ✅ bor |
| `byAppointment` | Oldindan yozilish | belgilash | — | — | ✅ bor |
| `hasHomeService` | Uyга borish mumkin | belgilash | — | — | 🆕 yangi |

## Kategoriya maydonlari

### Soch turmagi / bo'yash (`HAIR`)

| Maydon | Nomi | Turi | Qiymatlar | Majburiy | Holat |
|---|---|---|---|---|---|
| `hairLength` | Soch uzunligi | **tanlash** | `Kalta` · `O'rta` · `Uzun` | **ha** | 🆕 yangi |
| `service` | Xizmat | **tanlash** | `Turmak` · `Bo'yash` · `Kesish` · `Keratin` · `Ombre` | **ha** | 🆕 yangi |
| `colorBrand` | Bo'yoq brendi | matn | e.g. Estel | — | 🆕 yangi |

### Makiyaj (`MAKEUP`)

| Maydon | Nomi | Turi | Qiymatlar | Majburiy | Holat |
|---|---|---|---|---|---|
| `occasion` | Tadbir | **tanlash** | `Kundalik` · `Kechki` · `To'y` · `Fotosessiya` | **ha** | 🆕 yangi |
| `cosmeticsBrand` | Kosmetika | matn | e.g. MAC | — | 🆕 yangi |
| `lashesIncluded` | Kiprik bilan | belgilash | — | — | 🆕 yangi |

### Manikyur (`MANICURE`)

| Maydon | Nomi | Turi | Qiymatlar | Majburiy | Holat |
|---|---|---|---|---|---|
| `coating` | Qoplama | **tanlash** | `Oddiy lak` · `Gel-lak` · `Kengaytma (gel)` · `Akril` | **ha** | 🆕 yangi |
| `hasDesign` | Dizayn bor | belgilash | — | — | 🆕 yangi |
| `removalIncluded` | Eskisini olish kiradi | belgilash | — | — | 🆕 yangi |

### Pedikyur (`PEDICURE`)

| Maydon | Nomi | Turi | Qiymatlar | Majburiy | Holat |
|---|---|---|---|---|---|
| `coating` | Qoplama | **tanlash** | `Oddiy lak` · `Gel-lak` | **ha** | 🆕 yangi |
| `pedicureType` | Turi | **tanlash** | `Klassik` · `Apparat` · `SPA` | **ha** | 🆕 yangi |

### Qosh / kiprik (`EYEBROWS_LASHES`)

| Maydon | Nomi | Turi | Qiymatlar | Majburiy | Holat |
|---|---|---|---|---|---|
| `procedure` | Muolaja | **tanlash** | `Qosh shakl` · `Qosh bo'yash` · `Laminatsiya` · `Kiprik yopishtirish` · `Kiprik laminatsiya` | **ha** | 🆕 yangi |
| `effect` | Effekt | **tanlash** | `Klassik` · `2D` · `3D` · `Volume` | — | 🆕 yangi |

### Kosmetologiya (`COSMETOLOGY`)

| Maydon | Nomi | Turi | Qiymatlar | Majburiy | Holat |
|---|---|---|---|---|---|
| `procedure` | Muolaja | **tanlash** | `Tozalash` · `Peeling` · `Mezoterapiya` · `Biorevitalizatsiya` · `Maska` | **ha** | 🆕 yangi |
| `isDeviceBased` | Apparatli | belgilash | — | — | 🆕 yangi |
| `courseSessions` | Kurs | raqam | _seans_ | — | 🆕 yangi |

### SPA / massaj (`SPA_MASSAGE`)

| Maydon | Nomi | Turi | Qiymatlar | Majburiy | Holat |
|---|---|---|---|---|---|
| `massageType` | Massaj turi | **tanlash** | `Klassik` · `Relaks` · `Anticellulit` · `Tosh bilan` · `Tay` | **ha** | 🆕 yangi |
| `zones` | Zonalar | teglar | e.g. Yelka, Bel, Oyoq | — | 🆕 yangi |
| `forCouples` | Juftlik uchun | belgilash | — | — | 🆕 yangi |

### Epilyatsiya (`EPILATION`)

| Maydon | Nomi | Turi | Qiymatlar | Majburiy | Holat |
|---|---|---|---|---|---|
| `method` | Usul | **tanlash** | `Shakar (shugaring)` · `Vosk` · `Lazer` · `Elektro` | **ha** | 🆕 yangi |
| `zones` | Zonalar | teglar | e.g. Oyoq, Qo'l, Qo'ltiq | **ha** | 🆕 yangi |
| `courseSessions` | Kurs | raqam | _seans_ | — | 🆕 yangi |

---

# 👕 Kiyim — jins darvozasi

`CLOTHING` да avval **Erkak/Ayol** tanlanadi (`attributes._gender`), keyin kategoriyalar almashadi:

| Jins | Kategoriyalar |
|---|---|
| 👨 Erkak | Ko'ylak/futbolka · Shim/jinsi · Kostyum · Ustki kiyim · Poyabzal · Sport kiyim · Aksessuar |
| 👩 Ayol | Ko'ylak/libos · Yubka · Bluzka · Ustki kiyim · Poyabzal · Sumka · Aksessuar |

# 🔑 Maxsus kalitlar

| Kalit | Qiymat | Ma'no |
|---|---|---|
| `_regular` | `"1"` | Oddiy e'lon (1 narx). Yo'q bo'lsa — chegirma (2 narx) |
| `_gender` | `MALE` \| `FEMALE` | Kiyim kimga mo'ljallangan |
| `_phone` | `+998…` | E'longa xos raqam |

# 🛠 Amalga oshirish

1. `ListingCatalog.kt` — `categoryAttributes()` ni to'ldirish + `attributes()` bilan **birlashtirish**
2. `catalog-seed.json` — maydonlarni `categoryAttributes` ga yozish
3. Backend — `GET /business/types/{type}/attributes-schema?categoryKey=`
4. Forma — `ListingFormSections.kt` `SELECT`/`TAGS`/`BOOLEAN` ni allaqachon chizadi → **yangi UI kodi kerak emas**

Batafsil: [`API_JSON.md`](API_JSON.md) · [`backend/BACKEND_PROMPT.md`](backend/BACKEND_PROMPT.md)
