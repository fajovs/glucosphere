package com.ensias.glucosphere.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "medications",
    foreignKeys = [
        ForeignKey(
            entity = UserProfile::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Medication(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long, // added userId to associate medications with specific user
    val name: String,
    val dosage: String,
    val instructions: String = "",
    val isActive: Boolean = true
)
