package org.adg.ui.console

import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.execution.ui.ConsoleView
import com.intellij.execution.ui.ConsoleViewContentType
import com.intellij.openapi.project.Project

class AdgConsoleHolder {
    private var consoleView: ConsoleView? = null
    fun createConsole(project: Project?): ConsoleView {
        consoleView = TextConsoleBuilderFactory.getInstance().createBuilder(project!!).console
        return consoleView!!
    }

    fun info(text: String) {
        consoleView!!.print(
            """
    $text
    
    """.trimIndent(), ConsoleViewContentType.LOG_VERBOSE_OUTPUT
        )
    }

    fun data(text: String?) {
        consoleView!!.print(
            """
    $text
    
    """.trimIndent(), ConsoleViewContentType.NORMAL_OUTPUT
        )
    }

    fun error(text: String?) {
        consoleView!!.print(
            """
    $text
    
    """.trimIndent(), ConsoleViewContentType.ERROR_OUTPUT
        )
    }

    companion object {
        val INSTANCE = AdgConsoleHolder()
    }
}