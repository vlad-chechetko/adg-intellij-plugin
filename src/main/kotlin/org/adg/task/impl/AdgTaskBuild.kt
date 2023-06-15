package org.adg.task.impl

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import org.adg.diagram.parser.C4Parser
import org.adg.doc.DocGeneratorAdocShort
import org.adg.gpt.GptClient
import org.adg.gpt.GptQueryBuilder
import org.adg.gpt.ya.YaGptClient
import org.adg.gpt.ya.YaGptQueryBuilder
import org.adg.task.api.AdgTask
import org.adg.task.api.AdgTaskProgressCallback
import org.adg.task.api.AdgTaskResult
import org.adg.ui.console.AdgConsoleHolder

class AdgTaskBuild : AdgTask("Build") {
    private val c4Parser: C4Parser
    private val gptQueryBuilder: GptQueryBuilder
    private val gptClient: GptClient
    private val docGenerator: DocGeneratorAdocShort

    init {
        c4Parser = C4Parser()
        gptQueryBuilder = YaGptQueryBuilder()
        gptClient = YaGptClient()
        docGenerator = DocGeneratorAdocShort()
    }

    override fun execute(project: Project?, taskProgressCallback: AdgTaskProgressCallback): AdgTaskResult {
        LOG.info("AdgTaskBuild")
        try {
            process(project, taskProgressCallback)
        } catch (e: Exception) {
            LOG.error(e)
            AdgConsoleHolder.Companion.INSTANCE.error(e.message)
            throw RuntimeException(e)
        }
        return AdgTaskResult(true)
    }

    @Throws(Exception::class)
    private fun process(project: Project?, taskProgressCallback: AdgTaskProgressCallback) {
        AdgConsoleHolder.Companion.INSTANCE.info("> Parsing c4context diagram info...")
        c4Parser.parseContextDiagram(project)
        val description = c4Parser.diagramDescription
        val objects = c4Parser.diagramObjects
        LOG.info("description:$description")
        LOG.info("objects.size:" + objects!!.size)
        taskProgressCallback.progress(10)
        printParsingInfo()
        AdgConsoleHolder.Companion.INSTANCE.info("> Success")
        taskProgressCallback.progress("Starting GPT dialog")
        AdgConsoleHolder.Companion.INSTANCE.info("> Starting GPT dialog...")
        gptClient.startDialog()
        taskProgressCallback.progress(30)
        AdgConsoleHolder.Companion.INSTANCE.data(gptClient.stateInfo)
        AdgConsoleHolder.Companion.INSTANCE.info("> Success")
        AdgConsoleHolder.Companion.INSTANCE.info("> Asking GPT about c4context diagram...")
        taskProgressCallback.progress("Asking GPT about c4context diagram")
        val appQuery = gptQueryBuilder.buildApplicationQuery(description, objects)
        AdgConsoleHolder.Companion.INSTANCE.data("GPT query: $appQuery")
        LOG.info("appQuery:$appQuery")
        Thread.sleep(1000L)
        taskProgressCallback.progress(35)
        val appQueryResponse = gptClient.ask(appQuery)
        LOG.info("appQueryResponse:$appQueryResponse")
        AdgConsoleHolder.Companion.INSTANCE.data("GPT response: $appQueryResponse")
        if (appQueryResponse == null) {
            throw RuntimeException("No response from GPT")
        }
        taskProgressCallback.progress(55)
        AdgConsoleHolder.Companion.INSTANCE.info("> Success")
        AdgConsoleHolder.Companion.INSTANCE.info("> Asking GPT about abbreviations...")
        taskProgressCallback.progress("Asking GPT about abbreviations")
        val abbrQuery = gptQueryBuilder.buildAbbreviationsQuery(appQueryResponse)
        LOG.info("abbrQuery:$abbrQuery")
        AdgConsoleHolder.Companion.INSTANCE.data("GPT query: $abbrQuery")
        Thread.sleep(1000L)
        taskProgressCallback.progress(60)
        val abbrQueryResponse = gptClient.ask(abbrQuery)
        LOG.info("abbrQueryResponse:$abbrQueryResponse")
        AdgConsoleHolder.Companion.INSTANCE.data("GPT response: $abbrQueryResponse")
        if (abbrQueryResponse == null) {
            throw RuntimeException("No response from GPT")
        }
        taskProgressCallback.progress(80)
        AdgConsoleHolder.Companion.INSTANCE.info("> Success")
        AdgConsoleHolder.Companion.INSTANCE.info("> Finishing GPT dialog...")
        taskProgressCallback.progress("Finishing GPT dialog")
        taskProgressCallback.progress(100)
        Thread.sleep(1000L)
        gptClient.endDialog()
        AdgConsoleHolder.Companion.INSTANCE.data(gptClient.stateInfo)
        AdgConsoleHolder.Companion.INSTANCE.info("> Success")
        AdgConsoleHolder.Companion.INSTANCE.info("> Generating documentation...")
        taskProgressCallback.progress("Generating documentation")
        docGenerator.generate(appQueryResponse, abbrQueryResponse, project)
        AdgConsoleHolder.Companion.INSTANCE.info("> Success")
    }

    private fun printParsingInfo() {
        val description = c4Parser.diagramDescription
        val objects = c4Parser.diagramObjects
        AdgConsoleHolder.Companion.INSTANCE.data("Diagram description: $description")
        AdgConsoleHolder.Companion.INSTANCE.data("Diagram objects count: " + objects!!.size)
        if (objects.size == 0) return
        var sb = StringBuilder("Diagram objects:")
        for (i in objects.indices) {
            sb.append("\n\t- ")
            sb.append(objects[i].type.toString())
            sb.append(" ")
            sb.append(objects[i].name)
        }
        AdgConsoleHolder.Companion.INSTANCE.data(sb.toString())
        for (o in objects) {
            sb = StringBuilder("")
            sb.append("Object ")
            sb.append(o.id)
            sb.append("\n\tType = ")
            sb.append(o.type)
            sb.append("\n\tName = ")
            sb.append(o.name)
            sb.append("\n\tDescription = ")
            sb.append(o.description)
            if (o.relationList.size > 0) {
                sb.append("\n\tRelations: ")
                for (i in o.relationList.indices) {
                    val r = o.relationList[i]
                    sb.append("\n\t\t")
                    sb.append("- ")
                    sb.append(r?.description ?: "")
                    sb.append(" (")
                    sb.append(r?.targetObject?.type ?: "")
                    sb.append(", ")
                    sb.append(r?.targetObject?.name ?: "")
                    sb.append(", ")
                    sb.append(r?.targetObject?.id ?: "")
                    sb.append(")")
                }
            }
            AdgConsoleHolder.Companion.INSTANCE.data(sb.toString())
        }
    }

    companion object {
        private val LOG = Logger.getInstance(
            AdgTaskBuild::class.java
        )
    }
}