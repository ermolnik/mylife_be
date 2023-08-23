package ru.ermolnik

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import ru.ermolnik.plugins.configureDatabases
import ru.ermolnik.plugins.configureRouting
import ru.ermolnik.plugins.configureSecurity
import ru.ermolnik.plugins.configureSerialization

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureSerialization()
    configureDatabases()
    configureSecurity()
    configureRouting()
}
