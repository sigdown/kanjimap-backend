package com.example.application.usecase.progress

import com.example.application.dto.response.WordProgressResponse
import com.example.domain.repository.ProgressRepository
import com.example.domain.repository.WordRepository

class GetWordProgressUseCase(
    private val progressRepository: ProgressRepository,
    private val wordRepository: WordRepository,
) {
    suspend operator fun invoke(userId: Long, wordId: Long): WordProgressResponse {
        wordRepository.findById(wordId)
            ?: throw NoSuchElementException("Word with id=$wordId not found")

        return (progressRepository.getWordProgress(userId, wordId) ?: defaultWordProgress(userId, wordId))
            .toResponse()
    }
}
