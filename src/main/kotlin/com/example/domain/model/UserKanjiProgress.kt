package com.example.domain.model

import java.time.Instant

data class UserKanjiProgress(
    val userId: Long,
    val kanjiId: Long,
    val status: ProgressStatus,
    val correctNumber: Int,
    val wrongNumber: Int,
    val repetitionLevel: Int,
    val lastReviewAt: Instant?,
    val nextReviewAt: Instant?,
    val updatedAt: Instant,
)
