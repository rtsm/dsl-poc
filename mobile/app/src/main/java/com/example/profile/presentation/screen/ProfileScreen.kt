package com.example.profile.presentation.screen

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
import com.example.profile.domain.ProfileRepository
import com.example.profile.presentation.action.ProfileAction
import com.example.profile.presentation.state.ProfileState
import com.toggl.komposable.architecture.Store
import org.koin.compose.koinInject

@Composable
fun ProfileScreen(
    store: Store<ProfileState, ProfileAction>,
    navController: NavHostController
) {
    val state by store.state.collectAsState(initial = ProfileState())
    val repository = koinInject<ProfileRepository>()
    val viewModel = viewModel { ProfileViewModel(
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
            Text(text = "Hello from Profile")
            Text(text = "Current State:")
            Text(text = "isLoading: ${state.isLoading}")
            Text(text = "profile: ${state.profile ?: "null"}")
            Text(text = "accounts: ${state.accounts ?: "null"}")
            Text(text = "notificationsEnabled: ${state.notificationsEnabled}")
        }
    }
}
