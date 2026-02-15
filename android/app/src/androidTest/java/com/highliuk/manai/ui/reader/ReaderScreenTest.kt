package com.highliuk.manai.ui.reader

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTopPositionInRootIsEqualTo
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import com.highliuk.manai.domain.model.Manga
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ReaderScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testManga = Manga(id = 1, uri = "content://test", title = "One Piece", pageCount = 10)

    @Test
    fun displaysMangaTitleInTopBar() {
        composeTestRule.setContent {
            ReaderScreen(
                manga = testManga,
                currentPage = 0,
                onPageChanged = {},
                onBack = {},
                onSettingsClick = {}
            )
        }

        composeTestRule.onNodeWithText("One Piece").assertIsDisplayed()
    }

    @Test
    fun backButton_callsOnBack() {
        var backCalled = false

        composeTestRule.setContent {
            ReaderScreen(
                manga = testManga,
                currentPage = 0,
                onPageChanged = {},
                onBack = { backCalled = true },
                onSettingsClick = {}
            )
        }

        composeTestRule.onNodeWithContentDescription("Back").performClick()
        assertTrue(backCalled)
    }

    @Test
    fun settingsButton_callsOnSettingsClick() {
        var settingsCalled = false

        composeTestRule.setContent {
            ReaderScreen(
                manga = testManga,
                currentPage = 0,
                onPageChanged = {},
                onBack = {},
                onSettingsClick = { settingsCalled = true }
            )
        }

        composeTestRule.onNodeWithContentDescription("Reader settings").performClick()
        assertTrue(settingsCalled)
    }

    @Test
    fun pagerStartsAtTopOfScreen_overlayTopBar() {
        composeTestRule.setContent {
            ReaderScreen(
                manga = testManga,
                currentPage = 0,
                onPageChanged = {},
                onBack = {},
                onSettingsClick = {}
            )
        }

        composeTestRule.onNodeWithTag("reader_pager")
            .assertTopPositionInRootIsEqualTo(0.dp)
    }
}
