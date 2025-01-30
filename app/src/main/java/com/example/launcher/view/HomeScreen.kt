package com.example.launcher.view

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.launcher.viewmodel.HomeViewModel

@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel(), navController: NavController) {
    val folders = viewModel.folders.collectAsState().value
    var showInputDialog by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectHorizontalDragGestures { change, dragAmount ->
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
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            folders.forEach { folder ->
                Text(
                    text = folder,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }
    }

    if (showInputDialog) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp)
        ) {
            BasicTextField(
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
                    .background(Color.LightGray)
                    .padding(8.dp)
            )
            Button(onClick = {
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
}
