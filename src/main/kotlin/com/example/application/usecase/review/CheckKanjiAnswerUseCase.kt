package com.example.application.usecase.review

import com.example.application.dto.request.CheckKanjiAnswerRequest
import com.example.application.dto.response.ReviewResultResponse
import com.example.application.usecase.progress.defaultKanjiProgress
import com.example.domain.repository.KanjiRepository
import com.example.domain.repository.ProgressRepository
import java.time.Instant

class CheckKanjiAnswerUseCase(
    private val progressRepository: ProgressRepository,
    private val kanjiRepository: KanjiRepository,
) {
    suspend operator fun invoke(userId: Long, kanjiId: Long, request: CheckKanjiAnswerRequest): ReviewResultResponse {
        require(request.answer.isNotBlank()) { "Answer must not be blank" }

        val kanji = kanjiRepository.findById(kanjiId)
            ?: throw NoSuchElementException("Kanji with id=$kanjiId not found")

        val acceptedAnswers = buildSet {
            add(kanji.kanji)
            kanjiRepository.getReadings(kanjiId).forEach { add(it.reading) }
            kanjiRepository.getMeanings(kanjiId, "rus").forEach { add(it.meaning) }
        }.toList()

        val current = progressRepository.getKanjiProgress(userId, kanjiId) ?: defaultKanjiProgress(userId, kanjiId)
        val isCorrect = acceptedAnswers.any { it.normalizedAnswer() == request.answer.normalizedAnswer() }
        val updated = ReviewProgressScheduler.updateKanji(current, isCorrect, Instant.now())

        progressRepository.saveKanjiProgress(updated)

        return ReviewResultResponse(
            itemId = kanji.kanjiId,
            itemType = "kanji",
            isCorrect = isCorrect,
            acceptedAnswers = acceptedAnswers,
            status = updated.status.name.lowercase(),
            correctNumber = updated.correctNumber,
            wrongNumber = updated.wrongNumber,
            repetitionLevel = updated.repetitionLevel,
            lastReviewAt = updated.lastReviewAt,
            nextReviewAt = updated.nextReviewAt,
        )
    }
}
