package com.example.launcher.view


import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
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
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.launcher.R
import com.example.launcher.viewmodel.DrawerViewModel
import com.example.launcher.viewmodel.HomeViewModel
import com.google.accompanist.drawablepainter.DrawablePainter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppDrawer(
    navController: NavController,
    drawerViewModel: DrawerViewModel,
    homeViewModel: HomeViewModel
) {

    // Get apps list from DrawerViewModel
    val apps by drawerViewModel.apps.collectAsState()
    val greyScale by drawerViewModel.greyScaledApps.collectAsState()
    val pendingApp by drawerViewModel.pendingLaunchApp.collectAsState()
    val folders by homeViewModel.folders.collectAsState()
    val context = LocalContext.current
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val packageManager = context.packageManager
    val expandedMenuState = remember { mutableStateMapOf<String, Boolean>() } // Remember the dropdown (better for performance to be out here)
    val greyscaleMatrix = ColorMatrix().apply { setToSaturation(0f) }

    //Cache app icons
    val iconCache = remember { mutableMapOf<String, Drawable>() }

    // State for the search bar
    var searchQuery by remember { mutableStateOf("") }

    // Derive a filtered list of apps based on the searchbar query.
    // If the search query is blank, show all apps.
    val filteredApps = if (searchQuery.isBlank()) {
        apps
    } else {
        apps.filter { it.label.contains(searchQuery, ignoreCase = true) }
    }

    // Following is the stuff related to making a smooth "swipe" system to go to another page
    val totalWidth = with(density) { configuration.screenWidthDp.dp.toPx() }    // Screen width
    val duration = 300                                                          // Animation speed in ms
    val animationSpec = tween<Float>(duration)                                  // Animation when returning
    val decaySpec = rememberSplineBasedDecay<Float>()                           // Physics based decay
    val anchors = DraggableAnchors {
        0 at 0f                                                                 // Default position anchor
        1 at -totalWidth                                                        // Right swipe anchor
        2 at totalWidth                                                         // Left swipe anchor
    }

    // Define the anchors and swipe conditions for navigation
    val offsetX by remember { mutableStateOf(0f) }
    val draggableState = remember {
        AnchoredDraggableState(
         initialValue = 0,
         anchors = anchors,
         positionalThreshold = { totalWidth * 0.4f },               // 40% of screen to swipe
         velocityThreshold = { with(density) { 125.dp.toPx() } },   //Fling speed
         snapAnimationSpec = animationSpec,                         // Animation when releasing mid-swipe
         decayAnimationSpec = decaySpec,                            // Fling animation
         confirmValueChange = { true }                              // Always allow swiping
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
                if (target == 1 || target == 2) {               // If swiped left or right
                    navController.navigate("homeScreen")  // Go to homescreen
                }
            }
    }

    // If a launch is pending, show the confirmation dialog
    if (pendingApp != null) {
        AlertDialog(
            onDismissRequest = { drawerViewModel.cancelLaunch() },
            title = { Text("Confirm App Launch") },
            text = { Text("Are you sure you want to open this app?") },
            confirmButton = {
                TextButton(onClick = { drawerViewModel.confirmLaunch() }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { drawerViewModel.cancelLaunch() }) {
                    Text("No")
                }
            }
        )
    }

    // Layout for basic drawer interface, this holds everything (searchbar, settings, title, apps)
    LazyVerticalGrid(
        columns = GridCells.Fixed(2), // # items per row
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.background.copy(alpha = 0.8F)
            )
            .padding(bottom = 20.dp)
            .fillMaxSize()
            .graphicsLayer { translationX = animatedOffsetX }
            .anchoredDraggable(
                state = draggableState,
                orientation = Orientation.Horizontal,
            ),
        contentPadding = PaddingValues(20.dp) // Padding around the whole grid
    ) {


        // Title item
        item(span = { GridItemSpan(2) }) { // Span across 2 columns, hols settings button and apps label
            Box(
                modifier = Modifier
                    .padding( // Padding around title and settings
                        16.dp,
                        bottom = 32.dp,
                        top = 64.dp
                    )
            )
            {
                Button( // This is the button that brings you to the settings screen, I wasn't sure if I should make this a new page or do this
                    onClick = { navController.navigate("settingsScreen") } ,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,     // Black background
                        contentColor = MaterialTheme.colorScheme.onPrimary      // White text and icon
                    ),
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(8.dp)
                ) {

                        Icon(
                            painter = painterResource(id = R.drawable.settings_24), // I got the icons from https://fonts.google.com/icons
                            contentDescription = "Settings",
                            modifier = Modifier
                                .size(24.dp)
                                .padding(4.dp)
                        )
                        Text(
                            text = "Settings"
                        )

                }
                Text( // This is the title item, next to the settings button
                    text = "Apps",
                    style = MaterialTheme.typography.displayMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp) // Padding around the title
                        .align(Alignment.CenterStart),
                    textAlign = TextAlign.Left,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        item (span = { GridItemSpan(2) }) { // Search Bar
            TextField(
                value = searchQuery, // Send this back up to the top where we filter down the apps to only be apps which include this value
                onValueChange = { searchQuery = it },
                label = { Text("Search Apps") },
                modifier = Modifier
                    .fillMaxWidth()
            )
        }

        // This is where the "true" lazy grid starts, this shows the apps and icons
        items(filteredApps) { app -> // Only display apps that have been searched for (filtered apps)
            val isExpanded = expandedMenuState[app.packageName] ?: false

            // Cache the icons, use a background thread using dispatchers. Hopefully this triggers GC less
            val cachedIcon = iconCache[app.packageName]
            LaunchedEffect(app.packageName) {
                if (cachedIcon == null) {
                    // laod the icon in a background thread
                    val loadedIcon = withContext(Dispatchers.IO) {
                        packageManager.getApplicationIcon(app.packageName)
                    }
                    iconCache[app.packageName] = loadedIcon
                }
            }

            // Certain apps, like system apps (bloatware lol) cant be uninstalled, for instance the google play store, this bit here notes what apps
            // those are and makes sure the dropdown menu doesn't display an uninstall button
            val isSystemApp = try {
                val appInfo = packageManager.getApplicationInfo(app.packageName, 0)
                (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            } catch (e: PackageManager.NameNotFoundException) {
                false
            }

            Box( // This is the actual "app"
                modifier = Modifier
                    .padding(8.dp)
                    .size(64.dp)
            ) {
                Row( // Responsible for the organization inside the box
                    modifier = Modifier
                        .combinedClickable(
                            onClick = { drawerViewModel.preLaunchApp(app.packageName) },
                            onLongClick = { expandedMenuState[app.packageName] = !isExpanded }
                        )
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                )
                {
                    // Display app icon
                    if (cachedIcon != null) { // Show icon if its been loaded
                        Image(
                            painter = DrawablePainter(cachedIcon), // This is the use of the accompanist library, which is shit, just an fyi, took my almost two days to get working
                            contentDescription = "${app.label} icon",
                            colorFilter = if (greyScale) ColorFilter.colorMatrix(greyscaleMatrix) else null,
                            modifier = Modifier
                                .size(50.dp)            // Icon Size
                                .padding(end = 12.dp)   // Space between icon and text
                        )
                    } else { // Show a placeholder box that's the same size as the icon if the icon hasn't been loaded (prevents lag when loading icons)
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .background(MaterialTheme.colorScheme.surface)
                        )
                    }


                    // Display app text
                    Text(
                        text = app.label,
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.labelLarge,
                        textAlign = TextAlign.Left,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                    DropdownMenu( // This dropdown menu allows users to uninstall apps, move them to folders, or to the app dock
                        expanded = isExpanded,
                        onDismissRequest = { expandedMenuState[app.packageName] = false }
                    ) {
                        if(!isSystemApp) {
                            DropdownMenuItem( // Uninstall app button
                                text = { Text("Uninstall") },
                                onClick = {
                                    homeViewModel.deleteApp(app.packageName)
                                    Log.d("AppDrawer", "Deleting app $app.label")
                                    expandedMenuState[app.packageName] = false
                                }
                            )
                        }
                        DropdownMenuItem( // Add to the app dock on the homescreen
                            text = { Text("Dock") },
                            onClick = {
                                homeViewModel.addToDock(app.packageName)
                                Log.d("AppDrawer", "add to app dock button clicked")
                                expandedMenuState[app.packageName] = false
                            }
                        )
                        folders.forEach { folder -> // Display list of folders that have been added to the home screen
                            DropdownMenuItem(  // Allow users to click on those folders to add it to that folder
                                text = { Text(folder) },
                                onClick = {
                                    homeViewModel.addAppToFolder(app.packageName, folder)
                                    expandedMenuState[app.packageName] = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}