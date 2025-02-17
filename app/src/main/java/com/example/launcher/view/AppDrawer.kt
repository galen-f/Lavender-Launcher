package com.example.launcher.view

import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.launcher.utils.AppIconUtils.getAppIconBitmap
import com.example.launcher.viewmodel.DrawerViewModel
import com.example.launcher.viewmodel.HomeViewModel
import com.google.accompanist.drawablepainter.DrawablePainter

@Composable
fun AppDrawer(
    navController: NavController,
    viewModel: DrawerViewModel,
    viewModel2: HomeViewModel
) {

    // Get apps list from DrawerViewModel
    val apps by viewModel.apps.collectAsState()
    val folders by viewModel2.folders.collectAsState()
    val context = LocalContext.current
    val packageManager = context.packageManager

    // Layout for basic drawer interface
    LazyVerticalGrid(
        columns = GridCells.Fixed(2), // # items per row
        modifier = Modifier
            .background(
                color = Color.LightGray.copy(alpha = 0.8F)
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
        contentPadding = PaddingValues(20.dp) // Padding around the whole grid
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
                color = Color.Black
            )
        }

        items(apps) { app ->
            var showMenu by remember { mutableStateOf(false) }
            val icon: Drawable = packageManager.getApplicationIcon(app.packageName)

            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .size(64.dp)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onLongPress = { showMenu = true }
                        )
                    }
            ) {
                Row( // Responsible for the organization inside the box
                    modifier = Modifier
                        .clickable {
                            viewModel.launchApp(app.packageName)
                            Log.d(TAG, "HomeScreen: Opening app: " + app.label)
                        }
                        .padding(20.dp)
                        .pointerInput(Unit) {
                        detectTapGestures(
                            onLongPress = { showMenu = true }
                        )
                        },
                    verticalAlignment = Alignment.CenterVertically
                )
                {
                    // Display app icon
                    Image(
                        painter = DrawablePainter(icon), // This is the use of the accompanist library, which is shit, just an fyi, took my almost two days to get working
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
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        folders.forEach { folder ->
                            DropdownMenuItem(
                                text = { Text(folder) },
                                onClick = {
                                    viewModel2.addAppToFolder(app.packageName, folder)
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
