package com.leopc.speakup.ui.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A minimalist, premium Welcome Screen shown immediately after the Splash Screen.
 */
@Composable
fun WelcomeScreen() {
    // Premium gradient background: Deep Navy to a slightly lighter Navy
    val gradientBackground = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1A237E), // Navy Profundo
            Color(0xFF283593)  // Indigo/Navy más suave
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "Welcome back",
                color = Color(0xFFFFD700), // Gold accent
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "let's learn English",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 18.sp,
                fontWeight = FontWeight.Light,
                textAlign = TextAlign.Center,
                letterSpacing = 1.sp
            )
        }
    }
}
