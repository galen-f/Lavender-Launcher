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
    var newFolderName by remember { mutableStateOf("") }
    val appsInFolder by viewModel.appsInFolder.collectAsState(emptyList())
    val screenTime by viewModel.screenTime.collectAsState()

    val context = LocalContext.current
    val packageManager = context.packageManager
    val showPermissionDialog = viewModel.showPermissionDialog.collectAsState()

//    LaunchedEffect(Unit) {
//        if (!viewModel.screentimeManager.hasUsageAccess(context)) {
//            showPermissionDialog.value = true
//        } else {
//            viewModel.fetchScreenTime()
//        }
//    }


    // TODO: Allow multiple folders to be open together
    var expandedFolder by remember { mutableStateOf<String?>(null) } // Track which folder is expanded

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


                if (folders.isEmpty()) {
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
                    folders.forEach { folder ->

                        var showFolderMenu by remember { mutableStateOf(false) }


                        // Define the folder modifier dynamically
                        val folderModifier =
                            if (expandedFolder == folder) { // Defines a new modifier for when the folder is opened.
                                Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.DarkGray.copy(alpha = 0.6F))
                                    .padding(8.dp)
                            } else {
                                Modifier
                            }

                        Column( // Opened folder column
                            modifier = folderModifier,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text( // Folder Label
                                text = folder,
                                style = MaterialTheme.typography.displaySmall,
                                color = Color.White,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                                    .combinedClickable(
                                        onClick = {
                                            if (expandedFolder == folder) {
                                                expandedFolder = null // Collapse if already open
                                            } else {
                                                expandedFolder = folder // Expand the clicked folder
                                                viewModel.displayAppsInFolder(folder)
                                            }
                                        },
                                        onLongClick = { showFolderMenu = true }
                                    )
                            )
                            DropdownMenu(
                                expanded = showFolderMenu,
                                onDismissRequest = { showFolderMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Remove folder") },
                                    onClick = {
                                        viewModel.removeFolder(
                                            folder
                                        )
                                        showFolderMenu = false
                                    }
                                )
                            }

                            // display apps when folder is expanded
                            if (expandedFolder == folder) {
                                Column(
                                    // Column that designates the folder label to appear above the app items
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                ) {
                                    LazyVerticalGrid(
                                        // Grid designates the locations of the apps inside the folder
                                        columns = GridCells.Fixed(3),
                                    ) {
                                        items(appsInFolder) { app ->
                                            var showMenu by remember { mutableStateOf(false) } // For delete menu
                                            val icon: Drawable =
                                                packageManager.getApplicationIcon(app.packageName)

                                            Box( // Box containing single app item
                                                modifier = Modifier
                                                    .padding(20.dp)
                                                    .size(64.dp)
                                            ) {
                                                Column( // Column designating the icon to be drawn above the label
                                                    modifier = Modifier
                                                        .combinedClickable(
                                                            onClick = { viewModel.launchApp(app.packageName) },
                                                            onLongClick = { showMenu = true }
                                                        ), // Launch app on click
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                )
                                                {
                                                    Image( // App Icon
                                                        painter = DrawablePainter(icon), // This is the use of the accompanist library, which is shit, just an fyi, took my almost two days to get working
                                                        contentDescription = "${app.label} icon",
                                                        modifier = Modifier
                                                            .size(40.dp) // Icon Size
                                                            .padding(5.dp)
                                                            .align(Alignment.CenterHorizontally)
                                                    )
                                                    Text( // App Label
                                                        text = app.label,
                                                        style = MaterialTheme.typography.titleMedium,
                                                        textAlign = TextAlign.Center,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                        color = Color.White,
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                    )
                                                    DropdownMenu(
                                                        expanded = showMenu,
                                                        onDismissRequest = { showMenu = false }
                                                    ) {
                                                        DropdownMenuItem(
                                                            text = { Text("Remove from folder") },
                                                            onClick = {
                                                                viewModel.removeAppFromFolder(
                                                                    app.packageName,
                                                                    folder
                                                                )
                                                                showMenu = false
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .windowInsetsPadding(WindowInsets.navigationBars) // Keep the dock above the gesturebar
            ) {
                // Screentime Tracker
                val hours = screenTime / (1000 * 60 * 60)
                val minutes = (screenTime / (1000 * 60)) % 60

                Text(
                    text = "Screen time: $hours h $minutes min",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()

                )
                AppDock(viewModel)
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