/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.model.highlighting;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.util.PropertyUtil;
import com.intellij.psi.util.PsiFormatUtil;
import com.intellij.spring.SpringBundle;
import com.intellij.spring.SpringModel;
import com.intellij.spring.model.ResolvedConstructorArgs;
import com.intellij.spring.model.xml.beans.*;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomUtil;
import com.intellij.util.xml.highlighting.DomElementAnnotationHolder;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class SpringAutowiringInspection extends SpringBeanInspectionBase {

  @NotNull
  public String getDisplayName() {
    return SpringBundle.message("model.inspection.bean.autowiring");
  }

  @NotNull
  @NonNls
  public String getShortName() {
    return "SpringBeanAutowiringInspection";
  }

  private static void checkAutowiring(@NotNull final SpringBean springBean,
                                      @NotNull final SpringModel springModel,
                                      final DomElementAnnotationHolder holder) {

    if (springBean.getBeanClass() == null) return;
    final Autowire autowire = springBean.getBeanAutowire();

    if (autowire.equals(Autowire.BY_TYPE)) {
      checkByTypeAutowire(springBean, springModel, holder);
    }
    else if (autowire.equals(Autowire.CONSTRUCTOR)) {
      checkByConstructorAutowire(springBean, holder);
    }
  }

  private static void checkByConstructorAutowire(@NotNull final SpringBean springBean, @NotNull final DomElementAnnotationHolder holder) {

    final ResolvedConstructorArgs resolvedArgs = springBean.getResolvedConstructorArgs();
    final List<PsiMethod> methods = resolvedArgs.getCheckedMethods();
    if (resolvedArgs.isResolved() || methods == null) {
      return;
    }
    for (final PsiMethod checkedMethod : methods) {
      final Map<PsiParameter, Collection<SpringBaseBeanPointer>> autowiredParams = resolvedArgs.getAutowiredParams(checkedMethod);
      if (autowiredParams != null && autowiredParams.size() > 0) {
        final Set<Map.Entry<PsiParameter, Collection<SpringBaseBeanPointer>>> entries = autowiredParams.entrySet();
        for (Map.Entry<PsiParameter, Collection<SpringBaseBeanPointer>> entry : entries) {
          checkAutowire(springBean, holder, checkedMethod, entry.getKey(), entry.getValue());
        }
      }
    }
  }

  private static void checkAutowire(final SpringBean springBean,
                                    final DomElementAnnotationHolder holder,
                                    final PsiMethod checkedMethod,
                                    final PsiParameter psiParameter,
                                    final Collection<SpringBaseBeanPointer> springBeans) {
    if (springBeans != null && springBeans.size() > 1) {
      List<String> beanNames = new ArrayList<String>();
      for (SpringBaseBeanPointer bean : springBeans) {
        String beanName = bean.getName();
        if (StringUtil.isEmpty(beanName)) beanName = "unknown";
        beanNames.add(beanName);
      }

      String methodName = PsiFormatUtil
        .formatMethod(checkedMethod, PsiSubstitutor.EMPTY, PsiFormatUtil.SHOW_NAME | PsiFormatUtil.SHOW_PARAMETERS,
                      PsiFormatUtil.SHOW_TYPE);

      final String message = SpringBundle.message("bean.autowiring.by.type", psiParameter.getType().getPresentableText(),
                                                  StringUtil.join(beanNames, ","), methodName);
      final DomElement problemElement;
      if (DomUtil.hasXml(springBean.getClazz())) {
        problemElement = springBean.getClazz();
      }
      else if (DomUtil.hasXml(springBean.getFactoryMethod())) {
        problemElement = springBean.getFactoryMethod();
      }
      else {
        problemElement = springBean;
      }
      holder.createProblem(problemElement, message);
    }
  }

  private static void checkByTypeAutowire(@NotNull final SpringBean springBean,
                                          @NotNull final SpringModel springModel,
                                          @NotNull final DomElementAnnotationHolder holder) {
    final PsiClass beanClass = springBean.getBeanClass();
    if (beanClass == null) return;

    Map<PsiType, List<PsiMethod>> propertyTypes = new HashMap<PsiType, List<PsiMethod>>();
    for (PsiMethod psiMethod : beanClass.getAllMethods()) {
      if (PropertyUtil.isSimplePropertySetter(psiMethod)) {
        final PsiParameter parameter = psiMethod.getParameterList().getParameters()[0];
        final PsiType type = parameter.getType();
        if (propertyTypes.get(type) == null) {
          propertyTypes.put(type, new ArrayList<PsiMethod>());
        }

        propertyTypes.get(type).add(psiMethod);
      }
    }

    for (PsiType psiType : propertyTypes.keySet()) {
      final Collection<SpringBaseBeanPointer> beans = SpringAutowireUtil.autowireByType(springModel, psiType);
      if (beans.size() > 1) {
        List<String> properties = new ArrayList<String>();
        for (PsiMethod psiMethod : propertyTypes.get(psiType)) {
          boolean isPropertyDefined = false;
          final String propertyName = PropertyUtil.getPropertyNameBySetter(psiMethod);
          for (SpringPropertyDefinition springProperty : springBean.getAllProperties()) {
            if (propertyName.equals(springProperty.getPropertyName())) {
              isPropertyDefined = true;
              break;
            }
          }
          if (!isPropertyDefined) {
            properties.add(propertyName);
          }
        }

        if (properties.size() > 0) {
          List<String> beanNames = new ArrayList<String>();
          for (SpringBaseBeanPointer bean : beans) {
            String beanName = bean.getName();
            if (StringUtil.isEmpty(beanName)) beanName = "unknown";
            beanNames.add(beanName);
          }

          final String message = SpringBundle.message("bean.autowiring.by.type", psiType.getPresentableText(),
                                                      StringUtil.join(beanNames, ","), StringUtil.join(properties, ","));

          holder.createProblem(springBean, message);
        }
      }
    }
  }

  protected void checkBean(SpringBean springBean, final Beans beans, final DomElementAnnotationHolder holder, final SpringModel model) {
    final Boolean autoWireCandidate = springBean.getAutowireCandidate().getValue();
    if (autoWireCandidate != null && !autoWireCandidate.booleanValue()) return;
    checkAutowiring(springBean, model, holder);
  }
}
