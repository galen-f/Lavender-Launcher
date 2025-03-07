package com.example.launcher

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
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

// TODO: Fix lag on dock screen
// TODO: Greyscale App Icons
// TODO: Broadcast receiver for (un)installed apps
// TODO: Quick Focus Mode Button
// TODO: Slide down from top to see top stuff (IDK wtf its called you know what I mean though)

@AndroidEntryPoint // HILT
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            val homeViewModel: HomeViewModel = hiltViewModel()
            val drawerViewModel: DrawerViewModel = hiltViewModel()

            LauncherTheme {
                LauncherNavHost(navController, homeViewModel, drawerViewModel)
            }
        }
    }
}

@Composable
fun LauncherNavHost(
    navController: NavHostController,
    homeViewModel: HomeViewModel,
    drawerViewModel: DrawerViewModel
) {

    NavHost(navController = navController, startDestination = "HomeScreen") {
        composable("appDrawer", arguments = emptyList()) {
            // HILT Injection
            AppDrawer(viewModel = drawerViewModel, viewModel2 = homeViewModel, navController = navController)
        }
        composable("homeScreen", arguments = emptyList()) {
            // HILT Injection
            HomeScreen(viewModel = homeViewModel, navController = navController)
        }

    }
}