package dev.feature.discounts.domain.model

/**
 * E'lonni publish qilish shartlari — `DISCOUNTS_BUSINESS_API.md` §6.1 ning klient tomoni.
 *
 * Backend baribir o'zi tekshiradi (klientga ishonib bo'lmaydi), lekin foydalanuvchi
 * xatoni **serverga bormasdan** ko'rishi kerak: forma tugmasi shu yerdan yoqiladi.
 *
 * Xato matnlari forma maydoniga bog'lanadi ([ListingField]) — UI ularni tegishli
 * maydon ostida ko'rsatadi.
 */
enum class ListingField {
    BUSINESS_NAME,
    CATEGORY,
    TITLE,
    IMAGES,
    PRICE,
    DISCOUNT,
    PROMO_CODE,
    REDEMPTION_URL,
    LIMITS,
    LOCATION,
    VALIDITY,
    ATTRIBUTES,
    OPTIONS,
}

data class ListingError(val field: ListingField, val message: String)

object ListingValidator {

    const val MAX_IMAGES = 5
    const val MAX_PERCENT = 90L
    const val MAX_OPTION_GROUPS = 10
    const val MAX_OPTIONS_PER_GROUP = 30
    const val MAX_BRANCHES = 20
    const val MIN_BRANCH_DISTANCE_METERS = 100.0

    /** Spec §3.5 — `validTo` `validFrom` dan ko'pi bilan bir yil keyin. */
    private const val MAX_DURATION_MILLIS = 366L * 24 * 60 * 60 * 1000

    /** Bo'sh ro'yxat — e'lon publish qilishga tayyor. */
    fun validate(listing: Listing): List<ListingError> = buildList {
        // Biznes nomi endi formadan so'ralmaydi (biznes profilidан keladi) — majburiy emas.

        if (listing.categoryKey == ListingCatalog.OTHER_KEY && listing.customCategoryName.isNullOrBlank()) {
            add(ListingError(ListingField.CATEGORY, "\"Boshqa\" tanlandi — bo'lim nomini yozing"))
        }

        when {
            listing.title.isBlank() -> add(ListingError(ListingField.TITLE, "Sarlavhani kiriting"))
            listing.title.length < 3 -> add(ListingError(ListingField.TITLE, "Sarlavha juda qisqa"))
            listing.title.length > 120 -> add(ListingError(ListingField.TITLE, "Sarlavha 120 belgidan oshmasin"))
        }

        if (listing.images.isEmpty()) {
            add(ListingError(ListingField.IMAGES, "Kamida 1 ta rasm qo'shing"))
        } else if (listing.images.size > MAX_IMAGES) {
            add(ListingError(ListingField.IMAGES, "Maksimal $MAX_IMAGES ta rasm"))
        }

        if (listing.originalPrice <= 0) {
            add(ListingError(ListingField.PRICE, "Narxni kiriting"))
        }

        // Kategoriyaga xos MAJBURIY maydonlar — masalan Futbolkaда razmer, PlayStation'да model.
        // Katalog `required = true` desa, to'ldirilmasdan e'lon joylanmaydi.
        ListingCatalog.categoryAttributes(listing.businessType, listing.categoryKey)
            .filter { it.required && listing.attributes[it.key].isNullOrBlank() }
            .forEach { add(ListingError(ListingField.ATTRIBUTES, "${it.label} — tanlanmagan")) }

        // Oddiy e'londa chegirma yo'q — faqat chegirma e'lonida tekshiriladi.
        if (listing.attributes[ListingCatalog.REGULAR_KEY] != "1") {
            addAll(validateDiscount(listing))
        }

        if (listing.redemption.method == RedemptionMethod.PROMO_CODE &&
            listing.redemption.promoCode.isNullOrBlank()
        ) {
            add(ListingError(ListingField.PROMO_CODE, "Promokodni kiriting"))
        }

        // "Onlayn havola" usulida talaba chegirmani `redemption.url` orqali ishlatadi (spec §3.6) —
        // havolasiz e'lon talabaga hech narsa bermaydi.
        if (listing.redemption.method == RedemptionMethod.ONLINE_LINK) {
            val url = listing.redemption.url?.trim()
            when {
                url.isNullOrBlank() ->
                    add(ListingError(ListingField.REDEMPTION_URL, "Havolani kiriting"))
                !url.startsWith("http://") && !url.startsWith("https://") ->
                    add(ListingError(ListingField.REDEMPTION_URL, "Havola http:// yoki https:// bilan boshlansin"))
            }
        }

        addAll(validateBranches(listing))

        if (listing.validTo <= listing.validFrom) {
            add(ListingError(ListingField.VALIDITY, "Tugash sanasi boshlanishdan keyin bo'lsin"))
        }
        // Spec §3.5: `validTo` `validFrom` dan ko'pi bilan bir yil keyin bo'lishi mumkin.
        if (listing.validTo - listing.validFrom > MAX_DURATION_MILLIS) {
            add(ListingError(ListingField.VALIDITY, "Amal qilish muddati bir yildan oshmasin"))
        }

        // Turga xos maydonlar endi formada so'ralmaydi — tafsilotlar erkin tavsifda yoziladi.
        // Shu sabab bu yerda majburiy atribut tekshiruvi yo'q.

        addAll(validateLimits(listing))

        addAll(validateOptions(listing))
    }

