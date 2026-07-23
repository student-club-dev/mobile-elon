package dev.core.domain

/**
 * **Local test rejimi** — backendsiz to'liq ishlaydi (barcha funksiyani sinash uchun).
 * Yoqilganda: katalog/biznes/chegirma local DB'dan (seed ma'lumot) o'qiladi.
 *
 * Backend ulangач `false` — Api* repository'lar ishlaydi (`https://api.studentclub.uz/v1/`).
 * Diqqat: kirish/ro'yxatdan o'tish bu bayroqqa BOG'LIQ EMAS — auth har doim backendда.
 */
const val USE_LOCAL_DATA = false
