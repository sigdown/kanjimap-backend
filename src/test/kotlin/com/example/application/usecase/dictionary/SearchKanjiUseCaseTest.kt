package com.example.application.usecase.dictionary

import com.example.domain.model.Kanji
import com.example.domain.repository.KanjiRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SearchKanjiUseCaseTest {

    private val kanjiRepository = mockk<KanjiRepository>()
    private val useCase = SearchKanjiUseCase(kanjiRepository)

    @Test
    fun `should return empty list for blank query`() = runTest {
        val result = useCase(" ")
        assertEquals(0, result.size)
        coVerify(exactly = 0) { kanjiRepository.search(any()) }
    }

    @Test
    fun `should map repository kanji list`() = runTest {
        coEvery { kanjiRepository.search("学") } returns listOf(Kanji(1, "学", 8, "N5"))

        val result = useCase("学")

        assertEquals(1, result.size)
        assertEquals("学", result.first().literal)
        assertEquals(8, result.first().strokeCount)
    }
}
