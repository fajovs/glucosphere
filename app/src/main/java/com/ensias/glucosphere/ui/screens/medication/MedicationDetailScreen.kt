package com.ensias.glucosphere.ui.screens.medication

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
fun MedicationDetailScreen(
    medicationId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    viewModel: MedicationDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(medicationId) {
        viewModel.loadMedication(medicationId)
    }

    LaunchedEffect(uiState.isMedicationDeleted) {
        if (uiState.isMedicationDeleted) {
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
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Medication Details",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = { onNavigateToEdit(medicationId) },
                enabled = !uiState.isLoading
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = "Edit Medication",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(
                onClick = { showDeleteDialog = true },
                enabled = !uiState.isLoading
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete Medication",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            uiState.medicationWithSchedules?.let { medicationWithSchedules ->
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Medication Info Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Medication Information",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                MedicationInfoRow(
                                    label = "Name",
                                    value = medicationWithSchedules.medication.name
                                )

                                MedicationInfoRow(
                                    label = "Dosage",
                                    value = medicationWithSchedules.medication.dosage
                                )

                                if (medicationWithSchedules.medication.instructions.isNotEmpty()) {
                                    MedicationInfoRow(
                                        label = "Instructions",
                                        value = medicationWithSchedules.medication.instructions
                                    )
                                }

                                MedicationInfoRow(
                                    label = "Status",
                                    value = if (medicationWithSchedules.medication.isActive) "Active" else "Inactive"
                                )
                            }
                        }
                    }

                    // Schedules Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Schedules",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Icon(
                                        Icons.Default.DateRange,
                                        contentDescription = "Schedules",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                if (medicationWithSchedules.schedules.isEmpty()) {
                                    Text(
                                        text = "No schedules set",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    medicationWithSchedules.schedules.forEach { schedule ->
                                        ScheduleItem(schedule = schedule)
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                }
                            }
                        }
                    }

                    // Recent Logs Card - Now Scrollable
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Medication Logs",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                if (uiState.recentLogs.isEmpty()) {
                                    Text(
                                        text = "No logs yet",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    // Scrollable logs section with fixed height
                                    LazyColumn(
                                        modifier = Modifier.height(200.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        items(uiState.recentLogs) { log ->
                                            LogItem(log = log)
                                        }
                                    }

                                    if (uiState.recentLogs.size >= 10) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Showing last 10 logs",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }
                    }
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

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Medication") },
            text = {
                Text("Are you sure you want to delete this medication? This will also remove all schedules and logs associated with it.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteMedication()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun MedicationInfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(2f)
        )
    }
}

@Composable
private fun ScheduleItem(
    schedule: com.ensias.glucosphere.data.database.entity.MedicationSchedule
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, schedule.timeHour)
                set(Calendar.MINUTE, schedule.timeMinute)
            }

            Text(
                text = timeFormat.format(calendar.time),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (schedule.reminderEnabled) {
                    Text(
                        text = "🔔",
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    text = if (schedule.isActive) "Active" else "Inactive",
                    fontSize = 12.sp,
                    color = if (schedule.isActive)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun LogItem(
    log: com.ensias.glucosphere.data.database.entity.MedicationLog
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (log.taken)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = if (log.taken) "✅ Taken" else "❌ Missed",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                        .format(log.actualTime),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (log.notes.isNotEmpty()) {
                Text(
                    text = log.notes,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
