package com.example.domain.repository

import com.example.domain.model.Kanji
import com.example.domain.model.Word
import com.example.domain.model.WordMeaning
import com.example.domain.model.WordRelation

interface WordRepository {
    suspend fun search(query: String): List<Word>
    suspend fun findById(id: Long): Word?
    suspend fun findByWritingAndReading(writingForm: String, readingKana: String): Word?
    suspend fun getMeanings(wordId: Long): List<WordMeaning>
    suspend fun getRelatedWords(wordId: Long): List<WordRelation>
    suspend fun getKanjis(wordId: Long): List<Kanji>
}
