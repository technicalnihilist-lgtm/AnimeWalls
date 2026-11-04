package com.animewalls.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.animewalls.app.ui.navigation.AnimeWallsNav
import com.animewalls.app.ui.theme.AmoledBlack
import com.animewalls.app.ui.theme.AnimeWallsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            AnimeWallsTheme {
                androidx.compose.foundation.layout.Box(
                    Modifier.fillMaxSize().background(AmoledBlack)
                ) {
                    AnimeWallsNav()
                }
            }
        }
    }
}
