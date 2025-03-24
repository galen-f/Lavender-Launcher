package com.example.launcher.view

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.launcher.viewmodel.HomeViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    navController: NavController
) {
    val folders = viewModel.folders.collectAsState().value
    var showInputDialog by remember { mutableStateOf(false) }
    val pendingApp by viewModel.pendingLaunchApp.collectAsState()

    val context = LocalContext.current
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val showPermissionDialog = viewModel.showPermissionDialog.collectAsState()

    // Following is the logic to handle swipe based navigation
    val totalWidth = with(density) { configuration.screenWidthDp.dp.toPx() }    // Screen width
    val duration = 300                                                          // Animation speed in ms
    val animationSpec = tween<Float>(duration)                                  // Animation when returning
    val decaySpec = rememberSplineBasedDecay<Float>()                           // Physics based decay
    val anchors = DraggableAnchors {
        0 at 0f                                                                 // Default position anchor
        1 at -totalWidth                                                        // Right swipe anchor
        2 at totalWidth                                                         // Left swipe anchor
    }

    // Defines the anchors for the swipe based navigation
    val offsetX by remember { mutableStateOf(0f) }
    val draggableState = remember {
        AnchoredDraggableState(
            initialValue = 0,
            anchors = anchors,
            positionalThreshold = { totalWidth * 0.4f },                // 40% of screen to swipe
            velocityThreshold = { with(density) { 125.dp.toPx() } },    //Fling speed
            snapAnimationSpec = animationSpec,                          // Animation when releasing mid-swipe
            decayAnimationSpec = decaySpec,                             // Fling animation
            confirmValueChange = { true }                               // Always allow swiping
        )
    }

    // Animate the offset for smooth sliding effect
    val animatedOffsetX by animateFloatAsState(
        targetValue = draggableState.offset,
        animationSpec = animationSpec,
        label = "Animated Drawer Offset"
    )

    LaunchedEffect(offsetX) {
        snapshotFlow { draggableState.targetValue }
            .collect { target ->
                if (target == 1 || target == 2) { // If swiped left or right
                    navController.navigate("appDrawer") // Go to appDrawer
                }
            }
    }

    // If a launch is pending, show the confirmation dialog
    if (pendingApp != null) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelLaunch() },
            title = { Text("Confirm App Launch") },
            text = { Text("Are you sure you want to open this app?") },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmLaunch() }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelLaunch() }) {
                    Text("No")
                }
            }
        )
    }

    Box( // Screen box
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { translationX = animatedOffsetX }
            .anchoredDraggable(
                state = draggableState,
                orientation = Orientation.Horizontal,
            )
            .pointerInput(Unit) { // Press anywhere in the background to make a new folder
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
                    color = Color.LightGray.copy(alpha = 0.7F),
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

    // If long press detection is heard, show the add folder modal
    if (showInputDialog) {
        FolderAddDialog(viewModel, onDismiss = { showInputDialog = false })
    }

    if (showPermissionDialog.value) { // Permissions dialog, the screen-time tracker cant work without these permissions
        AlertDialog(
            onDismissRequest = { viewModel.setPermissionDialogState(false) },
            title = { Text("Usage Stats Permission Required") },
            text = { Text("The launcher app needs permission to access screen time in order to show your daily screen-time estimate. Please enable it in settings.") },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable // Dialog Box that appears when the user wishes to add a new folder
fun FolderAddDialog(viewModel: HomeViewModel, onDismiss: () -> Unit) {
    var newFolderName by remember { mutableStateOf("") }

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
        ) {
            Column (
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Create new folder"
                )
                OutlinedTextField( // Field to make a new folder
                    value = newFolderName,
                    onValueChange = { newFolderName = it },
                    label = { Text("Folder Name")}
                )
                Row( // buttons
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            if (newFolderName.isNotBlank()) {
                                viewModel.addFolder(newFolderName)
                                newFolderName = ""
                                onDismiss()
                            }
                        }
                    ) {
                        Text("Add")
                    }
                }
            }
        }
    }
}

@Composable
fun ScreenTimeTracker(viewModel: HomeViewModel) { // This is really bad but gets the message across best I can, check screen time manager for more info
    val screenTime by viewModel.screenTime.collectAsState()

    val hours = screenTime / (1000 * 60 * 60) // Two variables to help distinguish between hours and minutes, could be improved to only display hours when needed
    val minutes = (screenTime / (1000 * 60)) % 60

    Text(
        text = "Screen time: $hours h $minutes min",
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
        color = MaterialTheme.colorScheme.onBackground,

        textAlign = TextAlign.Center,
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    )
}