    private fun validateDiscount(listing: Listing): List<ListingError> = buildList {
        val discount = listing.discount
        when (discount.type) {
            DiscountType.PERCENT -> when {
                discount.value <= 0 -> add(ListingError(ListingField.DISCOUNT, "Chegirma foizini kiriting"))
                discount.value > MAX_PERCENT ->
                    // Firibgarlikdan himoya: narxni sun'iy ko'tarib "95% chegirma" berish.
                    add(ListingError(ListingField.DISCOUNT, "Chegirma $MAX_PERCENT% dan oshmasin"))
            }

            DiscountType.FIXED_AMOUNT -> when {
                discount.value <= 0 -> add(ListingError(ListingField.DISCOUNT, "Chegirma summasini kiriting"))
                discount.value >= listing.originalPrice ->
                    add(ListingError(ListingField.DISCOUNT, "Chegirma narxdan kam bo'lsin"))
            }

            DiscountType.SPECIAL_PRICE -> when {
                discount.value <= 0 -> add(ListingError(ListingField.DISCOUNT, "Talaba narxini kiriting"))
                discount.value >= listing.originalPrice ->
                    add(ListingError(ListingField.DISCOUNT, "Talaba narxi asl narxdan past bo'lsin"))
            }

            // 1+1 — narx o'zgarmaydi, lekin talaba nima olishini bilishi kerak.
            DiscountType.FREE_ITEM ->
                if (discount.conditions.isNullOrBlank()) {
                    add(ListingError(ListingField.DISCOUNT, "Aksiya shartini yozing (masalan: ikkinchi kofe bepul)"))
                }
        }
    }

    /**
     * Filiallar. Har biri xaritadan tanlangani uchun koordinatasi bor — tekshiriladigan narsa
     * uning O'zbekiston chegarasida ekani va filiallar bir-birini takrorlamasligi.
     */
    private fun validateBranches(listing: Listing): List<ListingError> = buildList {
        if (listing.branches.isEmpty()) {
            add(ListingError(ListingField.LOCATION, "Kamida bitta filialni belgilang"))
            return@buildList
        }
        if (listing.branches.size > MAX_BRANCHES) {
            add(ListingError(ListingField.LOCATION, "Maksimal $MAX_BRANCHES ta filial"))
        }

        listing.branches.forEach { branch ->
            if (!branch.hasValidCoordinates) {
                add(ListingError(ListingField.LOCATION, "Nuqta O'zbekiston hududidan tashqarida"))
            }
            if (branch.address.isBlank()) {
                add(ListingError(ListingField.LOCATION, "Filial manzili bo'sh"))
            }
        }

        // Bitta joyni ikki marta belgilash — spec §6.6: 100 m radiusda dublikat filial bo'lmaydi.
        listing.branches.forEachIndexed { i, a ->
            listing.branches.drop(i + 1).forEach { b ->
                if (Geo.distanceMeters(a.lat, a.lng, b.lat, b.lng) < MIN_BRANCH_DISTANCE_METERS) {
                    add(ListingError(ListingField.LOCATION, "Ikkita filial bir joyda belgilangan"))
                }
            }
        }
    }

    private fun validateOptions(listing: Listing): List<ListingError> = buildList {
        if (listing.optionGroups.size > MAX_OPTION_GROUPS) {
            add(ListingError(ListingField.OPTIONS, "Maksimal $MAX_OPTION_GROUPS ta qo'shimcha guruhi"))
        }
        listing.optionGroups.forEach { group ->
            if (group.name.isBlank()) {
                add(ListingError(ListingField.OPTIONS, "Qo'shimcha guruhining nomini kiriting"))
            }
            if (group.options.isEmpty()) {
                add(ListingError(ListingField.OPTIONS, "\"${group.name}\" guruhida kamida 1 ta variant bo'lsin"))
            }
            if (group.options.size > MAX_OPTIONS_PER_GROUP) {
                add(ListingError(ListingField.OPTIONS, "\"${group.name}\" da $MAX_OPTIONS_PER_GROUP tadan ko'p variant"))
            }
            if (group.options.any { it.name.isBlank() }) {
                add(ListingError(ListingField.OPTIONS, "\"${group.name}\" da nomsiz variant bor"))
            }
            // Min/maks faqat ko'p tanlovli guruhda ma'noga ega va bir-biriga zid bo'lmasligi kerak.
            if (group.selectionType == SelectionType.MULTIPLE) {
                val min = group.minSelect
                val max = group.maxSelect
                if (min != null && max != null && min > max) {
                    add(ListingError(ListingField.OPTIONS, "\"${group.name}\": eng kami eng ko'pidan katta"))
                }
                if (max != null && max > group.options.size) {
                    add(ListingError(ListingField.OPTIONS, "\"${group.name}\": eng ko'pi variantlar sonidan ortiq"))
                }
            }
        }
    }

    /** Chegirmani ishlatish limitlari — backend musbat son kutadi (`null` = cheksiz). */
    private fun validateLimits(listing: Listing): List<ListingError> = buildList {
        val perUser = listing.redemption.perUserLimit
        if (perUser != null && perUser <= 0) {
            add(ListingError(ListingField.LIMITS, "Bir talaba uchun limit 1 dan kam bo'lmasin"))
        }
        val total = listing.redemption.totalLimit
        if (total != null && total <= 0) {
            add(ListingError(ListingField.LIMITS, "Umumiy limit 1 dan kam bo'lmasin"))
        }
        if (perUser != null && total != null && perUser > total) {
            add(ListingError(ListingField.LIMITS, "Bir talaba uchun limit umumiy limitdan oshmasin"))
        }
    }
}
