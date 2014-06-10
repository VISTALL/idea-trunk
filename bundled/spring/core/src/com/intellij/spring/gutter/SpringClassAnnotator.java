/*
 * Copyright (c) 2000-2007 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.gutter;

import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.ide.util.DefaultPsiElementCellRenderer;
import com.intellij.ide.util.PsiElementListCellRenderer;
import com.intellij.jam.JamService;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.psi.*;
import com.intellij.psi.util.PropertyUtil;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlTag;
import com.intellij.spring.SpringBundle;
import com.intellij.spring.SpringIcons;
import com.intellij.spring.SpringManager;
import com.intellij.spring.SpringModel;
import com.intellij.spring.java.SpringJavaClassInfo;
import com.intellij.spring.model.highlighting.SpringAutowireUtil;
import com.intellij.spring.model.highlighting.SpringJavaAutowiringInspection;
import com.intellij.spring.model.jam.JamPsiMemberSpringBean;
import com.intellij.spring.model.jam.utils.SpringJamUtils;
import com.intellij.spring.model.xml.CommonSpringBean;
import com.intellij.spring.model.xml.DomSpringBean;
import com.intellij.spring.model.xml.beans.*;
import com.intellij.util.NotNullFunction;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class SpringClassAnnotator implements Annotator {

  private static final String UNKNOWN = SpringBundle.message("spring.bean.with.unknown.name");
  private static final DomElementListCellRenderer DOM_RENDERER = new DomElementListCellRenderer(UNKNOWN);

  @Nullable
  private static SpringBean getSpringBean(final PsiElement element) {
    final DomElement domElement = DomUtil.getDomElement(element);
    return domElement == null ? null : domElement.getParentOfType(SpringBean.class, false);
  }

  private static final PsiElementListCellRenderer BEAN_RENDERER = new DefaultPsiElementCellRenderer() {

    public String getElementText(final PsiElement element) {
      if (element instanceof XmlTag) {
        return DOM_RENDERER.getElementText((XmlTag)element);
      }
      else if (element instanceof PsiAnnotation) {
        final PsiMember member = PsiTreeUtil.getParentOfType(element, PsiMember.class);
        final CommonSpringBean springBean =
          member == null ? null : JamService.getJamService(element.getProject()).getJamElement(JamPsiMemberSpringBean.class, member);
        if (springBean != null) {
          final String beanName = springBean.getBeanName();
          return beanName == null ? UNKNOWN : beanName;
        }
      }
      return super.getElementText(element);
    }

    public String getContainerText(final PsiElement element, final String name) {
      if (element instanceof XmlTag) {
        return DOM_RENDERER.getContainerText((XmlTag)element, name);
      }
      else if (element instanceof PsiAnnotation) {
        final PsiClass psiClass = PsiTreeUtil.getParentOfType(element, PsiClass.class);
        if (psiClass != null && psiClass.getName() != null) {
          return psiClass.getName();
        }
      }
      return super.getContainerText(element, name);
    }

    @Nullable
    @Override
    protected Icon getIcon(final PsiElement element) {
      if (element instanceof XmlTag) {
        return DOM_RENDERER.getIcon(element);
      }
      else if (element instanceof PsiAnnotation) {
        final PsiMember member = PsiTreeUtil.getParentOfType(element, PsiMember.class);
        final CommonSpringBean springBean =
          member == null ? null : JamService.getJamService(element.getProject()).getJamElement(JamPsiMemberSpringBean.class, member);
        if (springBean != null) {
          return SpringIcons.SPRING_JAVA_BEAN_ICON;
        }
      }
      return super.getIcon(element);
    }
  };

  private static final NotNullFunction<SpringBaseBeanPointer,Collection<? extends PsiElement>> BEAN_POINTER_CONVERTOR = new NotNullFunction<SpringBaseBeanPointer, Collection<? extends PsiElement>>() {
    @NotNull
    public Collection<? extends PsiElement> fun(final SpringBaseBeanPointer pointer) {
      return Collections.singleton(pointer.getPsiElement());
    }
  };


  public void annotate(final PsiElement psiElement, final AnnotationHolder holder) {
    if (psiElement instanceof PsiIdentifier) {
      final PsiElement parent = psiElement.getParent();
      if (parent instanceof PsiClass) {
        final PsiClass psiClass = (PsiClass)parent;
        final SpringJavaClassInfo info = SpringJavaClassInfo.getSpringJavaClassInfo(psiClass);
        if (info.isMapped()) {
          addSpringBeanGutterIcon(holder, psiClass.getNameIdentifier(), new NotNullLazyValue<Collection<? extends SpringBaseBeanPointer>>() {
            @NotNull
            protected Collection<? extends SpringBaseBeanPointer> compute() {
              final SpringJavaClassInfo info = SpringJavaClassInfo.getSpringJavaClassInfo(psiClass);
              return info.getMappedBeans();
            }
          });
        }
      } else if (parent instanceof PsiMethod) {
        annotateMethod((PsiMethod)parent, holder);
      } else if (parent instanceof PsiField) {
        PsiField field = (PsiField)parent;
        if (SpringAutowireUtil.isAutowiredByAnnotation(field)) {
          final Module module = ModuleUtil.findModuleForPsiElement(field);
          final SpringModel model = SpringManager.getInstance(field.getProject()).getCombinedModel(module);
          if (model != null) {
            final boolean required = SpringAutowireUtil.isRequired(field);
            processVariable(field, holder, model, psiElement, required, field.getType());
          }
        }
      }
    }
  }

  private static void annotateMethod(final PsiMethod method, final AnnotationHolder holder) {
    boolean autowired = false;
    if (PropertyUtil.isSimplePropertySetter(method)) {
      PsiClass psiClass = method.getContainingClass();
      if (psiClass != null) {
        final SpringJavaClassInfo info = SpringJavaClassInfo.getSpringJavaClassInfo(psiClass);
        final String propertyName = PropertyUtil.getPropertyNameBySetter(method);
        final Collection<SpringPropertyDefinition> list = info.getMappedProperties(propertyName);
        if (list.size() > 0) {
          addPropertiesGutterIcon(holder, method);
        }
        final List<DomSpringBeanPointer> pointers = info.getMappedBeans();
        for (DomSpringBeanPointer pointer : pointers) {
          final DomSpringBean springBean = pointer.getSpringBean();
          if (springBean instanceof SpringBean) {
            final Autowire autowire = ((SpringBean)springBean).getBeanAutowire();
            if (autowire.isAutowired()) {
              autowired = true;
              break;
            }
          }
        }
        if (autowired) {
          final Module module = ModuleUtil.findModuleForPsiElement(method);
          final SpringModel model = SpringManager.getInstance(method.getProject()).getCombinedModel(module);
          if (model != null) {
            final PsiType type = PropertyUtil.getPropertyType(method);
            if (type != null) {
              processVariable(method, holder, model, method.getNameIdentifier(), false, type);
            }
          }
        }
      }
    } else {
      final List<SpringBaseBeanPointer> externalBeans = SpringJamUtils.findExternalBeans(method);
      if (externalBeans.size() > 0) {
        addSpringBeanGutterIcon(holder, method.getNameIdentifier(), new NotNullLazyValue<Collection<? extends SpringBaseBeanPointer>>() {
          @NotNull
          protected Collection<? extends SpringBaseBeanPointer> compute() {
            return SpringJamUtils.findExternalBeans(method);
          }
        });
      }
    }
    if (!autowired) {
      processAnnotatedMethod(method, holder);
    }
  }

  private static void processAnnotatedMethod(final PsiMethod method, final AnnotationHolder holder) {
    if (SpringAutowireUtil.isAutowiredByAnnotation(method)) {
      final Module module = ModuleUtil.findModuleForPsiElement(method);
      final SpringModel model = SpringManager.getInstance(method.getProject()).getCombinedModel(module);
      if (model != null) {
        final boolean required = SpringAutowireUtil.isRequired(method);
        for (PsiVariable variable : method.getParameterList().getParameters()) {
          processVariable(variable, holder, model, variable, required, variable.getType());
        }
      }
    }
  }

  private static void processVariable(final PsiModifierListOwner variable, final AnnotationHolder holder,
                                      @NotNull final SpringModel model,
                                      final PsiElement element, final boolean required, @NotNull final PsiType type) {
    final Collection<SpringBaseBeanPointer> list =
        SpringJavaAutowiringInspection.checkAutowiredPsiMember(variable, type, null, model, required);
    if (list != null && !list.isEmpty()) {
      NavigationGutterIconBuilder.create(SpringIcons.SPRING_DEPENDENCIES_GRAPH_SHOW_AUTOWIRED, BEAN_POINTER_CONVERTOR).
          setPopupTitle(SpringBundle.message("spring.bean.class.navigate.choose.class.title")).
          setCellRenderer(BEAN_RENDERER).
          setTooltipText(SpringBundle.message("navigate.to.autowired.dependencies")).
          setTargets(list).install(holder, element);
    }
  }

  private static void addPropertiesGutterIcon(final AnnotationHolder holder,
                                              final PsiMethod psiMethod) {


    NavigationGutterIconBuilder.create(SpringIcons.SPRING_BEAN_PROPERTY_ICON, NavigationGutterIconBuilder.DEFAULT_DOM_CONVERTOR).
      setTargets(new NotNullLazyValue<Collection<? extends DomElement>>() {
        @NotNull
        protected Collection<? extends DomElement> compute() {
          final String propertyName = PropertyUtil.getPropertyNameBySetter(psiMethod);
          final SpringJavaClassInfo info = SpringJavaClassInfo.getSpringJavaClassInfo((PsiClass)psiMethod.getParent());
          return info.getMappedProperties(propertyName);
        }
      }).
      setPopupTitle(SpringBundle.message("spring.bean.property.navigate.choose.class.title")).
      setCellRenderer(new DefaultPsiElementCellRenderer() {
        @Override
        public String getElementText(final PsiElement element) {
          final SpringBean springBean = getSpringBean(element);
          assert springBean != null;
          final String elementName = springBean.getBeanName();
          assert elementName != null;
          return elementName;
        }

        @Nullable
        @Override
        protected Icon getIcon(final PsiElement element) {
          final SpringBean springBean = getSpringBean(element);
          assert springBean != null;
          return SpringIcons.SPRING_BEAN_ICON;
        }

        @Override
        public String getContainerText(final PsiElement element, final String name) {
          return DomElementListCellRenderer.getContainerText(element);
        }
      }).
      setTooltipText(SpringBundle.message("spring.bean.property.tooltip.navigate.declaration")).
      install(holder, psiMethod.getNameIdentifier());
  }

  private static void addSpringBeanGutterIcon(final AnnotationHolder holder, final PsiIdentifier psiIdentifier,
                                              final NotNullLazyValue<Collection<? extends SpringBaseBeanPointer>> targets) {

    NavigationGutterIconBuilder.create(SpringIcons.SPRING_BEAN_ICON, BEAN_POINTER_CONVERTOR).
      setTargets(targets).
      setPopupTitle(SpringBundle.message("spring.bean.class.navigate.choose.class.title")).
      setCellRenderer(DOM_RENDERER).
      setTooltipText(SpringBundle.message("spring.bean.class.tooltip.navigate.declaration")).
      install(holder, psiIdentifier);
  }
}
