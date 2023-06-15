package org.adg.gpt.ya

import com.intellij.openapi.diagnostic.Logger
import org.adg.gpt.GptClient
import org.adg.ws.WebSocketClient
import org.adg.ws.WebSocketMessageHandler
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

class YaGptClient : GptClient {
    private var webSocketClient: WebSocketClient? = null
    private var responses: BlockingQueue<String>? = null
    private val messageBuilder: YaGptMessageBuilder
    private val responseParser: YaGptResponseParser
    private var connected: Boolean

    init {
        messageBuilder = YaGptMessageBuilder()
        responseParser = YaGptResponseParser()
        connected = false
    }

    override val stateInfo: String
        get() {
            val sb = StringBuilder()
            if (connected) {
                sb.append("Connected to GPT server: ")
                sb.append(YAGPT_WS_URL)
            } else {
                sb.append("Disconnected from GPT server")
            }
            return sb.toString()
        }

    override fun startDialog() {
        check(!connected) { "Client has been already connected to GPT server" }
        responses = LinkedBlockingQueue(100)
        webSocketClient = WebSocketClient()
        val h = object : WebSocketMessageHandler {
            override fun handleMessage(msg: String) {
                try {
                    (responses as LinkedBlockingQueue<String>).put(msg as String?)
                } catch (e: InterruptedException) {
                    throw RuntimeException(e)
                }
            }
        }
        webSocketClient!!.connect(YAGPT_WS_URL, h)
        val authMessage = messageBuilder.authJson
        LOG.info("authMessage: \n$authMessage")
        webSocketClient!!.sendMessage(authMessage)
        val runGptMessage = messageBuilder.runGptJson
        LOG.info("runGptMessage: \n$runGptMessage")
        webSocketClient!!.sendMessage(runGptMessage)
        val runGptResponse = getResponse(WAIT_FOR_WS_RESPONSE_SEC)
        LOG.info("runGptResponse: \n$runGptResponse")
        val startDialogMessage = messageBuilder.startDialogJson
        LOG.info("startDialogMessage: \n$startDialogMessage")
        webSocketClient!!.sendMessage(startDialogMessage)
        val startDialogResponse = getResponse(WAIT_FOR_WS_RESPONSE_SEC)
        LOG.info("startDialogResponse: \n$startDialogResponse")
        connected = true
    }

    override fun endDialog() {
        if (connected) {
            webSocketClient!!.disconnect()
            connected = false
        }
    }

    override fun ask(message: String?): String? {
        check(connected) { "Client is not connected to GPT server" }
        val requestJson = messageBuilder.getMessageJson(message)
        LOG.info("requestJson: \n$requestJson")
        webSocketClient!!.sendMessage(requestJson)
        val responseJson = getResponse(WAIT_FOR_WS_RESPONSE_SEC)
        LOG.info("responseJson: \n$responseJson")
        val responseText = responseParser.getResponseText(responseJson)
        LOG.info("responseText: \n$responseText")
        return responseText
    }

    private fun getResponse(timeoutSec: Int): String? {
        return try {
            responses!!.poll(timeoutSec.toLong(), TimeUnit.SECONDS)
        } catch (e: InterruptedException) {
            null
        }
    }

    companion object {
        private val LOG = Logger.getInstance(
            YaGptClient::class.java
        )
        private const val YAGPT_WS_URL = "wss://uniproxy.alice.ya.ru/uni.ws"
        private const val WAIT_FOR_WS_RESPONSE_SEC = 10
    }
}