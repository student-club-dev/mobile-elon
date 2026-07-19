# Google + Telefon (OTP) autentifikatsiyasi — sozlash

Ilovaga Firebase Auth orqali **Google Sign-In** va **Telefon raqami (SMS OTP)** ro'yxatdan
o'tish qo'shildi. Kod (UI + domain + data + platforma qatlamlari) tayyor va kompilyatsiya
bo'ladi, lekin **haqiqiy ishlashi uchun** o'zingizning Firebase loyihangiz kalitlari kerak.
Hozir repo'da faqat *placeholder* konfiguratsiya fayllari bor.

## 1. Firebase loyihasini yaratish

1. https://console.firebase.google.com → yangi loyiha.
2. **Authentication → Sign-in method** da yoqing:
   - **Google**
   - **Phone** (test raqamlari ham shu yerda qo'shiladi).

## 2. Android

1. Firebase'ga Android ilova qo'shing, package name: `uz.elonuz.app`.
2. Debug **SHA-1** ni qo'shing (Google Sign-In va Phone auth uchun majburiy):
   ```bash
   ./gradlew :androidApp:signingReport
   ```
3. Haqiqiy `google-services.json` ni yuklab olib,
   `androidApp/google-services.json` (placeholder) o'rniga qo'ying.
   > `default_web_client_id` shu fayldan avtomatik generatsiya bo'ladi —
   > `SocialAuthController.android.kt` uni resurs sifatida o'qiydi.

Boshqa hech narsa shart emas — Android tomoni to'liq ishlaydi:
- Google: **Credential Manager + Google ID** → Firebase `signInWithCredential`.
- Telefon: Firebase `PhoneAuthProvider.verifyPhoneNumber` (avtomatik SMS o'qish ham bor).

## 3. iOS

1. Firebase'ga iOS ilova qo'shing, bundle ID: `uz.elonuz.ios`
   (yoki o'zingiznikini — `GoogleService-Info.plist` va Info.plist'da moslang).
2. Haqiqiy `GoogleService-Info.plist` ni yuklab, `iosApp/iosApp/GoogleService-Info.plist`
   (placeholder) o'rniga qo'ying va Xcode target'iga qo'shilганini tekshiring.
3. `iosApp/iosApp/Info.plist` dagi `CFBundleURLTypes` → URL scheme'ni haqiqiy
   **REVERSED_CLIENT_ID** bilan almashtiring (GoogleService-Info.plist ichida bor).
4. Xcode'da **Firebase** va **GoogleSignIn** SDK'larini qo'shing (Swift Package Manager):
   - `https://github.com/firebase/firebase-ios-sdk` → `FirebaseAuth`
   - `https://github.com/google/GoogleSignIn-iOS` → `GoogleSignIn`
5. `SocialAuthBridge.swift` va o'zgargan `iOSApp.swift` allaqachon qo'shilgan —
   ular `IosSocialAuthBridge.shared.delegate` ni o'rnatadi.
6. Telefon auth uchun **APNs** (Push) sozlanishi kerak (Firebase Console → Cloud Messaging).

## Arxitektura (qanday ulangan)

```
AuthScreen (Compose, common)
  → AuthViewModel (common)
      • email/parol  → LoginUseCase → AuthRepository
      • Google/telefon → SocialAuthController (expect/actual)
                         → ExternalAuthUser
                         → SyncExternalUserUseCase → AuthRepository.syncExternalUser
```

- `SocialAuthController` — `expect` (commonMain), `actual`:
  - **Android**: `SocialAuthController.android.kt` (Firebase + Credential Manager).
  - **iOS**: `SocialAuthController.ios.kt` → `IosSocialAuthBridge` → Swift `SocialAuthBridge`.
- Backend haqiqiy bo'lganda: `AuthRepositoryImpl.syncExternalUser` ичida Firebase ID token'ni
  backendga yuborib, ilova sessiyasini oching (TODO qo'yilgan).

## Placeholder fayllar (almashtirilishi shart)

| Fayl | Holat |
|------|-------|
| `androidApp/google-services.json` | placeholder — Firebase'dan haqiqiysi bilan almashtiring |
| `iosApp/iosApp/GoogleService-Info.plist` | placeholder — almashtiring |
| `iosApp/iosApp/Info.plist` (URL scheme) | placeholder REVERSED_CLIENT_ID — almashtiring |
