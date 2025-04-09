package com.example.profile.di

import com.example.profile.domain.ProfileRepository
import com.example.profile.domain.ProfileRepositoryImpl
import com.example.profile.presentation.reducer.ProfileReducer
import com.example.profile.presentation.state.ProfileState
import com.example.core.NetworkClient
import com.toggl.komposable.extensions.createStore
import com.toggl.komposable.scope.DispatcherProvider
import com.toggl.komposable.scope.StoreScopeProvider
import org.koin.core.module.Module
import org.koin.core.qualifier.named

fun Module.profileDependencies() {
    single<ProfileRepository> { ProfileRepositoryImpl(get<NetworkClient>()) }
    single { ProfileReducer() }
    single(named("profile")) { createStore(
            initialState = ProfileState(),
            reducer = ProfileReducer(),
            storeScopeProvider = get<StoreScopeProvider>(),
            dispatcherProvider = get<DispatcherProvider>(),
        ) }
}
