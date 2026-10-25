package com.fedos.floatingmuse

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import androidx.core.app.NotificationCompat
import kotlin.math.abs

class FloatingButtonService : Service() {
    private lateinit var windowManager: WindowManager
    private var button: View? = null
    private var initialX = 0
    private var initialY = 0
    private var touchX = 0f
    private var touchY = 0f

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
        if (Settings.canDrawOverlays(this)) showButton() else stopSelf()
    }

    private fun showButton() {
        if (button != null) return
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val image = ImageView(this).apply {
            setImageResource(com.fedos.floatingmuse.R.drawable.muse_button)
            contentDescription = getString(com.fedos.floatingmuse.R.string.floating_button_description)
            setOnTouchListener(::handleTouch)
        }
        button = image
        val params = WindowManager.LayoutParams(
            dp(64), dp(64),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = dp(16)
            y = dp(180)
        }
        windowManager.addView(image, params)
    }

    private fun handleTouch(view: View, event: MotionEvent): Boolean {
        val params = view.layoutParams as WindowManager.LayoutParams
        return when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                initialX = params.x
                initialY = params.y
                touchX = event.rawX
                touchY = event.rawY
                true
            }
            MotionEvent.ACTION_MOVE -> {
                params.x = initialX + (event.rawX - touchX).toInt()
                params.y = initialY + (event.rawY - touchY).toInt()
                params.y = params.y.coerceAtLeast(dp(8))
                windowManager.updateViewLayout(view, params)
                true
            }
            MotionEvent.ACTION_UP -> {
                val moved = abs(event.rawX - touchX) > dp(8) || abs(event.rawY - touchY) > dp(8)
                if (!moved) startActivity(Intent(this, MiniChatActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                true
            }
            else -> false
        }
    }

    private fun buildNotification(): Notification {
        val open = PendingIntent.getActivity(
            this, 0, Intent(this, MiniChatActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(com.fedos.floatingmuse.R.drawable.ic_stat_muse)
            .setContentTitle(getString(com.fedos.floatingmuse.R.string.app_name))
            .setContentText(getString(com.fedos.floatingmuse.R.string.service_running))
            .setContentIntent(open)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getSystemService(NotificationManager::class.java).createNotificationChannel(
                NotificationChannel(CHANNEL_ID, getString(com.fedos.floatingmuse.R.string.notification_channel_name), NotificationManager.IMPORTANCE_LOW)
            )
        }
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()

    override fun onDestroy() {
        button?.let { windowManager.removeView(it) }
        button = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val CHANNEL_ID = "floating_muse_service"
        private const val NOTIFICATION_ID = 1001
    }
}
