package dev.core.database

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import dev.core.database.sql.ElonUzDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Host (JVM) testlari — SQLDelight sxemasi, migratsiya zanjiri va yangi jadvallar CRUD'ini
 * real SQLite engine'da tekshiradi. SQL mantiqi Android va iOS'da bir xil bo'lgani uchun
 * bu ikkala platforma uchun ham amal qiladi.
 */
class DatabaseSchemaTest {

    private fun freshDriver() = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)

    /**
     * v1 bazasini taqlid qiladi — migratsiya zanjiri tegadigan jadvallar:
     * ClubEntity (3.sqm `joined` qo'shadi, 11.sqm butunlay tashlaydi),
     * ConversationEntity (4.sqm `archived` qo'shadi),
     * UserEntity (5.sqm profilni ajratib oladi — profil ustunlari hali ichida).
     */
    private fun createV1Tables(driver: JdbcSqliteDriver) {
        driver.execute(
            null,
            """
            CREATE TABLE ClubEntity (
                id INTEGER NOT NULL PRIMARY KEY,
                name TEXT NOT NULL,
                description TEXT NOT NULL,
                membersCount INTEGER NOT NULL,
                imageUrl TEXT
            )
            """.trimIndent(),
            0,
        )
        driver.execute(
            null,
            """
            CREATE TABLE ConversationEntity (
                id TEXT NOT NULL PRIMARY KEY,
                peerName TEXT NOT NULL,
                peerInitial TEXT NOT NULL,
                type TEXT NOT NULL,
                online INTEGER NOT NULL,
                lastMessage TEXT NOT NULL,
                lastTime TEXT NOT NULL,
                unreadCount INTEGER NOT NULL
            )
            """.trimIndent(),
            0,
        )
        driver.execute(
            null,
            """
            CREATE TABLE UserEntity (
                uid TEXT NOT NULL PRIMARY KEY,
                userId INTEGER NOT NULL,
                fullName TEXT NOT NULL,
                email TEXT NOT NULL,
                role TEXT NOT NULL,
                phoneNumber TEXT,
                photoUrl TEXT,
                firstName TEXT,
                lastName TEXT,
                universityId TEXT,
                universityEmail TEXT,
                birthYear INTEGER,
                courseYear TEXT,
                profileRole TEXT
            )
            """.trimIndent(),
            0,
        )
    }

    @Test
    fun schemaVersionIsEleven() {
        // 7.sqm — ListingEntity (chegirma e'lonlari), 8.sqm — ko'p filial (branchesJson),
        // 9.sqm — biznes egasi profili (businessName/businessType), 10.sqm — profil emaili.
        assertEquals(12L, ElonUzDatabase.Schema.version)
    }

    @Test
    fun freshSchemaCreatesAllTablesAndCrudWorks() {
        val driver = freshDriver()
        ElonUzDatabase.Schema.create(driver)
        val db = ElonUzDatabase(driver)

        // AppSetting (C5)
        db.appSettingQueries.upsert("theme_mode", "DARK")
        assertEquals("DARK", db.appSettingQueries.selectByKey("theme_mode").executeAsOne())

        // Notification (C1)
        db.notificationQueries.insert("n1", "Sarlavha", "Matn", "JOB", "hozir", 1, 0)
        db.notificationQueries.insert("n2", "Sarlavha 2", "Matn 2", "CHAT", "hozir", 2, 1)
        assertEquals(1L, db.notificationQueries.countUnread().executeAsOne())
        db.notificationQueries.markAllRead()
        assertEquals(0L, db.notificationQueries.countUnread().executeAsOne())

        // Profile (feature:profile) — sessiyadan ajratilgan profil keshi
        db.profileQueries.upsert(
            uid = "uid-1",
            firstName = "Quvonchbek",
            lastName = "G'afurov",
            phoneNumber = "+998901234567",
            role = "STUDENT",
            universityId = "tuit",
            universityEmail = null,
            birthYear = 2004L,
            courseYear = "3",
            avatarUrl = "https://cdn.elon.uz/avatars/uid-1.jpg",
            businessName = null,
            businessType = null,
            email = "quvonchbek@example.com",
        )
        val profile = db.profileQueries.selectCurrent().executeAsOne()
        assertEquals("Quvonchbek", profile.firstName)
        assertEquals("tuit", profile.universityId)
        assertEquals(2004L, profile.birthYear)
        assertEquals("https://cdn.elon.uz/avatars/uid-1.jpg", profile.avatarUrl)
        assertEquals("quvonchbek@example.com", profile.email)

        db.profileQueries.clear()
        assertNull(db.profileQueries.selectCurrent().executeAsOneOrNull())

        driver.close()
    }

    @Test
    fun migrationFromV1AddsNewTablesAndColumn() {
        val driver = freshDriver()
        createV1Tables(driver)
        // Profili to'ldirilgan mavjud foydalanuvchi — v6 da ProfileEntity'ga ko'chishi kerak.
        driver.execute(
            null,
            """
            INSERT INTO UserEntity(
                uid, userId, fullName, email, role, phoneNumber, photoUrl,
                firstName, lastName, universityId, universityEmail, birthYear, courseYear, profileRole
            ) VALUES (
                'uid-1', 7, 'Eski Foydalanuvchi', 'a@b.uz', 'STUDENT', '+998901234567', NULL,
                'Quvonchbek', 'G''afurov', 'tuit', NULL, 2004, '3', 'STUDENT'
            )
            """.trimIndent(),
            0,
        )

        // 1.sqm (AppSetting), 2.sqm (Notification), 3.sqm (Club.joined),
        // 4.sqm (Chat.archived), 5.sqm (ProfileEntity ajratish), 6.sqm (avatarUrl),
        // 11.sqm (ClubEntity tashlanadi) — hammasi ishga tushadi.
        ElonUzDatabase.Schema.migrate(driver, 1L, ElonUzDatabase.Schema.version)
        val db = ElonUzDatabase(driver)

        // Migratsiyadan keyin yangi jadvallar mavjud bo'lishi kerak.
        db.appSettingQueries.upsert("k", "v")
        assertEquals("v", db.appSettingQueries.selectByKey("k").executeAsOne())

        db.notificationQueries.insert("n1", "T", "B", "SYSTEM", "hozir", 1, 0)
        assertEquals(1L, db.notificationQueries.count().executeAsOne())

        // v12: Klublar vertikali olib tashlangan — 11.sqm ClubEntity'ni tashlagan bo'lishi kerak.
        assertEquals(
            0L,
            driver.executeQuery(
                null,
                "SELECT count(*) FROM sqlite_master WHERE type = 'table' AND name = 'ClubEntity'",
                { cursor -> app.cash.sqldelight.db.QueryResult.Value(if (cursor.next().value) cursor.getLong(0) else 0L) },
                0,
            ).value,
        )

        // v6: profil UserEntity'dan ProfileEntity'ga ko'chgan bo'lishi kerak...
        val profile = db.profileQueries.selectCurrent().executeAsOne()
        assertEquals("uid-1", profile.uid)
        assertEquals("Quvonchbek", profile.firstName)
        assertEquals("tuit", profile.universityId)
        assertEquals("STUDENT", profile.role) // eski `profileRole` ustunidan
        assertEquals(2004L, profile.birthYear)
        assertNull(profile.avatarUrl) // v7 da qo'shilgan ustun — eski yozuvlarda bo'sh

        // ...sessiya esa UserEntity'da saqlanib qolgan (profil ustunlarisiz).
        val user = db.userQueries.selectCurrent().executeAsOne()
        assertEquals("uid-1", user.uid)
        assertEquals("Eski Foydalanuvchi", user.fullName)
        assertEquals(7L, user.userId)

        // v8/v9 (7.sqm + 8.sqm): chegirma e'lonlari jadvali migratsiyadan keyin mavjud
        // va ko'p filialli (branchesJson) ustunga ega bo'lishi kerak.
        db.listingQueries.selectAll().executeAsList().also { assertEquals(0, it.size) }

        driver.close()
    }

    /**
     * 8.sqm — eng nozik migratsiya: v8 dagi yagona manzil ustunlari (lat/lng/address)
     * bitta filialga aylanib `branchesJson` ga ko'chishi kerak. Koordinatasiz e'lon esa
     * filialsiz qoladi (egasi uni xaritadan qayta belgilaydi).
     */
    @Test
    fun migrationV8ConvertsSingleLocationIntoBranch() {
        val driver = freshDriver()

        // v8 dagi ListingEntity (eski shakl — yagona lokatsiya ustunlari bilan).
        driver.execute(
            null,
            """
            CREATE TABLE ListingEntity (
                id TEXT NOT NULL PRIMARY KEY,
                ownerId TEXT NOT NULL,
                businessId TEXT,
                businessType TEXT NOT NULL,
                businessName TEXT NOT NULL,
                categoryKey TEXT NOT NULL,
                customCategoryName TEXT,
                title TEXT NOT NULL,
                description TEXT,
                imagesJson TEXT NOT NULL DEFAULT '[]',
                priceUnit TEXT NOT NULL,
                originalPrice INTEGER NOT NULL,
                currency TEXT NOT NULL DEFAULT 'UZS',
                discountType TEXT NOT NULL,
                discountValue INTEGER NOT NULL,
                finalPrice INTEGER NOT NULL,
                discountConditions TEXT,
                redemptionMethod TEXT NOT NULL,
                promoCode TEXT,
                perUserLimit INTEGER,
                totalLimit INTEGER,
                usedCount INTEGER NOT NULL DEFAULT 0,
                regionId TEXT,
                districtId TEXT,
                address TEXT,
                landmark TEXT,
                lat REAL,
                lng REAL,
                validFrom INTEGER NOT NULL,
                validTo INTEGER NOT NULL,
                attributesJson TEXT NOT NULL DEFAULT '{}',
                optionGroupsJson TEXT NOT NULL DEFAULT '[]',
                status TEXT NOT NULL,
                rejectionReason TEXT,
                viewsCount INTEGER NOT NULL DEFAULT 0,
                createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL
            )
            """.trimIndent(),
            0,
        )

        // v8 dagi ProfileEntity — keyingi migratsiyalar (9.sqm, 10.sqm) shu jadvalga
        // ustun qo'shadi, jadvalsiz migratsiya yiqiladi.
        driver.execute(
            null,
            """
            CREATE TABLE ProfileEntity (
                uid TEXT NOT NULL PRIMARY KEY,
                firstName TEXT,
                lastName TEXT,
                phoneNumber TEXT,
                role TEXT,
                universityId TEXT,
                universityEmail TEXT,
                birthYear INTEGER,
                courseYear TEXT,
                avatarUrl TEXT
            )
            """.trimIndent(),
            0,
        )

        // Koordinatasi bor e'lon — filialga aylanishi kerak.
        driver.execute(
            null,
            """
            INSERT INTO ListingEntity(
                id, ownerId, businessType, businessName, categoryKey, title,
                priceUnit, originalPrice, discountType, discountValue, finalPrice,
                redemptionMethod, regionId, districtId, address, lat, lng,
                validFrom, validTo, status, createdAt, updatedAt
            ) VALUES (
                'l-1', 'u1', 'CAFE_RESTAURANT', 'Navruz', 'PIZZA', 'Pepperoni',
                'PER_ITEM', 55000, 'PERCENT', 20, 44000,
                'QR', 'TOSHKENT_SHAHRI', 'CHILONZOR', 'Chilonzor 9-kvartal, 42-uy', 41.2856, 69.2034,
                0, 9999999999999, 'ACTIVE', 0, 0
            )
            """.trimIndent(),
            0,
        )

        // Koordinatasiz e'lon — filialsiz qolishi kerak.
        driver.execute(
            null,
            """
            INSERT INTO ListingEntity(
                id, ownerId, businessType, businessName, categoryKey, title,
                priceUnit, originalPrice, discountType, discountValue, finalPrice,
                redemptionMethod, address, validFrom, validTo, status, createdAt, updatedAt
            ) VALUES (
                'l-2', 'u1', 'CAFE_RESTAURANT', 'Korzinka', 'PIZZA', 'Pitsa',
                'PER_ITEM', 12000, 'PERCENT', 10, 10800,
                'STUDENT_ID', 'Qayerdadir', 0, 9999999999999, 'DRAFT', 0, 0
            )
            """.trimIndent(),
            0,
        )

        ElonUzDatabase.Schema.migrate(driver, 8L, ElonUzDatabase.Schema.version)
        val db = ElonUzDatabase(driver)

        val withBranch = db.listingQueries.selectById("l-1").executeAsOne()
        assertTrue(withBranch.branchesJson.contains("41.2856"), "lat ko'chmadi: ${withBranch.branchesJson}")
        assertTrue(withBranch.branchesJson.contains("69.2034"), "lng ko'chmadi: ${withBranch.branchesJson}")
        assertTrue(withBranch.branchesJson.contains("Chilonzor 9-kvartal, 42-uy"), "manzil ko'chmadi")
        assertTrue(withBranch.branchesJson.contains("CHILONZOR"), "tuman ko'chmadi")

        val withoutBranch = db.listingQueries.selectById("l-2").executeAsOne()
        assertEquals("[]", withoutBranch.branchesJson)

        driver.close()
    }

    /** Chegirma e'loni (feature:discounts) — yozish, faol e'lonlarni tanlash, status, o'chirish. */
    @Test
    fun listingCrudAndActiveFilterWork() {
        val driver = freshDriver()
        ElonUzDatabase.Schema.create(driver)
        val db = ElonUzDatabase(driver)
        val q = db.listingQueries

        fun insert(id: String, status: String, validTo: Long) = q.upsert(
            id = id,
            ownerId = "u1",
            businessId = null,
            businessType = "CAFE_RESTAURANT",
            businessName = "Chaykhana Navruz",
            categoryKey = "PIZZA",
            customCategoryName = null,
            title = "Pepperoni pitsa",
            description = null,
            imagesJson = """["data:image/jpeg;base64,AAA"]""",
            priceUnit = "PER_ITEM",
            originalPrice = 55_000,
            currency = "UZS",
            discountType = "PERCENT",
            discountValue = 20,
            finalPrice = 44_000,
            discountConditions = null,
            redemptionMethod = "QR",
            promoCode = null,
            perUserLimit = 1,
            totalLimit = null,
            usedCount = 0,
            branchesJson = """[{"id":"br1","lat":41.2856,"lng":69.2034,"address":"Chilonzor 9-kvartal, 42-uy"}]""",
            validFrom = 0,
            validTo = validTo,
            attributesJson = """{"isHalal":"true"}""",
            optionGroupsJson = "[]",
            status = status,
            rejectionReason = null,
            viewsCount = 0,
            createdAt = 0,
            updatedAt = 0,
        )

        insert("l-active", "ACTIVE", validTo = 2_000)
        insert("l-draft", "DRAFT", validTo = 2_000)
        insert("l-expired", "ACTIVE", validTo = 500) // muddati o'tgan

        assertEquals(3, q.selectByOwner("u1").executeAsList().size)
        assertEquals(44_000L, q.selectById("l-active").executeAsOne().finalPrice)

        // Talabaga faqat ACTIVE va muddati o'tmaganlari ko'rinadi.
        val visible = q.selectActive(now = 1_000).executeAsList()
        assertEquals(1, visible.size)
        assertEquals("l-active", visible.single().id)

        q.updateStatus(status = "PAUSED", updatedAt = 5, id = "l-active")
        assertTrue(q.selectActive(now = 1_000).executeAsList().isEmpty())

        q.deleteById("l-draft")
        assertEquals(2, q.selectByOwner("u1").executeAsList().size)

        driver.close()
    }

    @Test
    fun migrationSkipsEmptyProfileRows() {
        val driver = freshDriver()
        createV1Tables(driver)
        // Profili to'ldirilmagan foydalanuvchi — bo'sh ProfileEntity qatori YARATILMASLIGI kerak,
        // aks holda `hasProfile()` noto'g'ri `true` qaytaradi.
        driver.execute(
            null,
            """
            INSERT INTO UserEntity(uid, userId, fullName, email, role, phoneNumber, photoUrl)
            VALUES ('uid-2', 9, 'Profilsiz', 'c@d.uz', 'STUDENT', NULL, NULL)
            """.trimIndent(),
            0,
        )

        ElonUzDatabase.Schema.migrate(driver, 1L, ElonUzDatabase.Schema.version)
        val db = ElonUzDatabase(driver)

        assertNull(db.profileQueries.selectCurrent().executeAsOneOrNull())
        assertEquals("Profilsiz", db.userQueries.selectCurrent().executeAsOne().fullName)

        driver.close()
    }

    @Test
    fun seedInsertIsIdempotentByPrimaryKey() {
        val driver = freshDriver()
        ElonUzDatabase.Schema.create(driver)
        val db = ElonUzDatabase(driver)

        // Bir xil id bilan ikki marta — INSERT OR REPLACE dublikat yaratmasligi kerak.
        db.universityQueries.upsert("tuit", "TATU", "Toshkent", "TU", null, 1L)
        db.universityQueries.upsert("tuit", "TATU yangilangan", "Toshkent", "TU", null, 2L)
        assertEquals(1, db.universityQueries.selectAll().executeAsList().size)
        assertTrue(db.universityQueries.selectAll().executeAsOne().name == "TATU yangilangan")

        driver.close()
    }
}
