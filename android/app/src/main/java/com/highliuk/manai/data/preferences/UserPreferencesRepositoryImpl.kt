package com.highliuk.manai.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.highliuk.manai.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserPreferencesRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : UserPreferencesRepository {

    private companion object {
        val GRID_COLUMNS = intPreferencesKey("grid_columns")
        const val DEFAULT_GRID_COLUMNS = 2
        const val MIN_GRID_COLUMNS = 2
        const val MAX_GRID_COLUMNS = 3
    }

    override val gridColumns: Flow<Int> = dataStore.data.map { preferences ->
        preferences[GRID_COLUMNS] ?: DEFAULT_GRID_COLUMNS
    }

    override suspend fun setGridColumns(columns: Int) {
        val clamped = columns.coerceIn(MIN_GRID_COLUMNS, MAX_GRID_COLUMNS)
        dataStore.edit { preferences ->
            preferences[GRID_COLUMNS] = clamped
        }
    }
}
