@file:Suppress("DEPRECATION", "DEPRECATION_ERROR")

package com.example.infra.database.repository

import com.example.domain.model.Kanji
import com.example.domain.model.Word
import com.example.domain.model.WordMeaning
import com.example.domain.model.WordRelation
import com.example.domain.model.WordRelationType
import com.example.domain.repository.WordRepository
import com.example.infra.database.dbQuery
import com.example.infra.database.tables.KanjiWordsTable
import com.example.infra.database.tables.KanjisTable
import com.example.infra.database.tables.WordMeaningsTable
import com.example.infra.database.tables.WordRelationsTable
import com.example.infra.database.tables.WordsTable
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.lowerCase
import org.jetbrains.exposed.v1.core.like
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll

class ExposedWordRepository : WordRepository {
    override suspend fun search(query: String): List<Word> = dbQuery {
        val q = query.trim()
        if (q.isBlank()) return@dbQuery emptyList()

        val pattern = "%${q.lowercase()}%"
        WordsTable
            .selectAll()
            .where {
                (WordsTable.writingForm.lowerCase() like pattern) or
                    (WordsTable.readingKana.lowerCase() like pattern)
            }
            .limit(100)
            .map(::toWord)
    }

    override suspend fun findById(id: Long): Word? = dbQuery {
        WordsTable
            .selectAll()
            .where { WordsTable.wordId eq id }
            .limit(1)
            .map(::toWord)
            .singleOrNull()
    }

    override suspend fun findByWritingAndReading(writingForm: String, readingKana: String): Word? = dbQuery {
        WordsTable
            .selectAll()
            .where {
                (WordsTable.writingForm eq writingForm) and
                    (WordsTable.readingKana eq readingKana)
            }
            .limit(1)
            .map(::toWord)
            .singleOrNull()
    }

    override suspend fun getMeanings(wordId: Long): List<WordMeaning> = dbQuery {
        WordMeaningsTable
            .selectAll()
            .where { WordMeaningsTable.wordId eq wordId }
            .map(::toWordMeaning)
    }

    override suspend fun getRelatedWords(wordId: Long): List<WordRelation> = dbQuery {
        WordRelationsTable
            .selectAll()
            .where { WordRelationsTable.wordId eq wordId }
            .map(::toWordRelation)
    }

    override suspend fun getKanjis(wordId: Long): List<Kanji> = dbQuery {
        (KanjiWordsTable innerJoin KanjisTable)
            .select(KanjisTable.columns)
            .where { KanjiWordsTable.wordId eq wordId }
            .map(::toKanji)
    }

    private fun toWord(row: ResultRow): Word = Word(
        wordId = row[WordsTable.wordId],
        writingForm = row[WordsTable.writingForm],
        readingKana = row[WordsTable.readingKana],
        jlptLevel = row[WordsTable.jlptLevel],
        topicName = row[WordsTable.topicName],
    )

    private fun toWordMeaning(row: ResultRow): WordMeaning = WordMeaning(
        meaningId = row[WordMeaningsTable.meaningId],
        wordId = row[WordMeaningsTable.wordId],
        meaning = row[WordMeaningsTable.meaning],
        exampleJp = row[WordMeaningsTable.exampleJp],
        exampleTranslation = row[WordMeaningsTable.exampleTranslation],
        partOfSpeech = row[WordMeaningsTable.partOfSpeech],
    )

    private fun toWordRelation(row: ResultRow): WordRelation = WordRelation(
        wordId = row[WordRelationsTable.wordId],
        relatedWordId = row[WordRelationsTable.relatedWordId],
        relationType = when (row[WordRelationsTable.relationType].lowercase()) {
            "variant" -> WordRelationType.VARIANT
            "confusable" -> WordRelationType.CONFUSABLE
            else -> WordRelationType.SIMILAR
        },
        note = row[WordRelationsTable.note],
    )

    private fun toKanji(row: ResultRow): Kanji = Kanji(
        kanjiId = row[KanjisTable.kanjiId],
        kanji = row[KanjisTable.kanji],
        strokeCount = row[KanjisTable.strokeCount],
        jlptLevel = row[KanjisTable.jlptLevel],
    )
}
