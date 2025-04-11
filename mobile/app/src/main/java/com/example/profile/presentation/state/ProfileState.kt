package com.example.profile.presentation.state

import com.example.profile.domain.model.UserProfile
import com.example.accounts.domain.model.Account

data class ProfileState(
    val isLoading: Boolean = false,
    val profile: UserProfile? = null,
    val accounts: List<Account>? = null,
    val notificationsEnabled: Boolean = false,
    val error: String? = null
)
