package com.example.perfbubble

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class BubbleService : Service() {
    private lateinit var windowManager: WindowManager
    private lateinit var bubble: Button
    private lateinit var menu: LinearLayout
    private lateinit var bubbleParams: WindowManager.LayoutParams
    private lateinit var menuParams: WindowManager.LayoutParams

    private var menuShown = false
    private var bubbleSize = 0
    private var startX = 0
    private var startY = 0
    private var touchX = 0f
    private var touchY = 0f
    private var moved = false

    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onCreate() {
        super.onCreate()
        startForegroundNotification()
        buildBubble()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startForegroundNotification() {
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Bolha de performance",
                NotificationManager.IMPORTANCE_LOW
            )
            channel.description = "Toque na bolha para ativar ou desativar o turbo"
            manager.createNotificationChannel(channel)
        }

        val openApp = Intent(this, MainActivity::class.java)
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val pending = PendingIntent.getActivity(this, 0, openApp, flags)

        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
        } else {
            Notification.Builder(this)
        }

        startForeground(
            NOTIFICATION_ID,
            builder
                .setSmallIcon(android.R.drawable.ic_menu_manage)
                .setContentTitle("Perf Bubble")
                .setContentText("Toque na bolha para alternar o turbo")
                .setContentIntent(pending)
                .setOngoing(true)
                .build()
        )
    }

    @SuppressLint("InflateParams")
    private fun buildBubble() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        bubbleSize = dp(56)

        bubble = Button(this).apply {
            text = "⚡"
            textSize = 20f
            setTextColor(Color.WHITE)
            isAllCaps = false
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.rgb(79, 140, 255))
            }
        }

        val overlayType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }

        bubbleParams = WindowManager.LayoutParams(
            bubbleSize,
            bubbleSize,
            overlayType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 24
            y = 180
        }
        windowManager.addView(bubble, bubbleParams)

        bubble.setOnTouchListener { _, event -> onBubbleTouched(event) }

        val themed = ContextThemeWrapper(this, android.R.style.Theme_Material_Light)
        menu = LinearLayout(themed).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(8, 8, 8, 8)
            setBackgroundColor(Color.rgb(32, 32, 32))
        }

        menu.addView(
            Button(themed).apply {
                text = "Ativar turbo"
                setOnClickListener {
                    execute("on")
                    hideMenu()
                }
            }
        )
        menu.addView(
            Button(themed).apply {
                text = "Desativar turbo"
                setOnClickListener {
                    execute("off")
                    hideMenu()
                }
            }
        )

        menuParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = bubbleParams.x
            y = bubbleParams.y + bubbleSize + dp(8)
        }
    }

    private fun onBubbleTouched(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = bubbleParams.x
                startY = bubbleParams.y
                touchX = event.rawX
                touchY = event.rawY
                moved = false
                bubble.alpha = 0.8f
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                val dx = (event.rawX - touchX).toInt()
                val dy = (event.rawY - touchY).toInt()
                if (abs(dx) > 8 || abs(dy) > 8) moved = true

                val maxX = max(0, resources.displayMetrics.widthPixels - bubbleSize)
                val maxY = max(0, resources.displayMetrics.heightPixels - bubbleSize)
                bubbleParams.x = min(max(0, startX + dx), maxX)
                bubbleParams.y = min(max(0, startY + dy), maxY)
                windowManager.updateViewLayout(bubble, bubbleParams)
                if (menuShown) positionMenu()
                return true
            }

            MotionEvent.ACTION_UP -> {
                bubble.alpha = 1f
                if (!moved) {
                    if (menuShown) hideMenu() else showMenu()
                }
                return true
            }

            else -> return false
        }
    }

    private fun positionMenu() {
        menuParams.x = bubbleParams.x
        menuParams.y = min(
            bubbleParams.y + bubbleSize + dp(8),
            max(0, resources.displayMetrics.heightPixels - menu.height - dp(16))
        )
        windowManager.updateViewLayout(menu, menuParams)
    }

    private fun showMenu() {
        positionMenu()
        if (!menuShown) {
            windowManager.addView(menu, menuParams)
            menuShown = true
        }
    }

    private fun hideMenu() {
        if (menuShown) {
            try {
                windowManager.removeView(menu)
            } catch (_: Exception) {
            }
            menuShown = false
        }
    }

    private fun execute(action: String) {
        Thread {
            val result = RootBridge.run(action)
            mainHandler.post {
                Toast.makeText(applicationContext, result.output, Toast.LENGTH_LONG).show()
            }
        }.start()
    }

    private fun dp(value: Int): Int =
        Math.round(value * resources.displayMetrics.density)

    override fun onDestroy() {
        hideMenu()
        try {
            windowManager.removeView(bubble)
        } catch (_: Exception) {
        }
        super.onDestroy()
    }

    private companion object {
        const val CHANNEL_ID = "perf_bubble"
        const val NOTIFICATION_ID = 1001
    }
}
