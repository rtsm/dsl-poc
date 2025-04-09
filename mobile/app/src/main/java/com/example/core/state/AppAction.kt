package com.example.core.state


sealed class AppAction: Action {
    data object Logout: AppAction()
}