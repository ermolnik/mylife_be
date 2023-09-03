package ru.ermolnik.models.exposedb

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
data class Income(
    val id: Int,
    val category: IncomeCategory,
    val accountId: String,
    val value: Int,
    val valueInMainCurrency: Int,
    val note: String?,
    val date: Long
)

@Serializable
data class IncomeCategory(
    val id: String,
    val title: String,
    val emoji: String?,
    val isSystem: Boolean,
    val isVisible: Boolean,
    val order: Int
)

object IncomeDB : Table() {
    val id = integer("id").autoIncrement()
    val categoryId = varchar("categoryId", 1024)
    val categoryTitle = varchar("categoryTitle", 1024)
    val categoryEmoji = varchar("categoryEmoji", 1024)
    val categoryIsSystem = bool("categoryIsSystem")
    val categoryIsVisible = bool("categoryIsVisible")
    val categoryOrder = integer("categoryOrder")
    val accountId = varchar("accountId", 1024)
    val value = integer("value")
    val valueInMainCurrency = integer("valueInMainCurrency")
    val note = varchar("note", 1024)
    val date = long("date")

    override val primaryKey = PrimaryKey(id)
}

interface IncomeDAO {
    suspend fun allIncome(): List<Income>
    suspend fun income(id: Int): Income?
    suspend fun addNewIncome(income: Income): Income?
    suspend fun editIncome(id: Int, income: Income): Boolean
    suspend fun deleteIncome(id: Int): Boolean
}

class IncomeDAOImpl : IncomeDAO {

    private fun resultRowToIncome(row: ResultRow) = Income(
        id = row[IncomeDB.id],
        category = IncomeCategory(
            id = row[IncomeDB.categoryId],
            title = row[IncomeDB.categoryTitle],
            emoji = row[IncomeDB.categoryEmoji],
            isSystem = row[IncomeDB.categoryIsSystem],
            isVisible = row[IncomeDB.categoryIsVisible],
            order = row[IncomeDB.categoryOrder]
        ),
        accountId = row[IncomeDB.accountId],
        value = row[IncomeDB.value],
        valueInMainCurrency = row[IncomeDB.valueInMainCurrency],
        note = row[IncomeDB.note],
        date = row[IncomeDB.date]
    )

    override suspend fun allIncome(): List<Income> = dbQuery {
        IncomeDB.selectAll().map(::resultRowToIncome)
    }

    override suspend fun income(id: Int): Income? = dbQuery {
        IncomeDB
            .select { IncomeDB.id eq id }
            .map(::resultRowToIncome)
            .singleOrNull()
    }

    override suspend fun addNewIncome(income: Income): Income? = dbQuery {
        val insertStatement = IncomeDB.insert {
            it[categoryId] = income.category.id
            it[categoryTitle] = income.category.title
            it[categoryEmoji] = income.category.emoji ?: ""
            it[categoryIsSystem] = income.category.isSystem
            it[categoryIsVisible] = income.category.isVisible
            it[categoryOrder] = income.category.order
            it[accountId] = income.accountId
            it[value] = income.value
            it[valueInMainCurrency] = income.valueInMainCurrency
            it[note] = income.note ?: ""
            it[date] = income.date
        }
        insertStatement.resultedValues?.singleOrNull()?.let(::resultRowToIncome)
    }

    override suspend fun editIncome(id: Int, income: Income): Boolean = dbQuery {
        IncomeDB.update({ IncomeDB.id eq id }) {
            it[categoryId] = income.category.id
            it[categoryTitle] = income.category.title
            it[categoryEmoji] = income.category.emoji ?: ""
            it[categoryIsSystem] = income.category.isSystem
            it[categoryIsVisible] = income.category.isVisible
            it[categoryOrder] = income.category.order
            it[accountId] = income.accountId
            it[value] = income.value
            it[valueInMainCurrency] = income.valueInMainCurrency
            it[note] = income.note ?: ""
            it[date] = income.date
        } > 0
    }

    override suspend fun deleteIncome(id: Int): Boolean = dbQuery {
        IncomeDB.deleteWhere { IncomeDB.id eq id } > 0
    }
}

fun Application.incomeRoutes(incomeDAO: IncomeDAO) {
    routing {

        post("/incomes") {
            val income = call.receive<Income>()
            val incomeModel = incomeDAO.addNewIncome(income)
            try {
                call.respond(HttpStatusCode.Created, incomeModel!!)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.NotFound)
            }
        }
        get("/incomes/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            try {
                val income = incomeDAO.income(id)
                call.respond(HttpStatusCode.OK, income!!)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.NotFound)
            }
        }
        put("/incomes/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            val income = call.receive<Income>()
            incomeDAO.editIncome(id, income)
            call.respond(HttpStatusCode.OK)
        }
        delete("/incomes/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
            incomeDAO.deleteIncome(id)
            call.respond(HttpStatusCode.OK)
        }
    }
}
