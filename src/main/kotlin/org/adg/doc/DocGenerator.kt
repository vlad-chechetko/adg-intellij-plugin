package org.adg.doc

import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.IOException
import java.io.StringWriter
import java.nio.charset.Charset

abstract class DocGenerator {
    protected fun generateDocFromTemplate(templateName: String, params: MutableMap<String?, String?>, targetFile: String?) {
        val `is` = this.javaClass.getResourceAsStream("/templates/$templateName")
        val writer = StringWriter()
        try {
            IOUtils.copy(`is`, writer, ENCODING)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
        var template = writer.toString()
        for ((key, value) in params) {
            template = template.replace("\${$key}", value!!)
        }
        val f = File(targetFile)
        try {
            FileUtils.writeStringToFile(f, template, Charset.forName("UTF-8"))
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    companion object {
        private const val ENCODING = "UTF-8"
    }
}