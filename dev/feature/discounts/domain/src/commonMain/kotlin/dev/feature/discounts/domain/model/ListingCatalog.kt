package dev.feature.discounts.domain.model

/**
 * Katalogning **klient zaxirasi** — biznes turlari, kategoriyalari va forma maydonlari.
 *
 * Asosiy manba — **backend** (`GET /v1/business/types`, `.../categories`). Bu yerdagi nusxa
 * faqat serverga yetib bo'lmaganda ishlaydi, shuning uchun u backend bilan **bir xil kalitlar**
 * ustida qurilgan: aks holda foydalanuvchi bu yerdan tur tanlab, keyin serverda rad etilgan
 * e'lon yozib qolardi.
 *
 * ⚠️ **Qo'lda tahrirlanmaydi** — backend `catalog-seed.json` faylidan ko'chirilgan
 * (27 tur, 172 kategoriya, maydonlar **tur darajasida**). Backend katalogi o'zgarsa shu
 * fayl qayta ko'chiriladi; oraliqda serverdan kelgan javob baribir ustun turadi.
 *
 * Zaxirada yo'q tur ham normal holat: [BusinessType] — ochiq kalit, server yangi tur
 * qo'shsa ilova uni o'zgarishsiz ko'rsatadi (nomi/rangi javobning o'zidan keladi).
 */
object ListingCatalog {

    /** "Boshqa" kategoriyasi — tanlansa `customCategoryName` majburiy bo'ladi. */
    const val OTHER_KEY = "OTHER"

    /** "Hammasiga" — chegirma butun assortimentga amal qiladi (eng ko'p uchraydigan holat). */
    const val ALL_KEY = "ALL"

    /** `attributes` ichidagi belgi: e'lon ODDIY (chegirmasiz). Validatsiya chegirmani o'tkazadi. */
    const val REGULAR_KEY = "_regular"

    /** `attributes` ichidagi aloqa telefoni. */
    const val PHONE_KEY = "_phone"

    /** `attributes` ichidagi e'lon jinsi (Kiyim-kechak uchun: "MALE"/"FEMALE"). */
    const val GENDER_KEY = "_gender"

    /** Zaxiradagi barcha turlar — backenddagi tartibda. */
    val types: List<BusinessType> get() = entries.keys.map(::BusinessType)

    /** Turning zaxira ma'lumoti. Noma'lum tur uchun `null` — chaqiruvchi standartga tushadi. */
    fun info(type: BusinessType): BusinessTypeFallback? = entries[type.key]

    /**
     * Jinsga mos turlar: sartaroshxona ayollarga, go'zallik saloni erkaklarga ko'rsatilmaydi.
     * Jins noma'lum bo'lsa — hammasi.
     */
    fun typesForGender(gender: Gender?): List<BusinessType> = entries
        .filter { (_, info) -> gender == null || gender in info.genders }
        .keys.map(::BusinessType)

    /** Turning kategoriyalari. Noma'lum tur — faqat "hammasiga" va "boshqa". */
    fun categories(type: BusinessType): List<ListingCategory> =
        info(type)?.categories ?: defaultCategories

    /**
     * Jinsga moslangan kategoriyalar. Faqat `CLOTHING` ga ta'sir qiladi (erkak/ayol kiyimi),
     * qolgan turlarda jins e'tiborsiz — backend ham shunday ishlaydi.
     */
    fun categoriesFor(type: BusinessType, gender: Gender?): List<ListingCategory> =
        genderCategories[type.key]?.get(gender) ?: categories(type)

    fun category(type: BusinessType, key: String): ListingCategory? {
        // Kiyimda kalitlar jinsli ro'yxatlardan keladi (SHIRTS, DRESSES...) — hammasida qidiramiz.
        val all = categories(type) + genderCategories[type.key]?.values?.flatten().orEmpty()
        return all.firstOrNull { it.key == key }
    }

    /**
     * Turga xos forma maydonlari.
     *
     * Backendda maydonlar **tur darajasida** (bir turdagi barcha kategoriyalar bir xil
     * `fields` oladi), shuning uchun kategoriya bo'yicha alohida ro'yxat yo'q.
     */
    fun attributes(type: BusinessType): List<AttributeSpec> = info(type)?.fields.orEmpty()

    /**
     * Formada ko'rsatiladigan maydonlar. Kategoriya tanlanmaguncha bo'sh — bo'lim ko'rinmaydi.
     */
    fun categoryAttributes(type: BusinessType, categoryKey: String): List<AttributeSpec> =
        if (categoryKey.isBlank()) emptyList() else attributes(type)

    /** Turga mos narx birliklari (birinchisi — odatiy). Noma'lum tur — hammasi. */
    fun priceUnits(type: BusinessType): List<PriceUnit> =
        info(type)?.priceUnits ?: PriceUnit.entries

    /** Turning odatiy narx birligi. */
    fun defaultPriceUnit(type: BusinessType): PriceUnit =
        info(type)?.defaultPriceUnit ?: PriceUnit.PER_ITEM

    /** Turga qarab "hammasiga" chipining yozuvi. */
    fun allLabel(type: BusinessType): String =
        info(type)?.allCategoryLabel ?: DEFAULT_ALL_LABEL

    /** Turga mos qo'shimchalar guruhi uchun taklif ("Hajmni tanlang" kabi). */
    fun optionGroupHint(type: BusinessType): String =
        info(type)?.optionGroupHint ?: DEFAULT_OPTION_HINT

    private const val DEFAULT_ALL_LABEL = "Hammasiga"
    private const val DEFAULT_OPTION_HINT = "Variant, qo'shimcha"

    private val defaultCategories = listOf(
        ListingCategory(ALL_KEY, DEFAULT_ALL_LABEL),
        ListingCategory(OTHER_KEY, "Boshqa"),
    )

