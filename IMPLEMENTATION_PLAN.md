# ElonUz — To'liq Yakunlash Rejasi (Master Promt)

> Bu fayl — Claude uchun ishchi promt/checklist. Maqsad: **har bir button ishlaydigan** bo'lishi,
> ilova **real API** bilan ishlashga tayyor bo'lishi, va **local DB** (Android + iOS) to'liq ishlashi.
> Foydalanuvchi bandlarni **birma-bir** aytadi; Claude shu bandni bajaradi va bu yerda `[x]` belgilaydi.

---

## 0. Loyihaning hozirgi holati (kontekst)

**Stack:** KMP (Android + iOS), Compose Multiplatform 1.7.3, Koin 4.0 DI, Compose Navigation
(multiplatform `2.8.0-alpha10`), Ktor 3.0 + OpenAPI Generator 7.10, SQLDelight 2.0.2,
GitLive Firebase (auth/firestore/functions), Firebase Cloud Functions (Node.js).

**Arxitektura:** MVVM + unidirectional state (`StateFlow` UI state + `Channel` bir martalik event).
Qatlam: `presentation → domain (usecase → repository interface) → data (repo impl) → network/database`.

**Muhim haqiqat:**
- Foydalanuvchi ko'radigan **deyarli hamma narsa real, ulangan kod**. Namuna (fake) seed
  ma'lumot olib tashlandi (`LocalDataSeeder.kt` o'chirildi) — endi ekran nimani ko'rsatsa,
  u backenddan kelgan yoki bo'sh.
- **Network qatlami (Ktor + OpenAPI) qurilgan, lekin ishlatilmaydi** — birorta ViewModel/UI uni chaqirmaydi.
  Yagona chaqiruv `ClubRepositoryImpl.refreshClubs()` — u ham hech qayerda ishlatilmagan.
- Auth eng to'liq ishlangan qism (faqat Firebase kalitlari kerak).

**Modul tuzilishi muammosi:** butun post-login ilova (`Home/Jobs/Students/Discounts/Chat/Profile/PostAd`)
`:dev:feature:auth` moduli ichida yashaydi. Alohida feature modullar yo'q (bu texnik qarz, hozircha bloklamaydi).

---

## 1. Ustuvorlik bo'yicha bosqichlar (roadmap)

| Bosqich | Nima | Nega |
|---|---|---|
| **A** | O'lik (ishlamaydigan) buttonlarni tuzatish | "Har bir button ishlashi kerak" — eng tez natija |
| **B** | Real API'ga tayyorlash (network qatlamini ulash, offline-first sync) | "Real api bilan ishlashga tayyor" |
| **C** | Yetishmayotgan ekran/funksiyalar (Notifications, Edit Profile, Settings, Clubs) | Buttonlar bosilganda haqiqiy ekran ochilishi uchun |
| **D** | Local DB to'liqligi (Android + iOS) + migratsiya/test | "Local db da ios va android uchun" |
| **E** | Auth konfiguratsiya (Firebase kalitlar) va yakuniy tekshiruv | Real ishga tushirish |

> Har bosqichni **birma-bir** bajaramiz. Foydalanuvchi "A1 qil" desa — faqat shu bandni bajaraman,
> qolganini kutaman.

---

## A. O'lik buttonlar / no-op harakatlar (tuzatish)

Har biri hozir bosilganda **hech narsa qilmaydi**. Aniq fayl:qator ko'rsatilgan.

- [x] **A1 — Home: Bell / Bildirishnomalar ikonkasi.** ✅
  ~~`HomeScreen.kt:114-120` — `onClick` yo'q~~ → `onOpenNotifications` callback qo'shildi;
  `MainShell.kt`'da `notifications` route + `NotificationsScreen.kt` (bo'sh-holat) yaratildi.
  Bosilganda ekran ochiladi, back ishlaydi. Android kompilyatsiya ✅.
  Keyingi: **C1** — bu ekranga NotificationsViewModel + `Notification.sq` (real data) ulanadi.

