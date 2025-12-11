package com.ensias.glucosphere.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String,
    val age: Int,
    val healthStatus: String? = null, // "healthy", "multiple_conditions", "frail"
    val insulinUser: Boolean = false,
    val targetGlucoseMin: Int,
    val targetGlucoseMax: Int,
    val fastingMin: Int? = null,
    val fastingMax: Int? = null,
    val preMealMin: Int? = null,
    val preMealMax: Int? = null,
    val postMealMax: Int? = null,
    val randomMax: Int? = null,
    val isActive: Boolean = false
)
