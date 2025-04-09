package com.example.accounts.domain.model

data class Account(
    val id: String,
    val accountNumber: String,
    val accountType: AccountType,
    val balance: Double,
    val currency: String,
    val ownerName: String
)

enum class AccountType {
    SAVINGS,
    CHECKING,
    CREDIT
} 