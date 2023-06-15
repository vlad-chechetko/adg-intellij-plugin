package org.adg.gpt

interface GptClient {
    val stateInfo: String
    fun startDialog()
    fun endDialog()
    fun ask(message: String?): String?
}