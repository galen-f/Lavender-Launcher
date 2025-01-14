package com.example.launcher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.launcher.ui.theme.LauncherTheme
import com.example.launcher.view.HomeScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint // HILT
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LauncherTheme {
                HomeScreen()
            }
        }
    }
}