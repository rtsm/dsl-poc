package com.example.accounts

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.example.accounts.di.accountsDependencies
import com.example.accounts.navigation.accountsNavigation
import com.example.accounts.presentation.action.AccountsAction
import com.example.accounts.presentation.state.AccountsState
import com.example.core.Journey
import com.toggl.komposable.architecture.Store
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.module.Module
import org.koin.core.qualifier.named

object AccountsJourney: Journey(), KoinComponent {
    private val store: Store<AccountsState, AccountsAction> by inject(named("accounts"))

    override fun Module.journeyDependencies() {
        this.accountsDependencies()
    }

    override fun NavGraphBuilder.journeyNavigation(navController: NavHostController) {
        this.accountsNavigation(store, navController)
    }
}