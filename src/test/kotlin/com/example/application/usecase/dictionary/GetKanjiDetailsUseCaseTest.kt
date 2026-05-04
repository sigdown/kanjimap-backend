package com.example.application.usecase.dictionary

import com.example.domain.model.Kanji
import com.example.domain.model.KanjiMeaning
import com.example.domain.model.KanjiReading
import com.example.domain.model.KanjiReadingType
import com.example.domain.model.Word
import com.example.domain.repository.KanjiRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

class GetKanjiDetailsUseCaseTest {

    private val kanjiRepository = mockk<KanjiRepository>()
    private val useCase = GetKanjiDetailsUseCase(kanjiRepository)

    @Test
    fun `should throw when kanji not found`() = runTest {
        coEvery { kanjiRepository.findById(1) } returns null
        assertFailsWith<NoSuchElementException> { useCase(1) }
    }

    @Test
    fun `should split readings and map details`() = runTest {
        coEvery { kanjiRepository.findById(1) } returns Kanji(1, "生", 5, "N5")
        coEvery { kanjiRepository.getReadings(1) } returns listOf(
            KanjiReading(1, 1, "セイ", KanjiReadingType.ON),
            KanjiReading(2, 1, "い", KanjiReadingType.KUN),
            KanjiReading(3, 1, "み", KanjiReadingType.NANORI),
        )
        coEvery { kanjiRepository.getMeanings(1, "rus") } returns listOf(
            KanjiMeaning(1, 1, "rus", "жизнь", null),
        )
        coEvery { kanjiRepository.getWords(1) } returns listOf(
            Word(10, "先生", "せんせい", "N5", "education"),
        )

        val result = useCase(1)

        assertEquals("生", result.kanji.literal)
        assertEquals(listOf("セイ"), result.onReadings)
        assertEquals(listOf("い"), result.kunReadings)
        assertEquals(listOf("み"), result.nanoriReadings)
        assertEquals("жизнь", result.meanings.first().meaning)
        assertEquals("先生", result.words.first().writingForm)
    }
}
