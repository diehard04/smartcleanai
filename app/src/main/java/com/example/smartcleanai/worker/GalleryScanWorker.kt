package com.example.smartcleanai.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.smartcleanai.MainActivity
import com.example.smartcleanai.R
import com.example.smartcleanai.data.repository.MediaRepositoryImpl
import kotlinx.coroutines.flow.firstOrNull
import java.util.concurrent.TimeUnit

class GalleryScanWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        const val WORK_NAME = "SmartClean_WeeklyAutoScan"
        const val NOTIFICATION_CHANNEL_ID = "smart_clean_auto_scan_channel"
        const val NOTIFICATION_ID = 1001
        const val JUNK_THRESHOLD_BYTES = 500L * 1024 * 1024 // 500 MB

        fun enqueuePeriodicScan(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()

            val request = PeriodicWorkRequestBuilder<GalleryScanWorker>(7, TimeUnit.DAYS)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        fun cancelPeriodicScan(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }

    override suspend fun doWork(): Result {
        return try {
            val repository = MediaRepositoryImpl(context)
            val resultMap = repository.scanGallery(isIncremental = true) {}.firstOrNull() ?: return Result.success()

            val totalJunkBytes = resultMap.values.sumOf { it.totalBytes }

            if (totalJunkBytes >= JUNK_THRESHOLD_BYTES) {
                showJunkDetectedNotification(totalJunkBytes)
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun showJunkDetectedNotification(junkBytes: Long) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "SmartClean Auto-Scan Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies when more than 500 MB of reclaimable junk is found"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val mb = junkBytes / (1024.0 * 1024.0)
        val gb = mb / 1024.0
        val formattedSize = if (gb >= 1.0) String.format("%.1f GB", gb) else String.format("%.0f MB", mb)

        val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("SmartClean AI: Junk Detected!")
            .setContentText("Found $formattedSize of duplicates, blurry photos & memes. Tap to clean!")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Over $formattedSize of reclaimable storage detected in your gallery. Review and free up space in one tap.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}
