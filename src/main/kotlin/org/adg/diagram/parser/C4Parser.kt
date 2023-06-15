package org.adg.diagram.parser

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import org.adg.diagram.model.DiagramObject
import org.adg.diagram.model.DiagramObjectType
import org.adg.diagram.model.DiagramRelation
import org.w3c.dom.Document
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.InputSource
import java.io.*
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathExpressionException
import javax.xml.xpath.XPathFactory

class C4Parser {
    private var diagramObjectList: MutableList<DiagramObject>? = null
    var diagramDescription: String? = null
        private set
    val diagramObjects: List<DiagramObject>?
        get() = diagramObjectList

    @Throws(Exception::class)
    fun parseContextDiagram(project: Project?) {
        val diargamFileName = project!!.basePath + DIAGRAM_FILE
        val factory = DocumentBuilderFactory.newInstance()
        factory.isNamespaceAware = true
        val builder = factory.newDocumentBuilder()
        val inputStream: InputStream = FileInputStream(diargamFileName)
        val reader: Reader = InputStreamReader(inputStream, ENCODING)
        val `is` = InputSource(reader)
        `is`.encoding = ENCODING
        val doc = builder.parse(`is`)
        diagramObjectList = ArrayList()
        collectPersons(diagramObjectList as ArrayList<DiagramObject>, doc)
        collectSystems(diagramObjectList as ArrayList<DiagramObject>, doc)
        collectRelationships(diagramObjectList as ArrayList<DiagramObject>, doc)
        diagramDescription = getDiagramDescription(doc)
        if (diagramDescription == null || diagramDescription!!.isEmpty()) {
            diagramDescription = getSystemName(diagramObjectList as ArrayList<DiagramObject>, doc)
        }
        if (diagramDescription == null || diagramDescription!!.isEmpty()) {
            diagramDescription = ""
        }
        (diagramObjectList as ArrayList<DiagramObject>).stream()
            .forEach { diagramObject: DiagramObject -> LOG.info(diagramObject.id + " : type=" + diagramObject.type + " : relations=" + diagramObject.relationList.size) }
    }

    @Throws(XPathExpressionException::class)
    fun getDiagramDescription(doc: Document): String? {
        val xpathfactory = XPathFactory.newInstance()
        val xpath = xpathfactory.newXPath()
        val expr = xpath.compile("//object[@c4Type='ContainerScopeBoundary']")
        val result = expr.evaluate(doc, XPathConstants.NODESET)
        val nodes = result as NodeList
        for (i in 0 until nodes.length) {
            val nameAttr = nodes.item(i).attributes.getNamedItem("c4Name")
            if (nameAttr == null || nameAttr.textContent == null) {
                continue
            }
            if (nameAttr.textContent.startsWith("[System Context]")) {
                return nameAttr.textContent.substring("[System Context]".length + 1)
            }
        }
        for (i in 0 until nodes.length) {
            val nameAttr = nodes.item(i).attributes.getNamedItem("c4Name")
            if (nameAttr == null || nameAttr.textContent == null) {
                continue
            }
            return nameAttr.textContent.substring("[System Context]".length + 1)
        }
        return null
    }

    @Throws(XPathExpressionException::class)
    private fun getSystemName(diagramObjectList: List<DiagramObject>, doc: Document): String {
        val xpathfactory = XPathFactory.newInstance()
        val xpath = xpathfactory.newXPath()
        val expr = xpath.compile("//object[@c4Type='Software System']")
        val result = expr.evaluate(doc, XPathConstants.NODESET)
        val nodes = result as NodeList
        LOG.info("nodes.getLength() = " + nodes.length)
        for (i in 0 until nodes.length) {
            if (!hasGrayColor(nodes.item(i))) {
                return nodes.item(i).attributes.getNamedItem("c4Name").textContent
            }
        }
        return ""
    }

