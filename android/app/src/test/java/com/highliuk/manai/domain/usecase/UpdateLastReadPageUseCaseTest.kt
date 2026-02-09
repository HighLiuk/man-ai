package com.highliuk.manai.domain.usecase

import com.highliuk.manai.domain.repository.MangaRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class UpdateLastReadPageUseCaseTest {

    private val repository = mockk<MangaRepository>()
    private val useCase = UpdateLastReadPageUseCase(repository)

    @Test
    fun `delegates with correct arguments`() = runTest {
        coEvery { repository.updateLastReadPage(1L, 42) } returns Unit

        useCase(1L, 42)

        coVerify(exactly = 1) { repository.updateLastReadPage(1L, 42) }
    }

    @Test
    fun `passes zero page correctly`() = runTest {
        coEvery { repository.updateLastReadPage(5L, 0) } returns Unit

        useCase(5L, 0)

        coVerify(exactly = 1) { repository.updateLastReadPage(5L, 0) }
    }
}
