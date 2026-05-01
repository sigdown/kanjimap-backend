package com.example.infra.database.tables

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table

object KanjiWordsTable : Table("kanji_word") {
    val kanjiId = long("kanji_id").references(KanjisTable.kanjiId, onDelete = ReferenceOption.CASCADE)
    val wordId = long("word_id").references(WordsTable.wordId, onDelete = ReferenceOption.CASCADE)
    val positionIndex = integer("position_index").nullable()

    init {
        index("idx_kanji_word_word_id", false, wordId)
    }

    override val primaryKey = PrimaryKey(kanjiId, wordId)
}
