package com.example.launcher.view

import androidx.compose.material3.AlertDialog
import android.content.Intent
import android.graphics.drawable.Drawable
import android.provider.Settings
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.launcher.viewmodel.HomeViewModel
import com.google.accompanist.drawablepainter.DrawablePainter

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    navController: NavController
) {
    val folders = viewModel.folders.collectAsState().value
    var showInputDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val showPermissionDialog = viewModel.showPermissionDialog.collectAsState()

    Box( // Screen box
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

        Column( // Screen Column (Folders + dock + Screen-time tracker)
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp) // Space between screen items (Folders and docks)
        ) {
            if (folders.isEmpty()) { // Show a placeholder text so users know how to add folders
                Text(
                    text = "Press and hold anywhere to add a new folder",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.7F),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                )
            } else {
                FolderList(viewModel) // Display the folder (and things inside them if they're opened)
            }
        }

        Column( // Column to display dock below folders
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.navigationBars) // Keep the dock above the gesturebar
        ) {
            ScreenTimeTracker(viewModel)
            AppDock(viewModel)
        }

    }

    // On long-press, show the add folder modal
    if (showInputDialog) { // If user wants to create a new folder
        FolderAddDialog(viewModel, onDismiss = { showInputDialog = false })
    }

    // TODO: This will prompt every time the user opens the app even if they deny it
    if (showPermissionDialog.value) {
        AlertDialog(
            onDismissRequest = { viewModel.setPermissionDialogState(false) },
            title = { Text("Permission Required") },
            text = { Text("This app needs permission to access screen time. Please enable it in settings.") },
            confirmButton = {
                Button(onClick = {
                    viewModel.setPermissionDialogState(false)
                    viewModel.screentimeManager.requestUsageAccess(context)
                }) {
                    Text("Go to Settings")
                }
            },
            dismissButton = {
                Button(onClick = { viewModel.setPermissionDialogState(false) }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable // Dialog Box that appears when the user wishes to add a new folder
fun FolderAddDialog(viewModel: HomeViewModel, onDismiss: () -> Unit) {
    var newFolderName by remember { mutableStateOf("") }

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
                    onDismiss()
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
                onDismiss()
            }
        }) {
            Text("Add Folder")
        }
    }
}

@Composable
fun ScreenTimeTracker(viewModel: HomeViewModel) {
    val screenTime by viewModel.screenTime.collectAsState()

    val hours = screenTime / (1000 * 60 * 60) // Two variables to help distinguish between hours and minutes, could be improved to only display hours when needed
    val minutes = (screenTime / (1000 * 60)) % 60

    Text(
        text = "Screen time: $hours h $minutes min",
        style = MaterialTheme.typography.titleLarge,
        color = Color.White,

        textAlign = TextAlign.Center,
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    )
}