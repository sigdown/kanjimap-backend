package com.example.application.usecase.dictionary

import com.example.application.dto.response.WordSearchItemResponse
import com.example.domain.repository.WordRepository

class SearchWordsUseCase(
    private val wordRepository: WordRepository,
) {
    suspend operator fun invoke(query: String): List<WordSearchItemResponse> {
        if (query.isBlank()) return emptyList()

        return wordRepository.search(query.trim()).map { word ->
            WordSearchItemResponse(
                wordId = word.wordId,
                writingForm = word.writingForm,
                readingKana = word.readingKana,
                jlptLevel = word.jlptLevel,
                topicName = word.topicName,
            )
        }
    }
}
