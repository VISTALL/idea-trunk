/*
 * User: anna
 * Date: 12-Jul-2007
 */
package org.jetbrains.idea.eclipse.importWizard;

import com.intellij.ide.util.ElementsChooser;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.projectImport.SelectImportedProjectsStep;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.eclipse.util.PathUtil;

import javax.swing.*;
import java.util.HashSet;
import java.util.Set;

class SelectEclipseImportedProjectsStep extends SelectImportedProjectsStep<String> {
  private static final Icon ICON_CONFLICT = IconLoader.getIcon("/actions/cancel.png");

  Set<String> duplicateNames;

  public SelectEclipseImportedProjectsStep(WizardContext context) {
    super(context);
    fileChooser.addElementsMarkListener(new ElementsChooser.ElementsMarkListener<String>() {
      public void elementMarkChanged(final String element, final boolean isMarked) {
        duplicateNames = null;
        fileChooser.repaint();
      }
    });
  }

  private boolean isInConflict(final String item) {
    calcDuplicates();
    return fileChooser.getMarkedElements().contains(item) && duplicateNames.contains(EclipseProjectFinder.findProjectName(item));
  }

  private void calcDuplicates() {
    if (duplicateNames == null) {
      duplicateNames = new HashSet<String>();
      Set<String> usedNames = new HashSet<String>();
      for (String model : fileChooser.getMarkedElements()) {
        final String projectName = EclipseProjectFinder.findProjectName(model);
        if (!usedNames.add(projectName)) {
          duplicateNames.add(projectName);
        }
      }
    }
  }

  protected String getElementText(final String item) {
    StringBuilder stringBuilder = new StringBuilder();
    final String projectName = EclipseProjectFinder.findProjectName(item);
    stringBuilder.append(projectName);
    String relPath = PathUtil.getRelative(((EclipseImportBuilder)getBuilder()).getParameters().root, item);
    if (!relPath.equals(".") && !relPath.equals(projectName)) {
      stringBuilder.append(" (").append(relPath).append(")");
    }
    return stringBuilder.toString();
  }

  @Nullable
  protected Icon getElementIcon(final String item) {
    return isInConflict(item) ? ICON_CONFLICT : null;
  }

  public void updateStep() {
    super.updateStep();
    duplicateNames = null;
  }

  public boolean validate() throws ConfigurationException {
    calcDuplicates();
    if (!duplicateNames.isEmpty()) {
      throw new ConfigurationException("Duplicated names found:" + StringUtil.join(ArrayUtil.toStringArray(duplicateNames), ","), "Unable to proceed");
    }
    return super.validate();
  }

  @NonNls
  public String getHelpId() {
    return "reference.dialogs.new.project.import.eclipse.page2";
  }
}