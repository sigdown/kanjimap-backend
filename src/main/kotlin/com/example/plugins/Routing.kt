package com.example.plugins

import com.example.users.UserRepository
import com.example.users.userRoutes
import io.ktor.server.application.Application
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun Application.configureRouting() {
    val userRepository = UserRepository()

    routing {
        get("/health") {
            call.respond(mapOf("status" to "ok"))
        }

        userRoutes(userRepository)
    }
}
