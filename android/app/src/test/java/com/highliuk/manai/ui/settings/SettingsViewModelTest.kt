package com.highliuk.manai.ui.settings

import app.cash.turbine.test
import com.highliuk.manai.domain.repository.UserPreferencesRepository
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val userPreferencesRepository = mockk<UserPreferencesRepository>(relaxed = true)
    private val gridColumnsFlow = MutableStateFlow(2)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { userPreferencesRepository.gridColumns } returns gridColumnsFlow
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = SettingsViewModel(userPreferencesRepository)

    @Test
    fun `gridColumns emits current preference value`() = runTest(testDispatcher) {
        val viewModel = createViewModel()

        viewModel.gridColumns.test {
            assertEquals(2, awaitItem())
            gridColumnsFlow.value = 3
            assertEquals(3, awaitItem())
        }
    }

    @Test
    fun `setGridColumns updates preference`() = runTest(testDispatcher) {
        val viewModel = createViewModel()

        viewModel.setGridColumns(3)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { userPreferencesRepository.setGridColumns(3) }
    }
}
