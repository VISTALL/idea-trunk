package org.jetbrains.plugins.groovy.mvc;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.DumbAware;

/**
 * @author ilyas
 */
public class MvcRunTarget extends AnAction implements DumbAware {

  public void actionPerformed(final AnActionEvent event) {
    final Module module = event.getData(DataKeys.MODULE);
    if (module == null) return;

    final MvcFramework framework = MvcModuleStructureSynchronizer.getFramework(module);
    assert framework != null;

    MvcRunTargetDialog dialog = new MvcRunTargetDialog(module, framework);
    dialog.show();
    if (!dialog.isOK()) {
      return;
    }
    String[] targetArgs = dialog.getTargetArguments();
    ProcessBuilder pb = framework.createCommand(module, false, targetArgs);
    MvcConsole.getInstance(module.getProject()).executeProcess(module, pb, null, false);
  }

  public void update(AnActionEvent event) {
    final Module module = event.getData(DataKeys.MODULE);
    final MvcFramework framework = MvcModuleStructureSynchronizer.getFramework(module);
    final boolean enabled = module != null && framework != null && framework.hasSupport(module);
    event.getPresentation().setEnabled(enabled);
    event.getPresentation().setVisible(enabled);
    if (enabled) {
      event.getPresentation().setText("Run " + framework.getDisplayName() + " target...");
    }
  }


}
