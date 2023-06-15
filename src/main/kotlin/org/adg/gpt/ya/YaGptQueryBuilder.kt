package org.adg.gpt.ya

import org.adg.diagram.model.DiagramObject
import org.adg.diagram.model.DiagramObjectType
import org.adg.gpt.GptQueryBuilder

class YaGptQueryBuilder : GptQueryBuilder {
    override fun buildApplicationQuery(
        appDiagramDescription: String?,
        appDiagramObjects: List<DiagramObject?>?
    ): String {
        val sb = StringBuilder()
        sb.append("Подготовь описание для веб приложения ")
        sb.append(appDiagramDescription)
        sb.append(". ")
        appendUsers(appDiagramDescription, appDiagramObjects, sb)
        appendFeatures(appDiagramDescription, appDiagramObjects, sb)
        appendExternalSystemsList(appDiagramDescription, appDiagramObjects, sb)
        appendExternalSystemsDescription(appDiagramObjects, sb)
        return sb.toString().replace("\"", "'").replace("\n", " ")
    }

    override fun buildAbbreviationsQuery(applicationQuery: String): String {
        return "Подготовь глоссарий для следующего текста. " +
                applicationQuery.replace("\"", "'").replace("\n", " ")
    }

    private fun appendUsers(
        appDiagramDescription: String?,
        appDiagramObjects: List<DiagramObject?>?,
        sb: StringBuilder
    ) {
        sb.append("С приложением ")
        sb.append(appDiagramDescription)
        sb.append(" работают следующие пользователи: ")
        var firstElement = true
        for (o in appDiagramObjects!!) {
            if (o!!.type == DiagramObjectType.PERSON) {
                if (!firstElement) {
                    sb.append(", ")
                }
                firstElement = false
                sb.append(o.name)
            }
        }
        sb.append(". ")
    }

    private fun appendFeatures(
        appDiagramDescription: String?,
        appDiagramObjects: List<DiagramObject?>?,
        sb: StringBuilder
    ) {
        for (o in appDiagramObjects!!) {
            if (o!!.type == DiagramObjectType.PERSON) {
                sb.append("Приложение ")
                sb.append(appDiagramDescription)
                sb.append(" предоставляет пользователю ")
                sb.append(o.name)
                sb.append(" следующие функции: ")
                appendPersonFeatures(appDiagramDescription, appDiagramObjects, o, sb)
            }
        }
    }

    private fun appendPersonFeatures(
        appDiagramDescription: String?,
        appDiagramObjects: List<DiagramObject?>?,
        person: DiagramObject?,
        sb: StringBuilder
    ) {
        val features: MutableList<String?> = ArrayList()
        for (r in person!!.relationList) {
            if (r?.description == null || r.description!!.isBlank()) {
                continue
            }
            features.add(r.description)
        }
        if (features.size == 0) {
            return
        }
        for (i in features.indices) {
            if (i > 0) {
                sb.append(", ")
            }
            sb.append(features[i])
        }
        sb.append(". ")
    }

    private fun appendExternalSystemsList(
        appDiagramDescription: String?,
        appDiagramObjects: List<DiagramObject?>?,
        sb: StringBuilder
    ) {
        val hasExternalSystems = appDiagramObjects!!.stream()
            .anyMatch { d: DiagramObject? -> d!!.type == DiagramObjectType.EXTERNAL_SYSTEM }
        if (!hasExternalSystems) {
            return
        }
        sb.append("Приложение ")
        sb.append(appDiagramDescription)
        sb.append(" использует следующие внешние системы: ")
        var firstItem = true
        for (d in appDiagramObjects) {
            if (d!!.type != DiagramObjectType.EXTERNAL_SYSTEM) {
                continue
            }
            if (!firstItem) {
                sb.append(", ")
            }
            firstItem = false
            sb.append(d.name)
        }
        sb.append(". ")
    }

    private fun appendExternalSystemsDescription(appDiagramObjects: List<DiagramObject?>?, sb: StringBuilder) {
        val hasExternalSystems = appDiagramObjects!!.stream()
            .anyMatch { d: DiagramObject? -> d!!.type == DiagramObjectType.EXTERNAL_SYSTEM }
        if (!hasExternalSystems) {
            return
        }
        for (d in appDiagramObjects) {
            if (d!!.type == DiagramObjectType.EXTERNAL_SYSTEM) {
                val systemUsecases = getSystemUsecases(appDiagramObjects, d)
                if (systemUsecases.size == 0) {
                    continue
                }
                sb.append("Внешняя система ")
                sb.append(d.name)
                if (systemUsecases.size == 1) {
                    sb.append(" используется для ")
                    sb.append(systemUsecases[0])
                    sb.append(". ")
                } else {
                    sb.append(" используется для: ")
                    var firstItem = true
                    for (s in systemUsecases) {
                        if (!firstItem) {
                            sb.append(", ")
                        }
                        firstItem = false
                        sb.append(s)
                    }
                    sb.append(". ")
                }
            }
        }
    }

    private fun getSystemUsecases(
        appDiagramObjects: List<DiagramObject?>?,
        externalSystem: DiagramObject?
    ): List<String?> {
        val systemUsecases: MutableList<String?> = ArrayList()
        for (d in appDiagramObjects!!) {
            for (r in d!!.relationList) {
                if (r!!.targetObject == externalSystem) {
                    systemUsecases.add(r.description)
                }
            }
        }
        return systemUsecases
    }
}