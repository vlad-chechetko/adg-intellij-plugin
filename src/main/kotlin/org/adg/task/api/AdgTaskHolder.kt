package org.adg.task.api

import org.adg.task.impl.*

class AdgTaskHolder {
    private val buildTasks: MutableList<AdgTask>
    private val checkTasks: MutableList<AdgTask>

    init {
        buildTasks = ArrayList()
        buildTasks.add(AdgTaskValidate())
        buildTasks.add(AdgTaskClear())
        buildTasks.add(AdgTaskBuild())
        checkTasks = ArrayList()
        checkTasks.add(AdgTaskCheck1())
        checkTasks.add(AdgTaskCheck2())
    }

    fun getBuildTasks(): List<AdgTask> {
        return buildTasks
    }

    fun getCheckTasks(): List<AdgTask> {
        return checkTasks
    }

    companion object {
        val INSTANCE = AdgTaskHolder()
    }
}