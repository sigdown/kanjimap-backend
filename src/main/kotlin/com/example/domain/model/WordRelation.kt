package com.example.domain.model

data class WordRelation(
    val wordId: Long,
    val relatedWordId: Long,
    val relationType: WordRelationType,
    val note: String?,
)

enum class WordRelationType {
    VARIANT,
    SIMILAR,
    CONFUSABLE,
}
