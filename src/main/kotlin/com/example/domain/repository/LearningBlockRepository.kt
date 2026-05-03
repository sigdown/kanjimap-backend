package com.example.domain.repository

import com.example.domain.model.Kanji
import com.example.domain.model.LearningBlock
import com.example.domain.model.Word

interface LearningBlockRepository {
    suspend fun findAll(): List<LearningBlock>
    suspend fun findById(id: Long): LearningBlock?
    suspend fun getWords(blockId: Long): List<Word>
    suspend fun getKanjis(blockId: Long): List<Kanji>
}
