package com.intellij.spring.model.actions.patterns.frameworks.ui;

import com.intellij.codeInsight.template.Template;
import com.intellij.facet.ui.libraries.LibrariesValidationComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.Disposer;
import com.intellij.spring.SpringBundle;

import javax.swing.*;
import java.util.LinkedList;
import java.util.List;

public class ChooseTemplatesDialogWrapper extends DialogWrapper {
  private final ChooseTemplatesForm myTemplatesForm;

  public ChooseTemplatesDialogWrapper(Project project, List<TemplateInfo> infos, LibrariesInfo libInfo, String frameworkTitle) {
    super(project, true);

    myTemplatesForm = new ChooseTemplatesForm(infos, libInfo);
    myTemplatesForm.getLibrariesValidationComponent().addValidityListener(new LibrariesValidationComponent.ValidityListener() {
      public void valididyChanged(final boolean isValid) {
        setOKActionEnabled(isValid);
      }
    });
    setOKActionEnabled(myTemplatesForm.getLibrariesValidationComponent().isValid());
    setTitle(SpringBundle.message("spring.choose.bean.templates.dialog.title", frameworkTitle));

    init();
  }

  protected Action[] createActions() {
    return new Action[]{getOKAction(), getCancelAction()};
  }

  protected JComponent createCenterPanel() {
    return myTemplatesForm.getComponent();
  }

  public JComponent getPreferredFocusedComponent() {
    return myTemplatesForm.getComponent();
  }

  public ChooseTemplatesForm getTemplatesForm() {
    return myTemplatesForm;
  }

  public List<Template> getSelectedTemplates() {
    List<Template> templates = new LinkedList<Template>();
    for (TemplateInfo info : myTemplatesForm.getTemplateInfos()) {
      if (info.isAccepted()) {
        templates.add(info.getTemplate());
      }
    }
    return templates;
  }

  protected void dispose() {
    Disposer.dispose(myTemplatesForm);
    super.dispose();
  }


}
