package com.example.launcher.view

import android.content.ContentValues.TAG
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
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
import androidx.compose.material3.Button
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.launcher.viewmodel.DrawerViewModel

@Composable
fun AppDrawer(navController: NavController, viewModel: DrawerViewModel = hiltViewModel()) {
    // Get apps list from HomeViewModel
    val apps by viewModel.apps.collectAsState()

    // Scaffold layout for a basic launcher interface
    LazyVerticalGrid(
        columns = GridCells.Fixed(2), // # items per row
        modifier = Modifier
            .background(
                color = Color.LightGray.copy(alpha = 1F)) // Can be made transparent.
            .fillMaxSize(),
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

        // Placeholder navigation button
        item(span = { GridItemSpan(2) }) {
            // Navigate to AppDrawer
            Button(
                onClick = { navController.navigate("homeScreen") },
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text(text = "Go to home screen")
            }
        }

        items(apps) { app ->

            // Layout of the apps
            Row(
                modifier = Modifier
                    .clickable {
                        viewModel.launchApp(app.packageName)
                        Log.d(TAG, "HomeScreen: Opening app: " + app.label)
                    }
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
