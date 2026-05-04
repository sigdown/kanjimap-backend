package com.example.application.dto.response

data class KanjiSearchItemResponse(
    val kanjiId: Long,
    val literal: String,
    val strokeCount: Int?,
    val jlptLevel: String?,
)
