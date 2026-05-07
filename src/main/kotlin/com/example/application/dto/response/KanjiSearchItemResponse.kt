package com.example.application.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class KanjiSearchItemResponse(
    val kanjiId: Long,
    val literal: String,
    val strokeCount: Int?,
    val jlptLevel: String?,
)
