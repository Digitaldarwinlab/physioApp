package com.darwin.physioai.agora

import com.darwin.physioai.agora.NotificationUtils
import android.app.NotificationManager
import android.annotation.TargetApi
import android.os.Build
import android.app.NotificationChannel
import android.app.Activity
import android.os.Bundle
import android.content.Intent
import com.darwin.physioai.agora.ScreenCaptureActivity
import android.media.projection.MediaProjectionManager
import android.media.projection.MediaProjection
import android.hardware.display.VirtualDisplay
import com.darwin.physioai.agora.ScreenCaptureService.OrientationChangeCallback
import android.graphics.Bitmap
import com.darwin.physioai.agora.ScreenCaptureService
import android.view.OrientationEventListener
import android.os.IBinder
import android.os.Looper
import android.view.WindowManager
import com.darwin.physioai.agora.ScreenCaptureService.MediaProjectionStopCallback
import android.annotation.SuppressLint
import android.app.Notification
import android.content.Context
import com.darwin.physioai.agora.ScreenCaptureService.ImageAvailableListener
import android.hardware.display.DisplayManager
import androidx.core.app.NotificationCompat
import androidx.core.util.Pair

object NotificationUtils {
    const val NOTIFICATION_ID = 1337
    private const val NOTIFICATION_CHANNEL_ID = "com.mtsahakis.mediaprojectiondemo.app"
    private const val NOTIFICATION_CHANNEL_NAME = "com.mtsahakis.mediaprojectiondemo.app"
    fun getNotification(context: Context): Pair<Int, Notification> {
        createNotificationChannel(context)
        val notification = createNotification(context)
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
        return Pair(NOTIFICATION_ID, notification)
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            )
            channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            val manager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(context: Context): Notification {
        val builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
        builder.setSmallIcon(R.drawable.ic_camera)
        builder.setContentTitle(context.getString(R.string.app_name))
        builder.setContentText(context.getString(R.string.recording))
        builder.setOngoing(true)
        builder.setCategory(Notification.CATEGORY_SERVICE)
        builder.priority = Notification.PRIORITY_LOW
        builder.setShowWhen(true)
        return builder.build()
    }
}