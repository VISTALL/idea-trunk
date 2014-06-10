package com.intellij.webBeans.jam.interceptor;

import com.intellij.jam.JamElement;
import com.intellij.jam.annotations.JamPsiConnector;
import com.intellij.jam.reflect.JamAnnotationMeta;
import com.intellij.jam.reflect.JamClassMeta;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.webBeans.constants.WebBeansAnnoConstants;
import com.intellij.webBeans.utils.WebBeansCommonUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Set;

public abstract class WebBeansInterceptor implements JamElement {

  public static final JamClassMeta<WebBeansInterceptor> META = new JamClassMeta<WebBeansInterceptor>(WebBeansInterceptor.class);

  private final JamAnnotationMeta myMeta = new JamAnnotationMeta(WebBeansAnnoConstants.INTERCEPTOR_ANNOTATION);

  public Set<PsiClass> getInterceptorBindingTypes() {
     Module module = ModuleUtil.findModuleForPsiElement(getPsiElement());
    if (module == null) return Collections.emptySet();

    return WebBeansCommonUtils.getInterceptorBindingTypesClasses(getPsiElement(), module);
  }

  @NotNull
  @JamPsiConnector
  public abstract PsiClass getPsiElement();

  @Nullable
  public PsiAnnotation getAnnotation() {
    return myMeta.getAnnotation(getPsiElement());
  }

}

