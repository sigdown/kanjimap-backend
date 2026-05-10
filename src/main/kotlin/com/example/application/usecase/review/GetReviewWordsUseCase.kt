package com.example.application.usecase.review

import com.example.application.dto.response.ReviewItemResponse
import com.example.application.dto.response.WordSearchItemResponse
import com.example.domain.repository.ProgressRepository
import com.example.domain.repository.WordRepository
import java.time.Instant

class GetReviewWordsUseCase(
    private val progressRepository: ProgressRepository,
    private val wordRepository: WordRepository,
) {
    suspend operator fun invoke(userId: Long, now: Instant = Instant.now()): List<ReviewItemResponse> {
        return progressRepository.getWordsForReview(userId, now).mapNotNull { progress ->
            val word = wordRepository.findById(progress.wordId) ?: return@mapNotNull null
            ReviewItemResponse(
                itemId = word.wordId,
                itemType = "word",
                word = WordSearchItemResponse(
                    wordId = word.wordId,
                    writingForm = word.writingForm,
                    readingKana = word.readingKana,
                    jlptLevel = word.jlptLevel,
                    topicName = word.topicName,
                ),
                status = progress.status.name.lowercase(),
                correctNumber = progress.correctNumber,
                wrongNumber = progress.wrongNumber,
                repetitionLevel = progress.repetitionLevel,
                nextReviewAt = progress.nextReviewAt,
            )
        }
    }
}
