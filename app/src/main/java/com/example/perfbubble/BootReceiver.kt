package com.example.perfbubble

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Settings

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        if (!Settings.canDrawOverlays(context)) return

        try {
            context.startForegroundService(Intent(context, BubbleService::class.java))
        } catch (_: Exception) {
            // Android pode bloquear FGS em background; use o botao do app nesse caso.
        }
    }
}
