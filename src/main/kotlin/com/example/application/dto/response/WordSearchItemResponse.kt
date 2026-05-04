package com.example.application.dto.response

data class WordSearchItemResponse(
    val wordId: Long,
    val writingForm: String,
    val readingKana: String,
    val jlptLevel: String?,
    val topicName: String?,
)
