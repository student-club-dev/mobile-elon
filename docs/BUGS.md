# ElonUz — Bug ro'yxati (13.08.2026)

Manba: `BUG ELONUZ.docx` (Sherzod Jo'ra, 12–13.08.2026). Har bir bug alohida nomerlangan,
qavs ichida hujjatdagi skrinshot raqami ko'rsatilgan.

Holat belgilari: `[ ]` — qilinmagan, `[x]` — tuzatilgan.

**Holat (13.08.2026): 48 tasi ham tuzatildi.** `./gradlew :elonUzApp:compileDebugKotlin` va
`./gradlew testDebugUnitTest` muvaffaqiyatli o'tadi. Quyida ba'zi bandlar ostida qanday
tuzatilgani qisqacha izohlangan.

---

## A. Autentifikatsiya (Sign in / Reset password)

- [x] **1.** Error message'lar tugma tagida oddiy qizil text sifatida chiqmoqda. Ular toast
  bo'lib, ekranning yuqori o'ng tomonidan chiqib kelishi kerak. *(img 1 — Business sign-in,
  "Hisobingiz bloklangan")*
- [x] **2.** Reset password: `Your phone number` label ikki qatorga siqilib qolgan — bir
  qatorda turishi kerak. *(img 2)*
- [x] **3.** Reset password: tugma matni `Send reset link` — noto'g'ri, SMS orqali **kod**
  yuboriladi, shuning uchun `Send reset code` bo'lishi kerak. *(img 2)*
- [x] **4.** Verification code ekrani: yuqoridagi ikonka ramkasiz/wrappersiz turibdi, chapdan
  padding yo'q va sarlavha bilan bitta vertikal chiziqda emas. *(img 3)*
- [x] **5.** Verification code ekrani: orqaga qaytish tugmasi juda tepaga chiqib ketgan,
  status bar bilan ustma-ust tushmoqda. *(img 3)*
- [x] **6.** New password ekrani: ikonka joylashuvi va wrapper muammosi (4-bug bilan bir xil).
  *(img 4)*
- [x] **7.** Parol muvaffaqiyatli yangilanganda hech qanday xabar ko'rsatilmay login sahifasiga
  redirect qilinmoqda — `Password updated successfully` toast kerak. *(img 4)*
- [x] **8.** Sign in: tugma bosilganda javob kelguncha loading indikatori yo'q. *(img 1)*
- [x] **9.** Sign in: home sahifasiga o'tishdan oldin password input tozalanib/reset qilinib
  ketmoqda — ko'zga tashlanadi, bunday bo'lmasligi kerak. *(img 1)*

## B. Biznes yaratish / tahrirlash

- [x] **10.** Add business → Logo yuklash maydonida border yo'q. Dashed border va mos border
  radius kerak. *(img 5)*
- [x] **11.** "Select a business type" bottom sheet'da qidiruv input'i yo'q. Sarlavhadan keyin,
  ro'yxatdan oldin qidiruv kerak (API'da search bo'lmasa mobil tomonda filter). *(img 6)*
- [x] **12.** "Select a district" bottom sheet'da ham qidiruv kerak — 20 tagacha tumani bor
  viloyatlar bor. *(img 9)*
- [x] **13.** Xarita: qidiruv placeholder'i ikki qatorga chiqib ketgan. Oddiy bir qatorli input
  bo'lsin — uzun matn kiritilsa chapga surilib ketaversin. *(img 7)*
- [x] **14.** Xarita: `OpenFreeMap © OpenMapTiles Data from OpenStreetMap` attribution matni
  ekranni egallab turibdi — yashirilishi kerak. *(img 7)*
- [x] **15.** Xarita: `My location` matni kerak emas — faqat qizil rangdagi ikonka, zoom
  controller tagida joylashsin. *(img 7)*
- [x] **16.** Xaritadan joylashuv tanlanganda tuman (district) avtomatik aniqlanmayapti —
  manzilga qarab auto-select qilinishi kerak. *(img 8)*
  → Nominatim javobidagi `city_district`/`state_district` maydonlari umuman o'qilmasdi;
  endi ular ham hisobga olinadi va nom taqqoslash "tumani/district/район" qo'shimchalarini
  tashlab, ikki tomonlama solishtiradi.
- [x] **17.** Working hours: har bir kun uchun boshlanish/tugash vaqti ikkita alohida card'da,
  ortiqcha joy egallaydi va soat kesilib qolgan (`07:0`). Bitta wrapper ichida `—` bilan
  ko'rsatilsin. *(img 10)*
- [x] **18.** Working hours: `Closed` ko'rinishi chalg'ituvchi — UI'da har kuni yopiqday
  ko'rinadi. Yaxshiroq yechim kerak. *(img 10)*
- [x] **19.** Biznes turlari uchun ikonkalar mos emas (Barbershop → gaechniy kalit, Kinoteatr →
  fotoapparat, barcha sport turlari bir xil ikonka). *(img 11, 12, 13)*
- [x] **20.** Biznes ichiga kirilganda (My listings) yuqorida edit tugmasi yo'q. *(img 14)*
- [x] **21.** My businesses: o'chirish uchun `X` ikonka ishlatilgan — `trash` ikonka bo'lishi
  kerak. *(img 15)*
- [x] **22.** Add business: majburiy maydonlar bilinmaydi (tugma disabled turaveradi). Majburiy
  maydon label'i oldiga qizil `*` qo'yilsin. *(img 16, 17)*
- [x] **23.** Add business: tugma matni `Save business` — bu saqlash emas, yaratish.
  `Create business` bo'lishi kerak. *(img 17)*
- [x] **24.** Tugmalarga rangiga mos shadow kerak — hozir fonga yopishib qolganday. *(img 18)*

## C. My listings / listing formasi

- [x] **25.** My listings header'idagi `BUSINESS CENTER` + `My listings` matni kerak emas.
  O'rniga biznes rasmi va nomi bir qatorda; nom uzun bo'lsa `line-clamp: 1` truncate. *(img 19)*
- [x] **26.** My listings: o'ng yuqoridagi profilga yuboradigan ikonka kerak emas. *(img 19)*
- [x] **27.** `Tasdiqlangan` badge tarjima qilinmagan — ingliz tilida ham o'zbekcha
  ko'rinmoqda. *(img 19)*
- [x] **28.** My listings'da ikkita qo'shish tugmasi bor. `Add listing` qoladi (chapdan `+`
  ikonka bilan), pastdagi `+ Listing` FAB olib tashlanadi. *(img 20)*
- [x] **29.** Listing formasi sahifasiga kirish/chiqishda transition silliq emas, qotib qoladi.
  *(img 21)*
- [x] **30.** Sahifaga kirishdan **oldin** loading ko'rsatilmoqda — noto'g'ri. Sahifa ochilgandan
  keyin so'rov yuborilib, loading o'sha sahifa ichida ko'rsatilishi kerak. *(img 21)*
- [x] **31.** Listing formasi tarjima qilinmagan (kategoriya chip'lari va h.k. aralash til).
  *(img 22)*
  → Kategoriya nomlari serverdan faqat o'zbekcha keladi: 102 ta kalit uchun ru/en tarjimasi
  qo'shildi (`CategoryLabels.kt`). O'zbek tilida serverning aniqroq nomi saqlanadi.
- [x] **32.** Category va listing type chip'larining fon rangi juda past — ko'rinadigan rangda
  bo'lsin. *(img 22)*
- [x] **33.** Listing formasidagi photo upload style ham tuzatilsin (10-bug bilan bir xil).
  *(img 23, 24)*
- [x] **34.** Price input: format/parse helper funksiya kerak. Summa bo'sh joy bilan
  formatlansin va format `blur`da emas, har bir input event'da ishlasin. *(img 24)*
- [x] **35.** `Qoralama saqlandi` inline banner o'rniga toast bo'lsin (default ~2.5 s). *(img 25)*
- [x] **36.** `3 fields are still empty — they are marked red above` matni ham pastda inline
  emas, toast'da chiqsin. *(img 25)*
- [x] **37.** Edit listing sahifasida tugma `Publish listing` (create) turibdi — tahrirlashda
  saqlash matni bo'lishi kerak. *(img 26)*
- [x] **38.** My listing card: amal ikonkalari orasi juda ochiq. Har bir ikonka kichik wrapperga
  o'ralib, orqasiga past to'yinganlikdagi mos fon rangi berilsin, oradagi gap kichraytirilsin.
  *(img 27)*
- [x] **39.** My listing card: `X` ikonka → `trash` ikonka. *(img 27)*
- [x] **40.** Delete bosilganda darrov o'chirib yubormasin — avval modalda item nomi bilan
  "are you sure" so'ralsin, Yes/No. Yes'da o'chiriladi. *(img 27)*
- [x] **41.** Listing card ustiga bosilganda hech narsa ochilmaydi — ko'rish yoki edit sahifasi
  ochilishi kerak. *(img 27)*
- [x] **42.** `+ Listing` tugmasi orqasiga ozgina shadow kerak. *(img 27)*
- [x] **43.** Duplicate qilingan e'lonni (`Cheeseburger 2`) edit qilib publish qilinsa ham
  qoralamada qolib ketmoqda — faol holatga o'tmayapti. *(img 28)*
- [x] **44.** Statistika bottom sheet ochilganda header va `+ Listing` tugmasi yuqoriga chiqib
  ketmoqda. *(img 29)*

## D. Profil va sozlamalar

- [x] **45.** Rus harflarida (Кириллица) font family ishlamayapti — o'zbek va ingliz tillarida
  normal. *(img 30)*
  → Sabab aniqlandi: Plus Jakarta Sans'da **kirill glifi yo'q**. Ruscha tilda butun ilova
  tizim sans-serifiga (Android — Roboto, iOS — SF Pro) o'tadi. Brend shrifti ruschada ham
  kerak bo'lsa, kirillni qo'llaydigan shrift `composeResources/font/` ga qo'shilishi lozim.
- [x] **46.** Profil: karta ustiga bosilganda ham edit sahifasi ochilsin. Yoki edit tugmasi FISH
  va telefon raqamidan keyin o'ng tarafda, vertikal markazda tursin. *(img 31)*
- [x] **47.** Settings'da til o'zgartirilganda home sahifasiga redirect bo'lmoqda — foydalanuvchi
  shu sahifada qolishi kerak (boshqa o'zgartiradigan sozlamalari bo'lishi mumkin). *(img 30)*
  → Til almashganda daraxt `key(language)` bilan qayta yaratiladi (aks holda `stringResource`
  eski tilni keshda ushlaydi). Endi karkas o'sha ondagi marshrutni tiklaydi.
- [x] **48.** Biznesni o'chirishda tasdiqlash modali yo'q — avval "are you sure" so'ralsin,
  Yes/No. Yes'da o'chiriladi. *(img 32)*
  → Tasdiq oynasi allaqachon bor edi; u umumiy `ConfirmDialog` komponentiga ko'chirildi va
  e'lon o'chirishда ham xuddi shu oyna ishlatiladi.
