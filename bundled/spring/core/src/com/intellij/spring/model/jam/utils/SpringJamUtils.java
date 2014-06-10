package com.intellij.spring.model.jam.utils;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiPackage;
import com.intellij.psi.xml.XmlTag;
import com.intellij.spring.SpringManager;
import com.intellij.spring.SpringModel;
import com.intellij.spring.model.SpringUtils;
import com.intellij.spring.model.jam.SpringJamModel;
import com.intellij.spring.model.jam.javaConfig.SpringJavaConfiguration;
import com.intellij.spring.model.jam.javaConfig.SpringJavaExternalBean;
import com.intellij.spring.model.jam.javaConfig.JavaConfigConfiguration;
import com.intellij.spring.model.jam.stereotype.SpringStereotypeElement;
import com.intellij.spring.model.xml.CommonSpringBean;
import com.intellij.spring.model.xml.beans.SpringBaseBeanPointer;
import com.intellij.spring.model.xml.context.ComponentScan;
import com.intellij.util.NullableFunction;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class SpringJamUtils {
  private SpringJamUtils() {
  }

  @NotNull
  public static List<SpringStereotypeElement> getAllStereotypeJavaBeans(final SpringModel domModel) {
    List<SpringStereotypeElement> allStereotypes = new ArrayList<SpringStereotypeElement>();
    final Module module = domModel.getModule();
    if (module != null) {
      List<ComponentScan> scanBeans = getComponentScans(domModel.getAllDomBeans());
      if (scanBeans.size() > 0) {
        List<PsiPackage> psiPackages = getScannedPackages(scanBeans);
        if (psiPackages.isEmpty()) {
          return Collections.emptyList();
        }
        SpringJamModel javaModel = SpringJamModel.getModel(module);

        List<SpringStereotypeElement> components = getAllStereotypeComponents(javaModel);
        return filterStereotypeComponents(components, psiPackages);
      }
    }

    return allStereotypes;
  }

  @NotNull
  public static List<SpringJavaConfiguration> getJavaConfigurations(final SpringModel domModel) {
    List<SpringJavaConfiguration> javaConfigurations = new ArrayList<SpringJavaConfiguration>();
    final Module module = domModel.getModule();
    if (module != null) {
      List<ComponentScan> scanBeans = getComponentScans(domModel.getAllDomBeans());
      if (scanBeans.size() > 0) {
        List<PsiPackage> psiPackages = getScannedPackages(scanBeans);
        if (psiPackages.isEmpty()) {
          return Collections.emptyList();
        }

        List<SpringJavaConfiguration> components = SpringJamModel.getModel(module).getConfigurations();

        return filterJavaConfigurations(components, psiPackages);
      }
    }

    return javaConfigurations;
  }

  private static List<ComponentScan> getComponentScans(final Collection<SpringBaseBeanPointer> allDomBeans) {
    return ContainerUtil.mapNotNull(allDomBeans, new NullableFunction<SpringBaseBeanPointer, ComponentScan>() {
      public ComponentScan fun(final SpringBaseBeanPointer domSpringBeanPointer) {
        final CommonSpringBean domSpringBean = domSpringBeanPointer.getSpringBean();
        if (domSpringBean instanceof ComponentScan) {
          return (ComponentScan)domSpringBean;
        }
        return null;
      }
    });
  }

  private static List<SpringStereotypeElement> filterStereotypeComponents(final List<SpringStereotypeElement> components,
                                                                final List<PsiPackage> psiPackages) {
    List<SpringStereotypeElement> filtered = new ArrayList<SpringStereotypeElement>();
    for (SpringStereotypeElement component : components) {
      final PsiClass psiClass = component.getBeanClass();
      if (isInPackage(psiPackages, psiClass)) {
        filtered.add(component);
      }
    }

    return filtered;
  }

  private static List<SpringJavaConfiguration> filterJavaConfigurations(final List<SpringJavaConfiguration> javaConfigurations,
                                                                final List<PsiPackage> psiPackages) {
    List<SpringJavaConfiguration> filtered = new ArrayList<SpringJavaConfiguration>();
    for (SpringJavaConfiguration component : javaConfigurations) {
      final PsiClass psiClass = component.getPsiClass();
      if (isInPackage(psiPackages, psiClass)) {
        filtered.add(component);
      }
    }

    return filtered;
  }

  private static boolean isInPackage(List<PsiPackage> psiPackages, @Nullable PsiClass psiClass) {
    if (psiClass != null) {
      final String qualifiedName = psiClass.getQualifiedName();
      if (qualifiedName != null) {
        for (PsiPackage psiPackage : psiPackages) {
          if (StringUtil.startsWithConcatenationOf(qualifiedName, psiPackage.getQualifiedName(), ".")) {
            return true;
          }
        }
      }
    }
    return false;
  }

  private static List<SpringStereotypeElement> getAllStereotypeComponents(final SpringJamModel javaModel) {
    List<SpringStereotypeElement> elements = new ArrayList<SpringStereotypeElement>();

    elements.addAll(javaModel.getAllStereotypeComponents());

    return elements;
  }

  private static List<PsiPackage> getScannedPackages(final List<ComponentScan> scanBeans) {
    final ArrayList<PsiPackage> list = new ArrayList<PsiPackage>(scanBeans.size());
    for (ComponentScan scanBean : scanBeans) {
      list.addAll(scanBean.getBasePackage().getValue());
    }
    return list;
  }

  @NotNull
  public static List<SpringBaseBeanPointer> findExternalBeans(final PsiMethod psiMethod) {
    if (!isExternalBean(psiMethod)) {
      return Collections.emptyList();
    }
    final Module module = ModuleUtil.findModuleForPsiElement(psiMethod);
    PsiClass psiClass = psiMethod.getContainingClass();
    if (module == null || psiClass == null) {
      return Collections.emptyList();
    }
    List<SpringBaseBeanPointer> extBeans = new ArrayList<SpringBaseBeanPointer>();
    final List<SpringModel> springModels = SpringManager.getInstance(psiMethod.getProject()).getAllModels(module);
    for (SpringModel model : springModels) {
      List<SpringBaseBeanPointer> javaConfigBeans = model.findBeansByPsiClass(psiClass);
      if (javaConfigBeans.size() > 0) {
        for (SpringBaseBeanPointer springBean : model.getAllDomBeans()) {

          final String externalBeanName = psiMethod.getName();
          final String beanName = springBean.getName();
          if (externalBeanName.equals(beanName) || Arrays.asList(springBean.getAliases()).contains(externalBeanName)) {
            extBeans.add(springBean);
          }
        }
      }
    }
    return extBeans;
  }

  @NotNull
  public static List<SpringJavaExternalBean> findExternalBeanReferences(final CommonSpringBean springBean) {
    List<SpringJavaExternalBean> extBeans = new ArrayList<SpringJavaExternalBean>();
    final Set<String> strings = SpringUtils.getAllBeanNames(springBean);

    if (strings.size() > 0) {
      final XmlTag element = springBean.getXmlTag();
      if (element != null) {
        final Module module = ModuleUtil.findModuleForPsiElement(element);

        if (module != null) {
          for (SpringJavaConfiguration javaConfiguration : SpringJamModel.getModel(module).getConfigurations()) {
            if (javaConfiguration instanceof JavaConfigConfiguration) {
              for (SpringJavaExternalBean externalBean : ((JavaConfigConfiguration)javaConfiguration).getExternalBeans()) {
                final PsiMethod psiMethod = externalBean.getPsiElement();
                if (psiMethod != null && strings.contains(psiMethod.getName())) {
                  extBeans.add(externalBean);
                }
              }
            }
          }
        }
      }
    }
    return extBeans;
  }

  public static boolean isExternalBean(final PsiMethod psiMethod) {
    return getExternalBean(psiMethod) != null;
  }

  @Nullable
  public static SpringJavaExternalBean getExternalBean(final PsiMethod psiMethod) {
    final Module module = ModuleUtil.findModuleForPsiElement(psiMethod);
    if (module != null) {
      for (SpringJavaConfiguration javaConfiguration : SpringJamModel.getModel(module).getConfigurations()) {
        if (javaConfiguration instanceof JavaConfigConfiguration) {
          if (psiMethod.getContainingFile().equals(javaConfiguration.getPsiClass().getContainingFile())) {
            for (SpringJavaExternalBean externalBean : ((JavaConfigConfiguration)javaConfiguration).getExternalBeans()) {
              if (psiMethod.equals(externalBean.getPsiElement())) {
                return externalBean;
              }
            }
          }
        }
      }
    }
    return null;
  }
}
