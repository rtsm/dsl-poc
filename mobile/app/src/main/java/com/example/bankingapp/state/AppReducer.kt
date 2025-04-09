package com.example.bankingapp.state

import com.example.core.state.Action
import com.example.core.state.AppAction
import com.example.core.state.AppState
import com.toggl.komposable.architecture.Effect
import com.toggl.komposable.architecture.Mutable
import com.toggl.komposable.architecture.Reducer
import com.toggl.komposable.extensions.mutateWithoutEffects

class AppReducer : Reducer<AppState, Action> {
    override fun reduce(
        state: Mutable<AppState>,
        action: Action
    ): List<Effect<Action>> {
        return when (action) {
            is AppAction.Logout -> {
                state.mutateWithoutEffects {
                    state.invoke().copy(
                        loggedIn = false
                    ).also {
                        //action.navController.navigate("welcome")
                    }
                }
            }

            else -> emptyList()
        }
    }
} 