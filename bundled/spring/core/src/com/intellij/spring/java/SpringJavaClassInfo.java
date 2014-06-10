package com.intellij.spring.java;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.spring.SpringManager;
import com.intellij.spring.SpringModel;
import com.intellij.spring.model.xml.DomSpringBean;
import com.intellij.spring.model.xml.beans.DomSpringBeanPointer;
import com.intellij.spring.model.xml.beans.SpringBaseBeanPointer;
import com.intellij.spring.model.xml.beans.SpringBean;
import com.intellij.spring.model.xml.beans.SpringPropertyDefinition;
import com.intellij.util.containers.ConcurrentMultiMap;
import com.intellij.util.containers.MultiMap;
import com.intellij.util.xml.DomManager;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Caches Spring mappings for given PsiClass.
 *
 * @author Dmitry Avdeev
 */
public class SpringJavaClassInfo {

  private static final Key<SpringJavaClassInfo> KEY = new Key<SpringJavaClassInfo>("Spring Java Class Info");

  private final PsiClass myPsiClass;
  private final CachedValue<List<DomSpringBeanPointer>> myBeans;
  private final CachedValue<MultiMap<String, SpringPropertyDefinition>> myProperties;

  private SpringJavaClassInfo(final PsiClass psiClass) {

    myPsiClass = psiClass;
    final Project project = psiClass.getProject();

    myBeans = PsiManager.getInstance(project).getCachedValuesManager().createCachedValue(new CachedValueProvider<List<DomSpringBeanPointer>>() {
      public Result<List<DomSpringBeanPointer>> compute() {
        final Module module = ModuleUtil.findModuleForPsiElement(myPsiClass);
        if (module == null) {
          return null;
        }
        final List<DomSpringBeanPointer> result = new ArrayList<DomSpringBeanPointer>();
        ModuleUtil.visitMeAndDependentModules(module, new ModuleUtil.ModuleVisitor() {
          public boolean visit(final Module module) {
            final SpringModel model = SpringManager.getInstance(project).getCombinedModel(module);
            if (model != null) {
              final List<SpringBaseBeanPointer> list = model.findBeansByEffectivePsiClassWithInheritance(myPsiClass);
              for (SpringBaseBeanPointer pointer : list) {
                if (pointer instanceof DomSpringBeanPointer) {
                  result.add((DomSpringBeanPointer)pointer);
                }
              }
              return true;
            }
            return true;
          }
        });
        return new Result<List<DomSpringBeanPointer>>(result, DomManager.getDomManager(project));
      }
    }, false);

    myProperties = PsiManager.getInstance(project).getCachedValuesManager().createCachedValue(new CachedValueProvider<MultiMap<String, SpringPropertyDefinition>>() {
      public Result<MultiMap<String, SpringPropertyDefinition>> compute() {
        final List<DomSpringBeanPointer> list = getMappedBeans();
        final MultiMap<String, SpringPropertyDefinition> map = new ConcurrentMultiMap<String, SpringPropertyDefinition>() ;
        for (DomSpringBeanPointer beanPointer : list) {
          final DomSpringBean bean = beanPointer.getSpringBean();
          if (bean instanceof SpringBean) {
            final List<SpringPropertyDefinition> properties = ((SpringBean)bean).getAllProperties();
            for (SpringPropertyDefinition property : properties) {
              final String propertyName = property.getPropertyName();
              if (propertyName != null) {
                map.putValue(propertyName, property);
              }
            }
          }
        }
        return new Result<MultiMap<String, SpringPropertyDefinition>>(map, DomManager.getDomManager(project));
      }
    }, false);
  }

  @NotNull
  public static SpringJavaClassInfo getSpringJavaClassInfo(final @NotNull PsiClass psiClass) {
    SpringJavaClassInfo info = psiClass.getUserData(KEY);
    if (info == null) {
      info = new SpringJavaClassInfo(psiClass);
      psiClass.putUserData(KEY, info);
    }
    return info;
  }

  public boolean isMapped() {
    return getMappedBeans().size() > 0;
  }

  @NotNull
  public List<DomSpringBeanPointer> getMappedBeans() {
    final List<DomSpringBeanPointer> list = myBeans.getValue();
    return list == null ? Collections.<DomSpringBeanPointer>emptyList() : list;
  }

  @NotNull
  public Collection<SpringPropertyDefinition> getMappedProperties(String propertyName) {
    final MultiMap<String, SpringPropertyDefinition> value = myProperties.getValue();
    if (value == null) {
      return Collections.emptyList();
    }
    return value.get(propertyName);
  }
}
