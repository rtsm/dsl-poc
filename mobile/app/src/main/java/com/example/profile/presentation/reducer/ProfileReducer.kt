package com.example.profile.presentation.reducer

import com.example.profile.presentation.action.ProfileAction
import com.example.profile.presentation.state.ProfileState
import com.toggl.komposable.architecture.Effect
import com.toggl.komposable.architecture.Mutable
import com.toggl.komposable.architecture.Reducer
import com.toggl.komposable.extensions.mutateWithoutEffects

class ProfileReducer : Reducer<ProfileState, ProfileAction> {
    override fun reduce(state: Mutable<ProfileState>, action: ProfileAction): List<Effect<ProfileAction>> {
        return when (action) {

            is ProfileAction.LoadProfile -> state.mutateWithoutEffects {
                state.invoke().copy(
                    isLoading = true
                )
            }

            is ProfileAction.ProfileLoaded -> state.mutateWithoutEffects {
                state.invoke().copy(
                    isLoading = false,
                    profile = action.profile
                )
            }

            is ProfileAction.ProfileLoadFailed -> state.mutateWithoutEffects {
                state.invoke().copy(
                    isLoading = false, error = action.error
                )
            }
        }
    }
}
