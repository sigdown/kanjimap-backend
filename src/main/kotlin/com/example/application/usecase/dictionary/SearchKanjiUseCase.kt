package com.example.application.usecase.dictionary

import com.example.application.dto.response.KanjiSearchItemResponse
import com.example.domain.repository.KanjiRepository

class SearchKanjiUseCase(
    private val kanjiRepository: KanjiRepository,
) {
    suspend operator fun invoke(query: String): List<KanjiSearchItemResponse> {
        if (query.isBlank()) return emptyList()

        return kanjiRepository.search(query.trim()).map { kanji ->
            KanjiSearchItemResponse(
                kanjiId = kanji.kanjiId,
                literal = kanji.kanji,
                strokeCount = kanji.strokeCount,
                jlptLevel = kanji.jlptLevel,
            )
        }
    }
}
