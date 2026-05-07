package com.example.application.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    val accessToken: String,
    val user: UserProfileResponse,
)
