package com.ensias.glucosphere.data.database.dao

import androidx.room.*
import com.ensias.glucosphere.data.database.entity.Medication
import com.ensias.glucosphere.data.database.entity.MedicationWithSchedules
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationDao {
    @Query("SELECT * FROM medications WHERE isActive = 1 ORDER BY name")
    fun getActiveMedications(): Flow<List<Medication>>

    @Query("SELECT * FROM medications ORDER BY name")
    fun getAllMedications(): Flow<List<Medication>>

    @Transaction
    @Query("SELECT * FROM medications WHERE isActive = 1 ORDER BY name")
    fun getMedicationsWithSchedules(): Flow<List<MedicationWithSchedules>>

    @Insert
    suspend fun insertMedication(medication: Medication): Long

    @Update
    suspend fun updateMedication(medication: Medication)

    @Delete
    suspend fun deleteMedication(medication: Medication)

    @Query("SELECT * FROM medications WHERE id = :id")
    suspend fun getMedicationById(id: Long): Medication?
}
