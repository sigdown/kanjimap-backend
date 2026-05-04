package com.example.application.usecase.auth

import com.example.application.dto.response.UserProfileResponse
import com.example.domain.repository.UserRepository

class GetCurrentUserUseCase(
    private val userRepository: UserRepository,
) {
    suspend operator fun invoke(userId: Long): UserProfileResponse {
        val user = userRepository.findById(userId)
            ?: throw NoSuchElementException("User with id=$userId not found")

        return UserProfileResponse(
            userId = user.userId,
            username = user.username,
            email = user.email,
            createdAt = user.createdAt,
            updatedAt = user.updatedAt,
        )
    }
}
