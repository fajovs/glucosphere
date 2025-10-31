package com.ensias.glucosphere.ui.screens.analysis

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.core.entry.entryOf
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisScreen(
    onNavigateBack: () -> Unit,
    viewModel: AnalysisViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analysis & Insights") },
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
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Last Reading Alert Card
                if (uiState.lastReading != null) {
                    LastReadingAlertCard(
                        lastReading = uiState.lastReading!!,
                        status = uiState.lastReadingStatus,
                        userProfile = uiState.userProfile
                    )
                }

                // Summary Stats
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "30-Day Summary",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                Icons.Default.DateRange,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        Divider()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatItem(
                                label = "Average",
                                value = "${uiState.averageGlucose} mg/dL"
                            )
                            StatItem(
                                label = "Highest",
                                value = "${uiState.highestGlucose} mg/dL"
                            )
                            StatItem(
                                label = "Lowest",
                                value = "${uiState.lowestGlucose} mg/dL"
                            )
                        }
                    }
                }

                // Glucose Trend Line Chart
                if (uiState.chartData.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                "Glucose Trend",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            GlucoseTrendChart(
                                chartData = uiState.chartData,
                                targetMin = uiState.userProfile?.targetGlucoseMin ?: 80,
                                targetMax = uiState.userProfile?.targetGlucoseMax ?: 120
                            )
                        }
                    }
                }

                // Target Range Distribution
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Target Distribution",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        TargetRangeItem(
                            label = "Below Target",
                            count = uiState.belowTargetCount,
                            percentage = uiState.belowTargetPercentage,
                            color = Color(0xFFE53935)
                        )

                        TargetRangeItem(
                            label = "In Target",
                            count = uiState.inTargetCount,
                            percentage = uiState.inTargetPercentage,
                            color = Color(0xFF4CAF50)
                        )

                        TargetRangeItem(
                            label = "Above Target",
                            count = uiState.aboveTargetCount,
                            percentage = uiState.aboveTargetPercentage,
                            color = Color(0xFF9C27B0)
                        )
                    }
                }

                // Insights
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Insights",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Divider()

                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            uiState.insights.forEach { insight ->
                                InsightRow(insight)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun LastReadingAlertCard(
    lastReading: com.ensias.glucosphere.data.database.entity.GlucoseReading,
    status: ReadingStatus,
    userProfile: com.ensias.glucosphere.data.database.entity.UserProfile?
) {
    val backgroundColor = when (status) {
        ReadingStatus.LOW -> Color(0xFFFFEBEE)
        ReadingStatus.HIGH -> Color(0xFFFFF3E0)
        ReadingStatus.NORMAL -> Color(0xFFF1F8E9)
    }

    val borderColor = when (status) {
        ReadingStatus.LOW -> Color(0xFFE53935)
        ReadingStatus.HIGH -> Color(0xFFFFA726)
        ReadingStatus.NORMAL -> Color(0xFF4CAF50)
    }

    val icon = when (status) {
        ReadingStatus.LOW -> Icons.Default.Warning
        ReadingStatus.HIGH -> Icons.Default.AddCircle
        ReadingStatus.NORMAL -> Icons.Default.CheckCircle
    }

    val title = when (status) {
        ReadingStatus.LOW -> "Low Glucose Alert"
        ReadingStatus.HIGH -> "High Glucose Alert"
        ReadingStatus.NORMAL -> "Glucose Normal"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(2.dp, borderColor, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = borderColor,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = borderColor
                )
            }

            Text(
                "Current: ${lastReading.glucoseLevel} mg/dL",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = borderColor
            )

            if (userProfile != null) {
                Text(
                    "Target Range: ${userProfile.targetGlucoseMin} - ${userProfile.targetGlucoseMax} mg/dL",
                    fontSize = 14.sp,
                    color = borderColor.copy(alpha = 0.8f)
                )
            }

            val timestamp = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(lastReading.timestamp)
            Text(
                timestamp,
                fontSize = 12.sp,
                color = borderColor.copy(alpha = 0.6f)
            )

            when (status) {
                ReadingStatus.LOW -> {
                    Divider()
                    Text(
                        "⚠️ Take action: Consume a fast-acting carbohydrate (juice, glucose tablet, candy) and recheck in 15 minutes.",
                        fontSize = 12.sp,
                        color = borderColor,
                        textAlign = TextAlign.Start
                    )
                }
                ReadingStatus.HIGH -> {
                    Divider()
                    Text(
                        "⚠️ Stay hydrated and monitor closely. Contact your healthcare provider if levels remain elevated.",
                        fontSize = 12.sp,
                        color = borderColor,
                        textAlign = TextAlign.Start
                    )
                }
                ReadingStatus.NORMAL -> {
                    Divider()
                    Text(
                        "✓ Keep up your current routine!",
                        fontSize = 12.sp,
                        color = borderColor,
                        textAlign = TextAlign.Start
                    )
                }
            }
        }
    }
}

@Composable
private fun GlucoseTrendChart(
    chartData: List<Pair<Long, Int>>,
    targetMin: Int,
    targetMax: Int
) {
    val entries = chartData.mapIndexed { index, (_, glucose) ->
        entryOf(index.toFloat(), glucose.toFloat())
    }

    val chartEntryModel = entryModelOf(entries)

    Chart(
        chart = lineChart(
            targetVerticalAxisPosition = AxisPosition.Vertical.Start
        ),
        model = chartEntryModel,
        startAxis = rememberStartAxis(
            title = "mg/dL"
        ),
        bottomAxis = rememberBottomAxis(
            title = "Date",
            valueFormatter = AxisValueFormatter { value, _ ->
                val index = value.toInt()
                if (index in chartData.indices) {
                    val timestamp = chartData[index].first
                    SimpleDateFormat("MM/dd", Locale.getDefault()).format(Date(timestamp))
                } else ""
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    )
}

@Composable
private fun StatItem(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TargetRangeItem(
    label: String,
    count: Int,
    percentage: Int,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(color, RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = label)
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "$count",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "$percentage%",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun InsightRow(insight: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        when {
            insight.contains("⚠️") -> {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFFFA726),
                    modifier = Modifier.size(20.dp)
                )
            }
            insight.contains("✓") -> {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(20.dp)
                )
            }
            insight.contains("📈") -> {
                Icon(
                    Icons.Default.KeyboardArrowUp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(20.dp)
                )
            }
            insight.contains("📉") -> {
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(20.dp)
                )
            }
            else -> {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Text(
            text = insight,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun Modifier.border(
    width: androidx.compose.ui.unit.Dp,
    color: Color,
    shape: androidx.compose.foundation.shape.RoundedCornerShape
) = this.then(
    Modifier.background(color, shape).padding(width)
)
