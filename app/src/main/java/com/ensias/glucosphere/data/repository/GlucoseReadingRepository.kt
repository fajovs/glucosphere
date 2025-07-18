package com.ensias.glucosphere.data.repository

import com.ensias.glucosphere.data.database.dao.GlucoseReadingDao
import com.ensias.glucosphere.data.database.entity.GlucoseReading
import kotlinx.coroutines.flow.Flow
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GlucoseReadingRepository @Inject constructor(
    private val glucoseReadingDao: GlucoseReadingDao
) {
    fun getAllReadings(): Flow<List<GlucoseReading>> = glucoseReadingDao.getAllReadings()

    fun getReadingsFromDate(startDate: Date): Flow<List<GlucoseReading>> =
        glucoseReadingDao.getReadingsFromDate(startDate)

    suspend fun insertReading(reading: GlucoseReading) {
        glucoseReadingDao.insertReading(reading)
    }

    suspend fun deleteReading(reading: GlucoseReading) {
        glucoseReadingDao.deleteReading(reading)
    }

    fun getRecentReadings(): Flow<List<GlucoseReading>> = glucoseReadingDao.getRecentReadings()
}
