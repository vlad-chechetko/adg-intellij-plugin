package org.adg.gpt

import org.adg.diagram.model.DiagramObject

interface GptQueryBuilder {
    fun buildApplicationQuery(appDiagramDescription: String?, appDiagramObjects: List<DiagramObject?>?): String
    fun buildAbbreviationsQuery(applicationQuery: String): String
}