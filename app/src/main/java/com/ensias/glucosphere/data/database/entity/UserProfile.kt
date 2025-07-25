package com.ensias.glucosphere.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String,
    val age: Int,
    val targetGlucoseMin: Int,
    val targetGlucoseMax: Int,
    val isActive: Boolean = false // Track which user is currently active
)
