package com.leopc.speakup

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.leopc.speakup.ui.auth.LoginActivity
import com.leopc.speakup.ui.navigation.MainNavigation
import com.leopc.speakup.ui.splash.SplashViewModel
import com.leopc.speakup.ui.splash.WelcomeScreen
import com.leopc.speakup.ui.theme.SpeakUpTheme

class MainActivity : ComponentActivity() {

    private val splashViewModel: SplashViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        // ── 1. Install Splash Screen API ─────────────────────────────────
        val splashScreen = installSplashScreen()
        
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Keep splash on screen while ViewModel initializes (2 seconds)
        splashScreen.setKeepOnScreenCondition { !splashViewModel.isReady.value }

        // Premium exit animation for the splash icon
        splashScreen.setOnExitAnimationListener { splashScreenView ->
            val slideUp = android.animation.ObjectAnimator.ofFloat(
                splashScreenView.iconView,
                View.TRANSLATION_Y,
                0f,
                -splashScreenView.iconView.height.toFloat() * 2
            ).apply {
                duration = 500L
                interpolator = DecelerateInterpolator()
            }
            val fadeOut = android.animation.ObjectAnimator.ofFloat(
                splashScreenView.view,
                View.ALPHA,
                1f,
                0f
            ).apply { duration = 500L }
            
            android.animation.AnimatorSet().apply {
                playTogether(slideUp, fadeOut)
                doOnEnd { splashScreenView.remove() }
                start()
            }
        }

        setContent {
            SpeakUpTheme {
                val isReady by splashViewModel.isReady.collectAsState()
                
                // ── 2. Handle Authentication and Screen Transitions ─────────
                val currentUser = FirebaseAuth.getInstance().currentUser
                
                if (!isReady) {
                    // Show Welcome Screen while delay is active
                    WelcomeScreen()
                } else {
                    if (currentUser == null) {
                        // User not logged in? Redirect to Login once and finish
                        LaunchedEffect(Unit) {
                            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                            finish()
                        }
                    } else {
                        // User is logged in? Show Dashboard
                        val userName = currentUser.displayName ?: intent.getStringExtra("USER_NAME") ?: "User"
                        val userPhotoUrl = currentUser.photoUrl?.toString()
                        
                        val navController = rememberNavController()
                        MainNavigation(
                            navController = navController,
                            userName = userName,
                            userPhotoUrl = userPhotoUrl,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}