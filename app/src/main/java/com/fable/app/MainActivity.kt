package com.fable.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.fable.app.di.FableDependencies
import com.fable.app.presentation.FableApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Draw behind the system bars; each screen applies the insets it needs.
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val dependencies = (application as? FableApplication)?.dependencies
            ?: FableDependencies(applicationContext)
        setContent {
            FableApp(dependencies)
        }
    }
}
