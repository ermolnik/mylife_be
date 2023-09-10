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
        .references(PurchaseDB.id)
    val title = varchar("title", 1024)
    val date = long("date")

    override val primaryKey = PrimaryKey(id)
}
