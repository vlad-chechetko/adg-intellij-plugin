package org.adg.task.impl

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import org.adg.task.api.AdgTask
import org.adg.task.api.AdgTaskProgressCallback
import org.adg.task.api.AdgTaskResult
import org.adg.ui.console.AdgConsoleHolder

class AdgTaskClear : AdgTask("Clear") {
    override fun execute(project: Project?, taskProgressCallback: AdgTaskProgressCallback): AdgTaskResult {
        LOG.info("AdgTaskClear")
        AdgConsoleHolder.Companion.INSTANCE.info("> Clearing project workspace...")
        taskProgressCallback.progress("Clearing data")
        taskProgressCallback.progress(50)
        wait(1)
        taskProgressCallback.progress(100)
        taskProgressCallback.progress("Data was cleared")
        AdgConsoleHolder.Companion.INSTANCE.info("> Success")
        return AdgTaskResult(true)
    }

    companion object {
        private val LOG = Logger.getInstance(
            AdgTaskClear::class.java
        )
    }
}