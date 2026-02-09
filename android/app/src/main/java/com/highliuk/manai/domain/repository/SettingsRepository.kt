package com.highliuk.manai.domain.repository

import com.highliuk.manai.domain.model.ReadingDirection
import com.highliuk.manai.domain.model.ReadingMode
import com.highliuk.manai.domain.model.ReaderSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getSettings(): Flow<ReaderSettings>
    suspend fun updateReadingMode(mode: ReadingMode)
    suspend fun updateReadingDirection(direction: ReadingDirection)
    suspend fun updateTapNavigationEnabled(enabled: Boolean)
    suspend fun updateCoverAlone(enabled: Boolean)
}
