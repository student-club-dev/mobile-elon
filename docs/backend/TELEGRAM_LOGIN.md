# Telegram bilan kirish — backend talabi

Ilovaga "Telegram bilan kirish" tugmasi qo'shildi, lekin **backend endpointи yo'q**, shuning
uchun hozircha u "tez orada" xabarini beradi. Ishga tushishi uchun backend quyidagilarni
qo'shishi kerak. Naqsh mavjud Google/Apple OAuth bilan bir xil: klient token/ma'lumot beradi,
backend tekshiradi va **`AuthTokensDto` (access + refresh)** qaytaradi.

## 1. Telegram bot
- BotFather'да bot yaratilsin (masalan `@qs_business_login_bot`).
- Domenга (Telegram Login Widget uchun) yoki bot username'ga ruxsat berilsin.
- Bot tokeni backend'да saqlansin (klientga BERILMAYDI).

## 2. Endpoint

```
POST /v1/auth/business/oauth/telegram
Content-Type: application/json

{
  "id":         123456789,          // Telegram user id
  "first_name": "Sherzod",
  "last_name":  "Axadov",           // ixtiyoriy
  "username":   "sherzod",          // ixtiyoriy
  "photo_url":  "https://...",      // ixtiyoriy
  "auth_date":  1690000000,         // unix vaqti
  "hash":       "…",                // Telegram imzosi (majburiy)
  "deviceName": "Samsung SM-S931B",
  "platform":   "Android"
}
```

**Tekshiruv (majburiy):** Telegram Login Widget spetsifikatsiyasi bo'yicha `hash`ни tekshirish —
`data_check_string`ni bot tokeni SHA-256 kaliti bilan HMAC qilib solishtirish; `auth_date`
eskirmaganini (masalan < 24 soat) ham tekshirish. Aks holда har kim soxta ma'lumot yubora oladi.

**Javob:** boshqa OAuth'lardagi kabi
```
200 → AuthTokensDto { accessToken, refreshToken }
401 → imzo/hash noto'g'ri
422 → maydonlar yetishmaydi
```

## 3. Hisob bog'lash
- `telegram.id` bo'yicha mavjud biznes hisobi topilса — o'shанга kiritiladi.
- Topilmasa — yangi biznes hisobi yaratiladi (Google'даgidek avtomatik ro'yxatdан o'tish).

## Klient tomoni (tayyor bo'lgach)
Endpoint qo'shilса, ilovaда: `AuthRepository.loginWithTelegram(...)` +
`AuthBusinessApi.telegramOAuth(...)` (spec qayta generatsiya qilинganda) qo'shiladi va Telegram
tugmasi Google'даgidek ulanadi. Hozir faqat backend qismi yetishmaydi.

> Eslatma: **SMS'siz OTP** so'ralган edi — buni ham backend hal qiladi: OTP'ни Telegram bot
> orqali yuborish (yoki yuqoridagi Telegram login) — ikkalasi ham shu endpoint oilasiga kiradi.
