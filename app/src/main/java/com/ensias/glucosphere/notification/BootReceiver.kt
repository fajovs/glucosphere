package com.ensias.glucosphere.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.ensias.glucosphere.data.database.GlucoseTrackerDatabase

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED) {

            Log.d("BootReceiver", "Device booted, rescheduling medication reminders")

            // Handle async work safely
            val pendingResult = goAsync()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val database = GlucoseTrackerDatabase.getDatabase(context)
                    val medicationDao = database.medicationDao()
                    val scheduleDao = database.medicationScheduleDao()
                    val reminderManager = MedicationReminderManager(context)

                    // 🔹 Get the active user ID
                    val activeUserId = getActiveUserId(context)

                    if (activeUserId == -1) {
                        Log.w("BootReceiver", "No active user ID found. Skipping reminder reschedule.")
                        pendingResult.finish()
                        return@launch
                    }

                    // Get all medications for this user
                    val medications = medicationDao.getAllMedications(activeUserId).first()

                    medications.forEach { medication ->
                        if (medication.isActive) {
                            val schedules = scheduleDao.getSchedulesForMedication(medication.id).first()
                            schedules.forEach { schedule ->
                                if (schedule.isActive && schedule.reminderEnabled) {
                                    reminderManager.scheduleReminder(medication, schedule)
                                }
                            }
                        }
                    }

                    Log.d("BootReceiver", "Reminders successfully rescheduled for user $activeUserId")

                } catch (e: Exception) {
                    Log.e("BootReceiver", "Error rescheduling reminders", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }

    /**
     * Helper to retrieve the currently active user's ID from SharedPreferences.
     * Make sure to save this value when the user logs in.
     */
    private fun getActiveUserId(context: Context): Int {
        val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return prefs.getInt("active_user_id", -1)
    }
}
