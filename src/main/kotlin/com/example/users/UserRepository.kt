package com.example.users

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update

class UserRepository {
    suspend fun findAll(): List<UserResponse> = dbQuery {
        UsersTable.selectAll()
            .orderBy(UsersTable.id)
            .map { it.toResponse() }
    }

    suspend fun findById(id: Int): UserResponse? = dbQuery {
        UsersTable.selectAll()
            .where { UsersTable.id eq id }
            .singleOrNull()
            ?.toResponse()
    }

    suspend fun create(request: UserCreateRequest): UserResponse = dbQuery {
        val id = UsersTable.insert { row ->
            row[email] = request.email
            row[name] = request.name
        }[UsersTable.id]

        UserResponse(
            id = id,
            email = request.email,
            name = request.name,
        )
    }

    suspend fun update(id: Int, request: UserUpdateRequest): Boolean = dbQuery {
        UsersTable.update({ UsersTable.id eq id }) { row ->
            request.email?.let { row[email] = it }
            request.name?.let { row[name] = it }
        } > 0
    }

    suspend fun delete(id: Int): Boolean = dbQuery {
        UsersTable.deleteWhere { UsersTable.id eq id } > 0
    }

    private fun ResultRow.toResponse(): UserResponse = UserResponse(
        id = this[UsersTable.id],
        email = this[UsersTable.email],
        name = this[UsersTable.name],
    )

    private suspend fun <T> dbQuery(block: () -> T): T = withContext(Dispatchers.IO) {
        transaction {
            block()
        }
    }
}
