package com.ensias.glucosphere.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import java.util.Date

@Entity(
    tableName = "medication_schedules",
    foreignKeys = [
        ForeignKey(
            entity = Medication::class,
            parentColumns = ["id"],
            childColumns = ["medicationId"],
            onDelete = ForeignKey.CASCADE
        )
    ] ,
    indices = [androidx.room.Index(value = ["medicationId"])]
)
data class MedicationSchedule(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val medicationId: Long,
    val timeHour: Int, // 24-hour format
    val timeMinute: Int,
    val isActive: Boolean = true,
    val reminderEnabled: Boolean = true
)
