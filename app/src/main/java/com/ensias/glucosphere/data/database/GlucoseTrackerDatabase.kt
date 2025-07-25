package com.ensias.glucosphere.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.ensias.glucosphere.data.database.dao.GlucoseReadingDao
import com.ensias.glucosphere.data.database.dao.UserProfileDao
import com.ensias.glucosphere.data.database.entity.GlucoseReading
import com.ensias.glucosphere.data.database.entity.UserProfile
import com.ensias.glucosphere.data.database.converter.DateConverter
import com.ensias.glucosphere.data.database.entity.Medication
import com.ensias.glucosphere.data.database.entity.MedicationSchedule
import com.ensias.glucosphere.data.database.entity.MedicationLog
import com.ensias.glucosphere.data.database.dao.MedicationDao
import com.ensias.glucosphere.data.database.dao.MedicationScheduleDao
import com.ensias.glucosphere.data.database.dao.MedicationLogDao

@Database(
    entities = [
        UserProfile::class,
        GlucoseReading::class,
        Medication::class,
        MedicationSchedule::class,
        MedicationLog::class
    ],
    version = 3, // Increment version for schema change
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class GlucoseTrackerDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun glucoseReadingDao(): GlucoseReadingDao
    abstract fun medicationDao(): MedicationDao
    abstract fun medicationScheduleDao(): MedicationScheduleDao
    abstract fun medicationLogDao(): MedicationLogDao

    companion object {
        @Volatile
        private var INSTANCE: GlucoseTrackerDatabase? = null

        fun getDatabase(context: Context): GlucoseTrackerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GlucoseTrackerDatabase::class.java,
                    "glucose_tracker_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