    @Throws(XPathExpressionException::class)
    private fun collectPersons(diagramObjectList: MutableList<DiagramObject>, doc: Document) {
        val xpathfactory = XPathFactory.newInstance()
        val xpath = xpathfactory.newXPath()
        val expr = xpath.compile("//object[@c4Type='Person']")
        val result = expr.evaluate(doc, XPathConstants.NODESET)
        val nodes = result as NodeList
        for (i in 0 until nodes.length) {
            val o = DiagramObject()
            o.type = DiagramObjectType.PERSON
            o.id = nodes.item(i).attributes.getNamedItem("id").textContent
            o.name = nodes.item(i).attributes.getNamedItem("c4Name").textContent
            o.description = nodes.item(i).attributes.getNamedItem("c4Description").textContent
            diagramObjectList.add(o)
        }
    }

    @Throws(XPathExpressionException::class)
    private fun collectSystems(diagramObjectList: MutableList<DiagramObject>, doc: Document) {
        val xpathfactory = XPathFactory.newInstance()
        val xpath = xpathfactory.newXPath()
        val expr = xpath.compile("//object[@c4Type='Software System']")
        val result = expr.evaluate(doc, XPathConstants.NODESET)
        val nodes = result as NodeList
        LOG.info("nodes.getLength() = " + nodes.length)
        for (i in 0 until nodes.length) {
            val o = DiagramObject()
            if (hasGrayColor(nodes.item(i))) {
                o.type = DiagramObjectType.EXTERNAL_SYSTEM
            } else {
                o.type = DiagramObjectType.SYSTEM
            }
            o.id = nodes.item(i).attributes.getNamedItem("id").textContent
            o.name = nodes.item(i).attributes.getNamedItem("c4Name").textContent
            o.description = nodes.item(i).attributes.getNamedItem("c4Description").textContent
            diagramObjectList.add(o)
        }
    }

    private fun hasGrayColor(n: Node): Boolean {
        val mxCellList = n.childNodes
        if (mxCellList == null || mxCellList.length == 0) {
            return false
        }
        for (i in 0 until mxCellList.length) {
            val mxCell = mxCellList.item(i)
            if ("mxCell" == mxCell.nodeName) {
                val styleAttr = mxCell.attributes.getNamedItem("style")
                if (styleAttr == null || styleAttr.textContent == null) {
                    continue
                }
                if (styleAttr.textContent.contains("fillColor=#8C8496")) {
                    return true
                }
                if (styleAttr.textContent.contains("fillColor=#999999")) {
                    return true
                }
            }
        }
        return false
    }

    @Throws(XPathExpressionException::class)
    private fun collectRelationships(diagramObjectList: List<DiagramObject>, doc: Document) {
        val xpathfactory = XPathFactory.newInstance()
        val xpath = xpathfactory.newXPath()
        val expr = xpath.compile("//object[@c4Type='Relationship']")
        val result = expr.evaluate(doc, XPathConstants.NODESET)
        val nodes = result as NodeList
        LOG.info("nodes.getLength() = " + nodes.length)
        for (i in 0 until nodes.length) {
            val mxCellList = nodes.item(i).childNodes
            if (mxCellList == null || mxCellList.length == 0) {
                continue
            }
            for (t in 0 until mxCellList.length) {
                val mxCell = mxCellList.item(t)
                if (mxCell == null || mxCell.nodeName != "mxCell") {
                    continue
                }
                val sourceId = mxCell.attributes.getNamedItem("source").textContent
                val targetId = mxCell.attributes.getNamedItem("target").textContent
                val sourceObject = findById(diagramObjectList, sourceId)
                val targetObject = findById(diagramObjectList, targetId)
                if (sourceObject == null || targetObject == null) {
                    continue
                }
                val r = DiagramRelation()
                r.description = nodes.item(i).attributes.getNamedItem("c4Description").textContent
                r.targetObject = targetObject
                sourceObject.relationList.add(r)
                break
            }
        }
    }

    private fun findById(diagramObjectList: List<DiagramObject>, id: String?): DiagramObject? {
        if (id == null || id.isBlank()) {
            return null
        }
        for (o in diagramObjectList) {
            if (o.id == id) {
                return o
            }
        }
        return null
    }

    companion object {
        private val LOG = Logger.getInstance(
            C4Parser::class.java
        )
        const val ENCODING = "UTF-8"
        const val DIAGRAM_FILE = "/diagrams/c4context.drawio"
    }
}