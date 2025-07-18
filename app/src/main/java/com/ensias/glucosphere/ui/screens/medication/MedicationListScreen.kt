package com.ensias.glucosphere.ui.screens.medication

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddMedication: () -> Unit,
    onNavigateToMedicationDetail: (Long) -> Unit,
    viewModel: MedicationListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

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
                text = "Medications",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onNavigateToAddMedication) {
                Icon(Icons.Default.Add, contentDescription = "Add Medication")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.medicationsWithSchedules.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "💊",
                            fontSize = 48.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No medications added yet",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Tap the + button to add your first medication",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.medicationsWithSchedules) { medicationWithSchedules ->
                    MedicationCard(
                        medicationWithSchedules = medicationWithSchedules,
                        onClick = { onNavigateToMedicationDetail(medicationWithSchedules.medication.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MedicationCard(
    medicationWithSchedules: com.ensias.glucosphere.data.database.entity.MedicationWithSchedules,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = medicationWithSchedules.medication.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = medicationWithSchedules.medication.dosage,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (medicationWithSchedules.medication.instructions.isNotEmpty()) {
                        Text(
                            text = medicationWithSchedules.medication.instructions,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Icon(
                    Icons.Default.DateRange,
                    contentDescription = "Schedules",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            if (medicationWithSchedules.schedules.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Scheduled times:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))

                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                val scheduleTimes = medicationWithSchedules.schedules
                    .filter { it.isActive }
                    .map { schedule ->
                        val calendar = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, schedule.timeHour)
                            set(Calendar.MINUTE, schedule.timeMinute)
                        }
                        timeFormat.format(calendar.time)
                    }
                    .joinToString(", ")

                Text(
                    text = scheduleTimes.ifEmpty { "No active schedules" },
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
