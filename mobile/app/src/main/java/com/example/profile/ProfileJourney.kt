package com.example.profile

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import com.example.profile.di.profileDependencies
import com.example.profile.navigation.profileNavigation
import com.example.profile.presentation.action.ProfileAction
import com.example.profile.presentation.state.ProfileState
import com.example.core.Journey
import com.toggl.komposable.architecture.Store
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.module.Module
import org.koin.core.qualifier.named

object ProfileJourney: Journey(), KoinComponent {
    private val store: Store<ProfileState, ProfileAction> by inject(named("profile"))

    override fun Module.journeyDependencies() {
        this.profileDependencies()
    }

    override fun NavGraphBuilder.journeyNavigation(navController: NavHostController) {
        this.profileNavigation(store, navController)
    }
}
