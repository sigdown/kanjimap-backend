package com.example.application.dto.response

data class WordCardResponse(
    val word: WordSearchItemResponse,
    val meanings: List<WordMeaningResponse>,
    val relatedWords: List<RelatedWordResponse>,
    val kanjis: List<KanjiSearchItemResponse>,
)

data class WordMeaningResponse(
    val meaningId: Long,
    val meaning: String,
    val exampleJp: String?,
    val exampleTranslation: String?,
    val partOfSpeech: String?,
)

data class RelatedWordResponse(
    val relationType: String,
    val note: String?,
    val word: WordSearchItemResponse?,
)
