package com.highliuk.manai.ui.reader

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.highliuk.manai.domain.model.ReaderSettings
import com.highliuk.manai.domain.repository.SettingsRepository
import com.highliuk.manai.domain.usecase.GetMangaByIdUseCase
import com.highliuk.manai.domain.usecase.UpdateLastReadPageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

fun spreadCount(pageCount: Int, coverAlone: Boolean): Int {
    if (pageCount <= 0) return 0
    return if (coverAlone) {
        1 + (pageCount - 1 + 1) / 2 // cover + ceil((remaining)/2)
    } else {
        (pageCount + 1) / 2 // ceil(pageCount/2)
    }
}

fun spreadToPages(spreadIndex: Int, pageCount: Int, coverAlone: Boolean): Pair<Int, Int?> {
    if (coverAlone) {
        if (spreadIndex == 0) return Pair(0, null)
        val firstPage = 1 + (spreadIndex - 1) * 2
        val secondPage = firstPage + 1
        return Pair(firstPage, if (secondPage < pageCount) secondPage else null)
    } else {
        val firstPage = spreadIndex * 2
        val secondPage = firstPage + 1
        return Pair(firstPage, if (secondPage < pageCount) secondPage else null)
    }
}

fun pageToSpread(pageIndex: Int, coverAlone: Boolean): Int {
    if (coverAlone) {
        if (pageIndex == 0) return 0
        return 1 + (pageIndex - 1) / 2
    } else {
        return pageIndex / 2
    }
}

sealed interface ReaderUiState {
    data object Loading : ReaderUiState
    data object Error : ReaderUiState
    data class Ready(
        val title: String,
        val filePath: String,
        val pageCount: Int,
        val currentPage: Int,
        val settings: ReaderSettings,
    ) : ReaderUiState
}

@HiltViewModel
class ReaderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getMangaById: GetMangaByIdUseCase,
    private val updateLastReadPage: UpdateLastReadPageUseCase,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val mangaId: Long = checkNotNull(savedStateHandle["mangaId"])

    private val _uiState = MutableStateFlow<ReaderUiState>(ReaderUiState.Loading)
    val uiState: StateFlow<ReaderUiState> = _uiState.asStateFlow()

    private var filePath: String = ""
    private var pageCount: Int = 0
    private var title: String = ""

    companion object {
        const val DEFAULT_RENDER_WIDTH = 1080
    }

    init {
        loadManga()
        observeSettings()
    }

    private fun loadManga() {
        viewModelScope.launch {
            val manga = getMangaById(mangaId)
            if (manga == null) {
                _uiState.value = ReaderUiState.Error
                return@launch
            }
            filePath = manga.filePath
            pageCount = manga.pageCount
            title = manga.title
            val settings = (_uiState.value as? ReaderUiState.Ready)?.settings ?: ReaderSettings()
            _uiState.value = ReaderUiState.Ready(
                title = title,
                filePath = filePath,
                pageCount = pageCount,
                currentPage = manga.lastReadPage,
                settings = settings,
            )
        }
    }

    private fun observeSettings() {
        viewModelScope.launch {
            settingsRepository.getSettings().collect { settings ->
                val current = _uiState.value
                if (current is ReaderUiState.Ready) {
                    _uiState.value = current.copy(settings = settings)
                }
            }
        }
    }

    fun goToNextPage() {
        val current = _uiState.value as? ReaderUiState.Ready ?: return
        val newPage = (current.currentPage + 1).coerceAtMost(pageCount - 1)
        if (newPage == current.currentPage) return
        goToPage(newPage)
    }

    fun goToPreviousPage() {
        val current = _uiState.value as? ReaderUiState.Ready ?: return
        val newPage = (current.currentPage - 1).coerceAtLeast(0)
        if (newPage == current.currentPage) return
        goToPage(newPage)
    }

    fun goToPage(page: Int) {
        val current = _uiState.value as? ReaderUiState.Ready ?: return
        val clampedPage = page.coerceIn(0, pageCount - 1)
        _uiState.value = current.copy(currentPage = clampedPage)
        viewModelScope.launch {
            updateLastReadPage(mangaId, clampedPage)
        }
    }

    fun onVisiblePageChanged(page: Int) {
        val current = _uiState.value as? ReaderUiState.Ready ?: return
        if (page == current.currentPage) return
        goToPage(page)
    }
}
