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
    walletRoutes(dbConnection)
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
