package com.intellij.webBeans.beans;

import com.intellij.psi.*;
import com.intellij.webBeans.constants.WebBeansAnnoConstants;
import com.intellij.webBeans.utils.WebBeansCommonUtils;
import com.intellij.webBeans.manager.WebBeansManager;
import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.Function;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;

/**
 * User: Sergey.Vasiliev
 */
public abstract class WebBeanPsiClassDescriptor extends AbstractWebBeanDescriptor<PsiClass> {
  private final PsiClass myPsiClass;

  protected WebBeanPsiClassDescriptor(@NotNull PsiClass psiClass) {
    myPsiClass = psiClass;
  }

  @NotNull
  public PsiClass getAnnotatedItem() {
    return myPsiClass;
  }

  protected PsiClass getDefaultDeploymentType() {
    return getAnnotationClass(WebBeansAnnoConstants.PRODUCTION_ANNOTATION);
  }

  @NotNull
  public List<PsiMethod> getInitializerConstructors() {
    final List<PsiMethod> constructors = new ArrayList<PsiMethod>();

    PsiMethod[] methods = getAnnotatedItem().getConstructors();

    for (PsiMethod psiMethod : methods) {
      if (AnnotationUtil.isAnnotated(psiMethod, WebBeansAnnoConstants.INITIALIZER_ANNOTATION, true)) {
        constructors.add(psiMethod);
      }
    }

    return constructors;
  }

  @NotNull
  public List<PsiMethod> getInitializerMethods() {
    final List<PsiMethod> initializers = new ArrayList<PsiMethod>();

    PsiMethod[] methods = getAnnotatedItem().getAllMethods();

    for (PsiMethod psiMethod : methods) {
      if (AnnotationUtil.isAnnotated(psiMethod, WebBeansAnnoConstants.INITIALIZER_ANNOTATION, true)) {
        initializers.add(psiMethod);
      }
    }

    return initializers;
  }

  @NotNull
  public List<ProducerBeanDescriptor> getProducerWebBeansDescriptors() {
    final WebBeansManager service = WebBeansManager.getService(getModule());
    return ContainerUtil.mapNotNull(collectProducesPsiMembers(), new Function<PsiMember, ProducerBeanDescriptor>() {
      public ProducerBeanDescriptor fun(PsiMember psiMember) {
        return service.createProducerWebBeanDescriptor(psiMember);
      }
    });
  }

  private List<PsiMember> collectProducesPsiMembers() {
    final List<PsiMember> candidates = new ArrayList<PsiMember>();

    candidates.addAll(getProducerMethods());
    candidates.addAll(getProducerFields());

    return candidates;
  }

  public List<PsiMethod> getProducerMethods() {
    final List<PsiMethod> producers = new ArrayList<PsiMethod>();

    PsiMethod[] methods = getAnnotatedItem().getAllMethods();

    for (PsiMethod psiMethod : methods) {
      if (AnnotationUtil.isAnnotated(psiMethod, WebBeansAnnoConstants.PRODUCES_ANNOTATION, true)) {
        producers.add(psiMethod);
      }
    }

    return producers;
  }
  public List<PsiField> getProducerFields() {
    final List<PsiField> producers = new ArrayList<PsiField>();

   for (PsiField psiField : getAnnotatedItem().getFields()) {
      if (AnnotationUtil.isAnnotated(psiField, WebBeansAnnoConstants.PRODUCES_ANNOTATION, true)) {
        producers.add(psiField);
      }
    }

    return producers;
  }

  @NotNull
  public List<PsiField> getInjectableFields() {
    final List<PsiField> injectableFields = new ArrayList<PsiField>();

    PsiField[] fields = getAnnotatedItem().getAllFields();

    Collection<String> bindingTypesClasses = WebBeansCommonUtils.getQualifiedNames(WebBeansCommonUtils.getBindingTypesClasses(getModule()));

    for (PsiField field : fields) {
      final PsiModifierList modifierList = field.getModifierList();
      if (modifierList != null) {
        for (PsiAnnotation annotation : modifierList.getAnnotations()) {
          if (bindingTypesClasses.contains(annotation.getQualifiedName()) &&
              !AnnotationUtil.isAnnotated(field, WebBeansAnnoConstants.PRODUCES_ANNOTATION, false)) {
            injectableFields.add(field);
            break;
          }
        }
      }
    }

    return injectableFields;
  }
}
