package com.example.application.usecase.auth

import com.example.domain.model.User
import com.example.domain.repository.UserRepository
import com.example.infra.security.PasswordHasher
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import java.time.Instant
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

class RegisterUserUseCaseTest {

    private val userRepository = mockk<UserRepository>()
    private val passwordHasher = mockk<PasswordHasher>()
    private val useCase = RegisterUserUseCase(userRepository, passwordHasher)

    @Test
    fun `should throw when username already exists`() = runTest {
        coEvery { userRepository.findByUsername("vasya") } returns user(1, "vasya", "v@example.com")
        assertFailsWith<IllegalArgumentException> { useCase("vasya", "new@example.com", "123456") }
    }

    @Test
    fun `should create user with hashed password`() = runTest {
        coEvery { userRepository.findByUsername("vasya") } returns null
        coEvery { userRepository.findByEmail("vasya@example.com") } returns null
        every { passwordHasher.hash("123456") } returns "hashed"
        coEvery { userRepository.create("vasya", "vasya@example.com", "hashed") } returns user(10, "vasya", "vasya@example.com")

        val result = useCase("vasya", "vasya@example.com", "123456")

        assertEquals(10, result.userId)
        assertEquals("vasya", result.username)
        coVerify { userRepository.create("vasya", "vasya@example.com", "hashed") }
    }

    private fun user(id: Long, username: String, email: String): User = User(
        userId = id,
        username = username,
        email = email,
        passwordHash = "hash",
        createdAt = Instant.parse("2026-01-01T00:00:00Z"),
        updatedAt = Instant.parse("2026-01-01T00:00:00Z"),
    )
}
