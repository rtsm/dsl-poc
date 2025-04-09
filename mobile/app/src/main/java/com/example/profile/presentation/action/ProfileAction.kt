package com.example.profile.presentation.action

import com.example.profile.domain.model.UserProfile

sealed class ProfileAction {

    data object LoadProfile : ProfileAction()


    data class ProfileLoaded(
        val profile: UserProfile
    ) : ProfileAction()


    data class ProfileLoadFailed(
        val error: String
    ) : ProfileAction()
}
