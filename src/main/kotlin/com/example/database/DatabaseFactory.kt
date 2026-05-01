package com.example.database

import com.example.config.AppConfig
import io.ktor.server.application.Application
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import javax.sql.DataSource

fun Application.configureDatabase() {
    val databaseConfig = AppConfig.from(environment.config)
    val dataSource = DataSourceFactory.create(databaseConfig)

    runMigrations(dataSource)
    Database.connect(dataSource)

    transaction {
        exec("SELECT 1")
    }
}

private fun runMigrations(dataSource: DataSource) {
    Flyway.configure()
        .dataSource(dataSource)
        .locations("classpath:db/migration")
        .load()
        .migrate()
}
