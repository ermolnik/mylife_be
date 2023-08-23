package ru.ermolnik.plugins

import com.typesafe.config.ConfigFactory
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import ru.ermolnik.models.*
import java.sql.Connection
import java.sql.DriverManager

fun Application.configureDatabases() {
    val dbConnection: Connection = connectToPostgres()
    incomeRoutes(dbConnection)
    purchaseRoutes(dbConnection)
    walletRoutes(dbConnection)
}

fun Application.incomeRoutes(connection: Connection) {
    val incomeService = IncomeService(connection)
    routing {

        post("/incomes") {
            val income = call.receive<Income>()
            val id = incomeService.create(income)
            call.respond(HttpStatusCode.Created, id)
        }
        get("/incomes/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            try {
                val income = incomeService.read(id)
                call.respond(HttpStatusCode.OK, income)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.NotFound)
            }
        }
        put("/incomes/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            val income = call.receive<Income>()
            incomeService.update(id, income)
            call.respond(HttpStatusCode.OK)
        }
        delete("/incomes/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            incomeService.delete(id)
            call.respond(HttpStatusCode.OK)
        }
    }
}

fun Application.purchaseRoutes(connection: Connection) {
    val purchaseService = PurchaseService(connection)
    routing {

        post("/purchases") {
            val purchase = call.receive<Purchase>()
            val id = purchaseService.create(purchase)
            call.respond(HttpStatusCode.Created, id)
        }
        get("/purchases/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            try {
                val purchase = purchaseService.read(id)
                call.respond(HttpStatusCode.OK, purchase)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.NotFound)
            }
        }
        put("/purchases/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            val purchase = call.receive<Purchase>()
            purchaseService.update(id, purchase)
            call.respond(HttpStatusCode.OK)
        }
        delete("/incomes/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            purchaseService.delete(id)
            call.respond(HttpStatusCode.OK)
        }
    }
}

fun Application.walletRoutes(connection: Connection) {
    val walletService = WalletService(connection)
    routing {

        post("/wallets") {
            val wallet = call.receive<Wallet>()
            val id = walletService.create(wallet)
            call.respond(HttpStatusCode.Created, id)
        }
        get("/wallets/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            try {
                val wallet = walletService.read(id)
                call.respond(HttpStatusCode.OK, wallet)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.NotFound)
            }
        }
        put("/wallets/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            val wallet = call.receive<Wallet>()
            walletService.update(id, wallet)
            call.respond(HttpStatusCode.OK)
        }
        delete("/incomes/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            walletService.delete(id)
            call.respond(HttpStatusCode.OK)
        }
    }
}

fun Application.connectToPostgres(): Connection {
    Class.forName("org.postgresql.Driver")

    val appConfig = HoconApplicationConfig(ConfigFactory.load())
    val url = appConfig.property("postgres.url").getString()
    val user = appConfig.property("postgres.user").getString()
    val password = appConfig.property("postgres.password").getString()

    return DriverManager.getConnection(url, user, password)

}
