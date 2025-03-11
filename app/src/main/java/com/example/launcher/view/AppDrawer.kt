package com.example.launcher.view


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
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.launcher.viewmodel.DrawerViewModel
import com.example.launcher.viewmodel.HomeViewModel
import com.google.accompanist.drawablepainter.DrawablePainter

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppDrawer(
    navController: NavController,
    viewModel: DrawerViewModel,
    viewModel2: HomeViewModel
) {

    // Get apps list from DrawerViewModel
    val apps by viewModel.apps.collectAsState()
    val greyScale by viewModel.greyScaledApps.collectAsState()
    val folders by viewModel2.folders.collectAsState()
    val context = LocalContext.current
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val packageManager = context.packageManager
    val expandedMenuState = remember { mutableStateMapOf<String, Boolean>() } // Remember the dropdown (better for performance to be out here)
    val greyscaleMatrix = ColorMatrix().apply { setToSaturation(0f) }
    val isGreyscaleEnabled = greyScale


    val totalWidth = with(density) { configuration.screenWidthDp.dp.toPx() } // Screen width
    val duration = 300 // Animation speed in ms
    val animationSpec = tween<Float>(duration) // Animation when returning
    val decaySpec = rememberSplineBasedDecay<Float>() // Physics based decay
    val anchors = DraggableAnchors {
        0 at 0f // Default position anchor
        1 at -totalWidth // Right swipe anchor
        2 at totalWidth // Left swipe anchor
    }

    val offsetX by remember { mutableStateOf(0f) }
    val draggableState = remember {
        AnchoredDraggableState(
         initialValue = 0,
         anchors = anchors,
         positionalThreshold = { totalWidth * 0.4f }, // 40% of screen to swipe
         velocityThreshold = { with(density) { 125.dp.toPx() } }, //Fling speed
         snapAnimationSpec = animationSpec, // Animation when releasing mid-swipe
         decayAnimationSpec = decaySpec, // Fling animation
         confirmValueChange = { true } // Always allow swiping
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
                    navController.navigate("homeScreen") // Go to homescreen
                }
            }
    }

    // Layout for basic drawer interface
    LazyVerticalGrid(
        columns = GridCells.Fixed(2), // # items per row
        modifier = Modifier
            .background(
                color = Color.LightGray.copy(alpha = 0.8F)
            ) // BG color and transparency value.
            .fillMaxSize()
            .graphicsLayer { translationX = animatedOffsetX }
            .anchoredDraggable(
                state = draggableState,
                orientation = Orientation.Horizontal,
            ),
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

        item(span = {GridItemSpan(1)}) {
            Button(
                onClick = {viewModel.toggleGreyScaleApps()}
            ) {
                Text("Greyscale")
            }
        }

        item(span = {GridItemSpan(1)}) {
            Button(
                onClick = {viewModel.sendToWellbeing(context)}
            ) {
                Text("Digital Wellbeing")
            }
        }

        items(apps) { app ->
            val isExpanded = expandedMenuState[app.packageName] ?: false
            val icon: Drawable = packageManager.getApplicationIcon(app.packageName)

            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .size(64.dp)
            ) {
                Row( // Responsible for the organization inside the box
                    modifier = Modifier
                        .combinedClickable(
                            onClick = { viewModel.launchApp(app.packageName) },
                            onLongClick = { expandedMenuState[app.packageName] = !isExpanded }
                        )
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                )
                {
                    // Display app icon
                    Image(
                        painter = DrawablePainter(icon), // This is the use of the accompanist library, which is shit, just an fyi, took my almost two days to get working
                        contentDescription = "${app.label} icon",
                        colorFilter = if (isGreyscaleEnabled) ColorFilter.colorMatrix(greyscaleMatrix) else null,
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
                        expanded = isExpanded,
                        onDismissRequest = { expandedMenuState[app.packageName] = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Dock") },
                            onClick = {
                                viewModel2.addToDock(app.packageName)
                                Log.d("AppDrawer", "add to app dock button clicked")
                                expandedMenuState[app.packageName] = false
                            }
                        )
                        folders.forEach { folder ->
                            DropdownMenuItem(
                                text = { Text(folder) },
                                onClick = {
                                    viewModel2.addAppToFolder(app.packageName, folder)
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