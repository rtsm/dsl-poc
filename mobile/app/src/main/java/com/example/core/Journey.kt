package com.example.core

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import org.koin.core.module.Module

abstract class Journey {
    abstract fun Module.journeyDependencies()
    abstract fun NavGraphBuilder.journeyNavigation(navController: NavHostController)

    fun registerNavigation(graphBuilder: NavGraphBuilder, navController: NavHostController) {
        graphBuilder.journeyNavigation(navController)
    }

    fun registerDependencyTree(module: Module) {
        module.journeyDependencies()
    }
}