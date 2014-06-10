package com.intellij.spring.model.converters;

import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.xml.ConvertContext;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class SpringBeanResolveConverterForDefiniteClasses extends SpringBeanResolveConverter {

  @Nullable
  protected abstract String[] getClassNames(final ConvertContext context) ;

  @Nullable
  public List<PsiClassType> getRequiredClasses(final ConvertContext context) {
    final List<PsiClassType> required = new ArrayList<PsiClassType>();
    final PsiManager psiManager = context.getPsiManager();
    final String[] strings = getClassNames(context);

    if (strings == null || strings.length == 0) return null;

    for (String className : strings) {
      final PsiClass psiClass = JavaPsiFacade.getInstance(psiManager.getProject())
          .findClass(className, GlobalSearchScope.allScope(psiManager.getProject()));

      if (psiClass != null) {
        required.add(JavaPsiFacade.getInstance(psiClass.getProject()).getElementFactory().createType(psiClass));
      }
    }


    return required;
  }
}
