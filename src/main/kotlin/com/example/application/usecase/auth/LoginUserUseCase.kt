package com.example.application.usecase.auth

import com.example.application.dto.response.AuthResponse
import com.example.application.dto.response.UserProfileResponse
import com.example.domain.repository.UserRepository
import com.example.infra.security.JwtTokenProvider
import com.example.infra.security.PasswordHasher

class LoginUserUseCase(
    private val userRepository: UserRepository,
    private val passwordHasher: PasswordHasher,
    private val jwtTokenProvider: JwtTokenProvider,
) {
    suspend operator fun invoke(login: String, password: String): AuthResponse {
        require(login.isNotBlank()) { "Login must not be blank" }
        require(password.isNotBlank()) { "Password must not be blank" }

        val user = userRepository.findByEmail(login.trim())
            ?: userRepository.findByUsername(login.trim())
            ?: throw IllegalArgumentException("Invalid credentials")

        if (!passwordHasher.verify(password, user.passwordHash)) {
            throw IllegalArgumentException("Invalid credentials")
        }

        val token = jwtTokenProvider.generateToken(
            userId = user.userId,
            username = user.username,
            email = user.email,
        )

        return AuthResponse(
            accessToken = token,
            user = UserProfileResponse(
                userId = user.userId,
                username = user.username,
                email = user.email,
                createdAt = user.createdAt,
                updatedAt = user.updatedAt,
            ),
        )
    }
}
