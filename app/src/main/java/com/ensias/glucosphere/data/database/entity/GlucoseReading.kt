package com.ensias.glucosphere.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "glucose_readings")
data class GlucoseReading(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val glucoseLevel: Int,
    val timestamp: Date,
    val notes: String = "",
    val readingType: ReadingType = ReadingType.RANDOM
)

enum class ReadingType {
    FASTING, BEFORE_MEAL, AFTER_MEAL, RANDOM
}
