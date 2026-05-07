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

    @Test
    fun `findById should return matching user when table has multiple rows`() = runTest {
        repository.create(
            username = "first",
            email = "first@example.com",
            passwordHash = "hash-1",
        )
        repository.create(
            username = "second",
            email = "second@example.com",
            passwordHash = "hash-2",
        )

        val result = repository.findById(2)

        assertEquals(2, result?.userId)
        assertEquals("second", result?.username)
    }
}
