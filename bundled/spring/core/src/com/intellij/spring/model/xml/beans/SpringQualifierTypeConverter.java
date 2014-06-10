package com.intellij.spring.model.xml.beans;

import com.intellij.util.xml.PsiClassConverter;
import com.intellij.util.xml.ConvertContext;
import com.intellij.psi.PsiClass;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.openapi.project.Project;

/**
 * @author Dmitry Avdeev
 */
public class SpringQualifierTypeConverter extends PsiClassConverter {

  public PsiClass fromString(final String s, final ConvertContext context) {
    if (s == null) {
      final Project project = context.getPsiManager().getProject();
      return JavaPsiFacade.getInstance(project).findClass("org.springframework.beans.factory.annotation.Qualifier",
                                                          GlobalSearchScope.allScope(project));
    }
    return super.fromString(s, context);
  }
}
