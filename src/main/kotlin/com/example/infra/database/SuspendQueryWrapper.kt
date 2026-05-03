package com.example.infra.database

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

suspend fun <T> dbQuery(block: () -> T): T {
    return withContext(Dispatchers.IO) {
        transaction {
            block()
        }
    }
}