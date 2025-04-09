package com.example.core.state

data class AppState(
    val hasNetwork: Boolean = true,
    val loggedIn: Boolean = false,
    val globalError: String? = null
) 