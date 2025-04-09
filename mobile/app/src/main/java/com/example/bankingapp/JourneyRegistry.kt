package com.example.bankingapp

import com.example.accounts.AccountsJourney
import com.example.core.Journey
import com.example.profile.ProfileJourney

object JourneyRegistry {
    private val registeredJourneys = setOf<Journey>(
        AccountsJourney, ProfileJourney
    )

    fun getAllJourneys(): Set<Journey> = registeredJourneys
}