@file:Suppress("DEPRECATION", "DEPRECATION_ERROR")

package com.example.infra.database.repository

import com.example.domain.model.Kanji
import com.example.domain.model.KanjiMeaning
import com.example.domain.model.KanjiReading
import com.example.domain.model.KanjiReadingType
import com.example.domain.model.Word
import com.example.domain.repository.KanjiRepository
import com.example.infra.database.dbQuery
import com.example.infra.database.tables.KanjiMeaningsTable
import com.example.infra.database.tables.KanjiReadingsTable
import com.example.infra.database.tables.KanjiWordsTable
import com.example.infra.database.tables.KanjisTable
import com.example.infra.database.tables.WordsTable
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.lowerCase
import org.jetbrains.exposed.v1.core.like
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll

class ExposedKanjiRepository : KanjiRepository {
    override suspend fun search(query: String): List<Kanji> = dbQuery {
        val q = query.trim()
        if (q.isBlank()) return@dbQuery emptyList()

        val pattern = "%${q.lowercase()}%"
        KanjisTable
            .selectAll()
            .where {
                (KanjisTable.kanji.lowerCase() like pattern) or
                    (KanjisTable.jlptLevel.lowerCase() like pattern)
            }
            .limit(100)
            .map(::toKanji)
    }

    override suspend fun findById(id: Long): Kanji? = dbQuery {
        KanjisTable
            .selectAll()
            .singleOrNull { row -> row[KanjisTable.kanjiId] == id }
            ?.let(::toKanji)
    }

    override suspend fun findByLiteral(literal: String): Kanji? = dbQuery {
        KanjisTable
            .selectAll()
            .where { KanjisTable.kanji eq literal }
            .limit(1)
            .map(::toKanji)
            .singleOrNull()
    }

    override suspend fun getReadings(kanjiId: Long): List<KanjiReading> = dbQuery {
        KanjiReadingsTable
            .selectAll()
            .filter { row -> row[KanjiReadingsTable.kanjiId] == kanjiId }
            .map(::toKanjiReading)
    }

    override suspend fun getMeanings(kanjiId: Long, languageCode: String): List<KanjiMeaning> = dbQuery {
        KanjiMeaningsTable
            .selectAll()
            .filter { row ->
                row[KanjiMeaningsTable.kanjiId] == kanjiId &&
                    row[KanjiMeaningsTable.languageCode] == languageCode
            }
            .map(::toKanjiMeaning)
    }

    override suspend fun getWords(kanjiId: Long): List<Word> = dbQuery {
        (KanjiWordsTable innerJoin WordsTable)
            .select(KanjiWordsTable.columns + WordsTable.columns)
            .filter { row -> row[KanjiWordsTable.kanjiId] == kanjiId }
            .map(::toWord)
    }

    private fun toKanji(row: ResultRow): Kanji = Kanji(
        kanjiId = row[KanjisTable.kanjiId],
        kanji = row[KanjisTable.kanji],
        strokeCount = row[KanjisTable.strokeCount],
        jlptLevel = row[KanjisTable.jlptLevel],
    )

    private fun toKanjiReading(row: ResultRow): KanjiReading = KanjiReading(
        kanjiReadingId = row[KanjiReadingsTable.kanjiReadingId],
        kanjiId = row[KanjiReadingsTable.kanjiId],
        reading = row[KanjiReadingsTable.reading],
        readingType = toKanjiReadingType(row[KanjiReadingsTable.readingType]),
    )

    private fun toKanjiMeaning(row: ResultRow): KanjiMeaning = KanjiMeaning(
        kanjiMeaningId = row[KanjiMeaningsTable.kanjiMeaningId],
        kanjiId = row[KanjiMeaningsTable.kanjiId],
        languageCode = row[KanjiMeaningsTable.languageCode],
        meaning = row[KanjiMeaningsTable.meaning],
        example = row[KanjiMeaningsTable.example],
    )

    private fun toWord(row: ResultRow): Word = Word(
        wordId = row[WordsTable.wordId],
        writingForm = row[WordsTable.writingForm],
        readingKana = row[WordsTable.readingKana],
        jlptLevel = row[WordsTable.jlptLevel],
        topicName = row[WordsTable.topicName],
    )

    private fun toKanjiReadingType(value: String): KanjiReadingType = when (value.lowercase()) {
        "on" -> KanjiReadingType.ON
        "kun" -> KanjiReadingType.KUN
        else -> KanjiReadingType.NANORI
    }
}
