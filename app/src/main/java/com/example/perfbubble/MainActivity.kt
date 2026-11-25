package com.example.perfbubble

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import frb.axeron.shell.AxBroker

class MainActivity : Activity() {
    companion object {
        private const val REQ_NOTIFICATIONS = 1
        private const val REQ_AXMANAGER = 2
    }

    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(48, 48, 48, 48)
        }

        statusText = TextView(this).apply { text = "Status: pronto." }

        root.addView(statusText)
        root.addView(button("Permitir sobreposição") { requestOverlayPermission() })
        root.addView(button("Permitir acesso no AxManager") { askAxManagerAccess() })
        root.addView(button("Iniciar bolha") { startBubble() })
        root.addView(button("Parar bolha") { stopBubble() })
        root.addView(button("Testar status do módulo") { testStatus() })
        setContentView(root)

        if (Build.VERSION.SDK_INT >= 33
            && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                REQ_NOTIFICATIONS
            )
        }

        if (!Settings.canDrawOverlays(this)) {
            requestOverlayPermission()
        } else {
            askAxManagerAccess()
        }
    }

    private fun button(label: String, click: () -> Unit): Button =
        Button(this).apply {
            text = label
            setOnClickListener { click() }
        }

    private fun requestOverlayPermission() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        startActivity(intent)
    }

    private fun askAxManagerAccess() {
        if (AxBroker.findManager(this) == null) {
            statusText.text = "AxManager não encontrado (ou em modo stealth)."
            return
        }
        if (!AxBroker.requestAccess(this, REQ_AXMANAGER)) {
            statusText.text = "Não foi possível solicitar acesso ao AxManager."
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_AXMANAGER) {
            statusText.text = if (resultCode == RESULT_OK) {
                "Acesso ao AxManager concedido."
            } else {
                "Acesso ao AxManager negado."
            }
        }
    }

    private fun startBubble() {
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Conceda a sobreposição primeiro", Toast.LENGTH_LONG).show()
            requestOverlayPermission()
            return
        }
        startForegroundService(Intent(this, BubbleService::class.java))
        Toast.makeText(this, "Bolha iniciada", Toast.LENGTH_SHORT).show()
    }

    private fun stopBubble() {
        stopService(Intent(this, BubbleService::class.java))
        Toast.makeText(this, "Bolha parada", Toast.LENGTH_SHORT).show()
    }

    private fun testStatus() {
        statusText.text = "Consultando módulo..."
        Thread {
            val result = RootBridge.run("status")
            runOnUiThread { statusText.text = result.output }
        }.start()
    }
}
