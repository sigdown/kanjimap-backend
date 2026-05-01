package com.example.infra.database.tables

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table

object LearningBlockWordsTable : Table("learning_block_word") {
    val learningBlockId = long("learning_block_id").references(LearningBlocksTable.learningBlockId, onDelete = ReferenceOption.CASCADE)
    val wordId = long("word_id").references(WordsTable.wordId, onDelete = ReferenceOption.CASCADE)
    val orderIndex = integer("order_index").default(0)

    init {
        index("idx_learning_block_word_word_id", false, wordId)
    }

    override val primaryKey = PrimaryKey(learningBlockId, wordId)
}
