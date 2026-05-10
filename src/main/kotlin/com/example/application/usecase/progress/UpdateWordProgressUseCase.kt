package com.example.application.usecase.progress

import com.example.application.dto.request.UpdateWordProgressRequest
import com.example.application.dto.response.WordProgressResponse
import com.example.domain.repository.ProgressRepository
import com.example.domain.repository.WordRepository
import java.time.Instant

class UpdateWordProgressUseCase(
    private val progressRepository: ProgressRepository,
    private val wordRepository: WordRepository,
) {
    suspend operator fun invoke(userId: Long, wordId: Long, request: UpdateWordProgressRequest): WordProgressResponse {
        wordRepository.findById(wordId)
            ?: throw NoSuchElementException("Word with id=$wordId not found")

        validateCounters(request.correctNumber, request.wrongNumber, request.repetitionLevel)

        val current = progressRepository.getWordProgress(userId, wordId) ?: defaultWordProgress(userId, wordId)
        val updated = current.copy(
            status = request.status?.let(::parseProgressStatus) ?: current.status,
            correctNumber = request.correctNumber ?: current.correctNumber,
            wrongNumber = request.wrongNumber ?: current.wrongNumber,
            repetitionLevel = request.repetitionLevel ?: current.repetitionLevel,
            lastReviewAt = request.lastReviewAt ?: current.lastReviewAt,
            nextReviewAt = request.nextReviewAt ?: current.nextReviewAt,
            updatedAt = Instant.now(),
        )

        progressRepository.saveWordProgress(updated)
        return updated.toResponse()
    }
}
