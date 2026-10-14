package com.example

import android.app.Application
import com.example.data.repository.FirebaseAdConfigRepository
import com.example.ui.ads.AppOpenAdManager

class CreatorApplication : Application() {

    lateinit var adConfigRepository: FirebaseAdConfigRepository
    lateinit var appOpenAdManager: AppOpenAdManager

    override fun onCreate() {
        super.onCreate()
        adConfigRepository = FirebaseAdConfigRepository(this)
        appOpenAdManager = AppOpenAdManager(this, adConfigRepository)
        
        // Fetch remote config in background
        adConfigRepository.fetchConfig()
    }
}
