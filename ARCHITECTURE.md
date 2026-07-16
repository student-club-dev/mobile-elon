# Student Clubs — KMP Arxitektura

Kotlin Multiplatform (Android + iOS), Compose Multiplatform UI, modulli "feature + core" arxitektura.
Modullar `dev/` papkasi ostida joylashgan.

## Modullar daraxti

```
StudentClubs/
├── androidApp/                 # Android entry point (MainActivity, Application)
├── iosApp/                     # iOS entry point (SwiftUI + Shared.framework)
└── dev/
    ├── shared/                 # iOS framework (Shared) + Compose App() ildizi
    ├── core/
    │   ├── common/             # Resource<T>, AppDispatchers, Platform (expect/actual)
    │   ├── designsystem/       # Compose MP — AppTheme, ranglar
    │   ├── network/            # Ktor klient + OpenAPI'dan generatsiya qilingan API
    │   ├── database/           # SQLDelight (DriverFactory expect/actual)
    │   ├── domain/             # Modellar, repository interfeyslari, use-case'lar
    │   ├── data/               # Repository implementatsiyalari, DTO, mapper
    │   └── di/                 # Koin modullari, initKoin
    └── feature/
        └── auth/               # presentation (ViewModel + Screen) / domain / data / di
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

## JSON (OpenAPI) → API generatsiyasi

- Spetsifikatsiya: `dev/core/network/openapi/student-clubs.json`
- Plagin: `org.openapi.generator` (`library = multiplatform`, Ktor + kotlinx.serialization)
- Generatsiya qilingan kod: `dev/core/network/build/generated/openapi/.../generated/{api,model,infrastructure}`
- Har kompilyatsiyadan oldin avtomatik ishga tushadi (`openApiGenerate` task).

**Haqiqiy API kelganda:** `student-clubs.json` faylini almashtiring va `./gradlew :dev:core:network:openApiGenerate` ni ishga tushiring.

## Texnologiyalar

| Soha | Texnologiya |
|------|-------------|
| Build | Gradle 8.10.2, AGP 8.7.3, Kotlin 2.1.0 |
| UI | Compose Multiplatform 1.7.3 |
| DI | Koin 4.0 |
| Network | Ktor 3.0 + OpenAPI Generator 7.10 |
| Serialization | kotlinx.serialization |
| DB | SQLDelight 2.0 |
| Async | kotlinx.coroutines 1.9 |

## Ishga tushirish

**Android:**
```bash
./gradlew :androidApp:assembleDebug
```

**iOS:** `iosApp/iosApp.xcodeproj` ni Xcode'da oching va Run bosing
(framework `embedAndSignAppleFrameworkForXcode` orqali avtomatik quriladi).

> Gradle JDK 17 (Temurin) ishlatadi — `gradle.properties` da `org.gradle.java.home` orqali belgilangan.
