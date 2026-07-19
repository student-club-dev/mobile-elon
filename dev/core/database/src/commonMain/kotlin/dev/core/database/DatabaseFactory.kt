package dev.core.database

import dev.core.database.sql.ElonUzDatabase

object DatabaseFactory {
    fun create(driverFactory: DriverFactory): ElonUzDatabase =
        ElonUzDatabase(driverFactory.createDriver())
}
