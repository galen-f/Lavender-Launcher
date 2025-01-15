package com.example.launcher.view

import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.os.UserHandle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.launcher.viewmodel.HomeViewModel

@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    // Get apps list from HomeViewModel
    val apps by viewModel.apps.collectAsState()

    // Scaffold layout for a basic launcher interface
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(apps) { app ->
            // Display app icon
            Image(
                painter = remember { BitmapPainter(app.icon.asImageBitmap()) },
                contentDescription = "${app.label} icon",
                modifier = Modifier
                    .size(48.dp)
                    .padding(end = 16.dp)
            )

            Text(
                text = app.label,
                modifier = Modifier
                    .clickable { viewModel.launchApp(app.packageName) }
                    .padding(16.dp),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
