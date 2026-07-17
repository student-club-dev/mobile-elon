package dev.feature.discounts.domain.model

/**
 * Biznes turi. E'lon yaratilganda tanlanadi va **keyin o'zgarmaydi** — chunki
 * kategoriyalar ([ListingCatalog.categories]) va turga xos maydonlar
 * ([ListingCatalog.attributes]) aynan shunga bog'liq.
 */
enum class BusinessType(
    val label: String,
    val emoji: String,
    val accent: Long,
    val defaultPriceUnit: PriceUnit,
) {
    GAME_CLUB("Game Club", "🎮", 0xFF7C5CFF, PriceUnit.PER_HOUR),
    CLOTHING("Kiyim-kechak", "👕", 0xFFEC4899, PriceUnit.PER_ITEM),
    CAFE_RESTAURANT("Kafe va Restoran", "🍕", 0xFFF97316, PriceUnit.PER_ITEM),
    EDUCATION_CENTER("O'quv markaz", "📚", 0xFF3B82F6, PriceUnit.PER_MONTH),
    ENTERTAINMENT("Kino va ko'ngilochar", "🎬", 0xFFEF4444, PriceUnit.PER_TICKET),
    BARBERSHOP("Sartaroshxona", "💈", 0xFF14B8A6, PriceUnit.PER_ITEM),
    BEAUTY_SALON("Go'zallik saloni", "💅", 0xFFF472B6, PriceUnit.PER_ITEM),
}

/** Foydalanuvchi jinsi — profildan olinadi, biznes turlari/kategoriyalarni moslaydi. */
enum class Gender { MALE, FEMALE }

/** Biznes turi ichidagi bo'lim: "Pitsa", "PS5", "IELTS kurslari". */
data class ListingCategory(val key: String, val label: String)

/** Turga xos maydonning kiritish usuli. */
enum class AttributeKind {
    TEXT,
    NUMBER,
    BOOLEAN,
    /** Bir nechta variant tanlanadi (masalan futbolkaда mavjud razmerlar: S, M, L). */
    MULTI_SELECT,
    /** [AttributeSpec.options] dan bittasi tanlanadi. */
    SELECT,
    /** Vergul bilan ajratilgan ro'yxat: "Mozzarella, Pepperoni". */
    TAGS,
}

/**
 * Turga xos maydon tavsifi. Forma **shu ro'yxatdan dinamik quriladi** — ilovada
 * har bir biznes turi uchun alohida forma yozilmaydi.
 *
 * Bu backenddagi `GET /business/types/{type}/attributes-schema` ning offline ekvivalenti:
 * backend yoqilganda shu ro'yxat serverdan keladi va ilova o'zgarmaydi.
 */
data class AttributeSpec(
    val key: String,
    val label: String,
    val kind: AttributeKind,
    val hint: String = "",
    val options: List<String> = emptyList(),
    val required: Boolean = false,
    /** Raqamli maydon uchun o'lchov birligi ("gramm", "oy", "daqiqa"). */
    val suffix: String? = null,
)

/**
 * Biznes turlarining kategoriyalari va maydonlari — `DISCOUNTS_BUSINESS_API.md` §4 dan.
 * Backend tayyor bo'lganda bu katalog `/business/types/...` javoblari bilan almashtiriladi.
 */
object ListingCatalog {

    /** "Boshqa" kategoriyasi — tanlansa `customCategoryName` majburiy bo'ladi. */
    const val OTHER_KEY = "OTHER"

    /** "Hammasiga" — chegirma butun assortimentga amal qiladi (eng ko'p ishlatiladigan holat). */
    const val ALL_KEY = "ALL"

    /** `attributes` ichidagi belgi: e'lon ODDIY (chegirmasiz). Validatsiya chegirmani o'tkazadi. */
    const val REGULAR_KEY = "_regular"

    /** `attributes` ichidagi aloqa telefoni. */
    const val PHONE_KEY = "_phone"

    /** `attributes` ichidagi e'lon jinsi (Kiyim-kechak uchun: "MALE"/"FEMALE"). */
    const val GENDER_KEY = "_gender"

    /**
     * Foydalanuvchi jinsiga qarab mavjud biznes turlari.
     *
     * Ayol → BeautySalon (Sartaroshxona yo'q), erkak → Sartaroshxona (BeautySalon yo'q).
     * Qolgan turlar ikkoviga ham. Jins noma'lum bo'lsa (`null`) — ikkalasi ham ko'rinadi.
     */
    fun typesForGender(gender: Gender?): List<BusinessType> = BusinessType.entries.filter { type ->
        when (type) {
            BusinessType.BARBERSHOP -> gender != Gender.FEMALE
            BusinessType.BEAUTY_SALON -> gender != Gender.MALE
            else -> true
        }
    }

    /** Turga qarab "hammasiga" chipining yozuvi. */
    fun allLabel(type: BusinessType): String = when (type) {
        BusinessType.CAFE_RESTAURANT -> "Butun menyuga"
        BusinessType.CLOTHING -> "Butun assortimentga"
        BusinessType.GAME_CLUB -> "Barcha zallarga"
        BusinessType.EDUCATION_CENTER -> "Barcha kurslarga"
        BusinessType.ENTERTAINMENT -> "Barcha seanslarga"
        BusinessType.BARBERSHOP -> "Barcha xizmatlarga"
        BusinessType.BEAUTY_SALON -> "Barcha xizmatlarga"
    }

    fun categories(type: BusinessType): List<ListingCategory> = categoryMap.getValue(type)

    /**
     * Jinsga moslangan kategoriyalar. Kiyim-kechakда erkak → erkaklar kiyim turlari,
     * ayol → ayollar kiyim turlari. Qolgan turlarда jins ta'sir qilmaydi.
     */
    fun categoriesFor(type: BusinessType, gender: Gender?): List<ListingCategory> = when {
        type == BusinessType.CLOTHING && gender == Gender.MALE -> menClothingCategories
        type == BusinessType.CLOTHING && gender == Gender.FEMALE -> womenClothingCategories
        else -> categories(type)
    }

