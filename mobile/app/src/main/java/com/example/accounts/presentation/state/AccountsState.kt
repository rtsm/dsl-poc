package com.example.accounts.presentation.state

import com.example.accounts.domain.model.Account

data class AccountsState(
    val accounts: List<Account> = emptyList(),
    val selectedAccount: Account? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)