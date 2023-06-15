package org.adg.ui.tool

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

class AdgToolWindowFactory : ToolWindowFactory, DumbAware {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val adgToolWindowPanel = AdgToolWindowPanel(project)
        val content = ContentFactory.SERVICE.getInstance().createContent(adgToolWindowPanel, "ADG tools", false)
        toolWindow.contentManager.addContent(content)
    }
}