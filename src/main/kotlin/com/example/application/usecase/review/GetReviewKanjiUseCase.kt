package com.example.application.usecase.review

import com.example.application.dto.response.KanjiSearchItemResponse
import com.example.application.dto.response.ReviewItemResponse
import com.example.domain.repository.KanjiRepository
import com.example.domain.repository.ProgressRepository
import java.time.Instant

class GetReviewKanjiUseCase(
    private val progressRepository: ProgressRepository,
    private val kanjiRepository: KanjiRepository,
) {
    suspend operator fun invoke(userId: Long, now: Instant = Instant.now()): List<ReviewItemResponse> {
        return progressRepository.getKanjisForReview(userId, now).mapNotNull { progress ->
            val kanji = kanjiRepository.findById(progress.kanjiId) ?: return@mapNotNull null
            ReviewItemResponse(
                itemId = kanji.kanjiId,
                itemType = "kanji",
                kanji = KanjiSearchItemResponse(
                    kanjiId = kanji.kanjiId,
                    literal = kanji.kanji,
                    strokeCount = kanji.strokeCount,
                    jlptLevel = kanji.jlptLevel,
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
