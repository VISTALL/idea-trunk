package com.intellij.webBeans.jam.events;

import com.intellij.jam.JamElement;
import com.intellij.jam.annotations.JamPsiConnector;
import com.intellij.jam.reflect.JamFieldMeta;
import com.intellij.jam.reflect.JamMethodMeta;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiUtil;
import com.intellij.semantic.SemKey;
import com.intellij.webBeans.constants.WebBeansAnnoConstants;
import com.intellij.webBeans.constants.WebBeansCommonConstants;
import com.intellij.webBeans.utils.WebBeansCommonUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Set;

public abstract class WebBeansFires<T extends PsiMember> implements JamElement {
  public static final SemKey<WebBeansFires> SEM_KEY = SemKey.createKey("WebBeansFires");
  public static final JamMethodMeta<WebBeansFires> METHOD_META = new JamMethodMeta<WebBeansFires>(null, Method.class, SEM_KEY);
  public static final JamFieldMeta<WebBeansFires> FIELD_META = new JamFieldMeta<WebBeansFires>(null, Field.class, SEM_KEY);

  @Nullable
  public abstract PsiType getType();

  @Nullable
  public PsiType getEventType() {
    return PsiUtil.substituteTypeParameter(getType(), WebBeansCommonConstants.EVENT_CLASS_NAME, 0, false);
  }
  
  @NotNull
  public Set<PsiClass> getBindingTypes() {
    Module module = ModuleUtil.findModuleForPsiElement(getPsiElement());
    if (module == null) return Collections.emptySet();


    Set<PsiClass> bindingTypesClasses = WebBeansCommonUtils.getBindingTypesClasses(getPsiElement(), module);

    bindingTypesClasses.remove(getFiresPsiClass(module));

    return bindingTypesClasses;
  }

  @Nullable
  private static PsiClass getFiresPsiClass(@NotNull Module module) {
    return JavaPsiFacade.getInstance(module.getProject())
      .findClass(WebBeansAnnoConstants.FIRES_ANNOTATION, GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module));
  }

  @NotNull
  @JamPsiConnector
  public abstract T getPsiElement();

  public abstract static class Field extends WebBeansFires<PsiField> {

    public PsiType getType() {
      return getPsiElement().getType();
    }
  }

  public abstract static class Method extends WebBeansFires<PsiMethod> {

    public PsiType getType() {
      return getPsiElement().getReturnType();
    }
  }
}
