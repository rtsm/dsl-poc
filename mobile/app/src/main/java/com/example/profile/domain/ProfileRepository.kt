package com.example.profile.domain

import com.example.profile.domain.model.*
import com.example.core.NetworkClient
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface ProfileRepository {
    fun getUserProfile(): Flow<UserProfile>
}

class ProfileRepositoryImpl(
    private val networkClient: NetworkClient,
    private val gson: Gson = Gson()
) : ProfileRepository {
    override fun getUserProfile(): Flow<UserProfile> = networkClient.request("/api/user/profile").map { deserialize(it) }

    private fun deserialize(responseBody: String): UserProfile {
        return gson.fromJson(responseBody, UserProfile::class.java)
    }

}
