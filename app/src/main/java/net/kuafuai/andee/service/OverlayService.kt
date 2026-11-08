package net.kuafuai.andee.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import androidx.core.app.NotificationCompat
import net.kuafuai.andee.R

/**
 * OverlayService - WindowManager floating ball (Andee-style).
 * The persistent UI element users interact with.
 */
class OverlayService : Service() {

    companion object {
        private const val CHANNEL_ID = "olivia_overlay"
        private const val NOTIFICATION_ID = 1002

        fun start(context: Context) {
            val intent = Intent(context, OverlayService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, OverlayService::class.java)
            context.stopService(intent)
        }

        fun isOverlayPermissionGranted(context: Context): Boolean {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return android.provider.Settings.canDrawOverlays(context)
            }
            return true
        }

        fun requestOverlayPermission(context: Context, requestCode: Int) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!android.provider.Settings.canDrawOverlays(context)) {
                    val intent = Intent(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        android.net.Uri.parse("package:${context.packageName}"))
                    (context as? android.app.Activity)?.startActivityForResult(intent, requestCode)
                }
            }
        }
    }

    private var windowManager: WindowManager? = null
    private var floatingBall: View? = null
    private var ballParams: WindowManager.LayoutParams? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
        addFloatingBall()
    }

    override fun onDestroy() {
        removeFloatingBall()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun addFloatingBall() {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val inflater = LayoutInflater.from(this)
        floatingBall = inflater.inflate(R.layout.floating_ball, null)

        ballParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 200
        }

        val ballImage = floatingBall?.findViewById<ImageView>(R.id.ball_image)
        ballImage?.setOnTouchListener { v, event ->
            handleBallTouch(v, event)
            true
        }

        ballImage?.setOnClickListener {
            // Tap to open chat overlay
            openChatOverlay()
        }

        windowManager?.addView(floatingBall!!, ballParams!!)
    }

    private fun removeFloatingBall() {
        if (floatingBall != null && windowManager != null) {
            try {
                windowManager?.removeView(floatingBall)
            } catch (e: Exception) {
                // Ignore
            }
            floatingBall = null
        }
    }

    private fun handleBallTouch(view: View, event: MotionEvent) {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                ballParams?.let { params ->
                    params.x = event.rawX.toInt() - view.width / 2
                    params.y = event.rawY.toInt() - view.height / 2 - 100 // Status bar offset
                    windowManager?.updateViewLayout(view, params)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                ballParams?.let { params ->
                    params.x = event.rawX.toInt() - view.width / 2
                    params.y = event.rawY.toInt() - view.height / 2 - 100
                    windowManager?.updateViewLayout(view, params)
                }
            }
            MotionEvent.ACTION_UP -> {
                // Save position
            }
        }
    }

    private fun openChatOverlay() {
        // TODO: Launch chat overlay activity
        val intent = Intent(this, net.kuafuai.andee.ui.ChatOverlayActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Olivia Overlay",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Floating ball overlay"
                setShowBadge(false)
                setSound(null, null)
                enableVibration(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val intent = Intent(this, net.kuafuai.andee.ui.SettingsActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle("Olivia Overlay Active")
            .setContentText("Floating ball is running")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
    }
}