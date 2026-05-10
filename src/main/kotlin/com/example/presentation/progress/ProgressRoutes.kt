package com.example.presentation.progress

import com.example.application.dto.request.UpdateKanjiProgressRequest
import com.example.application.dto.request.UpdateWordProgressRequest
import com.example.application.usecase.progress.GetKanjiProgressUseCase
import com.example.application.usecase.progress.GetWordProgressUseCase
import com.example.application.usecase.progress.UpdateKanjiProgressUseCase
import com.example.application.usecase.progress.UpdateWordProgressUseCase
import com.example.infra.security.UserPrincipal
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.put
import io.ktor.server.routing.route

fun Route.progressRoutes() {
    authenticate("auth-jwt") {
        route("/api/progress") {
            get("/words/{id}") {
                val userId = call.currentUserId() ?: return@get
                val wordId = call.parameters["id"]?.toLongOrNull()
                if (wordId == null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Path parameter 'id' must be a number"))
                    return@get
                }

                val useCase = call.application.dependencies.resolve<GetWordProgressUseCase>()
                call.respond(HttpStatusCode.OK, useCase(userId, wordId))
            }

            get("/kanji/{id}") {
                val userId = call.currentUserId() ?: return@get
                val kanjiId = call.parameters["id"]?.toLongOrNull()
                if (kanjiId == null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Path parameter 'id' must be a number"))
                    return@get
                }

                val useCase = call.application.dependencies.resolve<GetKanjiProgressUseCase>()
                call.respond(HttpStatusCode.OK, useCase(userId, kanjiId))
            }

            put("/words/{id}") {
                val userId = call.currentUserId() ?: return@put
                val wordId = call.parameters["id"]?.toLongOrNull()
                if (wordId == null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Path parameter 'id' must be a number"))
                    return@put
                }

                val request = call.receive<UpdateWordProgressRequest>()
                val useCase = call.application.dependencies.resolve<UpdateWordProgressUseCase>()
                call.respond(HttpStatusCode.OK, useCase(userId, wordId, request))
            }

            put("/kanji/{id}") {
                val userId = call.currentUserId() ?: return@put
                val kanjiId = call.parameters["id"]?.toLongOrNull()
                if (kanjiId == null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Path parameter 'id' must be a number"))
                    return@put
                }

                val request = call.receive<UpdateKanjiProgressRequest>()
                val useCase = call.application.dependencies.resolve<UpdateKanjiProgressUseCase>()
                call.respond(HttpStatusCode.OK, useCase(userId, kanjiId, request))
            }
        }
    }
}

private suspend fun io.ktor.server.application.ApplicationCall.currentUserId(): Long? {
    val principal = principal<UserPrincipal>()
    if (principal == null) {
        respond(HttpStatusCode.Unauthorized, mapOf("error" to "Unauthorized"))
        return null
    }

    return principal.userId
}
