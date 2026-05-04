package com.example.infra.database.repository

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class ExposedUserRepositoryTest : BaseRepositoryTest() {

    private val repository = ExposedUserRepository()

    @Test
    fun `create should persist and return user`() = runTest {
        val created = repository.create(
            username = "vasya",
            email = "vasya@example.com",
            passwordHash = "hashed-password",
        )

        assertNotNull(created.userId)
        assertEquals("vasya", created.username)
        assertEquals("vasya@example.com", created.email)
    }

    @Test
    fun `findByEmail should return created user`() = runTest {
        repository.create(
            username = "petya",
            email = "petya@example.com",
            passwordHash = "hashed-password",
        )

        val found = repository.findByEmail("petya@example.com")

        assertEquals("petya", found?.username)
    }

    @Test
    fun `findById should return null for unknown id`() = runTest {
        val result = repository.findById(999)
        assertNull(result)
    }
}
