package com.highliuk.manai.domain.usecase

import com.highliuk.manai.domain.model.Manga
import com.highliuk.manai.domain.repository.MangaRepository
import javax.inject.Inject

class GetMangaByIdUseCase @Inject constructor(
    private val repository: MangaRepository,
) {
    suspend operator fun invoke(id: Long): Manga? = repository.getMangaById(id)
}
