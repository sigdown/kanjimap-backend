package com.example.presentation.auth

import com.example.application.dto.request.LoginRequest
import com.example.application.dto.request.RegisterRequest
import com.example.application.usecase.auth.GetCurrentUserUseCase
import com.example.application.usecase.auth.LoginUserUseCase
import com.example.application.usecase.auth.RegisterUserUseCase
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

fun Route.authRoutes() {
    route("/api/auth") {
        post("/register") {
            val request = call.receive<RegisterRequest>()
            val useCase = call.application.dependencies.resolve<RegisterUserUseCase>()
            val createdUser = useCase(
                username = request.username,
                email = request.email,
                password = request.password,
            )
            call.respond(HttpStatusCode.Created, createdUser)
        }

        post("/login") {
            val request = call.receive<LoginRequest>()
            val useCase = call.application.dependencies.resolve<LoginUserUseCase>()
            try {
                val response = useCase(request.login, request.password)
                call.respond(HttpStatusCode.OK, response)
            } catch (cause: IllegalArgumentException) {
                if (cause.message == "Invalid credentials") {
                    call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid credentials"))
                } else {
                    throw cause
                }
            }
        }

        authenticate("auth-jwt") {
            get("/me") {
                val principal = call.principal<UserPrincipal>()
                if (principal == null) {
                    call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Unauthorized"))
                    return@get
                }

                val useCase = call.application.dependencies.resolve<GetCurrentUserUseCase>()
                call.respond(HttpStatusCode.OK, useCase(principal.userId))
            }
        }
    }
}
