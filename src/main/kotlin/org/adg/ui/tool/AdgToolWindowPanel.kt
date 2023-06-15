package org.adg.ui.tool

import com.intellij.icons.AllIcons
import com.intellij.ide.CommonActionsManager
import com.intellij.ide.DefaultTreeExpander
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.ui.ScrollPaneFactory
import com.intellij.ui.treeStructure.Tree
import com.intellij.util.EditSourceOnDoubleClickHandler.TreeMouseListener
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.tree.TreeUtil
import org.adg.action.AdgActionBuild
import org.adg.action.AdgActionCheck
import org.adg.action.AdgActionPrint
import org.adg.task.api.AdgTask
import org.adg.task.api.AdgTaskHolder
import org.adg.task.api.AdgTaskProgressCallback
import org.adg.task.api.DefaultAdgTaskProgressCallback
import java.awt.Component
import java.awt.event.MouseEvent
import javax.swing.JPanel
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeCellRenderer
import javax.swing.tree.TreePath
import javax.swing.tree.TreeSelectionModel

class AdgToolWindowPanel(private val project: Project) : SimpleToolWindowPanel(true, true) {
    init {
        val t = createTree()
        toolbar = createToolbar(t)
        setContent(ScrollPaneFactory.createScrollPane(t))
    }

    private fun createTree(): Tree {
        val top = DefaultMutableTreeNode(project.name)
        var category: DefaultMutableTreeNode
        category = DefaultMutableTreeNode("Build")
        top.add(category)
        for (t in AdgTaskHolder.Companion.INSTANCE.getBuildTasks()) {
            category.add(DefaultMutableTreeNode(t))
        }
        category = DefaultMutableTreeNode("Check list")
        top.add(category)
        for (t in AdgTaskHolder.Companion.INSTANCE.getCheckTasks()) {
            category.add(DefaultMutableTreeNode(t))
        }
        val tree = Tree(top)
        tree.isRootVisible = true
        tree.showsRootHandles = true
        tree.dragEnabled = false
        tree.isEditable = false
        tree.selectionModel.selectionMode = TreeSelectionModel.SINGLE_TREE_SELECTION
        TreeUtil.installActions(tree)
        TreeUtil.expandAll(tree)
        tree.cellRenderer = createTreeCellRenderer()
        object : TreeMouseListener(tree, null) {
            override fun processDoubleClick(e: MouseEvent, dataContext: DataContext, treePath: TreePath) {
                treeDoubleCLick(treePath)
            }
        }.installOn(tree)
        return tree
    }

    private fun treeDoubleCLick(treePath: TreePath?) {
        if (treePath == null) {
            return
        }
        val selectedNode = treePath.lastPathComponent as DefaultMutableTreeNode ?: return
        val o = selectedNode.userObject
        if (o == null || o is AdgTask == false) {
            return
        }
        executeTask(o)
    }

    private fun executeTask(task: AdgTask) {
        ProgressManager.getInstance().run(object : Task.Modal(
            project, "$task task", false
        ) {
            override fun run(progressIndicator: ProgressIndicator) {
                val taskProgressCallback: AdgTaskProgressCallback = DefaultAdgTaskProgressCallback(progressIndicator)
                try {
                    val r = task.execute(project, taskProgressCallback)
                    if (r!!.isSuccess) {
                        var message = r.messageToUser
                        if (message == null || message.isBlank()) {
                            message = "Task <b>$task</b> was success"
                        }
                        NotificationGroupManager.getInstance()
                            .getNotificationGroup("Tasks")
                            .createNotification(message, NotificationType.INFORMATION)
                            .notify(project)
                    } else {
                        var message = r.messageToUser
                        if (message == null || message.isBlank()) {
                            message = "Error executing task <b>$task</b>"
                        }
                        NotificationGroupManager.getInstance()
                            .getNotificationGroup("Tasks")
                            .createNotification(r.messageToUser!!, NotificationType.ERROR)
                            .notify(project)
                    }
                } catch (ex: Exception) {
                    NotificationGroupManager.getInstance()
                        .getNotificationGroup("Tasks")
                        .createNotification(ex.message!!, NotificationType.ERROR)
                        .notify(project)
                }
            }
        })
    }

    private fun createTreeCellRenderer(): DefaultTreeCellRenderer {
        return object : DefaultTreeCellRenderer() {
            override fun getTreeCellRendererComponent(
                tree: JTree,
                value: Any,
                sel: Boolean,
                expanded: Boolean,
                leaf: Boolean,
                row: Int,
                hasFocus: Boolean
            ): Component {
                super.getTreeCellRendererComponent(
                    tree, value, sel,
                    expanded, leaf, row,
                    hasFocus
                )
                if (leaf) {
                    icon = AllIcons.Actions.InlayGear
                } else {
                    icon = if (row == 0) {
                        AllIcons.General.ProjectTab
                    } else {
                        AllIcons.Actions.MenuOpen
                    }
                    toolTipText = null
                }
                return this
            }
        }
    }

    private fun createToolbar(tree: Tree): JPanel {
        val ag = DefaultActionGroup()
        ag.add(AdgActionBuild())
        ag.add(AdgActionCheck())
        //ag.add(new AdgActionRun());
        ag.add(AdgActionPrint())
        ag.addSeparator()
        val te = DefaultTreeExpander(tree)
        ag.add(CommonActionsManager.getInstance().createExpandAllAction(te, this))
        ag.add(CommonActionsManager.getInstance().createCollapseAllAction(te, this))
        val at = ActionManager.getInstance().createActionToolbar(ActionPlaces.TOOLWINDOW_TOOLBAR_BAR, ag, true)
        at.setTargetComponent(this)
        return JBUI.Panels.simplePanel(at.component)
    }
}