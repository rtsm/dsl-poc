package com.example.profile.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.profile.presentation.screen.ProfileScreen
import com.example.profile.presentation.action.ProfileAction
import com.example.profile.presentation.state.ProfileState
import com.toggl.komposable.architecture.Store

fun NavGraphBuilder.profileNavigation(
    store: Store<ProfileState, ProfileAction>,
    navController: NavHostController
) {
    composable("profile") {
        ProfileScreen(store, navController)
    }
}
