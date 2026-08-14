package dev.core.uikit.component

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/**
 * Ilova ikonalari — dizayndagi aynan SVG `d` yo'llari [PathParser] orqali
 * [ImageVector] ga aylantirilgan. Chiziqli (stroke) ikonalar `Icon(tint=...)` bilan
 * bo'yaladi; ko'p rangli logolar `Image` bilan chiziladi.
 *
 * Manba: `design_handoff_studentclub_elonuz/icons/ic_*.svg` (36 ta) va Lucide.
 * Handoff SVG'laridagi `<circle>`/`<rect>` shakllari shu yerda qo'lda `d` yo'liga
 * aylantirilgan, chunki [strokeIcon] faqat path qabul qiladi.
 *
 * MUHIM: ilovada emoji ISHLATILMAYDI — iOS'da Compose emoji chiza olmaydi va har bir
 * emoji `?` kvadratchaga aylanadi. Har qanday belgi shu yerdagi ikonka bilan beriladi.
 */
private fun strokeIcon(name: String, vararg paths: String, viewport: Float = 24f): ImageVector =
    ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = viewport,
        viewportHeight = viewport,
    ).apply {
        paths.forEach { d ->
            addPath(
                pathData = PathParser().parsePathString(d).toNodes(),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
        }
    }.build()

object AppIcons {
    /** `ic_university` — universitet/bitiruv qalpog'i (bottom nav "Universitet"). */
    val GraduationCap = strokeIcon(
        "GraduationCap",
        "M 12 4 L 2 9 l 10 5 l 8 -4",
        "M 6 12 v 4 c 0 1 2.7 3 6 3 s 6 -2 6 -3 v -4",
    )

    /** `ic_call` — telefon go'shagi. */
    val Phone = strokeIcon(
        "Phone",
        "M 6.5 4 h 3 l 1.5 4 l -2 1.5 a 11 11 0 0 0 5.5 5.5 l 1.5 -2 l 4 1.5 v 3 a 2 2 0 0 1 -2.2 2 A 16 16 0 0 1 4.5 6.2 A 2 2 0 0 1 6.5 4 z",
    )
    val Mail = strokeIcon(
        "Mail",
        "M 2 6 a 2 2 0 0 1 2 -2 h 16 a 2 2 0 0 1 2 2 v 12 a 2 2 0 0 1 -2 2 H 4 a 2 2 0 0 1 -2 -2 z",
        "m 22 7 l -10 5 L 2 7",
    )
    val Lock = strokeIcon(
        "Lock",
        "M 5 11 h 14 a 2 2 0 0 1 2 2 v 7 a 2 2 0 0 1 -2 2 H 5 a 2 2 0 0 1 -2 -2 v -7 a 2 2 0 0 1 2 -2 z",
        "M 7 11 V 7 a 5 5 0 0 1 10 0 v 4",
    )
    val Eye = strokeIcon(
        "Eye",
        "M 2 12 s 3 -7 10 -7 s 10 7 10 7 s -3 7 -10 7 s -10 -7 -10 -7 Z",
        "M 15 12 a 3 3 0 1 1 -6 0 a 3 3 0 0 1 6 0 z",
    )
    val EyeOff = strokeIcon(
        "EyeOff",
        "M 9.88 9.88 a 3 3 0 0 0 4.24 4.24",
        "M 10.73 5.08 A 10.43 10.43 0 0 1 12 5 c 7 0 10 7 10 7 a 13.16 13.16 0 0 1 -1.67 2.68",
        "M 6.61 6.61 A 13.526 13.526 0 0 0 2 12 s 3 7 10 7 a 9.74 9.74 0 0 0 5.39 -1.61",
        "M 2 2 l 20 20",
    )
    val ArrowRight = strokeIcon("ArrowRight", "M 5 12 h 14", "m 12 5 l 7 7 l -7 7")

    /** `ic_back` — orqaga qaytish. Handoff qoidasi: orqaga **doim `<`**, hech qachon `✕`. */
    val ArrowLeft = strokeIcon("ArrowLeft", "M 15 5 l -7 7 l 7 7")
    val Check = strokeIcon("Check", "M 20 6 L 9 17 l -5 -5")
    val Clock = strokeIcon(
        "Clock",
        "M 12 2 a 10 10 0 1 0 0 20 a 10 10 0 0 0 0 -20 z",
        "M 12 6 v 6 l 4 2",
    )
    val Calendar = strokeIcon(
        "Calendar",
        "M 5 4 h 14 a 2 2 0 0 1 2 2 v 14 a 2 2 0 0 1 -2 2 H 5 a 2 2 0 0 1 -2 -2 V 6 a 2 2 0 0 1 2 -2 z",
        "M 16 2 v 4",
        "M 8 2 v 4",
        "M 3 10 h 18",
    )

    /** `ic_search` — qidiruv (doira `<circle>` dan arc yo'liga aylantirilgan). */
    val Search = strokeIcon(
        "Search",
        "M 4 11 a 7 7 0 1 0 14 0 a 7 7 0 1 0 -14 0",
        "M 20 20 l -3.5 -3.5",
    )
    val ShieldCheck = strokeIcon(
        "ShieldCheck",
        "M 12 22 s 8 -4 8 -10 V 5 l -8 -3 l -8 3 v 7 c 0 6 8 10 8 10 Z",
        "m 9 12 l 2 2 l 4 -4",
    )
    val ChevronDown = strokeIcon("ChevronDown", "m 6 9 l 6 6 l 6 -6")

    /** `ic_close` — faqat modal/dialog/to'liq ekranli qidiruvni yopish uchun. */
    val Close = strokeIcon("Close", "M 6 6 l 12 12", "M 18 6 L 6 18")

    /** `ic_messages` — to'rtburchak suhbat pufagi (bottom nav "Xabarlar"). */
    val MessageSquare = strokeIcon("MessageSquare", "M 4 5 h 16 v 11 H 8 l -4 4 V 5 z")
    val ScanFace = strokeIcon(
        "ScanFace",
        "M 4 8 V 6 a 2 2 0 0 1 2 -2 h 2",
        "M 4 16 v 2 a 2 2 0 0 0 2 2 h 2",
        "M 16 4 h 2 a 2 2 0 0 1 2 2 v 2",
        "M 16 20 h 2 a 2 2 0 0 0 2 -2 v -2",
        "M 8 14 s 1.5 2 4 2 s 4 -2 4 -2",
        "M 9 9 h 0.01",
        "M 15 9 h 0.01",
    )

    /** `ic_briefcase` — ish e'lonlari (`<rect rx=2.5>` yumaloq burchakli yo'lga aylantirilgan). */
    val Briefcase = strokeIcon(
        "Briefcase",
        "M 5.5 7 h 13 a 2.5 2.5 0 0 1 2.5 2.5 v 8 a 2.5 2.5 0 0 1 -2.5 2.5 h -13 a 2.5 2.5 0 0 1 -2.5 -2.5 v -8 a 2.5 2.5 0 0 1 2.5 -2.5 z",
        "M 8 7 V 6 a 2 2 0 0 1 2 -2 h 4 a 2 2 0 0 1 2 2 v 1",
        "M 3 12 h 18",
    )
    val Store = strokeIcon(
        "Store",
        "M 6 2 L 3 6 v 14 a 2 2 0 0 0 2 2 h 14 a 2 2 0 0 0 2 -2 V 6 l -3 -4 Z",
        "M 3 6 h 18",
        "M 16 10 a 4 4 0 0 1 -8 0",
    )
    val Building = strokeIcon(
        "Building",
        "M 6 22 V 4 a 2 2 0 0 1 2 -2 h 8 a 2 2 0 0 1 2 2 v 18",
        "M 2 22 h 20",
        "M 10 7 h 4",
        "M 10 11 h 4",
        "M 10 15 h 4",
    )

    /** `ic_home` — bosh sahifa (kontur uy). */
    val Home = strokeIcon("Home", "M 3 10 l 9 -7 l 9 7 v 9 a 1 1 0 0 1 -1 1 h -5 v -6 H 9 v 6 H 4 a 1 1 0 0 1 -1 -1 v -9 z")

    /** `ic_bell` — bildirishnomalar. */
    val Bell = strokeIcon(
        "Bell",
        "M 18 8 a 6 6 0 1 0 -12 0 c 0 7 -3 9 -3 9 h 18 s -3 -2 -3 -9",
        "M 13.7 21 a 2 2 0 0 1 -3.4 0",
    )

    /** `ic_tag` — chegirma/narx yorlig'i, kategoriya chipi. */
    val Tag = strokeIcon(
        "Tag",
        "M 3 12 l 8 -8 h 8 v 8 l -8 8 l -8 -8 z",
        "M 14.1 8.5 a 1.4 1.4 0 1 0 2.8 0 a 1.4 1.4 0 1 0 -2.8 0",
    )

    /**
     * Bitta foydalanuvchi — hisob/profil tugmasi.
     *
     * Handoff'ning "Bizneslarim" ekranidagi profil tugmasidan olingan: bosh (doira) va
     * yelka chizig'i. `Users` (ikki kishi) jamoa/a'zolar uchun, shaxsiy hisob uchun emas.
     */
    val User = strokeIcon(
        "User",
        "M 8 8 a 4 4 0 1 0 8 0 a 4 4 0 1 0 -8 0",
        "M 4 20 c 0 -4.4 3.6 -7 8 -7 s 8 2.6 8 7",
    )

    /** `ic_users` — studentlar/do'stlar, klub a'zolari. */
    val Users = strokeIcon(
        "Users",
        "M 6 8 a 3 3 0 1 0 6 0 a 3 3 0 1 0 -6 0",
        "M 3 20 c 0 -3.3 2.7 -5 6 -5 s 6 1.7 6 5",
        "M 16 6 a 3 3 0 0 1 0 5.6",
        "M 18 20 c 0 -2.6 -1.3 -4.2 -3.4 -4.8",
    )

    /** `ic_add` — markaziy FAB "Elon" va barcha "qo'shish" tugmalari. */
    val Plus = strokeIcon("Plus", "M 12 5 v 14", "M 5 12 h 14")

    /** `ic_chevron_right` — "Barchasi ›", ro'yxat qatori oxiri. */
    val ChevronRight = strokeIcon("ChevronRight", "M 9 6 l 6 6 l -6 6")
    val Bookmark = strokeIcon(
        "Bookmark",
        "m 19 21 l -7 -4 l -7 4 V 5 a 2 2 0 0 1 2 -2 h 10 a 2 2 0 0 1 2 2 z",
    )

    /** `ic_filter` — qidiruv yonidagi filtr tugmasi. */
    val Filter = strokeIcon("Filter", "M 3 5 h 18 l -7 8 v 5 l -4 2 v -7 L 3 5 z")

    /** Nishon (crosshair) — "mening joylashuvim" tugmasi. */
    val Locate = strokeIcon(
        "Locate",
        "M 19 12 a 7 7 0 1 1 -14 0 a 7 7 0 0 1 14 0",
        "M 12 1.5 v 3",
        "M 12 19.5 v 3",
        "M 1.5 12 h 3",
        "M 19.5 12 h 3",
        "M 13.1 12 a 1.1 1.1 0 1 1 -2.2 0 a 1.1 1.1 0 0 1 2.2 0",
    )

    /**
     * Qo'llab-quvvatlash — garnitura: bosh kamar, ikkita quloqchin va pastga tushuvchi
     * mikrofon shtangasi og'izchasi bilan.
     *
     * Chizma ataylab sodda (suhbat pufagi va nuqtalarsiz): 24dp da har bir chiziq ajralib
     * turishi kerak, zich shakl kichik dog'ga aylanadi.
     */
    val Support = strokeIcon(
        "Support",
        // Bosh kamar
        "M 3.5 10.5 V 9 a 8.5 8.5 0 0 1 17 0 v 1.5",
        // Chap quloqchin
        "M 2.8 10 h 1.4 a 1.6 1.6 0 0 1 1.6 1.6 v 4.8 a 1.6 1.6 0 0 1 -1.6 1.6 h -1.4 a 1.6 1.6 0 0 1 -1.6 -1.6 v -4.8 a 1.6 1.6 0 0 1 1.6 -1.6 z",
        // O'ng quloqchin
        "M 19.8 10 h 1.4 a 1.6 1.6 0 0 1 1.6 1.6 v 4.8 a 1.6 1.6 0 0 1 -1.6 1.6 h -1.4 a 1.6 1.6 0 0 1 -1.6 -1.6 v -4.8 a 1.6 1.6 0 0 1 1.6 -1.6 z",
        // Mikrofon shtangasi — o'ng quloqchindan pastga va chapga
        "M 21 18 v 0.8 a 2.5 2.5 0 0 1 -2.5 2.5 h -3.2",
        // Mikrofon og'izchasi
        "M 10.1 21.3 a 2.6 2.6 0 1 0 5.2 0 a 2.6 2.6 0 1 0 -5.2 0",
    )

    /** `ic_send_enter` — xabar yuborish tugmasi (matn yozilganda). */
    val Send = strokeIcon(
        "Send",
        "M 9 10 V 7 a 1 1 0 0 1 1 -1 h 9",
        "M 19 6 v 6 a 1 1 0 0 1 -1 1 H 6",
        "M 9 10 l -3 3 l 3 3",
    )
    val Settings = strokeIcon(
        "Settings",
        "M 12 15 a 3 3 0 1 0 0 -6 a 3 3 0 0 0 0 6 z",
        "M 19.4 15 a 1.65 1.65 0 0 0 0.33 1.82 l 0.06 0.06 a 2 2 0 1 1 -2.83 2.83 l -0.06 -0.06 a 1.65 1.65 0 0 0 -1.82 -0.33 a 1.65 1.65 0 0 0 -1 1.51 V 21 a 2 2 0 0 1 -4 0 v -0.09 A 1.65 1.65 0 0 0 9 19.4 a 1.65 1.65 0 0 0 -1.82 0.33 l -0.06 0.06 a 2 2 0 1 1 -2.83 -2.83 l 0.06 -0.06 a 1.65 1.65 0 0 0 0.33 -1.82 a 1.65 1.65 0 0 0 -1.51 -1 H 3 a 2 2 0 0 1 0 -4 h 0.09 A 1.65 1.65 0 0 0 4.6 9 a 1.65 1.65 0 0 0 -0.33 -1.82 l -0.06 -0.06 a 2 2 0 1 1 2.83 -2.83 l 0.06 0.06 a 1.65 1.65 0 0 0 1.82 0.33 H 9 a 1.65 1.65 0 0 0 1 -1.51 V 3 a 2 2 0 0 1 4 0 v 0.09 a 1.65 1.65 0 0 0 1 1.51 a 1.65 1.65 0 0 0 1.82 -0.33 l 0.06 -0.06 a 2 2 0 1 1 2.83 2.83 l -0.06 0.06 a 1.65 1.65 0 0 0 -0.33 1.82 V 9 a 1.65 1.65 0 0 0 1.51 1 H 21 a 2 2 0 0 1 0 4 h -0.09 a 1.65 1.65 0 0 0 -1.51 1 z",
    )
    val LogOut = strokeIcon("LogOut", "M 9 21 H 5 a 2 2 0 0 1 -2 -2 V 5 a 2 2 0 0 1 2 -2 h 4", "m 16 17 l 5 -5 l -5 -5", "M 21 12 H 9")
    val FileText = strokeIcon(
        "FileText",
        "M 14 2 H 6 a 2 2 0 0 0 -2 2 v 16 a 2 2 0 0 0 2 2 h 12 a 2 2 0 0 0 2 -2 V 8 z",
        "M 14 2 v 6 h 6",
        "M 9 13 h 6",
        "M 9 17 h 6",
    )
    /** Ilovadan tashqariga (brauzerga) olib chiqadigan havola — qutidan chiquvchi strelka. */
    val ExternalLink = strokeIcon(
        "ExternalLink",
        "M 18 13 v 6 a 2 2 0 0 1 -2 2 H 5 a 2 2 0 0 1 -2 -2 V 8 a 2 2 0 0 1 2 -2 h 6",
        "M 15 3 h 6 v 6",
        "M 10 14 L 21 3",
    )
    val Pencil = strokeIcon(
        "Pencil",
        "M 12 20 h 9",
        "M 16.5 3.5 a 2.121 2.121 0 0 1 3 3 L 7 19 l -4 1 l 1 -4 z",
    )
    val Star = strokeIcon("Star", "M 12 2 l 3.09 6.26 L 22 9.27 l -5 4.87 l 1.18 6.88 L 12 17.77 l -6.18 3.25 L 7 14.14 l -5 -4.87 l 6.91 -1.01 z")
    val Camera = strokeIcon(
        "Camera",
        "M 14.5 4 h -5 L 7 7 H 4 a 2 2 0 0 0 -2 2 v 9 a 2 2 0 0 0 2 2 h 16 a 2 2 0 0 0 2 -2 V 9 a 2 2 0 0 0 -2 -2 h -3 l -2.5 -3 z",
        "M 15 13 a 3 3 0 1 1 -6 0 a 3 3 0 0 1 6 0 z",
    )
    val ImageIcon = strokeIcon(
        "Image",
        "M 5 3 h 14 a 2 2 0 0 1 2 2 v 14 a 2 2 0 0 1 -2 2 H 5 a 2 2 0 0 1 -2 -2 V 5 a 2 2 0 0 1 2 -2 z",
        "M 8.5 10 a 1.5 1.5 0 1 0 0 -3 a 1.5 1.5 0 0 0 0 3 z",
        "m 21 15 l -5 -5 L 5 21",
    )
    val UserPlus = strokeIcon(
        "UserPlus",
        "M 16 21 v -2 a 4 4 0 0 0 -4 -4 H 6 a 4 4 0 0 0 -4 4 v 2",
        "M 9 3 a 4 4 0 1 0 0 8 a 4 4 0 0 0 0 -8 z",
        "M 19 8 v 6",
        "M 22 11 h -6",
    )

    // -----------------------------------------------------------------------
    // Dizayn handoff ikonalari (`icons/ic_*.svg`) — yangi qo'shilganlari
    // -----------------------------------------------------------------------

    /** `ic_ads` — "Mening e'lonlarim" bo'limi (varaq + qatorlar). */
    val Ads = strokeIcon(
        "Ads",
        "M 6 3 h 8 l 4 4 v 14 H 6 V 3 z",
        "M 14 3 v 4 h 4",
        "M 9 13 h 6",
        "M 9 16 h 4",
    )

    /** `ic_attach` — xabar maydonida fayl/rasm biriktirish (qisqich). */
    val Attach = strokeIcon(
        "Attach",
        "M 21 11 l -8.5 8.5 a 5 5 0 0 1 -7 -7 L 14 4 a 3.3 3.3 0 0 1 4.7 4.7 l -8.5 8.5 a 1.6 1.6 0 0 1 -2.3 -2.3 l 7.8 -7.8",
    )

    /** `ic_backspace` — klaviaturada o'chirish. */
    val Backspace = strokeIcon(
        "Backspace",
        "M 21 5 H 9 L 3 12 l 6 7 h 12 V 5 z",
        "M 12 9.5 l 5 5",
        "M 17 9.5 l -5 5",
    )

    /** `ic_ball` — sport klubi (doira + ichki beshburchak bo'lagi). */
    val Ball = strokeIcon(
        "Ball",
        "M 3.5 12 a 8.5 8.5 0 1 0 17 0 a 8.5 8.5 0 1 0 -17 0",
        "M 12 7 l 2.4 1.7 l -0.9 2.8 h -3 l -0.9 -2.8 L 12 7 z",
    )

    /** `ic_book` — yordam e'lonlari (referat, kurs ishi) va o'quv markazlari. */
    val Book = strokeIcon(
        "Book",
        "M 4 5.5 A 1.5 1.5 0 0 1 5.5 4 H 11 v 15 H 5.5 A 1.5 1.5 0 0 1 4 17.5 v -12 z",
        "M 13 4 h 5.5 A 1.5 1.5 0 0 1 20 5.5 v 12 a 1.5 1.5 0 0 1 -1.5 1.5 H 13 V 4 z",
    )

    /** `ic_cafe` — kafe va restoran kategoriyasi. */
    val Cafe = strokeIcon(
        "Cafe",
        "M 4 8 h 13 a 3 3 0 0 1 0 6 h -1 M 4 8 v 7 a 4 4 0 0 0 4 4 h 4 a 4 4 0 0 0 4 -4",
        "M 8 3 v 2",
        "M 11 3 v 2",
    )

    /** `ic_cart` — oziq-ovqat / xarid / kiyim-kechak savati. */
    val Cart = strokeIcon(
        "Cart",
        "M 3 4 h 2 l 2 12 h 11 l 2 -8 H 6",
        "M 7.6 20 a 1.4 1.4 0 1 0 2.8 0 a 1.4 1.4 0 1 0 -2.8 0",
        "M 15.6 20 a 1.4 1.4 0 1 0 2.8 0 a 1.4 1.4 0 1 0 -2.8 0",
    )

    /** `ic_chat` — topbardagi "chatlarga o'tish" tugmasi (dumaloq pufak). */
    val Chat = strokeIcon(
        "Chat",
        "M 21 11.5 a 8.4 8.4 0 0 1 -11.9 7.6 L 4 21 l 1.9 -5.1 A 8.4 8.4 0 1 1 21 11.5 z",
    )

    /** `ic_check_double` — xabar yetkazildi/o'qildi belgisi (ikki galochka). */
    val CheckDouble = strokeIcon(
        "CheckDouble",
        "M 1 12 l 5 5 L 15 7",
        "M 9 17 L 18.5 7",
    )

    /** `ic_chevron_updown` — tanlov/dropdown (universitet, shahar, saralash). */
    val ChevronUpDown = strokeIcon("ChevronUpDown", "M 8 9 l 4 -4 l 4 4 M 8 15 l 4 4 l 4 -4")

    /** `ic_debate` — debat klubi (pufak + matn qatorlari). */
    val Debate = strokeIcon(
        "Debate",
        "M 4 5 h 16 v 11 H 8 l -4 4 V 5 z",
        "M 8 9 h 8",
        "M 8 12 h 5",
    )

    /** `ic_emoji` — xabar maydonida emoji tanlash tugmasi. */
    val Emoji = strokeIcon(
        "Emoji",
        "M 3 12 a 9 9 0 1 0 18 0 a 9 9 0 1 0 -18 0",
        "M 7.9 10 a 1.1 1.1 0 1 0 2.2 0 a 1.1 1.1 0 1 0 -2.2 0",
        "M 13.9 10 a 1.1 1.1 0 1 0 2.2 0 a 1.1 1.1 0 1 0 -2.2 0",
        "M 8.5 14 c 0.9 1.2 2.1 1.8 3.5 1.8 s 2.6 -0.6 3.5 -1.8",
    )

    /** `ic_gamepad` — Game Club / GameZone. */
    val Gamepad = strokeIcon(
        "Gamepad",
        "M 7 7 h 10 a 5 5 0 0 1 5 5 a 5 5 0 0 1 -5 5 h -10 a 5 5 0 0 1 -5 -5 a 5 5 0 0 1 5 -5 z",
        "M 7 11 v 2",
        "M 6 12 h 2",
        "M 15 11.5 a 1 1 0 1 0 2 0 a 1 1 0 1 0 -2 0",
        "M 17 13.5 a 1 1 0 1 0 2 0 a 1 1 0 1 0 -2 0",
    )

    /** `ic_home_filled` — ijara kvartira kartochkasi (to'ldirilgan uy). */
    val HomeFilled = strokeIcon(
        "HomeFilled",
        "M 4 11 l 8 -6 l 8 6 v 8 a 1 1 0 0 1 -1 1 H 5 a 1 1 0 0 1 -1 -1 v -8 z",
        "M 10 20 v -5 h 4 v 5",
    )

    /** `ic_laptop` — IT klub / IT xizmatlari / texnika. */
    val Laptop = strokeIcon(
        "Laptop",
        "M 5.5 5 h 13 a 2.5 2.5 0 0 1 2.5 2.5 v 7 a 2.5 2.5 0 0 1 -2.5 2.5 h -13 a 2.5 2.5 0 0 1 -2.5 -2.5 v -7 a 2.5 2.5 0 0 1 2.5 -2.5 z",
        "M 2 21 h 20",
        "M 9 10 l -2 2 l 2 2 M 13 10 l 2 2 l -2 2",
    )

    /** `ic_map` — xarita ko'rinishi / joylashuv bo'yicha qidiruv. */
    val Map = strokeIcon(
        "Map",
        "M 9 3 L 3 5 v 16 l 6 -2 l 6 2 l 6 -2 V 3 l -6 2 l -6 -2 z",
        "M 9 3 v 16",
        "M 15 5 v 16",
    )

    /** `ic_mic` — ovozli xabar (matn bo'sh bo'lganda). */
    val Mic = strokeIcon(
        "Mic",
        "M 12 3 a 3 3 0 0 1 3 3 v 5 a 3 3 0 0 1 -3 3 a 3 3 0 0 1 -3 -3 v -5 a 3 3 0 0 1 3 -3 z",
        "M 6 11 a 6 6 0 0 0 12 0",
        "M 12 17 v 3",
    )

    /** `ic_more` — qo'shimcha menyu (uch nuqta). */
    val More = strokeIcon(
        "More",
        "M 10.4 5 a 1.6 1.6 0 1 0 3.2 0 a 1.6 1.6 0 1 0 -3.2 0",
        "M 10.4 12 a 1.6 1.6 0 1 0 3.2 0 a 1.6 1.6 0 1 0 -3.2 0",
        "M 10.4 19 a 1.6 1.6 0 1 0 3.2 0 a 1.6 1.6 0 1 0 -3.2 0",
    )

    /** `ic_pin` — manzil / geolokatsiya belgisi. */
    val Pin = strokeIcon(
        "Pin",
        "M 12 21 s 7 -6.3 7 -11 a 7 7 0 1 0 -14 0 c 0 4.7 7 11 7 11 z",
        "M 9.6 10 a 2.4 2.4 0 1 0 4.8 0 a 2.4 2.4 0 1 0 -4.8 0",
    )

    /** `ic_shift` — klaviaturada katta harf. */
    val Shift = strokeIcon("Shift", "M 12 5 l 7 7 h -4 v 6 H 9 v -6 H 5 l 7 -7 z")

    /** `ic_tools` — xizmat e'lonlari (usta, ta'mirlash, sartaroshxona). */
    val Tools = strokeIcon(
        "Tools",
        "M 14.5 6 a 3.5 3.5 0 0 0 -4.8 4.3 l -5 5 a 1.5 1.5 0 0 0 2.1 2.1 l 5 -5 A 3.5 3.5 0 0 0 18 8.5 l -2.3 2.3 l -1.8 -1.8 L 16.2 6.7 A 3.5 3.5 0 0 0 14.5 6 z",
    )

    /**
     * `ic_trash` — o'chirish. Handoff qoidasi: **o'chirish doim savat**, hech qachon `✕`.
     * `✕` faqat yopish uchun ([Close]) — ikkalasi bir xil ikonka bo'lganda foydalanuvchi
     * "yopaman" deb bosib, e'lonini o'chirib yuborardi.
     */
    val Trash = strokeIcon(
        "Trash",
        "M 4 7 h 16",
        "M 9 7 V 5 a 1 1 0 0 1 1 -1 h 4 a 1 1 0 0 1 1 1 v 2",
        "M 6 7 l 1 12 a 2 2 0 0 0 2 2 h 6 a 2 2 0 0 0 2 -2 l 1 -12",
        "M 10 11 v 6",
        "M 14 11 v 6",
    )

    // ------------------------------------------------------------------
    // Biznes turlari — har bir tur o'z ikonkasini oladi.
    //
    // Ilgari yaqin turlar bitta ikonkani baham ko'rardi: barcha 12 ta sport turi bitta
    // koptok, sartaroshxona esa gaechniy kalit ("usta") edi. Ro'yxatda 27 tur bir-biriga
    // o'xshab ketardi va tanlash faqat matnga qolardi — ikonka umuman ma'lumot bermasdi.
    // ------------------------------------------------------------------

    /** Sartaroshxona — qaychi. */
    val Scissors = strokeIcon(
        "Scissors",
        "M 5 5 l 11 13",
        "M 19 5 L 8 18",
        "M 4 18.5 a 2.5 2.5 0 1 0 5 0 a 2.5 2.5 0 1 0 -5 0",
        "M 15 18.5 a 2.5 2.5 0 1 0 5 0 a 2.5 2.5 0 1 0 -5 0",
    )

    /** Go'zallik saloni — yaltiroq (uchqun). */
    val Sparkles = strokeIcon(
        "Sparkles",
        "M 11 3 l 1.7 4.3 L 17 9 l -4.3 1.7 L 11 15 l -1.7 -4.3 L 5 9 l 4.3 -1.7 L 11 3 z",
        "M 17.5 14 l 0.9 2.1 l 2.1 0.9 l -2.1 0.9 l -0.9 2.1 l -0.9 -2.1 l -2.1 -0.9 l 2.1 -0.9 l 0.9 -2.1 z",
    )

    /** Kinoteatr — kinolenta (ilgari fotoapparat edi, bu esa "rasm yuklash" bilan chalkashardi). */
    val Film = strokeIcon(
        "Film",
        "M 3 5 a 1 1 0 0 1 1 -1 h 16 a 1 1 0 0 1 1 1 v 14 a 1 1 0 0 1 -1 1 H 4 a 1 1 0 0 1 -1 -1 z",
        "M 7 4 v 16",
        "M 17 4 v 16",
        "M 3 9 h 4 M 3 15 h 4 M 17 9 h 4 M 17 15 h 4",
    )

    /** Fitnes / trenajyor zali — gantel. */
    val Dumbbell = strokeIcon(
        "Dumbbell",
        "M 3 9 h 2 v 6 H 3 z",
        "M 19 9 h 2 v 6 h -2 z",
        "M 5 7.5 h 3 v 9 H 5 z",
        "M 16 7.5 h 3 v 9 h -3 z",
        "M 8 12 h 8",
    )

    /** Tennis va stol tennisi — raketka. */
    val Racket = strokeIcon(
        "Racket",
        "M 9.5 3 a 6.5 6.5 0 0 1 5 11 l -2 -2 a 6.5 6.5 0 0 1 -3 -9 z",
        "M 4 8.5 a 6.5 6.5 0 0 0 8.5 8.5",
        "M 11.5 15.5 l 6 6",
        "M 17.5 19 l 2 -2",
    )

    /** Bouling — kegli va shar. */
    val BowlingPin = strokeIcon(
        "BowlingPin",
        "M 10 3 c 3 0 4 3 3 6 c -0.6 1.8 -0.6 3.2 0 5 c 1 3 0 6 -3 6 s -4 -3 -3 -6 c 0.6 -1.8 0.6 -3.2 0 -5 c -1 -3 0 -6 3 -6 z",
        "M 16 16 a 3 3 0 1 0 6 0 a 3 3 0 1 0 -6 0",
    )

    /** Bilyard — shar va kiy. */
    val Billiards = strokeIcon(
        "Billiards",
        "M 3 16 a 4.5 4.5 0 1 0 9 0 a 4.5 4.5 0 1 0 -9 0",
        "M 6.4 16 a 1.1 1.1 0 1 0 2.2 0 a 1.1 1.1 0 1 0 -2.2 0",
        "M 11 13 L 21 3",
    )

    /** Suzish havzasi — to'lqinlar. */
    val Swim = strokeIcon(
        "Swim",
        "M 2 16 c 2 -1.5 3.3 -1.5 5 0 s 3 1.5 5 0 s 3.3 -1.5 5 0 s 3 1.5 5 0",
        "M 2 20 c 2 -1.5 3.3 -1.5 5 0 s 3 1.5 5 0 s 3.3 -1.5 5 0 s 3 1.5 5 0",
        "M 7 12 l 6 -4",
        "M 14.5 5.5 a 1.5 1.5 0 1 0 3 0 a 1.5 1.5 0 1 0 -3 0",
        "M 13 8 l 4 3",
    )

    /** Boks va kurash / MMA — qo'lqop. */
    val BoxingGlove = strokeIcon(
        "BoxingGlove",
        "M 6 5 a 3 3 0 0 1 3 -3 h 5 a 5 5 0 0 1 5 5 v 4 a 4 4 0 0 1 -4 4 H 6 z",
        "M 6 15 h 9 v 3 a 2 2 0 0 1 -2 2 H 8 a 2 2 0 0 1 -2 -2 z",
        "M 6 9 h 3",
    )

    /** Kiyim-kechak — futbolka (savat "xarid"ni bildirardi, turni emas). */
    val Shirt = strokeIcon(
        "Shirt",
        "M 9 3 L 4 5.5 L 6 10 l 2 -1 v 12 h 8 V 9 l 2 1 l 2 -4.5 L 15 3",
        "M 9 3 a 3 3 0 0 0 6 0",
    )

    /** Fast food — burger. */
    val Burger = strokeIcon(
        "Burger",
        "M 4 9 a 8 5 0 0 1 16 0 z",
        "M 3.5 13 h 17",
        "M 4 16.5 h 16 a 3 3 0 0 1 -3 3 H 7 a 3 3 0 0 1 -3 -3 z",
    )

    /** Milliy taomlar — kosa. */
    val Bowl = strokeIcon(
        "Bowl",
        "M 3 11 h 18 a 9 9 0 0 1 -18 0 z",
        "M 9 7 c 0 -1.5 1.5 -1.5 1.5 -3",
        "M 13 7 c 0 -1.5 1.5 -1.5 1.5 -3",
        "M 5 20 h 14",
    )

    /** Somsa / nonvoyxona — uchburchak somsa. */
    val Pastry = strokeIcon(
        "Pastry",
        "M 12 4 l 8 15 H 4 z",
        "M 12 4 v 15",
        "M 8 12 h 8",
    )

    /** Bosmaxona / tipografiya — printer. */
    val Printer = strokeIcon(
        "Printer",
        "M 7 8 V 4 h 10 v 4",
        "M 5 8 h 14 a 2 2 0 0 1 2 2 v 5 a 2 2 0 0 1 -2 2 h -2 M 7 17 H 5 a 2 2 0 0 1 -2 -2 v -5 a 2 2 0 0 1 2 -2 z",
        "M 7 14 h 10 v 6 H 7 z",
    )

    /** Ijara uy-joy — kalit. */
    val Key = strokeIcon(
        "Key",
        "M 13.5 3 a 5.5 5.5 0 1 1 -4 9.3 L 3 19 v 2 h 3 v -2 h 2 v -2 h 2 l 1.5 -1.5 A 5.5 5.5 0 0 1 13.5 3 z",
        "M 15.5 7.5 a 1 1 0 1 0 2 0 a 1 1 0 1 0 -2 0",
    )

    /** Basketbol maydoni — chiziqli koptok. */
    val Basketball = strokeIcon(
        "Basketball",
        "M 3 12 a 9 9 0 1 0 18 0 a 9 9 0 1 0 -18 0",
        "M 12 3 v 18",
        "M 3.6 9 h 16.8 M 3.6 15 h 16.8",
    )

    /** Voleybol maydoni — egri chiziqli koptok. */
    val Volleyball = strokeIcon(
        "Volleyball",
        "M 3 12 a 9 9 0 1 0 18 0 a 9 9 0 1 0 -18 0",
        "M 12 3 a 12 12 0 0 0 0 18",
        "M 4 7.5 a 12 12 0 0 1 15.5 4",
        "M 4.5 16.5 a 12 12 0 0 0 15 -4",
    )

    /**
     * Erkak belgisi (Mars). Handoff to'plamida jins ikonkalari yo'q — Lucide'dan olindi,
     * chunki kiyim-kechak e'lonida jins tanlash 👨/👩 emojisidan xoli bo'lishi kerak.
     */
    val Mars = strokeIcon(
        "Mars",
        "M 4 14 a 6 6 0 1 0 12 0 a 6 6 0 1 0 -12 0",
        "M 16 3 h 5 v 5",
        "M 21 3 l -6.8 6.8",
    )

    /** Ayol belgisi (Venus) — [Mars] bilan juft. */
    val Venus = strokeIcon(
        "Venus",
        "M 7 9 a 5 5 0 1 0 10 0 a 5 5 0 1 0 -10 0",
        "M 12 14 v 7",
        "M 9 18 h 6",
    )

    /**
     * Katalog kaliti (kategoriya/taklif `id`) bo'yicha ikonka.
     *
     * Chegirma kategoriyalari va takliflari backenddan **barqaror ASCII id** bilan keladi
     * (`"ovqat"`, `"kiyim"`, ...). Ilgari ular yonida emoji chizilardi — iOS'da u `?` bo'lib
     * ko'rinadi, shuning uchun endi shu jadval orqali ikonkaga aylantiriladi.
     *
     * Noma'lum kalit uchun zaxira — [Tag] (chegirma yorlig'i).
     */
    fun forCatalog(key: String): ImageVector = when (key.lowercase()) {
        "ovqat", "food", "cafe", "restaurant" -> Cafe
        "kiyim", "clothing" -> Cart
        "kurslar", "education", "courses" -> Book
        "kino", "entertainment", "cinema" -> Camera
        "transport" -> Map
        "texnika", "it", "tech" -> Laptop
        "sport" -> Ball
        "uy", "ijara", "rent" -> HomeFilled
        "ish", "job" -> Briefcase
        "xizmat", "service" -> Tools
        "gameclub", "game" -> Gamepad
        else -> Tag
    }

    /** Apple logo — bitta to'ldirilgan yo'l; `Icon(tint=...)` bilan bo'yaladi. */
    val Apple: ImageVector = ImageVector.Builder(
        "Apple", 24.dp, 24.dp, 24f, 24f,
    ).apply {
        addPath(
            pathData = PathParser().parsePathString(
                "M 17.05 12.04 c -0.03 -2.94 2.4 -4.35 2.51 -4.42 c -1.37 -2 -3.5 -2.28 -4.26 -2.31 c -1.81 -0.18 -3.53 1.07 -4.45 1.07 c -0.92 0 -2.33 -1.04 -3.83 -1.01 c -1.97 0.03 -3.79 1.15 -4.8 2.91 c -2.05 3.55 -0.52 8.8 1.47 11.68 c 0.97 1.41 2.13 2.99 3.64 2.93 c 1.46 -0.06 2.01 -0.94 3.78 -0.94 c 1.76 0 2.26 0.94 3.8 0.91 c 1.57 -0.03 2.56 -1.43 3.52 -2.85 c 1.11 -1.63 1.57 -3.21 1.59 -3.29 c -0.03 -0.02 -3.05 -1.17 -3.08 -4.66 z M 14.13 3.57 c 0.81 -0.98 1.36 -2.34 1.21 -3.7 c -1.17 0.05 -2.59 0.78 -3.43 1.76 c -0.75 0.87 -1.41 2.26 -1.23 3.59 c 1.3 0.1 2.64 -0.66 3.45 -1.65 z",
            ).toNodes(),
            fill = SolidColor(Color.Black),
        )
    }.build()

    /** Google 'G' — ko'p rangli, `Image` bilan chiziladi. */
    val Google: ImageVector = ImageVector.Builder(
        "Google", 24.dp, 24.dp, 48f, 48f,
    ).apply {
        addPath(
            PathParser().parsePathString("M 43.6 20.1 H 42 V 20 H 24 v 8 h 11.3 C 33.7 32.7 29.3 36 24 36 c -6.6 0 -12 -5.4 -12 -12 s 5.4 -12 12 -12 c 3.1 0 5.8 1.2 7.9 3 l 5.7 -5.7 C 34 6.1 29.3 4 24 4 C 13 4 4 13 4 24 s 9 20 20 20 s 20 -9 20 -20 c 0 -1.3 -0.1 -2.7 -0.4 -3.9 z").toNodes(),
            fill = SolidColor(Color(0xFFFFC107)),
        )
        addPath(
            PathParser().parsePathString("m 6.3 14.7 l 6.6 4.8 C 14.7 15.1 19 12 24 12 c 3.1 0 5.8 1.2 7.9 3 l 5.7 -5.7 C 34 6.1 29.3 4 24 4 C 16.3 4 9.7 8.3 6.3 14.7 z").toNodes(),
            fill = SolidColor(Color(0xFFFF3D00)),
        )
        addPath(
            PathParser().parsePathString("M 24 44 c 5.2 0 9.9 -2 13.4 -5.2 l -6.2 -5.2 C 29.2 35.1 26.7 36 24 36 c -5.2 0 -9.6 -3.3 -11.3 -7.9 l -6.5 5 C 9.5 39.6 16.2 44 24 44 z").toNodes(),
            fill = SolidColor(Color(0xFF4CAF50)),
        )
        addPath(
            PathParser().parsePathString("M 43.6 20.1 H 42 V 20 H 24 v 8 h 11.3 c -0.8 2.2 -2.2 4.2 -4.1 5.6 l 6.2 5.2 C 36.9 40.2 44 34 44 24 c 0 -1.3 -0.1 -2.7 -0.4 -3.9 z").toNodes(),
            fill = SolidColor(Color(0xFF1976D2)),
        )
    }.build()

    /** Telegram — ko'k doira + oq samolyot, `Image` bilan chiziladi. */
    val Telegram: ImageVector = ImageVector.Builder(
        "Telegram", 24.dp, 24.dp, 24f, 24f,
    ).apply {
        addPath(
            PathParser().parsePathString("M 12 1 a 11 11 0 1 0 0 22 a 11 11 0 0 0 0 -22 z").toNodes(),
            fill = SolidColor(Color(0xFF229ED9)),
        )
        addPath(
            PathParser().parsePathString("M 5.6 11.7 l 11 -4.24 c 0.51 -0.19 0.96 0.12 0.79 0.9 l -1.87 8.82 c -0.13 0.6 -0.5 0.75 -1 0.47 l -2.76 -2.03 l -1.33 1.28 c -0.15 0.15 -0.27 0.27 -0.55 0.27 l 0.2 -2.8 l 5.12 -4.62 c 0.22 -0.2 -0.05 -0.31 -0.34 -0.11 L 9.9 13.5 l -2.73 -0.85 c -0.59 -0.19 -0.6 -0.59 0.13 -0.87 z").toNodes(),
            fill = SolidColor(Color.White),
        )
    }.build()
}
