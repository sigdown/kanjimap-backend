package com.example.plugins

import com.example.presentation.auth.authRoutes
import com.example.presentation.dictionary.dictionaryRoutes
import com.example.presentation.learning.learningRoutes
import io.ktor.server.application.Application
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun Application.configureRouting() {
    routing {
        get("/health") {
            call.respond(mapOf("status" to "ok"))
        }

        dictionaryRoutes()
        authRoutes()
        learningRoutes()
    }
}
