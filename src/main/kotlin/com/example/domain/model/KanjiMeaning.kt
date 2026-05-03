package com.example.domain.model

data class KanjiMeaning(
    val kanjiMeaningId: Long,
    val kanjiId: Long,
    val languageCode: String,
    val meaning: String,
    val example: String?,
)
