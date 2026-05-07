package com.example.plugins

import io.ktor.serialization.JsonConvertException
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.ContentTransformationException
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<BadRequestException> { call, _ ->
            call.respond(
                status = HttpStatusCode.BadRequest,
                message = mapOf("error" to "Malformed JSON request"),
            )
        }

        exception<ContentTransformationException> { call, _ ->
            call.respond(
                status = HttpStatusCode.BadRequest,
                message = mapOf("error" to "Malformed JSON request"),
            )
        }

        exception<JsonConvertException> { call, _ ->
            call.respond(
                status = HttpStatusCode.BadRequest,
                message = mapOf("error" to "Malformed JSON request"),
            )
        }

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
