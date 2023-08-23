package ru.ermolnik.models

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.sql.Connection
import java.sql.Statement

/*
 *  Модель, описывающая расход
 */
@Serializable
data class Purchase(
    val id: String = "1",
    val category: Category,
    val value: Int,
    val valueInMainCurrency: Int,
    val note: String?,
    val date: Long,
    val accountId: String,
    val tags: List<BudgetTag>
)

/*
 *  Модель, описывающая категорию расхода
 */
@Serializable
data class Category(
    val id: String,
    val title: String,
    val emoji: String?,
    val isSystem: Boolean,
    val isVisible: Boolean,
    val limit: Int?,
    val order: Int
)

/*
 *  Модель, описывающая тэг
 */
@Serializable
data class BudgetTag(
    val id: String,
    val title: String,
    val categoryIds: List<String>
)


class PurchaseService(private val connection: Connection) {
    companion object {
        private const val CREATE_TABLE_PURCHASE =
            """CREATE TABLE IF NOT EXISTS PURCHASES (ID SERIAL PRIMARY KEY, 
                СATEGORY_ID VARCHAR(255),
                CATEGORY_TITLE VARCHAR(255),
                CATEGORY_EMOJI VARCHAR(255),
                CATEGORY_IS_SYSTEM BOOLEAN,
                CATEGORY_IS_VISIBLE BOOLEAN,
                CATEGORY_LIMIT INT,
                CATEGORY_ORDER INT,
                VALUE INT,
                VALUE_IN_MAIN_CURRENCY INT,
                NOTE VARCHAR(255),
                DATE BIGINT,
                ACCOUNT_ID VARCHAR(255)
                );
                """
        private const val SELECT_PURCHASE_BY_ID =
            "SELECT СATEGORY_ID, CATEGORY_TITLE, CATEGORY_EMOJI, CATEGORY_IS_SYSTEM, CATEGORY_IS_VISIBLE, CATEGORY_LIMIT, CATEGORY_ORDER, VALUE, VALUE_IN_MAIN_CURRENCY, NOTE, DATE, ACCOUNT_ID FROM PURCHASES WHERE id = ?"
        private const val INSERT_PURCHASE =
            "INSERT INTO PURCHASES (СATEGORY_ID, CATEGORY_TITLE, CATEGORY_EMOJI, CATEGORY_IS_SYSTEM, CATEGORY_IS_VISIBLE, CATEGORY_LIMIT, CATEGORY_ORDER, VALUE, VALUE_IN_MAIN_CURRENCY, NOTE, DATE, ACCOUNT_ID) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
        private const val UPDATE_PURCHASE =
            "UPDATE PURCHASES SET ATEGORY_ID = ?, CATEGORY_TITLE = ?, CATEGORY_EMOJI = ?, CATEGORY_IS_SYSTEM = ?, CATEGORY_IS_VISIBLE = ?, CATEGORY_LIMIT = ?, CATEGORY_ORDER = ?, VALUE = ?, VALUE_IN_MAIN_CURRENCY = ?, NOTE = ?, DATE = ?, ACCOUNT_ID = ? WHERE id = ?"
        private const val DELETE_PURCHASE = "DELETE FROM PURCHASES WHERE id = ?"

    }

    init {
        val statement = connection.createStatement()
        statement.executeUpdate(CREATE_TABLE_PURCHASE)
    }

    // Create new city
    suspend fun create(purchase: Purchase): Int = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(INSERT_PURCHASE, Statement.RETURN_GENERATED_KEYS)
        statement.setString(1, purchase.category.id)
        statement.setString(2, purchase.category.title)
        statement.setString(3, purchase.category.emoji)
        statement.setBoolean(4, purchase.category.isSystem)
        statement.setBoolean(5, purchase.category.isVisible)
        purchase.category.limit?.let { statement.setInt(6, it) }
        statement.setInt(7, purchase.category.order)
        statement.setInt(8, purchase.value)
        statement.setInt(9, purchase.valueInMainCurrency)
        statement.setString(10, purchase.note)
        statement.setLong(11, purchase.date)
        statement.setString(12, purchase.accountId)
        statement.executeUpdate()

        val generatedKeys = statement.generatedKeys
        if (generatedKeys.next()) {
            return@withContext generatedKeys.getInt(1)
        } else {
            throw Exception("Unable to retrieve the id of the newly inserted city")
        }
    }

    suspend fun read(id: Int): Purchase = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_PURCHASE_BY_ID)
        statement.setInt(1, id)
        val resultSet = statement.executeQuery()

        if (resultSet.next()) {
            val categoryId = resultSet.getString("СATEGORY_ID")
            val categoryTitle = resultSet.getString("CATEGORY_TITLE")
            val categoryEmoji = resultSet.getString("CATEGORY_EMOJI")
            val categoryIsSystem = resultSet.getBoolean("CATEGORY_IS_SYSTEM")
            val categoryIsVisible = resultSet.getBoolean("CATEGORY_IS_VISIBLE")
            val categoryLimit = resultSet.getInt("CATEGORY_LIMIT")
            val categoryOrder = resultSet.getInt("CATEGORY_ORDER")
            val value = resultSet.getInt("VALUE")
            val valueInMainCurrency = resultSet.getInt("VALUE_IN_MAIN_CURRENCY")
            val note = resultSet.getString("NOTE")
            val date = resultSet.getLong("DATE")
            val accountId = resultSet.getString("ACCOUNT_ID")

            return@withContext Purchase(
                id = categoryId,
                category = Category(
                    categoryId, categoryTitle, categoryEmoji, categoryIsSystem, categoryIsVisible,
                    categoryLimit, categoryOrder
                ),
                value = value,
                valueInMainCurrency = valueInMainCurrency,
                note = note,
                date = date,
                accountId = accountId,
                tags = listOf()
            )
        } else {
            throw Exception("Record not found")
        }
    }

    suspend fun update(id: Int, purchase: Purchase) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(UPDATE_PURCHASE)
        statement.setString(1, purchase.category.id)
        statement.setString(2, purchase.category.title)
        statement.setString(3, purchase.category.emoji)
        statement.setBoolean(4, purchase.category.isSystem)
        statement.setBoolean(5, purchase.category.isVisible)
        purchase.category.limit?.let { statement.setInt(6, it) }
        statement.setInt(7, purchase.category.order)
        statement.setInt(8, purchase.value)
        statement.setInt(9, purchase.valueInMainCurrency)
        statement.setString(10, purchase.note)
        statement.setLong(11, purchase.date)
        statement.setString(12, purchase.accountId)
        statement.setInt(13, id)
        statement.executeUpdate()
    }

    suspend fun delete(id: Int) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(DELETE_PURCHASE)
        statement.setInt(1, id)
        statement.executeUpdate()
    }
}