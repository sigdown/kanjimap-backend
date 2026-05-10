package com.example.application.dto.response

import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class KanjiProgressResponse(
    val kanjiId: Long,
    val status: String,
    val correctNumber: Int,
    val wrongNumber: Int,
    val repetitionLevel: Int,
    @Serializable(with = InstantAsStringSerializer::class)
    val lastReviewAt: Instant? = null,
    @Serializable(with = InstantAsStringSerializer::class)
    val nextReviewAt: Instant? = null,
    @Serializable(with = InstantAsStringSerializer::class)
    val updatedAt: Instant,
)
