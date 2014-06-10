package com.advancedtools.webservices.utils;

import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.impl.TemplateImpl;
import com.intellij.codeInsight.template.impl.TemplateSettings;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataConstants;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;

/**
 * @author maxim
 * Date: 21.01.2006
 */
public abstract class BaseWSGenerateAction extends BaseWSAction {

  public void actionPerformed(AnActionEvent e) {
    final Editor editor = (Editor)e.getDataContext().getData(DataConstants.EDITOR);
    final Project project = (Project) e.getDataContext().getData(DataConstants.PROJECT);
    run(editor, project);
  }

  public void run(Editor editor, Project project) {
    final TemplateImpl template = TemplateSettings.getInstance().getTemplate(getTemplateActionName());

    if (template != null) {
      TemplateManager.getInstance(project).startTemplate(editor, template);
    } else {
      new Exception("Unexpected branch").printStackTrace();
    }
  }

  protected abstract String getTemplateActionName();

  public void update(AnActionEvent e) {
    super.update(e);

    BaseWSGenerateActionGroup.updatePresentation(e);
  }
}
