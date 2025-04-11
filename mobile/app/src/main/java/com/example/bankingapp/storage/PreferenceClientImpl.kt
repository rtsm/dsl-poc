package com.example.bankingapp.storage

import android.content.SharedPreferences
import com.example.core.PreferenceClient
import com.google.gson.Gson

class PreferenceClientImpl(
    private val sharedPreferences: SharedPreferences,
    private val gson: Gson = Gson()
) : PreferenceClient {
    
    override fun <T> getPreference(key: String, defaultValue: T): T {
        return when (defaultValue) {
            is String -> sharedPreferences.getString(key, defaultValue) as T
            is Int -> sharedPreferences.getInt(key, defaultValue) as T
            is Boolean -> sharedPreferences.getBoolean(key, defaultValue) as T
            is Long -> sharedPreferences.getLong(key, defaultValue) as T
            is Float -> sharedPreferences.getFloat(key, defaultValue) as T
            else -> {
                val json = sharedPreferences.getString(key, null)
                if (json != null) {
                    gson.fromJson(json, defaultValue!!::class.java)
                } else {
                    defaultValue
                }
            }
        }
    }

    override fun <T> setPreference(key: String, value: T) {
        when (value) {
            is String -> sharedPreferences.edit().putString(key, value).apply()
            is Int -> sharedPreferences.edit().putInt(key, value).apply()
            is Boolean -> sharedPreferences.edit().putBoolean(key, value).apply()
            is Long -> sharedPreferences.edit().putLong(key, value).apply()
            is Float -> sharedPreferences.edit().putFloat(key, value).apply()
            else -> {
                val json = gson.toJson(value)
                sharedPreferences.edit().putString(key, json).apply()
            }
        }
    }
} 