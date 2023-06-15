package org.adg.task.api

interface AdgTaskProgressCallback {
    fun progress(completedPercents: Int, text: String?)
    fun progress(completedPercents: Int)
    fun progress(text: String?)
}