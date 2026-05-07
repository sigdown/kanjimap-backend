package com.example.presentation.dictionary

import com.example.application.usecase.dictionary.GetKanjiDetailsUseCase
import com.example.application.usecase.dictionary.GetWordDetailsUseCase
import com.example.application.usecase.dictionary.SearchKanjiUseCase
import com.example.application.usecase.dictionary.SearchWordsUseCase
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route

fun Route.dictionaryRoutes() {
    route("/api") {
        get("/words") {
            val query = call.request.queryParameters["query"]?.trim().orEmpty()
            if (query.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Query parameter 'query' is required"))
                return@get
            }

            val useCase = call.application.dependencies.resolve<SearchWordsUseCase>()
            call.respond(HttpStatusCode.OK, useCase(query))
        }

        get("/words/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Path parameter 'id' must be a number"))
                return@get
            }

            val useCase = call.application.dependencies.resolve<GetWordDetailsUseCase>()
            call.respond(HttpStatusCode.OK, useCase(id))
        }

        get("/kanji") {
            val query = call.request.queryParameters["query"]?.trim().orEmpty()
            if (query.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Query parameter 'query' is required"))
                return@get
            }

            val useCase = call.application.dependencies.resolve<SearchKanjiUseCase>()
            call.respond(HttpStatusCode.OK, useCase(query))
        }

        get("/kanji/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Path parameter 'id' must be a number"))
                return@get
            }

            val useCase = call.application.dependencies.resolve<GetKanjiDetailsUseCase>()
            call.respond(HttpStatusCode.OK, useCase(id))
        }
    }
}
