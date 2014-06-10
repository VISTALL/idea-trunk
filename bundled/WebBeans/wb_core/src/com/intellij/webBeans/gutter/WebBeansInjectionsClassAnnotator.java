/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.webBeans.gutter;

import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.HashSet;
import com.intellij.webBeans.WebBeansIcons;
import com.intellij.webBeans.beans.AbstractWebBeanDescriptor;
import com.intellij.webBeans.beans.WebBeanDescriptor;
import com.intellij.webBeans.beans.WebBeanPsiClassDescriptor;
import com.intellij.webBeans.manager.WebBeansManager;
import com.intellij.webBeans.resources.WebBeansBundle;
import com.intellij.webBeans.utils.WebBeansCommonUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class WebBeansInjectionsClassAnnotator implements Annotator {
  public void annotate(final PsiElement psiElement, final AnnotationHolder holder) {
    if (psiElement instanceof PsiIdentifier) {
      final PsiElement parent = psiElement.getParent();
      if (parent instanceof PsiClass) {
        Module module = ModuleUtil.findModuleForPsiElement(parent);
        if (module != null && WebBeansCommonUtils.isWebBeansFacetDefined(module) && WebBeansCommonUtils.isSimpleWebBean((PsiClass)parent)) {
          WebBeanPsiClassDescriptor webBeanDescriptor =
            WebBeansManager.getService(module).getFactory().createWebBeanDescriptor(((PsiClass)parent));

          for (PsiField psiField : webBeanDescriptor.getInjectableFields()) {
            annotateField(psiField, holder, module);
          }

          Set<PsiMethod> methods = collectInjectableMethods(webBeanDescriptor);
          for (PsiMethod psiMethod : methods) {
            annotateMethod(psiMethod, holder, module);
          }
        }
      }
    }
  }

  private static Set<PsiMethod> collectInjectableMethods(WebBeanPsiClassDescriptor webBeanDescriptor) {
    Set<PsiMethod> methods = new HashSet<PsiMethod>();

    methods.addAll(webBeanDescriptor.getInitializerConstructors());
    methods.addAll(webBeanDescriptor.getInitializerMethods());
    methods.addAll(webBeanDescriptor.getProducerMethods());

    return methods;
  }

  private static void annotateField(PsiField field, AnnotationHolder holder, Module module) {
    addInjectableWebBeansGutterIcon(holder, getInjectableBeans(field, module, field.getType()), field, field.getType() );
  }

  private static Set<WebBeanDescriptor> getInjectableBeans(PsiModifierListOwner psiMember, Module module, PsiType type) {
    //todo primitive types
    if (type instanceof PsiClassType) {
      PsiClass psiClass = ((PsiClassType)type).resolve();
      Set<PsiClass> bindingTypes = WebBeansCommonUtils.getBindingTypesClasses(psiMember, module);

      return WebBeansManager.getService(module).resolveWebBeanByType(psiClass, bindingTypes.toArray(new PsiClass[bindingTypes.size()]));

    }
    return Collections.emptySet();
  }

  private static void addInjectableWebBeansGutterIcon(AnnotationHolder holder,
                                                      final Set<WebBeanDescriptor> beanDescriptors,
                                                      PsiModifierListOwner owner, PsiType type) {
    if (beanDescriptors != null && beanDescriptors.size() > 0) {
      addInjectedWebBeanGutterIcon(holder, owner, type, new NotNullLazyValue<Collection<? extends PsiElement>>() {
        @NotNull
        @Override
        protected Collection<PsiMember> compute() {
          return ContainerUtil.mapNotNull(beanDescriptors, new Function<WebBeanDescriptor, PsiMember>() {
            public PsiMember fun(WebBeanDescriptor webBeanDescriptor) {
              if (webBeanDescriptor instanceof AbstractWebBeanDescriptor) {
                return ((AbstractWebBeanDescriptor)webBeanDescriptor).getAnnotatedItem();
              }
              return null;
            }
          });
        }
      });
    }
  }

  private static void annotateMethod(final PsiMethod method, final AnnotationHolder holder, Module module) {
    for (PsiParameter psiParameter : method.getParameterList().getParameters()) {
      Set<WebBeanDescriptor> beans = getInjectableBeans(psiParameter, module, psiParameter.getType());

      addInjectableWebBeansGutterIcon(holder, beans, psiParameter, psiParameter.getType());
    }
  }

  private static void addInjectedWebBeanGutterIcon(final AnnotationHolder holder,
                                                   final PsiModifierListOwner psiIdentifier, PsiType type, final NotNullLazyValue<Collection<? extends PsiElement>> targets) {



    NavigationGutterIconBuilder.create(WebBeansIcons.WEB_BEAN).
      setTargets(targets).
      setPopupTitle(WebBeansBundle.message("gutter.choose.web.beans")).
      setTooltipText(WebBeansBundle.message("gutter.navigate.to.web.beans", getWebBeanDescription(psiIdentifier, type))).
      install(holder, psiIdentifier);
  }

  private static String getWebBeanDescription(@NotNull PsiModifierListOwner owner, PsiType type) {

    Collection<PsiClass> psiClasses = WebBeansCommonUtils.getBindingTypesClasses(owner);
    String[] names = ContainerUtil.map2Array(psiClasses, String.class, new Function<PsiClass, String>() {
      public String fun(PsiClass psiClass) {
        return "@" + psiClass.getName();
      }
    });

    return StringUtil.join(names, " ") + " " + type.getPresentableText();
  }
}
