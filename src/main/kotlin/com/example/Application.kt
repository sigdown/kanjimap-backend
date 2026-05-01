package com.example

import com.example.database.configureDatabase
import com.example.plugins.configureCallLogging
import com.example.plugins.configureRouting
import com.example.plugins.configureSerialization
import com.example.plugins.configureStatusPages
import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main() {
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8080

    embeddedServer(
        factory = Netty,
        port = port,
        host = "0.0.0.0",
        module = Application::module,
    ).start(wait = true)
}

fun Application.module() {
    configureCallLogging()
    configureSerialization()
    configureStatusPages()
    configureDatabase()
    configureRouting()
}
