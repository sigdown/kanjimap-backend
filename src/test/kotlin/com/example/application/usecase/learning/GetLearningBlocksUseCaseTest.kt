package com.example.application.usecase.learning

import com.example.domain.model.LearningBlock
import com.example.domain.model.LearningBlockType
import com.example.domain.repository.LearningBlockRepository
import io.mockk.coEvery
import io.mockk.mockk
import java.time.Instant
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GetLearningBlocksUseCaseTest {

    private val repository = mockk<LearningBlockRepository>()
    private val useCase = GetLearningBlocksUseCase(repository)

    @Test
    fun `should map blocks list`() = runTest {
        coEvery { repository.findAll() } returns listOf(
            LearningBlock(1, "N5", "intro", LearningBlockType.WORD, 1, Instant.parse("2026-01-01T00:00:00Z")),
        )

        val result = useCase()

        assertEquals(1, result.size)
        assertEquals("N5", result.first().title)
        assertEquals("word", result.first().blockType)
    }
}
