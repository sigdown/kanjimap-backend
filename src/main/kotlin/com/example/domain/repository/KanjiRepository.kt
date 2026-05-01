package com.example.domain.repository

import com.example.domain.model.Kanji
import com.example.domain.model.KanjiMeaning
import com.example.domain.model.Word

interface KanjiRepository {
    suspend fun getById(kanjiId: Long) : Kanji?
    suspend fun search(query: String): List<Kanji>
    suspend fun getMeanings(kanjiId: Long): List<KanjiMeaning>
    suspend fun getWordsByKanjiId(kanjiId: Long): List<Word>
}