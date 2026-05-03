package com.example.infra.security

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import java.util.Date

class JwtTokenProvider(
    private val config: JwtConfig,
) {
    private val algorithm = Algorithm.HMAC256(config.secret)

    fun generateToken(userId: Long, username: String, email: String): String {
        val now = System.currentTimeMillis()
        val expiresAt = Date(now + config.expirationMillis)

        return JWT.create()
            .withIssuer(config.issuer)
            .withAudience(config.audience)
            .withClaim("userId", userId)
            .withClaim("username", username)
            .withClaim("email", email)
            .withIssuedAt(Date(now))
            .withExpiresAt(expiresAt)
            .sign(algorithm)
    }

    fun verifier(): JWTVerifier = JWT
        .require(algorithm)
        .withIssuer(config.issuer)
        .withAudience(config.audience)
        .build()
}
