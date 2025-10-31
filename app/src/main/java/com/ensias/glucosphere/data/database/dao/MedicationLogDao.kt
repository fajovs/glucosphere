package com.ensias.glucosphere.data.database.dao

import androidx.room.*
import com.ensias.glucosphere.data.database.entity.MedicationLog
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface MedicationLogDao {
    @Query("SELECT ml.* FROM medication_logs ml INNER JOIN medications m ON ml.medicationId = m.id WHERE m.userId = :userId ORDER BY ml.scheduledTime DESC LIMIT 20")
    fun getRecentMedicationLogs(userId: Long): Flow<List<MedicationLog>>

    @Query("SELECT ml.* FROM medication_logs ml INNER JOIN medications m ON ml.medicationId = m.id WHERE m.userId = :userId AND ml.scheduledTime >= :startDate ORDER BY ml.scheduledTime DESC")
    fun getMedicationLogsFromDate(userId: Long, startDate: Date): Flow<List<MedicationLog>>

    @Insert
    suspend fun insertMedicationLog(log: MedicationLog)

    @Update
    suspend fun updateMedicationLog(log: MedicationLog)

    @Query("SELECT * FROM medication_logs WHERE medicationId = :medicationId AND DATE(scheduledTime) = DATE(:date)")
    suspend fun getMedicationLogForDate(medicationId: Long, date: Date): List<MedicationLog>
}
