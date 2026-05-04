package com.example.infra.database.repository

import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class ExposedKanjiRepositoryTest : BaseRepositoryTest() {

    private val repository = ExposedKanjiRepository()

    @Test
    fun `findByLiteral should return inserted kanji`() = runTest {
        transaction {
            exec(
                """
                INSERT INTO kanji (kanji, stroke_count, jlpt_level)
                VALUES ('学', 8, 'N5');
                """.trimIndent()
            )
        }

        val result = repository.findByLiteral("学")

        assertEquals("学", result?.kanji)
        assertEquals(8, result?.strokeCount)
    }

    @Test
    fun `findById should return null when kanji does not exist`() = runTest {
        val result = repository.findById(999)
        assertNull(result)
    }

    @Test
    fun `getReadings and getMeanings should return data for kanji`() = runTest {
        transaction {
            exec("INSERT INTO kanji (kanji, stroke_count, jlpt_level) VALUES ('生', 5, 'N5');")
            exec("INSERT INTO kanji_reading (kanji_id, reading, reading_type) VALUES (1, 'セイ', 'on');")
            exec("INSERT INTO kanji_reading (kanji_id, reading, reading_type) VALUES (1, 'い', 'kun');")
            exec("INSERT INTO kanji_meaning (kanji_id, language_code, meaning, example) VALUES (1, 'rus', 'жизнь', NULL);")
        }

        val readings = repository.getReadings(1)
        val meanings = repository.getMeanings(1, "rus")

        assertEquals(2, readings.size)
        assertEquals(1, meanings.size)
        assertEquals("жизнь", meanings.first().meaning)
    }
}
