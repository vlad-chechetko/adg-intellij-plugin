package org.adg.task.impl

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import org.adg.diagram.model.DiagramObject
import org.adg.diagram.model.DiagramObjectType
import org.adg.diagram.parser.C4Parser
import org.adg.gpt.GptQueryBuilder
import org.adg.gpt.ya.YaGptQueryBuilder
import org.adg.task.api.AdgTask
import org.adg.task.api.AdgTaskProgressCallback
import org.adg.task.api.AdgTaskResult
import org.adg.ui.console.AdgConsoleHolder

class AdgTaskValidate : AdgTask("Validate") {
    private val c4Parser: C4Parser
    private val gptQueryBuilder: GptQueryBuilder

    init {
        c4Parser = C4Parser()
        gptQueryBuilder = YaGptQueryBuilder()
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

    private fun process(project: Project?, taskProgressCallback: AdgTaskProgressCallback) {
        LOG.info("AdgTaskValidate")
        AdgConsoleHolder.Companion.INSTANCE.info("> Validating...")
        taskProgressCallback.progress("Validating source files")
        try {
            c4Parser.parseContextDiagram(project)
            AdgConsoleHolder.Companion.INSTANCE.data("Diagram is parsed")
        } catch (e: Exception) {
            throw RuntimeException("Failed to parse diagram file", e)
        }
        taskProgressCallback.progress(15)
        val description = c4Parser.diagramDescription ?: throw RuntimeException("Failed to find diagram description")
        AdgConsoleHolder.Companion.INSTANCE.data("Diagram description is defined")
        taskProgressCallback.progress(30)
        val objects = c4Parser.diagramObjects
        if (objects!!.size < 2) {
            throw RuntimeException("The diagram must contain at last 2 objects - System and Person")
        }
        AdgConsoleHolder.Companion.INSTANCE.data("Diagram objects count is ok")
        taskProgressCallback.progress(45)
        wait(1)
        val systemObjectsCount =
            objects.stream().filter { o: DiagramObject? -> o!!.type == DiagramObjectType.SYSTEM }
                .count()
        if (systemObjectsCount != 1L) {
            throw RuntimeException("The diagram must contain exactly one System object")
        }
        AdgConsoleHolder.Companion.INSTANCE.data("Diagram System object was found")
        taskProgressCallback.progress(60)
        val personObjectsCount =
            objects.stream().filter { o: DiagramObject? -> o!!.type == DiagramObjectType.PERSON }
                .count()
        if (personObjectsCount == 0L) {
            throw RuntimeException("The diagram must contain at last one Person object")
        }
        AdgConsoleHolder.Companion.INSTANCE.data("Diagram Person(s) object(s) was found")
        taskProgressCallback.progress(75)
        checkRelations(objects)
        AdgConsoleHolder.Companion.INSTANCE.data("Diagram relations are correct")
        taskProgressCallback.progress(90)
        val appQuery = gptQueryBuilder.buildApplicationQuery(description, objects)
        if (appQuery == null || appQuery.isEmpty()) {
            throw RuntimeException("Unable to build GPT query from diagram data")
        }
        AdgConsoleHolder.Companion.INSTANCE.data("GPT query building is ok")
        taskProgressCallback.progress(100)
        AdgConsoleHolder.Companion.INSTANCE.info("> Success")
        taskProgressCallback.progress("Validating finished")
    }

    private fun checkRelations(objects: List<DiagramObject?>?) {
        for (o in objects!!) {
            for (r in o!!.relationList) {
                val sourceObjectType = o.type
                val targetObjectType = r!!.targetObject!!.type
                if (sourceObjectType == DiagramObjectType.PERSON && targetObjectType != DiagramObjectType.SYSTEM) {
                    throw RuntimeException("Person must be connected to System object only")
                }
                if (sourceObjectType == DiagramObjectType.SYSTEM && targetObjectType != DiagramObjectType.EXTERNAL_SYSTEM) {
                    throw RuntimeException("System must be connected to External System object only")
                }
                if (sourceObjectType == DiagramObjectType.EXTERNAL_SYSTEM) {
                    throw RuntimeException("External System relations are not supported")
                }
                if (targetObjectType == DiagramObjectType.PERSON) {
                    throw RuntimeException("Person object must not be a target for incoming relations")
                }
            }
        }
        for (o1 in objects) {
            if (o1!!.type == DiagramObjectType.PERSON) {
                continue
            }
            var incomingRelationsFound = false
            for (o2 in objects) {
                if (o1 === o2) {
                    continue
                }
                for (r in o2!!.relationList) {
                    if (r!!.targetObject == o1) {
                        incomingRelationsFound = true
                    }
                }
            }
            if (!incomingRelationsFound) {
                throw RuntimeException("Object " + o1.type.toString() + " must have at last one incoming relation")
            }
        }
    }

    companion object {
        private val LOG = Logger.getInstance(
            AdgTaskValidate::class.java
        )
    }
}