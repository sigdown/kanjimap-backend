package com.example.domain.repository

import com.example.domain.model.Kanji
import com.example.domain.model.KanjiMeaning
import com.example.domain.model.KanjiReading
import com.example.domain.model.Word

interface KanjiRepository {
    suspend fun search(query: String): List<Kanji>
    suspend fun findById(id: Long): Kanji?
    suspend fun findByLiteral(literal: String): Kanji?
    suspend fun getReadings(kanjiId: Long): List<KanjiReading>
    suspend fun getMeanings(kanjiId: Long, languageCode: String = "rus"): List<KanjiMeaning>
    suspend fun getWords(kanjiId: Long): List<Word>
}
