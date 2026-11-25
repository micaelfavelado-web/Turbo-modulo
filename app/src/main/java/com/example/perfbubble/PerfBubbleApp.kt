package com.example.perfbubble

import android.app.Application
import frb.axeron.shell.AxBroker

class PerfBubbleApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Inicia o broker da API nativa do AxManager (axsh).
        // A conexao e entregue ao app quando o AxManager o permite.
        AxBroker.start(this)
    }
}
