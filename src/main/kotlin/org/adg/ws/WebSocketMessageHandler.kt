package org.adg.ws

interface WebSocketMessageHandler {
    fun handleMessage(msg: String)
}