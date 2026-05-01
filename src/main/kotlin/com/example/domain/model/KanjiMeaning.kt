package com.example.domain.model

data class KanjiMeaning(
    val kanjiMeaningId: Long,
    val kanjiId: Long,
    val reading: String?,
    val readingType: KanjiReadingType?,
    val meaning: String?,
    val example: String?,
)

enum class KanjiReadingType {
    ON,
    KUN,
    MEANING,
    OTHER,
}
