package com.ensias.glucosphere.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val username: String,
    val age: Int,
    val targetGlucoseMin: Int,
    val targetGlucoseMax: Int
)
