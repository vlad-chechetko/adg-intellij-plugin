package org.adg.action

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.Logger

class AdgActionCheck : AnAction("Check", "Verify the checklist", AllIcons.Actions.Checked) {
    override fun actionPerformed(e: AnActionEvent) {
        throw RuntimeException("Not implemented")
    }

    companion object {
        private val LOG = Logger.getInstance(
            AdgActionCheck::class.java
        )
    }
}