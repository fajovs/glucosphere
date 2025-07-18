package com.ensias.glucosphere.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.ensias.glucosphere.data.database.GlucoseTrackerDatabase

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {

            Log.d("BootReceiver", "Device booted, rescheduling medication reminders")

            // Use goAsync() to handle the operation properly
            val pendingResult = goAsync()

            // Reschedule all medication reminders
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val database = GlucoseTrackerDatabase.getDatabase(context)
                    val medicationDao = database.medicationDao()
                    val scheduleDao = database.medicationScheduleDao()

                    val reminderManager = MedicationReminderManager(context)

                    // Get all active medications with schedules
                    medicationDao.getAllMedications().collect { medications ->
                        medications.forEach { medication ->
                            if (medication.isActive) {
                                scheduleDao.getSchedulesForMedication(medication.id).collect { schedules ->
                                    schedules.forEach { schedule ->
                                        if (schedule.isActive && schedule.reminderEnabled) {
                                            reminderManager.scheduleReminder(medication, schedule)
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("BootReceiver", "Error rescheduling reminders", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
