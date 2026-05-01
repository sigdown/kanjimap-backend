package com.example.infra.database.tables

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table

object KanjiMeaningsTable : Table("kanji_meaning") {
    val kanjiMeaningId = long("kanji_meaning_id").autoIncrement()
    val kanjiId = long("kanji_id").references(KanjisTable.kanjiId, onDelete = ReferenceOption.CASCADE)
    val reading = varchar("reading", 100).nullable()
    val readingType = varchar("reading_type", 20).nullable()
    val meaning = text("meaning").nullable()
    val example = text("example").nullable()

    init {
        index("idx_kanji_meaning_kanji_id", false, kanjiId)
    }

    override val primaryKey = PrimaryKey(kanjiMeaningId)
}
