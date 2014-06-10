package com.intellij.spring.model.actions.patterns.frameworks;

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateEditingAdapter;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.facet.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.ReadonlyStatusHandler;
import com.intellij.psi.xml.XmlFile;
import com.intellij.spring.model.SpringUtils;
import com.intellij.spring.model.actions.patterns.frameworks.ui.ChooseTemplatesDialogWrapper;
import com.intellij.spring.model.actions.patterns.frameworks.ui.LibrariesInfo;
import com.intellij.spring.model.actions.patterns.frameworks.ui.TemplateInfo;
import com.intellij.spring.model.xml.beans.SpringBean;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class AbstractFrameworkIntegrationAction extends FrameworkIntegrationAction {

  protected void generateSpringBeans(final Module module, final Editor editor, final XmlFile xmlFile) {

    final ChooseTemplatesDialogWrapper dialogWrapper =
      new ChooseTemplatesDialogWrapper(module.getProject(), getTemplateInfos(module), getLibrariesInfo(module), getDescription());

    dialogWrapper.show();

    if (dialogWrapper.getExitCode() == DialogWrapper.OK_EXIT_CODE) {
      final ReadonlyStatusHandler.OperationStatus status = ReadonlyStatusHandler.getInstance(xmlFile.getProject()).ensureFilesWritable(xmlFile.getVirtualFile());
      if (status.hasReadonlyFiles()) return;

      new WriteCommandAction(module.getProject()) {
        protected void run(Result result) throws Throwable {
          addFacet(module);
          dialogWrapper.getTemplatesForm().getLibrariesValidationComponent().setupLibraries();

          moveCaretIfNeeded(module.getProject(), editor, xmlFile);

          runTemplates(module.getProject(), editor, dialogWrapper.getSelectedTemplates(), 0);
        }
      }.execute();
    }
  }

  protected void addFacet(final Module module) {
    if (module == null) return;

    final String facetId = getFacetId();
    if (!StringUtil.isEmptyOrSpaces(facetId)) {

      final FacetManager facetManager = FacetManager.getInstance(module);
      final FacetType<?,?> type = FacetTypeRegistry.getInstance().findFacetType(facetId);

      if (type != null) {

        if (facetManager.getFacetByType(type.getId()) == null) {
          final ModifiableFacetModel model = facetManager.createModifiableModel();

          final Facet facet = facetManager.addFacet(type, type.getDefaultFacetName(), null);

          model.addFacet(facet);
          model.commit();
        }
      }
    }
  }

  @Nullable
  protected String getFacetId() {
    return null;
  }

  protected abstract LibrariesInfo getLibrariesInfo(final Module module);

  protected abstract List<TemplateInfo> getTemplateInfos(final Module module);

  private static void moveCaretIfNeeded(final Project project, final Editor editor, final XmlFile xmlFile) {
    final SpringBean bean = SpringUtils.getSpringBeanForCurrentCaretPosition(editor, xmlFile);
    if (bean != null) {
      SpringBean springBean = SpringUtils.getTopLevelBean(bean);

      if (springBean.getXmlTag() != null) {
        final TextRange range = springBean.getXmlTag().getTextRange();
        int offset = range.getEndOffset();
        editor.getCaretModel().moveToOffset(offset);
        editor.getScrollingModel().scrollToCaret(ScrollType.CENTER);
      }
    }
  }

  private static void runTemplates(final Project project, final Editor editor, final List<Template> templates, final int index) {
    Template template = templates.get(index);
    template.setToReformat(true);

    TemplateManager.getInstance(project).startTemplate(editor, template, new TemplateEditingAdapter() {
      public void templateFinished(Template template) {
        if (index + 1 < templates.size()) {
          ApplicationManager.getApplication().invokeLater(new Runnable() {
            public void run() {
              new WriteCommandAction(project) {
                protected void run(Result result) throws Throwable {
                  runTemplates(project, editor, templates, index + 1);
                }
              }.execute();
            }
          });
        }
      }
    });
  }

}
