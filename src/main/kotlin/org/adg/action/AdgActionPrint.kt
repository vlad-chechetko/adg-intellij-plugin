package org.adg.action

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import org.adg.ui.console.AdgConsoleHolder
import java.io.File
import java.io.IOException

class AdgActionPrint : AnAction("Print", "Log some debug info", AllIcons.General.Print) {
    override fun actionPerformed(e: AnActionEvent) {
        showInfo(e.project)
    }

    private fun showInfo(project: Project?) {
        val startDir = File(project!!.basePath)
        try {
            val s = getFirstDrawioFile(startDir)
            AdgConsoleHolder.Companion.INSTANCE.info("> Some debug info...")
            AdgConsoleHolder.Companion.INSTANCE.data("Project base path: " + project.basePath)
            if (s != null) {
                AdgConsoleHolder.Companion.INSTANCE.data("First *.drawio file in the project: $s")
            }
            AdgConsoleHolder.Companion.INSTANCE.info("> End of debug info")
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    @Throws(IOException::class)
    private fun getFirstDrawioFile(folder: File): String? {
        val files = folder.listFiles()
        for (fileEntry in files) {
            if (!fileEntry.isDirectory) {
                if (fileEntry.name.endsWith(".drawio")) {
                    //return Files.readString(fileEntry.toPath());
                    return fileEntry.name
                }
            }
        }
        for (fileEntry in files) {
            if (fileEntry.isDirectory) {
                val f = getFirstDrawioFile(fileEntry)
                if (f != null) {
                    return f
                }
            }
        }
        return null
    }
}