    fun category(type: BusinessType, key: String): ListingCategory? {
        // Kiyim-kechakда kalitlar jinsли ro'yxatlardan keladi (SHIRTS, DRESSES...) — hammasида qidiramiz.
        val all = if (type == BusinessType.CLOTHING) {
            categories(type) + menClothingCategories + womenClothingCategories
        } else {
            categories(type)
        }
        return all.firstOrNull { it.key == key }
    }

    /** Erkaklar kiyim kategoriyalari (jins = erkak bo'lganda). */
    private val menClothingCategories: List<ListingCategory> = cats(
        BusinessType.CLOTHING,
        "SHIRTS" to "Ko'ylak / futbolka",
        "PANTS" to "Shim / jinsi",
        "SUITS" to "Kostyum",
        "OUTERWEAR" to "Ustki kiyim",
        "SHOES" to "Poyabzal",
        "SPORTSWEAR" to "Sport kiyim",
        "ACCESSORIES" to "Aksessuar",
    )

    /** Ayollar kiyim kategoriyalari (jins = ayol bo'lganda). */
    private val womenClothingCategories: List<ListingCategory> = cats(
        BusinessType.CLOTHING,
        "DRESSES" to "Ko'ylak / libos",
        "SKIRTS" to "Yubka",
        "BLOUSES" to "Bluzka",
        "OUTERWEAR" to "Ustki kiyim",
        "SHOES" to "Poyabzal",
        "BAGS" to "Sumka",
        "ACCESSORIES" to "Aksessuar",
    )

    fun attributes(type: BusinessType): List<AttributeSpec> = attributeMap.getValue(type)

    /**
     * Formaда ko'rsatiladigan maydonlar: **turga xos umumiy** + **kategoriyaga xos**.
     * Kategoriya tanlanmaguncha bo'sh (bo'lim ko'rinmaydi). Kalit ikkalasида ham bo'lsa —
     * kategoriya versiyasi ustun turadi.
     */
    fun categoryAttributes(type: BusinessType, categoryKey: String): List<AttributeSpec> {
        if (categoryKey.isBlank()) return emptyList()
        val extra = categoryAttributeMap[type]?.get(categoryKey).orEmpty()
        val overridden = extra.mapTo(mutableSetOf()) { it.key }
        // Kategoriyaga xos maydonlar OLDIN — foydalanuvchi endigina tanlagan narsaga tegishli
        // (Futbolka → razmerlar), umumiy maydonlar (brend, material) keyin.
        return extra + attributes(type).filterNot { it.key in overridden }
    }

