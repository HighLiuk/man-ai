package com.highliuk.manai.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AppLanguageTest {
    @Test
    fun `SYSTEM has null tag`() {
        assertNull(AppLanguage.SYSTEM.tag)
    }

    @Test
    fun `ENGLISH has en tag`() {
        assertEquals("en", AppLanguage.ENGLISH.tag)
    }

    @Test
    fun `ITALIAN has it tag`() {
        assertEquals("it", AppLanguage.ITALIAN.tag)
    }

    @Test
    fun `has exactly 3 entries`() {
        assertEquals(3, AppLanguage.entries.size)
    }
}
