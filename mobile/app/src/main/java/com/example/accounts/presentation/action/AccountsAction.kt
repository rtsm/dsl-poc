package com.example.accounts.presentation.action

import androidx.navigation.NavHostController
import com.example.accounts.domain.model.Account
import com.example.core.state.Action

sealed class AccountsAction: Action {
    data class LoadAccounts(val accounts: List<Account>) : AccountsAction()
    data class SelectAccount(val account: Account, val navController: NavHostController) : AccountsAction()
    data class ShowError(val message: String) : AccountsAction()
    data object Loading : AccountsAction()
} 