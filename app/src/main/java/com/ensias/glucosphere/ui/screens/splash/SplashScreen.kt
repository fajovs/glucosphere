package com.ensias.glucosphere.ui.screens.splash

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    isUserProfileExists: Boolean?,
    onNavigateToHome: () -> Unit,
    onNavigateToProfileSetup: () -> Unit
) {
    LaunchedEffect(isUserProfileExists) {
        delay(2000) // Show splash for 2 seconds
        when (isUserProfileExists) {
            true -> onNavigateToHome()
            false -> onNavigateToProfileSetup()
            null -> {} // Still loading
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "🩸",
                fontSize = 64.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "GlucoseSphere",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Monitor your health",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (isUserProfileExists == null) {
                Spacer(modifier = Modifier.height(32.dp))
                CircularProgressIndicator()
            }
        }
    }
}
