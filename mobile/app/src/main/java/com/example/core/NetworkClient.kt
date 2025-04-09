package com.example.core

import kotlinx.coroutines.flow.Flow

interface NetworkClient {
    fun request(request: String): Flow<String>
}