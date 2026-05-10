package com.example.presentation.review

import com.example.application.dto.request.CheckKanjiAnswerRequest
import com.example.application.dto.request.CheckWordAnswerRequest
import com.example.application.usecase.review.CheckKanjiAnswerUseCase
import com.example.application.usecase.review.CheckWordAnswerUseCase
import com.example.application.usecase.review.GetReviewKanjiUseCase
import com.example.application.usecase.review.GetReviewWordsUseCase
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
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.reviewRoutes() {
    authenticate("auth-jwt") {
        route("/api/review") {
            get("/words") {
                val userId = call.currentUserId() ?: return@get
                val useCase = call.application.dependencies.resolve<GetReviewWordsUseCase>()
                call.respond(HttpStatusCode.OK, useCase(userId))
            }

            get("/kanji") {
                val userId = call.currentUserId() ?: return@get
                val useCase = call.application.dependencies.resolve<GetReviewKanjiUseCase>()
                call.respond(HttpStatusCode.OK, useCase(userId))
            }

            post("/words/{id}/check") {
                val userId = call.currentUserId() ?: return@post
                val wordId = call.parameters["id"]?.toLongOrNull()
                if (wordId == null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Path parameter 'id' must be a number"))
                    return@post
                }

                val request = call.receive<CheckWordAnswerRequest>()
                val useCase = call.application.dependencies.resolve<CheckWordAnswerUseCase>()
                call.respond(HttpStatusCode.OK, useCase(userId, wordId, request))
            }

            post("/kanji/{id}/check") {
                val userId = call.currentUserId() ?: return@post
                val kanjiId = call.parameters["id"]?.toLongOrNull()
                if (kanjiId == null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Path parameter 'id' must be a number"))
                    return@post
                }

                val request = call.receive<CheckKanjiAnswerRequest>()
                val useCase = call.application.dependencies.resolve<CheckKanjiAnswerUseCase>()
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
