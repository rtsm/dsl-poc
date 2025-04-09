package com.example.bankingapp

import android.app.Application
import com.example.bankingapp.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.component.KoinComponent

class BankingApplication : Application(), KoinComponent {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@BankingApplication)
            modules(appModule)
        }
    }
} 