package com.highliuk.manai.domain.usecase

import com.highliuk.manai.domain.model.Manga
import com.highliuk.manai.domain.repository.MangaRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GetMangaByIdUseCaseTest {

    private val repository = mockk<MangaRepository>()
    private val useCase = GetMangaByIdUseCase(repository)

    @Test
    fun `returns manga when found`() = runTest {
        val expected = Manga(id = 1, title = "Test", filePath = "/test.pdf", pageCount = 50)
        coEvery { repository.getMangaById(1L) } returns expected

        val result = useCase(1L)

        assertEquals(expected, result)
    }

    @Test
    fun `returns null when not found`() = runTest {
        coEvery { repository.getMangaById(99L) } returns null

        val result = useCase(99L)

        assertNull(result)
    }
}
