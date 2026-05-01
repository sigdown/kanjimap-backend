package com.example.domain.model

import java.time.Instant

data class UserWordProgress(
    val userId: Long,
    val wordId: Long,
    val status: ProgressStatus,
    val correctNumber: Int,
    val wrongNumber: Int,
    val repetitionLevel: Int,
    val lastReviewAt: Instant?,
    val nextReviewAt: Instant?,
    val updatedAt: Instant,
)

enum class ProgressStatus {
    NEW,
    LEARNING,
    REVIEW,
    MASTERED,
}
