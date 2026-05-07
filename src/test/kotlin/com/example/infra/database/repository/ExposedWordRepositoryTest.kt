package com.example.infra.database.repository

import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class ExposedWordRepositoryTest : BaseRepositoryTest() {

    private val repository = ExposedWordRepository()

    @Test
    fun `search should return matching word`() = runTest {
        transaction {
            exec(
                """
                INSERT INTO word (writing_form, reading_kana, jlpt_level, topic_name)
                VALUES ('学校', 'がっこう', 'N5', 'education');
                """.trimIndent()
            )

            exec(
                """
                INSERT INTO word_meaning (
                    word_id,
                    meaning,
                    example_jp,
                    example_translation,
                    part_of_speech
                )
                VALUES (
                    1,
                    'школа',
                    '学校へ行きます。',
                    'Я иду в школу.',
                    'noun'
                );
                """.trimIndent()
            )
        }

        val result = repository.search("学")

        assertEquals(1, result.size)
        assertEquals("学校", result.first().writingForm)
        assertEquals("がっこう", result.first().readingKana)
    }

    @Test
    fun `findById should return null when word does not exist`() = runTest {
        val result = repository.findById(999)
        assertNull(result)
    }

    @Test
    fun `getMeanings should return meanings for word`() = runTest {
        transaction {
            exec(
                """
                INSERT INTO word (writing_form, reading_kana, jlpt_level, topic_name)
                VALUES ('先生', 'せんせい', 'N5', 'education');
                """.trimIndent()
            )

            exec(
                """
                INSERT INTO word_meaning (
                    word_id,
                    meaning,
                    example_jp,
                    example_translation,
                    part_of_speech
                )
                VALUES (
                    1,
                    'учитель',
                    '先生は優しいです。',
                    'Учитель добрый.',
                    'noun'
                );
                """.trimIndent()
            )
        }

        val result = repository.getMeanings(1)

        assertEquals(1, result.size)
        assertEquals("учитель", result.first().meaning)
    }
}