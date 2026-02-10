@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.highliuk.manai.ui.reader

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.test.click
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests that reproduce the exact layout structure of the reader screen
 * to verify tap and swipe gesture coexistence.
 *
 * The current code uses a sibling overlay (Row with clickable Boxes) on top
 * of a HorizontalPager. This blocks swipe gestures that start in the tap zones.
 *
 * The fix moves tap detection inside each pager page using detectTapGestures,
 * which lets the pager's scrollable (Initial pass) steal drag gestures from
 * the child's tap detector (Main pass).
 */
@RunWith(AndroidJUnit4::class)
class TapNavigationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // -- Tests for the CURRENT (buggy) overlay pattern --
    // These reproduce the exact TapOverlay structure from ReaderScreen.kt

    @Test
    fun overlayWithClickable_swipeFromRightZone_isBlocked() {
        lateinit var pagerState: PagerState
        composeTestRule.setContent {
            pagerState = rememberPagerState(initialPage = 0, pageCount = { 5 })
            Box(modifier = Modifier.fillMaxSize().testTag("reader")) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                ) { page ->
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) { Text("Page $page") }
                }
                // Sibling overlay — exact same pattern as TapOverlay
                Row(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { },
                            ),
                    )
                    Box(modifier = Modifier.weight(1f).fillMaxHeight())
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { },
                            ),
                    )
                }
            }
        }

        // Swipe left starting from the right tap zone
        composeTestRule.onNodeWithTag("reader").performTouchInput {
            swipe(
                start = Offset(width * 0.9f, height * 0.5f),
                end = Offset(width * 0.1f, height * 0.5f),
                durationMillis = 200,
            )
        }
        composeTestRule.waitForIdle()

        // BUG: overlay blocks swipe, page stays at 0
        assertEquals(
            "Clickable overlay blocks swipe from right zone",
            0,
            pagerState.currentPage,
        )
    }

    @Test
    fun overlayWithClickable_swipeFromLeftZone_isBlocked() {
        lateinit var pagerState: PagerState
        composeTestRule.setContent {
            pagerState = rememberPagerState(initialPage = 1, pageCount = { 5 })
            Box(modifier = Modifier.fillMaxSize().testTag("reader")) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                ) { page ->
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) { Text("Page $page") }
                }
                Row(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { },
                            ),
                    )
                    Box(modifier = Modifier.weight(1f).fillMaxHeight())
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { },
                            ),
                    )
                }
            }
        }

        // Swipe right starting from the left tap zone
        composeTestRule.onNodeWithTag("reader").performTouchInput {
            swipe(
                start = Offset(width * 0.1f, height * 0.5f),
                end = Offset(width * 0.9f, height * 0.5f),
                durationMillis = 200,
            )
        }
        composeTestRule.waitForIdle()

        // BUG: overlay blocks swipe, page stays at 1
        assertEquals(
            "Clickable overlay blocks swipe from left zone",
            1,
            pagerState.currentPage,
        )
    }

    // -- Tests for the FIXED pattern --
    // detectTapGestures on page content INSIDE the pager

    @Test
    fun tapInsidePages_swipeFromRightZone_changesPage() {
        lateinit var pagerState: PagerState
        composeTestRule.setContent {
            pagerState = rememberPagerState(initialPage = 0, pageCount = { 5 })
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize().testTag("pager"),
            ) { page ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures { }
                        },
                    contentAlignment = Alignment.Center,
                ) { Text("Page $page") }
            }
        }

        composeTestRule.onNodeWithTag("pager").performTouchInput {
            swipe(
                start = Offset(width * 0.9f, height * 0.5f),
                end = Offset(width * 0.1f, height * 0.5f),
                durationMillis = 200,
            )
        }
        composeTestRule.waitForIdle()

        assertEquals(
            "Swipe from right zone should go to next page",
            1,
            pagerState.currentPage,
        )
    }

    @Test
    fun tapInsidePages_swipeFromLeftZone_changesPage() {
        lateinit var pagerState: PagerState
        composeTestRule.setContent {
            pagerState = rememberPagerState(initialPage = 1, pageCount = { 5 })
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize().testTag("pager"),
            ) { page ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures { }
                        },
                    contentAlignment = Alignment.Center,
                ) { Text("Page $page") }
            }
        }

        composeTestRule.onNodeWithTag("pager").performTouchInput {
            swipe(
                start = Offset(width * 0.1f, height * 0.5f),
                end = Offset(width * 0.9f, height * 0.5f),
                durationMillis = 200,
            )
        }
        composeTestRule.waitForIdle()

        assertEquals(
            "Swipe from left zone should go to previous page",
            0,
            pagerState.currentPage,
        )
    }

    @Test
    fun tapInsidePages_swipeLeft_changesPage() {
        lateinit var pagerState: PagerState
        composeTestRule.setContent {
            pagerState = rememberPagerState(initialPage = 0, pageCount = { 5 })
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize().testTag("pager"),
            ) { page ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures { }
                        },
                    contentAlignment = Alignment.Center,
                ) { Text("Page $page") }
            }
        }

        composeTestRule.onNodeWithTag("pager").performTouchInput {
            swipeLeft()
        }
        composeTestRule.waitForIdle()

        assertEquals(
            "Regular swipe left should go to next page",
            1,
            pagerState.currentPage,
        )
    }

    @Test
    fun tapInsidePages_swipeRight_changesPage() {
        lateinit var pagerState: PagerState
        composeTestRule.setContent {
            pagerState = rememberPagerState(initialPage = 1, pageCount = { 5 })
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize().testTag("pager"),
            ) { page ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures { }
                        },
                    contentAlignment = Alignment.Center,
                ) { Text("Page $page") }
            }
        }

        composeTestRule.onNodeWithTag("pager").performTouchInput {
            swipeRight()
        }
        composeTestRule.waitForIdle()

        assertEquals(
            "Regular swipe right should go to previous page",
            0,
            pagerState.currentPage,
        )
    }

    @Test
    fun tapInsidePages_tapOnLeftZone_firesCallback() {
        var tappedZone = ""
        lateinit var pagerState: PagerState
        composeTestRule.setContent {
            pagerState = rememberPagerState(initialPage = 2, pageCount = { 5 })
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize().testTag("pager"),
            ) { page ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                val third = size.width / 3f
                                tappedZone = when {
                                    offset.x < third -> "left"
                                    offset.x > 2 * third -> "right"
                                    else -> "center"
                                }
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) { Text("Page $page") }
            }
        }

        composeTestRule.onNodeWithTag("pager").performTouchInput {
            click(Offset(width * 0.1f, height * 0.5f))
        }
        composeTestRule.waitForIdle()

        assertEquals("Tap on left zone detected", "left", tappedZone)
        assertEquals("Tap should not change page", 2, pagerState.currentPage)
    }

    @Test
    fun tapInsidePages_tapOnRightZone_firesCallback() {
        var tappedZone = ""
        lateinit var pagerState: PagerState
        composeTestRule.setContent {
            pagerState = rememberPagerState(initialPage = 2, pageCount = { 5 })
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize().testTag("pager"),
            ) { page ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                val third = size.width / 3f
                                tappedZone = when {
                                    offset.x < third -> "left"
                                    offset.x > 2 * third -> "right"
                                    else -> "center"
                                }
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) { Text("Page $page") }
            }
        }

        composeTestRule.onNodeWithTag("pager").performTouchInput {
            click(Offset(width * 0.9f, height * 0.5f))
        }
        composeTestRule.waitForIdle()

        assertEquals("Tap on right zone detected", "right", tappedZone)
        assertEquals("Tap should not change page", 2, pagerState.currentPage)
    }

    @Test
    fun tapInsidePages_tapOnCenterZone_doesNothing() {
        var tappedZone = ""
        lateinit var pagerState: PagerState
        composeTestRule.setContent {
            pagerState = rememberPagerState(initialPage = 2, pageCount = { 5 })
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize().testTag("pager"),
            ) { page ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                val third = size.width / 3f
                                tappedZone = when {
                                    offset.x < third -> "left"
                                    offset.x > 2 * third -> "right"
                                    else -> "center"
                                }
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) { Text("Page $page") }
            }
        }

        composeTestRule.onNodeWithTag("pager").performTouchInput {
            click(Offset(width * 0.5f, height * 0.5f))
        }
        composeTestRule.waitForIdle()

        assertEquals("Center zone detected", "center", tappedZone)
        assertEquals("Center tap should not change page", 2, pagerState.currentPage)
    }
}
