package org.jetbrains.plugins.groovy.mvc;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.ProjectWizardStepFactory;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.groovy.config.GroovyAwareModuleBuilder;
import org.jetbrains.plugins.groovy.config.GroovySupportConfigurable;
import org.jetbrains.plugins.groovy.config.LibraryManager;
import org.jetbrains.plugins.groovy.config.ui.GroovyFacetEditor;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import java.awt.*;

/**
 * @author peter
 */
public class MvcModuleBuilder extends GroovyAwareModuleBuilder {
  private final MvcFramework myFramework;
  private final Class<? extends LibraryManager> myManagerClass;

  protected MvcModuleBuilder(MvcFramework framework, Icon bigIcon, @NotNull Class<? extends LibraryManager> managerClass) {
    super(framework.getFrameworkName(), framework.getDisplayName() + " Application", "Create a new " + framework.getDisplayName() + " application", bigIcon);
    myFramework = framework;
    myManagerClass = managerClass;
  }

  @Override
  public ModuleWizardStep[] createWizardSteps(WizardContext wizardContext, ModulesProvider modulesProvider) {
    final GroovySupportConfigurable supportConfigurable = new GroovySupportConfigurable(new GroovyFacetEditor(wizardContext.getProject(), myManagerClass,
                                                                                                              myFramework.getSdkHomePropertyName()));
    final ModuleWizardStep sdkStep = new ModuleWizardStep() {
      @Override
      public JComponent getComponent() {
        final JComponent component = supportConfigurable.getComponent();
        final JPanel panel = new JPanel(new BorderLayout());
        panel.add(component, BorderLayout.NORTH);

        final JLabel caption = new JLabel("Please specify " + myFramework.getDisplayName() + " SDK");
        caption.setFont(caption.getFont().deriveFont(14.0f));
        caption.setBorder(new EmptyBorder(0, 0, 10, 0));

        final JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(5, 5, 5, 5)));
        mainPanel.add(caption, BorderLayout.NORTH);
        mainPanel.add(panel, BorderLayout.CENTER);
        return mainPanel;
      }

      @Override
      public void updateDataModel() {
      }
    };

    addModuleConfigurationUpdater(new ModuleConfigurationUpdater() {
      @Override
      public void update(@NotNull Module module, @NotNull ModifiableRootModel rootModel) {
        supportConfigurable.addGroovySupport(module, rootModel);
        module.putUserData(MvcFramework.CREATE_APP_STRUCTURE, Boolean.TRUE);
      }
    });

    return new ModuleWizardStep[]{ProjectWizardStepFactory.getInstance().createProjectJdkStep(wizardContext), sdkStep};
  }

}
