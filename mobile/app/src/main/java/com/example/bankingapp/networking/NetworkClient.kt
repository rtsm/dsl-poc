package com.example.bankingapp.networking

import com.example.core.NetworkClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class MockClientImpl: NetworkClient {
    override fun request(request: String): Flow<String> {
        return flow {
            delay(1000L)
            if (request.contains("accounts")) {
                emit(ACCOUNTS_MOCK)
            } else {
                emit(PROFILE_MOCK)
            }
        }
    }

}


const val ACCOUNTS_MOCK = "" +
        "[\n" +
        "  {\n" +
        "    \"id\": \"1\",\n" +
        "    \"accountNumber\": \"1234567890\",\n" +
        "    \"accountType\": \"CHECKING\",\n" +
        "    \"balance\": 5000.0,\n" +
        "    \"currency\": \"USD\",\n" +
        "    \"ownerName\": \"John Doe\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"id\": \"2\",\n" +
        "    \"accountNumber\": \"0987654321\",\n" +
        "    \"accountType\": \"SAVINGS\",\n" +
        "    \"balance\": 10000.0,\n" +
        "    \"currency\": \"USD\",\n" +
        "    \"ownerName\": \"John Doe\"\n" +
        "  },\n" +
        "  {\n" +
        "    \"id\": \"3\",\n" +
        "    \"accountNumber\": \"5555555555\",\n" +
        "    \"accountType\": \"CREDIT\",\n" +
        "    \"balance\": -1500.0,\n" +
        "    \"currency\": \"USD\",\n" +
        "    \"ownerName\": \"John Doe\"\n" +
        "  }\n" +
        "]"

const val PROFILE_MOCK = "{\n" +
        "  \"id\": \"user123\",\n" +
        "  \"name\": \"Jacek Lifo\",\n" +
        "  \"email\": \"j.lifo@test.com\",\n" +
        "  \"avatarUrl\": \"https://i1.sndcdn.com/artworks-x8zI2HVC2pnkK7F5-4xKLyA-t1080x1080.jpg\"\n" +
        "}"