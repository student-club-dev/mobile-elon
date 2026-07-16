package dev.feature.discounts.presentation.form

import dev.feature.discounts.domain.model.BusinessType

/**
 * Bitta biznes turining ekrandagi **matnlari**.
 *
 * Nega alohida: kafe "taom", game club "sessiya", o'quv markaz "kurs" sotadi — bir xil
 * "Sarlavha / Narx / Tavsif" yozuvlari ularning hech biriga to'g'ri kelmaydi. Har turning
 * o'z ekrani bor ([TypeForms]), ekranlar esa umumiy bloklardan yig'iladi va shu yerdagi
 * matnlarni oladi. Shunday qilib takrorlanish ham yo'q, yozuvlar ham har xil.
 */
data class ListingFormCopy(
    val screenTitle: String,
    val screenSubtitle: String,

    val businessSection: String,
    val businessHint: String,
    val categoryHint: String,

    val imagesSection: String,
    val imagesHint: String,

    val aboutSection: String,
    val titleHint: String,
    val descriptionHint: String,

    val priceSection: String,
    val priceHint: String,

    val conditionsHint: String,

    val detailsSection: String,
    val detailsHint: String,

    /** Qo'shimchalar bo'limi ko'rsatiladimi (kafeda "Hajm", kiyimda "O'lcham"...). */
    val hasOptions: Boolean,
    val optionsSection: String,
    val optionsHint: String,
    val optionGroupHint: String,
    val optionItemHint: String,
) {
    companion object {
        fun of(type: BusinessType): ListingFormCopy = when (type) {

            BusinessType.CAFE_RESTAURANT -> ListingFormCopy(
                screenTitle = "Kafe chegirmasi",
                screenSubtitle = "Taomga talaba chegirmasi",
                businessSection = "Kafe va menyu bo'limi",
                businessHint = "Kafe nomi: Chaykhana Navruz",
                categoryHint = "Menyudagi bo'limni tanlang",
                imagesSection = "Taom rasmlari",
                imagesHint = "Birinchisi muqova bo'ladi",
                aboutSection = "Taom haqida",
                titleHint = "Taom nomi: Pepperoni pitsa",
                descriptionHint = "Tarkibi, tayyorlash usuli...",
                priceSection = "Menyudagi narx",
                priceHint = "55000",
                conditionsHint = "Shart: talaba ID bilan, 12:00–17:00",
                detailsSection = "Taom tafsilotlari",
                detailsHint = "Talaba nima olishini bilishi uchun",
                hasOptions = true,
                optionsSection = "Hajm va qo'shimchalar",
                optionsHint = "Masalan: 30/35 sm, qo'shimcha pishloq",
                optionGroupHint = "Guruh nomi: Hajmni tanlang",
                optionItemHint = "Variant: 35 sm",
            )

            BusinessType.GAME_CLUB -> ListingFormCopy(
                screenTitle = "Game Club chegirmasi",
                screenSubtitle = "O'yin sessiyasiga talaba chegirmasi",
                businessSection = "Klub va qurilma",
                businessHint = "Klub nomi: CyberZone",
                categoryHint = "Qaysi qurilma yoki o'yin turi",
                imagesSection = "Zal rasmlari",
                imagesHint = "Zal, qurilmalar, o'yin joyi",
                aboutSection = "Sessiya haqida",
                titleHint = "Masalan: PS5 — 1 soat o'yin",
                descriptionHint = "Zal sharoiti, qulayliklar...",
                priceSection = "Bir soatlik narx",
                priceHint = "25000",
                conditionsHint = "Shart: talaba ID bilan, dush–juma 10:00–17:00",
                detailsSection = "Zal va qurilma ma'lumotlari",
                detailsHint = "Talaba nimaga o'tirishini bilsin",
                hasOptions = true,
                optionsSection = "Zal turi va qo'shimchalar",
                optionsHint = "Masalan: Standart / VIP, qo'shimcha joystik",
                optionGroupHint = "Guruh nomi: Zal turini tanlang",
                optionItemHint = "Variant: VIP zal",
            )

            BusinessType.GROCERY -> ListingFormCopy(
                screenTitle = "Oziq-ovqat chegirmasi",
                screenSubtitle = "Mahsulotga talaba chegirmasi",
                businessSection = "Do'kon va bo'lim",
                businessHint = "Do'kon nomi: Korzinka",
                categoryHint = "Mahsulot bo'limini tanlang",
                imagesSection = "Mahsulot rasmlari",
                imagesHint = "Qadoq va mahsulotning o'zi",
                aboutSection = "Mahsulot haqida",
                titleHint = "Mahsulot nomi: Sut 2.5%, 1 l",
                descriptionHint = "Tarkibi, saqlash sharti...",
                priceSection = "Do'kondagi narx",
                priceHint = "12000",
                conditionsHint = "Shart: talaba ID bilan, kuniga 2 tagacha",
                detailsSection = "Mahsulot tafsilotlari",
                detailsHint = "Og'irlik, muddat, brend",
                hasOptions = false,
                optionsSection = "",
                optionsHint = "",
                optionGroupHint = "",
                optionItemHint = "",
            )

            BusinessType.CLOTHING -> ListingFormCopy(
                screenTitle = "Kiyim chegirmasi",
                screenSubtitle = "Kiyimga talaba chegirmasi",
                businessSection = "Do'kon va bo'lim",
                businessHint = "Do'kon nomi: Zara",
                categoryHint = "Kiyim bo'limini tanlang",
                imagesSection = "Kiyim rasmlari",
                imagesHint = "Old va orqa ko'rinishi",
                aboutSection = "Kiyim haqida",
                titleHint = "Mahsulot nomi: Oversize futbolka",
                descriptionHint = "Material, o'lcham jadvali...",
                priceSection = "Do'kondagi narx",
                priceHint = "199000",
                conditionsHint = "Shart: talaba ID bilan, chegirma boshqa aksiyalar bilan qo'shilmaydi",
                detailsSection = "Kiyim tafsilotlari",
                detailsHint = "Brend, material, mavsum",
                hasOptions = true,
                optionsSection = "O'lcham va rang",
                optionsHint = "Talaba tanlaydigan variantlar",
                optionGroupHint = "Guruh nomi: O'lchamni tanlang",
                optionItemHint = "Variant: M",
            )

            BusinessType.EDUCATION_CENTER -> ListingFormCopy(
                screenTitle = "Kurs chegirmasi",
                screenSubtitle = "O'quv kursiga talaba chegirmasi",
                businessSection = "Markaz va yo'nalish",
                businessHint = "Markaz nomi: PDP Academy",
                categoryHint = "Kurs yo'nalishini tanlang",
                imagesSection = "Markaz rasmlari",
                imagesHint = "Sinf xonasi, o'quvchilar",
                aboutSection = "Kurs haqida",
                titleHint = "Kurs nomi: IELTS 6.5+ intensiv",
                descriptionHint = "Dastur, natija, o'qituvchi haqida...",
                priceSection = "Oylik to'lov",
                priceHint = "500000",
                conditionsHint = "Shart: faqat yangi o'quvchilar uchun",
                detailsSection = "Kurs tafsilotlari",
                detailsHint = "Davomiylik, format, daraja",
                hasOptions = false,
                optionsSection = "",
                optionsHint = "",
                optionGroupHint = "",
                optionItemHint = "",
            )

            BusinessType.ENTERTAINMENT -> ListingFormCopy(
                screenTitle = "Ko'ngilochar chegirma",
                screenSubtitle = "Chiptaga talaba chegirmasi",
                businessSection = "Muassasa va turi",
                businessHint = "Nomi: Cinema Park",
                categoryHint = "Tadbir turini tanlang",
                imagesSection = "Tadbir rasmlari",
                imagesHint = "Afisha yoki zal rasmi",
                aboutSection = "Tadbir haqida",
                titleHint = "Nomi: Dune — IMAX seansi",
                descriptionHint = "Seans, zal, qo'shimcha ma'lumot...",
                priceSection = "Chipta narxi",
                priceHint = "60000",
                conditionsHint = "Shart: dush–payshanba seanslarida",
                detailsSection = "Seans tafsilotlari",
                detailsHint = "Format, til, yosh chegarasi",
                hasOptions = false,
                optionsSection = "",
                optionsHint = "",
                optionGroupHint = "",
                optionItemHint = "",
            )

            BusinessType.ELECTRONICS -> ListingFormCopy(
                screenTitle = "Texnika chegirmasi",
                screenSubtitle = "Qurilmaga talaba chegirmasi",
                businessSection = "Do'kon va bo'lim",
                businessHint = "Do'kon nomi: Texnomart",
                categoryHint = "Qurilma bo'limini tanlang",
                imagesSection = "Qurilma rasmlari",
                imagesHint = "Qurilma va qutisi",
                aboutSection = "Qurilma haqida",
                titleHint = "Model: MacBook Air M3 13\"",
                descriptionHint = "Xotira, protsessor, komplekt...",
                priceSection = "Do'kondagi narx",
                priceHint = "14500000",
                conditionsHint = "Shart: talaba ID bilan, kafolat saqlanadi",
                detailsSection = "Qurilma tafsilotlari",
                detailsHint = "Brend, holati, kafolat",
                hasOptions = true,
                optionsSection = "Xotira va rang",
                optionsHint = "Talaba tanlaydigan variantlar",
                optionGroupHint = "Guruh nomi: Xotira hajmi",
                optionItemHint = "Variant: 512 GB",
            )

            BusinessType.BARBERSHOP -> ListingFormCopy(
                screenTitle = "Sartaroshxona chegirmasi",
                screenSubtitle = "Xizmatga talaba chegirmasi",
                businessSection = "Sartaroshxona va bo'lim",
                businessHint = "Nomi: Barber House",
                categoryHint = "Xizmat turini tanlang",
                imagesSection = "Ish namunalari",
                imagesHint = "Soch turmagi / ish natijasi rasmi",
                aboutSection = "Xizmat haqida",
                titleHint = "Xizmat: Erkaklar soch olish + soqol",
                descriptionHint = "Usta, davomiyligi, qo'shimcha xizmat...",
                priceSection = "Xizmat narxi",
                priceHint = "50000",
                conditionsHint = "Shart: dush–juma kunlari, talaba ID bilan",
                detailsSection = "Xizmat tafsilotlari",
                detailsHint = "Usta darajasi, kimlar uchun, davomiyligi",
                hasOptions = true,
                optionsSection = "Qo'shimcha xizmatlar",
                optionsHint = "Talaba tanlaydigan qo'shimchalar",
                optionGroupHint = "Guruh nomi: Qo'shimcha xizmat",
                optionItemHint = "Variant: Soch yuvish",
            )

            BusinessType.BEAUTY_SALON -> ListingFormCopy(
                screenTitle = "Go'zallik saloni chegirmasi",
                screenSubtitle = "Xizmatga talaba chegirmasi",
                businessSection = "Salon va bo'lim",
                businessHint = "Nomi: Beauty Room",
                categoryHint = "Xizmat turini tanlang",
                imagesSection = "Ish namunalari",
                imagesHint = "Xizmat natijasi rasmi",
                aboutSection = "Xizmat haqida",
                titleHint = "Xizmat: Makiyaj + soch turmagi",
                descriptionHint = "Usta, davomiyligi, qo'shimcha xizmat...",
                priceSection = "Xizmat narxi",
                priceHint = "80000",
                conditionsHint = "Shart: dush–juma kunlari, talaba ID bilan",
                detailsSection = "Xizmat tafsilotlari",
                detailsHint = "Usta, davomiyligi",
                hasOptions = true,
                optionsSection = "Qo'shimcha xizmatlar",
                optionsHint = "Talaba tanlaydigan qo'shimchalar",
                optionGroupHint = "Guruh nomi: Qo'shimcha xizmat",
                optionItemHint = "Variant: Qosh terish",
            )
        }
    }
}
