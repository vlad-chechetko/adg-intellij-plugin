package org.adg.task.api

import com.intellij.openapi.project.Project

abstract class AdgTask(private val taskName: String) {
    override fun toString(): String {
        return taskName
    }

    protected fun wait(sec: Int) {
        try {
            Thread.sleep((sec * 1000).toLong())
        } catch (e: InterruptedException) {
            throw RuntimeException(e)
        }
    }

    abstract fun execute(project: Project?, taskProgressCallback: AdgTaskProgressCallback): AdgTaskResult
}