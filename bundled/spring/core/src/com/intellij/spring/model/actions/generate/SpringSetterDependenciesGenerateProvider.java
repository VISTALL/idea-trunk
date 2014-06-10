package com.intellij.spring.model.actions.generate;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiFile;
import com.intellij.spring.SpringBundle;
import com.intellij.spring.model.SpringUtils;
import com.intellij.spring.model.xml.beans.SpringBean;
import com.intellij.spring.model.xml.beans.SpringInjection;
import com.intellij.spring.model.xml.beans.SpringProperty;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomElementNavigationProvider;
import com.intellij.util.xml.actions.generate.AbstractDomGenerateProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SpringSetterDependenciesGenerateProvider extends AbstractDomGenerateProvider<SpringProperty> {
  public SpringSetterDependenciesGenerateProvider() {
    super(SpringBundle.message("spring.generate.setter.dependencies"), SpringProperty.class);
  }

  protected DomElement getParentDomElement(final Project project, final Editor editor, final PsiFile file) {
    return SpringUtils.getSpringBeanForCurrentCaretPosition(editor, file);
  }

  public SpringProperty generate(@Nullable final DomElement parent, final Editor editor) {

    if (!(parent instanceof SpringBean)) return null;
    final List<Pair<SpringInjection, SpringGenerateTemplatesHolder>> list =
      GenerateSpringBeanDependenciesUtil.generateDependenciesFor((SpringBean)parent, true);

    SpringGenerateTemplatesHolder merged = new SpringGenerateTemplatesHolder(editor.getProject() );
    for (Pair<SpringInjection, SpringGenerateTemplatesHolder> pair : list) {
      merged.addAll(pair.getSecond());
    }
    merged.runTemplates();

    return list.size() > 0 ? (SpringProperty)list.get(0).getFirst() : null;
  }

  protected void doNavigate(final DomElementNavigationProvider navigateProvider, final DomElement element) {
  }

  public boolean isAvailableForElement(@NotNull final DomElement contextElement) {
    return contextElement instanceof SpringBean && GenerateSpringBeanDependenciesUtil.acceptBean((SpringBean)contextElement, true);
  }
}
