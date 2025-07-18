package com.ensias.glucosphere.data.repository

import com.ensias.glucosphere.data.database.dao.MedicationDao
import com.ensias.glucosphere.data.database.dao.MedicationScheduleDao
import com.ensias.glucosphere.data.database.dao.MedicationLogDao
import com.ensias.glucosphere.data.database.entity.*
import kotlinx.coroutines.flow.Flow
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MedicationRepository @Inject constructor(
    private val medicationDao: MedicationDao,
    private val medicationScheduleDao: MedicationScheduleDao,
    private val medicationLogDao: MedicationLogDao
) {
    fun getActiveMedications(): Flow<List<Medication>> = medicationDao.getActiveMedications()

    fun getMedicationsWithSchedules(): Flow<List<MedicationWithSchedules>> =
        medicationDao.getMedicationsWithSchedules()

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
        medicationLogDao.getRecentMedicationLogs()

    suspend fun insertMedicationLog(log: MedicationLog) =
        medicationLogDao.insertMedicationLog(log)

    suspend fun getMedicationLogForDate(medicationId: Long, date: Date): List<MedicationLog> =
        medicationLogDao.getMedicationLogForDate(medicationId, date)
}
