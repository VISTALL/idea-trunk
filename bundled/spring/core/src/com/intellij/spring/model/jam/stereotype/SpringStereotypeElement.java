package com.intellij.spring.model.jam.stereotype;

import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.jam.JamStringAttributeElement;
import com.intellij.jam.reflect.JamAnnotationMeta;
import com.intellij.jam.reflect.JamAttributeMeta;
import com.intellij.jam.reflect.JamStringAttributeMeta;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.spring.model.jam.utils.JamAnnotationTypeUtil;
import com.intellij.spring.model.jam.JamPsiClassSpringBean;
import com.intellij.spring.model.jam.qualifiers.SpringJamQualifier;
import com.intellij.spring.model.xml.SpringQualifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.ElementType;
import java.util.List;

public abstract class SpringStereotypeElement extends JamPsiClassSpringBean {

  private static final JamStringAttributeMeta.Single<String> NAME_VALUE_META = JamAttributeMeta.singleString("value");

  private final JamAnnotationMeta myMeta;

  protected final PsiClass myPsiClass;

  protected SpringStereotypeElement(@NotNull String anno, @NotNull PsiClass psiClass) {
    myPsiClass = psiClass;
    myMeta = new JamAnnotationMeta(anno);
  }


  @NotNull
  private JamStringAttributeElement<String> getNamedStringAttributeElement() {
    return myMeta.getAttribute(myPsiClass, NAME_VALUE_META);
  }

  public String getBeanName() {
    final String definedName = getAnnotationDefinedBeanName();

    if (!StringUtil.isEmptyOrSpaces(definedName)) return definedName;

    return super.getBeanName();
  }

  private String getAnnotationDefinedBeanName() {
    return getNamedStringAttributeElement().getStringValue();
  }

  @Nullable
  public SpringJamQualifier getQualifier() {
    final Module module = getModule();
    if (module == null) {
      return null;
    }

    final List<PsiClass> annotationTypeClasses = JamAnnotationTypeUtil.getQualifierAnnotationTypesWithChildren(module);

    for (PsiClass annotationTypeClass : annotationTypeClasses) {
      if (JamAnnotationTypeUtil.isAcceptedFor(annotationTypeClass, ElementType.TYPE)) {
        final PsiAnnotation annotation = AnnotationUtil.findAnnotation(getPsiElement(), annotationTypeClass.getQualifiedName());
        if(annotation != null) {
           return new SpringJamQualifier(annotation, getPsiElement(), this);
        }
      }
    }

    return null; // TODO !!!!
  }

    public SpringQualifier getSpringQualifier() {
    return getQualifier();
  }

  @Override
  public String toString() {
    final String beanName = getBeanName();
    return beanName == null ? "" : beanName;
  }
}
