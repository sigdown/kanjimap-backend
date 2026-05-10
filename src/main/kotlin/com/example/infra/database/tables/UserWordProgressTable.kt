package com.example.infra.database.tables

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.timestamp

object UserWordProgressTable : Table("user_word_progress") {
    val userId = long("user_id").references(AppUsersTable.userId, onDelete = ReferenceOption.CASCADE)
    val wordId = long("word_id").references(WordsTable.wordId, onDelete = ReferenceOption.CASCADE)
    val status = varchar("status", 20).default("new")
    val correctNumber = integer("correct_number").default(0)
    val wrongNumber = integer("wrong_number").default(0)
    val repetitionLevel = integer("repetition_level").default(0)
    val lastReviewAt = timestamp("last_review_at").nullable()
    val nextReviewAt = timestamp("next_review_at").nullable()
    val updatedAt = timestamp("updated_at")

    init {
        index("idx_user_word_progress_next_review", false, userId, nextReviewAt)
    }

    override val primaryKey = PrimaryKey(userId, wordId)
}
