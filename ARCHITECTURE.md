# Elon Uz — KMP Arxitektura

Kotlin Multiplatform (Android + iOS), Compose Multiplatform UI, modulli "feature + core" arxitektura.
Modullar `dev/` papkasi ostida joylashgan. ElonUz — **biznes egalari** uchun ilova.

## Modullar daraxti

```
ElonUz/
├── elonUzApp/                  # Android entry point (ElonUzActivity, ElonUzApp)
├── iosApp/                     # iOS entry point (SwiftUI + Shared.framework)
└── dev/
    ├── shared/                 # iOS framework (Shared) + Compose App() ildizi
    ├── api-client-generator/   # elon-uz.json (spec) + cleanSwagger/openApiGenerate tasklari
    ├── api-client/             # Generatsiya qilingan Kotlin klienti (dev.core.network.generated)
    ├── core/
    │   ├── common/             # Resource<T>, AppDispatchers, TokenStore, Platform (expect/actual)
    │   ├── uikit/              # Compose MP — AppTheme, komponentlar, resurslar
    │   ├── network/            # Ktor klient (Bearer + refresh) + javob konverti
    │   ├── database/           # SQLDelight (DriverFactory expect/actual, migratsiyalar)
    │   ├── domain/             # Modellar, repository interfeyslari, use-case'lar
    │   ├── data/               # Repository implementatsiyalari, TokenStore, DTO, mapper
    │   └── di/                 # Koin modullari, initKoin
    └── feature/
        ├── auth/               # Kirish/ro'yxat oqimi + sessiya (backend `/v1/auth/business/…`)
        ├── business/           # Biznesmen ekranlari (sof UI)
        ├── profile/            # domain / data / presentation
        └── discounts/          # domain / data / presentation (biznes, filial, e'lon, katalog)
```

## Qatlamlar oqimi (har bir feature shu tartibda)

```
presentation (Compose Screen + ViewModel)
      │  use-case chaqiradi
      ▼
domain (UseCase → Repository interfeysi)
      │  implementatsiya
      ▼
data (RepositoryImpl) ──> network (Ktor/OpenAPI) + database (SQLDelight)
```

DI: barcha bog'lanishlar **Koin** orqali (`dev/core/di` + har feature'ning `di` paketi).

## Autentifikatsiya

Sessiya to'liq **backendда** (`/v1/auth/business/…`) — Firebase ishlatilmaydi.

1. **Kirish / ro'yxat** — telefon (`+998…`) yoki email + parol → `accessToken` (15 daqiqa) +
   `refreshToken` (rotatsiyali). SMS kod **kirish usuli emas**: u faqat raqamni tasdiqlash
   (`/otp/…`) va parolni tiklash (`/password/forgot` → `/password/reset`) uchun.
2. **Tokenlar** `TokenStore` da saqlanadi (`:dev:core:data` → SQLDelight `AppSettingEntity`).
3. **Har so'rovga** `Authorization: Bearer …` avtomatik qo'shiladi; 401 (`TOKEN_EXPIRED`) kelganda
   Ktor `Auth` plagini `refresh` qilib so'rovni takrorlaydi (`createHttpClient`).
4. **Foydalanuvchi id'si** — access-token (JWT) ning `sub` maydoni (`JwtClaims`). Biznes/e'lon
   egaligi shu id bo'yicha aniqlanadi (`SessionProvider`).
5. Local `UserEntity` — sessiyaning reaktiv keshi: ilova ochilganda avtomatik kirish shundan.

## JSON (OpenAPI) → API generatsiyasi

- Spetsifikatsiya: `dev/api-client-generator/elon-uz.json` (backend bergan **xom** NestJS fayli).
- Plagin: `org.openapi.generator` (`library = multiplatform`, Ktor + kotlinx.serialization).
- `cleanSwagger` taski spec'ni **normalizatsiya qiladi** (fayl o'zgarmaydi):
  javob konvertini yechadi, `/v1` prefiksini serverga ko'chiradi, tipsiz nullable maydonlarni
  tiplaydi, `operationId`/tag nomlarini qisqartiradi. Batafsil izoh —
  `dev/api-client-generator/build.gradle.kts`.
- Generatsiya qilingan kod: `dev/api-client-generator/build/generated-client/…`,
  uni `:dev:api-client` moduli srcDir sifatida ulaydi (paket `dev.core.network.generated`).

**Yangi spec kelganda:** `elon-uz.json` ni ustiga yozing va
`./gradlew :dev:api-client-generator:generateAllApi` ni ishga tushiring.

**Bazaviy manzil:** `dev.core.di.DEV_BASE_URL` (`https://api.studentclub.uz/v1/`).

## Texnologiyalar

| Soha | Texnologiya |
|------|-------------|
| Build | Gradle 8.13, AGP 8.13, Kotlin 2.1.0 |
| UI | Compose Multiplatform 1.7.3 |
| DI | Koin 4.0 |
| Network | Ktor 3.0 + OpenAPI Generator 7.10 |
| Serialization | kotlinx.serialization |
| DB | SQLDelight 2.0 |
| Async | kotlinx.coroutines 1.9 |

## Ishga tushirish

**Android:**
```bash
./gradlew :elonUzApp:assembleDebug
```

**iOS:** `iosApp/iosApp.xcodeproj` ni Xcode'da oching va Run bosing
(framework `embedAndSignAppleFrameworkForXcode` orqali avtomatik quriladi).

> Gradle JDK 21 (Temurin) ishlatadi — `gradle.properties` da `org.gradle.java.home` orqali belgilangan.
