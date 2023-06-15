package org.adg.task.impl

import com.intellij.openapi.project.Project
import org.adg.task.api.AdgTask
import org.adg.task.api.AdgTaskProgressCallback
import org.adg.task.api.AdgTaskResult

class AdgTaskCheck1 : AdgTask("Check-1") {
    override fun execute(project: Project?, taskProgressCallback: AdgTaskProgressCallback): AdgTaskResult {
        return AdgTaskResult(true)
    }
}