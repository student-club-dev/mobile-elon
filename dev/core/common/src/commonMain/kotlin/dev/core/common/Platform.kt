package dev.core.common

expect val platformName: String

/**
 * Qurilma nomi — backend'ga sessiya yozuvi uchun uzatiladi (`deviceName`), foydalanuvchi
 * "Faol qurilmalar" ro'yxatida aynan shu matnni ko'radi.
 */
expect val deviceName: String
