package com.example.presentation.learning

import com.example.application.usecase.learning.GetLearningBlockDetailsUseCase
import com.example.application.usecase.learning.GetLearningBlocksUseCase
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route

fun Route.learningRoutes() {
    route("/api/learning") {
        get("/blocks") {
            val useCase = call.application.dependencies.resolve<GetLearningBlocksUseCase>()
            call.respond(HttpStatusCode.OK, useCase())
        }

        get("/blocks/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Path parameter 'id' must be a number"))
                return@get
            }

            val useCase = call.application.dependencies.resolve<GetLearningBlockDetailsUseCase>()
            call.respond(HttpStatusCode.OK, useCase(id))
        }
    }
}
