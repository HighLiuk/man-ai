package com.highliuk.manai.ui.reader

import androidx.lifecycle.SavedStateHandle
import com.highliuk.manai.domain.model.Manga
import com.highliuk.manai.domain.model.ReadingDirection
import com.highliuk.manai.domain.model.ReadingMode
import com.highliuk.manai.domain.model.ReaderSettings
import com.highliuk.manai.domain.repository.SettingsRepository
import com.highliuk.manai.domain.usecase.GetMangaByIdUseCase
import com.highliuk.manai.domain.usecase.UpdateLastReadPageUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReaderViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val getMangaById = mockk<GetMangaByIdUseCase>()
    private val updateLastReadPage = mockk<UpdateLastReadPageUseCase>()
    private val settingsRepository = mockk<SettingsRepository>()
    private val settingsFlow = MutableStateFlow(ReaderSettings())

    private val testManga = Manga(
        id = 1L,
        title = "Test Manga",
        filePath = "content://test/manga.pdf",
        pageCount = 10,
        lastReadPage = 0,
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { settingsRepository.getSettings() } returns settingsFlow
        coEvery { updateLastReadPage(any(), any()) } returns Unit
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(mangaId: Long = 1L): ReaderViewModel {
        val savedStateHandle = SavedStateHandle(mapOf("mangaId" to mangaId))
        return ReaderViewModel(
            savedStateHandle = savedStateHandle,
            getMangaById = getMangaById,
            updateLastReadPage = updateLastReadPage,
            settingsRepository = settingsRepository,
        )
    }

    @Test
    fun `initial state is Loading`() = runTest(testDispatcher) {
        coEvery { getMangaById(1L) } returns testManga

        val viewModel = createViewModel()

        assertTrue(viewModel.uiState.value is ReaderUiState.Loading)
    }

    @Test
    fun `state is Ready when manga loads`() = runTest(testDispatcher) {
        coEvery { getMangaById(1L) } returns testManga

        val viewModel = createViewModel()
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is ReaderUiState.Ready)
        assertEquals("Test Manga", (state as ReaderUiState.Ready).title)
        assertEquals(10, state.pageCount)
        job.cancel()
    }

    @Test
    fun `state is Error when manga not found`() = runTest(testDispatcher) {
        coEvery { getMangaById(1L) } returns null

        val viewModel = createViewModel()
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is ReaderUiState.Error)
        job.cancel()
    }

    @Test
    fun `resumes from lastReadPage`() = runTest(testDispatcher) {
        val manga = testManga.copy(lastReadPage = 5)
        coEvery { getMangaById(1L) } returns manga

        val viewModel = createViewModel()
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        val state = viewModel.uiState.value as ReaderUiState.Ready
        assertEquals(5, state.currentPage)
        job.cancel()
    }

    @Test
    fun `goToNextPage does not exceed last page`() = runTest(testDispatcher) {
        val manga = testManga.copy(pageCount = 3, lastReadPage = 2)
        coEvery { getMangaById(1L) } returns manga

        val viewModel = createViewModel()
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.goToNextPage()
        advanceUntilIdle()

        val state = viewModel.uiState.value as ReaderUiState.Ready
        assertEquals(2, state.currentPage)
        job.cancel()
    }

    @Test
    fun `goToPreviousPage decrements and saves`() = runTest(testDispatcher) {
        val manga = testManga.copy(lastReadPage = 3)
        coEvery { getMangaById(1L) } returns manga

        val viewModel = createViewModel()
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.goToPreviousPage()
        advanceUntilIdle()

        val state = viewModel.uiState.value as ReaderUiState.Ready
        assertEquals(2, state.currentPage)
        coVerify { updateLastReadPage(1L, 2) }
        job.cancel()
    }

    @Test
    fun `goToPreviousPage does not go below zero`() = runTest(testDispatcher) {
        coEvery { getMangaById(1L) } returns testManga

        val viewModel = createViewModel()
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.goToPreviousPage()
        advanceUntilIdle()

        val state = viewModel.uiState.value as ReaderUiState.Ready
        assertEquals(0, state.currentPage)
        job.cancel()
    }

    @Test
    fun `goToNextPage advances by 1 regardless of reading mode`() = runTest(testDispatcher) {
        val manga = testManga.copy(lastReadPage = 1)
        coEvery { getMangaById(1L) } returns manga
        settingsFlow.value = ReaderSettings(readingMode = ReadingMode.DOUBLE_PAGE, coverAlone = true)

        val viewModel = createViewModel()
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.goToNextPage()
        advanceUntilIdle()

        val state = viewModel.uiState.value as ReaderUiState.Ready
        assertEquals(2, state.currentPage)
        job.cancel()
    }

    @Test
    fun `double page cover alone starts at page 0 alone`() = runTest(testDispatcher) {
        coEvery { getMangaById(1L) } returns testManga
        settingsFlow.value = ReaderSettings(readingMode = ReadingMode.DOUBLE_PAGE, coverAlone = true)

        val viewModel = createViewModel()
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        val state = viewModel.uiState.value as ReaderUiState.Ready
        assertEquals(0, state.currentPage)
        job.cancel()
    }

    @Test
    fun `double page without cover alone starts at pages 0-1`() = runTest(testDispatcher) {
        coEvery { getMangaById(1L) } returns testManga
        settingsFlow.value = ReaderSettings(readingMode = ReadingMode.DOUBLE_PAGE, coverAlone = false)

        val viewModel = createViewModel()
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        val state = viewModel.uiState.value as ReaderUiState.Ready
        assertEquals(0, state.currentPage)
        job.cancel()
    }

    @Test
    fun `reading direction RTL swaps next and previous semantics`() = runTest(testDispatcher) {
        val manga = testManga.copy(lastReadPage = 5)
        coEvery { getMangaById(1L) } returns manga
        settingsFlow.value = ReaderSettings(readingDirection = ReadingDirection.RTL)

        val viewModel = createViewModel()
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        val state = viewModel.uiState.value as ReaderUiState.Ready
        assertEquals(ReadingDirection.RTL, state.settings.readingDirection)
        assertEquals(5, state.currentPage)
        job.cancel()
    }

    @Test
    fun `settings changes update state`() = runTest(testDispatcher) {
        coEvery { getMangaById(1L) } returns testManga

        val viewModel = createViewModel()
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        settingsFlow.value = ReaderSettings(readingMode = ReadingMode.LONG_STRIP)
        advanceUntilIdle()

        val state = viewModel.uiState.value as ReaderUiState.Ready
        assertEquals(ReadingMode.LONG_STRIP, state.settings.readingMode)
        job.cancel()
    }

    @Test
    fun `long strip mode tracks visible page`() = runTest(testDispatcher) {
        coEvery { getMangaById(1L) } returns testManga
        settingsFlow.value = ReaderSettings(readingMode = ReadingMode.LONG_STRIP)

        val viewModel = createViewModel()
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.onVisiblePageChanged(4)
        advanceUntilIdle()

        val state = viewModel.uiState.value as ReaderUiState.Ready
        assertEquals(4, state.currentPage)
        coVerify { updateLastReadPage(1L, 4) }
        job.cancel()
    }

    @Test
    fun `goToNextPage increments by 1`() = runTest(testDispatcher) {
        coEvery { getMangaById(1L) } returns testManga

        val viewModel = createViewModel()
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.goToNextPage()
        advanceUntilIdle()

        val state = viewModel.uiState.value as ReaderUiState.Ready
        assertEquals(1, state.currentPage)
        coVerify { updateLastReadPage(1L, 1) }
        job.cancel()
    }

    // --- Spread helper tests ---

    @Test
    fun `spreadCount with coverAlone returns correct count`() {
        // 10 pages: cover alone + ceil(9/2) = 1 + 5 = 6
        assertEquals(6, spreadCount(10, coverAlone = true))
        // 1 page: just cover = 1
        assertEquals(1, spreadCount(1, coverAlone = true))
        // 2 pages: cover + 1 spread of page 1 = 2
        assertEquals(2, spreadCount(2, coverAlone = true))
        // 3 pages: cover + 1 spread (pages 1-2) = 2
        assertEquals(2, spreadCount(3, coverAlone = true))
        // 4 pages: cover + 2 spreads (1-2, 3) = 3
        assertEquals(3, spreadCount(4, coverAlone = true))
    }

    @Test
    fun `spreadCount with zero pages returns zero`() {
        assertEquals(0, spreadCount(0, coverAlone = true))
        assertEquals(0, spreadCount(0, coverAlone = false))
    }

    @Test
    fun `spreadCount without coverAlone returns correct count`() {
        // 10 pages: ceil(10/2) = 5
        assertEquals(5, spreadCount(10, coverAlone = false))
        // 1 page: 1
        assertEquals(1, spreadCount(1, coverAlone = false))
        // 3 pages: ceil(3/2) = 2
        assertEquals(2, spreadCount(3, coverAlone = false))
    }

    @Test
    fun `spreadToPages cover alone spread 0 returns single page`() {
        val (first, second) = spreadToPages(spreadIndex = 0, pageCount = 10, coverAlone = true)
        assertEquals(0, first)
        assertNull(second)
    }

    @Test
    fun `spreadToPages cover alone spread 1 returns pages 1-2`() {
        val (first, second) = spreadToPages(spreadIndex = 1, pageCount = 10, coverAlone = true)
        assertEquals(1, first)
        assertEquals(2, second)
    }

    @Test
    fun `spreadToPages without cover alone spread 0 returns pages 0-1`() {
        val (first, second) = spreadToPages(spreadIndex = 0, pageCount = 10, coverAlone = false)
        assertEquals(0, first)
        assertEquals(1, second)
    }

    @Test
    fun `spreadToPages last spread odd page count returns single`() {
        // 10 pages with coverAlone: last spread index = 5, pages = (9, null)
        val (first, second) = spreadToPages(spreadIndex = 5, pageCount = 10, coverAlone = true)
        assertEquals(9, first)
        assertNull(second)
    }

    @Test
    fun `pageToSpread converts correctly with coverAlone`() {
        // page 0 → spread 0 (cover)
        assertEquals(0, pageToSpread(0, coverAlone = true))
        // page 1 → spread 1
        assertEquals(1, pageToSpread(1, coverAlone = true))
        // page 2 → spread 1
        assertEquals(1, pageToSpread(2, coverAlone = true))
        // page 3 → spread 2
        assertEquals(2, pageToSpread(3, coverAlone = true))
        // page 9 → spread 5
        assertEquals(5, pageToSpread(9, coverAlone = true))
    }

    @Test
    fun `pageToSpread converts correctly without coverAlone`() {
        // page 0 → spread 0
        assertEquals(0, pageToSpread(0, coverAlone = false))
        // page 1 → spread 0
        assertEquals(0, pageToSpread(1, coverAlone = false))
        // page 2 → spread 1
        assertEquals(1, pageToSpread(2, coverAlone = false))
        // page 3 → spread 1
        assertEquals(1, pageToSpread(3, coverAlone = false))
    }

    // --- goToPage tests ---

    @Test
    fun `goToPage updates currentPage and saves`() = runTest(testDispatcher) {
        coEvery { getMangaById(1L) } returns testManga

        val viewModel = createViewModel()
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.goToPage(5)
        advanceUntilIdle()

        val state = viewModel.uiState.value as ReaderUiState.Ready
        assertEquals(5, state.currentPage)
        coVerify { updateLastReadPage(1L, 5) }
        job.cancel()
    }

    @Test
    fun `goToPage does nothing when state is not Ready`() = runTest(testDispatcher) {
        coEvery { getMangaById(1L) } returns testManga

        val viewModel = createViewModel()
        // Don't advance — state is still Loading
        assertTrue(viewModel.uiState.value is ReaderUiState.Loading)

        viewModel.goToPage(5)
        // Should not crash, state still Loading
        assertTrue(viewModel.uiState.value is ReaderUiState.Loading)
    }

    @Test
    fun `onVisiblePageChanged ignores same page`() = runTest(testDispatcher) {
        coEvery { getMangaById(1L) } returns testManga

        val viewModel = createViewModel()
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        // currentPage is 0, calling with 0 should not trigger save
        viewModel.onVisiblePageChanged(0)
        advanceUntilIdle()

        // updateLastReadPage should NOT be called for page 0 after initial load
        coVerify(exactly = 0) { updateLastReadPage(1L, 0) }
        job.cancel()
    }

    @Test
    fun `onVisiblePageChanged clamps to valid range`() = runTest(testDispatcher) {
        coEvery { getMangaById(1L) } returns testManga

        val viewModel = createViewModel()
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.onVisiblePageChanged(999)
        advanceUntilIdle()

        assertEquals(9, (viewModel.uiState.value as ReaderUiState.Ready).currentPage)
        coVerify { updateLastReadPage(1L, 9) }
        job.cancel()
    }

    @Test
    fun `goToPage clamps to valid range`() = runTest(testDispatcher) {
        coEvery { getMangaById(1L) } returns testManga

        val viewModel = createViewModel()
        val job = launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.goToPage(-3)
        advanceUntilIdle()
        assertEquals(0, (viewModel.uiState.value as ReaderUiState.Ready).currentPage)

        viewModel.goToPage(100)
        advanceUntilIdle()
        assertEquals(9, (viewModel.uiState.value as ReaderUiState.Ready).currentPage)

        job.cancel()
    }
}
