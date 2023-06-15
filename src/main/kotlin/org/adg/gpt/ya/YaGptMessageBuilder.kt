package org.adg.gpt.ya

import org.apache.commons.io.IOUtils
import java.io.IOException
import java.io.StringWriter
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class YaGptMessageBuilder {
    private val sessionUUID: String
    private val runGptMessageGUID: String
    private var messageCount = 0

    init {
        sessionUUID = sessionID
        runGptMessageGUID = gUID
    }

    val authJson: String
        get() {
            val `is` = this.javaClass.getResourceAsStream("/yagpt/auth.json")
            val writer = StringWriter()
            try {
                IOUtils.copy(`is`, writer, ENCODING)
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
            var msg = writer.toString()
            msg = msg.replace("\${MessageGUID}", gUID)
            msg = msg.replace("\${MessageNumber}", nexMessageNumber)
            msg = msg.replace("\${AuthToken}", YAGPT_AUTH_TOKEN)
            msg = msg.replace("\${SessionUUID}", sessionUUID)
            return msg
        }
    val runGptJson: String
        get() {
            val `is` = this.javaClass.getResourceAsStream("/yagpt/run_gpt.json")
            val writer = StringWriter()
            try {
                IOUtils.copy(`is`, writer, ENCODING)
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
            var msg = writer.toString()
            msg = msg.replace("\${MessageGUID}", gUID)
            msg = msg.replace("\${MessageNumber}", nexMessageNumber)
            msg = msg.replace("\${SessionUUID}", sessionUUID)
            msg = msg.replace("\${TimeGMT}", gmtTime)
            msg = msg.replace("\${TimeUnix}", unixTime)
            msg = msg.replace("\${RequestGUID}", runGptMessageGUID)
            return msg
        }
    val startDialogJson: String
        get() {
            val `is` = this.javaClass.getResourceAsStream("/yagpt/start_dialog.json")
            val writer = StringWriter()
            try {
                IOUtils.copy(`is`, writer, ENCODING)
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
            var msg = writer.toString()
            msg = msg.replace("\${MessageGUID}", gUID)
            msg = msg.replace("\${MessageNumber}", nexMessageNumber)
            msg = msg.replace("\${SessionUUID}", sessionUUID)
            msg = msg.replace("\${TimeGMT}", gmtTime)
            msg = msg.replace("\${TimeUnix}", unixTime)
            msg = msg.replace("\${RequestGUID}", gUID)
            msg = msg.replace("\${DialogGUID}", YAGPT_DIALOG_ID)
            msg = msg.replace("\${PrvRequestGUID}", runGptMessageGUID)
            msg = msg.replace("\${DialogGUID}", YAGPT_DIALOG_ID)
            return msg
        }

    fun getMessageJson(messageText: String?): String {
        val `is` = this.javaClass.getResourceAsStream("/yagpt/message.json")
        val writer = StringWriter()
        try {
            IOUtils.copy(`is`, writer, ENCODING)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
        var msg = writer.toString()
        msg = msg.replace("\${MessageGUID}", gUID)
        msg = msg.replace("\${MessageNumber}", nexMessageNumber)
        msg = msg.replace("\${SessionUUID}", sessionUUID)
        msg = msg.replace("\${TimeGMT}", gmtTime)
        msg = msg.replace("\${TimeUnix}", unixTime)
        msg = msg.replace("\${RequestGUID}", gUID)
        msg = msg.replace("\${DialogGUID}", YAGPT_DIALOG_ID)
        msg = msg.replace("\${MessageText}", messageText!!)
        return msg
    }

    private val nexMessageNumber: String
        private get() {
            messageCount++
            return Integer.toString(messageCount)
        }
    private val gUID: String
        private get() = UUID.randomUUID().toString()
    private val sessionID: String
        private get() = "0".repeat(13) + getRandomDigits(19)

    private fun getRandomDigits(digitsCount: Int): String {
        val sb = StringBuilder()
        for (i in 0 until digitsCount) {
            val rand = Random()
            val n = rand.nextInt(9) + 1
            sb.append(n)
        }
        return sb.toString()
    }

    private val gmtTime: String
        private get() {
            val currentTime = ZonedDateTime.now(ZoneOffset.UTC)
            val df = DateTimeFormatter.ofPattern("yyyyMMdd")
            val tf = DateTimeFormatter.ofPattern("HHmmss")
            return currentTime.format(df) + "T" + currentTime.format(tf)
        }
    private val unixTime: String
        private get() {
            val unixTimestamp = Instant.now().epochSecond
            return unixTimestamp.toString()
        }

    companion object {
        private const val YAGPT_AUTH_TOKEN = "effd5a3f-fd42-4a18-83a1-61766a6d0924"
        private const val YAGPT_DIALOG_ID = "b7c42cab-db61-46ba-871a-b10a6ecf3e0d"
        private const val ENCODING = "UTF-8"
    }
}