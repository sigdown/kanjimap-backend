package com.example.infra.database.repository

import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class ExposedLearningBlockRepositoryTest : BaseRepositoryTest() {

    private val repository = ExposedLearningBlockRepository()

    @Test
    fun `findAll should return inserted blocks`() = runTest {
        transaction {
            exec(
                """
                INSERT INTO learning_block (title, description, block_type, order_index, created_at)
                VALUES ('N5 words', 'intro', 'word', 1, NOW());
                """.trimIndent()
            )
        }

        val result = repository.findAll()
        assertEquals(1, result.size)
        assertEquals("N5 words", result.first().title)
    }

    @Test
    fun `findById should return null when block does not exist`() = runTest {
        val result = repository.findById(999)
        assertNull(result)
    }

    @Test
    fun `findById should return matching block when table has multiple rows`() = runTest {
        transaction {
            exec("INSERT INTO learning_block (title, description, block_type, order_index, created_at) VALUES ('block-1', NULL, 'mixed', 1, NOW());")
            exec("INSERT INTO learning_block (title, description, block_type, order_index, created_at) VALUES ('block-2', NULL, 'word', 2, NOW());")
        }

        val result = repository.findById(2)

        assertEquals(2, result?.learningBlockId)
        assertEquals("block-2", result?.title)
    }

    @Test
    fun `getWords and getKanjis should return linked entities`() = runTest {
        transaction {
            exec("INSERT INTO learning_block (title, description, block_type, order_index, created_at) VALUES ('mixed-1', NULL, 'mixed', 1, NOW());")
            exec("INSERT INTO word (writing_form, reading_kana, jlpt_level, topic_name) VALUES ('学校', 'がっこう', 'N5', 'education');")
            exec("INSERT INTO kanji (kanji, stroke_count, jlpt_level) VALUES ('学', 8, 'N5');")
            exec("INSERT INTO learning_block_word (learning_block_id, word_id, order_index) VALUES (1, 1, 1);")
            exec("INSERT INTO learning_block_kanji (learning_block_id, kanji_id, order_index) VALUES (1, 1, 1);")
        }

        val words = repository.getWords(1)
        val kanjis = repository.getKanjis(1)

        assertEquals(1, words.size)
        assertEquals("学校", words.first().writingForm)
        assertEquals(1, kanjis.size)
        assertEquals("学", kanjis.first().kanji)
    }
}
