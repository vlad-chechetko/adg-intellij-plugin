package org.adg.ui.console

import com.intellij.execution.ui.ConsoleView
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory

class AdgConsoleWindowFactory : ToolWindowFactory, DumbAware {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val consoleView: ConsoleView = AdgConsoleHolder.Companion.INSTANCE.createConsole(project)
        val content = toolWindow.contentManager.factory.createContent(consoleView.component, "ADG console", false)
        toolWindow.contentManager.addContent(content)
    }
}