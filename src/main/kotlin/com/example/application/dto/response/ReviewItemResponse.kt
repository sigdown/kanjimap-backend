package com.example.application.dto.response

import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class ReviewItemResponse(
    val itemId: Long,
    val itemType: String,
    val word: WordSearchItemResponse? = null,
    val kanji: KanjiSearchItemResponse? = null,
    val status: String,
    val correctNumber: Int,
    val wrongNumber: Int,
    val repetitionLevel: Int,
    @Serializable(with = InstantAsStringSerializer::class)
    val nextReviewAt: Instant? = null,
)
