package com.example.infra.database.tables

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table

object KanjiReadingsTable : Table("kanji_reading") {
    val kanjiReadingId = long("kanji_reading_id").autoIncrement()
    val kanjiId = long("kanji_id").references(KanjisTable.kanjiId, onDelete = ReferenceOption.CASCADE)
    val reading = varchar("reading", 100)
    val readingType = varchar("reading_type", 20)

    init {
        uniqueIndex("uq_kanji_reading", kanjiId, reading, readingType)
        index("idx_kanji_reading_kanji_id", false, kanjiId)
        index("idx_kanji_reading_type", false, readingType)
    }

    override val primaryKey = PrimaryKey(kanjiReadingId)
}
