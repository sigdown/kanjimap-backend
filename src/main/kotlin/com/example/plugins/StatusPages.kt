package com.example.plugins

import com.example.users.ErrorResponse
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
                message = ErrorResponse(cause.message ?: "Bad request"),
            )
        }

        exception<NoSuchElementException> { call, cause ->
            call.respond(
                status = HttpStatusCode.NotFound,
                message = ErrorResponse(cause.message ?: "Not found"),
            )
        }

        exception<Throwable> { call, cause ->
            call.application.environment.log.error("Unhandled error", cause)
            call.respond(
                status = HttpStatusCode.InternalServerError,
                message = ErrorResponse("Internal server error"),
            )
        }
    }
}
