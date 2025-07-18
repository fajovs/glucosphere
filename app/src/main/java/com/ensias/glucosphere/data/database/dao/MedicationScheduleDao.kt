package com.ensias.glucosphere.data.database.dao

import androidx.room.*
import com.ensias.glucosphere.data.database.entity.MedicationSchedule
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationScheduleDao {
    @Query("SELECT * FROM medication_schedules WHERE medicationId = :medicationId AND isActive = 1")
    fun getSchedulesForMedication(medicationId: Long): Flow<List<MedicationSchedule>>

    @Query("SELECT * FROM medication_schedules WHERE isActive = 1 AND reminderEnabled = 1")
    fun getActiveSchedulesWithReminders(): Flow<List<MedicationSchedule>>

    @Insert
    suspend fun insertSchedule(schedule: MedicationSchedule): Long

    @Update
    suspend fun updateSchedule(schedule: MedicationSchedule)

    @Delete
    suspend fun deleteSchedule(schedule: MedicationSchedule)

    @Query("DELETE FROM medication_schedules WHERE medicationId = :medicationId")
    suspend fun deleteSchedulesForMedication(medicationId: Long)
}
