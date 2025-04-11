package com.example.core

interface PreferenceClient {
    fun <T> getPreference(key: String, defaultValue: T): T
    fun <T> setPreference(key: String, value: T)
} 