    /** Tur → zaxira ma'lumoti. Tartib backenddagi bilan bir xil. */
    private val entries: Map<String, BusinessTypeFallback> = mapOf(
        "TENNIS" to BusinessTypeFallback(
            nameUz = "Katta tennis",
            emoji = "🎾",
            accentColor = 0xFF16A34A,
            defaultPriceUnit = PriceUnit.PER_HOUR,
            priceUnits = listOf(PriceUnit.PER_HOUR, PriceUnit.PER_SESSION, PriceUnit.PER_PERSON),
            genders = setOf(Gender.MALE, Gender.FEMALE),
            allCategoryLabel = "Barcha kortlar",
            optionGroupHint = "Kort turi, qoplama",
            categories = listOf(
                ListingCategory("ALL", "Barcha kortlar"),
                ListingCategory("OUTDOOR", "Ochiq kort"),
                ListingCategory("INDOOR", "Yopiq kort"),
                ListingCategory("TRAINING", "Trening / darslar"),
                ListingCategory("KIDS", "Bolalar guruhi"),
                ListingCategory("OTHER", "Boshqa"),
            ),
            fields = listOf(
                AttributeSpec("courtSurface", "Qoplama", AttributeKind.SELECT, options = listOf("Gruntli", "Sun'iy o't", "Qattiq (hard)")),
                AttributeSpec("sessionMinutes", "Sessiya davomiyligi", AttributeKind.NUMBER, hint = "60", suffix = "daqiqa"),
                AttributeSpec("gearIncluded", "Raketka beriladi", AttributeKind.BOOLEAN),
                AttributeSpec("coachAvailable", "Murabbiy bor", AttributeKind.BOOLEAN),
            ),
        ),
        "TABLE_TENNIS" to BusinessTypeFallback(
            nameUz = "Stol tennis",
            emoji = "🏓",
            accentColor = 0xFF0EA5E9,
            defaultPriceUnit = PriceUnit.PER_HOUR,
            priceUnits = listOf(PriceUnit.PER_HOUR, PriceUnit.PER_SESSION),
            genders = setOf(Gender.MALE, Gender.FEMALE),
            allCategoryLabel = "Barcha stollar",
            optionGroupHint = "Stollar soni",
            categories = listOf(
                ListingCategory("ALL", "Barcha stollar"),
                ListingCategory("RENT", "Stol ijarasi"),
                ListingCategory("TRAINING", "Trening"),
                ListingCategory("OTHER", "Boshqa"),
            ),
            fields = listOf(
                AttributeSpec("tables", "Stollar soni", AttributeKind.NUMBER, hint = "2", suffix = "ta"),
                AttributeSpec("sessionMinutes", "Sessiya davomiyligi", AttributeKind.NUMBER, hint = "60", suffix = "daqiqa"),
                AttributeSpec("racketsIncluded", "Raketka va koptok beriladi", AttributeKind.BOOLEAN),
            ),
        ),
        "FITNESS" to BusinessTypeFallback(
            nameUz = "Fitnes / Trenajyor zali",
            emoji = "🏋️",
            accentColor = 0xFF22C55E,
            defaultPriceUnit = PriceUnit.PER_MONTH,
            priceUnits = listOf(PriceUnit.PER_MONTH, PriceUnit.PER_SESSION, PriceUnit.PER_PERSON),
            genders = setOf(Gender.MALE, Gender.FEMALE),
            allCategoryLabel = "Barcha xizmatlar",
            optionGroupHint = "Abonement, mashg'ulot turi",
            categories = listOf(
                ListingCategory("ALL", "Barcha xizmatlar"),
                ListingCategory("GYM", "Trenajyor zali"),
                ListingCategory("GROUP", "Guruh mashg'ulotlari"),
                ListingCategory("PERSONAL", "Shaxsiy murabbiy"),
                ListingCategory("CROSSFIT", "Crossfit"),
                ListingCategory("YOGA", "Yoga / Pilates"),
                ListingCategory("OTHER", "Boshqa"),
            ),
            fields = listOf(
                AttributeSpec("durationMonths", "Abonement muddati", AttributeKind.NUMBER, hint = "1", suffix = "oy"),
                AttributeSpec("sessionsPerWeek", "Haftada", AttributeKind.NUMBER, hint = "3", suffix = "marta"),
                AttributeSpec("hasTrainer", "Murabbiy bor", AttributeKind.BOOLEAN),
                AttributeSpec("hasLocker", "Shkaf / dush bor", AttributeKind.BOOLEAN),
                AttributeSpec("hasFreeTrial", "Sinov mashg'uloti bepul", AttributeKind.BOOLEAN),
            ),
        ),
        "BOXING" to BusinessTypeFallback(
            nameUz = "Boks / Yakkakurash zali",
            emoji = "🥊",
            accentColor = 0xFFDC2626,
            defaultPriceUnit = PriceUnit.PER_MONTH,
            priceUnits = listOf(PriceUnit.PER_MONTH, PriceUnit.PER_SESSION),
            genders = setOf(Gender.MALE, Gender.FEMALE),
            allCategoryLabel = "Barcha yo'nalishlar",
            optionGroupHint = "Daraja, guruh",
            categories = listOf(
                ListingCategory("ALL", "Barcha yo'nalishlar"),
                ListingCategory("BOXING", "Boks"),
                ListingCategory("KICKBOXING", "Kikboksing"),
                ListingCategory("KIDS", "Bolalar guruhi"),
                ListingCategory("PERSONAL", "Shaxsiy trening"),
                ListingCategory("OTHER", "Boshqa"),
            ),
            fields = listOf(
                AttributeSpec("level", "Daraja", AttributeKind.SELECT, options = listOf("Boshlang'ich", "O'rta", "Professional")),
                AttributeSpec("durationMonths", "Abonement muddati", AttributeKind.NUMBER, hint = "1", suffix = "oy"),
                AttributeSpec("sessionsPerWeek", "Haftada", AttributeKind.NUMBER, hint = "3", suffix = "marta"),
                AttributeSpec("gearIncluded", "Jihoz beriladi", AttributeKind.BOOLEAN),
                AttributeSpec("hasFreeTrial", "Sinov mashg'uloti bepul", AttributeKind.BOOLEAN),
            ),
        ),
        "FOOTBALL_FIELD" to BusinessTypeFallback(
            nameUz = "Futbol maydoni",
            emoji = "⚽",
            accentColor = 0xFF15803D,
            defaultPriceUnit = PriceUnit.PER_HOUR,
            priceUnits = listOf(PriceUnit.PER_HOUR, PriceUnit.PER_SESSION),
            genders = setOf(Gender.MALE, Gender.FEMALE),
            allCategoryLabel = "Barcha maydonlar",
            optionGroupHint = "Qoplama, o'lcham",
            categories = listOf(
                ListingCategory("ALL", "Barcha maydonlar"),
                ListingCategory("INDOOR", "Yopiq (manej)"),
                ListingCategory("OUTDOOR", "Ochiq maydon"),
                ListingCategory("MINI", "Mini-futbol"),
                ListingCategory("OTHER", "Boshqa"),
            ),
            fields = listOf(
                AttributeSpec("surface", "Qoplama", AttributeKind.SELECT, options = listOf("Sun'iy o't", "Tabiiy o't", "Zal parketi")),
                AttributeSpec("fieldSize", "Maydon o'lchami", AttributeKind.SELECT, options = listOf("5x5", "6x6", "8x8", "11x11")),
                AttributeSpec("sessionMinutes", "Sessiya davomiyligi", AttributeKind.NUMBER, hint = "60", suffix = "daqiqa"),
                AttributeSpec("hasLighting", "Yoritish bor", AttributeKind.BOOLEAN),
                AttributeSpec("hasShower", "Dush / kiyinish xonasi", AttributeKind.BOOLEAN),
            ),
        ),
        "FOOTBALL_TRAINING" to BusinessTypeFallback(
            nameUz = "Futbol maktabi",
            emoji = "🥅",
            accentColor = 0xFF65A30D,
            defaultPriceUnit = PriceUnit.PER_MONTH,
            priceUnits = listOf(PriceUnit.PER_MONTH, PriceUnit.PER_SESSION),
            genders = setOf(Gender.MALE, Gender.FEMALE),
            allCategoryLabel = "Barcha guruhlar",
            optionGroupHint = "Yosh guruhi",
            categories = listOf(
                ListingCategory("ALL", "Barcha guruhlar"),
                ListingCategory("KIDS", "Bolalar guruhi"),
                ListingCategory("TEEN", "O'smirlar"),
                ListingCategory("ADULT", "Kattalar"),
                ListingCategory("GOALKEEPER", "Darvozabon maktabi"),
                ListingCategory("OTHER", "Boshqa"),
            ),
            fields = listOf(
                AttributeSpec("ageGroup", "Yosh guruhi", AttributeKind.SELECT, options = listOf("5-8 yosh", "9-12 yosh", "13-16 yosh", "Kattalar")),
                AttributeSpec("durationMonths", "Abonement muddati", AttributeKind.NUMBER, hint = "1", suffix = "oy"),
                AttributeSpec("sessionsPerWeek", "Haftada", AttributeKind.NUMBER, hint = "3", suffix = "marta"),
                AttributeSpec("hasUniform", "Forma beriladi", AttributeKind.BOOLEAN),
                AttributeSpec("hasFreeTrial", "Sinov mashg'uloti bepul", AttributeKind.BOOLEAN),
            ),
        ),
        "BASKETBALL" to BusinessTypeFallback(
            nameUz = "Basketbol maydoni",
            emoji = "🏀",
            accentColor = 0xFFEA580C,
            defaultPriceUnit = PriceUnit.PER_HOUR,
            priceUnits = listOf(PriceUnit.PER_HOUR, PriceUnit.PER_SESSION),
            genders = setOf(Gender.MALE, Gender.FEMALE),
            allCategoryLabel = "Barcha maydonlar",
            optionGroupHint = "Maydon turi",
            categories = listOf(
                ListingCategory("ALL", "Barcha maydonlar"),
                ListingCategory("INDOOR", "Yopiq zal"),
                ListingCategory("OUTDOOR", "Ochiq maydon"),
                ListingCategory("TRAINING", "Trening"),
                ListingCategory("OTHER", "Boshqa"),
            ),
            fields = listOf(
                AttributeSpec("courtType", "Maydon turi", AttributeKind.SELECT, options = listOf("To'liq maydon", "Yarim maydon (3x3)")),
                AttributeSpec("sessionMinutes", "Sessiya davomiyligi", AttributeKind.NUMBER, hint = "60", suffix = "daqiqa"),
                AttributeSpec("hasLighting", "Yoritish bor", AttributeKind.BOOLEAN),
                AttributeSpec("gearIncluded", "To'p beriladi", AttributeKind.BOOLEAN),
            ),
        ),
        "VOLLEYBALL" to BusinessTypeFallback(
            nameUz = "Voleybol maydoni",
            emoji = "🏐",
            accentColor = 0xFFEAB308,
            defaultPriceUnit = PriceUnit.PER_HOUR,
            priceUnits = listOf(PriceUnit.PER_HOUR, PriceUnit.PER_SESSION),
            genders = setOf(Gender.MALE, Gender.FEMALE),
            allCategoryLabel = "Barcha maydonlar",
            optionGroupHint = "Maydon turi",
            categories = listOf(
                ListingCategory("ALL", "Barcha maydonlar"),
                ListingCategory("INDOOR", "Yopiq zal"),
                ListingCategory("BEACH", "Plyaj voleybol"),
                ListingCategory("TRAINING", "Trening"),
                ListingCategory("OTHER", "Boshqa"),
            ),
            fields = listOf(
                AttributeSpec("sessionMinutes", "Sessiya davomiyligi", AttributeKind.NUMBER, hint = "60", suffix = "daqiqa"),
                AttributeSpec("hasLighting", "Yoritish bor", AttributeKind.BOOLEAN),
                AttributeSpec("gearIncluded", "To'p / to'r beriladi", AttributeKind.BOOLEAN),
            ),
        ),
        "SWIMMING_POOL" to BusinessTypeFallback(
            nameUz = "Suzish havzasi",
            emoji = "🏊",
            accentColor = 0xFF06B6D4,
            defaultPriceUnit = PriceUnit.PER_SESSION,
            priceUnits = listOf(PriceUnit.PER_SESSION, PriceUnit.PER_TICKET, PriceUnit.PER_MONTH),
            genders = setOf(Gender.MALE, Gender.FEMALE),
            allCategoryLabel = "Barcha xizmatlar",
            optionGroupHint = "Havza turi, sessiya",
            categories = listOf(
                ListingCategory("ALL", "Barcha xizmatlar"),
                ListingCategory("FREE_SWIM", "Erkin suzish"),
                ListingCategory("TRAINING", "Suzish darslari"),
                ListingCategory("KIDS", "Bolalar guruhi"),
                ListingCategory("AQUA_AEROBICS", "Aqua-aerobika"),
                ListingCategory("OTHER", "Boshqa"),
            ),
            fields = listOf(
                AttributeSpec("poolType", "Havza turi", AttributeKind.SELECT, options = listOf("Yopiq", "Ochiq")),
                AttributeSpec("sessionMinutes", "Sessiya davomiyligi", AttributeKind.NUMBER, hint = "45", suffix = "daqiqa"),
                AttributeSpec("hasCoach", "Murabbiy bor", AttributeKind.BOOLEAN),
                AttributeSpec("gearIncluded", "Jihoz beriladi", AttributeKind.BOOLEAN),
            ),
        ),
        "WRESTLING_MMA" to BusinessTypeFallback(
            nameUz = "Kurash / MMA",
            emoji = "🤼",
            accentColor = 0xFFB91C1C,
            defaultPriceUnit = PriceUnit.PER_MONTH,
            priceUnits = listOf(PriceUnit.PER_MONTH, PriceUnit.PER_SESSION),
            genders = setOf(Gender.MALE, Gender.FEMALE),
            allCategoryLabel = "Barcha yo'nalishlar",
            optionGroupHint = "Yo'nalish, daraja",
            categories = listOf(
                ListingCategory("ALL", "Barcha yo'nalishlar"),
                ListingCategory("WRESTLING", "Kurash"),
                ListingCategory("MMA", "MMA"),
                ListingCategory("JUDO", "Dzyudo"),
                ListingCategory("KIDS", "Bolalar guruhi"),
                ListingCategory("OTHER", "Boshqa"),
            ),
            fields = listOf(
                AttributeSpec("discipline", "Yo'nalish", AttributeKind.SELECT, options = listOf("Kurash", "MMA", "Dzyudo", "Sambo")),
                AttributeSpec("level", "Daraja", AttributeKind.SELECT, options = listOf("Boshlang'ich", "O'rta", "Professional")),
                AttributeSpec("durationMonths", "Abonement muddati", AttributeKind.NUMBER, hint = "1", suffix = "oy"),
                AttributeSpec("sessionsPerWeek", "Haftada", AttributeKind.NUMBER, hint = "3", suffix = "marta"),
                AttributeSpec("hasFreeTrial", "Sinov mashg'uloti bepul", AttributeKind.BOOLEAN),
            ),
        ),
        "BOWLING" to BusinessTypeFallback(
            nameUz = "Bouling",
            emoji = "🎳",
            accentColor = 0xFFA855F7,
            defaultPriceUnit = PriceUnit.PER_HOUR,
            priceUnits = listOf(PriceUnit.PER_HOUR, PriceUnit.PER_SESSION, PriceUnit.PER_PERSON),
            genders = setOf(Gender.MALE, Gender.FEMALE),
            allCategoryLabel = "Barcha yo'laklar",
            optionGroupHint = "Yo'lak turi",
            categories = listOf(
                ListingCategory("ALL", "Barcha yo'laklar"),
                ListingCategory("STANDARD", "Oddiy yo'lak"),
                ListingCategory("VIP", "VIP yo'lak"),
                ListingCategory("OTHER", "Boshqa"),
            ),
            fields = listOf(
                AttributeSpec("lanes", "Yo'laklar soni", AttributeKind.NUMBER, hint = "1", suffix = "yo'lak"),
                AttributeSpec("sessionMinutes", "Sessiya davomiyligi", AttributeKind.NUMBER, hint = "60", suffix = "daqiqa"),
                AttributeSpec("shoesIncluded", "Poyabzal beriladi", AttributeKind.BOOLEAN),
                AttributeSpec("maxPlayers", "Maksimal o'yinchi", AttributeKind.NUMBER, hint = "6", suffix = "kishi"),
            ),
        ),
        "BILLIARDS" to BusinessTypeFallback(
            nameUz = "Billiard",
            emoji = "🎱",
            accentColor = 0xFF7C3AED,
            defaultPriceUnit = PriceUnit.PER_HOUR,
            priceUnits = listOf(PriceUnit.PER_HOUR, PriceUnit.PER_SESSION),
            genders = setOf(Gender.MALE, Gender.FEMALE),
            allCategoryLabel = "Barcha stollar",
            optionGroupHint = "Stol turi, zal",
            categories = listOf(
                ListingCategory("ALL", "Barcha stollar"),
                ListingCategory("POOL", "Pul (Amerika)"),
                ListingCategory("RUSSIAN", "Rus billiard"),
                ListingCategory("SNOOKER", "Snuker"),
                ListingCategory("OTHER", "Boshqa"),
            ),
            fields = listOf(
                AttributeSpec("tableType", "Stol turi", AttributeKind.SELECT, options = listOf("Pul (Amerika)", "Rus", "Snuker")),
                AttributeSpec("tables", "Stollar soni", AttributeKind.NUMBER, hint = "3", suffix = "ta"),
                AttributeSpec("sessionMinutes", "Sessiya davomiyligi", AttributeKind.NUMBER, hint = "60", suffix = "daqiqa"),
                AttributeSpec("hallType", "Zal turi", AttributeKind.SELECT, options = listOf("Standart", "VIP", "Alohida xona")),
            ),
        ),
        "PLAYSTATION" to BusinessTypeFallback(
            nameUz = "PlayStation",
            emoji = "🎮",
            accentColor = 0xFF7C5CFF,
            defaultPriceUnit = PriceUnit.PER_HOUR,
            priceUnits = listOf(PriceUnit.PER_HOUR, PriceUnit.PER_SESSION, PriceUnit.PER_PERSON),
            genders = setOf(Gender.MALE, Gender.FEMALE),
            allCategoryLabel = "Barcha zallar",
            optionGroupHint = "Model, zal turi",
            categories = listOf(
                ListingCategory("ALL", "Barcha zallar"),
                ListingCategory("PS5", "PlayStation 5"),
                ListingCategory("PS4", "PlayStation 4"),
                ListingCategory("VIP", "VIP xona"),
                ListingCategory("OTHER", "Boshqa"),
            ),
            fields = listOf(
                AttributeSpec("model", "Model", AttributeKind.SELECT, options = listOf("PS5", "PS4 Pro", "PS4", "PS3"), required = true),
                AttributeSpec("joysticks", "Joystiklar", AttributeKind.SELECT, options = listOf("2 ta", "4 ta")),
                AttributeSpec("sessionMinutes", "Sessiya davomiyligi", AttributeKind.NUMBER, hint = "60", suffix = "daqiqa"),
                AttributeSpec("hallType", "Zal turi", AttributeKind.SELECT, options = listOf("Standart", "VIP", "Alohida xona")),
                AttributeSpec("games", "Mashhur o'yinlar", AttributeKind.TAGS, hint = "FIFA 25, Mortal Kombat, UFC"),
            ),
        ),
        "CYBER_CLUB" to BusinessTypeFallback(
            nameUz = "Kompyuter klubi",
            emoji = "🖥️",
            accentColor = 0xFF6366F1,
            defaultPriceUnit = PriceUnit.PER_HOUR,
            priceUnits = listOf(PriceUnit.PER_HOUR, PriceUnit.PER_SESSION),
            genders = setOf(Gender.MALE, Gender.FEMALE),
            allCategoryLabel = "Barcha joylar",
            optionGroupHint = "PC quvvati",
            categories = listOf(
                ListingCategory("ALL", "Barcha joylar"),
                ListingCategory("STANDARD", "Standart PC"),
                ListingCategory("VIP", "VIP / Gaming PC"),
                ListingCategory("PRO", "Pro / e-sport"),
                ListingCategory("OTHER", "Boshqa"),
            ),
            fields = listOf(
                AttributeSpec("pcTier", "Kompyuter quvvati", AttributeKind.SELECT, options = listOf("Standart", "Gaming", "Pro / e-sport"), required = true),
                AttributeSpec("sessionMinutes", "Sessiya davomiyligi", AttributeKind.NUMBER, hint = "60", suffix = "daqiqa"),
                AttributeSpec("hasHeadset", "Garnitura bor", AttributeKind.BOOLEAN),
                AttributeSpec("games", "O'yinlar", AttributeKind.TAGS, hint = "CS2, Dota 2, Valorant"),
            ),
        ),
        "CINEMA" to BusinessTypeFallback(
            nameUz = "Kinoteatr",
            emoji = "🎬",
            accentColor = 0xFFEF4444,
            defaultPriceUnit = PriceUnit.PER_TICKET,
            priceUnits = listOf(PriceUnit.PER_TICKET, PriceUnit.PER_PERSON, PriceUnit.PER_SESSION),
            genders = setOf(Gender.MALE, Gender.FEMALE),
            allCategoryLabel = "Barcha seanslar",
            optionGroupHint = "Format, zal turi",
            categories = listOf(
                ListingCategory("ALL", "Barcha seanslar"),
                ListingCategory("STANDARD", "Oddiy zal"),
                ListingCategory("VIP", "VIP zal"),
                ListingCategory("KIDS", "Bolalar seansi"),
                ListingCategory("OTHER", "Boshqa"),
            ),
            fields = listOf(
                AttributeSpec("eventTitle", "Film nomi", AttributeKind.TEXT, hint = "Dune: Part Three"),
                AttributeSpec("format", "Format", AttributeKind.SELECT, options = listOf("2D", "3D", "IMAX", "4DX", "VR")),
                AttributeSpec("language", "Til", AttributeKind.SELECT, options = listOf("O'zbek", "Rus", "Ingliz", "Original (subtitr)")),
                AttributeSpec("ageLimit", "Yosh chegarasi", AttributeKind.SELECT, options = listOf("0+", "6+", "12+", "16+", "18+")),
                AttributeSpec("sessionTimes", "Seans vaqtlari", AttributeKind.TAGS, hint = "12:30, 16:00, 19:40"),
            ),
        ),
        "KARAOKE" to BusinessTypeFallback(
            nameUz = "Karaoke",
            emoji = "🎤",
            accentColor = 0xFFEC4899,
            defaultPriceUnit = PriceUnit.PER_HOUR,
            priceUnits = listOf(PriceUnit.PER_HOUR, PriceUnit.PER_PERSON, PriceUnit.PER_SESSION),
            genders = setOf(Gender.MALE, Gender.FEMALE),
            allCategoryLabel = "Barcha xonalar",
            optionGroupHint = "Xona turi",
            categories = listOf(
                ListingCategory("ALL", "Barcha xonalar"),
                ListingCategory("STANDARD", "Oddiy xona"),
                ListingCategory("VIP", "VIP xona"),
                ListingCategory("OTHER", "Boshqa"),
            ),
            fields = listOf(
                AttributeSpec("roomCapacity", "Xona sig'imi", AttributeKind.NUMBER, hint = "8", suffix = "kishi"),
                AttributeSpec("sessionMinutes", "Sessiya davomiyligi", AttributeKind.NUMBER, hint = "60", suffix = "daqiqa"),
                AttributeSpec("hasFood", "Taom / ichimlik bor", AttributeKind.BOOLEAN),
                AttributeSpec("languages", "Qo'shiq tillari", AttributeKind.TAGS, hint = "O'zbek, Rus, Ingliz"),
            ),
        ),
        "EDUCATION_CENTER" to BusinessTypeFallback(
            nameUz = "O'quv markaz",
            emoji = "📚",
            accentColor = 0xFF3B82F6,
            defaultPriceUnit = PriceUnit.PER_MONTH,
            priceUnits = listOf(PriceUnit.PER_MONTH, PriceUnit.PER_COURSE, PriceUnit.PER_LESSON),
            genders = setOf(Gender.MALE, Gender.FEMALE),
            allCategoryLabel = "Barcha kurslar",
            optionGroupHint = "Yo'nalish, format",
            categories = listOf(
                ListingCategory("ALL", "Barcha kurslar"),
                ListingCategory("FOREIGN_LANGUAGES", "Chet tillari"),
                ListingCategory("IELTS_CEFR", "IELTS / CEFR"),
                ListingCategory("IT_PROGRAMMING", "IT va dasturlash"),
                ListingCategory("DESIGN", "Dizayn"),
                ListingCategory("MATH_SCIENCE", "Matematika va fanlar"),
                ListingCategory("UNIVERSITY_PREP", "Abituriyent tayyorlash"),
                ListingCategory("BUSINESS_MARKETING", "Biznes va marketing"),
                ListingCategory("MASTER_CLASS", "Master-klass"),
                ListingCategory("OTHER", "Boshqa"),
            ),
            fields = listOf(
                AttributeSpec("subject", "Yo'nalish", AttributeKind.TEXT, hint = "Ingliz tili — IELTS 6.5+"),
                AttributeSpec("level", "Daraja", AttributeKind.SELECT, options = listOf("Boshlang'ich", "O'rta", "Yuqori")),
                AttributeSpec("format", "Format", AttributeKind.SELECT, options = listOf("Offline", "Online", "Aralash")),
                AttributeSpec("durationMonths", "Davomiyligi", AttributeKind.NUMBER, hint = "3", suffix = "oy"),
                AttributeSpec("lessonsPerWeek", "Haftada", AttributeKind.NUMBER, hint = "3", suffix = "marta"),
                AttributeSpec("hasFreeTrialLesson", "Birinchi dars bepul", AttributeKind.BOOLEAN),
            ),
        ),
        "LIBRARY" to BusinessTypeFallback(
            nameUz = "Kutubxona / Co-working",
            emoji = "📖",
            accentColor = 0xFF2563EB,
            defaultPriceUnit = PriceUnit.PER_MONTH,
            priceUnits = listOf(PriceUnit.PER_MONTH, PriceUnit.PER_HOUR, PriceUnit.PER_TICKET),
            genders = setOf(Gender.MALE, Gender.FEMALE),
            allCategoryLabel = "Barcha xizmatlar",
            optionGroupHint = "Zona turi",
            categories = listOf(
                ListingCategory("ALL", "Barcha xizmatlar"),
                ListingCategory("READING_HALL", "O'qish zali"),
                ListingCategory("COWORKING", "Co-working"),
                ListingCategory("BOOK_RENT", "Kitob ijarasi"),
                ListingCategory("KIDS", "Bolalar zonasi"),
                ListingCategory("OTHER", "Boshqa"),
            ),
            fields = listOf(
                AttributeSpec("seats", "Joylar soni", AttributeKind.NUMBER, hint = "40", suffix = "joy"),
                AttributeSpec("openHours", "Ish vaqti", AttributeKind.TEXT, hint = "09:00–22:00"),
                AttributeSpec("hasWifi", "Wi-Fi bor", AttributeKind.BOOLEAN),
                AttributeSpec("hasQuietZone", "Sokin zona bor", AttributeKind.BOOLEAN),
                AttributeSpec("hasPrinting", "Chop etish xizmati bor", AttributeKind.BOOLEAN),
            ),
        ),
        "TUTOR" to BusinessTypeFallback(
            nameUz = "Repetitor",
            emoji = "🧑‍🏫",
            accentColor = 0xFF0284C7,
            defaultPriceUnit = PriceUnit.PER_LESSON,
            priceUnits = listOf(PriceUnit.PER_LESSON, PriceUnit.PER_MONTH, PriceUnit.PER_COURSE),
            genders = setOf(Gender.MALE, Gender.FEMALE),
            allCategoryLabel = "Barcha fanlar",
            optionGroupHint = "Fan, format",
            categories = listOf(
                ListingCategory("ALL", "Barcha fanlar"),
                ListingCategory("MATH", "Matematika"),
                ListingCategory("ENGLISH", "Ingliz tili"),
                ListingCategory("PHYSICS", "Fizika"),
                ListingCategory("NATIVE_LANG", "Ona tili / adabiyot"),
                ListingCategory("IT", "Informatika / IT"),
                ListingCategory("EXAM_PREP", "Imtihonga tayyorlash"),
                ListingCategory("OTHER", "Boshqa"),
            ),
            fields = listOf(
                AttributeSpec("subject", "Fan", AttributeKind.TEXT, hint = "Matematika"),
                AttributeSpec("level", "Daraja", AttributeKind.SELECT, options = listOf("Boshlang'ich", "O'rta", "Yuqori", "Abituriyent")),
                AttributeSpec("format", "Format", AttributeKind.SELECT, options = listOf("Offline", "Online", "O'quvchi uyida")),
                AttributeSpec("lessonMinutes", "Dars davomiyligi", AttributeKind.NUMBER, hint = "60", suffix = "daqiqa"),
                AttributeSpec("hasFreeTrialLesson", "Birinchi dars bepul", AttributeKind.BOOLEAN),
            ),
        ),
        "PRINTING" to BusinessTypeFallback(
            nameUz = "Bosmaxona / Tipografiya",
            emoji = "🖨️",
            accentColor = 0xFF64748B,
            defaultPriceUnit = PriceUnit.PER_ITEM,
            priceUnits = listOf(PriceUnit.PER_ITEM, PriceUnit.PER_KG),
            genders = setOf(Gender.MALE, Gender.FEMALE),
            allCategoryLabel = "Barcha xizmatlar",
            optionGroupHint = "Xizmat turi",
            categories = listOf(
                ListingCategory("ALL", "Barcha xizmatlar"),
                ListingCategory("DOCUMENT_PRINT", "Hujjat chop etish"),
                ListingCategory("COPY", "Nusxa (kseroks)"),
                ListingCategory("BANNER", "Banner / Nakleyka"),
                ListingCategory("BOOK_BINDING", "Muqova / tikish"),
                ListingCategory("PHOTO", "Foto chop etish"),
                ListingCategory("OTHER", "Boshqa"),
            ),
            fields = listOf(
                AttributeSpec("colorMode", "Rang", AttributeKind.SELECT, options = listOf("Oq-qora", "Rangli")),
                AttributeSpec("paperSize", "Qog'oz o'lchami", AttributeKind.SELECT, options = listOf("A4", "A3", "A5", "Boshqa")),
                AttributeSpec("minOrder", "Minimal buyurtma", AttributeKind.NUMBER, hint = "1", suffix = "dona"),
                AttributeSpec("express", "Tezkor (express)", AttributeKind.BOOLEAN),
            ),
        ),
        "NATIONAL_FOOD" to BusinessTypeFallback(
            nameUz = "Milliy taomlar",
            emoji = "🍲",
            accentColor = 0xFFEA580C,
            defaultPriceUnit = PriceUnit.PER_ITEM,
            priceUnits = listOf(PriceUnit.PER_ITEM, PriceUnit.PER_KG, PriceUnit.PER_PERSON),
            genders = setOf(Gender.MALE, Gender.FEMALE),
            allCategoryLabel = "Butun menyu",
            optionGroupHint = "Porsiya, tarkib",
            categories = listOf(
                ListingCategory("ALL", "Butun menyu"),
                ListingCategory("PALOV", "Osh / Palov"),
                ListingCategory("KABOB", "Kabob"),
                ListingCategory("SHORVA", "Sho'rva"),
                ListingCategory("MANTI_CHUCHVARA", "Manti / Chuchvara"),
                ListingCategory("LAGMON", "Lag'mon"),
                ListingCategory("SALAD", "Salatlar"),
                ListingCategory("OTHER", "Boshqa"),
            ),
            fields = listOf(
                AttributeSpec("portionGrams", "Porsiya", AttributeKind.NUMBER, hint = "450", suffix = "gramm"),
                AttributeSpec("spicyLevel", "O'tkirlik", AttributeKind.SELECT, options = listOf("Yo'q", "Yengil", "O'rtacha", "O'tkir")),
                AttributeSpec("isHalal", "Halol", AttributeKind.BOOLEAN),
                AttributeSpec("hasDelivery", "Yetkazib berish bor", AttributeKind.BOOLEAN),
            ),
        ),
        "FAST_FOOD" to BusinessTypeFallback(
            nameUz = "Fast food",
            emoji = "🍔",
            accentColor = 0xFFF97316,
            defaultPriceUnit = PriceUnit.PER_ITEM,
            priceUnits = listOf(PriceUnit.PER_ITEM, PriceUnit.PER_PERSON),
            genders = setOf(Gender.MALE, Gender.FEMALE),
            allCategoryLabel = "Butun menyu",
            optionGroupHint = "Porsiya, tarkib",
            categories = listOf(
                ListingCategory("ALL", "Butun menyu"),
                ListingCategory("BURGER", "Burger"),
                ListingCategory("PIZZA", "Pitsa"),
                ListingCategory("HOTDOG", "Hot-dog"),
                ListingCategory("LAVASH_SHAWARMA", "Lavash / Shaurma"),
                ListingCategory("FRIES", "Kartoshka fri"),
                ListingCategory("COMBO", "Combo setlar"),
                ListingCategory("DRINKS", "Ichimliklar"),
                ListingCategory("OTHER", "Boshqa"),
            ),
            fields = listOf(
                AttributeSpec("portionGrams", "Porsiya", AttributeKind.NUMBER, hint = "300", suffix = "gramm"),
                AttributeSpec("ingredients", "Tarkibi", AttributeKind.TAGS, hint = "Mozzarella, Tovuq, Sous"),
                AttributeSpec("isHalal", "Halol", AttributeKind.BOOLEAN),
                AttributeSpec("hasDelivery", "Yetkazib berish bor", AttributeKind.BOOLEAN),
            ),
        ),
        "SOMSA" to BusinessTypeFallback(
            nameUz = "Somsa / Nonvoyxona",
            emoji = "🥟",
            accentColor = 0xFFD97706,
            defaultPriceUnit = PriceUnit.PER_ITEM,
            priceUnits = listOf(PriceUnit.PER_ITEM, PriceUnit.PER_KG),
            genders = setOf(Gender.MALE, Gender.FEMALE),
            allCategoryLabel = "Barcha mahsulot",
            optionGroupHint = "To'ldirma, tandir",
            categories = listOf(
                ListingCategory("ALL", "Barcha mahsulot"),
                ListingCategory("MEAT_SOMSA", "Go'shtli somsa"),
                ListingCategory("POTATO_SOMSA", "Kartoshkali somsa"),
                ListingCategory("GREENS_SOMSA", "Ko'k somsa"),
                ListingCategory("TANDIR_NON", "Tandir non"),
                ListingCategory("PATIR", "Patir / Non"),
                ListingCategory("OTHER", "Boshqa"),
            ),
            fields = listOf(
                AttributeSpec("filling", "To'ldirma", AttributeKind.SELECT, options = listOf("Mol go'shti", "Qo'y go'shti", "Tovuq", "Kartoshka", "Ko'k")),
                AttributeSpec("ovenType", "Pishirish", AttributeKind.SELECT, options = listOf("Tandir", "Pech")),
                AttributeSpec("isHalal", "Halol", AttributeKind.BOOLEAN),
                AttributeSpec("hasDelivery", "Yetkazib berish bor", AttributeKind.BOOLEAN),
            ),
        ),
        "BARBERSHOP" to BusinessTypeFallback(
            nameUz = "Sartaroshxona",
            emoji = "💈",
            accentColor = 0xFF14B8A6,
            defaultPriceUnit = PriceUnit.PER_ITEM,
            priceUnits = listOf(PriceUnit.PER_ITEM, PriceUnit.PER_SESSION, PriceUnit.PER_PERSON),
            genders = setOf(Gender.MALE),
            allCategoryLabel = "Barcha xizmatlar",
            optionGroupHint = "Usta darajasi",
            categories = listOf(
                ListingCategory("ALL", "Barcha xizmatlar"),
                ListingCategory("HAIRCUT_MEN", "Erkaklar soch olish"),
                ListingCategory("KIDS", "Bolalar soch olish"),
                ListingCategory("BEARD", "Soqol / ustara"),
                ListingCategory("HAIR_COLOR", "Soch bo'yash"),
                ListingCategory("STYLING", "Ukladka / styling"),
                ListingCategory("HAIR_CARE", "Parvarish (spa)"),
                ListingCategory("OTHER", "Boshqa"),
            ),
            fields = listOf(
                AttributeSpec("master", "Usta", AttributeKind.TEXT, hint = "Aziz aka"),
                AttributeSpec("masterLevel", "Usta darajasi", AttributeKind.SELECT, options = listOf("Junior", "Usta", "Top-usta")),
                AttributeSpec("durationMinutes", "Davomiyligi", AttributeKind.NUMBER, hint = "40", suffix = "daqiqa"),
                AttributeSpec("byAppointment", "Oldindan yozilish", AttributeKind.BOOLEAN),
            ),
        ),
        "BEAUTY_SALON" to BusinessTypeFallback(
            nameUz = "Go'zallik saloni",
            emoji = "💅",
            accentColor = 0xFFF472B6,
            defaultPriceUnit = PriceUnit.PER_ITEM,
            priceUnits = listOf(PriceUnit.PER_ITEM, PriceUnit.PER_SESSION, PriceUnit.PER_PERSON),
            genders = setOf(Gender.FEMALE),
            allCategoryLabel = "Barcha xizmatlar",
            optionGroupHint = "Usta darajasi",
            categories = listOf(
                ListingCategory("ALL", "Barcha xizmatlar"),
                ListingCategory("HAIR", "Soch turmagi / bo'yash"),
                ListingCategory("MAKEUP", "Makiyaj"),
                ListingCategory("MANICURE", "Manikyur"),
                ListingCategory("PEDICURE", "Pedikyur"),
                ListingCategory("EYEBROWS_LASHES", "Qosh / kiprik"),
                ListingCategory("COSMETOLOGY", "Kosmetologiya"),
                ListingCategory("SPA_MASSAGE", "SPA / massaj"),
                ListingCategory("EPILATION", "Epilyatsiya"),
                ListingCategory("OTHER", "Boshqa"),
            ),
            fields = listOf(
                AttributeSpec("master", "Usta", AttributeKind.TEXT, hint = "Malika opa"),
                AttributeSpec("masterLevel", "Usta darajasi", AttributeKind.SELECT, options = listOf("Junior", "Usta", "Top-usta")),
                AttributeSpec("durationMinutes", "Davomiyligi", AttributeKind.NUMBER, hint = "60", suffix = "daqiqa"),
                AttributeSpec("byAppointment", "Oldindan yozilish", AttributeKind.BOOLEAN),
            ),
        ),
        "RENTAL_HOUSE" to BusinessTypeFallback(
            nameUz = "Ijara uy-joy",
            emoji = "🏠",
            accentColor = 0xFF0891B2,
            defaultPriceUnit = PriceUnit.PER_MONTH,
            priceUnits = listOf(PriceUnit.PER_MONTH, PriceUnit.PER_PERSON),
            genders = setOf(Gender.MALE, Gender.FEMALE),
            allCategoryLabel = "Barcha variantlar",
            optionGroupHint = "Uy turi, xonalar",
            categories = listOf(
                ListingCategory("ALL", "Barcha variantlar"),
                ListingCategory("ROOM", "Xona (bo'lib turish)"),
                ListingCategory("APARTMENT", "Kvartira"),
                ListingCategory("HOSTEL", "Hostel / Yotoqxona"),
                ListingCategory("STUDIO", "Studiya"),
                ListingCategory("OTHER", "Boshqa"),
            ),
            fields = listOf(
                AttributeSpec("rooms", "Xonalar soni", AttributeKind.NUMBER, hint = "2", suffix = "xona"),
                AttributeSpec("capacity", "Necha kishiga", AttributeKind.NUMBER, hint = "3", suffix = "kishi"),
                AttributeSpec("forGender", "Kimlar uchun", AttributeKind.SELECT, options = listOf("Erkaklar", "Ayollar", "Aralash")),
                AttributeSpec("furnished", "Jihozlangan", AttributeKind.BOOLEAN),
                AttributeSpec("hasWifi", "Wi-Fi bor", AttributeKind.BOOLEAN),
                AttributeSpec("utilitiesIncluded", "Kommunal narxga kiritilgan", AttributeKind.BOOLEAN),
                AttributeSpec("forStudents", "Talabalar uchun", AttributeKind.BOOLEAN),
            ),
        ),
        "CLOTHING" to BusinessTypeFallback(
            nameUz = "Kiyim-kechak",
            emoji = "👕",
            accentColor = 0xFFDB2777,
            defaultPriceUnit = PriceUnit.PER_ITEM,
            priceUnits = listOf(PriceUnit.PER_ITEM),
            genders = setOf(Gender.MALE, Gender.FEMALE),
            allCategoryLabel = "Butun assortimentga",
            optionGroupHint = "O'lcham, rang",
            categories = listOf(
                ListingCategory("ALL", "Butun assortimentga"),
                ListingCategory("MEN", "Erkaklar"),
                ListingCategory("WOMEN", "Ayollar"),
                ListingCategory("OUTERWEAR", "Ustki kiyim"),
                ListingCategory("SHOES", "Poyabzal"),
                ListingCategory("SPORTSWEAR", "Sport kiyim"),
                ListingCategory("BAGS", "Sumkalar"),
                ListingCategory("ACCESSORIES", "Aksessuarlar"),
                ListingCategory("OTHER", "Boshqa"),
            ),
            fields = listOf(
                AttributeSpec("brand", "Brend", AttributeKind.TEXT, hint = "Zara"),
                AttributeSpec("gender", "Kimlar uchun", AttributeKind.SELECT, options = listOf("Erkaklar", "Ayollar", "Uniseks", "Bolalar")),
                AttributeSpec("material", "Material", AttributeKind.TEXT, hint = "100% paxta"),
                AttributeSpec("season", "Mavsum", AttributeKind.SELECT, options = listOf("Qish", "Bahor", "Yoz", "Kuz", "Barcha mavsum")),
            ),
        ),
    )

