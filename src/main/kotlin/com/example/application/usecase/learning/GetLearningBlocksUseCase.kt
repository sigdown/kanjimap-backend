package com.example.application.usecase.learning

import com.example.application.dto.response.LearningBlockResponse
import com.example.domain.repository.LearningBlockRepository

class GetLearningBlocksUseCase(
    private val learningBlockRepository: LearningBlockRepository,
) {
    suspend operator fun invoke(): List<LearningBlockResponse> {
        return learningBlockRepository.findAll().map { block ->
            LearningBlockResponse(
                learningBlockId = block.learningBlockId,
                title = block.title,
                description = block.description,
                blockType = block.blockType.name.lowercase(),
                orderIndex = block.orderIndex,
            )
        }
    }
}
