package org.adg.task.api

import com.intellij.openapi.progress.ProgressIndicator
import java.math.BigDecimal

class DefaultAdgTaskProgressCallback : AdgTaskProgressCallback {
    private var progressIndicator: ProgressIndicator
    private var taskWeight: Int
    private var currentTotalCompletePercents: Int

    constructor(progressIndicator: ProgressIndicator) {
        this.progressIndicator = progressIndicator
        progressIndicator.isIndeterminate = false;
        taskWeight = 100
        currentTotalCompletePercents = 0
    }

    constructor(progressIndicator: ProgressIndicator, taskWeight: Int, currentTotalCompletePercents: Int) {
        this.progressIndicator = progressIndicator
        progressIndicator.isIndeterminate = false;
        this.taskWeight = taskWeight
        this.currentTotalCompletePercents = currentTotalCompletePercents
    }

    override fun progress(completedPercents: Int, text: String?) {
        progress(completedPercents)
        progress(text)
    }

    override fun progress(completedPercents: Int) {
        progressIndicator.text = getIntFraction(completedPercents).toString() + " %"
        progressIndicator.fraction = getDoubleFraction(completedPercents)
    }

    override fun progress(text: String?) {
        progressIndicator.text2 = text
    }

    private fun getDoubleFraction(completePercets: Int): Double {
        val weight = BigDecimal.valueOf(taskWeight.toLong()).divide(BigDecimal.valueOf(100))
        val complete = BigDecimal.valueOf(completePercets.toLong()).divide(BigDecimal.valueOf(100)).multiply(weight)
        val completeTotal =
            complete.add(BigDecimal.valueOf(currentTotalCompletePercents.toLong()).divide(BigDecimal.valueOf(100)))
        return completeTotal.toDouble()
    }

    private fun getIntFraction(completePercets: Int): Int {
        val weight = BigDecimal.valueOf(taskWeight.toLong()).divide(BigDecimal.valueOf(100))
        val complete = BigDecimal.valueOf(completePercets.toLong()).divide(BigDecimal.valueOf(100)).multiply(weight)
        val completeTotal =
            complete.add(BigDecimal.valueOf(currentTotalCompletePercents.toLong()).divide(BigDecimal.valueOf(100)))
        return completeTotal.multiply(BigDecimal.valueOf(100)).toInt()
    }
}