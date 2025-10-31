package com.ensias.glucosphere.data.database.dao

import androidx.room.*
import com.ensias.glucosphere.data.database.entity.GlucoseReading
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface GlucoseReadingDao {
    @Query("SELECT * FROM glucose_readings WHERE userId = :userId ORDER BY timestamp DESC")
    fun getAllReadings(userId: Long): Flow<List<GlucoseReading>>

    @Query("SELECT * FROM glucose_readings WHERE userId = :userId AND timestamp >= :startDate ORDER BY timestamp DESC")
    fun getReadingsFromDate(userId: Long, startDate: Date): Flow<List<GlucoseReading>>

    @Insert
    suspend fun insertReading(reading: GlucoseReading)

    @Delete
    suspend fun deleteReading(reading: GlucoseReading)

    @Query("SELECT * FROM glucose_readings WHERE userId = :userId ORDER BY timestamp DESC LIMIT 10")
    fun getRecentReadings(userId: Long): Flow<List<GlucoseReading>>
}