- [x] **A2 — Profile: "Profilni tahrirlash" (Edit profile).** ✅
  ~~`ProfileScreen.kt:94-97` bosiladigan emas~~ → `onEditProfile` callback + `edit_profile` route +
  **funksional `EditProfileScreen.kt`**: ism/familiya/telefon/universitet/kurs tahrirlanadi,
  `ProfileViewModel.saveProfile` → `SaveProfileUseCase` → Firestore + local kesh. C2 ham bajarildi. ✅

- [x] **A3 — Profile: "Sozlamalar" (Settings).** ✅
  ~~`ProfileScreen.kt:108` bo'sh lambda~~ → `onOpenSettings` + `settings` route +
  **`SettingsScreen.kt`**: Hisob (edit havolasi), Bildirishnoma toggle'lari (interaktiv),
  Ilova haqida (versiya), Chiqish (real logout). C3 ham bajarildi. ✅
  ⚠️ Toggle'lar hozircha local `remember` — doimiy saqlash uchun **SettingsStore** taski (pastda).

- [~] **A4 — Email login: Biometric / Face ID button.** (alohida feature'ga ko'chirildi)
  `AuthFlowViewModel.kt:379` — endi chalg'itmaydigan xabar beradi (jim emas).
  Haqiqiy biometrik login — `androidx.biometric` (Android, `FragmentActivity` kerak) + iOS `LAContext`
  bilan expect/actual `BiometricAuthenticator`. Bu **alohida to'liq task**: pastdagi "F. Biometrik login".

**A bosqich "bajarildi" mezoni:** har bir button bosilganda ko'rinadigan reaksiya bor
(ekran ochiladi yoki aniq harakat bajariladi), birorta ham "jim" button qolmaydi.

---

## B. Real API'ga tayyorlash

Maqsad: seed data o'rniga (yoki ustiga) **real backend** kelganda ilova avtomatik ishlashi.
Strategiya: **offline-first** — UI har doim SQLDelight'dan `Flow` o'qiydi; repository backend'dan
sync qilib DB'ni yangilaydi. Shunda API bo'lmasa ham ilova ishlaydi, API kelsa — yangilanadi.

- [ ] **B1 — OpenAPI spekni aniqlashtirish.**
  `dev/api-client-generator/elon-uz.json` hozir namuna. Real API kelganda shu faylni
  almashtirib `./gradlew :dev:api-client-generator:generateAllApi` ishga tushiriladi.
  → Har bir domen uchun endpoint'lar ro'yxatini bu faylga kiritish
  (universities, discounts, jobs, students, ads, chat, clubs, notifications, profile).

- [x] **B2 — `ApiConfig` / base URL'ni markazlashtirish.** ✅
  `CoreModules.kt` — `DEV_BASE_URL`/`PROD_BASE_URL` + `USE_PROD_API` bayrog'i (bitta manba).
  Real API kelганда faqat shu URL'larni almashtirasiz.

- [x] **B3 — Ktor klientga Firebase ID token'ni ulash.** ✅
  `AuthTokenProvider` (core:common) → `FirebaseTokenProvider` (auth) → Ktor `Auth Bearer` plugin.
  Har so'rovda `Authorization: Bearer <firebase id token>`; 401'da avtomatik yangilanadi.
  Network moduli Firebase'ga bog'lanmaydi (faqat interfeysga). Android build ✅.

