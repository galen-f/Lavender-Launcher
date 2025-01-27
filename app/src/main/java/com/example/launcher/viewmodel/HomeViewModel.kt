package com.example.launcher.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {

    // Placeholder state
    private val _message = MutableStateFlow("Welcome to the Home Screen!")
    val message: StateFlow<String> = _message
}
