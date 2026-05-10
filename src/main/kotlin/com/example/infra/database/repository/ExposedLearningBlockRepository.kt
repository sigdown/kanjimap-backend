@file:Suppress("DEPRECATION", "DEPRECATION_ERROR")

package com.example.infra.database.repository

import com.example.domain.model.Kanji
import com.example.domain.model.LearningBlock
import com.example.domain.model.LearningBlockType
import com.example.domain.model.Word
import com.example.domain.repository.LearningBlockRepository
import com.example.infra.database.dbQuery
import com.example.infra.database.tables.KanjisTable
import com.example.infra.database.tables.KanjiWordsTable
import com.example.infra.database.tables.LearningBlockKanjisTable
import com.example.infra.database.tables.LearningBlocksTable
import com.example.infra.database.tables.LearningBlockWordsTable
import com.example.infra.database.tables.WordsTable
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll

class ExposedLearningBlockRepository : LearningBlockRepository {
    override suspend fun findAll(): List<LearningBlock> = dbQuery {
        LearningBlocksTable
            .selectAll()
            .orderBy(LearningBlocksTable.orderIndex)
            .map(::toLearningBlock)
    }

    override suspend fun findById(id: Long): LearningBlock? = dbQuery {
        LearningBlocksTable
            .selectAll()
            .filter { row -> row[LearningBlocksTable.learningBlockId] == id }
            .map(::toLearningBlock)
            .singleOrNull()
    }

    override suspend fun getWords(blockId: Long): List<Word> = dbQuery {
        (LearningBlockWordsTable innerJoin WordsTable)
            .select(LearningBlockWordsTable.columns + WordsTable.columns)
            .filter { row -> row[LearningBlockWordsTable.learningBlockId] == blockId }
            .map(::toWord)
    }

    override suspend fun getKanjis(blockId: Long): List<Kanji> = dbQuery {
        (LearningBlockKanjisTable innerJoin KanjisTable)
            .select(LearningBlockKanjisTable.columns + KanjisTable.columns)
            .filter { row -> row[LearningBlockKanjisTable.learningBlockId] == blockId }
            .map(::toKanji)
    }

    private fun toLearningBlock(row: ResultRow): LearningBlock = LearningBlock(
        learningBlockId = row[LearningBlocksTable.learningBlockId],
        title = row[LearningBlocksTable.title],
        description = row[LearningBlocksTable.description],
        blockType = toBlockType(row[LearningBlocksTable.blockType]),
        orderIndex = row[LearningBlocksTable.orderIndex],
        createdAt = row[LearningBlocksTable.createdAt],
    )

    private fun toWord(row: ResultRow): Word = Word(
        wordId = row[WordsTable.wordId],
        writingForm = row[WordsTable.writingForm],
        readingKana = row[WordsTable.readingKana],
        jlptLevel = row[WordsTable.jlptLevel],
        topicName = row[WordsTable.topicName],
    )

    private fun toKanji(row: ResultRow): Kanji = Kanji(
        kanjiId = row[KanjisTable.kanjiId],
        kanji = row[KanjisTable.kanji],
        strokeCount = row[KanjisTable.strokeCount],
        jlptLevel = row[KanjisTable.jlptLevel],
    )

    private fun toBlockType(value: String): LearningBlockType = when (value.lowercase()) {
        "word" -> LearningBlockType.WORD
        "kanji" -> LearningBlockType.KANJI
        else -> LearningBlockType.MIXED
    }
}
