package com.example.infra.database.tables

import org.jetbrains.exposed.v1.core.Table

object LearningBlocksTable : Table("learning_block") {
    val learningBlockId = long("learning_block_id").autoIncrement()
    val title = varchar("title", 255)
    val description = text("description").nullable()
    val blockType = varchar("block_type", 20).default("mixed")
    val orderIndex = integer("order_index").default(0)
    val createdAt = text("created_at")

    init {
        index("idx_learning_block_order_index", false, orderIndex)
    }

    override val primaryKey = PrimaryKey(learningBlockId)
}
