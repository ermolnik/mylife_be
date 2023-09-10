package ru.ermolnik.models

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import ru.ermolnik.models.PurchaseDB.categoryLimit
import ru.ermolnik.plugins.dbQuery

@Serializable
data class Purchase(
    val id: Int,
    val category: PurchaseCategory,
    val value: Int,
    val valueInMainCurrency: Int,
    val note: String?,
    val date: Long,
    val accountId: String,
    val purchaseId: Int
)

@Serializable
data class PurchaseCategory(
    val id: String,
    val title: String,
    val emoji: String?,
    val isSystem: Boolean,
    val isVisible: Boolean,
    val limit: Int?,
    val order: Int
)

object PurchaseDB : Table() {
    val id = integer("id").autoIncrement()
    val categoryId = varchar("categoryId", 1024)
    val categoryTitle = varchar("categoryTitle", 1024)
    val categoryEmoji = varchar("categoryEmoji", 1024)
    val categoryIsSystem = bool("categoryIsSystem")
    val categoryIsVisible = bool("categoryIsVisible")
    val categoryLimit = integer("categoryLimit")
    val categoryOrder = integer("categoryOrder")
    val accountId = varchar("accountId", 1024)
    val value = integer("value")
    val valueInMainCurrency = integer("valueInMainCurrency")
    val note = varchar("note", 1024)
    val date = long("date")
    val purchaseId = integer("purchaseId")

    override val primaryKey = PrimaryKey(id)
}

interface PurchaseDAO {
    suspend fun allPurchase(): List<Purchase>
    suspend fun purchase(id: Int): Purchase?
    suspend fun addNewPurchase(income: Purchase): Purchase?
    suspend fun editPurchase(id: Int, income: Purchase): Boolean
    suspend fun deletePurchase(id: Int): Boolean
}

class PurchaseDAOImpl : PurchaseDAO {

    private fun resultRowToPurchase(row: ResultRow) = Purchase(
        id = row[PurchaseDB.id],
        category = PurchaseCategory(
            id = row[PurchaseDB.categoryId],
            title = row[PurchaseDB.categoryTitle],
            emoji = row[PurchaseDB.categoryEmoji],
            isSystem = row[PurchaseDB.categoryIsSystem],
            isVisible = row[PurchaseDB.categoryIsVisible],
            limit = row[categoryLimit],
            order = row[PurchaseDB.categoryOrder]
        ),
        accountId = row[PurchaseDB.accountId],
        value = row[PurchaseDB.value],
        valueInMainCurrency = row[PurchaseDB.valueInMainCurrency],
        note = row[PurchaseDB.note],
        date = row[PurchaseDB.date],
        purchaseId = row[PurchaseDB.purchaseId]
    )

    override suspend fun allPurchase(): List<Purchase> = dbQuery {
        IncomeDB.selectAll().map(::resultRowToPurchase)
    }

    override suspend fun purchase(id: Int): Purchase? = dbQuery {
        IncomeDB
            .select { IncomeDB.id eq id }
            .map(::resultRowToPurchase)
            .singleOrNull()
    }

    override suspend fun addNewPurchase(purchase: Purchase): Purchase? = dbQuery {
        val insertStatement = PurchaseDB.insert {
            it[categoryId] = purchase.category.id
            it[categoryTitle] = purchase.category.title
            it[categoryEmoji] = purchase.category.emoji ?: ""
            it[categoryIsSystem] = purchase.category.isSystem
            it[categoryIsVisible] = purchase.category.isVisible
            it[categoryLimit] = purchase.category.limit ?: 0
            it[categoryOrder] = purchase.category.order
            it[accountId] = purchase.accountId
            it[value] = purchase.value
            it[valueInMainCurrency] = purchase.valueInMainCurrency
            it[note] = purchase.note ?: ""
            it[date] = purchase.date
        }
        insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToPurchase)
    }

    override suspend fun editPurchase(id: Int, purchase: Purchase): Boolean = dbQuery {
        IncomeDB.update({ IncomeDB.id eq id }) {
            it[categoryId] = purchase.category.id
            it[categoryTitle] = purchase.category.title
            it[categoryEmoji] = purchase.category.emoji ?: ""
            it[categoryIsSystem] = purchase.category.isSystem
            it[categoryIsVisible] = purchase.category.isVisible
            it[categoryLimit] = purchase.category.limit ?: 0
            it[categoryOrder] = purchase.category.order
            it[accountId] = purchase.accountId
            it[value] = purchase.value
            it[valueInMainCurrency] = purchase.valueInMainCurrency
            it[note] = purchase.note ?: ""
            it[date] = purchase.date
        } > 0
    }

    override suspend fun deletePurchase(id: Int): Boolean = dbQuery {
        PurchaseDB.deleteWhere { PurchaseDB.id eq id } > 0
    }
}

fun Application.purchaseRoutes(purchaseDAO: PurchaseDAO) {
    routing {

        post("/purchases") {
            val purchase = call.receive<Purchase>()
            val purchaseModel = purchaseDAO.addNewPurchase(purchase)
            try {
                call.respond(HttpStatusCode.Created, purchaseModel!!)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.NotFound)
            }
        }
        get("/purchases/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            try {
                val income = purchaseDAO.purchase(id)
                call.respond(HttpStatusCode.OK, income!!)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.NotFound)
            }
        }
        put("/purchases/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            val purchase = call.receive<Purchase>()
            purchaseDAO.editPurchase(id, purchase)
            call.respond(HttpStatusCode.OK)
        }
        delete("/purchases/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            purchaseDAO.deletePurchase(id)
            call.respond(HttpStatusCode.OK)
        }
    }
}
