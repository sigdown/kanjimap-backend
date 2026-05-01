package com.example.config

import io.github.cdimascio.dotenv.dotenv

object AppConfig {
    private val dotenv = dotenv {
        ignoreIfMissing = true
    }

    fun load(): Config = Config(
        serverHost = optionalString("SERVER_HOST") ?: "0.0.0.0",
        serverPort = optionalInt("SERVER_PORT")
            ?: optionalInt("PORT")
            ?: 8080,
        databaseUrl = requiredString("DATABASE_JDBC_URL"),
        databaseUser = requiredString("DATABASE_USER"),
        databasePassword = requiredString("DATABASE_PASSWORD"),
        databaseMaximumPoolSize = optionalInt("DATABASE_MAX_POOL_SIZE") ?: 10,
    )

    private fun requiredString(name: String): String = optionalString(name)
        ?: throw IllegalStateException(
            "Missing required configuration '$name'. " +
                "Set it as an environment variable or in a local .env file.",
        )

    private fun optionalString(name: String): String? =
        System.getenv(name)?.takeIf { it.isNotBlank() }
            ?: dotenv[name]?.takeIf { it.isNotBlank() }

    private fun optionalInt(name: String): Int? {
        val value = optionalString(name) ?: return null
        return value.toIntOrNull()
            ?: throw IllegalStateException("Configuration '$name' must be a valid integer, got '$value'.")
    }
}

data class Config(
    val serverHost: String,
    val serverPort: Int,
    val databaseUrl: String,
    val databaseUser: String,
    val databasePassword: String,
    val databaseMaximumPoolSize: Int,
)
