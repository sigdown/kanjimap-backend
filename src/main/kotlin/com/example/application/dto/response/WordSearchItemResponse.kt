package com.example.application.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class WordSearchItemResponse(
    val wordId: Long,
    val writingForm: String,
    val readingKana: String,
    val jlptLevel: String?,
    val topicName: String?,
)
