package com.highliuk.manai.ui.reader

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.highliuk.manai.domain.model.Manga
import com.highliuk.manai.domain.repository.MangaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ReaderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    repository: MangaRepository
) : ViewModel() {

    private val mangaId: Long = savedStateHandle["mangaId"] ?: 0L

    val manga: StateFlow<Manga?> = repository.getMangaById(mangaId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    fun onPageChanged(page: Int) {
        _currentPage.value = page
    }
}