    /**
     * KATEGORIYAGA xos maydonlar — turga xos umumiy maydonlar **ustiga** qo'shiladi.
     * Masalan ko'ylakда brend (umumiy) ham, o'lcham (kategoriya) ham so'raladi.
     * Kalit to'qnashsa — kategoriya versiyasi ustun (masalan CLOTHING'да `size`).
     */
    private val categoryAttributeMap: Map<BusinessType, Map<String, List<AttributeSpec>>> = mapOf(
        BusinessType.GAME_CLUB to mapOf(
            "PLAYSTATION" to listOf(
                AttributeSpec("model", "Model", AttributeKind.SELECT, options = listOf("PS5", "PS4 Pro", "PS4", "PS3"), required = true),
                AttributeSpec("joysticks", "Joystiklar", AttributeKind.SELECT, options = listOf("2 ta", "4 ta")),
                AttributeSpec("games", "Mashhur o'yinlar", AttributeKind.TAGS, hint = "FIFA 25, Mortal Kombat, UFC"),
            ),
            "TABLE_TENNIS" to listOf(
                AttributeSpec("tables", "Stollar soni", AttributeKind.NUMBER, hint = "2", suffix = "ta"),
                AttributeSpec("racketsIncluded", "Raketka va koptok beriladi", AttributeKind.BOOLEAN),
            ),
            "TENNIS" to listOf(
                AttributeSpec("courtType", "Kort turi", AttributeKind.SELECT, options = listOf("Ochiq", "Yopiq"), required = true),
                AttributeSpec("surface", "Qoplama", AttributeKind.SELECT, options = listOf("Gruntli", "Sun'iy o't", "Qattiq (hard)")),
                AttributeSpec("gearIncluded", "Raketka beriladi", AttributeKind.BOOLEAN),
            ),
            "PC_GAMING" to listOf(
                AttributeSpec("pcTier", "Kompyuter quvvati", AttributeKind.SELECT, options = listOf("Standart", "Gaming", "Pro / e-sport"), required = true),
                AttributeSpec("games", "O'yinlar", AttributeKind.TAGS, hint = "CS2, Dota 2, Valorant"),
                AttributeSpec("gpu", "Videokarta", AttributeKind.TEXT, hint = "RTX 4060"),
                AttributeSpec("monitorHz", "Monitor", AttributeKind.SELECT, options = listOf("60Hz", "144Hz", "240Hz")),
            ),
            "BILLIARDS" to listOf(
                AttributeSpec("tableType", "Stol turi", AttributeKind.SELECT, options = listOf("Pul (Amerika)", "Rus", "Snuker"), required = true),
                AttributeSpec("tables", "Stollar soni", AttributeKind.NUMBER, hint = "3", suffix = "ta"),
            ),
            "POLYA" to listOf(
                AttributeSpec("fieldType", "Maydon turi", AttributeKind.SELECT, options = listOf("Mini-futbol", "Basketbol", "Voleybol", "Boshqa")),
                AttributeSpec("fields", "Maydonlar soni", AttributeKind.NUMBER, hint = "2", suffix = "ta"),
                AttributeSpec("surface", "Qoplama", AttributeKind.SELECT, options = listOf("Sun'iy o't", "Tabiiy o't", "Parket", "Rezina")),
                AttributeSpec("hasLighting", "Yoritish bor (kechqurun)", AttributeKind.BOOLEAN),
            ),
        ),
        BusinessType.CLOTHING to mapOf(
            "SHIRTS" to listOf(
                AttributeSpec("sizes", "Mavjud razmerlar", AttributeKind.MULTI_SELECT, options = listOf("XS", "S", "M", "L", "XL", "XXL"), required = true),
                AttributeSpec("fit", "Mavjud bichimlar", AttributeKind.MULTI_SELECT, options = listOf("Slim", "Regular", "Oversize")),
                AttributeSpec("sleeve", "Yeng", AttributeKind.MULTI_SELECT, options = listOf("Kalta", "Uzun")),
            ),
            "DRESSES" to listOf(
                AttributeSpec("sizes", "Mavjud razmerlar", AttributeKind.MULTI_SELECT, options = listOf("XS", "S", "M", "L", "XL", "XXL"), required = true),
                AttributeSpec("length", "Uzunligi", AttributeKind.SELECT, options = listOf("Mini", "Midi", "Maksi")),
                AttributeSpec("occasion", "Uslub", AttributeKind.SELECT, options = listOf("Kundalik", "Ofis", "Kechki", "Milliy")),
            ),
            "BLOUSES" to listOf(
                AttributeSpec("sizes", "Mavjud razmerlar", AttributeKind.MULTI_SELECT, options = listOf("XS", "S", "M", "L", "XL", "XXL"), required = true),
                AttributeSpec("sleeve", "Yeng", AttributeKind.MULTI_SELECT, options = listOf("Kalta", "Uzun", "Yengsiz")),
            ),
            "PANTS" to listOf(
                AttributeSpec("waistSizes", "Mavjud bel o'lchamlari", AttributeKind.MULTI_SELECT, options = listOf("28", "30", "32", "34", "36", "38", "40"), required = true),
                AttributeSpec("cut", "Bichimi", AttributeKind.SELECT, options = listOf("Slim", "Straight", "Wide", "Skinny")),
                AttributeSpec("length", "Uzunligi", AttributeKind.NUMBER, hint = "32", suffix = "inch"),
            ),
            "SKIRTS" to listOf(
                AttributeSpec("sizes", "Mavjud razmerlar", AttributeKind.MULTI_SELECT, options = listOf("XS", "S", "M", "L", "XL", "XXL"), required = true),
                AttributeSpec("length", "Uzunligi", AttributeKind.SELECT, options = listOf("Mini", "Midi", "Maksi")),
            ),
            "SUITS" to listOf(
                AttributeSpec("sizes", "Mavjud razmerlar", AttributeKind.MULTI_SELECT, options = listOf("XS", "S", "M", "L", "XL", "XXL"), required = true),
                AttributeSpec("pieces", "Nechta qism", AttributeKind.SELECT, options = listOf("2 (pidjak+shim)", "3 (+jilet)")),
                AttributeSpec("occasion", "Uslub", AttributeKind.SELECT, options = listOf("Klassik", "Biznes", "Kechki")),
            ),
            "OUTERWEAR" to listOf(
                AttributeSpec("sizes", "Mavjud razmerlar", AttributeKind.MULTI_SELECT, options = listOf("XS", "S", "M", "L", "XL", "XXL"), required = true),
                AttributeSpec("insulation", "Issiqlik", AttributeKind.SELECT, options = listOf("Yengil", "O'rtacha", "Qishki")),
                AttributeSpec("isWaterproof", "Suv o'tkazmaydi", AttributeKind.BOOLEAN),
            ),
            "SHOES" to listOf(
                AttributeSpec("shoeSizes", "Mavjud razmerlar", AttributeKind.MULTI_SELECT, options = listOf("36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46"), required = true),
                AttributeSpec("soleType", "Taglik", AttributeKind.SELECT, options = listOf("Rezina", "Poliuretan", "Charm")),
                AttributeSpec("heelCm", "Poshna", AttributeKind.NUMBER, hint = "5", suffix = "sm"),
            ),
            "SPORTSWEAR" to listOf(
                AttributeSpec("sizes", "Mavjud razmerlar", AttributeKind.MULTI_SELECT, options = listOf("XS", "S", "M", "L", "XL", "XXL"), required = true),
                AttributeSpec("sport", "Sport turi", AttributeKind.SELECT, options = listOf("Fitnes", "Yugurish", "Futbol", "Yoga", "Basketbol")),
            ),
            "BAGS" to listOf(
                AttributeSpec("bagType", "Turi", AttributeKind.SELECT, options = listOf("Yelka", "Qo'l", "Ryukzak", "Klatch"), required = true),
                AttributeSpec("capacityL", "Hajmi", AttributeKind.NUMBER, hint = "20", suffix = "litr"),
            ),
            "MEN" to listOf(
                AttributeSpec("sizes", "Mavjud razmerlar", AttributeKind.MULTI_SELECT, options = listOf("XS", "S", "M", "L", "XL", "XXL"), required = true),
            ),
            "WOMEN" to listOf(
                AttributeSpec("sizes", "Mavjud razmerlar", AttributeKind.MULTI_SELECT, options = listOf("XS", "S", "M", "L", "XL", "XXL"), required = true),
            ),
            "ACCESSORIES" to listOf(
                AttributeSpec("accessoryType", "Turi", AttributeKind.SELECT, options = listOf("Kamar", "Sharf", "Qo'lqop", "Bosh kiyim", "Soat", "Zargarlik"), required = true),
            ),
        ),
        BusinessType.CAFE_RESTAURANT to mapOf(
            "PIZZA" to listOf(
                AttributeSpec("size", "O'lcham", AttributeKind.SELECT, options = listOf("25 sm", "30 sm", "35 sm", "40 sm"), required = true),
                AttributeSpec("dough", "Xamir", AttributeKind.SELECT, options = listOf("Yupqa", "Qalin", "To'ldirilgan chekka")),
                AttributeSpec("slices", "Bo'laklar", AttributeKind.NUMBER, hint = "8", suffix = "ta"),
            ),
            "BURGER" to listOf(
                AttributeSpec("pattyType", "Kotlet", AttributeKind.SELECT, options = listOf("Mol", "Tovuq", "Baliq", "Vegetarian"), required = true),
                AttributeSpec("pattyCount", "Kotlet soni", AttributeKind.SELECT, options = listOf("1", "2", "3")),
                AttributeSpec("hasFries", "Kartoshka bilan", AttributeKind.BOOLEAN),
            ),
            "LAVASH_SHAWARMA" to listOf(
                AttributeSpec("meatType", "Go'sht", AttributeKind.SELECT, options = listOf("Tovuq", "Mol", "Aralash"), required = true),
                AttributeSpec("size", "O'lcham", AttributeKind.SELECT, options = listOf("Standart", "Katta", "Mini")),
            ),
            "SUSHI" to listOf(
                AttributeSpec("piecesCount", "Bo'laklar", AttributeKind.NUMBER, hint = "8", suffix = "ta"),
                AttributeSpec("fishType", "Baliq", AttributeKind.SELECT, options = listOf("Losos", "Tunes", "Ilon baliq", "Krevetka", "Vegetarian"), required = true),
                AttributeSpec("isRaw", "Xom (raw)", AttributeKind.BOOLEAN),
            ),
            "NATIONAL" to listOf(
                AttributeSpec("dishType", "Taom", AttributeKind.SELECT, options = listOf("Osh", "Manti", "Somsa", "Lag'mon", "Shashlik", "Norin", "Sho'rva"), required = true),
            ),
            "SOUPS" to listOf(
                AttributeSpec("volumeMl", "Hajm", AttributeKind.NUMBER, hint = "400", suffix = "ml"),
                AttributeSpec("hasBread", "Non bilan", AttributeKind.BOOLEAN),
            ),
            "SALADS" to listOf(
                AttributeSpec("saladType", "Turi", AttributeKind.SELECT, options = listOf("Sabzavotli", "Go'shtli", "Sezar", "Olivye", "Yunon")),
            ),
            "GRILL_BBQ" to listOf(
                AttributeSpec("meatType", "Go'sht", AttributeKind.SELECT, options = listOf("Mol", "Qo'y", "Tovuq", "Jigar", "Baliq"), required = true),
                AttributeSpec("skewers", "Nechta sixda", AttributeKind.NUMBER, hint = "2", suffix = "ta"),
            ),
            "BREAKFAST" to listOf(
                AttributeSpec("servedUntil", "Nechagacha", AttributeKind.TEXT, hint = "11:00"),
                AttributeSpec("includes", "Tarkibida", AttributeKind.TAGS, hint = "Tuxum, Non, Choy"),
            ),
            "DESSERTS" to listOf(
                AttributeSpec("dessertType", "Turi", AttributeKind.SELECT, options = listOf("Tort", "Pirojnoe", "Muzqaymoq", "Chizkeyk", "Vafli")),
                AttributeSpec("sugarFree", "Shakarsiz", AttributeKind.BOOLEAN),
            ),
            "HOT_DRINKS" to listOf(
                AttributeSpec("volumeMl", "Hajm", AttributeKind.SELECT, options = listOf("200 ml", "300 ml", "400 ml"), required = true),
                AttributeSpec("milkType", "Sut", AttributeKind.SELECT, options = listOf("Oddiy", "Bodom", "Soya", "Sutsiz")),
            ),
            "COLD_DRINKS" to listOf(
                AttributeSpec("volumeMl", "Hajm", AttributeKind.SELECT, options = listOf("300 ml", "400 ml", "500 ml", "1 L"), required = true),
                AttributeSpec("hasIce", "Muz bilan", AttributeKind.BOOLEAN),
            ),
            "FAST_FOOD" to listOf(
                AttributeSpec("comboIncludes", "Tarkibida", AttributeKind.TAGS, hint = "Burger, Kartoshka, Ichimlik"),
            ),
            "COMBO_SETS" to listOf(
                AttributeSpec("comboIncludes", "Setда nima bor", AttributeKind.TAGS, hint = "2 pitsa, 2 ichimlik", required = true),
                AttributeSpec("personCount", "Nechta kishiga", AttributeKind.NUMBER, hint = "2", required = true, suffix = "kishi"),
            ),
        ),
        BusinessType.EDUCATION_CENTER to mapOf(
            "FOREIGN_LANGUAGES" to listOf(
                AttributeSpec("language", "Til", AttributeKind.SELECT, options = listOf("Ingliz", "Rus", "Koreys", "Nemis", "Fransuz", "Arab", "Xitoy", "Turk"), required = true),
                AttributeSpec("hasNativeSpeaker", "Native speaker bor", AttributeKind.BOOLEAN),
            ),
            "IELTS_CEFR" to listOf(
                AttributeSpec("examType", "Imtihon", AttributeKind.SELECT, options = listOf("IELTS", "CEFR", "TOEFL", "SAT"), required = true),
                AttributeSpec("targetScore", "Maqsad ball", AttributeKind.TEXT, hint = "6.5+"),
                AttributeSpec("hasMockExam", "Mock imtihon bor", AttributeKind.BOOLEAN),
            ),
            "IT_PROGRAMMING" to listOf(
                AttributeSpec("stack", "Texnologiyalar", AttributeKind.TAGS, hint = "Python, Django, PostgreSQL", required = true),
                AttributeSpec("hasProject", "Real loyiha bor", AttributeKind.BOOLEAN),
                AttributeSpec("hasJobHelp", "Ishga joylashishда yordam", AttributeKind.BOOLEAN),
            ),
            "DESIGN" to listOf(
                AttributeSpec("tools", "Dasturlar", AttributeKind.TAGS, hint = "Figma, Photoshop", required = true),
                AttributeSpec("hasPortfolio", "Portfolio yig'iladi", AttributeKind.BOOLEAN),
            ),
            "MATH_SCIENCE" to listOf(
                AttributeSpec("subjectName", "Fan", AttributeKind.SELECT, options = listOf("Matematika", "Fizika", "Kimyo", "Biologiya"), required = true),
                AttributeSpec("gradeLevel", "Sinf", AttributeKind.TEXT, hint = "9-11"),
            ),
            "UNIVERSITY_PREP" to listOf(
                AttributeSpec("targetUniversity", "Qaysi OTMga", AttributeKind.TEXT, hint = "TATU"),
                AttributeSpec("subjects", "Fanlar", AttributeKind.TAGS, hint = "Matematika, Fizika", required = true),
            ),
            "BUSINESS_MARKETING" to listOf(
                AttributeSpec("topic", "Mavzu", AttributeKind.SELECT, options = listOf("SMM", "Targeting", "Sotuv", "Moliya", "Startap"), required = true),
            ),
            "MASTER_CLASS" to listOf(
                AttributeSpec("speaker", "Spiker", AttributeKind.TEXT, hint = "Aziz Karimov"),
                AttributeSpec("eventDate", "Sana", AttributeKind.TEXT, hint = "2026-08-01"),
                AttributeSpec("hours", "Davomiyligi", AttributeKind.NUMBER, hint = "4", suffix = "soat"),
            ),
        ),
        BusinessType.ENTERTAINMENT to mapOf(
            "CINEMA" to listOf(
                AttributeSpec("hallType", "Zal", AttributeKind.SELECT, options = listOf("Standart", "VIP", "Lounge", "IMAX")),
                AttributeSpec("seatType", "Joy", AttributeKind.SELECT, options = listOf("Oddiy", "Divan", "Recliner")),
            ),
            "THEATER_CONCERT" to listOf(
                AttributeSpec("artist", "Ijrochi / truppa", AttributeKind.TEXT, hint = "Ozodbek Nazarbekov", required = true),
                AttributeSpec("hasIntermission", "Tanaffus bor", AttributeKind.BOOLEAN),
            ),
            "ESCAPE_ROOM" to listOf(
                AttributeSpec("players", "Nechta o'yinchi", AttributeKind.NUMBER, hint = "4", required = true, suffix = "kishi"),
                AttributeSpec("difficulty", "Qiyinlik", AttributeKind.SELECT, options = listOf("Oson", "O'rtacha", "Qiyin"), required = true),
                AttributeSpec("theme", "Mavzu", AttributeKind.TEXT, hint = "Qo'rqinchli"),
                AttributeSpec("hasActor", "Aktyor bor", AttributeKind.BOOLEAN),
            ),
            "TRAMPOLINE_PARK" to listOf(
                AttributeSpec("minutes", "Vaqt", AttributeKind.NUMBER, hint = "60", required = true, suffix = "daqiqa"),
                AttributeSpec("ageGroup", "Yosh guruhi", AttributeKind.SELECT, options = listOf("Bolalar", "Kattalar", "Barchasi")),
                AttributeSpec("socksIncluded", "Paypoq beriladi", AttributeKind.BOOLEAN),
            ),
            "BOWLING" to listOf(
                AttributeSpec("lanes", "Yo'laklar", AttributeKind.NUMBER, hint = "1", suffix = "ta"),
                AttributeSpec("players", "Nechta o'yinchi", AttributeKind.NUMBER, hint = "6", suffix = "kishi"),
                AttributeSpec("shoesIncluded", "Poyabzal beriladi", AttributeKind.BOOLEAN),
            ),
            "AQUAPARK" to listOf(
                AttributeSpec("zones", "Zonalar", AttributeKind.TAGS, hint = "Katta gorka, Bolalar zonasi, Jakuzi"),
                AttributeSpec("hasLocker", "Shkaf beriladi", AttributeKind.BOOLEAN),
                AttributeSpec("allDay", "Kun bo'yi", AttributeKind.BOOLEAN),
            ),
            "AMUSEMENT_PARK" to listOf(
                AttributeSpec("rideCount", "Attraksionlar", AttributeKind.NUMBER, hint = "10", suffix = "ta"),
                AttributeSpec("isUnlimited", "Cheksiz", AttributeKind.BOOLEAN),
            ),
            "MUSEUM_EXPO" to listOf(
                AttributeSpec("expoTitle", "Ko'rgazma", AttributeKind.TEXT, hint = "Zamonaviy san'at"),
                AttributeSpec("hasGuide", "Gid bor", AttributeKind.BOOLEAN),
            ),
            "KARAOKE" to listOf(
                AttributeSpec("roomSize", "Xona", AttributeKind.SELECT, options = listOf("Kichik (4)", "O'rta (8)", "Katta (15+)"), required = true),
                AttributeSpec("songLanguages", "Qo'shiq tillari", AttributeKind.TAGS, hint = "O'zbek, Rus, Ingliz"),
            ),
        ),
        BusinessType.BARBERSHOP to mapOf(
            "HAIRCUT_MEN" to listOf(
                AttributeSpec("style", "Uslub", AttributeKind.SELECT, options = listOf("Klassik", "Fade", "Undercut", "Mashinka", "Bolalar")),
                AttributeSpec("beardIncluded", "Soqol bilan", AttributeKind.BOOLEAN),
            ),
            "HAIRCUT_WOMEN" to listOf(
                AttributeSpec("hairLength", "Soch uzunligi", AttributeKind.SELECT, options = listOf("Kalta", "O'rta", "Uzun"), required = true),
                AttributeSpec("style", "Uslub", AttributeKind.SELECT, options = listOf("To'g'ri", "Kaskad", "Kare", "Chelka")),
            ),
            "KIDS" to listOf(
                AttributeSpec("ageGroup", "Yosh", AttributeKind.SELECT, options = listOf("0-3", "4-7", "8-12"), required = true),
                AttributeSpec("hasCartoon", "Multfilm bor", AttributeKind.BOOLEAN),
            ),
            "BEARD" to listOf(
                AttributeSpec("beardService", "Xizmat", AttributeKind.SELECT, options = listOf("Qirqish", "Ustara", "Shakl berish", "Bo'yash"), required = true),
            ),
            "HAIR_COLOR" to listOf(
                AttributeSpec("hairLength", "Soch uzunligi", AttributeKind.SELECT, options = listOf("Kalta", "O'rta", "Uzun"), required = true),
                AttributeSpec("colorBrand", "Bo'yoq brendi", AttributeKind.TEXT, hint = "L'Oréal"),
                AttributeSpec("technique", "Texnika", AttributeKind.SELECT, options = listOf("To'liq", "Ombre", "Balayaj", "Melirovka", "Ildiz")),
            ),
            "STYLING" to listOf(
                AttributeSpec("occasion", "Tadbir", AttributeKind.SELECT, options = listOf("Kundalik", "To'y", "Bayram", "Fotosessiya")),
                AttributeSpec("hairLength", "Soch uzunligi", AttributeKind.SELECT, options = listOf("Kalta", "O'rta", "Uzun")),
            ),
            "HAIR_CARE" to listOf(
                AttributeSpec("procedure", "Muolaja", AttributeKind.SELECT, options = listOf("Keratin", "Botoks", "Maska", "Peeling"), required = true),
                AttributeSpec("courseSessions", "Kurs", AttributeKind.NUMBER, hint = "1", suffix = "seans"),
            ),
            "MANICURE" to listOf(
                AttributeSpec("coating", "Qoplama", AttributeKind.SELECT, options = listOf("Oddiy", "Gel-lak", "Kengaytma"), required = true),
                AttributeSpec("withPedicure", "Pedikyur bilan", AttributeKind.BOOLEAN),
            ),
        ),
        BusinessType.BEAUTY_SALON to mapOf(
            "HAIR" to listOf(
                AttributeSpec("hairLength", "Soch uzunligi", AttributeKind.SELECT, options = listOf("Kalta", "O'rta", "Uzun"), required = true),
                AttributeSpec("service", "Xizmat", AttributeKind.SELECT, options = listOf("Turmak", "Bo'yash", "Kesish", "Keratin", "Ombre"), required = true),
                AttributeSpec("colorBrand", "Bo'yoq brendi", AttributeKind.TEXT, hint = "Estel"),
            ),
            "MAKEUP" to listOf(
                AttributeSpec("occasion", "Tadbir", AttributeKind.SELECT, options = listOf("Kundalik", "Kechki", "To'y", "Fotosessiya"), required = true),
                AttributeSpec("cosmeticsBrand", "Kosmetika", AttributeKind.TEXT, hint = "MAC"),
                AttributeSpec("lashesIncluded", "Kiprik bilan", AttributeKind.BOOLEAN),
            ),
            "MANICURE" to listOf(
                AttributeSpec("coating", "Qoplama", AttributeKind.SELECT, options = listOf("Oddiy lak", "Gel-lak", "Kengaytma (gel)", "Akril"), required = true),
                AttributeSpec("hasDesign", "Dizayn bor", AttributeKind.BOOLEAN),
                AttributeSpec("removalIncluded", "Eskisini olish kiradi", AttributeKind.BOOLEAN),
            ),
            "PEDICURE" to listOf(
                AttributeSpec("coating", "Qoplama", AttributeKind.SELECT, options = listOf("Oddiy lak", "Gel-lak"), required = true),
                AttributeSpec("pedicureType", "Turi", AttributeKind.SELECT, options = listOf("Klassik", "Apparat", "SPA"), required = true),
            ),
            "EYEBROWS_LASHES" to listOf(
                AttributeSpec("procedure", "Muolaja", AttributeKind.SELECT, options = listOf("Qosh shakl", "Qosh bo'yash", "Laminatsiya", "Kiprik yopishtirish", "Kiprik laminatsiya"), required = true),
                AttributeSpec("effect", "Effekt", AttributeKind.SELECT, options = listOf("Klassik", "2D", "3D", "Volume")),
            ),
            "COSMETOLOGY" to listOf(
                AttributeSpec("procedure", "Muolaja", AttributeKind.SELECT, options = listOf("Tozalash", "Peeling", "Mezoterapiya", "Biorevitalizatsiya", "Maska"), required = true),
                AttributeSpec("isDeviceBased", "Apparatli", AttributeKind.BOOLEAN),
                AttributeSpec("courseSessions", "Kurs", AttributeKind.NUMBER, hint = "5", suffix = "seans"),
            ),
            "SPA_MASSAGE" to listOf(
                AttributeSpec("massageType", "Massaj turi", AttributeKind.SELECT, options = listOf("Klassik", "Relaks", "Anticellulit", "Tosh bilan", "Tay"), required = true),
                AttributeSpec("zones", "Zonalar", AttributeKind.TAGS, hint = "Yelka, Bel, Oyoq"),
                AttributeSpec("forCouples", "Juftlik uchun", AttributeKind.BOOLEAN),
            ),
            "EPILATION" to listOf(
                AttributeSpec("method", "Usul", AttributeKind.SELECT, options = listOf("Shakar (shugaring)", "Vosk", "Lazer", "Elektro"), required = true),
                AttributeSpec("zones", "Zonalar", AttributeKind.TAGS, hint = "Oyoq, Qo'l, Qo'ltiq", required = true),
                AttributeSpec("courseSessions", "Kurs", AttributeKind.NUMBER, hint = "6", suffix = "seans"),
            ),
        ),
    )

