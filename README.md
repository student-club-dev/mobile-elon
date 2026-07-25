# QS Business

Biznes egalari uchun ilova — chegirma va e'lonlar, biznes/filiallar, moderatsiya. Kotlin
Multiplatform (Android + iOS), Compose Multiplatform.

## 📥 APK yuklab olish (Android)

**[⬇️ app-debug.apk (28 MB)](https://github.com/student-club-dev/mobile-elon/raw/main/app-debug.apk)**

O'rnatish: faylni telefonга yuklab, oching → "Noma'lum manbalar"ga ruxsat bering → o'rnating.

> Debug variant — sinov uchun.

## 🔑 Google bilan kirish (majburiy sozlama)

Ilovaga kirishning **yagona usuli — Google hisobi**, shuning uchun client ID sozlanmasa
ilovaga umuman kirib bo'lmaydi.

1. `local.properties.example` ni `local.properties` ga ko'chiring (yoki mavjudiga qo'shing)
2. `GOOGLE_WEB_CLIENT_ID` ga Google Cloud'даgi **Web application** turidagi OAuth client ID ni yozing

```properties
GOOGLE_WEB_CLIENT_ID=1234567890-abcdefg.apps.googleusercontent.com
```

`local.properties` `.gitignore` да — qiymat repoga tushmaydi. CI'да o'sha nomdagi **muhit
o'zgaruvchisi** o'qiladi. Gradle uni `google_web_client_id` resursiga aylantiradi
(`elonUzApp/build.gradle.kts`).

Google Cloud Console'да **ikkita** OAuth client kerak:

| Turi | Nima uchun |
|---|---|
| **Web application** | ID token uchun — ID si `local.properties` ga yoziladi, backend ham shuni tekshiradi |
| **Android** | Imzoni ro'yxatдан o'tkazish — package `uz.elonuz.app` + SHA-1 (debug va release alohida) |

Debug SHA-1:

```bash
keytool -list -v -keystore ~/.android/debug.keystore \
  -alias androiddebugkey -storepass android -keypass android
```
