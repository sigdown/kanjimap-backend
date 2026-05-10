package com.example.application.usecase.progress

import com.example.application.dto.response.KanjiProgressResponse
import com.example.application.dto.response.WordProgressResponse
import com.example.domain.model.ProgressStatus
import com.example.domain.model.UserKanjiProgress
import com.example.domain.model.UserWordProgress
import java.time.Instant

internal fun defaultWordProgress(userId: Long, wordId: Long, now: Instant = Instant.now()): UserWordProgress =
    UserWordProgress(
        userId = userId,
        wordId = wordId,
        status = ProgressStatus.NEW,
        correctNumber = 0,
        wrongNumber = 0,
        repetitionLevel = 0,
        lastReviewAt = null,
        nextReviewAt = null,
        updatedAt = now,
    )

internal fun defaultKanjiProgress(userId: Long, kanjiId: Long, now: Instant = Instant.now()): UserKanjiProgress =
    UserKanjiProgress(
        userId = userId,
        kanjiId = kanjiId,
        status = ProgressStatus.NEW,
        correctNumber = 0,
        wrongNumber = 0,
        repetitionLevel = 0,
        lastReviewAt = null,
        nextReviewAt = null,
        updatedAt = now,
    )

internal fun UserWordProgress.toResponse(): WordProgressResponse = WordProgressResponse(
    wordId = wordId,
    status = status.name.lowercase(),
    correctNumber = correctNumber,
    wrongNumber = wrongNumber,
    repetitionLevel = repetitionLevel,
    lastReviewAt = lastReviewAt,
    nextReviewAt = nextReviewAt,
    updatedAt = updatedAt,
)

internal fun UserKanjiProgress.toResponse(): KanjiProgressResponse = KanjiProgressResponse(
    kanjiId = kanjiId,
    status = status.name.lowercase(),
    correctNumber = correctNumber,
    wrongNumber = wrongNumber,
    repetitionLevel = repetitionLevel,
    lastReviewAt = lastReviewAt,
    nextReviewAt = nextReviewAt,
    updatedAt = updatedAt,
)

internal fun parseProgressStatus(value: String): ProgressStatus = when (value.lowercase()) {
    "new" -> ProgressStatus.NEW
    "learning" -> ProgressStatus.LEARNING
    "review" -> ProgressStatus.REVIEW
    "mastered" -> ProgressStatus.MASTERED
    else -> throw IllegalArgumentException("Unsupported progress status '$value'")
}
