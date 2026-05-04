package com.example.application.usecase.dictionary

import com.example.domain.model.Word
import com.example.domain.repository.WordRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SearchWordsUseCaseTest {

    private val wordRepository = mockk<WordRepository>()
    private val useCase = SearchWordsUseCase(wordRepository)

    @Test
    fun `should return empty list for blank query`() = runTest {
        val result = useCase(" ")
        assertEquals(0, result.size)
        coVerify(exactly = 0) { wordRepository.search(any()) }
    }

    @Test
    fun `should map repository words`() = runTest {
        coEvery { wordRepository.search("学") } returns listOf(
            Word(1, "学校", "がっこう", "N5", "education"),
        )

        val result = useCase("学")

        assertEquals(1, result.size)
        assertEquals("学校", result.first().writingForm)
        assertEquals("がっこう", result.first().readingKana)
    }
}
