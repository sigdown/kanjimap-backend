package com.example.plugins

import com.example.infra.security.JwtConfig
import com.example.infra.security.JwtTokenProvider
import com.example.infra.security.UserPrincipal
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.jwt

fun Application.configureSecurity() {
    val jwtConfig = JwtConfig.load()
    val tokenProvider = JwtTokenProvider(jwtConfig)

    install(Authentication) {
        jwt("auth-jwt") {
            realm = jwtConfig.realm
            verifier(tokenProvider.verifier())
            validate { credential ->
                val userId = credential.payload.getClaim("userId").asLong() ?: return@validate null
                val username = credential.payload.getClaim("username").asString() ?: return@validate null
                val email = credential.payload.getClaim("email").asString() ?: return@validate null

                UserPrincipal(
                    userId = userId,
                    username = username,
                    email = email,
                )
            }
        }
    }
}