- [ ] **B4 — Har repository'ni offline-first qilish (RemoteMediator pattern).**
  Hozir repo'lar faqat DB o'qiydi (`FeatureRepositories.kt`). Har biriga qo'shish:
  `refresh()` — API'dan olib DB'ga yozadi; UI DB'ni kuzatadi.
  Domenlar: **University, Discount, Job, Student, Ad, Chat, Club**.
  → Har `observeX()` chaqirilganda fon rejimida `refreshX()` ishga tushadi; xato bo'lsa cache qoladi.
  ✅ **BARCHA domenlar offline-first qilindi** (6 domen): Discount, Job, Student, Ad, University, Chat.
  Har biri: DTO + `RemoteDataSource` (Ktor) + `refresh()` (clear+upsert, xatoда cache) + DI + `init { refresh() }`.
  Fayllar: `dto/DiscountDto.kt`, `dto/FeatureDtos.kt`, `remote/DiscountRemoteDataSource.kt`,
  `remote/FeatureRemoteDataSources.kt`, `FeatureRepositories` (domain+data), `CoreModules.kt`, ViewModel'lar.
  Refresh triggerlari: Discounts/Jobs/Students/Chat → o'z ekran VM init'ida; University → HomeVM; Ad → ProfileVM.
  Android + iOS build ✅.
  ⚙️ Hozir `REMOTE_SYNC_ENABLED = false` (backend yo'q — seed ishlaydi). Real API kelганда `true`.
  ℹ️ Eslatma: Club allaqачon `refreshClubs()` ga ega edi (boshqa imzo); Chat faqat suhbatlar ro'yxatini
  sinxronlaydi (xabarlar real-time/alohida — B7).

- [x] **B5 — `LocalDataSeeder` olib tashlandi.** Bayroq ostiga olish o'rniga butunlay o'chirildi:
  namuna ma'lumot ekranda haqiqiy ma'lumotdan farq qilmasdi. Endi seed'ga tayangan bo'limlar
  (universitetlar ro'yxati, chat, bildirishnomalar) backend endpoint'i chiqmaguncha bo'sh.

- [ ] **B6 — Xatoliklar va yuklanish holati (`Resource<T>`) UI'da ko'rsatilishi.**
  `core:common`'da `Resource` bor. Har ekranda loading/empty/error holatlarini ko'rsatish
  (hozir seed data har doim mavjud bo'lgani uchun bu holatlar sinovdan o'tmagan).

- [x] **B7 — Chat real-time (Firestore listener).** ✅
  Tanlov: **Firestore** (loyiha allaqachon GitLive Firestore ishlatadi; WebSocket/alohida backend shart emas;
  Firestore o'zi offline cache beradi → real-time + offline-first).
  `ChatRealtimeSource` (domain) → `FirestoreChatRealtimeSource` (auth, GitLive `.snapshots` Flow) →
  `ChatRepositoryImpl` `enabled` bo'lsa Firestore'ga, aks holda local DB'ga delegatsiya qiladi.
  Struktura: `conversations/{id}` + `conversations/{id}/messages`. `send`/`markRead` Firestore'ga yozadi.
  Flag `CHAT_REALTIME_ENABLED` (AuthModule) — hozir `false` (Firebase sozlangач `true`). Android + iOS build ✅.
  - **Xabarlar tarixi real-time (yaxshilandi):** `messages()` endi `orderBy("createdAt", ASCENDING)` bilan
    **server-side tartiblangan to'liq tarixni** `.snapshots` orqali jonli oqim qiladi; har xabarga `conversationId` ulanadi.
    `ChatViewModel.send()` endi **haqiqiy epoch vaqt** (kotlinx-datetime) va `HH:mm` yorlig'i ishlatadi —
    qurilmalararo to'g'ri tartib va vaqt. `ChatViewModel` xabarlarni `flatMapLatest`+`observeMessages` bilan reaktiv oqadi.

**B bosqich "bajarildi" mezoni:** `elon-uz.json`'ni real spek bilan almashtirsak va base URL'ni
qo'ysak — ilova API'dan ma'lumot tortadi, token yuboradi, offline'da cache'dan ishlaydi.

---

## C. Yetishmayotgan ekran / funksiyalar

Bu ekranlar A bosqichdagi buttonlar ochishi kerak bo'lgan joylar.

- [x] **C1 — Notifications (Bildirishnomalar) ekrani.** ✅
  `Notification.sq` + migratsiya `2.sqm` (v3), `AppNotification`/`NotificationType` (domain),
  `NotificationRepositoryImpl`, `NotificationsViewModel`, real `NotificationsScreen` (ro'yxat,
  o'qilgan/o'qilmagan, bosilганда o'qildi, "Hammasini o'qildi"). Seeder 5 ta namuna qo'shadi (3 o'qilmagan).
  Home qo'ng'iroq badge'i endi **real o'qilmagan songa** bog'landi. androidApp build ✅.

- [x] **C2 — Edit Profile (Profilni tahrirlash) ekrani.** ✅ (A2 bilan birga)
  `EditProfileScreen.kt` — ism/familiya/telefon/universitet/kurs; `saveProfile` → Firestore + local kesh.
  Avatar (rasm) yuklash keyinroq (rasm storage kerak).

- [x] **C3 — Settings (Sozlamalar) ekrani.** ✅ (A3 bilan birga)
  `SettingsScreen.kt` — hisob, bildirishnoma toggle'lari, ilova haqida, chiqish.
  Til/mavzu almashtirish va toggle saqlash → **SettingsStore** taski.

- [x] **C5 — SettingsStore (sozlama saqlash + mavzu override).** ✅
  `SettingsRepository` (domain) → `SettingsRepositoryImpl` (SQLDelight `AppSettingEntity`).
  `ThemeMode` (SYSTEM/LIGHT/DARK) + `LocalDarkTheme` CompositionLocal (designsystem) —
  `AppTheme` va `authPalette` ikkalasi shunga ergashadi. `App.kt` mavzuni DB'dan o'qiydi.
  `SettingsViewModel` + `SettingsScreen`: mavzu tanlovi + push/email toggle'lari **doimiy saqlanadi**.
  Barcha modullar + androidApp build ✅.

- [x] **C4 — Clubs ekrani.** ✅
  `ClubsScreen` (to'liq ro'yxat, **ishlaydigan "Qo'shilish/A'zosiz"** tugmasi) + `ClubsViewModel`.
  DB: `Club.sq`ga `joined` ustuni + `setJoined` query + migratsiya `3.sqm` (v4).
  Model/mapper/repo (`setJoined`) yangilandi. Seeder 6 ta namuna klub qo'shadi.
  Home'ga "Klublar" bo'limi (gorizontal kartalar + "Barchasi" → `clubs` route). androidApp APK ✅.

---

## F. Biometrik login (A4'dan ko'chirilgan)

- [x] **F1 — `BiometricAuthenticator` expect/actual.** ✅
  - Common: `biometric/BiometricAuthenticator.kt` (expect + `BiometricOutcome` + `@Composable rememberBiometricAuthenticator()`).
  - Android: `androidx.biometric` `BiometricPrompt` — `MainActivity` endi `FragmentActivity`;
    `LocalContext`'dan activity topiladi; `BiometricManager.canAuthenticate`.
  - iOS: `LAContext` (`evaluatePolicy`, `@OptIn(ExperimentalForeignApi)`).
  - AuthNavHost Face ID tugmasi → biometrik prompt; SUCCESS → `vm.onBiometricAuthenticated()`
    (keshda sessiya bo'lsa HOME, aks holda xato). Qurilma qo'llab-quvvatlamasa aniq xabar.
  - Android APK + iOS build ✅.
  - ⏳ iOS runtime uchun `Info.plist`'ga `NSFaceIDUsageDescription` qo'shish kerak (Xcode qadami, E bilan).

---

## D. Local DB — Android + iOS to'liqligi

- [x] **D1 — Mavjud sxemani tekshirish.** ✅
  Endi 10 jadval: oldingi 8 + `AppSetting` + `Notification` (+ `Club.joined` ustuni).
  Driver: `DriverFactory.android.kt` / `.ios.kt` ikkalasi ishlaydi (build tasdiqlandi).
  B4 uchun `lastSyncedAt`/`remoteId` ustunlari — real API kelганда qo'shiladi.

- [x] **D2 — Schema migratsiyalari (PREREKVIZIT: C1, C5, B4 uchun).** ✅
  Migratsiya infratuzilmasi o'rnatildi. `migrations/1.sqm` (v1→v2) + `AppSetting.sq`.
  Generatsiya tasdiqlandi: `Schema.version = 2`; `create()` yangi o'rnatishda, `migrateInternal`
  (`oldVersion <= 1 && newVersion > 1`) mavjud bazalarda `AppSettingEntity` yaratadi.
  Drayverlar (`AndroidSqliteDriver`/`NativeSqliteDriver`) avtomatik migrate qiladi.
  Keyingi jadval (Notification, C1) → `2.sqm` bilan qo'shiladi.
  ℹ️ `verifyMigrations` yoqilmadi (schema dump kerak) — hozircha qo'lда moslik saqlanadi.

- [x] **D3 — iOS driver + butun graf iOS build.** ✅ (build darajasida)
  Gradle orqali tasdiqlandi: `compileKotlinIosSimulatorArm64` ✅, `compileKotlinIosArm64` (qurilma) ✅,
  `linkDebugFrameworkIosSimulatorArm64` ✅ — `Shared.framework` to'liq yig'ildi.
  `NativeSqliteDriver` + Schema v4 + barcha yangi kod iOS uchun quriladi va linklanadi.
  ⏳ Qolgan: haqiqiy simulyator/qurilmada **runtime** ishga tushirish (Xcode kerak) → D4.

- [x] **D4 — DB test/verify.** ✅ (haqiqiy SQLite engine'da)
  Host (JVM) testlari `dev/core/database/src/androidUnitTest/.../DatabaseSchemaTest.kt` —
  JDBC SQLite drayveri bilan **4 test o'tdi** (`testDebugUnitTest`, failures=0):
  fresh v4 sxema + CRUD (AppSetting/Notification/Club.joined), **migratsiya v1→v4 zanjiri**,
  idempotent `INSERT OR REPLACE`. SQL mantiqi umumiy → Android+iOS uchun amal qiladi.
  ⏳ Qolgan (ixtiyoriy): to'liq UI runtime'ni haqiqiy Android emulyator / iOS simulyator (Xcode)da
  qo'lda sinash — hozir bu muhitda qurilma/AVD yo'q.

---

## E. Auth konfiguratsiya (real ishga tushirish)

Bular almashtirilmaguncha social/phone auth **ishlamaydi** (placeholder):

- [ ] **E1** — `androidApp/google-services.json` — Firebase'dan haqiqiysi bilan almashtirish + debug SHA-1 qo'shish.
- [ ] **E2** — `iosApp/iosApp/GoogleService-Info.plist` + `Info.plist` URL scheme (REVERSED_CLIENT_ID).
- [ ] **E3** — `TelegramConfig.kt:14` `LOGIN_URL` — haqiqiy `web.app` sahifasiga.
- [ ] **E4** — `AuthModule.kt:23` `USE_EMAIL_CODE` — Cloud Functions deploy qilingach `true`.
- [ ] **E5** — Cloud Functions secrets: `TELEGRAM_BOT_TOKEN`, `GMAIL_EMAIL`, `GMAIL_APP_PASSWORD` + `firebase deploy`.
- [ ] **E6** — iOS Xcode'da Firebase + GoogleSignIn SPM paketlari qo'shilganini tasdiqlash.

> Bu bandlar ko'pincha **foydalanuvchi tomonidan** bajariladi (Firebase Console, Xcode). Claude qadamlarni ko'rsatadi.

---

## G. Qo'shimcha tugmalar / UX (avtomatik rejimda)

- [x] **G1 — E'lonni o'chirish** — Profil → "Mening e'lonlarim" qatoriga ✕ tugma + tasdiq dialogi → `ProfileViewModel.deleteAd` → `AdRepository.delete`. ✅
- [x] **G2 — E'lonni tahrirlash** — qatorga ✏️ tugma → `post_ad?adId=...` (ixtiyoriy nav arg) → `PostAdViewModel.loadForEdit` prefill → submit **o'sha id** bilan upsert (yangi id yaratmaydi). ✅
- [x] **G3 — Home "Barchasi"/"Ko'proq" havolalari** — Chegirmalar/Ishlar/Studentlar sarlavhalari + kategoriya chiplari + featured karta bosilганда tegishli tab ochiladi (`selectTab`). ✅
- [x] **G4 — Promo kodni nusxalash** — DiscountsScreen OfferCard'da promo kod bosilganda clipboard'ga nusxalanadi ("Nusxalandi ✓"). ✅
- [x] **G5 — Chat boshqaruvi** — xabarni o'chirish (long-press), suhbatni tozalash (sarlavha), suhbatni o'chirish (long-press), **suhbatni arxivlash** (long-press chooser + "Arxiv (N)" ko'rinishi). DB migratsiya v5 (`archived`). Firestore + local DB. ✅
- [x] **G6 — Tizim navigatsiya paneli inset'i** — `App.kt`'da global `windowInsetsPadding(navigationBars)` — butun ilova pastki 3 tizim tugmasi / iOS home indikatori ortida qolmaydi; fon gradienti panel ostida ham chiziladi. ✅

---

## H. Modullarga ajratish (feature = domain / data / presentation)

Maqsad: butun post-login ilova `:dev:feature:auth` ichida yashash muammosini (yuqoridagi
"Modul tuzilishi muammosi") hal qilish. **Profil** — birinchi ajratilgan feature, qolganlari
uchun namuna.

- [x] **H0 — Umumiy UI kit `core:designsystem` ga ko'chirildi.** ✅
  `AuthPalette`→`AppPalette`, `AuthIcons`→`AppIcons`, `AuthComponents`→`AppComponents`,
  `AuthUtils`→`AppUtils`, `AuthFontFamily`→`AppFontFamily`.
  Endi **har qanday feature** UI kitni ishlatadi va `feature:auth` ga bog'lanmaydi —
  keyingi feature'larni ajratish shu bilan ochildi.

- [x] **H1 — `:dev:feature:profile:{domain,data,presentation}`.** ✅
  - **domain** — `UserProfile` (+`displayName`/`isComplete`), `ProfileRepository`,
    use-case'lar: `ObserveProfile`/`SaveProfile`/`HasProfile`/`RefreshProfile`.
    Hech qanday framework'ga bog'lanmaydi (faqat `core:common` + coroutines).
  - **data** — `ProfileRepositoryImpl` (offline-first: UI **faqat** local DB'ni kuzatadi),
    `ProfileRemoteDataSource` interfeysi + **ikkita** implementatsiya:
    `ApiProfileRemoteDataSource` (REST, generatsiya qilingan klient) va
    `FirestoreProfileRemoteDataSource` (backendsiz rejim). Repository manbani bilmaydi.
  - **presentation** — `ProfileViewModel`/`ProfileUiState`, `ProfileScreen`, `EditProfileScreen`,
    `profileModule(useRemoteApi)` — barcha qatlamlarni bog'laydigan Koin moduli.
  - `core:domain`'dan profil butunlay olib tashlandi (`AuthRepository` endi faqat sessiya).
  - Android APK + iOS framework ✅.

- [x] **H2 — DB: profil sessiyadan ajratildi (migratsiya v6).** ✅
  Yangi `ProfileEntity` jadvali (`Profile.sq`) — egasi `feature:profile`.
  `UserEntity` profil ustunlarisiz qayta qurildi (`5.sqm`, SQLite table-rebuild);
  mavjud o'rnatishlardagi profil **ko'chiriladi**, bo'sh profil qatorlari yaratilmaydi
  (aks holda `hasProfile()` noto'g'ri `true` qaytarardi).
  Host testlari: **5/5 o'tdi** — v1→v6 zanjiri, profil ko'chishi, bo'sh profilni o'tkazib yuborish.

- [x] **H3 — B1 (qisman): OpenAPI v1 spec + profil endpointlari ulandi.** ✅
  `dev/api-client-generator/elon-uz.json` — endi **API'ning yagona manbasi**: `servers: .../v1`,
  `bearerAuth` (Firebase ID token), `GET/PUT /profile/me` + `UserProfileDto`/`UpdateProfileRequestDto`
  (+ `ProfileRoleDto`/`CourseYearDto` enum'lari).
  `openApiGenerate` → `dev.core.network.generated.api.ProfileApi` → `ApiProfileRemoteDataSource`.
  Bazaviy URL `.../v1/` ga o'tdi (`CoreModules`) — **ham** generatsiya qilingan klient,
  **ham** qo'lda yozilgan Ktor nisbiy yo'llari (`get("jobs")`) shu bazadan ishlaydi.
  ⚙️ Manba tanlovi `REMOTE_SYNC_ENABLED` bilan: `false` → Firestore, `true` → REST.

- [x] **H4 — Profil rasmi (avatar).** ✅
  - **Spec:** `POST /profile/me/avatar` (multipart `file`) → `AvatarUploadResponseDto{avatarUrl}`;
    `UserProfileDto`/`UpdateProfileRequestDto` ga `avatarUrl` qo'shildi. Generatsiya qilingan
    `ProfileApi.uploadMyAvatar(InputProvider)` → `ApiProfileRemoteDataSource`.
  - **DB:** `ProfileEntity.avatarUrl` + migratsiya `6.sqm` (v7). Testlar 5/5 ✅.
  - **Rasm tanlash** (`presentation/media/ImagePicker.kt`, expect/actual) — **ruxsat so'ramaydi**:
    Android `ActivityResultContracts.PickVisualMedia`, iOS `PHPickerViewController`.
  - **UI:** `EditProfileScreen` — bosiladigan avatar + kamera nishoni; tanlangan rasm **darrov**
    ko'rinadi (local `ImageBitmap`), fon rejimida yuklanadi; "yuklanmoqda"/xato holati bor.
    `ProfileScreen` — harf o'rniga rasm. URL'dan rasm — **Coil 3** (`App.kt` da ilovaning
    Ktor klienti bilan sozlangan `ImageLoader`, shunda himoyalangan URL'larga token ham ketadi).
  - **Chegara:** 5 MB (`UploadAvatarUseCase`), spec ham 413 qaytaradi.
  - ⚠️ **Hozir yuklash ISHLAMAYDI:** `REMOTE_SYNC_ENABLED = false` → Firestore rejimi, u esa
    fayl ombori emas va aniq xato beradi ("Rasm yuklash uchun backend kerak").
    Backend `POST /v1/profile/me/avatar` ni chiqargach — `REMOTE_SYNC_ENABLED = true`, tamom.
  - ℹ️ Generatsiya qilingan multipart chaqiruvi `Content-Disposition` ga **filename qo'ymaydi**
    (generator shunday yozadi). Server fayl nomiga tayanmasin — turini kontent bo'yicha aniqlasin.

> **Keyingi feature'ni ajratish retsepti:** H1 dagi 3 modul + `featureModule(...)` Koin
> moduli + kerakli endpoint'larni `elon-uz.json` ga qo'shib `openApiGenerate`.
> ⚠️ `MainShell` hamon `feature:auth` da va Profil ekranlarini chaqiradi — shuning uchun
> `feature:auth` → `feature:profile:presentation` bog'liqligi bor. Keyinroq `MainShell`ni
> alohida `feature:main` (yoki `dev:shared`) moduliga ko'chirsak, bu bog'liqlik yo'qoladi.

---

## Ish tartibi (Claude uchun qoidalar)

1. Foydalanuvchi bir band raqamini aytadi (masalan "A1") → **faqat shuni** bajaraman.
2. Har o'zgarishdan keyin tegishli modul kompilyatsiya bo'lishini tekshiraman
   (`./gradlew :dev:feature:auth:compileDebugKotlinAndroid` yoki tegishli task).
3. Kod uslubi atrofdagi kodga mos (Uzbek izohlar, mavjud naming/pattern).
4. Har band tugagach bu faylda `[ ]` → `[x]` belgilayman.
5. Ikkilanish bo'lsa (A4 biometrik, B7 chat, C4 clubs) — bajarishdan oldin **so'rayman**.

---

## Tezkor status jadvali

| ID | Vazifa | Holat |
|----|--------|-------|
| A1 | Home bell → notifications | ☑ |
| A2 | Edit profile button + ekran (C2) | ☑ |
| A3 | Settings button + ekran (C3) | ☑ |
| A4/F1 | Biometric login (expect/actual) | ☑ |
| B1 | OpenAPI spek (real API kutilmoqda) | ☐ |
| B2 | ApiConfig / base URL | ☑ |
| B3 | Firebase ID token → Ktor | ☑ |
| B4 | Offline-first sync — 6 domen (Discount/Job/Student/Ad/Univ/Chat) | ☑ |
| B5 | Seed feature-flag | ☐ |
| B6 | Loading/error UI | ☐ |
| B7 | Chat real-time (Firestore listener) | ☑ |
| C1 | Notifications ekran (real DB data) | ☑ |
| C2 | Edit Profile ekran | ☑ |
| C3 | Settings ekran | ☑ |
| C4 | Clubs ekran + join tugma | ☑ |
| C5 | SettingsStore + mavzu override | ☑ |
| D2 | Schema migratsiya infratuzilmasi | ☑ |
| F1 | Biometrik login (expect/actual) | ☑ |
| D1 | DB sxema tekshiruv | ☑ |
| D2 | Migratsiyalar | ☑ |
| D3 | iOS build + framework link | ☑ |
| D4 | DB verify (JVM host testlari, 4/4 ✅) | ☑ |
| E1–E6 | Auth konfiguratsiya | ☐ |
