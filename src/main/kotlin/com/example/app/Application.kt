package com.example

import com.example.config.AppConfig
import com.example.config.Config
import com.example.database.configureDatabase
import com.example.plugins.configureCallLogging
import com.example.plugins.configureDI
import com.example.plugins.configureRouting
import com.example.plugins.configureSecurity
import com.example.plugins.configureSerialization
import com.example.plugins.configureStatusPages
import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main() {
    val config = AppConfig.load()

    embeddedServer(
        factory = Netty,
        port = config.serverPort,
        host = config.serverHost,
        module = { module(config) },
    ).start(wait = true)
}

fun Application.module(config: Config = AppConfig.load()) {
    configureCallLogging()
    configureSerialization()
    configureStatusPages()
    configureDI()
    configureSecurity()
    configureDatabase(config)
    configureRouting()
}
