package com.example.application.dto.request

import kotlinx.serialization.Serializable

@Serializable
data class CheckKanjiAnswerRequest(
    val answer: String,
)
