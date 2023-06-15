package org.adg.diagram.model

import java.util.*

class DiagramObject {
    var type: DiagramObjectType? = null
    var name: String? = null
    var description: String? = null
    var id: String? = null
    val relationList: MutableList<DiagramRelation?> = ArrayList()
    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as DiagramObject
        return id == that.id
    }

    override fun hashCode(): Int {
        return Objects.hash(id)
    }
}