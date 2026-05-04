package com.example.infra.database.repository

import com.example.domain.model.ProgressStatus
import com.example.domain.model.UserKanjiProgress
import com.example.domain.model.UserWordProgress
import java.time.Instant
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class ExposedProgressRepositoryTest : BaseRepositoryTest() {

    private val repository = ExposedProgressRepository()

    @Test
    fun `getWordProgress should return null for missing row`() = runTest {
        val result = repository.getWordProgress(1, 1)
        assertNull(result)
    }

    @Test
    fun `saveWordProgress should insert and then update progress`() = runTest {
        transaction {
            exec("INSERT INTO app_user (username, email, password_hash, created_at, updated_at) VALUES ('u1', 'u1@example.com', 'hash', NOW()::text, NOW()::text);")
            exec("INSERT INTO word (writing_form, reading_kana, jlpt_level, topic_name) VALUES ('先生', 'せんせい', 'N5', 'education');")
        }

        val first = UserWordProgress(
            userId = 1,
            wordId = 1,
            status = ProgressStatus.NEW,
            correctNumber = 0,
            wrongNumber = 0,
            repetitionLevel = 0,
            lastReviewAt = null,
            nextReviewAt = Instant.now().minusSeconds(60),
            updatedAt = Instant.now(),
        )
        repository.saveWordProgress(first)

        val second = first.copy(status = ProgressStatus.LEARNING, correctNumber = 1, updatedAt = Instant.now())
        repository.saveWordProgress(second)

        val loaded = repository.getWordProgress(1, 1)
        assertNotNull(loaded)
        assertEquals(ProgressStatus.LEARNING, loaded?.status)
        assertEquals(1, loaded?.correctNumber)
    }

    @Test
    fun `getKanjisForReview should return due progress`() = runTest {
        transaction {
            exec("INSERT INTO app_user (username, email, password_hash, created_at, updated_at) VALUES ('u2', 'u2@example.com', 'hash', NOW()::text, NOW()::text);")
            exec("INSERT INTO kanji (kanji, stroke_count, jlpt_level) VALUES ('生', 5, 'N5');")
        }

        repository.saveKanjiProgress(
            UserKanjiProgress(
                userId = 1,
                kanjiId = 1,
                status = ProgressStatus.REVIEW,
                correctNumber = 2,
                wrongNumber = 1,
                repetitionLevel = 1,
                lastReviewAt = Instant.now().minusSeconds(600),
                nextReviewAt = Instant.now().minusSeconds(60),
                updatedAt = Instant.now(),
            ),
        )

        val due = repository.getKanjisForReview(1, Instant.now())
        assertEquals(1, due.size)
        assertEquals(ProgressStatus.REVIEW, due.first().status)
    }
}
