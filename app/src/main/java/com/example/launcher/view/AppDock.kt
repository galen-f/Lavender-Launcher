package com.example.launcher.view

import android.graphics.drawable.Drawable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.launcher.viewmodel.HomeViewModel
import com.google.accompanist.drawablepainter.DrawablePainter

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppDock(viewModel: HomeViewModel) {
    val dockApps by viewModel.dockApps.collectAsState(emptyList())
    val greyScale by viewModel.greyScaledApps.collectAsState()
    val greyscaleMatrix = ColorMatrix().apply { setToSaturation(0f) }
    val context = LocalContext.current
    val packageManager = context.packageManager

    if (dockApps.isEmpty()) {
        // If there are no apps in the app dock, don't show the app dock
    } else {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.6F))
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
                dockApps.forEach { app ->
                    var showMenu by remember { mutableStateOf(false) }
                    val icon: Drawable = packageManager.getApplicationIcon(app.packageName)
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .combinedClickable(
                                    onClick = { viewModel.launchApp(app.packageName) },
                                    onLongClick = { showMenu = true }
                                )
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                painter = DrawablePainter(icon),
                                contentDescription = "${app.label} icon",
                                colorFilter = if (greyScale) ColorFilter.colorMatrix(greyscaleMatrix) else null,
                                modifier = Modifier.size(50.dp)
                            )
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Remove from dock") },
                                    onClick = {
                                        viewModel.removeFromDock(app.packageName)
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
