package com.example.accounts.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.accounts.presentation.action.AccountsAction
import com.example.accounts.presentation.screens.AccountsScreen
import com.example.accounts.presentation.state.AccountsState
import com.toggl.komposable.architecture.Store

internal fun NavGraphBuilder.accountsNavigation(
    store: Store<AccountsState, AccountsAction>,
    navController: NavHostController
) {
    composable("accounts") {
        AccountsScreen(
            navController = navController,
            store = store,
        )
    }
}