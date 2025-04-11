package com.example.dsl.template

object DomainModelTemplate {
    const val TEMPLATE = """package com.example.{package_name}.domain.model

data class {class_name}(
{properties}
)
"""
}

object RepositoryTemplate {
    const val TEMPLATE = """package com.example.{package_name}.domain

{model_imports}
import com.example.core.NetworkClient
import com.example.core.PreferenceClient
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File
import kotlin.reflect.typeOf
import kotlin.reflect.javaType

interface {repository_name} {
{repository_methods}
}

class {repository_impl_name}(
    private val networkClient: NetworkClient,
    private val preferenceClient: PreferenceClient,
    private val gson: Gson = Gson()
) : {repository_name}, KoinComponent {
{repository_impl_methods}
}
"""
}

object RepositoryImplTemplate {
    const val TEMPLATE = """    override fun {repository_method}(): Flow<{model_name}> {
        {implementation}
    }
"""
}

object UIStateTemplate {
    const val TEMPLATE = """package com.example.{package_name}.presentation.state

{domain_model_imports}

data class {state_name}(
{properties}
)
"""
}

object UIActionTemplate {
    const val TEMPLATE = """package com.example.{package_name}.presentation.action

{domain_model_imports}

sealed class {action_name} {
{action_classes}
}
"""
}

object JourneyTemplate {
    const val TEMPLATE = """package com.example.{package_name}

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.example.{package_name}.di.{package_name}Dependencies
import com.example.{package_name}.navigation.{package_name}Navigation
import com.example.{package_name}.presentation.action.{action_name}
import com.example.{package_name}.presentation.state.{state_name}
import com.example.core.Journey
import com.toggl.komposable.architecture.Store
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.module.Module
import org.koin.core.qualifier.named

object {journey_name}: Journey(), KoinComponent {
    private val store: Store<{state_name}, {action_name}> by inject(named("{package_name}"))

    override fun Module.journeyDependencies() {
        this.{package_name}Dependencies()
    }

    override fun NavGraphBuilder.journeyNavigation(navController: NavHostController) {
        this.{package_name}Navigation(store, navController)
    }
}
"""
}

object ReducerTemplate {
    const val TEMPLATE = """package com.example.{package_name}.presentation.reducer

import com.example.{package_name}.presentation.action.{action_name}
import com.example.{package_name}.presentation.state.{state_name}
import com.toggl.komposable.architecture.Effect
import com.toggl.komposable.architecture.Mutable
import com.toggl.komposable.architecture.Reducer
import com.toggl.komposable.extensions.mutateWithoutEffects

class {reducer_name} : Reducer<{state_name}, {action_name}> {
    override fun reduce(state: Mutable<{state_name}>, action: {action_name}): List<Effect<{action_name}>> {
        return when (action) {
{reducer_cases}
        }
    }
}
"""
}

object DITemplate {
    const val TEMPLATE = """package com.example.{package_name}.di

import com.example.{package_name}.domain.{repository_name}
import com.example.{package_name}.domain.{repository_impl_name}
import com.example.{package_name}.presentation.reducer.{reducer_name}
import com.example.{package_name}.presentation.state.{state_name}
import com.example.core.NetworkClient
import com.example.core.PreferenceClient
import com.toggl.komposable.extensions.createStore
import com.toggl.komposable.scope.DispatcherProvider
import com.toggl.komposable.scope.StoreScopeProvider
import org.koin.core.module.Module
import org.koin.core.qualifier.named

fun Module.{package_name}Dependencies() {
    single<{repository_name}> { {repository_impl_name}(get<NetworkClient>(), get<PreferenceClient>()) }
    single { {reducer_name}() }
    single(named("{package_name}")) { createStore(
            initialState = {state_name}(),
            reducer = {reducer_name}(),
            storeScopeProvider = get<StoreScopeProvider>(),
            dispatcherProvider = get<DispatcherProvider>(),
        ) }
}
"""
}

object NavigationTemplate {
    const val TEMPLATE = """package com.example.{package_name}.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.{package_name}.presentation.screen.{screen_name}
import com.example.{package_name}.presentation.action.{action_name}
import com.example.{package_name}.presentation.state.{state_name}
import com.toggl.komposable.architecture.Store

fun NavGraphBuilder.{package_name}Navigation(
    store: Store<{state_name}, {action_name}>,
    navController: NavHostController
) {
    composable("{route_name}") {
        {screen_name}(store, navController)
    }
}
"""
}

object ViewModelTemplate {
    const val TEMPLATE = """package com.example.{package_name}.presentation.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.{package_name}.domain.{repository_name}
import com.example.{package_name}.presentation.action.{action_name}
import com.example.{package_name}.presentation.state.{state_name}
import com.toggl.komposable.architecture.Store
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

class {viewmodel_name}(
    private val store: Store<{state_name}, {action_name}>,
    private val repository: {repository_name},
) : ViewModel(), KoinComponent {

    fun initialise() {
        store.send({action_name}.LoadProfile)
        viewModelScope.launch {
            try {
                repository.{repository_method}().collectLatest {
                    store.send({action_name}.{action_loaded}(it))
                }
            } catch (ex: Exception) {
                notifyError(ex)
            }
        }
    }

    private fun notifyError(ex: Exception) {
        store.send({action_name}.{action_failed}(ex.message ?: "Unknown error"))
    }
}
"""
}

object ScreenTemplate {
    const val TEMPLATE = """package com.example.{package_name}.presentation.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.{package_name}.domain.{repository_name}
import com.example.{package_name}.presentation.action.{action_name}
import com.example.{package_name}.presentation.state.{state_name}
import com.toggl.komposable.architecture.Store
import org.koin.compose.koinInject

@Composable
fun {screen_name}(
    store: Store<{state_name}, {action_name}>,
    navController: NavHostController
) {
    val state by store.state.collectAsState(initial = {state_name}())
    val repository = koinInject<{repository_name}>()
    val viewModel = viewModel { {viewmodel_name}(
        repository = repository,
        store = store
    ) }

    LaunchedEffect(Unit) {
        viewModel.initialise()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = "Hello from {feature_name}")
            Text(text = "Current State:")
            Text(text = "isLoading: ${'$'}{state.isLoading}")
            {state_properties}
        }
    }
}
"""
} 