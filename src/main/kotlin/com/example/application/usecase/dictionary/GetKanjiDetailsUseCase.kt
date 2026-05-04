package com.example.application.usecase.dictionary

import com.example.application.dto.response.KanjiDetailsResponse
import com.example.application.dto.response.KanjiMeaningResponse
import com.example.application.dto.response.KanjiSearchItemResponse
import com.example.application.dto.response.WordSearchItemResponse
import com.example.domain.model.KanjiReadingType
import com.example.domain.repository.KanjiRepository

class GetKanjiDetailsUseCase(
    private val kanjiRepository: KanjiRepository,
) {
    suspend operator fun invoke(kanjiId: Long, languageCode: String = "rus"): KanjiDetailsResponse {
        val kanji = kanjiRepository.findById(kanjiId)
            ?: throw NoSuchElementException("Kanji with id=$kanjiId not found")

        val readings = kanjiRepository.getReadings(kanji.kanjiId)
        val onReadings = readings.filter { it.readingType == KanjiReadingType.ON }.map { it.reading }
        val kunReadings = readings.filter { it.readingType == KanjiReadingType.KUN }.map { it.reading }
        val nanoriReadings = readings.filter { it.readingType == KanjiReadingType.NANORI }.map { it.reading }

        val meanings = kanjiRepository.getMeanings(kanji.kanjiId, languageCode).map { meaning ->
            KanjiMeaningResponse(
                kanjiMeaningId = meaning.kanjiMeaningId,
                languageCode = meaning.languageCode,
                meaning = meaning.meaning,
                example = meaning.example,
            )
        }

        val words = kanjiRepository.getWords(kanji.kanjiId).map { word ->
            WordSearchItemResponse(
                wordId = word.wordId,
                writingForm = word.writingForm,
                readingKana = word.readingKana,
                jlptLevel = word.jlptLevel,
                topicName = word.topicName,
            )
        }

        return KanjiDetailsResponse(
            kanji = KanjiSearchItemResponse(
                kanjiId = kanji.kanjiId,
                literal = kanji.kanji,
                strokeCount = kanji.strokeCount,
                jlptLevel = kanji.jlptLevel,
            ),
            onReadings = onReadings,
            kunReadings = kunReadings,
            nanoriReadings = nanoriReadings,
            meanings = meanings,
            words = words,
        )
    }
}
