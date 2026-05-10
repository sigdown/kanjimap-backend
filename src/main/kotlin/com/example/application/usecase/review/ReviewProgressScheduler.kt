package com.example.application.usecase.review

import com.example.domain.model.ProgressStatus
import com.example.domain.model.UserKanjiProgress
import com.example.domain.model.UserWordProgress
import java.time.Duration
import java.time.Instant

internal object ReviewProgressScheduler {
    fun updateWord(progress: UserWordProgress, isCorrect: Boolean, now: Instant): UserWordProgress =
        progress.copyWithReviewUpdate(isCorrect, now)

    fun updateKanji(progress: UserKanjiProgress, isCorrect: Boolean, now: Instant): UserKanjiProgress =
        progress.copyWithReviewUpdate(isCorrect, now)

    private fun UserWordProgress.copyWithReviewUpdate(isCorrect: Boolean, now: Instant): UserWordProgress {
        val next = nextState(status, repetitionLevel, correctNumber, wrongNumber, isCorrect, now)
        return copy(
            status = next.status,
            correctNumber = next.correctNumber,
            wrongNumber = next.wrongNumber,
            repetitionLevel = next.repetitionLevel,
            lastReviewAt = now,
            nextReviewAt = next.nextReviewAt,
            updatedAt = now,
        )
    }

    private fun UserKanjiProgress.copyWithReviewUpdate(isCorrect: Boolean, now: Instant): UserKanjiProgress {
        val next = nextState(status, repetitionLevel, correctNumber, wrongNumber, isCorrect, now)
        return copy(
            status = next.status,
            correctNumber = next.correctNumber,
            wrongNumber = next.wrongNumber,
            repetitionLevel = next.repetitionLevel,
            lastReviewAt = now,
            nextReviewAt = next.nextReviewAt,
            updatedAt = now,
        )
    }

    private fun nextState(
        currentStatus: ProgressStatus,
        currentLevel: Int,
        currentCorrect: Int,
        currentWrong: Int,
        isCorrect: Boolean,
        now: Instant,
    ): ReviewState {
        return if (isCorrect) {
            val nextLevel = currentLevel + 1
            ReviewState(
                status = when {
                    nextLevel >= 5 -> ProgressStatus.MASTERED
                    nextLevel >= 2 -> ProgressStatus.REVIEW
                    else -> ProgressStatus.LEARNING
                },
                correctNumber = currentCorrect + 1,
                wrongNumber = currentWrong,
                repetitionLevel = nextLevel,
                nextReviewAt = now.plus(reviewDelay(nextLevel)),
            )
        } else {
            val nextLevel = (currentLevel - 1).coerceAtLeast(0)
            ReviewState(
                status = if (currentStatus == ProgressStatus.MASTERED) ProgressStatus.REVIEW else ProgressStatus.LEARNING,
                correctNumber = currentCorrect,
                wrongNumber = currentWrong + 1,
                repetitionLevel = nextLevel,
                nextReviewAt = now.plus(Duration.ofHours(4)),
            )
        }
    }

    private fun reviewDelay(level: Int): Duration = when (level) {
        0 -> Duration.ZERO
        1 -> Duration.ofHours(12)
        2 -> Duration.ofDays(1)
        3 -> Duration.ofDays(3)
        4 -> Duration.ofDays(7)
        else -> Duration.ofDays(14)
    }

    private data class ReviewState(
        val status: ProgressStatus,
        val correctNumber: Int,
        val wrongNumber: Int,
        val repetitionLevel: Int,
        val nextReviewAt: Instant,
    )
}
