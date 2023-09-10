package ru.ermolnik.models

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import ru.ermolnik.plugins.dbQuery

@Serializable
data class Wallet(
    val id: Int,
    val relevanceTime: Long,
    val dateCreated: Long,
    val currency: Currency,
    val accounts: List<WalletAccount> = listOf()
)

@Serializable
data class Currency(
    val id: Int,
    val title: String,
    val symbol: String,
    val charCode: String
)

@Serializable
data class WalletAccount(
    val id: String,
    val title: String,
    val type: WalletAccountType,
    val limit: Int?,
    val currency: Currency,
    val incomeCategoryIds: List<String>,
    val spentCategoryIds: List<String>,
    val order: Int,
    val relevanceTime: Long,
    val dateCreated: Long
)

@Serializable
enum class WalletAccountType(name: String) {
    BUDGET("BUDGET"),
    SAVINGS("SAVINGS")
}

object WalletDB : Table() {

    val id = integer("id").autoIncrement()
    val relevanceTime = long("relevanceTime")
    val dateCreated = long("dateCreated")
    val currencyId = integer("currencyId")
    val currencyTitle = varchar("currencyTitle", 1024)
    val currencySymbol = varchar("categoryIsVisible", 1024)
    val currencyCharCode = varchar("currencyCharCode", 1024)

    override val primaryKey = PrimaryKey(id)
}

interface WalletDAO {
    suspend fun allWallet(): List<Wallet>
    suspend fun wallet(id: Int): Wallet?
    suspend fun addNewWallet(wallet: Wallet): Wallet?
    suspend fun editWallet(id: Int, wallet: Wallet): Boolean
    suspend fun deleteWallet(id: Int): Boolean
}

class WalletDAOImpl : WalletDAO {

    private fun resultRowToWallet(row: ResultRow) = Wallet(
        id = row[WalletDB.id],
        relevanceTime = row[WalletDB.relevanceTime],
        dateCreated = row[WalletDB.dateCreated],
        currency = Currency(
            id = row[WalletDB.currencyId],
            title = row[WalletDB.currencyTitle],
            symbol = row[WalletDB.currencySymbol],
            charCode = row[WalletDB.currencyCharCode],
        ),
    )

    override suspend fun allWallet(): List<Wallet> = dbQuery {
        IncomeDB.selectAll().map(::resultRowToWallet)
    }

    override suspend fun wallet(id: Int): Wallet? = dbQuery {
        IncomeDB
            .select { WalletDB.id eq id }
            .map(::resultRowToWallet)
            .singleOrNull()
    }

    override suspend fun addNewWallet(wallet: Wallet): Wallet? = dbQuery {
        val insertStatement = WalletDB.insert {
            it[relevanceTime] = wallet.relevanceTime
            it[dateCreated] = wallet.dateCreated
            it[currencyId] = wallet.currency.id
            it[currencySymbol] = wallet.currency.symbol
            it[currencyCharCode] = wallet.currency.charCode
        }
        insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToWallet)
    }

    override suspend fun editWallet(id: Int, wallet: Wallet): Boolean = dbQuery {
        WalletDB.update({ WalletDB.id eq id }) {
            it[relevanceTime] = wallet.relevanceTime
            it[dateCreated] = wallet.dateCreated
            it[currencyId] = wallet.currency.id
            it[currencySymbol] = wallet.currency.symbol
            it[currencyCharCode] = wallet.currency.charCode
        } > 0
    }

    override suspend fun deleteWallet(id: Int): Boolean = dbQuery {
        WalletDB.deleteWhere { PurchaseDB.id eq id } > 0
    }
}

fun Application.walletRoutes(walletDAO: WalletDAO) {
    routing {

        post("/wallets") {
            val wallet = call.receive<Wallet>()
            val walletModel = walletDAO.addNewWallet(wallet)
            try {
                call.respond(HttpStatusCode.Created, walletModel!!)
            } catch (e: Exception){
                call.respond(HttpStatusCode.NotFound)
            }
        }
        get("/wallets/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            try {
                val income = walletDAO.wallet(id)
                call.respond(HttpStatusCode.OK, income!!)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.NotFound)
            }
        }
        put("/wallets/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            val wallet = call.receive<Wallet>()
            walletDAO.editWallet(id, wallet)
            call.respond(HttpStatusCode.OK)
        }
        delete("/wallets/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            walletDAO.deleteWallet(id)
            call.respond(HttpStatusCode.OK)
        }
    }
}
