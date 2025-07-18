package com.ensias.glucosphere.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MedicationReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("MedicationReminder", "Received alarm broadcast")

        val medicationName = intent.getStringExtra("medication_name")
        val medicationDosage = intent.getStringExtra("medication_dosage")
        val medicationId = intent.getLongExtra("medication_id", -1)

        Log.d("MedicationReminder", "Medication: $medicationName, Dosage: $medicationDosage, ID: $medicationId")

        if (medicationName != null && medicationDosage != null && medicationId != -1L) {
            // Use goAsync() to handle the operation properly
            val pendingResult = goAsync()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val reminderManager = MedicationReminderManager(context)
                    reminderManager.showNotification(medicationName, medicationDosage, medicationId)

                    // Reschedule for next day
                    rescheduleForNextDay(context, intent)

                } catch (e: Exception) {
                    Log.e("MedicationReminder", "Error showing notification", e)
                } finally {
                    pendingResult.finish()
                }
            }
        } else {
            Log.e("MedicationReminder", "Missing required data in intent")
        }
    }

    private fun rescheduleForNextDay(context: Context, originalIntent: Intent) {
        try {
            val medicationName = originalIntent.getStringExtra("medication_name") ?: return
            val medicationDosage = originalIntent.getStringExtra("medication_dosage") ?: return
            val medicationId = originalIntent.getLongExtra("medication_id", -1)
            val scheduleId = originalIntent.getLongExtra("schedule_id", -1)

            if (medicationId != -1L && scheduleId != -1L) {
                // Get the time from the original schedule
                val timeHour = originalIntent.getIntExtra("time_hour", -1)
                val timeMinute = originalIntent.getIntExtra("time_minute", -1)

                if (timeHour != -1 && timeMinute != -1) {
                    val reminderManager = MedicationReminderManager(context)
                    reminderManager.scheduleNextDayReminder(
                        medicationId, scheduleId, medicationName, medicationDosage, timeHour, timeMinute
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("MedicationReminder", "Error rescheduling", e)
        }
    }
}
