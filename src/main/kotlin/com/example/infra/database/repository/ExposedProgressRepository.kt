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
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.upsert

class ExposedProgressRepository : ProgressRepository {
    override suspend fun getWordProgress(userId: Long, wordId: Long): UserWordProgress? = dbQuery {
        UserWordProgressTable
            .selectAll()
            .filter { row ->
                row[UserWordProgressTable.userId] == userId &&
                    row[UserWordProgressTable.wordId] == wordId
            }
            .map(::toUserWordProgress)
            .singleOrNull()
    }

    override suspend fun getKanjiProgress(userId: Long, kanjiId: Long): UserKanjiProgress? = dbQuery {
        UserKanjiProgressTable
            .selectAll()
            .filter { row ->
                row[UserKanjiProgressTable.userId] == userId &&
                    row[UserKanjiProgressTable.kanjiId] == kanjiId
            }
            .map(::toUserKanjiProgress)
            .singleOrNull()
    }

    override suspend fun saveWordProgress(progress: UserWordProgress) {
        dbQuery {
            UserWordProgressTable.upsert(UserWordProgressTable.userId, UserWordProgressTable.wordId) {
                it[status] = progress.status.name.lowercase()
                it[correctNumber] = progress.correctNumber
                it[wrongNumber] = progress.wrongNumber
                it[repetitionLevel] = progress.repetitionLevel
                it[lastReviewAt] = progress.lastReviewAt
                it[nextReviewAt] = progress.nextReviewAt
                it[updatedAt] = progress.updatedAt
                it[userId] = progress.userId
                it[wordId] = progress.wordId
            }
        }
    }

    override suspend fun saveKanjiProgress(progress: UserKanjiProgress) {
        dbQuery {
            UserKanjiProgressTable.upsert(UserKanjiProgressTable.userId, UserKanjiProgressTable.kanjiId) {
                it[status] = progress.status.name.lowercase()
                it[correctNumber] = progress.correctNumber
                it[wrongNumber] = progress.wrongNumber
                it[repetitionLevel] = progress.repetitionLevel
                it[lastReviewAt] = progress.lastReviewAt
                it[nextReviewAt] = progress.nextReviewAt
                it[updatedAt] = progress.updatedAt
                it[userId] = progress.userId
                it[kanjiId] = progress.kanjiId
            }
        }
    }

    override suspend fun getWordsForReview(userId: Long, now: Instant): List<UserWordProgress> = dbQuery {
        UserWordProgressTable
            .selectAll()
            .filter { row ->
                row[UserWordProgressTable.userId] == userId &&
                    row[UserWordProgressTable.nextReviewAt]?.let { !it.isAfter(now) } == true
            }
            .map(::toUserWordProgress)
    }

    override suspend fun getKanjisForReview(userId: Long, now: Instant): List<UserKanjiProgress> = dbQuery {
        UserKanjiProgressTable
            .selectAll()
            .filter { row ->
                row[UserKanjiProgressTable.userId] == userId &&
                    row[UserKanjiProgressTable.nextReviewAt]?.let { !it.isAfter(now) } == true
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
        lastReviewAt = row[UserWordProgressTable.lastReviewAt],
        nextReviewAt = row[UserWordProgressTable.nextReviewAt],
        updatedAt = row[UserWordProgressTable.updatedAt],
    )

    private fun toUserKanjiProgress(row: ResultRow): UserKanjiProgress = UserKanjiProgress(
        userId = row[UserKanjiProgressTable.userId],
        kanjiId = row[UserKanjiProgressTable.kanjiId],
        status = toProgressStatus(row[UserKanjiProgressTable.status]),
        correctNumber = row[UserKanjiProgressTable.correctNumber],
        wrongNumber = row[UserKanjiProgressTable.wrongNumber],
        repetitionLevel = row[UserKanjiProgressTable.repetitionLevel],
        lastReviewAt = row[UserKanjiProgressTable.lastReviewAt],
        nextReviewAt = row[UserKanjiProgressTable.nextReviewAt],
        updatedAt = row[UserKanjiProgressTable.updatedAt],
    )

    private fun toProgressStatus(value: String): ProgressStatus = when (value.lowercase()) {
        "learning" -> ProgressStatus.LEARNING
        "review" -> ProgressStatus.REVIEW
        "mastered" -> ProgressStatus.MASTERED
        else -> ProgressStatus.NEW
    }
}
