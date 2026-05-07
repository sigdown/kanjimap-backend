package com.example.application.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class KanjiDetailsResponse(
    val kanji: KanjiSearchItemResponse,
    val onReadings: List<String>,
    val kunReadings: List<String>,
    val nanoriReadings: List<String>,
    val meanings: List<KanjiMeaningResponse>,
    val words: List<WordSearchItemResponse>,
)

@Serializable
data class KanjiMeaningResponse(
    val kanjiMeaningId: Long,
    val languageCode: String,
    val meaning: String,
    val example: String?,
)