    /** Turga mos narx birliklari (birinchisi — odatiy). */
    fun priceUnits(type: BusinessType): List<PriceUnit> = when (type) {
        BusinessType.GAME_CLUB -> listOf(PriceUnit.PER_HOUR, PriceUnit.PER_SESSION, PriceUnit.PER_PERSON)
        BusinessType.CLOTHING -> listOf(PriceUnit.PER_ITEM)
        BusinessType.CAFE_RESTAURANT -> listOf(PriceUnit.PER_ITEM, PriceUnit.PER_KG)
        BusinessType.EDUCATION_CENTER -> listOf(PriceUnit.PER_MONTH, PriceUnit.PER_COURSE, PriceUnit.PER_LESSON)
        BusinessType.ENTERTAINMENT -> listOf(PriceUnit.PER_TICKET, PriceUnit.PER_PERSON, PriceUnit.PER_SESSION)
        BusinessType.BARBERSHOP -> listOf(PriceUnit.PER_ITEM, PriceUnit.PER_SESSION, PriceUnit.PER_PERSON)
        BusinessType.BEAUTY_SALON -> listOf(PriceUnit.PER_ITEM, PriceUnit.PER_SESSION, PriceUnit.PER_PERSON)
    }

    /** Turga mos qo'shimchalar guruhi uchun taklif ("Hajmni tanlang" kabi). */
    fun optionGroupHint(type: BusinessType): String = when (type) {
        BusinessType.GAME_CLUB -> "Zal turi, qo'shimcha joystik"
        BusinessType.CLOTHING -> "O'lcham, rang"
        BusinessType.CAFE_RESTAURANT -> "Hajm, qo'shimchalar"
        BusinessType.EDUCATION_CENTER -> "Guruh vaqti, format"
        BusinessType.ENTERTAINMENT -> "Seans vaqti, joy turi"
        BusinessType.BARBERSHOP -> "Usta darajasi, qo'shimcha xizmat"
        BusinessType.BEAUTY_SALON -> "Usta, qo'shimcha xizmat"
    }

