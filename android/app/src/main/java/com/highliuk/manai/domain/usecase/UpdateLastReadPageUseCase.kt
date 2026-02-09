package com.highliuk.manai.domain.usecase

import com.highliuk.manai.domain.repository.MangaRepository
import javax.inject.Inject

class UpdateLastReadPageUseCase @Inject constructor(
    private val repository: MangaRepository,
) {
    suspend operator fun invoke(id: Long, page: Int) = repository.updateLastReadPage(id, page)
}
