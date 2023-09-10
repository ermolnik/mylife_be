package ru.ermolnik.plugins

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import ru.ermolnik.models.*

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Hello World!")
        }
    }

    incomeRoutes(IncomeDAOImpl())
    purchaseRoutes(PurchaseDAOImpl())
    walletRoutes(WalletDAOImpl())
}
