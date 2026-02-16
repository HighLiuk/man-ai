package com.highliuk.manai.ui.reader

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReaderGestureStateTest {

    @Test
    fun `top bar is hidden by default`() {
        val state = ReaderGestureState()
        assertFalse(state.isTopBarVisible)
    }

    @Test
    fun `toggleTopBar shows top bar when hidden`() {
        val state = ReaderGestureState()
        state.toggleTopBar()
        assertTrue(state.isTopBarVisible)
    }

    @Test
    fun `toggleTopBar hides top bar when visible`() {
        val state = ReaderGestureState()
        state.toggleTopBar()
        state.toggleTopBar()
        assertFalse(state.isTopBarVisible)
    }
}
