package com.highliuk.manai.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.highliuk.manai.domain.model.ReadingDirection
import com.highliuk.manai.domain.model.ReadingMode
import com.highliuk.manai.domain.model.ReaderSettings
import com.highliuk.manai.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    val settings: StateFlow<ReaderSettings> = settingsRepository.getSettings()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ReaderSettings(),
        )

    fun updateReadingMode(mode: ReadingMode) {
        viewModelScope.launch { settingsRepository.updateReadingMode(mode) }
    }

    fun updateReadingDirection(direction: ReadingDirection) {
        viewModelScope.launch { settingsRepository.updateReadingDirection(direction) }
    }

    fun updateTapNavigationEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.updateTapNavigationEnabled(enabled) }
    }

    fun updateCoverAlone(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.updateCoverAlone(enabled) }
    }
}
