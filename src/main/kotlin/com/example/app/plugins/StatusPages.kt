package com.example.plugins

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<IllegalArgumentException> { call, cause ->
            call.respond(
                status = HttpStatusCode.BadRequest,
                message = mapOf("error" to (cause.message ?: "Bad request")),
            )
        }

        exception<NoSuchElementException> { call, cause ->
            call.respond(
                status = HttpStatusCode.NotFound,
                message = mapOf("error" to (cause.message ?: "Not found")),
            )
        }

        exception<Throwable> { call, cause ->
            call.application.environment.log.error("Unhandled error", cause)
            call.respond(
                status = HttpStatusCode.InternalServerError,
                message = mapOf("error" to "Internal server error"),
            )
        }
    }
}
