package com.intellij.webBeans.jam.events;

import com.intellij.jam.JamElement;
import com.intellij.jam.reflect.JamParameterMeta;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.psi.*;
import com.intellij.psi.ref.AnnotationChildLink;
import com.intellij.webBeans.constants.WebBeansAnnoConstants;
import com.intellij.webBeans.utils.WebBeansCommonUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class WebBeansObserves implements JamElement {
  public static final JamParameterMeta<WebBeansObserves> META = new JamParameterMeta<WebBeansObserves>(WebBeansObserves.class);

  private final PsiParameter myParameter;
  private final PsiRef<PsiAnnotation> myAnno;


  public WebBeansObserves(PsiParameter psiParameter) {
    myParameter = psiParameter;
    myAnno = AnnotationChildLink.createRef(psiParameter, WebBeansAnnoConstants.OBSERVES_ANNOTATION);
  }

  @Nullable
  public PsiType getType() {
    return myParameter.getType();
  }

  @NotNull
  public Set<PsiClass> getBindingTypes() {
    Module module = ModuleUtil.findModuleForPsiElement(getPsiElement());

    return WebBeansCommonUtils.getBindingTypesClasses(getPsiElement(), module);

  }

  public boolean isValid() {
    return myAnno.isValid();
  }

  @NotNull
  public PsiParameter getPsiElement() {
    return myParameter;
  }

  @Nullable
  public PsiAnnotation getAnnotation() {
    return myAnno.getPsiElement();
  }
}
