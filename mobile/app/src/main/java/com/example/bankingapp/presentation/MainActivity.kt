package com.example.bankingapp.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.bankingapp.presentation.ui.theme.BankingAppTheme
import com.example.bankingapp.JourneyRegistry
import org.koin.core.component.KoinComponent

class MainActivity : ComponentActivity(), KoinComponent {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BankingAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BankingApp()
                }
            }
        }
    }
}

@Composable
fun BankingApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "accounts") {
       JourneyRegistry.getAllJourneys().forEach {
           it.registerNavigation(this@NavHost, navController)
       }
    }
}