package com.example.database

import com.example.config.Config
import com.example.config.DatabaseConfig
import io.ktor.server.application.Application
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

fun Application.configureDatabase(config: Config) {
    val dataSource = DataSourceFactory.create(config)

    runMigrations(config.database)
    Database.connect(dataSource)

    transaction {
        exec("SELECT 1")
    }
}

private fun runMigrations(database: DatabaseConfig) {
    Flyway.configure()
        .dataSource(database.jdbcUrl, database.user, database.password)
        .locations("classpath:db/migration")
        .load()
        .migrate()
}
