package com.example.infra.database.tables

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table

object WordRelationsTable : Table("word_relation") {
    val wordId = long("word_id").references(WordsTable.wordId, onDelete = ReferenceOption.CASCADE)
    val relatedWordId = long("related_word_id").references(WordsTable.wordId, onDelete = ReferenceOption.CASCADE)
    val relationType = varchar("relation_type", 20).default("similar")
    val note = text("note").nullable()

    init {
        index("idx_word_relation_related_word_id", false, relatedWordId)
        index("idx_word_relation_relation_type", false, relationType)
    }

    override val primaryKey = PrimaryKey(wordId, relatedWordId)
}
