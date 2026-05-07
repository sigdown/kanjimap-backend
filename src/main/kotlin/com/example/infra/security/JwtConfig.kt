package com.example.infra.security

import io.github.cdimascio.dotenv.dotenv

data class JwtConfig(
    val secret: String,
    val issuer: String,
    val audience: String,
    val realm: String,
    val expirationMillis: Long,
) {
    companion object {
        private val dotenv = dotenv { ignoreIfMissing = true }

        fun load(): JwtConfig = JwtConfig(
            secret = optional("JWT_SECRET") ?: "dev-jwt-secret-change-me",
            issuer = optional("JWT_ISSUER") ?: "kanjimap-backend",
            audience = optional("JWT_AUDIENCE") ?: "kanjimap-clients",
            realm = optional("JWT_REALM") ?: "kanjimap-api",
            expirationMillis = (optional("JWT_EXPIRATION_MS") ?: "86400000").toLong(),
        )

        private fun optional(name: String): String? =
            System.getenv(name)?.takeIf { it.isNotBlank() }
                ?: dotenv[name]?.takeIf { it.isNotBlank() }
    }
}
