@file:Suppress("DEPRECATION", "DEPRECATION_ERROR")

package com.example.infra.database.repository

import com.example.domain.model.ProgressStatus
import com.example.domain.model.UserKanjiProgress
import com.example.domain.model.UserWordProgress
import com.example.domain.repository.ProgressRepository
import com.example.infra.database.dbQuery
import com.example.infra.database.tables.UserKanjiProgressTable
import com.example.infra.database.tables.UserWordProgressTable
import java.time.Instant
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update

class ExposedProgressRepository : ProgressRepository {
    override suspend fun getWordProgress(userId: Long, wordId: Long): UserWordProgress? = dbQuery {
        UserWordProgressTable
            .selectAll()
            .where {
                (UserWordProgressTable.userId eq userId) and
                    (UserWordProgressTable.wordId eq wordId)
            }
            .limit(1)
            .map(::toUserWordProgress)
            .singleOrNull()
    }

    override suspend fun getKanjiProgress(userId: Long, kanjiId: Long): UserKanjiProgress? = dbQuery {
        UserKanjiProgressTable
            .selectAll()
            .where {
                (UserKanjiProgressTable.userId eq userId) and
                    (UserKanjiProgressTable.kanjiId eq kanjiId)
            }
            .limit(1)
            .map(::toUserKanjiProgress)
            .singleOrNull()
    }

    override suspend fun saveWordProgress(progress: UserWordProgress) {
        dbQuery {
            val updated = UserWordProgressTable.update({
                (UserWordProgressTable.userId eq progress.userId) and
                    (UserWordProgressTable.wordId eq progress.wordId)
            }) {
                it[status] = progress.status.name.lowercase()
                it[correctNumber] = progress.correctNumber
                it[wrongNumber] = progress.wrongNumber
                it[repetitionLevel] = progress.repetitionLevel
                it[lastReviewAt] = progress.lastReviewAt?.toString()
                it[nextReviewAt] = progress.nextReviewAt?.toString()
                it[updatedAt] = progress.updatedAt.toString()
            }

            if (updated == 0) {
                UserWordProgressTable.insert {
                    it[userId] = progress.userId
                    it[wordId] = progress.wordId
                    it[status] = progress.status.name.lowercase()
                    it[correctNumber] = progress.correctNumber
                    it[wrongNumber] = progress.wrongNumber
                    it[repetitionLevel] = progress.repetitionLevel
                    it[lastReviewAt] = progress.lastReviewAt?.toString()
                    it[nextReviewAt] = progress.nextReviewAt?.toString()
                    it[updatedAt] = progress.updatedAt.toString()
                }
            }
        }
    }

    override suspend fun saveKanjiProgress(progress: UserKanjiProgress) {
        dbQuery {
            val updated = UserKanjiProgressTable.update({
                (UserKanjiProgressTable.userId eq progress.userId) and
                    (UserKanjiProgressTable.kanjiId eq progress.kanjiId)
            }) {
                it[status] = progress.status.name.lowercase()
                it[correctNumber] = progress.correctNumber
                it[wrongNumber] = progress.wrongNumber
                it[repetitionLevel] = progress.repetitionLevel
                it[lastReviewAt] = progress.lastReviewAt?.toString()
                it[nextReviewAt] = progress.nextReviewAt?.toString()
                it[updatedAt] = progress.updatedAt.toString()
            }

            if (updated == 0) {
                UserKanjiProgressTable.insert {
                    it[userId] = progress.userId
                    it[kanjiId] = progress.kanjiId
                    it[status] = progress.status.name.lowercase()
                    it[correctNumber] = progress.correctNumber
                    it[wrongNumber] = progress.wrongNumber
                    it[repetitionLevel] = progress.repetitionLevel
                    it[lastReviewAt] = progress.lastReviewAt?.toString()
                    it[nextReviewAt] = progress.nextReviewAt?.toString()
                    it[updatedAt] = progress.updatedAt.toString()
                }
            }
        }
    }

    override suspend fun getWordsForReview(userId: Long, now: Instant): List<UserWordProgress> = dbQuery {
        UserWordProgressTable
            .selectAll()
            .where {
                (UserWordProgressTable.userId eq userId) and
                    (UserWordProgressTable.nextReviewAt lessEq now.toString())
            }
            .map(::toUserWordProgress)
    }

    override suspend fun getKanjisForReview(userId: Long, now: Instant): List<UserKanjiProgress> = dbQuery {
        UserKanjiProgressTable
            .selectAll()
            .where {
                (UserKanjiProgressTable.userId eq userId) and
                    (UserKanjiProgressTable.nextReviewAt lessEq now.toString())
            }
            .map(::toUserKanjiProgress)
    }

    private fun toUserWordProgress(row: ResultRow): UserWordProgress = UserWordProgress(
        userId = row[UserWordProgressTable.userId],
        wordId = row[UserWordProgressTable.wordId],
        status = toProgressStatus(row[UserWordProgressTable.status]),
        correctNumber = row[UserWordProgressTable.correctNumber],
        wrongNumber = row[UserWordProgressTable.wrongNumber],
        repetitionLevel = row[UserWordProgressTable.repetitionLevel],
        lastReviewAt = row[UserWordProgressTable.lastReviewAt]?.let(Instant::parse),
        nextReviewAt = row[UserWordProgressTable.nextReviewAt]?.let(Instant::parse),
        updatedAt = Instant.parse(row[UserWordProgressTable.updatedAt]),
    )

    private fun toUserKanjiProgress(row: ResultRow): UserKanjiProgress = UserKanjiProgress(
        userId = row[UserKanjiProgressTable.userId],
        kanjiId = row[UserKanjiProgressTable.kanjiId],
        status = toProgressStatus(row[UserKanjiProgressTable.status]),
        correctNumber = row[UserKanjiProgressTable.correctNumber],
        wrongNumber = row[UserKanjiProgressTable.wrongNumber],
        repetitionLevel = row[UserKanjiProgressTable.repetitionLevel],
        lastReviewAt = row[UserKanjiProgressTable.lastReviewAt]?.let(Instant::parse),
        nextReviewAt = row[UserKanjiProgressTable.nextReviewAt]?.let(Instant::parse),
        updatedAt = Instant.parse(row[UserKanjiProgressTable.updatedAt]),
    )

    private fun toProgressStatus(value: String): ProgressStatus = when (value.lowercase()) {
        "learning" -> ProgressStatus.LEARNING
        "review" -> ProgressStatus.REVIEW
        "mastered" -> ProgressStatus.MASTERED
        else -> ProgressStatus.NEW
    }
}
