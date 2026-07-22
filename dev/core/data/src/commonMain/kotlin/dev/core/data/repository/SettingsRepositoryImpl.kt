package dev.core.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import dev.core.common.AppDispatchers
import dev.core.database.sql.ElonUzDatabase
import dev.core.domain.model.AppLanguage
import dev.core.domain.model.ThemeMode
import dev.core.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

private const val KEY_THEME_MODE = "theme_mode"
private const val KEY_LANGUAGE = "app_language"

/** [SettingsRepository] — SQLDelight `AppSettingEntity` (kalit/qiymat) ustidagi implementatsiya. */
class SettingsRepositoryImpl(
    private val db: ElonUzDatabase,
    private val dispatchers: AppDispatchers,
) : SettingsRepository {

    private val q get() = db.appSettingQueries

    override fun observeThemeMode(): Flow<ThemeMode> =
        q.selectByKey(KEY_THEME_MODE).asFlow().mapToOneOrNull(dispatchers.io).map { value ->
            ThemeMode.fromName(value)
        }

    override suspend fun setThemeMode(mode: ThemeMode) = withContext(dispatchers.io) {
        q.upsert(KEY_THEME_MODE, mode.name)
    }

    override fun observeLanguage(): Flow<AppLanguage> =
        q.selectByKey(KEY_LANGUAGE).asFlow().mapToOneOrNull(dispatchers.io).map { value ->
            AppLanguage.fromName(value)
        }

    override suspend fun setLanguage(language: AppLanguage) = withContext(dispatchers.io) {
        q.upsert(KEY_LANGUAGE, language.name)
    }

    override fun observeFlag(key: String, default: Boolean): Flow<Boolean> =
        q.selectByKey(key).asFlow().mapToOneOrNull(dispatchers.io).map { value ->
            value?.let { it.toBooleanStrictOrNull() } ?: default
        }

    override suspend fun setFlag(key: String, value: Boolean) = withContext(dispatchers.io) {
        q.upsert(key, value.toString())
    }

    override fun observeValue(key: String): Flow<String?> =
        q.selectByKey(key).asFlow().mapToOneOrNull(dispatchers.io)

    override suspend fun setValue(key: String, value: String?) = withContext(dispatchers.io) {
        if (value == null) q.deleteByKey(key) else q.upsert(key, value)
    }
}
