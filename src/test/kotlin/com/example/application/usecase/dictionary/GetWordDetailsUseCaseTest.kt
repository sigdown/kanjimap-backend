package com.example.application.usecase.dictionary

import com.example.domain.model.Kanji
import com.example.domain.model.Word
import com.example.domain.model.WordMeaning
import com.example.domain.model.WordRelation
import com.example.domain.model.WordRelationType
import com.example.domain.repository.WordRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

class GetWordDetailsUseCaseTest {

    private val wordRepository = mockk<WordRepository>()
    private val useCase = GetWordDetailsUseCase(wordRepository)

    @Test
    fun `should throw when word not found`() = runTest {
        coEvery { wordRepository.findById(1) } returns null
        assertFailsWith<NoSuchElementException> { useCase(1) }
    }

    @Test
    fun `should return full word details`() = runTest {
        coEvery { wordRepository.findById(1) } returns Word(1, "学校", "がっこう", "N5", "education")
        coEvery { wordRepository.getMeanings(1) } returns listOf(
            WordMeaning(1, 1, "школа", null, null, "noun"),
        )
        coEvery { wordRepository.getRelatedWords(1) } returns listOf(
            WordRelation(1, 2, WordRelationType.SIMILAR, "close meaning"),
        )
        coEvery { wordRepository.findById(2) } returns Word(2, "学園", "がくえん", "N4", "education")
        coEvery { wordRepository.getKanjis(1) } returns listOf(
            Kanji(10, "学", 8, "N5"),
        )

        val result = useCase(1)

        assertEquals("学校", result.word.writingForm)
        assertEquals(1, result.meanings.size)
        assertEquals("школа", result.meanings.first().meaning)
        assertEquals(1, result.relatedWords.size)
        assertEquals("similar", result.relatedWords.first().relationType)
        assertEquals("学園", result.relatedWords.first().word?.writingForm)
        assertEquals(1, result.kanjis.size)
        assertEquals("学", result.kanjis.first().literal)
    }
}
