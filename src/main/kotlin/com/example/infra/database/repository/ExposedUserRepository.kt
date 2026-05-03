@file:Suppress("DEPRECATION", "DEPRECATION_ERROR")

package com.example.infra.database.repository

import com.example.domain.model.User
import com.example.domain.repository.UserRepository
import com.example.infra.database.dbQuery
import com.example.infra.database.tables.AppUsersTable
import java.time.Instant
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll

class ExposedUserRepository : UserRepository {
    override suspend fun findById(id: Long): User? = dbQuery {
        AppUsersTable
            .selectAll()
            .where { AppUsersTable.userId eq id }
            .limit(1)
            .map(::toUser)
            .singleOrNull()
    }

    override suspend fun findByEmail(email: String): User? = dbQuery {
        AppUsersTable
            .selectAll()
            .where { AppUsersTable.email eq email }
            .limit(1)
            .map(::toUser)
            .singleOrNull()
    }

    override suspend fun findByUsername(username: String): User? = dbQuery {
        AppUsersTable
            .selectAll()
            .where { AppUsersTable.username eq username }
            .limit(1)
            .map(::toUser)
            .singleOrNull()
    }

    override suspend fun create(username: String, email: String, passwordHash: String): User = dbQuery {
        val now = Instant.now().toString()
        val id = AppUsersTable
            .insert {
                it[AppUsersTable.username] = username
                it[AppUsersTable.email] = email
                it[AppUsersTable.passwordHash] = passwordHash
                it[createdAt] = now
                it[updatedAt] = now
            }
            .resultedValues
            ?.firstOrNull()
            ?.get(AppUsersTable.userId)
            ?: error("Failed to create user")

        AppUsersTable
            .selectAll()
            .where { AppUsersTable.userId eq id }
            .limit(1)
            .map(::toUser)
            .single()
    }

    private fun toUser(row: ResultRow): User = User(
        userId = row[AppUsersTable.userId],
        username = row[AppUsersTable.username],
        email = row[AppUsersTable.email],
        passwordHash = row[AppUsersTable.passwordHash],
        createdAt = Instant.parse(row[AppUsersTable.createdAt]),
        updatedAt = Instant.parse(row[AppUsersTable.updatedAt]),
    )
}
