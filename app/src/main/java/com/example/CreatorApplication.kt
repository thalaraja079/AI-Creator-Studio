package com.example

import android.app.Application
import com.example.data.CreatorPreferences
import com.example.data.auth.FirebaseAuthClient
import com.example.data.repository.FirebaseAdConfigRepository
import com.example.ui.ads.AppOpenAdManager

class CreatorApplication : Application() {

    lateinit var preferences: CreatorPreferences
    lateinit var authClient: FirebaseAuthClient
    lateinit var adConfigRepository: FirebaseAdConfigRepository
    lateinit var appOpenAdManager: AppOpenAdManager

    override fun onCreate() {
        super.onCreate()
        preferences = CreatorPreferences(this)
        authClient = FirebaseAuthClient(this, preferences)
        adConfigRepository = FirebaseAdConfigRepository(this)
        appOpenAdManager = AppOpenAdManager(this, adConfigRepository)
        
        // Fetch remote config in background
        adConfigRepository.fetchConfig()
    }
}
