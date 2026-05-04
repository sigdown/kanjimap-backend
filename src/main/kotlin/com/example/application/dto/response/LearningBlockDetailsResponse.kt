package com.example.application.dto.response

data class LearningBlockDetailsResponse(
    val block: LearningBlockResponse,
    val words: List<WordSearchItemResponse>,
    val kanjis: List<KanjiSearchItemResponse>,
)
