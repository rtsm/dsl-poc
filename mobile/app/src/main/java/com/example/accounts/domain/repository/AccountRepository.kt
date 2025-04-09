package com.example.accounts.domain.repository

import com.example.accounts.domain.model.Account
import kotlinx.coroutines.flow.Flow

interface AccountRepository {
    fun getAccounts(): Flow<List<Account>>
    suspend fun getAccount(id: String): Account?
    suspend fun updateAccount(account: Account)
} 