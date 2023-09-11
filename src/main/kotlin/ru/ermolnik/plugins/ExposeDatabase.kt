package ru.ermolnik.plugins

import io.ktor.server.application.*
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import ru.ermolnik.models.BudgetTagDB
import ru.ermolnik.models.IncomeDB
import ru.ermolnik.models.PurchaseDB
import ru.ermolnik.models.WalletDB

fun Application.configureExposeDatabase() {
    val driverClassName = "org.h2.Driver"
    val jdbcURL = "jdbc:h2:file:./build/db"
    val database = Database.connect(jdbcURL, driverClassName)
    transaction(database) {
        SchemaUtils.create(IncomeDB)
        SchemaUtils.create(PurchaseDB)
        SchemaUtils.create(WalletDB)
//        SchemaUtils.create(BudgetTagDB)
    }
}

suspend fun <T> dbQuery(block: suspend () -> T): T =
    newSuspendedTransaction(Dispatchers.IO) { block() }
