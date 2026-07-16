package dev.core.data.seed

import dev.core.common.AppDispatchers
import dev.core.data.mapper.joinDb
import dev.core.data.mapper.toDb
import dev.core.database.sql.StudentClubsDatabase
import dev.core.domain.model.AdType
import dev.core.domain.model.ConversationType
import dev.core.domain.model.DiscountTag
import dev.core.domain.model.FriendStatus
import kotlinx.coroutines.withContext

/**
 * Local bazani dizayndagi namuna ma'lumot bilan to'ldiradi (jadval bo'sh bo'lsagina).
 * Backend ulanганда bu seed o'rniga API'dan sinxronlash keladi — tuzilma bir xil.
 */
class LocalDataSeeder(
    private val db: StudentClubsDatabase,
    private val dispatchers: AppDispatchers,
) {
    suspend fun seedIfEmpty() = withContext(dispatchers.io) {
        seedUniversities()
        seedDiscounts()
        seedJobs()
        seedStudents()
        seedAds()
        seedChat()
        seedNotifications()
        seedClubs()
    }

    private fun seedUniversities() {
        val q = db.universityQueries
        if (q.count().executeAsOne() > 0) return
        val list = listOf(
            Uni("tatu", "TATU — al-Xorazmiy nomidagi", "Toshkent shahri", "TA", "Axborot texnologiyalari", 0xFF6C47FF),
            Uni("nuu", "O‘zbekiston Milliy Universiteti", "Toshkent shahri", "NU", null, 0xFF2563EB),
            Uni("tdiu", "Toshkent Davlat Iqtisodiyot U.", "Toshkent shahri", "TD", null, 0xFF059669),
            Uni("tpi", "Toshkent Politexnika Instituti", "Toshkent shahri", "TP", null, 0xFFD97706),
            Uni("tta", "Toshkent Tibbiyot Akademiyasi", "Toshkent shahri", "TT", null, 0xFFBE185D),
            Uni("samdu", "Samarqand Davlat Universiteti", "Samarqand", "SD", null, 0xFF6C47FF),
            Uni("buxdu", "Buxoro Davlat Universiteti", "Buxoro", "BD", null, 0xFF2563EB),
            Uni("qarshi", "Qarshi Davlat Universiteti", "Qarshi", "QD", null, 0xFF059669),
            Uni("namdu", "Namangan Davlat Universiteti", "Namangan", "ND", null, 0xFFD97706),
            Uni("ferpi", "Farg‘ona Politexnika Instituti", "Farg‘ona", "FP", null, 0xFFBE185D),
            Uni("inha", "Inha University in Tashkent", "Toshkent shahri", "IU", null, 0xFF6C47FF),
            Uni("wiut", "Westminster University", "Toshkent shahri", "WU", null, 0xFF2563EB),
            Uni("turin", "Turin Politexnika Universiteti", "Toshkent shahri", "TU", null, 0xFF059669),
            Uni("adu", "Andijon Davlat Universiteti", "Andijon", "AD", null, 0xFFBE185D),
        )
        q.transaction { list.forEach { q.upsert(it.id, it.name, it.city, it.monogram, it.faculty, it.accent) } }
    }

    private fun seedDiscounts() {
        val q = db.discountQueries
        if (q.countCategories().executeAsOne() > 0) return
        q.transaction {
            q.upsertCategory("ovqat", "Ovqat", "🍔", 124, 0xFFF97316)
            q.upsertCategory("kiyim", "Kiyim-kechak", "👕", 86, 0xFFBE185D)
            q.upsertCategory("kurslar", "Kurslar", "📚", 52, 0xFF2563EB)
            q.upsertCategory("kino", "Kino & Ko‘ngil", "🎬", 33, 0xFF059669)
            q.upsertCategory("transport", "Transport", "🚌", 18, 0xFF0EA5E9)
            q.upsertCategory("texnika", "Texnika", "💻", 41, 0xFFD97706)

            // id, categoryId, merchant, title, %, tag, promo, location, expiry, emoji, banner, featured
            q.upsertOffer("of-evos", "ovqat", "Evos", "barcha pitsalarga", 30, DiscountTag.STUDENT_ID.name, null, "5 filial", "bugun tugaydi", "🍕", 0xFFF97316, 1)
            q.upsertOffer("of-chopar", "ovqat", "Chopar", "shirinliklar", 20, DiscountTag.PROMO_CODE.name, "STUDENT20", "yetkazib berish", null, "🍰", 0xFFBE185D, 0)
            q.upsertOffer("of-zara", "kiyim", "Zara", "yangi kolleksiya", 15, DiscountTag.STUDENT_ID.name, null, "Samarqand Darvoza", null, "👕", 0xFFBE185D, 0)
            q.upsertOffer("of-udemy", "kurslar", "Udemy", "online kurslar", 50, DiscountTag.PROMO_CODE.name, "STUD50", null, "shu oy", "📚", 0xFF2563EB, 1)
            q.upsertOffer("of-nebo", "kino", "NEBO Cinema", "kechki seanslar", 25, DiscountTag.STUDENT_ID.name, null, "Chilonzor", null, "🎬", 0xFF059669, 0)
        }
    }

    private fun seedJobs() {
        val q = db.jobQueries
        if (q.countJobs().executeAsOne() > 0) return
        q.transaction {
            q.upsertJob("j-smm", "SMM menejer (part-time)", "Uzum Market", "U", "masofaviy", "IT",
                listOf("IT", "SMM").joinDb(), "3–5 mln so‘m", true.toDb(), true.toDb(), "2 soat oldin", "IT", false.toDb())
            q.upsertJob("j-frontend", "Frontend intern", "PayNet", "P", "Toshkent", "IT",
                listOf("IT", "Vue", "Ofis").joinDb(), "4 mln so‘m", false.toDb(), false.toDb(), "bugun", "IT", false.toDb())
            q.upsertJob("j-ofitsiant", "Ofitsiant (kechqurun)", "Evos", "E", "Chilonzor", "Xizmat",
                listOf("Xizmat", "Smenali").joinDb(), "1.5 mln so‘m", false.toDb(), true.toDb(), "kecha", "Xizmat", false.toDb())
        }
    }

    private fun seedStudents() {
        val q = db.studentQueries
        if (q.countStudents().executeAsOne() > 0) return
        q.transaction {
            // id, first, last, initial, uniId, uniMono, course, faculty, friendStatus, interests, friends, ads, rating
            q.upsert("st-dilnoza", "Dilnoza", "Rahimova", "D", "tatu", "TATU", 2, "IT", FriendStatus.NONE.name,
                listOf("🎨 Dizayn", "💻 Frontend", "📷 Foto", "🏀 Sport").joinDb(), 148, 12, 4.9)
            q.upsert("st-sardor", "Sardor", "Aliyev", "S", "tatu", "TATU", 3, "Telekom", FriendStatus.NONE.name, "", 96, 3, 4.7)
            q.upsert("st-malika", "Malika", "Yo‘ldosheva", "M", "nuu", "O‘zMU", 2, "Iqtisod", FriendStatus.PENDING.name, "", 54, 1, 4.5)
            q.upsert("st-kamron", "Kamron", "Yusupov", "K", "tatu", "TATU", 2, "Dasturiy inj.", FriendStatus.NONE.name, "", 71, 5, 4.8)
            q.upsert("st-nigora", "Nigora", "Tosheva", "N", "tatu", "TATU", 1, "Kiberxavfsizlik", FriendStatus.NONE.name, "", 33, 0, 4.6)
        }
    }

    private fun seedAds() {
        val q = db.adQueries
        if (q.selectAll().executeAsList().isNotEmpty()) return
        q.transaction {
            q.upsert("ad-1", AdType.RENTAL.name, "Chilonzorda room-mate", "Turar joy", "1.2 mln/oy",
                "2 xonali, metroga yaqin, student uchun qulay.", "", "seed-user", "3 soat oldin")
            q.upsert("ad-2", AdType.SALE.name, "MacBook Air M1 sotiladi", "Texnika", "9.5 mln",
                "Holati a'lo, 100% batareya sikli past.", "", "seed-user", "kecha")
        }
    }

    private fun seedChat() {
        val q = db.chatQueries
        if (q.countConversations().executeAsOne() > 0) return
        q.transaction {
            q.upsertConversation("c-dilnoza", "Dilnoza Rahimova", "D", ConversationType.PEER.name, true.toDb(), "Konspekt bormi? 😊", "14:22", 2)
            q.upsertConversation("c-sardor", "Sardor Aliyev", "S", ConversationType.PEER.name, false.toDb(), "Rahmat, ko‘rishguncha!", "12:05", 0)
            q.upsertConversation("c-uzumhr", "Uzum Market · HR", "U", ConversationType.HR.name, false.toDb(), "Suhbatga taklif qilamiz", "Kecha", 1)

            // Dilnoza suhbati xabarlari
            q.insertMessage("c-dilnoza-1", "c-dilnoza", "Salom! Diskret matematikadan konspekt bormi? 😊", false.toDb(), "14:20", 1000)
            q.insertMessage("c-dilnoza-2", "c-dilnoza", "Ha, bor! Hozir yuboraman 👍", true.toDb(), "14:21", 2000)
            q.insertMessage("c-dilnoza-3", "c-dilnoza", "Ertaga kutubxonada uchrashamizmi?", false.toDb(), "14:22", 3000)
            q.insertMessage("c-dilnoza-4", "c-dilnoza", "Albatta, soat 10 da 👌", true.toDb(), "14:22", 4000)
        }
    }

    private fun seedNotifications() {
        val q = db.notificationQueries
        if (q.count().executeAsOne() > 0) return
        q.transaction {
            q.insert("nt-1", "Yangi ish taklifi", "Uzum Market — Frontend Intern lavozimiga mos keldingiz.", "JOB", "10 daqiqa oldin", 1, 0)
            q.insert("nt-2", "Chegirma tugayapti", "Chorsu Cafe'dagi 25% chegirma bugun tugaydi.", "DISCOUNT", "2 soat oldin", 2, 0)
            q.insert("nt-3", "Yangi xabar", "Dilnoza Rahimova sizga xabar yozdi.", "CHAT", "3 soat oldin", 3, 0)
            q.insert("nt-4", "E'loningiz ko'rildi", "\"MacBook Air M1\" e'loningizni 12 kishi ko'rdi.", "AD", "kecha", 4, 1)
            q.insert("nt-5", "Xush kelibsiz! 🎉", "StudentClubs'ga xush kelibsiz. Profilingizni to'ldiring.", "SYSTEM", "2 kun oldin", 5, 1)
        }
    }

    private fun seedClubs() {
        val q = db.clubQueries
        if (q.selectAll().executeAsList().isNotEmpty()) return
        q.transaction {
            q.upsert(1, "IT Klub", "Dasturlash, hackathonlar va IT loyihalar jamoasi.", 342, null)
            q.upsert(2, "Debat Klubi", "Mantiqiy fikrlash va notiqlik san'ati.", 128, null)
            q.upsert(3, "Sport Klubi", "Futbol, basketbol va umumjismoniy mashg'ulotlar.", 256, null)
            q.upsert(4, "Volontyorlar", "Ijtimoiy loyihalar va xayriya tadbirlari.", 189, null)
            q.upsert(5, "Dizayn Studiyasi", "UI/UX, grafika va ijodiy ustaxonalar.", 97, null)
            q.upsert(6, "Til Klubi", "Ingliz, koreys va arab tillari amaliyoti.", 214, null)
        }
    }

    private data class Uni(
        val id: String, val name: String, val city: String,
        val monogram: String, val faculty: String?, val accent: Long,
    )
}
