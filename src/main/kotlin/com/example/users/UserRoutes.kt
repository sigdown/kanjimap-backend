package com.example.users

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route

fun Route.userRoutes(repository: UserRepository) {
    route("/api/v1/users") {
        get {
            call.respond(repository.findAll())
        }

        get("/{id}") {
            val id = call.userIdParam()
            val user = repository.findById(id)
                ?: return@get call.respond(HttpStatusCode.NotFound, ErrorResponse("User not found"))

            call.respond(user)
        }

        post {
            val request = call.receive<UserCreateRequest>()
            request.validate()

            val user = repository.create(request)
            call.respond(HttpStatusCode.Created, user)
        }

        put("/{id}") {
            val id = call.userIdParam()
            val request = call.receive<UserUpdateRequest>()
            request.validate()

            val updated = repository.update(id, request)
            if (!updated) {
                return@put call.respond(HttpStatusCode.NotFound, ErrorResponse("User not found"))
            }

            call.respond(HttpStatusCode.NoContent)
        }

        delete("/{id}") {
            val id = call.userIdParam()
            val deleted = repository.delete(id)
            if (!deleted) {
                return@delete call.respond(HttpStatusCode.NotFound, ErrorResponse("User not found"))
            }

            call.respond(HttpStatusCode.NoContent)
        }
    }
}

private fun io.ktor.server.application.ApplicationCall.userIdParam(): Int =
    parameters["id"]?.toIntOrNull()
        ?: throw IllegalArgumentException("Invalid user id")
