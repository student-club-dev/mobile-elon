package dev.feature.discounts.domain.model

import kotlin.jvm.JvmInline

/**
 * Biznes turi — backend katalogidagi **kalit** (`"TENNIS"`, `"CLOTHING"`, `"SOMSA"` …).
 *
 * Nega enum emas: turlar ro'yxati **serverda** turadi va adminkadan o'zgaradi
 * (`GET /v1/business/types`). Enum bo'lganda ilova o'zi bilmagan turni jimgina tashlab
 * yuborardi — foydalanuvchi serverdagi 27 turdan faqat bir nechtasini ko'rardi, teskarisi
 * ham bo'lardi: ilovada bor, lekin serverda yo'q tur tanlanib, e'lon rad etilardi.
 *
 * Ochiq kalit bilan bunday bo'lmaydi: nomi, rangi, narx birliklari — hammasi javobning
 * o'zidan keladi ([BusinessTypeInfo]), ilova esa faqat kalitni uzatadi. Internet yo'q
 * bo'lganda [ListingCatalog] shu kalitlar bo'yicha zaxira ma'lumot beradi.
 *
 * E'lon yaratilganda tanlanadi va **keyin o'zgarmaydi** (backendda ham `BUSINESS_TYPE_IMMUTABLE`).
 */
@JvmInline
value class BusinessType(val key: String) {

    override fun toString(): String = key

    companion object {
        /**
         * Ilova **maxsus** ishlov beradigan turlar. Qolganlari uchun kalit shunchaki
         * serverdan kelgan matn bo'lib qolaveradi — bu yerda sanab chiqish shart emas.
         */
        val CLOTHING = BusinessType("CLOTHING")
        val BARBERSHOP = BusinessType("BARBERSHOP")
        val BEAUTY_SALON = BusinessType("BEAUTY_SALON")
    }
}

/** Foydalanuvchi jinsi — profildan olinadi, biznes turlari/kategoriyalarni moslaydi. */
enum class Gender { MALE, FEMALE }
