package com.ensias.glucosphere.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.ensias.glucosphere.data.database.GlucoseTrackerDatabase
import com.ensias.glucosphere.data.database.entity.MedicationLog
import java.util.*

class MedicationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("MedicationAction", "Received action: ${intent.action}")

        when (intent.action) {
            "MARK_AS_TAKEN" -> {
                val medicationId = intent.getLongExtra("medication_id", -1)
                Log.d("MedicationAction", "Mark as taken for medication ID: $medicationId")

                if (medicationId != -1L) {
                    markMedicationAsTaken(context, medicationId)
                } else {
                    Log.e("MedicationAction", "Invalid medication ID")
                }
            }
            else -> {
                Log.w("MedicationAction", "Unknown action: ${intent.action}")
            }
        }
    }

    private fun markMedicationAsTaken(context: Context, medicationId: Long) {
        // Use goAsync() to handle the operation properly in a broadcast receiver
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = GlucoseTrackerDatabase.getDatabase(context)
                val medicationLogDao = database.medicationLogDao()

                val now = Date()
                val log = MedicationLog(
                    medicationId = medicationId,
                    scheduledTime = now,
                    actualTime = now,
                    taken = true,
                    notes = "Marked from notification"
                )

                medicationLogDao.insertMedicationLog(log)
                Log.d("MedicationAction", "Successfully logged medication as taken")

                // Dismiss the notification
                NotificationManagerCompat.from(context).cancel(medicationId.toInt())
                Log.d("MedicationAction", "Notification dismissed")

                // Show a confirmation notification
                showConfirmationNotification(context, medicationId)

            } catch (e: Exception) {
                Log.e("MedicationAction", "Error marking medication as taken", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun showConfirmationNotification(context: Context, medicationId: Long) {
        try {
            val notificationManager = NotificationManagerCompat.from(context)

            val confirmationNotification = androidx.core.app.NotificationCompat.Builder(context, "medication_reminders")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("✅ Medication Taken")
                .setContentText("Successfully logged your medication")
                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_LOW)
                .setAutoCancel(true)
                .setTimeoutAfter(3000) // Auto dismiss after 3 seconds
                .build()

            // Use a different notification ID for confirmation
            notificationManager.notify((medicationId + 50000).toInt(), confirmationNotification)

        } catch (e: Exception) {
            Log.e("MedicationAction", "Error showing confirmation notification", e)
        }
    }
}
