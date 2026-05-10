package com.example.application.usecase.review

import com.example.application.dto.request.CheckWordAnswerRequest
import com.example.application.dto.response.ReviewResultResponse
import com.example.application.usecase.progress.defaultWordProgress
import com.example.domain.repository.ProgressRepository
import com.example.domain.repository.WordRepository
import java.time.Instant

class CheckWordAnswerUseCase(
    private val progressRepository: ProgressRepository,
    private val wordRepository: WordRepository,
) {
    suspend operator fun invoke(userId: Long, wordId: Long, request: CheckWordAnswerRequest): ReviewResultResponse {
        require(request.answer.isNotBlank()) { "Answer must not be blank" }

        val word = wordRepository.findById(wordId)
            ?: throw NoSuchElementException("Word with id=$wordId not found")

        val acceptedAnswers = buildSet {
            add(word.writingForm)
            add(word.readingKana)
            wordRepository.getMeanings(wordId).forEach { add(it.meaning) }
        }.toList()

        val current = progressRepository.getWordProgress(userId, wordId) ?: defaultWordProgress(userId, wordId)
        val isCorrect = acceptedAnswers.any { it.normalizedAnswer() == request.answer.normalizedAnswer() }
        val updated = ReviewProgressScheduler.updateWord(current, isCorrect, Instant.now())

        progressRepository.saveWordProgress(updated)

        return ReviewResultResponse(
            itemId = word.wordId,
            itemType = "word",
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
