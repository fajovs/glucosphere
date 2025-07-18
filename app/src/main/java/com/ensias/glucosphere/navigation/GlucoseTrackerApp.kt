package com.ensias.glucosphere.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.ensias.glucosphere.ui.screens.analysis.AnalysisScreen
import com.ensias.glucosphere.ui.screens.home.HomeScreen
import com.ensias.glucosphere.ui.screens.log.LogGlucoseScreen
import com.ensias.glucosphere.ui.screens.profile.ProfileSetupScreen
import com.ensias.glucosphere.ui.screens.settings.SettingsScreen
import com.ensias.glucosphere.ui.screens.splash.SplashScreen
import com.ensias.glucosphere.ui.screens.splash.SplashViewModel
import com.ensias.glucosphere.ui.screens.medication.MedicationListScreen
import com.ensias.glucosphere.ui.screens.medication.AddMedicationScreen
import com.ensias.glucosphere.ui.screens.medication.MedicationDetailScreen
import com.ensias.glucosphere.ui.screens.medication.EditMedicationScreen

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun GlucoseTrackerApp() {
    val navController = rememberNavController()
    val splashViewModel: SplashViewModel = hiltViewModel()
    val isUserProfileExists by splashViewModel.isUserProfileExists.collectAsState()

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            SplashScreen(
                isUserProfileExists = isUserProfileExists,
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo("splash") { inclusive = true }
                    }
                },
                onNavigateToProfileSetup = {
                    navController.navigate("profile_setup") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        composable("profile_setup") {
            ProfileSetupScreen(
                onProfileCreated = {
                    navController.navigate("home") {
                        popUpTo("profile_setup") { inclusive = true }
                    }
                }
            )
        }

        composable("home") {
            HomeScreen(
                onNavigateToLogGlucose = { navController.navigate("log_glucose") },
                onNavigateToAnalysis = { navController.navigate("analysis") },
                onNavigateToSettings = { navController.navigate("settings") },
                onNavigateToMedications = { navController.navigate("medications") },
                onLogout = {
                    // Clear user data and navigate to profile setup
                    splashViewModel.clearUserData()
                    navController.navigate("profile_setup") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }

        composable("log_glucose") {
            LogGlucoseScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("analysis") {
            AnalysisScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("settings") {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEditProfile = { navController.navigate("edit_profile") }
            )
        }

        composable("edit_profile") {
            ProfileSetupScreen(
                isEditMode = true,
                onProfileCreated = { navController.popBackStack() }
            )
        }

        composable("medications") {
            MedicationListScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddMedication = { navController.navigate("add_medication") },
                onNavigateToMedicationDetail = { medicationId ->
                    navController.navigate("medication_detail/$medicationId")
                }
            )
        }

        composable("add_medication") {
            AddMedicationScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            "medication_detail/{medicationId}",
            arguments = listOf(navArgument("medicationId") { type = NavType.LongType })
        ) { backStackEntry ->
            val medicationId = backStackEntry.arguments?.getLong("medicationId") ?: -1L
            MedicationDetailScreen(
                medicationId = medicationId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { medicationId ->
                    navController.navigate("edit_medication/$medicationId")
                }
            )
        }

        composable(
            "edit_medication/{medicationId}",
            arguments = listOf(navArgument("medicationId") { type = NavType.LongType })
        ) { backStackEntry ->
            val medicationId = backStackEntry.arguments?.getLong("medicationId") ?: -1L
            EditMedicationScreen(
                medicationId = medicationId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
