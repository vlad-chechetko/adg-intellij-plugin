package org.adg

import com.intellij.ide.util.projectWizard.ModuleBuilder
import com.intellij.ide.util.projectWizard.ModuleWizardStep
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.openapi.Disposable
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.roots.ModifiableRootModel
import com.intellij.openapi.ui.ComboBox
import com.intellij.util.ui.FormBuilder
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JTextField

class AdgModuleBuilder : ModuleBuilder() {
    private var moduleProps: MuduleProps? = null
    @Throws(ConfigurationException::class)
    override fun setupRootModel(modifiableRootModel: ModifiableRootModel) {
        LOG.info("setupRootModel")
        createContentRootDir(".work", modifiableRootModel)
        createContentRootDir("diagrams", modifiableRootModel)
        if ("Architecture - с4 short".equals(moduleProps?.templateName, ignoreCase = true)) {
            copyTemplate("template_short.adoc", ".work", modifiableRootModel)
        } else if ("Architecture - с4 full".equals(moduleProps?.templateName, ignoreCase = true)) {
            copyTemplate("template_full.adoc", ".work", modifiableRootModel)
        } else {
            copyTemplate("template_short.adoc", ".work", modifiableRootModel)
            copyTemplate("template_full.adoc", ".work", modifiableRootModel)
        }
        createArchitectureFile(modifiableRootModel)
        createDiagramFile(modifiableRootModel)
        doAddContentEntry(modifiableRootModel)
    }

    private fun createDiagramFile(modifiableRootModel: ModifiableRootModel) {
        var fileName: String? = moduleProps?.diagramFileName
        if (fileName == null) {
            fileName = "architecture.drawio"
        }
        val projectPath = modifiableRootModel.module.project.basePath
        val f = File("$projectPath/diagrams/$fileName")
        try {
            val success = f.createNewFile()
            if (!success) {
                LOG.error("Can't create file $fileName")
                throw RuntimeException("Can't create file $fileName")
            }
        } catch (e: IOException) {
            LOG.error("Can't create file $fileName")
            throw RuntimeException(e)
        }
    }

    private fun createArchitectureFile(modifiableRootModel: ModifiableRootModel) {
        var fileName: String? = moduleProps?.docFileName
        if (fileName == null) {
            fileName = "architecture.adoc"
        }
        val projectPath = modifiableRootModel.module.project.basePath
        val f = File("$projectPath/$fileName")
        try {
            val success = f.createNewFile()
            if (!success) {
                LOG.error("Can't create file $fileName")
                throw RuntimeException("Can't create file $fileName")
            }
        } catch (e: IOException) {
            LOG.error("Can't create file $fileName")
            throw RuntimeException(e)
        }
    }

    private fun copyTemplate(templateFileName: String, targetDir: String, modifiableRootModel: ModifiableRootModel) {
        val projectPath = modifiableRootModel.module.project.basePath
        val i = this.javaClass.getResourceAsStream("/templates/$templateFileName")
        try {
            Files.copy(i, Paths.get("$projectPath/$targetDir/$templateFileName"), StandardCopyOption.REPLACE_EXISTING)
        } catch (e: IOException) {
            LOG.error("Can't copy temp[late file $templateFileName to working directory $targetDir")
            throw RuntimeException(e)
        }
    }

    private fun createContentRootDir(name: String, modifiableRootModel: ModifiableRootModel) {
        val projectPath = modifiableRootModel.module.project.basePath
        val f = File("$projectPath/$name")
        val success = f.mkdir()
        if (!success) {
            LOG.error("Can't create directory $name")
            throw RuntimeException("Can't create directory $name")
        } else {
            modifiableRootModel.addContentEntry("$projectPath/.work")
        }
    }

    override fun getModuleType(): ModuleType<*> {
        return AdgModuleType.Companion.instance
    }

    override fun getCustomOptionsStep(
        context: WizardContext,
        parentDisposable: Disposable
    ): ModuleWizardStep? {
        moduleProps = MuduleProps()
        return object : ModuleWizardStep() {
            private var templateList: ComboBox<*>? = null
            private var docNameField: JTextField? = null
            private var diagramNameField: JTextField? = null
            override fun getComponent(): JComponent {
                //String[] templates = { "", "template_short.adoc", "template_full.adoc"};
                val templates: Array<String?> = arrayOf("Architecture - с4 short")
                templateList = ComboBox<Any?>(templates)
                docNameField = JTextField("architecture.adoc")
                docNameField!!.isEditable = false
                diagramNameField = JTextField("c4context.drawio")
                diagramNameField!!.isEditable = false
                val fb = FormBuilder()
                fb.addLabeledComponent("Project template", templateList as ComboBox<Any?>)
                fb.addLabeledComponent("Architecture file name", docNameField!!)
                fb.addLabeledComponent("Context diagram file name", diagramNameField!!)
                fb.addComponentFillVertically(JLabel(""), 0)
                val p = fb.panel
                p.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
                return p
            }

            override fun updateDataModel() {
                moduleProps!!.docFileName = docNameField!!.text
                moduleProps!!.diagramFileName = diagramNameField!!.text
                if (templateList!!.selectedItem != null) {
                    moduleProps!!.templateName = templateList!!.selectedItem.toString()
                }
            }
        }
    }

    private inner class MuduleProps {
        var templateName: String? = null
        var docFileName: String? = null
        var diagramFileName: String? = null
    }

    companion object {
        private val LOG = Logger.getInstance(
            AdgModuleBuilder::class.java
        )
    }
}