    private fun cats(type: BusinessType, vararg pairs: Pair<String, String>): List<ListingCategory> =
        listOf(ListingCategory(ALL_KEY, allLabel(type))) +
            pairs.map { (key, label) -> ListingCategory(key, label) } +
            ListingCategory(OTHER_KEY, "Boshqa")

    private val categoryMap: Map<BusinessType, List<ListingCategory>> = mapOf(
        BusinessType.GAME_CLUB to cats(
            BusinessType.GAME_CLUB,
            "PLAYSTATION" to "PlayStation",
            "TABLE_TENNIS" to "Stol tennis",
            "TENNIS" to "Katta tennis",
            "PC_GAMING" to "Kompyuter o'yinlari",
            "BILLIARDS" to "Billiard",
            "POLYA" to "Polya",
        ),
        BusinessType.CLOTHING to cats(
            BusinessType.CLOTHING,
            "MEN" to "Erkaklar",
            "WOMEN" to "Ayollar",
            "OUTERWEAR" to "Ustki kiyim",
            "SHOES" to "Poyabzal",
            "SPORTSWEAR" to "Sport kiyim",
            "BAGS" to "Sumkalar",
            "ACCESSORIES" to "Aksessuarlar",
        ),
        BusinessType.CAFE_RESTAURANT to cats(
            BusinessType.CAFE_RESTAURANT,
            "NATIONAL" to "Milliy taomlar",
            "PIZZA" to "Pitsa",
            "BURGER" to "Burger",
            "LAVASH_SHAWARMA" to "Lavash / Shaurma",
            "SUSHI" to "Sushi",
            "FAST_FOOD" to "Fast food",
            "SOUPS" to "Sho'rvalar",
            "SALADS" to "Salatlar",
            "GRILL_BBQ" to "Kabob va grill",
            "BREAKFAST" to "Nonushta",
            "DESSERTS" to "Shirinliklar",
            "HOT_DRINKS" to "Choy va kofe",
            "COLD_DRINKS" to "Sovuq ichimliklar",
            "COMBO_SETS" to "Setlar (combo)",
        ),
        BusinessType.EDUCATION_CENTER to cats(
            BusinessType.EDUCATION_CENTER,
            "FOREIGN_LANGUAGES" to "Chet tillari",
            "IELTS_CEFR" to "IELTS / CEFR",
            "IT_PROGRAMMING" to "IT va dasturlash",
            "DESIGN" to "Dizayn",
            "MATH_SCIENCE" to "Matematika va fanlar",
            "UNIVERSITY_PREP" to "Abituriyent tayyorlash",
            "BUSINESS_MARKETING" to "Biznes va marketing",
            "MASTER_CLASS" to "Master-klass",
        ),
        BusinessType.ENTERTAINMENT to cats(
            BusinessType.ENTERTAINMENT,
            "CINEMA" to "Kino seans",
            "THEATER_CONCERT" to "Teatr va konsert",
            "ESCAPE_ROOM" to "Kvest (escape room)",
            "TRAMPOLINE_PARK" to "Batut park",
            "BOWLING" to "Bouling",
            "AQUAPARK" to "Akvapark",
            "AMUSEMENT_PARK" to "Attraksionlar",
            "MUSEUM_EXPO" to "Muzey va ko'rgazma",
            "KARAOKE" to "Karaoke",
        ),
        BusinessType.BARBERSHOP to cats(
            BusinessType.BARBERSHOP,
            "HAIRCUT_MEN" to "Erkaklar soch olish",
            "HAIRCUT_WOMEN" to "Ayollar soch olish",
            "KIDS" to "Bolalar soch olish",
            "BEARD" to "Soqol / ustara",
            "HAIR_COLOR" to "Soch bo'yash",
            "STYLING" to "Ukladka / styling",
            "HAIR_CARE" to "Parvarish (spa)",
            "MANICURE" to "Manikyur-pedikyur",
        ),
        BusinessType.BEAUTY_SALON to cats(
            BusinessType.BEAUTY_SALON,
            "HAIR" to "Soch turmagi / bo'yash",
            "MAKEUP" to "Makiyaj",
            "MANICURE" to "Manikyur",
            "PEDICURE" to "Pedikyur",
            "EYEBROWS_LASHES" to "Qosh / kiprik",
            "COSMETOLOGY" to "Kosmetologiya",
            "SPA_MASSAGE" to "SPA / massaj",
            "EPILATION" to "Epilyatsiya",
        ),
    )

