package com.example.bankingapp.di

import android.content.Context
import com.example.bankingapp.JourneyRegistry
import com.example.bankingapp.networking.MockClientImpl
import com.example.bankingapp.state.AppReducer
import com.example.core.NetworkClient
import com.example.core.PreferenceClient
import com.example.core.state.Action
import com.example.core.state.AppState
import com.example.bankingapp.storage.PreferenceClientImpl
import com.toggl.komposable.architecture.Store
import com.toggl.komposable.extensions.createStore
import com.toggl.komposable.scope.DispatcherProvider
import com.toggl.komposable.scope.StoreScopeProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import kotlin.coroutines.CoroutineContext

val appModule = module {

    single<NetworkClient> { MockClientImpl() }
    single<PreferenceClient> {
        PreferenceClientImpl(
            androidContext().getSharedPreferences(
                "shared_prefs",
                Context.MODE_PRIVATE
            )
        )
    }
    single {
        DispatcherProvider(
            io = Dispatchers.IO,
            computation = Dispatchers.Default,
            main = Dispatchers.Main,
        )
    }
    single<CoroutineScope> {
        object : CoroutineScope {
            override val coroutineContext: CoroutineContext
                get() = get<DispatcherProvider>().main
        }
    }
    single { StoreScopeProvider { get<CoroutineScope>() } }

    single<Store<AppState, Action>> {
        createStore(
            initialState = AppState(),
            reducer = AppReducer(),
            storeScopeProvider = get<StoreScopeProvider>(),
            dispatcherProvider = get<DispatcherProvider>(),
        )
    }

    JourneyRegistry.getAllJourneys().forEach {
        it.registerDependencyTree(this@module)
    }
}
