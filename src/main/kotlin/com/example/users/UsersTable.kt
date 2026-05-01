package com.example.users

import org.jetbrains.exposed.v1.core.Table

object UsersTable : Table("users") {
    val id = integer("id").autoIncrement()
    val email = varchar("email", 255).uniqueIndex()
    val name = varchar("name", 120)

    override val primaryKey = PrimaryKey(id)
}
