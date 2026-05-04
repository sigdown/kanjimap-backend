package com.example.application.dto.response

data class LearningBlockResponse(
    val learningBlockId: Long,
    val title: String,
    val description: String?,
    val blockType: String,
    val orderIndex: Int,
)
