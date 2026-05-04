package com.example.application.usecase.auth

import com.example.application.dto.response.UserProfileResponse
import com.example.domain.repository.UserRepository
import com.example.infra.security.PasswordHasher

class RegisterUserUseCase(
    private val userRepository: UserRepository,
    private val passwordHasher: PasswordHasher,
) {
    suspend operator fun invoke(username: String, email: String, password: String): UserProfileResponse {
        require(username.isNotBlank()) { "Username must not be blank" }
        require(email.isNotBlank()) { "Email must not be blank" }
        require(password.length >= 6) { "Password must be at least 6 characters" }

        if (userRepository.findByUsername(username.trim()) != null) {
            throw IllegalArgumentException("Username is already taken")
        }
        if (userRepository.findByEmail(email.trim()) != null) {
            throw IllegalArgumentException("Email is already registered")
        }

        val createdUser = userRepository.create(
            username = username.trim(),
            email = email.trim(),
            passwordHash = passwordHasher.hash(password),
        )

        return UserProfileResponse(
            userId = createdUser.userId,
            username = createdUser.username,
            email = createdUser.email,
            createdAt = createdUser.createdAt,
            updatedAt = createdUser.updatedAt,
        )
    }
}
