package com.ensias.glucosphere.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.ensias.glucosphere.MainActivity
import com.ensias.glucosphere.data.database.entity.Medication
import com.ensias.glucosphere.data.database.entity.MedicationSchedule
import java.util.*
import android.util.Log
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

class MedicationReminderManager(private val context: Context) {

    companion object {
        private const val CHANNEL_ID = "medication_reminders"
        private const val CHANNEL_NAME = "Medication Reminders"
        private const val CHANNEL_DESCRIPTION = "Notifications for medication reminders"
        private const val TAG = "MedicationReminder"
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                setShowBadge(true)
                enableLights(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Notification channel created")
        }
    }

    fun scheduleReminder(medication: Medication, schedule: MedicationSchedule) {
        try {
            // Check for exact alarm permission on Android 12+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                if (!alarmManager.canScheduleExactAlarms()) {
                    Log.w(TAG, "Cannot schedule exact alarms - permission not granted")
                    return
                }
            }

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, MedicationReminderReceiver::class.java).apply {
                putExtra("medication_name", medication.name)
                putExtra("medication_dosage", medication.dosage)
                putExtra("medication_id", medication.id)
                putExtra("schedule_id", schedule.id)
                putExtra("time_hour", schedule.timeHour)
                putExtra("time_minute", schedule.timeMinute)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                schedule.id.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, schedule.timeHour)
                set(Calendar.MINUTE, schedule.timeMinute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)

                // If the time has passed today, schedule for tomorrow
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_MONTH, 1)
                }
            }

            Log.d(TAG, "Scheduling reminder for ${medication.name} at ${calendar.time}")

            // Use setExactAndAllowWhileIdle for better reliability
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling reminder", e)
        }
    }

    fun scheduleNextDayReminder(
        medicationId: Long,
        scheduleId: Long,
        medicationName: String,
        medicationDosage: String,
        timeHour: Int,
        timeMinute: Int
    ) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, MedicationReminderReceiver::class.java).apply {
                putExtra("medication_name", medicationName)
                putExtra("medication_dosage", medicationDosage)
                putExtra("medication_id", medicationId)
                putExtra("schedule_id", scheduleId)
                putExtra("time_hour", timeHour)
                putExtra("time_minute", timeMinute)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                scheduleId.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val calendar = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_MONTH, 1) // Tomorrow
                set(Calendar.HOUR_OF_DAY, timeHour)
                set(Calendar.MINUTE, timeMinute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            Log.d(TAG, "Scheduling next day reminder for $medicationName at ${calendar.time}")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling next day reminder", e)
        }
    }

    fun cancelReminder(scheduleId: Long) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, MedicationReminderReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                scheduleId.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            alarmManager.cancel(pendingIntent)
            Log.d(TAG, "Cancelled reminder for schedule ID: $scheduleId")
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling reminder", e)
        }
    }

    fun showNotification(medicationName: String, dosage: String, medicationId: Long) {
        try {
            Log.d(TAG, "Showing notification for $medicationName")

            // Check notification permission on Android 13+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                    Log.w(TAG, "Notification permission not granted")
                    return
                }
            }

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("open_medication", medicationId)
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                medicationId.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Create the "Mark as Taken" action
            val markAsTakenIntent = Intent(context, MedicationActionReceiver::class.java).apply {
                action = "MARK_AS_TAKEN"
                putExtra("medication_id", medicationId)
            }

            val markAsTakenPendingIntent = PendingIntent.getBroadcast(
                context,
                (medicationId + 10000).toInt(), // Unique request code
                markAsTakenIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("💊 Medication Reminder")
                .setContentText("Time to take $medicationName ($dosage)")
                .setStyle(NotificationCompat.BigTextStyle()
                    .bigText("It's time to take your medication: $medicationName ($dosage)"))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setAutoCancel(false) // Don't auto-cancel so user must take action
                .setOngoing(false)
                .setContentIntent(pendingIntent)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .addAction(
                    android.R.drawable.ic_menu_save,
                    "Mark as Taken",
                    markAsTakenPendingIntent
                )
                .build()

            with(NotificationManagerCompat.from(context)) {
                if (areNotificationsEnabled()) {
                    notify(medicationId.toInt(), notification)
                    Log.d(TAG, "Notification sent for $medicationName with ID: ${medicationId.toInt()}")
                } else {
                    Log.w(TAG, "Notifications are disabled")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error showing notification", e)
        }
    }

    // Test method to show immediate notification
    fun showTestNotification() {
        showNotification("Test Medication", "10mg", 999L)
    }
}
