package com.ensias.glucosphere.data.repository

import com.ensias.glucosphere.data.database.dao.GlucoseReadingDao
import com.ensias.glucosphere.data.database.entity.GlucoseReading
import com.ensias.glucosphere.data.repository.UserProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.switchMap
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GlucoseReadingRepository @Inject constructor(
    private val glucoseReadingDao: GlucoseReadingDao,
    private val userProfileRepository: UserProfileRepository
) {
    fun getAllReadings(): Flow<List<GlucoseReading>> =
        userProfileRepository.getUserProfile().flatMapLatest { profile ->
            if (profile != null) {
                glucoseReadingDao.getAllReadings(profile.id)
            } else {
                kotlinx.coroutines.flow.flowOf(emptyList())
            }
        }

    fun getReadingsFromDate(startDate: Date): Flow<List<GlucoseReading>> =
        userProfileRepository.getUserProfile().flatMapLatest { profile ->
            if (profile != null) {
                glucoseReadingDao.getReadingsFromDate(profile.id, startDate)
            } else {
                kotlinx.coroutines.flow.flowOf(emptyList())
            }
        }

    suspend fun insertReading(reading: GlucoseReading) {
        glucoseReadingDao.insertReading(reading)
    }

    suspend fun deleteReading(reading: GlucoseReading) {
        glucoseReadingDao.deleteReading(reading)
    }

    fun getRecentReadings(): Flow<List<GlucoseReading>> =
        userProfileRepository.getUserProfile().flatMapLatest { profile ->
            if (profile != null) {
                glucoseReadingDao.getRecentReadings(profile.id)
            } else {
                kotlinx.coroutines.flow.flowOf(emptyList())
            }
        }
}
