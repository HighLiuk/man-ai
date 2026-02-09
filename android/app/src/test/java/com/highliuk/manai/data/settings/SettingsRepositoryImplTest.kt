package com.highliuk.manai.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import app.cash.turbine.test
import com.highliuk.manai.domain.model.ReadingDirection
import com.highliuk.manai.domain.model.ReadingMode
import com.highliuk.manai.domain.model.ReaderSettings
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsRepositoryImplTest {

    @get:Rule
    val tmpFolder = TemporaryFolder()

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher + Job())

    private fun createDataStore(): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { tmpFolder.newFile("test_settings.preferences_pb") },
        )

    private fun createRepo(): SettingsRepositoryImpl =
        SettingsRepositoryImpl(createDataStore())

    @Test
    fun `default settings returned when no preference set`() = runTest(testDispatcher) {
        val repo = createRepo()

        repo.getSettings().test {
            assertEquals(ReaderSettings(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateReadingMode persists and emits new value`() = runTest(testDispatcher) {
        val repo = createRepo()

        repo.getSettings().test {
            assertEquals(ReadingMode.SINGLE_PAGE, awaitItem().readingMode)

            repo.updateReadingMode(ReadingMode.DOUBLE_PAGE)
            assertEquals(ReadingMode.DOUBLE_PAGE, awaitItem().readingMode)

            repo.updateReadingMode(ReadingMode.LONG_STRIP)
            assertEquals(ReadingMode.LONG_STRIP, awaitItem().readingMode)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateReadingDirection persists and emits`() = runTest(testDispatcher) {
        val repo = createRepo()

        repo.getSettings().test {
            assertEquals(ReadingDirection.RTL, awaitItem().readingDirection)

            repo.updateReadingDirection(ReadingDirection.LTR)
            assertEquals(ReadingDirection.LTR, awaitItem().readingDirection)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateTapNavigationEnabled persists and emits`() = runTest(testDispatcher) {
        val repo = createRepo()

        repo.getSettings().test {
            assertEquals(true, awaitItem().tapNavigationEnabled)

            repo.updateTapNavigationEnabled(false)
            assertEquals(false, awaitItem().tapNavigationEnabled)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateCoverAlone persists and emits`() = runTest(testDispatcher) {
        val repo = createRepo()

        repo.getSettings().test {
            assertEquals(true, awaitItem().coverAlone)

            repo.updateCoverAlone(false)
            assertEquals(false, awaitItem().coverAlone)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
