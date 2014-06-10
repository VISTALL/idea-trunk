/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.model.highlighting;

import com.intellij.codeInsight.AnnotationUtil;
import com.intellij.javaee.model.annotations.AnnotationModelUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.psi.*;
import com.intellij.psi.util.PropertyUtil;
import com.intellij.psi.util.PsiUtil;
import com.intellij.spring.SpringModel;
import com.intellij.spring.constants.SpringAnnotationsConstants;
import com.intellij.spring.model.jam.utils.JamAnnotationTypeUtil;
import com.intellij.spring.model.SpringUtils;
import com.intellij.spring.model.jam.qualifiers.SpringJamQualifier;
import com.intellij.spring.model.xml.CommonSpringBean;
import com.intellij.spring.model.xml.DomSpringBean;
import com.intellij.spring.model.xml.beans.*;
import com.intellij.util.containers.HashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NonNls;

import java.lang.annotation.ElementType;
import java.util.*;

public class SpringAutowireUtil {
  private SpringAutowireUtil() {
  }

  public static Map<PsiMethod, Collection<SpringBaseBeanPointer>> getByTypeAutowiredProperties(SpringBean springBean, final SpringModel model) {
    Map<PsiMethod, Collection<SpringBaseBeanPointer>> autowiredMap = new HashMap<PsiMethod, Collection<SpringBaseBeanPointer>>();
    final PsiClass beanClass = springBean.getBeanClass();
    if (beanClass != null) {
      if (model != null && isByTypeAutowired(springBean)) {
        for (PsiMethod psiMethod : beanClass.getAllMethods()) {
          if (isPropertyAutowired(psiMethod, springBean)) {
            final PsiParameter parameter = psiMethod.getParameterList().getParameters()[0];
            final Collection<SpringBaseBeanPointer> list = autowireByType(model, parameter.getType());
            if (list.size() > 0) {
              autowiredMap.put(psiMethod, list);
            }
          }
        }
      }
    }

    return autowiredMap;
  }

  @NotNull
  private static List<SpringBaseBeanPointer> excludeAutowireCandidates(@Nullable final List<SpringBaseBeanPointer> beans) {
    final List<SpringBaseBeanPointer> beanPointers = new ArrayList<SpringBaseBeanPointer>();
    if (beans != null) {
      for (SpringBaseBeanPointer bean : beans) {
        if (isAutowireCandidate(bean.getSpringBean())) {
          beanPointers.add(bean);
        }
      }
    }
    return beanPointers;
  }

  @NotNull
  public static List<SpringBaseBeanPointer> excludeAutowireCandidatesForCommonBeans(@Nullable final List<SpringBaseBeanPointer> beans) {
    final List<SpringBaseBeanPointer> list = new ArrayList<SpringBaseBeanPointer>();
    if (beans != null) {
      for (SpringBaseBeanPointer beanPointer : beans) {
        final CommonSpringBean commonSpringBean = beanPointer.getSpringBean();
        if (commonSpringBean instanceof DomSpringBean) {
          if (isAutowireCandidate(commonSpringBean)) {
            list.add(beanPointer);
          }
        }
        else {
          list.add(beanPointer);
        }
      }
    }
    return list;
  }

  private static boolean isAutowireCandidate(final CommonSpringBean springBean) {
    if (!(springBean instanceof SpringBean)) return true;
    final Boolean autoWireCandidate = ((SpringBean)springBean).getAutowireCandidate().getValue();

    return autoWireCandidate == null || autoWireCandidate.booleanValue();

  }

  public static Map<PsiType, Collection<SpringBaseBeanPointer>> getConstructorAutowiredProperties(SpringBean springBean,
                                                                                            final SpringModel model) {
    Map<PsiType, Collection<SpringBaseBeanPointer>> autowiredMap = new HashMap<PsiType, Collection<SpringBaseBeanPointer>>();
    final PsiClass beanClass = springBean.getBeanClass();
    if (beanClass != null) {
      if (isConstructorAutowire(springBean)) {
        final boolean instantiatedByFactory = SpringConstructorArgResolveUtil.isInstantiatedByFactory(springBean);

        PsiMethod checkedMethod = instantiatedByFactory
                                  ? springBean.getFactoryMethod().getValue()
                                  : SpringConstructorArgResolveUtil.getSpringBeanConstructor(springBean, model);

        if (checkedMethod != null) {
          final List<ConstructorArg> list = SpringUtils.getConstructorArgs(springBean);
          Map<Integer, ConstructorArg> indexedArgs = SpringConstructorArgResolveUtil.getIndexedConstructorArgs(list);
          final PsiParameter[] parameters = checkedMethod.getParameterList().getParameters();
          for (int i = 0; i < parameters.length; i++) {
            PsiParameter parameter = parameters[i];
            if (!SpringConstructorArgResolveUtil.acceptParameter(parameter, list, indexedArgs, i)) {
              final PsiType psiType = parameter.getType();
              final Collection<SpringBaseBeanPointer> springBeans = autowireByType(model, psiType);
              if (springBeans.size() > 0) {
                autowiredMap.put(psiType, springBeans);
              }
            }
          }
        }
      }
    }

    return autowiredMap;
  }

