package com.example.profile.domain

import com.example.profile.domain.model.UserProfile
import com.example.accounts.domain.model.Account
import com.example.accounts.domain.repository.AccountRepository
import com.example.core.NetworkClient
import com.example.core.PreferenceClient
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File
import kotlin.reflect.typeOf
import kotlin.reflect.javaType

interface ProfileRepository {
    fun profile(): Flow<UserProfile>
    fun getAccounts(): Flow<List<Account>>
    fun getNotificationsEnabled(): Flow<Boolean>
}

class ProfileRepositoryImpl(
    private val networkClient: NetworkClient,
    private val preferenceClient: PreferenceClient,
    private val gson: Gson = Gson()
) : ProfileRepository, KoinComponent {
    override fun profile(): Flow<UserProfile> {
        return networkClient.request("/api/profile").map { deserializeUserProfile(it) }
    }


    override fun getAccounts(): Flow<List<Account>> {
        return (inject<AccountRepository>()).value.getAccounts()
    }


    override fun getNotificationsEnabled(): Flow<Boolean> {
        return flow { emit(preferenceClient.getPreference("notifications_enabled", false)) }
    }


    private fun deserializeUserProfile(responseBody: String): UserProfile {
        return gson.fromJson(responseBody, UserProfile::class.java)
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun deserializeAccountList(responseBody: String): List<Account> {
        return gson.fromJson(responseBody, typeOf<List<Account>>().javaType)
    }
}
