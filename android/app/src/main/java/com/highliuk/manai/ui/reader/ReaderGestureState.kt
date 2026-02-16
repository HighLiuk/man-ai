package com.highliuk.manai.ui.reader

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class ReaderGestureState {
    var isTopBarVisible by mutableStateOf(false)
        private set

    fun toggleTopBar() {
        isTopBarVisible = !isTopBarVisible
    }
}
