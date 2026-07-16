package dev.core.database

import dev.core.database.sql.StudentClubsDatabase

object DatabaseFactory {
    fun create(driverFactory: DriverFactory): StudentClubsDatabase =
        StudentClubsDatabase(driverFactory.createDriver())
}
