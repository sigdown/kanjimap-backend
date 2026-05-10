package com.example.application.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class CheckWordAnswerRequest(
    val answer: String,
)
