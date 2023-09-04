package ru.ermolnik

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import ru.ermolnik.models.exposedb.*
import ru.ermolnik.plugins.*
import ru.ermolnik.plugins.configureSerialization

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureExposeDatabase()
    configureSerialization()
    configureSecurity()
    configureRouting()

    incomeRoutes(IncomeDAOImpl())
    purchaseRoutes(PurchaseDAOImpl())
    walletRoutes(WalletDAOImpl())
}
