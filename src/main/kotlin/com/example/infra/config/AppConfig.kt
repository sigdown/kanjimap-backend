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
        database = DatabaseConfig(
            host = requiredString("DATABASE_HOST"),
            port = optionalInt("DATABASE_PORT") ?: 5432,
            name = requiredString("DATABASE_NAME"),
            user = requiredString("DATABASE_USER"),
            password = requiredString("DATABASE_PASSWORD"),
            maxPoolSize = optionalInt("DATABASE_MAX_POOL_SIZE") ?: 10,
        ),
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
    val database: DatabaseConfig,
)

data class DatabaseConfig(
    val host: String,
    val port: Int,
    val name: String,
    val user: String,
    val password: String,
    val maxPoolSize: Int,
) {
    val jdbcUrl: String
        get() = "jdbc:postgresql://$host:$port/$name"
}
