package com.example.application.usecase.progress

import com.example.application.dto.request.UpdateKanjiProgressRequest
import com.example.application.dto.response.KanjiProgressResponse
import com.example.domain.repository.KanjiRepository
import com.example.domain.repository.ProgressRepository
import java.time.Instant

class UpdateKanjiProgressUseCase(
    private val progressRepository: ProgressRepository,
    private val kanjiRepository: KanjiRepository,
) {
    suspend operator fun invoke(userId: Long, kanjiId: Long, request: UpdateKanjiProgressRequest): KanjiProgressResponse {
        kanjiRepository.findById(kanjiId)
            ?: throw NoSuchElementException("Kanji with id=$kanjiId not found")

        validateCounters(request.correctNumber, request.wrongNumber, request.repetitionLevel)

        val current = progressRepository.getKanjiProgress(userId, kanjiId) ?: defaultKanjiProgress(userId, kanjiId)
        val updated = current.copy(
            status = request.status?.let(::parseProgressStatus) ?: current.status,
            correctNumber = request.correctNumber ?: current.correctNumber,
            wrongNumber = request.wrongNumber ?: current.wrongNumber,
            repetitionLevel = request.repetitionLevel ?: current.repetitionLevel,
            lastReviewAt = request.lastReviewAt ?: current.lastReviewAt,
            nextReviewAt = request.nextReviewAt ?: current.nextReviewAt,
            updatedAt = Instant.now(),
        )

        progressRepository.saveKanjiProgress(updated)
        return updated.toResponse()
    }
}
