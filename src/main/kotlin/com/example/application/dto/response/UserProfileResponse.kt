package com.example.application.dto.response

import java.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class UserProfileResponse(
    val userId: Long,
    val username: String,
    val email: String,
    @Serializable(with = InstantAsStringSerializer::class)
    val createdAt: Instant,
    @Serializable(with = InstantAsStringSerializer::class)
    val updatedAt: Instant,
)
