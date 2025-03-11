package com.example.launcher.view


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.launcher.R
import com.example.launcher.viewmodel.SettingsRepository
import com.example.launcher.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel
) {
    // Observe settings state
    val isDarkMode by viewModel.darkMode.collectAsState()
    val isGreyScale by viewModel.greyScaledApps.collectAsState()
    val dockSize by viewModel.maxDockSize.collectAsState(initial = SettingsRepository.DEFAULT_DOCK_SIZE)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = Color.LightGray.copy(alpha = 0.8F)
            ) // BG color and transparency value.
    )
    {
        Column(
            modifier = Modifier
                .padding(48.dp)
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Dark Mode Toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Toggle Dark-Mode", modifier = Modifier.weight(1f))
                Button(
                    onClick = { viewModel.toggleDarkMode() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.DarkGray,  // Black background
                        contentColor = Color.White     // White text and icon
                    ),
                    modifier = Modifier
                        .padding(8.dp)
                        .alpha(if (isDarkMode) 1f else 0.5f) // dim the button if dark mode is off
                ) {

                    Icon(
                        painter = painterResource(id = R.drawable.dark_mode_24),
                        contentDescription = "Dark-mode Button",
                        modifier = Modifier
                            .size(24.dp)
                            .padding(4.dp)
                    )
                    Text(
                        text = "Dark-mode"
                    )

                }
            }


            // Greyscale Apps Toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text(text = "Toggle Grey-Scaled App Icons", modifier = Modifier.weight(1f))

                Button(
                    onClick = { viewModel.toggleGreyScaleApps() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.DarkGray,  // Black background
                        contentColor = Color.White     // White text and icon
                    ),
                    modifier = Modifier
                        .padding(8.dp)
                        .alpha(if (isGreyScale) 1f else 0.5f) // dim the button if grey scale is off
                ) {

                    Icon(
                        painter = painterResource(id = R.drawable.tonality_24),
                        contentDescription = "Toggle Grey-scaled apps",
                        modifier = Modifier
                            .size(24.dp)
                            .padding(4.dp)
                    )
                    Text(
                        text = "Grey-Scale"
                    )

                }
            }

            // Dock Size Slider
            Text(text = "Dock Size: $dockSize", modifier = Modifier.padding(top = 16.dp))
            Slider(
                value = dockSize.toFloat(),
                onValueChange = { newValue ->
                    viewModel.setMaxDockSize(newValue.toInt())
                },
                valueRange = 1f..5f, // Size can be between 1 and 5 (UI Breaks past 5)
                steps = 5,
                modifier = Modifier.padding(top = 8.dp)
            )

            // Button to return to previous screen
            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .padding(top = 24.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                Text(text = "Go Back")
            }
        }
    }
}