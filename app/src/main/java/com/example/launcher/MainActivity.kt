package com.example.launcher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.launcher.ui.theme.LauncherTheme
import com.example.launcher.view.AppDrawer
import com.example.launcher.view.HomeScreen
import com.example.launcher.view.SettingsScreen
import dagger.hilt.android.AndroidEntryPoint

// Navigation Support Libraries
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.launcher.viewmodel.DrawerViewModel
import com.example.launcher.viewmodel.SettingsViewModel
import com.example.launcher.viewmodel.HomeViewModel

@AndroidEntryPoint // HILT
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // ViewModels are initialised outside of the navcontroller because dagger and
            // navcontroller don't play nice, initializing them inside will cause massive recompositions
            val navController = rememberNavController()
            val homeViewModel: HomeViewModel = hiltViewModel()
            val drawerViewModel: DrawerViewModel = hiltViewModel()
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val isDarkMode by settingsViewModel.darkMode.collectAsState() // settings repo sends to settingsVM, exposes it for here, not sure if this is ideal?

            LauncherTheme(darkTheme = isDarkMode) {
                LauncherNavHost(navController, homeViewModel, drawerViewModel, settingsViewModel)
            }
        }
    }
}

@Composable
fun LauncherNavHost(
    navController: NavHostController,
    homeViewModel: HomeViewModel,
    drawerViewModel: DrawerViewModel,
    settingsViewModel: SettingsViewModel
) {

    NavHost(navController = navController, startDestination = "HomeScreen") {
        composable("appDrawer", arguments = emptyList()) {
            // HILT Injection
            AppDrawer(drawerViewModel = drawerViewModel, homeViewModel = homeViewModel, navController = navController)
        }
        composable("homeScreen", arguments = emptyList()) {
            // HILT Injection
            HomeScreen(viewModel = homeViewModel, navController = navController)
        }
        composable("settingsScreen", arguments = emptyList()) {
            // HILT Injection
            SettingsScreen(viewModel = settingsViewModel, navController = navController)
        }
    }
}