package com.example.launcher.view

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.launcher.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    navController: NavController
) {

    val folders = viewModel.folders.collectAsState().value
    var showInputDialog by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }
    val appsInFolder by viewModel.appsInFolder.collectAsState(emptyList())

    // TODO: Allow multiple folders to be open together
    var expandedFolder by remember { mutableStateOf<String?>(null) } // Track which folder is expanded

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectHorizontalDragGestures { change, dragAmount -> // Gesture Based Navigation (Swipe right to go to app drawer)
                    change.consume()
                    if (dragAmount > 50F) {
                        navController.navigate("appDrawer")
                    }
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(onLongPress = {
                    showInputDialog = true // Trigger folder creation dialog
                })
            }
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            folders.forEach { folder ->
                Column {
                    // TODO: make this less ugly
                    // folder
                    Text(
                        text = folder,
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.LightGray.copy(alpha = 0.8F))
                            .padding(8.dp)
                            .clickable {
                                if (expandedFolder == folder) {
                                    expandedFolder = null // Collapse if already open
                                } else {
                                    expandedFolder = folder // Expand the clicked folder
                                    viewModel.displayAppsInFolder(folder)
                                }
                            }
                    )

                    // TODO: Display apps as app icons and horizontally
                    // display apps when folder is expanded
                    if (expandedFolder == folder) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White.copy(alpha = 0.9F))
                                .padding(8.dp)
                        ) {
                            appsInFolder.forEach { app ->
                                Text(
                                    text = app.label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .clickable { viewModel.launchApp(app.packageName) } // Launch app on click
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // TODO: make this less ugly
    if (showInputDialog) { // If user wants to create a new folder
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White.copy(alpha = 0.0F))
                .padding(16.dp),
        ) {
            BasicTextField( // Folder creation input
                value = newFolderName,
                onValueChange = { newFolderName = it },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    if (newFolderName.isNotBlank()) {
                        viewModel.addFolder(newFolderName)
                        newFolderName = ""
                        showInputDialog = false
                    }
                }),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.LightGray.copy(alpha = 0.6F))
                    .padding(8.dp)
            )
            Button(onClick = { // Folder creation button
                if (newFolderName.isNotBlank()) {
                    viewModel.addFolder(newFolderName)
                    newFolderName = ""
                    showInputDialog = false
                }
            }) {
                Text("Add Folder")
            }
        }
    }

    // TODO: add app dock
}
