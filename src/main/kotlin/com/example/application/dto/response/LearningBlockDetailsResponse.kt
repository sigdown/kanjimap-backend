package com.example.application.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class LearningBlockDetailsResponse(
    val block: LearningBlockResponse,
    val words: List<WordSearchItemResponse>,
    val kanjis: List<KanjiSearchItemResponse>,
)
