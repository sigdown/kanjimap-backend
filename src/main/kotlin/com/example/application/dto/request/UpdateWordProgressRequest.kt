package com.example.application.dto.request

import com.example.application.dto.response.InstantAsStringSerializer
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class UpdateWordProgressRequest(
    val status: String? = null,
    val correctNumber: Int? = null,
    val wrongNumber: Int? = null,
    val repetitionLevel: Int? = null,
    @Serializable(with = InstantAsStringSerializer::class)
    val lastReviewAt: Instant? = null,
    @Serializable(with = InstantAsStringSerializer::class)
    val nextReviewAt: Instant? = null,
)
