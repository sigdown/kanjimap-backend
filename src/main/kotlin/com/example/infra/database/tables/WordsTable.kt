package com.example.infra.database.tables

import org.jetbrains.exposed.v1.core.Table

object WordsTable : Table("word") {
    val wordId = long("word_id").autoIncrement()
    val writingForm = varchar("writing_form", 255)
    val readingKana = varchar("reading_kana", 255)
    val jlptLevel = varchar("jlpt_level", 10).nullable()
    val topicName = varchar("topic_name", 100).nullable()

    init {
        uniqueIndex("uq_word_writing_reading", writingForm, readingKana)
        index("idx_word_writing_form", false, writingForm)
        index("idx_word_reading_kana", false, readingKana)
    }

    override val primaryKey = PrimaryKey(wordId)
}
