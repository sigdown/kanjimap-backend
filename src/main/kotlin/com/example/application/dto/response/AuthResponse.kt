package com.example.application.dto.response

data class AuthResponse(
    val accessToken: String,
    val user: UserProfileResponse,
)
