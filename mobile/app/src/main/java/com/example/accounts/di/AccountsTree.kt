package com.example.accounts.di

import com.example.accounts.domain.repository.AccountRepository
import com.example.accounts.presentation.action.AccountsAction
import com.example.accounts.domain.repository.AccountRepositoryImpl
import com.example.accounts.presentation.reducer.AccountsReducer
import com.example.accounts.presentation.state.AccountsState
import com.toggl.komposable.architecture.Store
import com.toggl.komposable.extensions.createStore
import com.toggl.komposable.scope.DispatcherProvider
import kotlinx.coroutines.CoroutineScope
import org.koin.core.module.Module
import org.koin.core.qualifier.named

internal fun Module.accountsDependencies() {
    // Repositories
    single<AccountRepository> { AccountRepositoryImpl(get()) }
    // Store
    single<Store<AccountsState, AccountsAction>>(named("accounts")) { createStore(
        initialState = AccountsState(),
        reducer = AccountsReducer(),
        storeScopeProvider = { get<CoroutineScope>() },
        dispatcherProvider = get<DispatcherProvider>(),
    ) }
}