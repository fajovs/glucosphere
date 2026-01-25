package com.ensias.glucosphere.ui.screens.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ensias.glucosphere.data.database.entity.GlucoseReading
import com.ensias.glucosphere.data.database.entity.ReadingType
import com.ensias.glucosphere.data.database.entity.UserProfile
import com.ensias.glucosphere.data.repository.GlucoseReadingRepository
import com.ensias.glucosphere.data.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class InsightItem(
    val text: String,
    val citationUrl: String? = null
)

data class AnalysisUiState(
    val userProfile: UserProfile? = null,
    val lastReading: GlucoseReading? = null,
    val lastReadingStatus: ReadingStatus = ReadingStatus.NORMAL,
    val totalReadings: Int = 0,
    val averageGlucose: Int = 0,
    val highestGlucose: Int = 0,
    val lowestGlucose: Int = 0,
    val inTargetCount: Int = 0,
    val belowTargetCount: Int = 0,
    val aboveTargetCount: Int = 0,
    val inTargetPercentage: Int = 0,
    val belowTargetPercentage: Int = 0,
    val aboveTargetPercentage: Int = 0,
    val insights: List<InsightItem> = emptyList(),
    val chartData: List<Pair<Long, Int>> = emptyList(),
    val isLoading: Boolean = true
)

enum class ReadingStatus {
    LOW, NORMAL, HIGH
}

