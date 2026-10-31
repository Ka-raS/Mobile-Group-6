package com.group6.group6application.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import com.group6.group6application.MainActivity
import com.group6.group6application.R

internal data class StudyNotification(val title: String, val body: String)

internal object StudyNotifications {
    internal const val CHANNEL_ID = "study-sessions"
    private const val REMINDER_TAG = "study-reminder"
    private const val CHANGE_TAG = "study-change"

    fun createChannel(context: Context) {
        val channel = NotificationChannel(CHANNEL_ID, "Lịch học", NotificationManager.IMPORTANCE_DEFAULT).apply {
            description = "Nhắc buổi học sắp tới và thông báo khi lịch học thay đổi"
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    fun canNotify(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return false
        val manager = context.getSystemService(NotificationManager::class.java)
        return NotificationManagerCompat.from(context).areNotificationsEnabled() &&
            manager.getNotificationChannel(CHANNEL_ID)?.importance != NotificationManager.IMPORTANCE_NONE
    }

    fun show(context: Context, id: Int, message: StudyNotification, reminder: Boolean): Boolean {
        createChannel(context)
        if (!canNotify(context)) return false
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_study_notification)
            .setContentTitle(message.title)
            .setContentText(message.body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message.body))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .setCategory(if (reminder) NotificationCompat.CATEGORY_REMINDER else NotificationCompat.CATEGORY_EVENT)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .build()
        return try {
            NotificationManagerCompat.from(context).notify(if (reminder) REMINDER_TAG else CHANGE_TAG, id, notification)
            true
        } catch (_: SecurityException) {
            false
        }
    }

    fun dismissReminder(context: Context, id: Int) {
        NotificationManagerCompat.from(context).cancel(REMINDER_TAG, id)
    }

    fun wasReminded(context: Context, id: Int, startMillis: Long): Boolean =
        context.getSharedPreferences("delivered-study-reminders", Context.MODE_PRIVATE)
            .getLong(id.toString(), Long.MIN_VALUE) == startMillis

    fun markReminded(context: Context, id: Int, startMillis: Long) {
        context.getSharedPreferences("delivered-study-reminders", Context.MODE_PRIVATE)
            .edit(commit = true) { putLong(id.toString(), startMillis) }
    }

    fun dismiss(context: Context, id: Int) {
        dismissReminder(context, id)
        NotificationManagerCompat.from(context).cancel(CHANGE_TAG, id)
    }

    fun dismissAll(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.activeNotifications.filter { it.tag == REMINDER_TAG || it.tag == CHANGE_TAG }.forEach {
            manager.cancel(it.tag, it.id)
        }
    }
}
