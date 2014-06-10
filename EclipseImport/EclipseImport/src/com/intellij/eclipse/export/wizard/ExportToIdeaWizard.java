package com.intellij.eclipse.export.wizard;

import com.intellij.eclipse.export.exporter.Exporter;
import com.intellij.eclipse.export.model.IdeaProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;

public class ExportToIdeaWizard extends Wizard implements IExportWizard {
  private ExportParameters params;

  public void init(IWorkbench ignore1, IStructuredSelection ignore2) {
    setWindowTitle("Export wizard");

    params = new ExportParameters();
    addPage(new ExportParametersPage(params));
  }

  public boolean performFinish() {
    try {
      IdeaProject project = BuilderUtil.buildIdeaProject(
        params.getProjectName(),
        params.getProjectsToExport().toArray(new IProject[0]));

      Exporter exporter = new Exporter(project,
                                       params.getOutputDirectory(),
                                       params.shouldCopyContent(),
                                       params.shouldUsePathVariables(),
                                       params.shouldDeclareLibraries());
      exporter.export();
    }
    catch (Exception e) {
      showErrorDialog(e);
      e.printStackTrace();
    }

    return true;
  }

  private void showErrorDialog(Exception e) {
    MessageDialog dialog = new MessageDialog(getShell(), "Error", null, e.getMessage(),
                                             MessageDialog.ERROR, new String[]{"Close"}, 0);
    dialog.open();
  }
}