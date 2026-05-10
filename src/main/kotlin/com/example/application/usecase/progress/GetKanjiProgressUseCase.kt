package com.example.application.usecase.progress

import com.example.application.dto.response.KanjiProgressResponse
import com.example.domain.repository.KanjiRepository
import com.example.domain.repository.ProgressRepository

class GetKanjiProgressUseCase(
    private val progressRepository: ProgressRepository,
    private val kanjiRepository: KanjiRepository,
) {
    suspend operator fun invoke(userId: Long, kanjiId: Long): KanjiProgressResponse {
        kanjiRepository.findById(kanjiId)
            ?: throw NoSuchElementException("Kanji with id=$kanjiId not found")

        return (progressRepository.getKanjiProgress(userId, kanjiId) ?: defaultKanjiProgress(userId, kanjiId))
            .toResponse()
    }
}
