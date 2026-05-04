package com.example.application.usecase.learning

import com.example.domain.model.Kanji
import com.example.domain.model.LearningBlock
import com.example.domain.model.LearningBlockType
import com.example.domain.model.Word
import com.example.domain.repository.LearningBlockRepository
import io.mockk.coEvery
import io.mockk.mockk
import java.time.Instant
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

class GetLearningBlockDetailsUseCaseTest {

    private val repository = mockk<LearningBlockRepository>()
    private val useCase = GetLearningBlockDetailsUseCase(repository)

    @Test
    fun `should throw when block not found`() = runTest {
        coEvery { repository.findById(1) } returns null
        assertFailsWith<NoSuchElementException> { useCase(1) }
    }

    @Test
    fun `should map block words and kanjis`() = runTest {
        coEvery { repository.findById(1) } returns LearningBlock(
            1, "Mixed", null, LearningBlockType.MIXED, 1, Instant.parse("2026-01-01T00:00:00Z"),
        )
        coEvery { repository.getWords(1) } returns listOf(
            Word(11, "学校", "がっこう", "N5", "education"),
        )
        coEvery { repository.getKanjis(1) } returns listOf(
            Kanji(22, "学", 8, "N5"),
        )

        val result = useCase(1)

        assertEquals("Mixed", result.block.title)
        assertEquals(1, result.words.size)
        assertEquals("学校", result.words.first().writingForm)
        assertEquals(1, result.kanjis.size)
        assertEquals("学", result.kanjis.first().literal)
    }
}
