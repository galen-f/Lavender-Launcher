package com.example.launcher.view

import android.content.ContentValues.TAG
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.launcher.viewmodel.DrawerViewModel
import com.example.launcher.viewmodel.HomeViewModel

@Composable
fun AppDrawer(navController: NavController, viewModel: DrawerViewModel = hiltViewModel(), viewModel2: HomeViewModel = hiltViewModel()) {
    // Get apps list from DrawerViewModel
    val apps by viewModel.apps.collectAsState()
    val folders by viewModel2.folders.collectAsState()

    // Layout for basic drawer interface
    LazyVerticalGrid(
        columns = GridCells.Fixed(2), // # items per row
        modifier = Modifier
            .background(
                color = Color.DarkGray.copy(alpha = 0.8F)
            ) // BG color and transparency value.
            .fillMaxSize()
            .pointerInput(Unit) { // Gesture Based Navigation
                detectHorizontalDragGestures(
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        if (dragAmount < -50F) { // Prevent accidental swipes with this value
                            navController.navigate("homeScreen")
                        }
                    }
                )
            },
        contentPadding = PaddingValues(
            25.dp,
            bottom = 64.dp // extra padding for the navbar
        ) // Padding around the whole grid
    ) {
        // Title item
        item(span = { GridItemSpan(2) }) { // Span across 2 columns
            Text(
                text = "Apps",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(64.dp), // Padding around the title
                textAlign = TextAlign.Center,
                color = Color.White
            )
        }

        items(apps) { app ->
            var showMenu by remember { mutableStateOf(false) }

            Box(
                modifier = Modifier
                    .padding(0.dp) // Padding between app "boxes"
                    .size(80.dp)
                    .clickable { viewModel.launchApp(app.packageName) }
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onLongPress = { showMenu = true }
                        )
                    }
            ) { Row(
                    modifier = Modifier
                        .clickable {
                            viewModel.launchApp(app.packageName)
                            Log.d(TAG, "HomeScreen: Opening app: " + app.label)
                        }
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                )
                {
                    // Display app icon
                    Image(
                        painter = remember { BitmapPainter(app.icon.asImageBitmap()) },
                        contentDescription = "${app.label} icon",
                        modifier = Modifier
                            .size(50.dp) // Icon Size
                            .padding(end = 12.dp) // Space between icon and text
                    )
                    // Display app text
                    Text(
                        text = app.label,
                        style = MaterialTheme.typography.labelLarge,
                        textAlign = TextAlign.Left,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.White
                    )
                    // TODO: Make this a custom menu
                    // TODO: enable drag and drop to folders
                    // TODO: Allow user to add 4-5 apps to the app dock
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        folders.forEach { folder ->
                            DropdownMenuItem(
                                text = { Text(folder) },
                                onClick = {
                                    viewModel2.addAppToFolder(
                                        app.packageName,
                                        folder
                                    ) // Borrowed method from homeViewModel to make sure the folders are handled by home entirely
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
