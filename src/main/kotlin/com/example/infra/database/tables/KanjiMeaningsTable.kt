package com.example.infra.database.tables

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table

object KanjiMeaningsTable : Table("kanji_meaning") {
    val kanjiMeaningId = long("kanji_meaning_id").autoIncrement()
    val kanjiId = long("kanji_id").references(KanjisTable.kanjiId, onDelete = ReferenceOption.CASCADE)
    val languageCode = varchar("language_code", 10).default("eng")
    val meaning = text("meaning")
    val example = text("example").nullable()

    init {
        uniqueIndex("uq_kanji_meaning", kanjiId, languageCode, meaning)
        index("idx_kanji_meaning_kanji_id", false, kanjiId)
        index("idx_kanji_meaning_language_code", false, languageCode)
    }

    override val primaryKey = PrimaryKey(kanjiMeaningId)
}
