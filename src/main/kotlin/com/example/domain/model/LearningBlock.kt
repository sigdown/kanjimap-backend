package com.example.domain.model

import java.time.Instant

data class LearningBlock(
    val learningBlockId: Long,
    val title: String,
    val description: String?,
    val blockType: LearningBlockType,
    val orderIndex: Int,
    val createdAt: Instant,
)

enum class LearningBlockType {
    WORD,
    KANJI,
    MIXED,
}
