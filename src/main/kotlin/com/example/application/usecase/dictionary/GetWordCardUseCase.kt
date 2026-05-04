package com.example.application.usecase.dictionary

import com.example.application.dto.response.KanjiSearchItemResponse
import com.example.application.dto.response.RelatedWordResponse
import com.example.application.dto.response.WordCardResponse
import com.example.application.dto.response.WordMeaningResponse
import com.example.application.dto.response.WordSearchItemResponse
import com.example.domain.repository.WordRepository

class GetWordCardUseCase(
    private val wordRepository: WordRepository,
) {
    suspend operator fun invoke(wordId: Long): WordCardResponse {
        val word = wordRepository.findById(wordId)
            ?: throw NoSuchElementException("Word with id=$wordId not found")

        val meanings = wordRepository.getMeanings(word.wordId).map { meaning ->
            WordMeaningResponse(
                meaningId = meaning.meaningId,
                meaning = meaning.meaning,
                exampleJp = meaning.exampleJp,
                exampleTranslation = meaning.exampleTranslation,
                partOfSpeech = meaning.partOfSpeech,
            )
        }

        val relatedWords = wordRepository.getRelatedWords(word.wordId).map { relation ->
            val relatedWord = wordRepository.findById(relation.relatedWordId)
            RelatedWordResponse(
                relationType = relation.relationType.name.lowercase(),
                note = relation.note,
                word = relatedWord?.let {
                    WordSearchItemResponse(
                        wordId = it.wordId,
                        writingForm = it.writingForm,
                        readingKana = it.readingKana,
                        jlptLevel = it.jlptLevel,
                        topicName = it.topicName,
                    )
                },
            )
        }

        val kanjis = wordRepository.getKanjis(word.wordId).map { kanji ->
            KanjiSearchItemResponse(
                kanjiId = kanji.kanjiId,
                literal = kanji.kanji,
                strokeCount = kanji.strokeCount,
                jlptLevel = kanji.jlptLevel,
            )
        }

        return WordCardResponse(
            word = WordSearchItemResponse(
                wordId = word.wordId,
                writingForm = word.writingForm,
                readingKana = word.readingKana,
                jlptLevel = word.jlptLevel,
                topicName = word.topicName,
            ),
            meanings = meanings,
            relatedWords = relatedWords,
            kanjis = kanjis,
        )
    }
}
