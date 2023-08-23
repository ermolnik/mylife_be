package ru.ermolnik.models

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.sql.Connection
import java.sql.Statement

/*
 *  Модель, описывающая кошелёк
 */
@Serializable
data class Wallet(
    val id: String = "1",
    val relevanceTime: Long,
    val dateCreated: Long,
    val currency: Currency,
    val accounts: List<WalletAccount> = listOf()
)

/*
 *  Модель, описывающая валюту
 */
@Serializable
data class Currency(
    val id: String,
    val title: String,
    val symbol: String,
    val charCode: String
)

/*
 *  Модель, описывающая счет в кошельке
 */
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

/*
 *  Тип счета в кошельке
 */
@Serializable
enum class WalletAccountType {
    BUDGET,
    SAVINGS
}


class WalletService(private val connection: Connection) {
    companion object {
        private const val CREATE_TABLE_WALLET =
            "CREATE TABLE IF NOT EXISTS WALLETS (ID SERIAL PRIMARY KEY, WALLET_RELEVANCE_TIME BIGINT, WALLET_DATE_CREATED BIGINT, WALLET_CURRENCY_ID VARCHAR(255), WALLET_CURRENCY_TITLE VARCHAR(255), WALLET_CURRENCY_SYMBOL VARCHAR(255), WALLET_CURRENCY_CHAR_CODE VARCHAR(255));"
        private const val SELECT_WALLET_BY_ID =
            "SELECT WALLET_RELEVANCE_TIME, WALLET_DATE_CREATED, WALLET_CURRENCY_ID, WALLET_CURRENCY_TITLE, WALLET_CURRENCY_SYMBOL, WALLET_CURRENCY_CHAR_CODE FROM cities WHERE id = ?"
        private const val INSERT_WALLET =
            "INSERT INTO WALLETS (WALLET_RELEVANCE_TIME, WALLET_DATE_CREATED, WALLET_CURRENCY_ID, WALLET_CURRENCY_TITLE, WALLET_CURRENCY_SYMBOL, WALLET_CURRENCY_CHAR_CODE) VALUES (?, ?, ?, ?, ?, ?)"
        private const val UPDATE_WALLET =
            "UPDATE WALLETS SET WALLET_RELEVANCE_TIME = ?, WALLET_DATE_CREATED = ?, WALLET_CURRENCY_ID = ?, WALLET_CURRENCY_TITLE = ?, WALLET_CURRENCY_SYMBOL = ?, WALLET_CURRENCY_CHAR_CODE = ? WHERE id = ?"
        private const val DELETE_WALLET = "DELETE FROM WALLETS WHERE id = ?"

    }

    init {
        val statement = connection.createStatement()
        statement.executeUpdate(CREATE_TABLE_WALLET)
    }

    // Create new city
    suspend fun create(wallet: Wallet): Int = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(INSERT_WALLET, Statement.RETURN_GENERATED_KEYS)
        statement.setLong(1, wallet.relevanceTime)
        statement.setLong(2, wallet.dateCreated)
        statement.setString(3, wallet.currency.id)
        statement.setString(4, wallet.currency.title)
        statement.setString(5, wallet.currency.symbol)
        statement.setString(6, wallet.currency.charCode)
        statement.executeUpdate()

        val generatedKeys = statement.generatedKeys
        if (generatedKeys.next()) {
            return@withContext generatedKeys.getInt(1)
        } else {
            throw Exception("Unable to retrieve the id of the newly inserted city")
        }
    }

    // Read a city
    suspend fun read(id: Int): Wallet = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(SELECT_WALLET_BY_ID)
        statement.setInt(1, id)
        val resultSet = statement.executeQuery()
        if (resultSet.next()) {
            val walletRelevanceTime = resultSet.getLong("WALLET_RELEVANCE_TIME")
            val walletDateCreated = resultSet.getLong("WALLET_DATE_CREATED")
            val walletCurrencyId = resultSet.getString("WALLET_CURRENCY_ID")
            val walletCurrencyTitle = resultSet.getString("WALLET_CURRENCY_TITLE")
            val walletCurrencySymbol = resultSet.getString("WALLET_CURRENCY_SYMBOL")
            val walletCurrencyCharCode = resultSet.getString("WALLET_CURRENCY_CHAR_CODE")
            return@withContext Wallet(
                "1",
                walletRelevanceTime,
                walletDateCreated,
                Currency(walletCurrencyId, walletCurrencyTitle, walletCurrencySymbol, walletCurrencyCharCode)
            )
        } else {
            throw Exception("Record not found")
        }
    }

    // Update a city
    suspend fun update(id: Int, wallet: Wallet) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(UPDATE_WALLET)
        statement.setLong(1, wallet.relevanceTime)
        statement.setLong(2, wallet.dateCreated)
        statement.setString(3, wallet.currency.id)
        statement.setString(4, wallet.currency.title)
        statement.setString(5, wallet.currency.symbol)
        statement.setString(6, wallet.currency.charCode)
        statement.setInt(7, id)
        statement.executeUpdate()
    }

    // Delete a city
    suspend fun delete(id: Int) = withContext(Dispatchers.IO) {
        val statement = connection.prepareStatement(DELETE_WALLET)
        statement.setInt(1, id)
        statement.executeUpdate()
    }
}