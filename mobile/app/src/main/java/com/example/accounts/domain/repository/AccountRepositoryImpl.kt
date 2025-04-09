package com.example.accounts.domain.repository

import com.example.accounts.domain.model.Account
import com.example.accounts.domain.model.AccountType
import com.example.core.NetworkClient
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.reflect.javaType
import kotlin.reflect.typeOf

class AccountRepositoryImpl(
    private val networkClient: NetworkClient,
    private val gson: Gson = Gson()
) : AccountRepository {
    private val mockAccounts = listOf(
        Account(
            id = "1",
            accountNumber = "1234567890",
            accountType = AccountType.CHECKING,
            balance = 5000.0,
            currency = "USD",
            ownerName = "John Doe"
        ),
        Account(
            id = "2",
            accountNumber = "0987654321",
            accountType = AccountType.SAVINGS,
            balance = 10000.0,
            currency = "USD",
            ownerName = "John Doe"
        ),
        Account(
            id = "3",
            accountNumber = "5555555555",
            accountType = AccountType.CREDIT,
            balance = -1500.0,
            currency = "USD",
            ownerName = "John Doe"
        )
    )

    override fun getAccounts(): Flow<List<Account>> = networkClient.request("/api/accounts").map { deserialize(it) }

    @OptIn(ExperimentalStdlibApi::class)
    private fun deserialize(responseBody: String): List<Account> {
        return gson.fromJson(responseBody, typeOf<List<Account>>().javaType)
    }

    override suspend fun getAccount(id: String): Account? {
        return mockAccounts.find { it.id == id }
    }

    override suspend fun updateAccount(account: Account) {
        // In a real app, this would update the account in a database
    }
} 