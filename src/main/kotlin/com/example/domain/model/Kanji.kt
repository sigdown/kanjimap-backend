package com.example.domain.model

data class Kanji(
    val kanjiId: Long,
    val kanji: String,
    val strokeCount: Int?,
    val jlptLevel: String?,
)
