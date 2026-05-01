package com.example.config

import io.ktor.server.config.ApplicationConfig

object AppConfig {
    fun from(config: ApplicationConfig): DatabaseConfig = DatabaseConfig(
        jdbcUrl = readString(config, "database.jdbcUrl", "DATABASE_JDBC_URL", "jdbc:postgresql://localhost:5432/app"),
        username = readString(config, "database.username", "DATABASE_USER", "app"),
        password = readString(config, "database.password", "DATABASE_PASSWORD", "app"),
        maximumPoolSize = readInt(config, "database.maximumPoolSize", "DATABASE_MAX_POOL_SIZE", 10),
    )

    private fun readString(
        config: ApplicationConfig,
        path: String,
        envName: String,
        defaultValue: String,
    ): String = System.getenv(envName)
        ?: config.propertyOrNull(path)?.getString()
        ?: defaultValue

    private fun readInt(
        config: ApplicationConfig,
        path: String,
        envName: String,
        defaultValue: Int,
    ): Int = System.getenv(envName)?.toIntOrNull()
        ?: config.propertyOrNull(path)?.getString()?.toIntOrNull()
        ?: defaultValue
}

data class DatabaseConfig(
    val jdbcUrl: String,
    val username: String,
    val password: String,
    val maximumPoolSize: Int,
)
