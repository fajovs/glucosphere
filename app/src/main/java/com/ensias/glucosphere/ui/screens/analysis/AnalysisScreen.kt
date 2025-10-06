package com.ensias.glucosphere.ui.screens.analysis

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
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
                text = "Analysis",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
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
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Summary Stats
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Summary (Last 30 Days)",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                StatItem(
                                    label = "Total Readings",
                                    value = uiState.totalReadings.toString()
                                )
                                StatItem(
                                    label = "Average",
                                    value = "${uiState.averageGlucose} mg/dL"
                                )
                                StatItem(
                                    label = "In Target",
                                    value = "${uiState.inTargetPercentage}%"
                                )
                            }
                        }
                    }
                }

                // Glucose Trend Line Chart
                if (uiState.chartData.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Glucose Trend",
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
                }

                // Target Range Distribution Bar Chart
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Target Range Distribution",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            uiState.userProfile?.let { profile ->
                                Text(
                                    text = "Your target: ${profile.targetGlucoseMin} - ${profile.targetGlucoseMax} mg/dL",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                // Distribution Bar Chart
                                DistributionChart(
                                    belowCount = uiState.belowTargetCount,
                                    inCount = uiState.inTargetCount,
                                    aboveCount = uiState.aboveTargetCount
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                TargetRangeItem(
                                    label = "Below Target",
                                    count = uiState.belowTargetCount,
                                    percentage = uiState.belowTargetPercentage,
                                    color = MaterialTheme.colorScheme.error
                                )

                                TargetRangeItem(
                                    label = "In Target",
                                    count = uiState.inTargetCount,
                                    percentage = uiState.inTargetPercentage,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                TargetRangeItem(
                                    label = "Above Target",
                                    count = uiState.aboveTargetCount,
                                    percentage = uiState.aboveTargetPercentage,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                    }
                }

                // Insights
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Insights",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            if (uiState.insights.isEmpty()) {
                                Text(
                                    text = "Add more readings to see personalized insights!",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                uiState.insights.forEach { insight ->
                                    Text(
                                        text = "• $insight",
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
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

    ProvideChartStyle {
        Chart(
            chart = lineChart(
                targetVerticalAxisPosition = AxisPosition.Vertical.Start
            ),
            model = chartEntryModel,
            startAxis = rememberStartAxis(
                title = "Glucose (mg/dL)"
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
}

@Composable
private fun DistributionChart(
    belowCount: Int,
    inCount: Int,
    aboveCount: Int
) {
    val total = belowCount + inCount + aboveCount
    if (total == 0) return

    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
        ) {
            // Below target
            if (belowCount > 0) {
                Box(
                    modifier = Modifier
                        .weight(belowCount.toFloat() / total)
                        .fillMaxHeight()
                        .padding(end = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        color = colorScheme.error,
                        shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize())
                    }
                }
            }

            // In target
            if (inCount > 0) {
                Box(
                    modifier = Modifier
                        .weight(inCount.toFloat() / total)
                        .fillMaxHeight()
                        .padding(horizontal = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        color = colorScheme.primary,
                        shape = if (belowCount == 0 && aboveCount > 0)
                            RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp)
                        else if (aboveCount == 0 && belowCount > 0)
                            RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp)
                        else if (belowCount == 0 && aboveCount == 0)
                            RoundedCornerShape(8.dp)
                        else
                            RoundedCornerShape(0.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize())
                    }
                }
            }

            // Above target
            if (aboveCount > 0) {
                Box(
                    modifier = Modifier
                        .weight(aboveCount.toFloat() / total)
                        .fillMaxHeight()
                        .padding(start = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        color = colorScheme.tertiary,
                        shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize())
                    }
                }
            }
        }
    }
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
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
            ) {
                Surface(
                    color = color,
                    shape = RoundedCornerShape(2.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize())
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = label)
        }
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$count readings",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "$percentage%",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}
