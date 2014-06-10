package com.intellij.eclipse.export.wizard;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import java.util.ArrayList;
import java.util.List;

public class ExportParametersPage extends WizardPage {
  private Text outputDirectoryPathField;
  private Text projectNameField;
  private Table projectsTable;
  private Button shouldCopyContentButton;
  private Button shouldUsePathVariablesButton;
  private Button declareLibrariesAsProjectButton;
  private Button declareLibrariesAsGlobalButton;

  private ExportParameters model;
  private boolean loading = false;

  public ExportParametersPage(ExportParameters params) {
    super("", "IntelliJ IDEA Project Export Wizard",  ExporterImages.getImageDescriptor());
    setDescription("Please specify export parameters");
    model = params;
  }

  public void createControl(Composite parent) {
    Composite container = createGrid(parent, 1, GridData.FILL_BOTH);

    createTopControls(container);
    createProjectsTable(container);
    createParamsControls(container);

    setControl(container);

    load();
    save();
  }

  private void createTopControls(Composite parent) {
    Composite container = createGrid(parent, 2);

    createOutputDirectoryControls(container);
    createProjectNameControls(container);
  }

  private void createOutputDirectoryControls(Composite parent) {
    createLabel(parent, "Output directory");

    Composite leftContainer = createGrid(parent, 2);

    outputDirectoryPathField = createText(leftContainer);

    Button selectButton = new Button(leftContainer, SWT.NONE);
    selectButton.setText("...");
    selectButton.setLayoutData(new GridData(GridData.END));

    selectButton.addSelectionListener(new SelectionListener() {
      public void widgetSelected(SelectionEvent e) {
        selectOutputDirectory();
        save();
      }

      public void widgetDefaultSelected(SelectionEvent e) { }
    });
  }

  private void createProjectNameControls(Composite parent) {
    createLabel(parent, "Project name");
    Composite leftContainer = createGrid(parent, 1);
    projectNameField = createText(leftContainer);
  }

  private void createProjectsTable(Composite container) {
    projectsTable = new Table(container, SWT.BORDER | SWT.CHECK);
    projectsTable.setLayoutData(new GridData(GridData.FILL_BOTH));
    projectsTable.addSelectionListener(new DefaultSelectionListener());
  }

  private void createParamsControls(Composite parent) {
    Group paramGroup = new Group(parent, SWT.NONE);
    paramGroup.setLayout(new GridLayout(2, false));
    paramGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
                                          | GridData.VERTICAL_ALIGN_BEGINNING));
    paramGroup.setText("Parameters");

    Composite leftGrid = createGrid(paramGroup, 1);
    shouldCopyContentButton = createCheckBox(leftGrid, "Copy content");
    shouldUsePathVariablesButton = createCheckBox(leftGrid, "Export path variables");

    Composite rightGrid = createGrid(paramGroup, 2);
    createLabel(rightGrid, "Export libraries as");
    declareLibrariesAsProjectButton = createRadioButton(rightGrid, "project");

    createLabel(rightGrid, "");
    declareLibrariesAsGlobalButton = createRadioButton(rightGrid, "global");
  }

  private Label createLabel(Composite parent, String text) {
    Label result = new Label(parent, SWT.NONE);
    result.setText(text);
    result.setLayoutData(new GridData(GridData.BEGINNING));
    return result;
  }

  private Text createText(Composite parent) {
    Text result = new Text(parent, SWT.BORDER);
    result.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    result.addModifyListener(new DefaultModifyListener());
    return result;
  }

  private Button createCheckBox(Composite parent, String text) {
    Button result = new Button(parent, SWT.CHECK);
    result.setText(text);
    result.setLayoutData(new GridData(GridData.BEGINNING));
    result.addSelectionListener(new DefaultSelectionListener());
    return result;
  }

  private Button createRadioButton(Composite parent, String text) {
    Button result = new Button(parent, SWT.RADIO);
    result.setText(text);
    result.setLayoutData(new GridData(GridData.BEGINNING));
    result.addSelectionListener(new DefaultSelectionListener());
    return result;
  }

  private Composite createGrid(Composite parent, int rows) {
    return createGrid(parent, rows, GridData.FILL_HORIZONTAL);
  }

  private Composite createGrid(Composite parent, int rows, int style) {
    Composite result = new Composite(parent, SWT.NONE);

    GridLayout layout = new GridLayout(rows, false);
    layout.marginHeight = 0;
    layout.marginWidth = 0;
    result.setLayout(layout);
    result.setLayoutData(new GridData(style));

    return result;
  }

  private void selectOutputDirectory() {
    DirectoryDialog dialog = new DirectoryDialog(getShell());
    String result = dialog.open();
    if (result != null) outputDirectoryPathField.setText(result);
  }

  private void load() {
    loading = true;

    outputDirectoryPathField.setText(model.getOutputDirectory());
    projectNameField.setText(model.getProjectName());

    loadProjectsTable();

    shouldCopyContentButton.setSelection(model.shouldCopyContent());
    shouldUsePathVariablesButton.setSelection(model.shouldUsePathVariables());

    declareLibrariesAsProjectButton.setSelection(model.shouldDeclareLibraries());
    declareLibrariesAsGlobalButton.setSelection(!model.shouldDeclareLibraries());

    loading = false;
  }

  private void loadProjectsTable() {
    WorkbenchLabelProvider labels = new WorkbenchLabelProvider();

    for (IProject project : model.getProjects()) {
      TableItem item = new TableItem(projectsTable, SWT.NONE);

      item.setData(project);
      item.setText(project.getName());
      item.setImage(labels.getImage(project));
      item.setChecked(model.getProjectsToExport().contains(project));
    }
  }

  private void save() {
    if (loading) return;

    model.setOutputDirectory(outputDirectoryPathField.getText());
    model.setProjectName(projectNameField.getText());

    saveProjectsTable();

    model.setShouldCopyContent(shouldCopyContentButton.getSelection());
    model.setShouldUsePathVariables(shouldUsePathVariablesButton.getSelection());
    model.setShouldDeclareLibraries(declareLibrariesAsProjectButton.getSelection());

    setErrorMessage(model.getErrorMessage());
    setPageComplete(model.isValid());
  }

  private void saveProjectsTable() {
    List<IProject> selectedProjects = new ArrayList<IProject>();

    for (TableItem item : projectsTable.getItems()) {
      if (item.getChecked()) selectedProjects.add((IProject)item.getData());
    }

    model.setProjectsToExport(selectedProjects);
  }

  @Override
  public boolean isPageComplete() {
    return model.isValid();
  }

  private class DefaultModifyListener implements ModifyListener {
    public void modifyText(ModifyEvent modifyEvent) {
      save();
    }
  }

  private class DefaultSelectionListener implements SelectionListener {
    public void widgetSelected(SelectionEvent selectionEvent) {
      save();
    }

    public void widgetDefaultSelected(SelectionEvent selectionEvent) { }
  }
}