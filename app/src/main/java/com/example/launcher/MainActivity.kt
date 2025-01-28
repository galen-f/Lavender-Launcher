package com.example.launcher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.example.launcher.ui.theme.LauncherTheme
import com.example.launcher.view.AppDrawer
import com.example.launcher.view.HomeScreen
import dagger.hilt.android.AndroidEntryPoint
import com.example.launcher.viewmodel.HomeViewModel
import dagger.hilt.android.lifecycle.HiltViewModel

// Navigation Support Libraries
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.launcher.viewmodel.DrawerViewModel

// TODO: add widgets
// TODO: add folders
// TODO: add app dock
// TODO: add screen-time watcher
// TODO: add screen-time features
// TODO: add custom settings system
// TODO: Cache installed apps and their data using room?

@AndroidEntryPoint // HILT
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LauncherTheme {
                LauncherNavHost()
            }
        }
    }
}

@Composable
fun LauncherNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "HomeScreen") {
        composable("appDrawer") {
            // HILT Injection
            val viewModel: DrawerViewModel = hiltViewModel()
            AppDrawer(viewModel = viewModel, navController = navController)
        }
        composable("homeScreen") {
            // HILT Injection
            val viewModel: HomeViewModel = hiltViewModel()
            HomeScreen(viewModel = viewModel, navController = navController)
        }

    }
}