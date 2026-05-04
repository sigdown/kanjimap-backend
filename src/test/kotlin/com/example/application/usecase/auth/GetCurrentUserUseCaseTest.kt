package com.example.application.usecase.auth

import com.example.domain.model.User
import com.example.domain.repository.UserRepository
import io.mockk.coEvery
import io.mockk.mockk
import java.time.Instant
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

class GetCurrentUserUseCaseTest {

    private val userRepository = mockk<UserRepository>()
    private val useCase = GetCurrentUserUseCase(userRepository)

    @Test
    fun `should throw when user not found`() = runTest {
        coEvery { userRepository.findById(1) } returns null
        assertFailsWith<NoSuchElementException> { useCase(1) }
    }

    @Test
    fun `should map user profile`() = runTest {
        coEvery { userRepository.findById(1) } returns User(
            userId = 1,
            username = "vasya",
            email = "vasya@example.com",
            passwordHash = "hash",
            createdAt = Instant.parse("2026-01-01T00:00:00Z"),
            updatedAt = Instant.parse("2026-01-01T00:00:00Z"),
        )

        val result = useCase(1)
        assertEquals("vasya", result.username)
        assertEquals("vasya@example.com", result.email)
    }
}
