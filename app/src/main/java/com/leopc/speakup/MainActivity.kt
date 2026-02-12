package com.leopc.speakup

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.leopc.speakup.ui.navigation.MainNavigation
import com.leopc.speakup.ui.theme.SpeakUpTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Get user data from Firebase
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userName = currentUser?.displayName ?: intent.getStringExtra("USER_NAME") ?: "User"
        val userPhotoUrl = currentUser?.photoUrl?.toString()
        
        setContent {
            SpeakUpTheme {
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