    /**
     * Turga xos maydonlar — **faqat eng zaruriylari**. Ilgari 8-9 tadan edi va forma
     * cho'zilib ketardi; talabaga foydasi bo'lmagan maydonlar olib tashlandi.
     * Kalitlar backend spec'i bilan bir xil (`DISCOUNTS_BUSINESS_API.md` §4).
     */
    private val attributeMap: Map<BusinessType, List<AttributeSpec>> = mapOf(
        BusinessType.GAME_CLUB to listOf(
            AttributeSpec("hallType", "Zal turi", AttributeKind.SELECT, options = listOf("Standart", "VIP", "Alohida xona")),
            AttributeSpec("deviceModel", "Qurilma", AttributeKind.TEXT, hint = "PlayStation 5 Slim"),
            AttributeSpec("seatsCount", "Nechta o'yinchi", AttributeKind.NUMBER, hint = "4", suffix = "kishi"),
            AttributeSpec("sessionMinutes", "Sessiya davomiyligi", AttributeKind.NUMBER, hint = "60", suffix = "daqiqa"),
            AttributeSpec("gamesList", "O'yinlar", AttributeKind.TAGS, hint = "FIFA 25, Mortal Kombat"),
        ),
        BusinessType.CLOTHING to listOf(
            AttributeSpec("brand", "Brand nomi", AttributeKind.TEXT, hint = "Zara"),
            AttributeSpec("gender", "Kimlar uchun", AttributeKind.SELECT, options = listOf("Erkaklar", "Ayollar", "Uniseks", "Bolalar")),
            AttributeSpec("material", "Material", AttributeKind.TEXT, hint = "100% paxta"),
            AttributeSpec("season", "Mavsumlar", AttributeKind.MULTI_SELECT, options = listOf("Qish", "Bahor", "Yoz", "Kuz")),
        ),
        BusinessType.CAFE_RESTAURANT to listOf(
            AttributeSpec("portionGrams", "Porsiya", AttributeKind.NUMBER, hint = "550", suffix = "gramm"),
            AttributeSpec("ingredients", "Tarkibi", AttributeKind.TAGS, hint = "Mozzarella, Pepperoni, Tomat sousi"),
            AttributeSpec("spicyLevel", "O'tkirlik", AttributeKind.SELECT, options = listOf("Yo'q", "Yengil", "O'rtacha", "O'tkir")),
            AttributeSpec("isHalal", "Halol", AttributeKind.BOOLEAN),
            AttributeSpec("hasDelivery", "Yetkazib berish bor", AttributeKind.BOOLEAN),
        ),
        BusinessType.EDUCATION_CENTER to listOf(
            AttributeSpec("subject", "Yo'nalish", AttributeKind.TEXT, hint = "Ingliz tili — IELTS 6.5+"),
            AttributeSpec("level", "Daraja", AttributeKind.SELECT, options = listOf("Boshlang'ich", "O'rta", "Yuqori")),
            AttributeSpec("format", "Format", AttributeKind.SELECT, options = listOf("Offline", "Online", "Aralash")),
            AttributeSpec("durationMonths", "Davomiyligi", AttributeKind.NUMBER, hint = "3", suffix = "oy"),
            AttributeSpec("lessonsPerWeek", "Haftada", AttributeKind.NUMBER, hint = "3", suffix = "marta"),
            AttributeSpec("hasFreeTrialLesson", "Birinchi dars bepul", AttributeKind.BOOLEAN),
        ),
        BusinessType.ENTERTAINMENT to listOf(
            AttributeSpec("eventTitle", "Film / tadbir nomi", AttributeKind.TEXT, hint = "Dune: Part Three"),
            AttributeSpec("format", "Format", AttributeKind.SELECT, options = listOf("2D", "3D", "IMAX", "4DX", "VR")),
            AttributeSpec("language", "Til", AttributeKind.SELECT, options = listOf("O'zbek", "Rus", "Ingliz", "Original (subtitr)")),
            AttributeSpec("ageLimit", "Yosh chegarasi", AttributeKind.SELECT, options = listOf("0+", "6+", "12+", "16+", "18+")),
            AttributeSpec("sessionTimes", "Seans vaqtlari", AttributeKind.TAGS, hint = "12:30, 16:00, 19:40"),
        ),
        BusinessType.BARBERSHOP to listOf(
            AttributeSpec("master", "Usta", AttributeKind.TEXT, hint = "Aziz aka"),
            AttributeSpec("masterLevel", "Usta darajasi", AttributeKind.SELECT, options = listOf("Junior", "Usta", "Top-usta")),
            AttributeSpec("gender", "Kimlar uchun", AttributeKind.SELECT, options = listOf("Erkaklar", "Ayollar", "Bolalar", "Barcha")),
            AttributeSpec("durationMinutes", "Davomiyligi", AttributeKind.NUMBER, hint = "40", suffix = "daqiqa"),
            AttributeSpec("byAppointment", "Oldindan yozilish", AttributeKind.BOOLEAN),
        ),
        BusinessType.BEAUTY_SALON to listOf(
            AttributeSpec("master", "Usta", AttributeKind.TEXT, hint = "Malika opa"),
            AttributeSpec("masterLevel", "Usta darajasi", AttributeKind.SELECT, options = listOf("Junior", "Usta", "Top-usta")),
            AttributeSpec("durationMinutes", "Davomiyligi", AttributeKind.NUMBER, hint = "60", suffix = "daqiqa"),
            AttributeSpec("byAppointment", "Oldindan yozilish", AttributeKind.BOOLEAN),
        ),
    )
}
