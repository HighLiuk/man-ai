package com.highliuk.manai.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.highliuk.manai.domain.model.ReadingDirection
import com.highliuk.manai.domain.model.ReadingMode
import com.highliuk.manai.domain.model.ReaderSettings
import com.highliuk.manai.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : SettingsRepository {

    private companion object {
        val READING_MODE = stringPreferencesKey("reading_mode")
        val READING_DIRECTION = stringPreferencesKey("reading_direction")
        val TAP_NAVIGATION = booleanPreferencesKey("tap_navigation")
        val COVER_ALONE = booleanPreferencesKey("cover_alone")
    }

    override fun getSettings(): Flow<ReaderSettings> = dataStore.data.map { prefs ->
        ReaderSettings(
            readingMode = prefs[READING_MODE]?.let { ReadingMode.valueOf(it) }
                ?: ReadingMode.SINGLE_PAGE,
            readingDirection = prefs[READING_DIRECTION]?.let { ReadingDirection.valueOf(it) }
                ?: ReadingDirection.RTL,
            tapNavigationEnabled = prefs[TAP_NAVIGATION] ?: true,
            coverAlone = prefs[COVER_ALONE] ?: true,
        )
    }

    override suspend fun updateReadingMode(mode: ReadingMode) {
        dataStore.edit { it[READING_MODE] = mode.name }
    }

    override suspend fun updateReadingDirection(direction: ReadingDirection) {
        dataStore.edit { it[READING_DIRECTION] = direction.name }
    }

    override suspend fun updateTapNavigationEnabled(enabled: Boolean) {
        dataStore.edit { it[TAP_NAVIGATION] = enabled }
    }

    override suspend fun updateCoverAlone(enabled: Boolean) {
        dataStore.edit { it[COVER_ALONE] = enabled }
    }
}
