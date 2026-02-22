package com.leopc.speakup.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel responsible for controlling the Splash Screen visibility duration.
 *
 * The splash screen stays visible while [isReady] is false.
 * After a 2-second simulated loading delay it flips to true, allowing the
 * SplashScreen API to dismiss and reveal the Login screen.
 */
class SplashViewModel : ViewModel() {

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    init {
        viewModelScope.launch {
            // Simulate app initialisation / asset pre-loading
            delay(2_000L)
            _isReady.value = true
        }
    }
}
