package ru.ermolnik.models

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.sql.Connection
import java.sql.Statement

/*
 *  Модель, описывающая доход
 */
@Serializable
data class Income(
    val id: String = "1",
    val category: IncomeCategory,
    val accountId: String,
    val value: Int,
    val valueInMainCurrency: Int,
    val note: String?,
    val date: Long
)

/*
 *  Модель, описывающая категорию расхода
 */
@Serializable
data class IncomeCategory(
    val id: String,
    val title: String,
    val emoji: String?,
    val isSystem: Boolean,
    val isVisible: Boolean,
    val order: Int
)


class IncomeService(private val connection: Connection) {
    companion object {
        private const val CREATE_TABLE_INCOMES =
            """CREATE TABLE IF NOT EXISTS INCOMES 
                (ID SERIAL PRIMARY KEY,
                INCOME_CATEGORY_ID VARCHAR(255),
                INCOME_CATEGORY_TITLE VARCHAR(255),
                INCOME_CATEGORY_EMOJI VARCHAR(255),
                INCOME_CATEGORY_IS_SYSTEM BOOLEAN,
                INCOME_CATEGORY_IS_VISIBLE BOOLEAN,
                INCOME_CATEGORY_ORGER INT,
                ACCOUNT_ID VARCHAR(255),
                VALUE INT,
                VALUE_IN_MAIN_CURRENCY INT,
                NOTE VARCHAR(255),
                DATE BIGINT);
                """
        private const val SELECT_INCOME_BY_ID =
            "SELECT INCOME_CATEGORY_ID, INCOME_CATEGORY_TITLE, INCOME_CATEGORY_EMOJI, INCOME_CATEGORY_IS_SYSTEM, INCOME_CATEGORY_IS_VISIBLE, INCOME_CATEGORY_ORGER, ACCOUNT_ID, VALUE, VALUE_IN_MAIN_CURRENCY, NOTE, DATE FROM incomes WHERE id = ?"
        private const val INSERT_INCOME =
            "INSERT INTO incomes (INCOME_CATEGORY_ID, INCOME_CATEGORY_TITLE, INCOME_CATEGORY_EMOJI, INCOME_CATEGORY_IS_SYSTEM, INCOME_CATEGORY_IS_VISIBLE, INCOME_CATEGORY_ORGER, ACCOUNT_ID, VALUE, VALUE_IN_MAIN_CURRENCY, NOTE, DATE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
        private const val UPDATE_INCOME =
            "UPDATE incomes SET INCOME_CATEGORY_ID = ?, INCOME_CATEGORY_TITLE = ?, INCOME_CATEGORY_EMOJI = ?, INCOME_CATEGORY_IS_SYSTEM = ?, INCOME_CATEGORY_IS_VISIBLE = ?, INCOME_CATEGORY_ORGER = ?, ACCOUNT_ID = ?, VALUE = ?, VALUE_IN_MAIN_CURRENCY = ?, NOTE = ?, DATE = ? WHERE id = ?"
        private const val DELETE_INCOME = "DELETE FROM incomes WHERE id = ?"

    }

    init {
        val statement = connection.createStatement()
        statement.executeUpdate(CREATE_TABLE_INCOMES)
    }

    // Create new income
    suspend fun create(income: Income): Int = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(INSERT_INCOME, Statement.RETURN_GENERATED_KEYS)
        statement.setString(1, income.category.id)
        statement.setString(2, income.category.title)
        statement.setString(3, income.category.emoji)
        statement.setBoolean(4, income.category.isSystem)
        statement.setBoolean(5, income.category.isVisible)
        statement.setInt(6, income.category.order)
        statement.setString(7, income.accountId)
        statement.setInt(8, income.value)
        statement.setInt(9, income.valueInMainCurrency)
        statement.setString(10, income.note)
        statement.setLong(11, income.date)
        statement.executeUpdate()

        val generatedKeys = statement.generatedKeys
        if (generatedKeys.next()) {
            return@withContext generatedKeys.getInt(1)
        } else {
            throw Exception("Unable to retrieve the id of the newly inserted city")
        }
    }

    // Read an income
    suspend fun read(id: Int): Income = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_INCOME_BY_ID)
        statement.setInt(1, id)
        val resultSet = statement.executeQuery()

        if (resultSet.next()) {
//            val id: String = resultSet.getString("id")
            val category: IncomeCategory
            val categoryId = resultSet.getString("INCOME_CATEGORY_ID")
            val categoryTitle = resultSet.getString("INCOME_CATEGORY_TITLE")
            val categoryEmoji = resultSet.getString("INCOME_CATEGORY_EMOJI")
            val categoryIsSystem = resultSet.getBoolean("INCOME_CATEGORY_IS_SYSTEM")
            val categoryIsVisible = resultSet.getBoolean("INCOME_CATEGORY_IS_VISIBLE")
            val categoryOrder = resultSet.getInt("INCOME_CATEGORY_ORGER")
            category = IncomeCategory(
                categoryId,
                categoryTitle,
                categoryEmoji,
                categoryIsSystem,
                categoryIsVisible,
                categoryOrder
            )
            val accountId: String = resultSet.getString("ACCOUNT_ID")
            val value: Int = resultSet.getInt("VALUE")
            val valueInMainCurrency: Int = resultSet.getInt("VALUE_IN_MAIN_CURRENCY")
            val note: String? = resultSet.getString("NOTE")
            val date: Long = resultSet.getLong("DATE")
            return@withContext Income("1", category, accountId, value, valueInMainCurrency, note, date)
        } else {
            throw Exception("Record not found")
        }
    }

    // Update a city
    suspend fun update(id: Int, income: Income) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(UPDATE_INCOME)
        statement.setString(1, income.category.id)
        statement.setString(2, income.category.title)
        statement.setString(3, income.category.emoji)
        statement.setBoolean(4, income.category.isSystem)
        statement.setBoolean(5, income.category.isVisible)
        statement.setInt(6, income.category.order)
        statement.setString(7, income.accountId)
        statement.setInt(8, income.value)
        statement.setInt(9, income.valueInMainCurrency)
        statement.setString(10, income.note)
        statement.setLong(11, income.date)
        statement.setInt(12, id)
        statement.executeUpdate()
    }

    // Delete a city
    suspend fun delete(id: Int) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(DELETE_INCOME)
        statement.setInt(1, id)
        statement.executeUpdate()
    }
}