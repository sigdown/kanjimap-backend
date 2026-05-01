package com.example.infra.database.tables

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table

object UserKanjiProgressTable : Table("user_kanji_progress") {
    val userId = long("user_id").references(AppUsersTable.userId, onDelete = ReferenceOption.CASCADE)
    val kanjiId = long("kanji_id").references(KanjisTable.kanjiId, onDelete = ReferenceOption.CASCADE)
    val status = varchar("status", 20).default("new")
    val correctNumber = integer("correct_number").default(0)
    val wrongNumber = integer("wrong_number").default(0)
    val repetitionLevel = integer("repetition_level").default(0)
    val lastReviewAt = text("last_review_at").nullable()
    val nextReviewAt = text("next_review_at").nullable()
    val updatedAt = text("updated_at")

    init {
        index("idx_user_kanji_progress_next_review", false, userId, nextReviewAt)
    }

    override val primaryKey = PrimaryKey(userId, kanjiId)
}
