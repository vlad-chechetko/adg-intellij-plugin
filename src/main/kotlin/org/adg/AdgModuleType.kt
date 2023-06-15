package org.adg

import com.intellij.icons.AllIcons
import com.intellij.ide.util.projectWizard.ModuleWizardStep
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.module.ModuleTypeManager
import com.intellij.openapi.roots.ui.configuration.ModulesProvider
import org.jetbrains.annotations.Nls
import javax.swing.Icon

class AdgModuleType : ModuleType<AdgModuleBuilder>(ID) {
    override fun createModuleBuilder(): AdgModuleBuilder {
        return AdgModuleBuilder()
    }

    override fun getName(): @Nls(capitalization = Nls.Capitalization.Title) String {
        return "Autodoc generator"
    }

    override fun getDescription(): @Nls(capitalization = Nls.Capitalization.Sentence) String {
        return "Module allows automatically generate architecture documentation by it's diagram"
    }

    //    public @NotNull Icon getNodeIcon(boolean isOpened) {
    //        return IconLoader.getIcon("/icons/adg.png", this.getClass());
    //    }
    override fun getNodeIcon(isOpened: Boolean): Icon {
        return AllIcons.Actions.Colors
    }

    override fun createWizardSteps(
        wizardContext: WizardContext,
        moduleBuilder: AdgModuleBuilder,
        modulesProvider: ModulesProvider
    ): Array<ModuleWizardStep> {
        return super.createWizardSteps(wizardContext, moduleBuilder, modulesProvider)
    }

    companion object {
        private const val ID = "MY_MODULE"
        val instance: AdgModuleType
            get() = ModuleTypeManager.getInstance().findByID(ID) as AdgModuleType
    }
}