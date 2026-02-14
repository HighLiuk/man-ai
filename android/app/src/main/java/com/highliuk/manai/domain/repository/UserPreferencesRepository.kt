package com.highliuk.manai.domain.repository

import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val gridColumns: Flow<Int>
    suspend fun setGridColumns(columns: Int)
}
