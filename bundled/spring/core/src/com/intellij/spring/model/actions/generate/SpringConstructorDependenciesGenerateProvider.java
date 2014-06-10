package com.intellij.spring.model.actions.generate;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiFile;
import com.intellij.spring.SpringBundle;
import com.intellij.spring.model.SpringUtils;
import com.intellij.spring.model.xml.beans.ConstructorArg;
import com.intellij.spring.model.xml.beans.SpringBean;
import com.intellij.spring.model.xml.beans.SpringInjection;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomElementNavigationProvider;
import com.intellij.util.xml.actions.generate.AbstractDomGenerateProvider;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SpringConstructorDependenciesGenerateProvider extends AbstractDomGenerateProvider<ConstructorArg> {
  public SpringConstructorDependenciesGenerateProvider() {
    super(SpringBundle.message("spring.generate.constructor.dependencies"), ConstructorArg.class);
  }

  protected DomElement getParentDomElement(final Project project, final Editor editor, final PsiFile file) {
    return SpringUtils.getSpringBeanForCurrentCaretPosition(editor, file);
  }

  public ConstructorArg generate(@Nullable final DomElement parent, final Editor editor) {
    if ( !(parent instanceof SpringBean)) return null;
    final List<Pair<SpringInjection,SpringGenerateTemplatesHolder>> list = GenerateSpringBeanDependenciesUtil.generateDependenciesFor((SpringBean)parent, false);

    for (Pair<SpringInjection, SpringGenerateTemplatesHolder> pair : list) {
      pair.getSecond().runTemplates();
    }
    return  list.size() > 0 ? (ConstructorArg)list.get(0).getFirst() : null;
  }

  protected void doNavigate(final DomElementNavigationProvider navigateProvider, final DomElement element) {
   //super.doNavigate(navigateProvider, element);
  }
  
  public boolean isAvailableForElement(@NotNull final DomElement contextElement) {
    return contextElement instanceof SpringBean && GenerateSpringBeanDependenciesUtil.acceptBean((SpringBean)contextElement, false);
  }

}
