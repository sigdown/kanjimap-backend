package com.example.domain.model

data class WordMeaning(
    val meaningId: Long,
    val wordId: Long,
    val meaning: String,
    val exampleJp: String?,
    val exampleTranslation: String?,
    val partOfSpeech: String?,
)
