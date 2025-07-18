package com.ensias.glucosphere.di

import android.content.Context
import androidx.room.Room
import com.ensias.glucosphere.data.database.GlucoseTrackerDatabase
import com.ensias.glucosphere.data.database.dao.GlucoseReadingDao
import com.ensias.glucosphere.data.database.dao.UserProfileDao
import com.ensias.glucosphere.data.database.dao.MedicationDao
import com.ensias.glucosphere.data.database.dao.MedicationScheduleDao
import com.ensias.glucosphere.data.database.dao.MedicationLogDao
import com.ensias.glucosphere.notification.MedicationReminderManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideGlucoseTrackerDatabase(@ApplicationContext context: Context): GlucoseTrackerDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            GlucoseTrackerDatabase::class.java,
            "glucose_tracker_database"
        ).fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideUserProfileDao(database: GlucoseTrackerDatabase): UserProfileDao {
        return database.userProfileDao()
    }

    @Provides
    fun provideGlucoseReadingDao(database: GlucoseTrackerDatabase): GlucoseReadingDao {
        return database.glucoseReadingDao()
    }

    @Provides
    fun provideMedicationDao(database: GlucoseTrackerDatabase): MedicationDao {
        return database.medicationDao()
    }

    @Provides
    fun provideMedicationScheduleDao(database: GlucoseTrackerDatabase): MedicationScheduleDao {
        return database.medicationScheduleDao()
    }

    @Provides
    fun provideMedicationLogDao(database: GlucoseTrackerDatabase): MedicationLogDao {
        return database.medicationLogDao()
    }

    @Provides
    @Singleton
    fun provideMedicationReminderManager(@ApplicationContext context: Context): MedicationReminderManager {
        return MedicationReminderManager(context)
    }
}
