package ru.ermolnik.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table

@Serializable
data class BudgetTag(
    val id: String,
    val title: String,
    val categoryIds: List<String>
)

object BudgetTagDB : Table() {
    val id = integer("id").autoIncrement()
    val purchaseId = integer("purchaseId")
        .uniqueIndex()
        .references(PurchaseDB.purchaseId)
    val title = varchar("title", 1024)
    val date = long("date")

    override val primaryKey = PrimaryKey(id)
}

interface BudgetTagDAO {
    suspend fun allBudgetTags(): List<BudgetTag>
    suspend fun budgetTag(id: Int): BudgetTag?
    suspend fun addNewBudgetTag(budgetTag: BudgetTag): BudgetTag?
    suspend fun editBudgetTag(id: Int, budgetTag: BudgetTag): Boolean
    suspend fun deleteBudgetTag(id: Int): Boolean
}

class BudgetTagDAOImpl : BudgetTagDAO {
    override suspend fun allBudgetTags(): List<BudgetTag> {
        TODO("Not yet implemented")
    }

    override suspend fun budgetTag(id: Int): BudgetTag? {
        TODO("Not yet implemented")
    }

    override suspend fun addNewBudgetTag(budgetTag: BudgetTag): BudgetTag? {
        TODO("Not yet implemented")
    }

    override suspend fun editBudgetTag(id: Int, budgetTag: BudgetTag): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun deleteBudgetTag(id: Int): Boolean {
        TODO("Not yet implemented")
    }

}
