package com.group6.group6application.notification

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CancellationException

/** Mounted only on student routes. Request once; restore again after login/resume. */
@Composable
fun StudentNotificationSetup(studentId: Int) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val prefs = remember(context) { context.getSharedPreferences("notification-settings", Context.MODE_PRIVATE) }
    var permissionRevision by remember { mutableIntStateOf(0) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        permissionRevision++
        if (!granted) Toast.makeText(
            context, "Bạn có thể bật thông báo lịch học trong Cài đặt ứng dụng.", Toast.LENGTH_LONG,
        ).show()
    }

    LaunchedEffect(studentId) {
        StudyNotifications.createChannel(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED &&
            !prefs.getBoolean("permission-requested", false)
        ) {
            prefs.edit { putBoolean("permission-requested", true) }
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    LaunchedEffect(studentId, lifecycle, permissionRevision) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            try {
                NotificationScheduler.restore(context, studentId)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("StudyNotification", "Unable to restore reminders", e)
            }
        }
    }
}
