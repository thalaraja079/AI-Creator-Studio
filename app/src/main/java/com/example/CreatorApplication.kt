package com.example

import android.app.Application
import com.example.data.CreatorPreferences
import com.example.data.auth.FirebaseAuthClient
import com.example.data.repository.AppConfigRepository
import com.example.data.repository.UserRepository
import com.example.ui.ads.AppOpenAdManager

class CreatorApplication : Application() {

    lateinit var preferences: CreatorPreferences
    lateinit var authClient: FirebaseAuthClient
    lateinit var adConfigRepository: AppConfigRepository
    lateinit var userRepository: UserRepository
    lateinit var appOpenAdManager: AppOpenAdManager

    override fun onCreate() {
        super.onCreate()
        preferences = CreatorPreferences(this)
        authClient = FirebaseAuthClient(this, preferences)
        adConfigRepository = AppConfigRepository(this)
        userRepository = UserRepository("AIzaSyAduASPbIPCD5gJAv2-vgjyOVJ94HfBjt8")
        appOpenAdManager = AppOpenAdManager(this, adConfigRepository)
        
        // Fetch remote config in background
        adConfigRepository.fetchConfig()
    }
}
