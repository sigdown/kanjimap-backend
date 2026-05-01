package com.example.infra.database.tables

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table

object LearningBlockKanjisTable : Table("learning_block_kanji") {
    val learningBlockId = long("learning_block_id").references(LearningBlocksTable.learningBlockId, onDelete = ReferenceOption.CASCADE)
    val kanjiId = long("kanji_id").references(KanjisTable.kanjiId, onDelete = ReferenceOption.CASCADE)
    val orderIndex = integer("order_index").default(0)

    init {
        index("idx_learning_block_kanji_kanji_id", false, kanjiId)
    }

    override val primaryKey = PrimaryKey(learningBlockId, kanjiId)
}
