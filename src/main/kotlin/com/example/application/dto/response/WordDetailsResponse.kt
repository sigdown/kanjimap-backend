package com.example.application.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class WordDetailsResponse(
    val word: WordSearchItemResponse,
    val meanings: List<WordMeaningResponse>,
    val relatedWords: List<RelatedWordResponse>,
    val kanjis: List<KanjiSearchItemResponse>,
)

@Serializable
data class WordMeaningResponse(
    val meaningId: Long,
    val meaning: String,
    val exampleJp: String?,
    val exampleTranslation: String?,
    val partOfSpeech: String?,
)

@Serializable
data class RelatedWordResponse(
    val relationType: String,
    val note: String?,
    val word: WordSearchItemResponse?,
)
