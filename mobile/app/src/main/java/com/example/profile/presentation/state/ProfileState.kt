package com.example.profile.presentation.state

import com.example.profile.domain.model.UserProfile

data class ProfileState(
    val isLoading: Boolean = false,
    val userProfile: UserProfile? = null,
    val error: String? = null
)
