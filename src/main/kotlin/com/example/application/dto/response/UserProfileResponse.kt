package com.example.application.dto.response

import java.time.Instant

data class UserProfileResponse(
    val userId: Long,
    val username: String,
    val email: String,
    val createdAt: Instant,
    val updatedAt: Instant,
)
