package com.example.launcher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.launcher.ui.theme.LauncherTheme
import com.example.launcher.view.HomeScreen
import dagger.hilt.android.AndroidEntryPoint

// TODO: add background customization
// TODO: add app home/drawer
// TODO: add popular apps
// TODO: add widgets
// TODO: add folders
// TODO: add screen-time watcher
// TODO: add screen-time features
// TODO: add custom settings system

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