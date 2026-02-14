package com.highliuk.manai.ui.settings

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import androidx.compose.ui.test.onNodeWithContentDescription

class SettingsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun displaysGridColumnsRadioButtons() {
        composeTestRule.setContent {
            SettingsScreen(
                gridColumns = 2,
                onGridColumnsChange = {},
                onBack = {}
            )
        }

        composeTestRule.onNodeWithText("Grid Columns").assertIsDisplayed()
        composeTestRule.onNodeWithText("2 columns").assertIsDisplayed()
        composeTestRule.onNodeWithText("3 columns").assertIsDisplayed()
    }

    @Test
    fun selecting3ColumnsCallsCallback() {
        var selectedColumns = 0

        composeTestRule.setContent {
            SettingsScreen(
                gridColumns = 2,
                onGridColumnsChange = { selectedColumns = it },
                onBack = {}
            )
        }

        composeTestRule.onNodeWithText("3 columns").performClick()

        assertEquals(3, selectedColumns)
    }

    @Test
    fun backArrowCallsOnBack() {
        var backCalled = false

        composeTestRule.setContent {
            SettingsScreen(
                gridColumns = 2,
                onGridColumnsChange = {},
                onBack = { backCalled = true }
            )
        }

        composeTestRule.onNodeWithText("Settings").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Back").performClick()

        assertTrue(backCalled)
    }
}
