package com.intellij.spring.model.actions.generate;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.spring.SpringManager;
import com.intellij.spring.SpringModel;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.actions.generate.AbstractDomGenerateProvider;

/**
 * User: Sergey.Vasiliev
 */
public abstract class BasicSpringDomGenerateProvider<T extends DomElement> extends AbstractDomGenerateProvider<T> {

  protected BasicSpringDomGenerateProvider(final String description, final Class<T> tClass) {
    super(description, tClass);
  }

  protected BasicSpringDomGenerateProvider(final String description, final Class<T> tClass, final String mappingId) {
    super(description, tClass, mappingId);
  }

  protected DomElement getParentDomElement(final Project project, final Editor editor, final PsiFile file) {
    final SpringModel springModel = SpringManager.getInstance(project).getLocalSpringModel((XmlFile)file);
    return springModel == null ? null : springModel.getMergedModel();
  }
}
