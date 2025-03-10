package com.example.launcher.view

import android.graphics.drawable.Drawable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.launcher.viewmodel.HomeViewModel
import com.google.accompanist.drawablepainter.DrawablePainter

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FolderList(viewModel: HomeViewModel) {
    val folders = viewModel.folders.collectAsState().value

    var expandedFolder by remember { mutableStateOf<String?>(null) } // Track which folder is expanded


    folders.forEach { folder ->
        var showFolderMenu by remember { mutableStateOf(false) }

        val folderModifier =
            if (expandedFolder == folder) { // Makes a new look for the folder when its opened (dark background)
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.DarkGray.copy(alpha = 0.6F))
                    .padding(8.dp)
            } else {
                Modifier
            }

        Column( // folder names column
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
                        onLongClick = { showFolderMenu = true },
                    ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Press and hold folder to show this menu (Allows use to delete the folder)
            DropdownMenu(
                expanded = showFolderMenu,
                onDismissRequest = { showFolderMenu = false }
            ) {
                DropdownMenuItem( // TODO: This menu is revealed on all folders
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
                FolderItem(viewModel, folder)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FolderItem(viewModel: HomeViewModel, folder: String) {
    val appsInFolder by viewModel.appsInFolder.collectAsState(emptyList())
    val context = LocalContext.current
    val packageManager = context.packageManager


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