  private static boolean isPropertyDefined(final SpringBean springBean, final String propertyName) {
    for (SpringPropertyDefinition springProperty : springBean.getAllProperties()) {
      if (propertyName.equals(springProperty.getPropertyName())) {
        return true;
      }
    }
    return false;
  }

  public static Map<PsiMethod, SpringBaseBeanPointer> getByNameAutowiredProperties(SpringBean springBean) {
    Map<PsiMethod, SpringBaseBeanPointer> autowiredMap = new HashMap<PsiMethod, SpringBaseBeanPointer>();
    final PsiClass beanClass = springBean.getBeanClass();
    if (beanClass != null) {
      final SpringModel model = SpringUtils.getSpringModel(springBean);
      if (isByNameAutowired(springBean)) {
        for (PsiMethod psiMethod : beanClass.getAllMethods()) {
          if (PropertyUtil.isSimplePropertySetter(psiMethod)) {
            final PsiParameter parameter = psiMethod.getParameterList().getParameters()[0];
            final Collection<SpringBaseBeanPointer> list = autowireByType(model, parameter.getType());

            final String propertyName = PropertyUtil.getPropertyNameBySetter(psiMethod);
            for (SpringBaseBeanPointer bean : list) {
              if (SpringUtils.getAllBeanNames(bean.getSpringBean()).contains(propertyName)) {
                autowiredMap.put(psiMethod, bean);
              }
            }
          }
        }
      }
    }

    return autowiredMap;
  }

  private static boolean isPropertyAutowired(final PsiMethod psiMethod, final SpringBean springBean) {
    if (PropertyUtil.isSimplePropertySetter(psiMethod)) {
      final PsiParameter parameter = psiMethod.getParameterList().getParameters()[0];
      final PsiType psiType = parameter.getType();
      if (psiType instanceof PsiClassType) {
        final PsiClass psiClass = ((PsiClassType)psiType).resolve();

        return psiClass != null && !isPropertyDefined(springBean, PropertyUtil.getPropertyNameBySetter(psiMethod));
      }
    }
    return false;
  }

  public static boolean isByTypeAutowired(final SpringBean springBean) {
    return springBean.getBeanAutowire().equals(Autowire.BY_TYPE);
  }

  public static boolean isByNameAutowired(final SpringBean springBean) {
    return springBean.getBeanAutowire().equals(Autowire.BY_NAME);
  }

  public static boolean isConstructorAutowire(final SpringBean springBean) {
    return springBean.getBeanAutowire().equals(Autowire.CONSTRUCTOR);
  }

  public static Map<PsiMember, List<SpringBaseBeanPointer>> getAutowireAnnotationProperties(final CommonSpringBean springBean,
                                                                                            @NotNull final SpringModel model) {
    Map<PsiMember, List<SpringBaseBeanPointer>> map = new HashMap<PsiMember, List<SpringBaseBeanPointer>>();
    final PsiClass beanClass = springBean.getBeanClass();
    if (beanClass != null) {
      for (PsiMethod psiMethod : getAnnotatedAutowiredMethods(beanClass)) {
        for (PsiParameter parameter : psiMethod.getParameterList().getParameters()) {
          final PsiAnnotation psiAnnotation = getQualifiedAnnotation(parameter, model.getModule());
          if (psiAnnotation != null) {
            addAutowiredBeans(map, psiMethod, getQualifiedBeans(psiAnnotation, model));
          }
          else {
            addAutowiredBeans(map, psiMethod, SpringUtils.getBeansByType(parameter.getType(), model));
          }
        }
      }

      for (PsiField psiField : getAnnotatedAutowiredFields(beanClass)) {
        final PsiAnnotation psiAnnotation = getQualifiedAnnotation(psiField, model.getModule());
        if (psiAnnotation != null) {
          addAutowiredBeans(map, psiField, getQualifiedBeans(psiAnnotation, model));
        }
        else {
          addAutowiredBeans(map, psiField, SpringUtils.getBeansByType(psiField.getType(), model));
        }
      }
    }

    return map;
  }

  private static void addAutowiredBeans(final Map<PsiMember, List<SpringBaseBeanPointer>> map, final PsiMember psiMember,

                                        final List<SpringBaseBeanPointer> beans) {
    final List<SpringBaseBeanPointer> list = excludeAutowireCandidatesForCommonBeans(beans);
    if (list.size() > 0) {
      if (!map.containsKey(psiMember)) {
        map.put(psiMember, list);
      }
      else {
        map.get(psiMember).addAll(list);
      }
    }
  }

  @NotNull
  public static List<SpringBaseBeanPointer> getQualifiedBeans(@NotNull final PsiAnnotation psiAnnotation, @Nullable final SpringModel model) {
    //3.11.3. Fine-tuning annotation-based autowiring with qualifiers
    if (model == null) return Collections.emptyList();
    SpringJamQualifier qualifier = new SpringJamQualifier(psiAnnotation, null, null);
    return model.findQualifiedBeans(qualifier);
  }