@HiltViewModel
class AnalysisViewModel @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
    private val glucoseReadingRepository: GlucoseReadingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalysisUiState())
    val uiState: StateFlow<AnalysisUiState> = _uiState.asStateFlow()

    init {
        loadAnalysisData()
    }

    private fun loadAnalysisData() {
        viewModelScope.launch {
            val thirtyDaysAgo = Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30))

            combine(
                userProfileRepository.getUserProfile(),
                glucoseReadingRepository.getReadingsFromDate(thirtyDaysAgo)
            ) { profile, readings ->
                if (profile != null) {
                    calculateAnalysis(profile, readings)
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            }.collect()
        }
    }

    private fun calculateAnalysis(profile: UserProfile, readings: List<GlucoseReading>) {
        val sortedReadings = readings.sortedBy { it.timestamp }
        val lastReading = sortedReadings.lastOrNull()

        val totalReadings = readings.size
        val averageGlucose = if (readings.isNotEmpty()) {
            readings.map { it.glucoseLevel }.average().toInt()
        } else 0

        val highestGlucose = readings.maxOfOrNull { it.glucoseLevel } ?: 0
        val lowestGlucose = readings.minOfOrNull { it.glucoseLevel } ?: 0

        val inTargetCount = readings.count {
            it.glucoseLevel in profile.targetGlucoseMin..profile.targetGlucoseMax
        }
        val belowTargetCount = readings.count { it.glucoseLevel < profile.targetGlucoseMin }
        val aboveTargetCount = readings.count { it.glucoseLevel > profile.targetGlucoseMax }

        val inTargetPercentage = if (totalReadings > 0) (inTargetCount * 100) / totalReadings else 0
        val belowTargetPercentage = if (totalReadings > 0) (belowTargetCount * 100) / totalReadings else 0
        val aboveTargetPercentage = if (totalReadings > 0) (aboveTargetCount * 100) / totalReadings else 0

        // Determine status of last reading
        val lastReadingStatus = if (lastReading != null) {
            determineReadingStatus(lastReading, profile)
        } else {
            ReadingStatus.NORMAL
        }

        val insights = generateInsights(
            profile,
            lastReading,
            lastReadingStatus,
            totalReadings,
            inTargetPercentage,
            belowTargetPercentage,
            aboveTargetPercentage,
            averageGlucose,
            readings
        )

        // Prepare chart data (sorted by timestamp)
        val chartData = readings
            .sortedBy { it.timestamp }
            .map { reading ->
                Pair(reading.timestamp.time, reading.glucoseLevel)
            }

        _uiState.value = AnalysisUiState(
            userProfile = profile,
            lastReading = lastReading,
            lastReadingStatus = lastReadingStatus,
            totalReadings = totalReadings,
            averageGlucose = averageGlucose,
            highestGlucose = highestGlucose,
            lowestGlucose = lowestGlucose,
            inTargetCount = inTargetCount,
            belowTargetCount = belowTargetCount,
            aboveTargetCount = aboveTargetCount,
            inTargetPercentage = inTargetPercentage,
            belowTargetPercentage = belowTargetPercentage,
            aboveTargetPercentage = aboveTargetPercentage,
            insights = insights,
            chartData = chartData,
            isLoading = false
        )
    }

    private fun determineReadingStatus(reading: GlucoseReading, profile: UserProfile): ReadingStatus {
        val glucose = reading.glucoseLevel

        return when (reading.readingType) {
            ReadingType.FASTING -> {
                val min = profile.fastingMin ?: profile.targetGlucoseMin
                val max = profile.fastingMax ?: profile.targetGlucoseMax
                when {
                    glucose < min -> ReadingStatus.LOW
                    glucose > max -> ReadingStatus.HIGH
                    else -> ReadingStatus.NORMAL
                }
            }
            ReadingType.BEFORE_MEAL -> {
                val min = profile.preMealMin ?: profile.targetGlucoseMin
                val max = profile.preMealMax ?: profile.targetGlucoseMax
                when {
                    glucose < min -> ReadingStatus.LOW
                    glucose > max -> ReadingStatus.HIGH
                    else -> ReadingStatus.NORMAL
                }
            }
            ReadingType.AFTER_MEAL -> {
                val max = profile.postMealMax ?: 180
                when {
                    glucose < profile.targetGlucoseMin -> ReadingStatus.LOW
                    glucose > max -> ReadingStatus.HIGH
                    else -> ReadingStatus.NORMAL
                }
            }
            ReadingType.RANDOM -> {
                val max = profile.randomMax ?: 200
                when {
                    glucose < profile.targetGlucoseMin -> ReadingStatus.LOW
                    glucose > max -> ReadingStatus.HIGH
                    else -> ReadingStatus.NORMAL
                }
            }
        }
    }

    private fun generateInsights(
        profile: UserProfile,
        lastReading: GlucoseReading?,
        lastReadingStatus: ReadingStatus,
        totalReadings: Int,
        inTargetPercentage: Int,
        belowTargetPercentage: Int,
        aboveTargetPercentage: Int,
        averageGlucose: Int,
        readings: List<GlucoseReading>
    ): List<InsightItem> {
        val insights = mutableListOf<InsightItem>()

        if (lastReading == null) {
            return listOf(InsightItem("No glucose readings yet. Log your first reading to get started!"))
        }

        val readingTypeInsights = generateReadingTypeInsights(lastReading, profile)
        insights.addAll(readingTypeInsights)

        // Time since last reading
        val timeSinceReading = System.currentTimeMillis() - lastReading.timestamp.time
        val hoursAgo = TimeUnit.MILLISECONDS.toHours(timeSinceReading)
        val minutesAgo = TimeUnit.MILLISECONDS.toMinutes(timeSinceReading)

        when {
            hoursAgo >= 24 -> insights.add(InsightItem("It's been ${hoursAgo / 24} days since your last reading. Regular monitoring is important!"))
            hoursAgo > 0 -> insights.add(InsightItem("Last reading was ${hoursAgo} hours ago."))
            minutesAgo > 0 -> insights.add(InsightItem("Last reading was ${minutesAgo} minutes ago."))
        }

        // Overall trend
        if (totalReadings >= 10) {
            val recentReadings = readings.sortedByDescending { it.timestamp }.take(5)
            val trend = when {
                recentReadings.zipWithNext().all { (a, b) -> a.glucoseLevel >= b.glucoseLevel } ->
                    InsightItem("📈 Your recent glucose levels are trending upward.")
                recentReadings.zipWithNext().all { (a, b) -> a.glucoseLevel <= b.glucoseLevel } ->
                    InsightItem("📉 Your recent glucose levels are trending downward.")
                else ->
                    InsightItem("Your recent glucose levels are stable with fluctuations.")
            }
            insights.add(trend)
        }

        // Control quality feedback
        if (totalReadings >= 30) {
            when {
                inTargetPercentage >= 80 -> insights.add(InsightItem("✓ Excellent glucose control! Keep up the great work."))
                inTargetPercentage >= 60 -> insights.add(InsightItem("Good glucose control. Continue monitoring and maintaining your routine."))
                else -> insights.add(InsightItem("Your glucose control needs attention. Consider consulting with your healthcare provider."))
            }
        }

        return insights
    }

    private fun generateReadingTypeInsights(reading: GlucoseReading, profile: UserProfile): List<InsightItem> {
        val insights = mutableListOf<InsightItem>()
        val glucose = reading.glucoseLevel

        val minTarget = profile.targetGlucoseMin
        val maxTarget = profile.targetGlucoseMax

        when (reading.readingType) {
            ReadingType.FASTING -> {
                // For fasting: typically allow a slightly wider range, but base on user's target
                // Use 70 as lower bound for safety, but reference user's settings
                val fastingMin = 70
                val fastingMax = maxTarget

                when {
                    glucose < fastingMin -> {
                        insights.add(InsightItem("⚠️ WARNING: Your fasting glucose (${glucose} mg/dL) is critically low.", "https://diabetes.org/living-with-diabetes/hypoglycemia-low-blood-glucose"))
                        insights.add(InsightItem("Advice: Eat a small carbohydrate snack before bed or discuss your medication with your doctor.", "https://www.mayoclinic.org/diseases-conditions/diabetes/expert-answers/diabetes/faq-20058372"))
                        insights.add(InsightItem("⚠️ Low fasting glucose may lead to dizziness, fatigue, or even fainting if not addressed.", "https://www.mayoclinic.org/diseases-conditions/hyperglycemia/symptoms-causes/syc-20373631"))
                    }
                    glucose <= fastingMax -> {
                        insights.add(InsightItem("✓ Your fasting glucose (${glucose} mg/dL) is within acceptable range. Great job maintaining stable overnight levels!", "https://www.healthline.com/health/diabetes/normal-blood-sugar-level"))
                        insights.add(InsightItem("Advice: Keep following your routine and stay consistent with bedtime habits and medication.", "https://www.mayoclinic.org/diseases-conditions/diabetes/in-depth/diabetes-management/art-20047963"))
                        insights.add(InsightItem("Continue monitoring — maintaining stability overnight helps prevent long-term complications.", "https://www.bannerhealth.com/services/diabetes/complications/monitoring-importance"))
                    }
                    glucose > fastingMax -> {
                        insights.add(InsightItem("⚠️ ALERT: Your fasting glucose (${glucose} mg/dL) is higher than your target of ${fastingMax} mg/dL.", "https://www.mayoclinic.org/diseases-conditions/hyperglycemia/symptoms-causes/syc-20373631"))
                        insights.add(InsightItem("Advice: Avoid late-night snacks high in carbs and review your insulin or medication timing.", "https://medlineplus.gov/ency/patientinstructions/000322.htm"))
                        insights.add(InsightItem("⚠️ Repeated high fasting glucose can increase risk of chronic hyperglycemia and early morning fatigue.", "https://diabetes.org/living-with-diabetes/treatment-care/hyperglycemia"))
                    }
                }
            }
            ReadingType.BEFORE_MEAL -> {
                // Before meal: use user's target range
                when {
                    glucose < minTarget -> {
                        insights.add(InsightItem("⚠️ WARNING: Your pre-meal glucose (${glucose} mg/dL) is below your target of ${minTarget} mg/dL.", "https://diabetes.org/living-with-diabetes/hypoglycemia-low-blood-glucose"))
                        insights.add(InsightItem("Advice: Eat a small snack or fruit before your meal to prevent further drop.", "https://www.mayoclinic.org/diseases-conditions/diabetes/expert-answers/diabetes/faq-20058372"))
                        insights.add(InsightItem("⚠️ Skipping meals or overdosing insulin could cause severe hypoglycemia, which may lead to confusion or shakiness.", "https://www.mayoclinic.org/diseases-conditions/hyperglycemia/symptoms-causes/syc-20373631"))
                    }
                    glucose <= maxTarget -> {
                        insights.add(InsightItem("✓ Your pre-meal glucose (${glucose} mg/dL) is within your target range (${minTarget}-${maxTarget} mg/dL).", "https://www.healthline.com/health/diabetes/normal-blood-sugar-level"))
                        insights.add(InsightItem("Advice: Good job! Keep meals consistent and balanced.", "https://www.mayoclinic.org/diseases-conditions/diabetes/in-depth/diabetes-management/art-20047963"))
                        insights.add(InsightItem("Maintaining steady pre-meal readings helps avoid large post-meal spikes.", "https://www.bannerhealth.com/services/diabetes/complications/monitoring-importance"))
                    }
                    glucose > maxTarget -> {
                        insights.add(InsightItem("⚠️ ALERT: Your pre-meal glucose (${glucose} mg/dL) is above your target of ${maxTarget} mg/dL.", "https://www.mayoclinic.org/diseases-conditions/hyperglycemia/symptoms-causes/syc-20373631"))
                        insights.add(InsightItem("Advice: Stay hydrated and recheck after your next meal; limit refined carbohydrates.", "https://www.example.com/advice-high-pre-meal-glucose"))
                        insights.add(InsightItem("⚠️ Frequent high pre-meal readings can raise your HbA1c and long-term risk for complications.", "https://diabetes.org/living-with-diabetes/treatment-care/hyperglycemia"))
                    }
                }
            }
            ReadingType.AFTER_MEAL -> {
                // After meal: typically allow higher range (up to 180), but reference user's target as baseline
                val afterMealMax = maxOf(180, maxTarget)

                when {
                    glucose < minTarget -> {
                        insights.add(InsightItem("⚠️ WARNING: Your after-meal glucose (${glucose} mg/dL) is unusually low.", "https://diabetes.org/living-with-diabetes/hypoglycemia-low-blood-glucose"))
                        insights.add(InsightItem("Advice: Consider adjusting medication or increasing carbs at meals.", "https://www.mayoclinic.org/diseases-conditions/diabetes/expert-answers/diabetes/faq-20058372"))
                        insights.add(InsightItem("⚠️ Post-meal lows may lead to sudden fatigue, sweating, or even fainting.", "https://www.mayoclinic.org/diseases-conditions/hyperglycemia/symptoms-causes/syc-20373631"))
                    }
                    glucose <= afterMealMax -> {
                        insights.add(InsightItem("✓ Your after-meal glucose (${glucose} mg/dL) is within acceptable range. Great job keeping post-meal sugar under control!", "https://www.healthline.com/health/diabetes/normal-blood-sugar-level"))
                        insights.add(InsightItem("Advice: Keep balancing your meals with protein, fiber, and activity.", "https://www.mayoclinic.org/diseases-conditions/diabetes/in-depth/diabetes-management/art-20047963"))
                        insights.add(InsightItem("Stable post-meal glucose protects your heart and blood vessels long-term.", "https://www.bannerhealth.com/services/diabetes/complications/monitoring-importance"))
                    }
                    glucose > afterMealMax -> {
                        insights.add(InsightItem("⚠️ ALERT: Your after-meal glucose (${glucose} mg/dL) exceeds the recommended limit of ${afterMealMax} mg/dL.", "https://www.mayoclinic.org/diseases-conditions/hyperglycemia/symptoms-causes/syc-20373631"))
                        insights.add(InsightItem("Advice: Try reducing portion size, increasing fiber, or taking a short walk after eating.", "https://medlineplus.gov/ency/patientinstructions/000322.htm"))
                        insights.add(InsightItem("⚠️ Frequent high post-meal spikes can cause tiredness and increase risk of neuropathy and heart issues.", "https://diabetes.org/living-with-diabetes/treatment-care/hyperglycemia"))
                    }
                }
            }
            ReadingType.RANDOM -> {
                // Random reading: use user's target range as baseline
                when {
                    glucose < minTarget -> {
                        insights.add(InsightItem("⚠️ WARNING: Your random glucose (${glucose} mg/dL) is below your target minimum of ${minTarget} mg/dL.", "https://diabetes.org/living-with-diabetes/hypoglycemia-low-blood-glucose"))
                        insights.add(InsightItem("Advice: Eat a quick snack or drink something sugary if you feel weak.", "https://www.mayoclinic.org/diseases-conditions/diabetes/expert-answers/diabetes/faq-20058372"))
                        insights.add(InsightItem("⚠️ Low sugar episodes can become dangerous if ignored, leading to disorientation or fainting.", "https://www.mayoclinic.org/diseases-conditions/hyperglycemia/symptoms-causes/syc-20373631"))
                    }
                    glucose <= maxTarget -> {
                        insights.add(InsightItem("✓ Your random glucose (${glucose} mg/dL) is within your target range (${minTarget}-${maxTarget} mg/dL).", "https://www.healthline.com/health/diabetes/normal-blood-sugar-level"))
                        insights.add(InsightItem("Advice: Nice work! Keep tracking your meals and activity for stable control.", "https://www.mayoclinic.org/diseases-conditions/diabetes/in-depth/diabetes-management/art-20047963\n"))
                        insights.add(InsightItem("Consistent glucose stability helps prevent fatigue and long-term organ strain.", "https://www.bannerhealth.com/services/diabetes/complications/monitoring-importance"))
                    }
                    glucose > maxTarget -> {
                        insights.add(InsightItem("⚠️ ALERT: Your random glucose (${glucose} mg/dL) is higher than your target maximum of ${maxTarget} mg/dL.", "https://www.mayoclinic.org/diseases-conditions/hyperglycemia/symptoms-causes/syc-20373631"))
                        insights.add(InsightItem("Advice: Drink water, rest, and check again in 2 hours to ensure it's not persistently high.", "https://medlineplus.gov/ency/patientinstructions/000322.htm"))
                        insights.add(InsightItem("⚠️ Persistent random highs could signal poor control or stress-related spikes; consult your healthcare provider if it continues.", "https://diabetes.org/living-with-diabetes/treatment-care/hyperglycemia"))
                    }
                }
            }
        }

        return insights
    }
}
