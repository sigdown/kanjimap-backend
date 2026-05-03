package com.example.domain.repository

import com.example.domain.model.User

interface UserRepository {
    suspend fun findById(id: Long): User?
    suspend fun findByEmail(email: String): User?
    suspend fun findByUsername(username: String): User?
    suspend fun create(username: String, email: String, passwordHash: String): User
}