  @Nullable
  public static PsiAnnotation getQualifiedAnnotation(final PsiModifierListOwner modifierListOwner) {
    return getQualifiedAnnotation(modifierListOwner, ModuleUtil.findModuleForPsiElement(modifierListOwner));
  }

  @Nullable
  private static PsiAnnotation getQualifiedAnnotation(final PsiModifierListOwner modifierListOwner, @Nullable final Module module) {
    if (module == null) return null;

    final List<PsiClass> annotationTypeClasses = JamAnnotationTypeUtil.getQualifierAnnotationTypesWithChildren(module);

    for (PsiClass annotationTypeClass : annotationTypeClasses) {
      if ((JamAnnotationTypeUtil.isAcceptedFor(annotationTypeClass, ElementType.FIELD) && modifierListOwner instanceof PsiField) ||
          (JamAnnotationTypeUtil.isAcceptedFor(annotationTypeClass, ElementType.PARAMETER) && modifierListOwner instanceof PsiParameter)) {
        final PsiAnnotation annotation = AnnotationUtil.findAnnotation(modifierListOwner, annotationTypeClass.getQualifiedName());

        if (annotation != null) {
          return annotation;
        }
      }
    }

    return null;
  }

  @Nullable
  public static PsiAnnotation getAutowiredAnnotation(final @NotNull PsiModifierListOwner owner) {
    final PsiModifierList modifierList = owner.getModifierList();
    if (modifierList != null) {
      return modifierList.findAnnotation(SpringAnnotationsConstants.AUTOWIRED_ANNOTATION);
    }
    return null;
  }

  @Nullable
  public static PsiAnnotation getResourceAnnotation(final @NotNull PsiModifierListOwner owner) {
    final PsiModifierList modifierList = owner.getModifierList();
    if (modifierList != null) {
      return modifierList.findAnnotation(SpringAnnotationsConstants.RESOURCE_ANNOTATION);
    }
    return null;
  }

  public static boolean isAutowiredByAnnotation(final @NotNull PsiModifierListOwner owner) {
    final PsiModifierList modifierList = owner.getModifierList();
    return modifierList != null &&
           (modifierList.findAnnotation(SpringAnnotationsConstants.AUTOWIRED_ANNOTATION) != null ||
            modifierList.findAnnotation(SpringAnnotationsConstants.RESOURCE_ANNOTATION) != null);
  }

  public static boolean isRequired(final @NotNull PsiModifierListOwner owner) {
    final PsiModifierList modifierList = owner.getModifierList();
    if (modifierList != null) {
      final PsiAnnotation required = modifierList.findAnnotation(SpringAnnotationsConstants.REQUIRED_ANNOTATION);
      if (required != null) {
        return true;
      }
      final PsiAnnotation autowiredAnnotation = getAutowiredAnnotation(owner);
      if (autowiredAnnotation != null) {
        final Boolean value = AnnotationModelUtil.getBooleanValue(autowiredAnnotation, "required", true).getValue();
        return value == null || value.booleanValue();
      }
    }
    return true;
  }

  @NotNull
  public static List<PsiMethod> getAnnotatedAutowiredMethods(@NotNull final PsiClass psiClass) {
    final List<PsiMethod> methods = new ArrayList<PsiMethod>();
    for (PsiMethod psiMethod : psiClass.getAllMethods()) {
      if (isAutowiredByAnnotation(psiMethod)) {
        methods.add(psiMethod);
      }
    }
    return methods;
  }

  @NotNull
  public static List<PsiField> getAnnotatedAutowiredFields(@NotNull final PsiClass psiClass) {
    final List<PsiField> fields = new ArrayList<PsiField>();
    for (PsiField psiField : psiClass.getAllFields()) {
      if (isAutowiredByAnnotation(psiField)) {
        fields.add(psiField);
      }
    }
    return fields;
  }

  @NotNull
  public static List<SpringBaseBeanPointer> autowireByType(@NotNull final SpringModel model, final PsiType psiType) {
    if (psiType instanceof PsiClassType) {
      PsiType beanType = PsiUtil.extractIterableTypeParameter(psiType, false);
      if (beanType == null) {
        beanType = psiType;
      }
      if (beanType instanceof PsiClassType) {
        final PsiClass psiClass = ((PsiClassType)beanType).resolve();
        if (psiClass != null) {
          return excludeAutowireCandidates(model.findBeansByEffectivePsiClassWithInheritance(psiClass));
        }
      }
    }
    return Collections.emptyList();
  }

  @NonNls private final static Set<String> STANDARD_AUTOWIRINGS =
    new HashSet<String>(Arrays.asList("javax.servlet.http.HttpServletRequest",
                                      "javax.servlet.http.HttpSession",

                                      "org.springframework.beans.factory.BeanFactory",
                                      "org.springframework.context.ApplicationContext",
                                      "org.springframework.context.ApplicationEventPublisher",
                                      "org.springframework.core.io.ResourceLoader"

    ));

  public static boolean isAutowiredByDefault(@NotNull PsiType psiType) {
    final String text = psiType.getCanonicalText();
    return text != null && STANDARD_AUTOWIRINGS.contains(text);
  }
}
