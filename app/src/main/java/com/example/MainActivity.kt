package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.CreatorViewModel
import com.example.ui.MainScreen
import com.example.ui.theme.MyApplicationTheme
import com.google.android.gms.ads.MobileAds

class MainActivity : ComponentActivity() {

    private val creatorViewModel: CreatorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Safely initialize Mobile Ads SDK
        try {
            MobileAds.initialize(this) {}
        } catch (_: Exception) {
        }

        setContent {
            MyApplicationTheme {
                MainScreen(viewModel = creatorViewModel)
            }
        }
    }
}
