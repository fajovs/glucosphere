package com.ensias.glucosphere.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupScreen(
    isEditMode: Boolean = false,
    onProfileCreated: () -> Unit,
    viewModel: ProfileSetupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isProfileSaved) {
        if (uiState.isProfileSaved) {
            onProfileCreated()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        if (isEditMode) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onProfileCreated) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "Edit Profile",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            Text(
                text = "Set Up Your Diabetes Profile",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Step ${uiState.currentStep + 1} of 4",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 32.dp)
            )
        }

        when (uiState.currentStep) {
            0 -> StepBasicInfo(uiState, viewModel, isEditMode)
            1 -> StepHealthStatus(uiState, viewModel)
            2 -> StepInsulinQuestion(uiState, viewModel)
            3 -> StepReviewTargets(uiState, viewModel)
        }

        Spacer(modifier = Modifier.height(32.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (uiState.currentStep > 0) {
                OutlinedButton(
                    onClick = viewModel::previousStep,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Back")
                }
            } else if (!isEditMode) {
                Spacer(modifier = Modifier.weight(1f))
            }

            Button(
                onClick = {
                    if (uiState.currentStep < 3) viewModel.nextStep()
                    else viewModel.saveProfile()
                },
                modifier = Modifier.weight(1f),
                enabled = uiState.isFormValid && !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(if (uiState.currentStep < 3) "Next" else if (isEditMode) "Update Profile" else "Create Profile")
                }
            }
        }

        if (uiState.errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = uiState.errorMessage,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun StepBasicInfo(
    uiState: ProfileSetupUiState,
    viewModel: ProfileSetupViewModel,
    isEditMode: Boolean
) {
    Text(
        text = "Basic Information",
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 24.dp)
    )

    OutlinedTextField(
        value = uiState.username,
        onValueChange = viewModel::updateUsername,
        label = { Text("Username") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        enabled = !isEditMode || uiState.username.isEmpty()
    )

    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
        value = uiState.age,
        onValueChange = viewModel::updateAge,
        label = { Text("Age") },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true
    )

    if (uiState.isOver65) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "✓ Additional health information will be requested in the next step.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun StepHealthStatus(
    uiState: ProfileSetupUiState,
    viewModel: ProfileSetupViewModel
) {
    Text(
        text = "Health Status",
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 8.dp)
    )

    Text(
        text = "How would you describe your current health?",
        fontSize = 14.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 24.dp)
    )

    val healthOptions = listOf(
        "healthy" to "Healthy / Independent",
        "multiple_conditions" to "Multiple Chronic Conditions",
        "frail" to "Frail / Needs Assistance"
    )

    healthOptions.forEach { (value, label) ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = uiState.healthStatus == value,
                onClick = { viewModel.updateHealthStatus(value) }
            )
            Text(text = label, modifier = Modifier.padding(start = 16.dp))
        }
    }
}

@Composable
private fun StepInsulinQuestion(
    uiState: ProfileSetupUiState,
    viewModel: ProfileSetupViewModel
) {
    Text(
        text = "Insulin Therapy",
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 8.dp)
    )

    Text(
        text = "Are you currently taking insulin?",
        fontSize = 14.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 24.dp)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = uiState.insulinUser,
            onClick = { viewModel.updateInsulinUser(true) }
        )
        Text(text = "Yes", modifier = Modifier.padding(start = 16.dp))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = !uiState.insulinUser,
            onClick = { viewModel.updateInsulinUser(false) }
        )
        Text(text = "No", modifier = Modifier.padding(start = 16.dp))
    }

    Spacer(modifier = Modifier.height(16.dp))
    Text(
        text = "ℹ Insulin users often need stricter monitoring.",
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodySmall
    )
}

@Composable
private fun StepReviewTargets(
    uiState: ProfileSetupUiState,
    viewModel: ProfileSetupViewModel
) {
    Text(
        text = "Recommended Glucose Targets",
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 8.dp)
    )

    Text(
        text = "Based on your age and health profile",
        fontSize = 14.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 24.dp)
    )

    // Display recommended targets
    TargetRangeDisplay(
        label = "Fasting",
        min = uiState.fastingMin,
        max = uiState.fastingMax
    )
    TargetRangeDisplay(
        label = "Pre-Meal",
        min = uiState.preMealMin,
        max = uiState.preMealMax
    )
    TargetRangeDisplay(
        label = "Post-Meal (1-2 hr)",
        min = "—",
        max = uiState.postMealMax
    )
    TargetRangeDisplay(
        label = "Random",
        min = "—",
        max = uiState.randomMax
    )

    Spacer(modifier = Modifier.height(32.dp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = uiState.useRecommendedTargets,
            onCheckedChange = viewModel::toggleUseRecommendedTargets
        )
        Text(
            text = "Use recommended targets",
            modifier = Modifier.padding(start = 12.dp)
        )
    }

    if (!uiState.useRecommendedTargets) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Customize My Targets",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        TargetRangeInput(
            label = "Fasting (mg/dL)",
            minValue = uiState.fastingMin,
            maxValue = uiState.fastingMax,
            onMinChange = viewModel::updateFastingMin,
            onMaxChange = viewModel::updateFastingMax
        )

        TargetRangeInput(
            label = "Pre-Meal (mg/dL)",
            minValue = uiState.preMealMin,
            maxValue = uiState.preMealMax,
            onMinChange = viewModel::updatePreMealMin,
            onMaxChange = viewModel::updatePreMealMax
        )

        OutlinedTextField(
            value = uiState.postMealMax,
            onValueChange = viewModel::updatePostMealMax,
            label = { Text("Post-Meal Max (mg/dL)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = uiState.randomMax,
            onValueChange = viewModel::updateRandomMax,
            label = { Text("Random Max (mg/dL)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )
    }
}

@Composable
private fun TargetRangeDisplay(
    label: String,
    min: String,
    max: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 16.sp)
        Text(
            text = "$min – $max mg/dL",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun TargetRangeInput(
    label: String,
    minValue: String,
    maxValue: String,
    onMinChange: (String) -> Unit,
    onMaxChange: (String) -> Unit
) {
    Text(
        text = label,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(bottom = 8.dp)
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedTextField(
            value = minValue,
            onValueChange = onMinChange,
            label = { Text("Min") },
            modifier = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )

        OutlinedTextField(
            value = maxValue,
            onValueChange = onMaxChange,
            label = { Text("Max") },
            modifier = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )
    }

    Spacer(modifier = Modifier.height(16.dp))
}
