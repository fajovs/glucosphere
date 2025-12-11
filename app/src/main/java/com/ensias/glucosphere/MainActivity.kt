package com.ensias.glucosphere

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.ensias.glucosphere.navigation.GlucoseTrackerApp
import com.ensias.glucosphere.ui.theme.GlucoseTrackerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d("MainActivity", "Notification permission granted")
        } else {
            Log.w("MainActivity", "Notification permission denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request necessary permissions
        requestPermissions()

        setContent {
            GlucoseTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GlucoseTrackerApp()
                }
            }
        }
    }

    private fun requestPermissions() {
        // Request notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // Request exact alarm permission on Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                Log.w("MainActivity", "Exact alarm permission not granted, requesting...")
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:$packageName")
                }
                try {
                    startActivity(intent)
                } catch (e: Exception) {
                    Log.e("MainActivity", "Error requesting exact alarm permission", e)
                }
            }
        }
    }
}

//
//import android.Manifest
//import android.app.AlarmManager
//import android.content.Context
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.net.Uri
//import android.os.Build
//import android.os.Bundle
//import android.provider.Settings
//import android.util.Log
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Surface
//import androidx.compose.material3.Text
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.sp
//import androidx.core.content.ContextCompat
//import com.ensias.glucosphere.navigation.GlucoseTrackerApp
//import com.ensias.glucosphere.ui.theme.GlucoseTrackerTheme
//import dagger.hilt.android.AndroidEntryPoint
//import java.util.*
//
//@AndroidEntryPoint
//class MainActivity : ComponentActivity() {
//
//    private val notificationPermissionLauncher = registerForActivityResult(
//        ActivityResultContracts.RequestPermission()
//    ) { isGranted ->
//        if (isGranted) {
//            Log.d("MainActivity", "Notification permission granted")
//        } else {
//            Log.w("MainActivity", "Notification permission denied")
//        }
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        // 🔒 Timebomb: disable app after November 4, 2025
//        if (isExpired()) {
//            setContent {
//                GlucoseTrackerTheme {
//                    Surface(
//                        modifier = Modifier.fillMaxSize(),
//                        color = MaterialTheme.colorScheme.background
//                    ) {
//                        Column(
//                            modifier = Modifier.fillMaxSize(),
//                            horizontalAlignment = Alignment.CenterHorizontally,
//                            verticalArrangement = Arrangement.Center
//                        ) {
//                            Text(
//                                text = "App Disabled",
//                                fontSize = 28.sp,
//                                fontWeight = FontWeight.Bold,
//                                color = MaterialTheme.colorScheme.error
//                            )
//                            Text(
//                                text = "This application is no longer available.",
//                                fontSize = 18.sp
//                            )
//                        }
//                    }
//                }
//            }
//            return // 🚫 stop launching the rest of the app
//        }
//
//        // Request permissions normally
//        requestPermissions()
//
//        setContent {
//            GlucoseTrackerTheme {
//                Surface(
//                    modifier = Modifier.fillMaxSize(),
//                    color = MaterialTheme.colorScheme.background
//                ) {
//                    GlucoseTrackerApp()
//                }
//            }
//        }
//    }
//
//    private fun isExpired(): Boolean {
//        // December 15, 2025
//        val expiryDate = Calendar.getInstance().apply {
//            set(2025, Calendar.DECEMBER, 15, 0, 0, 0)
//        }.timeInMillis
//        val currentDate = System.currentTimeMillis()
//        return currentDate >= expiryDate
//    }
//
//    private fun requestPermissions() {
//        // Request notification permission on Android 13+
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
//                != PackageManager.PERMISSION_GRANTED) {
//                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
//            }
//        }
//
//        // Request exact alarm permission on Android 12+
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
//            if (!alarmManager.canScheduleExactAlarms()) {
//                Log.w("MainActivity", "Exact alarm permission not granted, requesting...")
//                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
//                    data = Uri.parse("package:$packageName")
//                }
//                try {
//                    startActivity(intent)
//                } catch (e: Exception) {
//                    Log.e("MainActivity", "Error requesting exact alarm permission", e)
//                }
//            }
//        }
//    }
//}
//
