package com.example.skycast

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.skycast.ui.SkyCastApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge so content goes under status & navigation bars
        enableEdgeToEdge()

        // Single AppContainer mapping from our custom SkyCastApplication
        val appContainer = (application as SkyCastApplication).container

        setContent {
            SkyCastApp(appContainer = appContainer)
        }
    }
}
