package com.ensias.glucosphere.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "glucose_readings",
    foreignKeys = [
        ForeignKey(
            entity = UserProfile::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class GlucoseReading(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long, // added userId to associate readings with specific user
    val glucoseLevel: Int,
    val timestamp: Date,
    val notes: String = "",
    val readingType: ReadingType = ReadingType.RANDOM
)

enum class ReadingType {
    FASTING, BEFORE_MEAL, AFTER_MEAL, RANDOM
}
