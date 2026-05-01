package com.example.domain.model

import java.time.Instant

data class User(
    val userId: Long,
    val username: String,
    val email: String,
    val passwordHash: String,
    val createdAt: Instant,
    val updatedAt: Instant
)