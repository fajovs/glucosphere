package com.ensias.glucosphere.data.repository

import com.ensias.glucosphere.data.database.dao.MedicationDao
import com.ensias.glucosphere.data.database.dao.MedicationScheduleDao
import com.ensias.glucosphere.data.database.dao.MedicationLogDao
import com.ensias.glucosphere.data.database.entity.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MedicationRepository @Inject constructor(
    private val medicationDao: MedicationDao,
    private val medicationScheduleDao: MedicationScheduleDao,
    private val medicationLogDao: MedicationLogDao,
    private val userProfileRepository: UserProfileRepository
) {
    fun getActiveMedications(): Flow<List<Medication>> =
        userProfileRepository.getUserProfile().flatMapLatest { profile ->
            if (profile != null) {
                medicationDao.getActiveMedications(profile.id)
            } else {
                flowOf(emptyList())
            }
        }

    fun getActiveMedications(userId: Long): Flow<List<Medication>> =
        medicationDao.getActiveMedications(userId)

    fun getMedicationsWithSchedules(): Flow<List<MedicationWithSchedules>> =
        userProfileRepository.getUserProfile().flatMapLatest { profile ->
            if (profile != null) {
                medicationDao.getMedicationsWithSchedules(profile.id)
            } else {
                flowOf(emptyList())
            }
        }

    suspend fun insertMedication(medication: Medication): Long =
        medicationDao.insertMedication(medication)

    suspend fun updateMedication(medication: Medication) =
        medicationDao.updateMedication(medication)

    suspend fun deleteMedication(medication: Medication) =
        medicationDao.deleteMedication(medication)

    suspend fun getMedicationById(id: Long): Medication? =
        medicationDao.getMedicationById(id)

    // Schedule methods
    fun getSchedulesForMedication(medicationId: Long): Flow<List<MedicationSchedule>> =
        medicationScheduleDao.getSchedulesForMedication(medicationId)

    fun getActiveSchedulesWithReminders(): Flow<List<MedicationSchedule>> =
        medicationScheduleDao.getActiveSchedulesWithReminders()

    suspend fun insertSchedule(schedule: MedicationSchedule): Long =
        medicationScheduleDao.insertSchedule(schedule)

    suspend fun updateSchedule(schedule: MedicationSchedule) =
        medicationScheduleDao.updateSchedule(schedule)

    suspend fun deleteSchedule(schedule: MedicationSchedule) =
        medicationScheduleDao.deleteSchedule(schedule)

    suspend fun deleteSchedulesForMedication(medicationId: Long) =
        medicationScheduleDao.deleteSchedulesForMedication(medicationId)

    // Log methods
    fun getRecentMedicationLogs(): Flow<List<MedicationLog>> =
        userProfileRepository.getUserProfile().flatMapLatest { profile ->
            if (profile != null) {
                medicationLogDao.getRecentMedicationLogs(profile.id)
            } else {
                flowOf(emptyList())
            }
        }

    fun getMedicationLogsForUser(userId: Long): Flow<List<MedicationLog>> =
        medicationLogDao.getRecentMedicationLogs(userId)

    suspend fun insertMedicationLog(log: MedicationLog) =
        medicationLogDao.insertMedicationLog(log)

    suspend fun updateMedicationLog(log: MedicationLog) =
        medicationLogDao.updateMedicationLog(log)

    suspend fun getMedicationLogForDate(medicationId: Long, date: Date): List<MedicationLog> =
        medicationLogDao.getMedicationLogForDate(medicationId, date)
}