    /** Jinsga xos kategoriyalar — backendda faqat `CLOTHING` uchun. */
    private val genderCategories: Map<String, Map<Gender?, List<ListingCategory>>> = mapOf(
        "CLOTHING" to mapOf(
            Gender.MALE to listOf(
                ListingCategory("ALL", "Butun assortimentga"),
                ListingCategory("SHIRTS", "Ko'ylak / futbolka"),
                ListingCategory("PANTS", "Shim / jinsi"),
                ListingCategory("SUITS", "Kostyum"),
                ListingCategory("OUTERWEAR", "Ustki kiyim"),
                ListingCategory("SHOES", "Poyabzal"),
                ListingCategory("SPORTSWEAR", "Sport kiyim"),
                ListingCategory("ACCESSORIES", "Aksessuar"),
                ListingCategory("OTHER", "Boshqa"),
            ),
            Gender.FEMALE to listOf(
                ListingCategory("ALL", "Butun assortimentga"),
                ListingCategory("DRESSES", "Ko'ylak / libos"),
                ListingCategory("SKIRTS", "Yubka"),
                ListingCategory("BLOUSES", "Bluzka"),
                ListingCategory("OUTERWEAR", "Ustki kiyim"),
                ListingCategory("SHOES", "Poyabzal"),
                ListingCategory("BAGS", "Sumka"),
                ListingCategory("ACCESSORIES", "Aksessuar"),
                ListingCategory("OTHER", "Boshqa"),
            ),
        ),
    )
}

/**
 * Bitta biznes turining zaxira ma'lumoti — serverdan kelgan `BusinessTypeInfo` ning
 * offline ekvivalenti. Server javob bersa **o'sha** ishlatiladi, bu esa faqat o'rin bosadi.
 */
data class BusinessTypeFallback(
    val nameUz: String,
    val emoji: String,
    val accentColor: Long,
    val defaultPriceUnit: PriceUnit,
    val priceUnits: List<PriceUnit>,
    /** Qaysi jinsdagi foydalanuvchiga ko'rsatiladi. */
    val genders: Set<Gender>,
    val allCategoryLabel: String,
    val optionGroupHint: String,
    val categories: List<ListingCategory>,
    /** Forma maydonlari — backendda ular **tur darajasida**. */
    val fields: List<AttributeSpec>,
)
