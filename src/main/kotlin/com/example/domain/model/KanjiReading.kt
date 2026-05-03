package com.example.domain.model

data class KanjiReading(
    val kanjiReadingId: Long,
    val kanjiId: Long,
    val reading: String,
    val readingType: KanjiReadingType,
)

enum class KanjiReadingType {
    ON,
    KUN,
    NANORI,
}
