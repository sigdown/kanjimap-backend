package com.example.application.usecase.auth

import com.example.domain.model.User
import com.example.domain.repository.UserRepository
import com.example.infra.security.JwtTokenProvider
import com.example.infra.security.PasswordHasher
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import java.time.Instant
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

class LoginUserUseCaseTest {

    private val userRepository = mockk<UserRepository>()
    private val passwordHasher = mockk<PasswordHasher>()
    private val jwtTokenProvider = mockk<JwtTokenProvider>()
    private val useCase = LoginUserUseCase(userRepository, passwordHasher, jwtTokenProvider)

    @Test
    fun `should throw when credentials are invalid`() = runTest {
        coEvery { userRepository.findByEmail("nope") } returns null
        coEvery { userRepository.findByUsername("nope") } returns null

        assertFailsWith<IllegalArgumentException> { useCase("nope", "123456") }
    }

    @Test
    fun `should return auth response for valid credentials`() = runTest {
        val user = user(1, "vasya", "vasya@example.com")
        coEvery { userRepository.findByEmail("vasya@example.com") } returns user
        every { passwordHasher.verify("123456", "hash") } returns true
        every { jwtTokenProvider.generateToken(1, "vasya", "vasya@example.com") } returns "token-1"

        val result = useCase("vasya@example.com", "123456")

        assertEquals("token-1", result.accessToken)
        assertEquals("vasya", result.user.username)
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
