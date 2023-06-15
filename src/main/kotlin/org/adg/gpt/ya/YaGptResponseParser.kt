package org.adg.gpt.ya

import com.fasterxml.jackson.core.JsonFactory
import java.io.IOException

class YaGptResponseParser {
    fun getResponseText(responseJson: String?): String? {
        var cardAttr = false
        try {
            val jfactory = JsonFactory()
            val jParser = jfactory.createParser(responseJson)
            while (jParser.nextToken() != null) {
                val fieldname = jParser.currentName
                if ("card" == fieldname) {
                    cardAttr = true
                }
                if ("text" == fieldname && cardAttr) {
                    jParser.nextToken()
                    return jParser.text
                }
            }
            jParser.close()
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
        return null
    }
}