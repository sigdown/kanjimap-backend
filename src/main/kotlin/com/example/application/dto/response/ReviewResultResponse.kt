package com.example.application.dto.response

import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class ReviewResultResponse(
    val itemId: Long,
    val itemType: String,
    val isCorrect: Boolean,
    val acceptedAnswers: List<String>,
    val status: String,
    val correctNumber: Int,
    val wrongNumber: Int,
    val repetitionLevel: Int,
    @Serializable(with = InstantAsStringSerializer::class)
    val lastReviewAt: Instant? = null,
    @Serializable(with = InstantAsStringSerializer::class)
    val nextReviewAt: Instant? = null,
)
