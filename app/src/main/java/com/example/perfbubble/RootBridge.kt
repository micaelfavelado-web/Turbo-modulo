package com.example.perfbubble

import frb.axeron.shell.Axsh

/**
 * Ponte para o modulo Magisk atraves da API nativa do AxManager.
 * Nenhum servidor HTTP local e usado.
 */
object RootBridge {
    private const val ACTION = "sh /data/adb/modules/turboperf/action.sh"

    fun run(command: String): CommandResult {
        return try {
            // Axsh.newProcess executa no shell do AxManager (shell ou root).
            val process = Axsh.newProcess("-c", "$ACTION $command")
            val output = process.inputStream.bufferedReader().use { it.readText() }.trim()
            val errors = process.errorStream.bufferedReader().use { it.readText() }.trim()
            val code = process.waitFor()

            val text = if (errors.isEmpty()) output else "$output\n$errors".trim()
            CommandResult(
                ok = code == 0 && text.isNotEmpty(),
                output = text.ifEmpty { "Comando executado (sem saida)." }
            )
        } catch (error: Exception) {
            CommandResult(false, "Falha na API AxManager: ${error.message}")
        }
    }
}

data class CommandResult(val ok: Boolean, val output: String)
