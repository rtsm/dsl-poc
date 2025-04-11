package com.example.profile.presentation.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.profile.domain.ProfileRepository
import com.example.profile.presentation.action.ProfileAction
import com.example.profile.presentation.state.ProfileState
import com.toggl.komposable.architecture.Store
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

class ProfileViewModel(
    private val store: Store<ProfileState, ProfileAction>,
    private val repository: ProfileRepository,
) : ViewModel(), KoinComponent {

    fun initialise() {
        store.send(ProfileAction.LoadProfile)
        viewModelScope.launch {
            try {
                repository.profile().collectLatest {
                    store.send(ProfileAction.ProfileLoaded(it))
                }
            } catch (ex: Exception) {
                notifyError(ex)
            }
        }
    }

    private fun notifyError(ex: Exception) {
        store.send(ProfileAction.ProfileLoadFailed(ex.message ?: "Unknown error"))
    }
}
