package com.example.infra.database.tables

import org.jetbrains.exposed.v1.core.Table

object KanjisTable : Table("kanji") {
    val kanjiId = long("kanji_id").autoIncrement()
    val kanji = varchar("kanji", 16).uniqueIndex("idx_kanji_symbol")
    val strokeCount = integer("stroke_count").nullable()
    val jlptLevel = varchar("jlpt_level", 10).nullable()

    override val primaryKey = PrimaryKey(kanjiId)
}
