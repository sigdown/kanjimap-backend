package com.example.infra.security

import io.ktor.server.auth.Principal

data class UserPrincipal(
    val userId: Long,
    val username: String,
    val email: String,
) : Principal
