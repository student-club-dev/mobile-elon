package dev.core.database

import app.cash.sqldelight.db.SqlDriver

/** Platformaga xos SQL drayver yaratuvchi (Android: AndroidSqliteDriver, iOS: NativeSqliteDriver). */
expect class DriverFactory {
    fun createDriver(): SqlDriver
}
