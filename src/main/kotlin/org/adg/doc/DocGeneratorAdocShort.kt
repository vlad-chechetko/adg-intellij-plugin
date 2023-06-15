package org.adg.doc

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFileManager

class DocGeneratorAdocShort : DocGenerator() {
    fun generate(archText: String?, abbrText: String?, project: Project?) {
        val imageFileName = project!!.basePath + "/.work/architecture.png"
        val targetFileName = project.basePath + "/architecture.adoc"
        val params: MutableMap<String?, String?> = HashMap()
        params["Terms"] = abbrText
        params["ContextImage"] = imageFileName
        params["ContextDescription"] = archText
        generateDocFromTemplate("template_short.adoc", params, targetFileName)
        ApplicationManager.getApplication().invokeLaterOnWriteThread { VirtualFileManager.getInstance().syncRefresh() }
    }
}