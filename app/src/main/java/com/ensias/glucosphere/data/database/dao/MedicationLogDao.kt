package com.ensias.glucosphere.data.database.dao

import androidx.room.*
import com.ensias.glucosphere.data.database.entity.MedicationLog
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface MedicationLogDao {
    @Query("SELECT * FROM medication_logs ORDER BY scheduledTime DESC LIMIT 20")
    fun getRecentMedicationLogs(): Flow<List<MedicationLog>>

    @Query("SELECT * FROM medication_logs WHERE scheduledTime >= :startDate ORDER BY scheduledTime DESC")
    fun getMedicationLogsFromDate(startDate: Date): Flow<List<MedicationLog>>

    @Insert
    suspend fun insertMedicationLog(log: MedicationLog)

    @Update
    suspend fun updateMedicationLog(log: MedicationLog)

    @Query("SELECT * FROM medication_logs WHERE medicationId = :medicationId AND DATE(scheduledTime) = DATE(:date)")
    suspend fun getMedicationLogForDate(medicationId: Long, date: Date): List<MedicationLog>
}
