package com.ensias.glucosphere.ui.screens.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.List
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
import com.ensias.glucosphere.data.database.entity.GlucoseReading
import com.ensias.glucosphere.data.database.entity.MedicationLog
import com.ensias.glucosphere.data.database.entity.Medication

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onNavigateBack: () -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("History") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                MonthYearFilter(
                    selectedMonth = uiState.selectedMonth,
                    selectedYear = uiState.selectedYear,
                    onMonthYearChanged = { month, year ->
                        viewModel.updateSelectedMonth(month, year)
                    }
                )

                // Tab Selection
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Glucose Readings") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Medication Logs") }
                    )
                }

                // Content
                when (selectedTab) {
                    0 -> GlucoseReadingHistory(
                        readings = uiState.filteredGlucoseReadings,
                        userProfile = uiState.userProfile
                    )
                    1 -> MedicationLogsHistory(
                        medicationLogs = uiState.filteredMedicationLogs,
                        medications = uiState.medications
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthYearFilter(
    selectedMonth: Int,
    selectedYear: Int,
    onMonthYearChanged: (Int, Int) -> Unit
) {
    val months = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = {
            val newMonth = if (selectedMonth == 0) 11 else selectedMonth - 1
            val newYear = if (selectedMonth == 0) selectedYear - 1 else selectedYear
            onMonthYearChanged(newMonth, newYear)
        }) {
            Icon(Icons.Default.KeyboardArrowLeft, "Previous Month")
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = months[selectedMonth],
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = selectedYear.toString(),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        IconButton(onClick = {
            val newMonth = if (selectedMonth == 11) 0 else selectedMonth + 1
            val newYear = if (selectedMonth == 11) selectedYear + 1 else selectedYear
            onMonthYearChanged(newMonth, newYear)
        }) {
            Icon(Icons.Default.KeyboardArrowRight, "Next Month")
        }
    }
}

@Composable
private fun GlucoseReadingHistory(
    readings: List<GlucoseReading>,
    userProfile: com.ensias.glucosphere.data.database.entity.UserProfile?
) {
    if (readings.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.List,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "No glucose readings recorded yet",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 16.sp
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(readings) { reading ->
                GlucoseReadingCard(
                    reading = reading,
                    userProfile = userProfile
                )
            }
        }
    }
}

@Composable
private fun GlucoseReadingCard(
    reading: GlucoseReading,
    userProfile: com.ensias.glucosphere.data.database.entity.UserProfile?
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "${reading.glucoseLevel} mg/dL",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Badge(
                        reading = reading,
                        userProfile = userProfile
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                        .format(reading.timestamp),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Type: ${reading.readingType.name.replace("_", " ")}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (reading.notes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Notes: ${reading.notes}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

@Composable
private fun Badge(
    reading: GlucoseReading,
    userProfile: com.ensias.glucosphere.data.database.entity.UserProfile?
) {
    if (userProfile != null) {
        val isInRange = reading.glucoseLevel in userProfile.targetGlucoseMin..userProfile.targetGlucoseMax
        val backgroundColor = when {
            isInRange -> MaterialTheme.colorScheme.onPrimary
            reading.glucoseLevel > userProfile.targetGlucoseMax -> MaterialTheme.colorScheme.inversePrimary
            else -> MaterialTheme.colorScheme.tertiaryContainer
        }
        val textColor = when {
            isInRange -> MaterialTheme.colorScheme.onPrimaryContainer
            reading.glucoseLevel > userProfile.targetGlucoseMax -> MaterialTheme.colorScheme.onErrorContainer
            else -> MaterialTheme.colorScheme.onTertiaryContainer
        }
        val label = when {
            isInRange -> "Normal"
            reading.glucoseLevel > userProfile.targetGlucoseMax -> "High"
            else -> "Low"
        }

        Surface(
            color = backgroundColor,
            shape = MaterialTheme.shapes.small
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = textColor,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun MedicationLogsHistory(
    medicationLogs: List<MedicationLog>,
    medications: Map<Long, Medication>
) {
    if (medicationLogs.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.List,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "No medication logs recorded yet",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 16.sp
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(medicationLogs) { log ->
                MedicationLogCard(
                    medicationLog = log,
                    medication = medications[log.medicationId]
                )
            }
        }
    }
}

@Composable
private fun MedicationLogCard(
    medicationLog: MedicationLog,
    medication: Medication?
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = medication?.name ?: "Unknown Medication",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (medication != null) {
                        Text(
                            text = medication.dosage,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Surface(
                    color = if (medicationLog.taken)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.errorContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = if (medicationLog.taken) "✓ Taken" else "✗ Missed",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (medicationLog.taken)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Divider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Scheduled",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                            .format(medicationLog.scheduledTime),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Column {
                    Text(
                        text = "Actual Time",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                            .format(medicationLog.actualTime),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (medicationLog.notes.isNotEmpty()) {
                Divider()
                Text(
                    text = "Notes: ${medicationLog.notes}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}
