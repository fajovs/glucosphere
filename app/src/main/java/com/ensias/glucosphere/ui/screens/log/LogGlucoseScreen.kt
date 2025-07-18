package com.ensias.glucosphere.ui.screens.log

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.ensias.glucosphere.data.database.entity.ReadingType


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogGlucoseScreen(
    onNavigateBack: () -> Unit,
    viewModel: LogGlucoseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isReadingSaved) {
        if (uiState.isReadingSaved) {
            onNavigateBack()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Log Glucose Reading",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Glucose Level Input
        OutlinedTextField(
            value = uiState.glucoseLevel,
            onValueChange = viewModel::updateGlucoseLevel,
            label = { Text("Glucose Level (mg/dL)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Reading Type Selection
        Text(
            text = "Reading Type",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        ReadingType.values().forEach { type ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                RadioButton(
                    selected = uiState.selectedReadingType == type,
                    onClick = { viewModel.updateReadingType(type) }
                )
                Text(
                    text = type.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Notes Input
        OutlinedTextField(
            value = uiState.notes,
            onValueChange = viewModel::updateNotes,
            label = { Text("Notes (optional)") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Save Button
        Button(
            onClick = viewModel::saveReading,
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState.isFormValid && !uiState.isLoading
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Save Reading")
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
