package com.example.application.usecase.learning

import com.example.application.dto.response.KanjiSearchItemResponse
import com.example.application.dto.response.LearningBlockDetailsResponse
import com.example.application.dto.response.LearningBlockResponse
import com.example.application.dto.response.WordSearchItemResponse
import com.example.domain.repository.LearningBlockRepository

class GetLearningBlockDetailsUseCase(
    private val learningBlockRepository: LearningBlockRepository,
) {
    suspend operator fun invoke(blockId: Long): LearningBlockDetailsResponse {
        val block = learningBlockRepository.findById(blockId)
            ?: throw NoSuchElementException("Learning block with id=$blockId not found")

        val words = learningBlockRepository.getWords(block.learningBlockId).map { word ->
            WordSearchItemResponse(
                wordId = word.wordId,
                writingForm = word.writingForm,
                readingKana = word.readingKana,
                jlptLevel = word.jlptLevel,
                topicName = word.topicName,
            )
        }

        val kanjis = learningBlockRepository.getKanjis(block.learningBlockId).map { kanji ->
            KanjiSearchItemResponse(
                kanjiId = kanji.kanjiId,
                literal = kanji.kanji,
                strokeCount = kanji.strokeCount,
                jlptLevel = kanji.jlptLevel,
            )
        }

        return LearningBlockDetailsResponse(
            block = LearningBlockResponse(
                learningBlockId = block.learningBlockId,
                title = block.title,
                description = block.description,
                blockType = block.blockType.name.lowercase(),
                orderIndex = block.orderIndex,
            ),
            words = words,
            kanjis = kanjis,
        )
    }
}
