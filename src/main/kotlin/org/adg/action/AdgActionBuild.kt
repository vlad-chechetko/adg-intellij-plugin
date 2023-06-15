package org.adg.action

import com.intellij.icons.AllIcons
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.MessageDialogBuilder
import com.intellij.openapi.ui.MessageDialogBuilder.YesNo
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFileManager
import org.adg.task.api.AdgTaskHolder
import org.adg.task.api.AdgTaskProgressCallback
import org.adg.task.api.DefaultAdgTaskProgressCallback

class AdgActionBuild : AnAction("Build", "Execute build process", AllIcons.Actions.Compile) {
    override fun actionPerformed(e: AnActionEvent) {
        val dialogBuilder: YesNo =
            MessageDialogBuilder.yesNo("Confirmation", "Are you sure you want to execute all task in build group?")
                .yesText("OK")
                .noText("Cancel")
                .icon(Messages.getQuestionIcon())
        val confirmed: Boolean = dialogBuilder.guessWindowAndAsk()
        if (confirmed) {
            build(e.project)
        }
    }

    private fun build(project: Project?) {
        //ProgressManager.getInstance().run(new Task.Backgroundable(project, "Title"){
        ProgressManager.getInstance().run(object : Task.Modal(project, "Preparing diagram documentation", true) {
            override fun run(progressIndicator: ProgressIndicator) {
                val weight: Int = 100 / AdgTaskHolder.Companion.INSTANCE.getBuildTasks().size
                var totalProgress = 0
                try {
                    for (t in AdgTaskHolder.Companion.INSTANCE.getBuildTasks()) {
                        val taskProgressCallback: AdgTaskProgressCallback =
                            DefaultAdgTaskProgressCallback(progressIndicator, weight, totalProgress)
                        val r = t.execute(project, taskProgressCallback)
                        totalProgress = totalProgress + weight
                        if (!r!!.isSuccess) {
                            NotificationGroupManager.getInstance()
                                .getNotificationGroup("Tasks")
                                .createNotification("Error." + r.messageToUser, NotificationType.ERROR)
                                .notify(project)
                            return
                        }
                        if (progressIndicator.isCanceled) {
                            return
                        }
                    }
                    NotificationGroupManager.getInstance()
                        .getNotificationGroup("Tasks")
                        .createNotification("Build success", NotificationType.INFORMATION)
                        .notify(project)
                    openResultFile(project)
                } catch (ex: Exception) {
                    LOG.error(ex)
                    NotificationGroupManager.getInstance()
                        .getNotificationGroup("Tasks")
                        .createNotification(ex.message!!, NotificationType.ERROR)
                        .notify(project)
                    return
                }
            }
        })
    }

    private fun openResultFile(project: Project?) {
        val r = Runnable {
            val resultFileURL = "file://" + project!!.basePath + "/architecture.adoc"
            val resultFile = VirtualFileManager.getInstance().findFileByUrl(resultFileURL)
            if (resultFile != null) {
                FileEditorManager.getInstance(project).openFile(resultFile, true, true)
            } else {
                LOG.warn("Can't open result file $resultFileURL")
            }
        }
        ApplicationManager.getApplication().invokeLater(r)
    }

    companion object {
        private val LOG = Logger.getInstance(
            AdgActionBuild::class.java
        )
    }
}