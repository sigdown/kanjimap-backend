package com.example.application.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class LearningBlockResponse(
    val learningBlockId: Long,
    val title: String,
    val description: String?,
    val blockType: String,
    val orderIndex: Int,
)
