package com.example.infra.database.tables

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table

object WordMeaningsTable : Table("word_meaning") {
    val meaningId = long("meaning_id").autoIncrement()
    val wordId = long("word_id").references(WordsTable.wordId, onDelete = ReferenceOption.CASCADE)
    val meaning = text("meaning")
    val exampleJp = text("example_jp").nullable()
    val exampleTranslation = text("example_translation").nullable()
    val partOfSpeech = varchar("part_of_speech", 50).nullable()

    init {
        index("idx_word_meaning_word_id", false, wordId)
    }

    override val primaryKey = PrimaryKey(meaningId)
}
